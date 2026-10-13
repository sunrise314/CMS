package cn.iocoder.yudao.module.crm.service.clue.score.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单条规则的评分结果 BO
 *
 * @author mashangan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClueRuleScoreResult {

    /** 本规则得分，0~maxScore */
    private int score;

    /** 本规则满分 */
    private int maxScore;

    /** 命中等级 HIGH / MEDIUM / LOW / NONE */
    private String hitLevel;

    /** 命中理由（可解释性，前端直接展示） */
    private String reason;

    /**
     * 快速构造
     */
    public static ClueRuleScoreResult of(int score, int maxScore, String hitLevel, String reason) {
        return ClueRuleScoreResult.builder()
                .score(Math.min(Math.max(score, 0), maxScore))
                .maxScore(maxScore)
                .hitLevel(hitLevel)
                .reason(reason)
                .build();
    }

}
