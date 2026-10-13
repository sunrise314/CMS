package cn.iocoder.yudao.module.crm.service.customer.assign;

import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.enums.customer.CrmCustomerAssignStrategyEnum;

/**
 * 客户公海智能分配的策略接口
 *
 * 新增分配策略时，实现该接口并注册为 Spring Bean 即可，无需修改分配流水线的任何代码
 *
 * @author 芋道源码
 */
public interface CrmCustomerAssignStrategy {

    /**
     * @return 策略类型
     */
    CrmCustomerAssignStrategyEnum getType();

    /**
     * 为指定客户选择负责人
     *
     * @param customer 客户
     * @param context  批次上下文，提供候选人、统计、片区绑定等共享数据
     * @return 策略结果：负责人 + 分配原因。负责人为空时，代表策略无法决定，调用方回退负载均衡
     */
    CrmCustomerAssignStrategyResult selectOwner(CrmCustomerDO customer, CrmCustomerAssignContext context);

}
