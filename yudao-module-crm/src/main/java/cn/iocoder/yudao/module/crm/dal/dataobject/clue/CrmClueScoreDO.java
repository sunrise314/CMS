package cn.iocoder.yudao.module.crm.dal.dataobject.clue;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * CRM 线索评分结果 DO
 *
 * 每次评分生成一条记录，支持多次迭代（version 递增）。
 * 前端查询线索当前评分时取同一 clueId 下 version 最大的一条。
 *
 * @author mashangan
 */
@TableName("crm_clue_score")
@KeySequence("crm_clue_score_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmClueScoreDO extends BaseDO {

    /** 主键 */
    @TableId
    private Long id;
    /** 线索 ID */
    private Long clueId;
    /** 评分版本号 */
    private Integer version;

    /** 来源分（0~20） */
    private Integer sourceScore;
    /** 行业分（0~20） */
    private Integer industryScore;
    /** 等级分（0~20） */
    private Integer levelScore;
    /** 联系方式完整度（0~20） */
    private Integer contactScore;
    /** 跟进活跃度（0~20） */
    private Integer activityScore;

    /** LLM 语义分（0~100，规则引擎先跑，LLM 后跑，可能为空） */
    private Integer llmScore;

    /** 综合分（0~100，最终输出） */
    private Integer totalScore;
    /** 分级 A/B/C/D */
    private String grade;
    /** 触发方式 MANUAL / BATCH / LLM */
    private String triggerType;
    /** LLM 推理理由（LLM 评分时才填充） */
    private String llmReason;

}
