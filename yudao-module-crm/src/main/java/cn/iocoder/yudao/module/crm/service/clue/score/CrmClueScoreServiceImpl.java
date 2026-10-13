package cn.iocoder.yudao.module.crm.service.clue.score;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.crm.dal.dataobject.clue.*;
import cn.iocoder.yudao.module.crm.dal.mysql.clue.*;
import cn.iocoder.yudao.module.crm.service.clue.score.bo.ClueRuleScoreResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

/**
 * CRM 线索评分 ServiceImpl
 *
 * 核心流程：
 *   1. Spring 注入所有 ClueScoreRule Bean，按 getCode() 建 Map
 *   2. 从 crm_score_rule_config 加载权重（5 条规则，weight 之和 = 100）
 *   3. 对一条线索：逐规则 evaluate → 汇总 → 分级 → 落主表 + 明细
 *   4. 回写 crm_clue.current_score / current_grade（冗余字段，查询优化）
 *
 * 为什么要版本号？
 *   运营调整权重/改规则逻辑后，需要重新评分但保留历史评分记录。
 *   每次 scoreOne 会 selectNextVersion，version++。
 *
 * @author mashangan
 */
@Service
@Slf4j
public class CrmClueScoreServiceImpl implements CrmClueScoreService {

    @Resource private CrmScoreRuleConfigMapper scoreRuleConfigMapper;
    @Resource private CrmClueMapper clueMapper;
    @Resource private CrmClueScoreMapper scoreMapper;
    @Resource private CrmClueScoreDetailMapper detailMapper;
    @Resource private CrmClueLlmScoreService llmScoreService;

    /** Spring 自动注入所有 ClueScoreRule 实现 */
    @Resource private List<ClueScoreRule> rules;

    /** 规则 Map：code → 规则实例 */
    private final Map<String, ClueScoreRule> ruleMap = new LinkedHashMap<>();
    /** 规则 Map：code → 权重（满分） */
    private final Map<String, Integer> weightMap = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        for (ClueScoreRule r : rules) {
            ruleMap.put(r.getCode(), r);
        }
        reloadWeights();
        log.info("[CrmClueScore] 评分引擎就绪，规则数={}, 配置={}", ruleMap.size(), weightMap);
    }

    public void reloadWeights() {
        weightMap.clear();
        for (CrmScoreRuleConfigDO cfg : scoreRuleConfigMapper.selectEnabled()) {
            weightMap.put(cfg.getRuleCode(), cfg.getWeight());
        }
    }

    // ==================== 对外 API ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmClueScoreDO scoreOne(Long clueId, String triggerType) {
        CrmClueDO clue = clueMapper.selectById(clueId);
        if (clue == null) {
            log.warn("[scoreOne] 线索不存在: clueId={}", clueId);
            return null;
        }

        // 1. 逐规则 evaluate
        List<RuleHit> hits = evaluateAll(clue);

        // 2. 汇总各维度得分
        int sourceScore   = hitOf(hits, "SOURCE").getScore();
        int industryScore = hitOf(hits, "INDUSTRY").getScore();
        int levelScore    = hitOf(hits, "LEVEL").getScore();
        int contactScore  = hitOf(hits, "CONTACT").getScore();
        int activityScore = hitOf(hits, "ACTIVITY").getScore();
        int ruleTotal = sourceScore + industryScore + levelScore + contactScore + activityScore;
        int total = ruleTotal;
        String grade = gradeOf(total);

        // 2.5 LLM 语义评分（双轨融合：规则分占 (100-w)%，LLM 分占 w%）
        //     未启用 / 未配 key / 调用失败 → 自动降级为纯规则分，评分流程不中断
        Integer llmScore = null;
        String llmReason = null;
        if (llmScoreService.isReady()) {
            try {
                CrmClueLlmScoreService.LlmScore llm = llmScoreService.score(clue);
                if (llm != null) {
                    int w = llmScoreService.getWeightPercent();
                    llmScore = llm.getScore();
                    llmReason = llm.getReason();
                    total = (ruleTotal * (100 - w) + llmScore * w) / 100;
                    grade = gradeOf(total);
                    log.info("[scoreOne] clueId={} LLM 评分={}，融合: 规则{}×{}% + LLM{}×{}% = {}",
                            clueId, llmScore, ruleTotal, 100 - w, llmScore, w, total);
                }
            } catch (Exception e) {
                log.warn("[scoreOne] clueId={} LLM 评分失败，降级为纯规则分: {}", clueId, e.getMessage());
            }
        }

        // 3. 存主表
        int version = scoreMapper.selectNextVersion(clueId);
        CrmClueScoreDO score = CrmClueScoreDO.builder()
                .clueId(clueId)
                .version(version)
                .sourceScore(sourceScore)
                .industryScore(industryScore)
                .levelScore(levelScore)
                .contactScore(contactScore)
                .activityScore(activityScore)
                .totalScore(total)
                .grade(grade)
                .triggerType(triggerType)
                .llmScore(llmScore)
                .llmReason(llmReason)
                .build();
        scoreMapper.insert(score);

        // 4. 存明细（可解释性的核心）
        int sort = 0;
        for (RuleHit h : hits) {
            detailMapper.insert(CrmClueScoreDetailDO.builder()
                    .scoreId(score.getId())
                    .ruleCode(h.rule.getCode())
                    .ruleName(h.rule.getName())
                    .hitLevel(h.result.getHitLevel())
                    .score(h.result.getScore())
                    .maxScore(h.result.getMaxScore())
                    .reason(h.result.getReason())
                    .sort(sort++)
                    .build());
        }
        // 4.5 LLM 明细行（score 存加权贡献值，原始 0-100 分看主表 llm_score）
        if (llmScore != null) {
            int w = llmScoreService.getWeightPercent();
            detailMapper.insert(CrmClueScoreDetailDO.builder()
                    .scoreId(score.getId())
                    .ruleCode("LLM")
                    .ruleName("LLM 语义评估")
                    .hitLevel(llmScore >= 80 ? "HIGH" : llmScore >= 60 ? "MEDIUM" : llmScore >= 40 ? "LOW" : "NONE")
                    .score(llmScore * w / 100)
                    .maxScore(w)
                    .reason(llmReason)
                    .sort(sort++)
                    .build());
        }

        // 5. 回写 crm_clue 冗余字段（查询优化）
        clueMapper.update(null, new LambdaUpdateWrapper<CrmClueDO>()
                .eq(CrmClueDO::getId, clueId)
                .set(CrmClueDO::getCurrentScore, total)
                .set(CrmClueDO::getCurrentGrade, grade));

        log.info("[scoreOne] clueId={} → score={}, grade={}, version={}, trigger={}",
                clueId, total, grade, version, triggerType);
        return score;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int scoreAll(String triggerType) {
        // 查所有未转化线索
        List<CrmClueDO> clues = clueMapper.selectList(new LambdaQueryWrapper<CrmClueDO>()
                .and(w -> w.eq(CrmClueDO::getTransformStatus, false)
                        .or().isNull(CrmClueDO::getTransformStatus)));
        if (CollUtil.isEmpty(clues)) {
            log.info("[scoreAll] 没有待评分的线索");
            return 0;
        }
        log.info("[scoreAll] 开始批量评分，共 {} 条，trigger={}", clues.size(), triggerType);
        int ok = 0;
        for (CrmClueDO clue : clues) {
            try {
                scoreOne(clue.getId(), triggerType);
                ok++;
            } catch (Exception e) {
                log.error("[scoreAll] clueId={} 评分失败", clue.getId(), e);
            }
        }
        log.info("[scoreAll] 完成，成功={}/{}", ok, clues.size());
        return ok;
    }

    @Override
    public ScoreView getCurrentScore(Long clueId) {
        CrmClueScoreDO score = scoreMapper.selectLatestByClueId(clueId);
        if (score == null) return null;
        List<CrmClueScoreDetailDO> details = detailMapper.selectByScoreId(score.getId());
        return new ScoreView(score, details);
    }

    // ==================== 私有 ====================

    /** 规则命中记录（rule + result 绑定在一起） */
    private static class RuleHit {
        final ClueScoreRule rule;
        final ClueRuleScoreResult result;
        RuleHit(ClueScoreRule rule, ClueRuleScoreResult result) {
            this.rule = rule; this.result = result;
        }
        int getScore() { return result.getScore(); }
    }

    private List<RuleHit> evaluateAll(CrmClueDO clue) {
        List<RuleHit> hits = new ArrayList<>();
        for (ClueScoreRule rule : rules) {
            int maxScore = weightMap.getOrDefault(rule.getCode(), 0);
            hits.add(new RuleHit(rule, rule.evaluate(clue, maxScore)));
        }
        return hits;
    }

    private RuleHit hitOf(List<RuleHit> hits, String code) {
        return hits.stream().filter(h -> h.rule.getCode().equals(code)).findFirst()
                .orElse(new RuleHit(null, ClueRuleScoreResult.of(0, 0, "NONE", "规则未配置")));
    }

    private String gradeOf(int total) {
        if (total >= 80) return "A";
        if (total >= 60) return "B";
        if (total >= 40) return "C";
        return "D";
    }

}
