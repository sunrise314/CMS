package cn.iocoder.yudao.module.crm.service.clue.score.rule;

import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueDO;
import cn.iocoder.yudao.module.crm.service.clue.score.ClueScoreRule;
import cn.iocoder.yudao.module.crm.service.clue.score.bo.ClueRuleScoreResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 规则 5：跟进活跃度
 *
 * - followUpStatus = true（已跟进）且最近 7 天内有跟进 → 满 20 分
 * - followUpStatus = true 但超过 30 天没跟进 → 8 分
 * - followUpStatus = false（从未跟进）→ 0 分
 *
 * @author mashangan
 */
@Component
public class ActivityScoreRule implements ClueScoreRule {

    @Override
    public String getCode() { return "ACTIVITY"; }

    @Override
    public String getName() { return "跟进活跃度"; }

    @Override
    public ClueRuleScoreResult evaluate(CrmClueDO clue, int maxScore) {
        Boolean followed = clue.getFollowUpStatus();
        if (followed == null || !followed) {
            return ClueRuleScoreResult.of(0, maxScore, "NONE", "线索从未被跟进过，销售需尽快触达");
        }
        LocalDateTime lastContact = clue.getContactLastTime();
        if (lastContact == null) {
            return ClueRuleScoreResult.of(maxScore * 50 / 100, maxScore, "MEDIUM",
                    "标记已跟进但无最后跟进时间，按中间档处理");
        }
        long days = ChronoUnit.DAYS.between(lastContact, LocalDateTime.now());
        if (days <= 7) {
            return ClueRuleScoreResult.of(maxScore, maxScore, "HIGH",
                    String.format("最近 %d 天内有跟进动作，销售关注度高", days));
        } else if (days <= 30) {
            int score = Math.round(maxScore * 60 / 100f);
            return ClueRuleScoreResult.of(score, maxScore, "MEDIUM",
                    String.format("最近 %d 天前跟进过，需继续推进", days));
        } else {
            int score = Math.round(maxScore * 30 / 100f);
            return ClueRuleScoreResult.of(score, maxScore, "LOW",
                    String.format("%d 天未跟进，线索有流失风险", days));
        }
    }

}
