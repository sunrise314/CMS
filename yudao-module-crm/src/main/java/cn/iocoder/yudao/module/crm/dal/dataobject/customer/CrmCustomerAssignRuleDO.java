package cn.iocoder.yudao.module.crm.dal.dataobject.customer;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

/**
 * 客户公海智能分配规则 DO
 *
 * @author 芋道源码
 */
@TableName(value = "crm_customer_assign_rule")
@KeySequence("crm_customer_assign_rule_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmCustomerAssignRuleDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 是否启用智能分配
     */
    private Boolean enabled;
    /**
     * 分配策略
     *
     * 枚举 {@link cn.iocoder.yudao.module.crm.enums.customer.CrmCustomerAssignStrategyEnum}
     */
    private Integer strategy;
    /**
     * 候选人用户编号列表，逗号分隔
     *
     * 为空时，代表不进行分配（避免全量用户误伤）
     */
    private String candidateUserIds;

}
