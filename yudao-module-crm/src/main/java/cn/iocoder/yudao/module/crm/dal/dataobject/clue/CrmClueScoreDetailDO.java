package cn.iocoder.yudao.module.crm.dal.dataobject.clue;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * CRM 线索评分明细 DO
 *
 * 可解释性的核心：每次评分的每条规则命中一条记录。
 * 前端点"为什么打这个分"时，把这些明细摊开就行。
 *
 * @author mashangan
 */
@TableName("crm_clue_score_detail")
@KeySequence("crm_clue_score_detail_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmClueScoreDetailDO extends BaseDO {

    /** 主键 */
    @TableId
    private Long id;
    /** 评分主表 ID */
    private Long scoreId;
    /** 规则代码 SOURCE / INDUSTRY / LEVEL / CONTACT / ACTIVITY / LLM */
    private String ruleCode;
    /** 规则中文名 */
    private String ruleName;
    /** 命中等级 HIGH / MEDIUM / LOW / NONE */
    private String hitLevel;
    /** 本规则得分 */
    private Integer score;
    /** 本规则满分 */
    private Integer maxScore;
    /** 命中理由（可解释性） */
    private String reason;
    /** 排序 */
    private Integer sort;

}
