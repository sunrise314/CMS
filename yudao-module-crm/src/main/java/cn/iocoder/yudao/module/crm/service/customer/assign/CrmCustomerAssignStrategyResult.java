package cn.iocoder.yudao.module.crm.service.customer.assign;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公海智能分配的策略结果 BO
 *
 * @author 芋道源码
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrmCustomerAssignStrategyResult {

    /**
     * 选中的负责人用户编号
     *
     * 为空时，代表当前策略无法做出决定，交由调用方回退到负载均衡策略
     */
    private Long ownerUserId;
    /**
     * 分配原因，保证每一次分配可解释、可追溯
     */
    private String reason;

}
