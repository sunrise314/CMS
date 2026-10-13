package cn.iocoder.yudao.module.crm.service.clue.score.rule;

import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueDO;
import cn.iocoder.yudao.module.crm.service.clue.score.ClueScoreRule;
import cn.iocoder.yudao.module.crm.service.clue.score.bo.ClueRuleScoreResult;
import org.springframework.stereotype.Component;

/**
 * 规则 1：来源优先级
 *
 * 字典 crm_customer_source：
 *   1 官网表单 → 100%；7 老客户推荐 → 90%；2 展会 → 80%；3 渠道 → 70%；
 *   6 朋友介绍 → 60%；4 广告 → 50%；5 电话/9 社交 → 40%；8 搜索 → 30%；10 其他 → 20%
 *
 * @author mashangan
 */
@Component
public class SourceScoreRule implements ClueScoreRule {

    @Override
    public String getCode() { return "SOURCE"; }

    @Override
    public String getName() { return "来源优先级"; }

    @Override
    public ClueRuleScoreResult evaluate(CrmClueDO clue, int maxScore) {
        Integer source = clue.getSource();
        if (source == null) {
            return ClueRuleScoreResult.of(0, maxScore, "NONE", "未填写客户来源，无法判断意向质量");
        }
        int pct;
        String reason;
        switch (source) {
            case 1:  pct = 100; reason = "官网主动提交，意向质量最高"; break;
            case 7:  pct = 90;  reason = "老客户推荐，信任背书强"; break;
            case 2:  pct = 80;  reason = "展会现场触达，有初步兴趣"; break;
            case 3:  pct = 70;  reason = "渠道合作方推荐"; break;
            case 6:  pct = 60;  reason = "朋友介绍"; break;
            case 4:  pct = 50;  reason = "广告投放引流"; break;
            case 5:  pct = 40;  reason = "电话营销呼出"; break;
            case 9:  pct = 40;  reason = "社交媒体获取"; break;
            case 8:  pct = 30;  reason = "搜索引擎获取"; break;
            case 10: pct = 20;  reason = "其他来源"; break;
            default: pct = 20; reason = "其他来源";
        }
        int score = Math.round(maxScore * pct / 100.0f);
        String hitLevel = pct >= 80 ? "HIGH" : pct >= 50 ? "MEDIUM" : pct >= 30 ? "LOW" : "NONE";
        return ClueRuleScoreResult.of(score, maxScore, hitLevel, reason);
    }

}
