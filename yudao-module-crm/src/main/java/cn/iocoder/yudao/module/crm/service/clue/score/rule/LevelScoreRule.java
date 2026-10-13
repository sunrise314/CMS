package cn.iocoder.yudao.module.crm.service.clue.score.rule;

import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueDO;
import cn.iocoder.yudao.module.crm.service.clue.score.ClueScoreRule;
import cn.iocoder.yudao.module.crm.service.clue.score.bo.ClueRuleScoreResult;
import org.springframework.stereotype.Component;

/**
 * 规则 3：客户等级
 *
 * 字典 crm_customer_level：1 A级 100% / 2 B级 70% / 3 C级 40% / null 0分
 *
 * @author mashangan
 */
@Component
public class LevelScoreRule implements ClueScoreRule {

    @Override
    public String getCode() { return "LEVEL"; }

    @Override
    public String getName() { return "客户等级"; }

    @Override
    public ClueRuleScoreResult evaluate(CrmClueDO clue, int maxScore) {
        Integer level = clue.getLevel();
        if (level == null) {
            return ClueRuleScoreResult.of(0, maxScore, "NONE", "线索尚未定级，跟进后请及时评估等级");
        }
        int pct;
        String gradeLabel;
        switch (level) {
            case 1: pct = 100; gradeLabel = "A 级"; break;
            case 2: pct = 70;  gradeLabel = "B 级"; break;
            case 3: pct = 40;  gradeLabel = "C 级"; break;
            default: pct = 30; gradeLabel = "未知等级";
        }
        int score = Math.round(maxScore * pct / 100.0f);
        String hitLevel = pct >= 80 ? "HIGH" : pct >= 50 ? "MEDIUM" : "LOW";
        return ClueRuleScoreResult.of(score, maxScore, hitLevel, "线索已评定为 " + gradeLabel);
    }

}
