package cn.iocoder.yudao.module.crm.service.clue.score;

import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueDO;
import cn.iocoder.yudao.module.crm.service.clue.score.bo.ClueRuleScoreResult;

/**
 * CRM 线索评分 — 规则接口
 *
 * 策略模式：每条规则一个实现，互不干扰。要加新规则，写一个新 Bean + 新枚举，Service 一行不动。
 *
 * @author mashangan
 */
public interface ClueScoreRule {

    /** 规则代码，对应 crm_score_rule_config.rule_code */
    String getCode();

    /** 规则中文名，用于落明细 */
    String getName();

    /**
     * 执行评分
     *
     * @param clue    线索
     * @param maxScore 本规则满分（从 crm_score_rule_config.weight 取）
     * @return 本规则得分 + 命中理由（可解释性）
     */
    ClueRuleScoreResult evaluate(CrmClueDO clue, int maxScore);

}
