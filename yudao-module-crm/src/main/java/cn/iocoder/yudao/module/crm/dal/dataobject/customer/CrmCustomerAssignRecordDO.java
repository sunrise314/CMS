package cn.iocoder.yudao.module.crm.dal.dataobject.customer;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

/**
 * 客户公海智能分配记录 DO
 *
 * 每一次智能分配都会留痕，记录当时为什么分给 TA，保证分配结果可追溯、可解释
 *
 * @author 芋道源码
 */
@TableName(value = "crm_customer_assign_record")
@KeySequence("crm_customer_assign_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmCustomerAssignRecordDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 客户编号
     */
    private Long customerId;
    /**
     * 客户名称（冗余，避免追溯时还要关联查询）
     */
    private String customerName;
    /**
     * 原负责人用户编号
     *
     * 为空时，代表客户来自公海
     */
    private Long preOwnerUserId;
    /**
     * 新负责人用户编号
     */
    private Long postOwnerUserId;
    /**
     * 分配策略
     *
     * 枚举 {@link cn.iocoder.yudao.module.crm.enums.customer.CrmCustomerAssignStrategyEnum}
     */
    private Integer strategy;
    /**
     * 分配原因（可解释性）
     */
    private String reason;

}
