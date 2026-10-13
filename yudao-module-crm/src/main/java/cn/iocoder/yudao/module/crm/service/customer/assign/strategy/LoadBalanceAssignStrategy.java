package cn.iocoder.yudao.module.crm.service.customer.assign.strategy;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.enums.customer.CrmCustomerAssignStrategyEnum;
import cn.iocoder.yudao.module.crm.service.customer.assign.CrmCustomerAssignContext;
import cn.iocoder.yudao.module.crm.service.customer.assign.CrmCustomerAssignStrategy;
import cn.iocoder.yudao.module.crm.service.customer.assign.CrmCustomerAssignStrategyResult;
import org.springframework.stereotype.Component;

/**
 * 负载均衡优先策略：把客户分给当前持有客户数最少的销售
 *
 * 解决原版公海"抢单忙闲不均"的痛点：能力强的销售疯狂抢单，新人抢不到客户
 *
 * @author 芋道源码
 */
@Component
public class LoadBalanceAssignStrategy implements CrmCustomerAssignStrategy {

    @Override
    public CrmCustomerAssignStrategyEnum getType() {
        return CrmCustomerAssignStrategyEnum.LOAD_BALANCE;
    }

    @Override
    public CrmCustomerAssignStrategyResult selectOwner(CrmCustomerDO customer, CrmCustomerAssignContext context) {
        // 1. 遍历候选人，找到持有客户数最少的人（批内计数已实时更新，同一批内不会全分给同一个人）
        Long bestUserId = null;
        long bestCount = Long.MAX_VALUE;
        for (Long userId : context.getCandidateUserIds()) {
            long count = context.getOwnerStats(userId).getTotalCount();
            if (count < bestCount) {
                bestCount = count;
                bestUserId = userId;
            }
        }
        // 2. 没有候选人，无法分配
        if (bestUserId == null) {
            return new CrmCustomerAssignStrategyResult(null, "候选人列表为空，无法分配");
        }
        // 3. 生成可解释的分配原因
        String reason = StrUtil.format("负载均衡：【{}】当前持有 {} 个客户，为候选人中最少",
                context.getNickname(bestUserId), bestCount);
        return new CrmCustomerAssignStrategyResult(bestUserId, reason);
    }

}
