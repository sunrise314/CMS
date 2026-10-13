package cn.iocoder.yudao.module.crm.service.clue.score.rule;

import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueDO;
import cn.iocoder.yudao.module.crm.service.clue.score.ClueScoreRule;
import cn.iocoder.yudao.module.crm.service.clue.score.bo.ClueRuleScoreResult;
import org.springframework.stereotype.Component;

/**
 * 规则 4：联系方式完整度
 *
 * 手机 / 电话 / email / 微信 四项，有几项算几项。
 * 四项齐全 → 满 20 分；三项 → 15；两项 → 10；一项 → 5；零 → 0
 *
 * @author mashangan
 */
@Component
public class ContactScoreRule implements ClueScoreRule {

    @Override
    public String getCode() { return "CONTACT"; }

    @Override
    public String getName() { return "联系方式完整度"; }

    @Override
    public ClueRuleScoreResult evaluate(CrmClueDO clue, int maxScore) {
        int count = 0;
        if (clue.getMobile() != null && !clue.getMobile().isBlank()) count++;
        if (clue.getTelephone() != null && !clue.getTelephone().isBlank()) count++;
        if (clue.getEmail() != null && !clue.getEmail().isBlank()) count++;
        if (clue.getWechat() != null && !clue.getWechat().isBlank()) count++;

        int[] scoreByCount = {0, 5, 10, 15, maxScore};
        int score = scoreByCount[Math.min(count, 4)];
        String hitLevel = count >= 4 ? "HIGH" : count >= 2 ? "MEDIUM" : count >= 1 ? "LOW" : "NONE";
        String reason = count == 0
                ? "未填写任何联系方式，无法联系"
                : String.format("已填写 %d 项联系方式，覆盖度 %d%%",
                        count, Math.round(count * 25f));
        return ClueRuleScoreResult.of(score, maxScore, hitLevel, reason);
    }

}
