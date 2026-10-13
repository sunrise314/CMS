package cn.iocoder.yudao.module.crm.dal.dataobject.performance;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;

/**
 * CRM 业绩快照 DO
 *
 * 每日全量聚合，避免实时计算扫大表。看板查这个表。
 *
 * @author mashangan
 */
@TableName("crm_performance_snapshot")
@KeySequence("crm_performance_snapshot_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmPerformanceSnapshotDO extends BaseDO {

    /** 主键 */
    @TableId
    private Long id;
    /** 快照日期 */
    private LocalDate snapshotDate;
    /** 销售负责人 */
    private Long ownerUserId;
    /** 维度 MONTH / QUARTER / YEAR / INDUSTRY */
    private String dimension;
    /** 维度值（月份 "2026-10" / 行业代码 "1" 等） */
    private String dimensionKey;
    /** 合同数 */
    private Integer contractCount;
    /** 合同总金额（分） */
    private Long contractAmount;
    /** 商机数 */
    private Integer businessCount;
    /** 客户数 */
    private Integer customerCount;

}
