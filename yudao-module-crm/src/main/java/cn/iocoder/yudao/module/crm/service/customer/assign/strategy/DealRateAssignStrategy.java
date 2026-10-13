package cn.iocoder.yudao.module.crm.service.customer.assign.strategy;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.enums.customer.CrmCustomerAssignStrategyEnum;
import cn.iocoder.yudao.module.crm.service.customer.assign.CrmCustomerAssignContext;
import cn.iocoder.yudao.module.crm.service.customer.assign.CrmCustomerAssignStrategy;
import cn.iocoder.yudao.module.crm.service.customer.assign.CrmCustomerAssignStrategyResult;
import cn.iocoder.yudao.module.crm.service.customer.assign.CrmCustomerOwnerStatsBO;
import org.springframework.stereotype.Component;

/**
 * 成单率优先策略：把客户分给当前成交率最高的销售
 *
 * 适合"线索贵、转化难"的场景：好线索优先给最能打的人
 * 注意：没有持有客户的销售成交率按 0 计算，避免新人拿不到客户（可结合负载均衡策略混用）
 *
 * @author 芋道源码
 */
@Component
public class DealRateAssignStrategy implements CrmCustomerAssignStrategy {

    @Override
    public CrmCustomerAssignStrategyEnum getType() {
        return CrmCustomerAssignStrategyEnum.DEAL_RATE;
    }

    @Override
    public CrmCustomerAssignStrategyResult selectOwner(CrmCustomerDO customer, CrmCustomerAssignContext context) {
        // 1. 遍历候选人，找到成交率最高的人（相同成交率时，保持候选人列表顺序，保证结果确定性）
        Long bestUserId = null;
        double bestRate = -1;
        CrmCustomerOwnerStatsBO bestStats = null;
        for (Long userId : context.getCandidateUserIds()) {
            CrmCustomerOwnerStatsBO stats = context.getOwnerStats(userId);
            double rate = stats.getDealRate();
            if (rate > bestRate) {
                bestRate = rate;
                bestUserId = userId;
                bestStats = stats;
            }
        }
        // 2. 没有候选人，无法分配
        if (bestUserId == null) {
            return new CrmCustomerAssignStrategyResult(null, "候选人列表为空，无法分配");
        }
        // 3. 生成可解释的分配原因
        String reason = StrUtil.format("成单率：【{}】成交率 {}%（成交 {}/持有 {}），为候选人中最高",
                context.getNickname(bestUserId),
                String.format("%.1f", bestStats.getDealRate() * 100),
                bestStats.getDealCount(), bestStats.getTotalCount());
        return new CrmCustomerAssignStrategyResult(bestUserId, reason);
    }

}
