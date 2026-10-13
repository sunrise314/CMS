package cn.iocoder.yudao.module.crm.service.clue.score.rule;

import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueDO;
import cn.iocoder.yudao.module.crm.service.clue.score.ClueScoreRule;
import cn.iocoder.yudao.module.crm.service.clue.score.bo.ClueRuleScoreResult;
import org.springframework.stereotype.Component;

/**
 * 规则 2：行业适配度
 *
 * 字典 crm_customer_industry：1 IT 100% / 2 制造 90% / 3 金融 85% / 4 医疗 70%
 * / 5 零售 65% / 6 教育 55% / 7 房产 50% / 8 其他 40%
 *
 * @author mashangan
 */
@Component
public class IndustryScoreRule implements ClueScoreRule {

    @Override
    public String getCode() { return "INDUSTRY"; }

    @Override
    public String getName() { return "行业适配度"; }

    @Override
    public ClueRuleScoreResult evaluate(CrmClueDO clue, int maxScore) {
        Integer ind = clue.getIndustryId();
        if (ind == null) {
            int pct = 40;
            return ClueRuleScoreResult.of(maxScore * pct / 100, maxScore, "LOW", "未填写行业，按通用行业处理");
        }
        int pct;
        String reason;
        switch (ind) {
            case 1: pct = 100; reason = "IT 互联网是我司核心覆盖行业"; break;
            case 2: pct = 90;  reason = "制造业，有较强信息化需求"; break;
            case 3: pct = 85;  reason = "金融业，客单价高"; break;
            case 4: pct = 70;  reason = "医疗行业"; break;
            case 5: pct = 65;  reason = "零售/电商行业"; break;
            case 6: pct = 55;  reason = "教育行业"; break;
            case 7: pct = 50;  reason = "房地产行业"; break;
            case 8: pct = 40;  reason = "其他行业"; break;
            default: pct = 40; reason = "其他行业";
        }
        int score = Math.round(maxScore * pct / 100.0f);
        String hitLevel = pct >= 80 ? "HIGH" : pct >= 60 ? "MEDIUM" : "LOW";
        return ClueRuleScoreResult.of(score, maxScore, hitLevel, reason);
    }

}
