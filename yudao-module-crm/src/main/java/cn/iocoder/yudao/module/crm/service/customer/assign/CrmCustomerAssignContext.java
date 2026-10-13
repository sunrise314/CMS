package cn.iocoder.yudao.module.crm.service.customer.assign;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 公海智能分配的批次上下文
 *
 * 一次分配任务中，统计数据（持有客户数、成交率、片区绑定、昵称）只查一次库，批内共享；
 * 每分出一个客户，通过 {@link #onAssigned(Long)} 在内存里累加计数，
 * 保证同一批客户之间的负载均衡依然是准确的（否则批内会把客户全分给同一个人）
 *
 * @author 芋道源码
 */
@Getter
@RequiredArgsConstructor
public class CrmCustomerAssignContext {

    /**
     * 候选人用户编号列表
     */
    private final List<Long> candidateUserIds;
    /**
     * 候选人持有客户统计，key = userId
     */
    private final Map<Long, CrmCustomerOwnerStatsBO> ownerStatsMap;
    /**
     * 候选人的片区绑定列表，key = userId
     */
    private final Map<Long, List<Integer>> areaBindingsMap;
    /**
     * 候选人昵称，key = userId
     */
    private final Map<Long, String> nicknameMap;

    /**
     * 获得候选人的昵称，用于生成可解释的分配原因
     */
    public String getNickname(Long userId) {
        return nicknameMap.getOrDefault(userId, String.valueOf(userId));
    }

    /**
     * 获得候选人的持有客户统计。没有记录（一个客户都没持有）时，返回 0/0
     */
    public CrmCustomerOwnerStatsBO getOwnerStats(Long userId) {
        return ownerStatsMap.getOrDefault(userId, new CrmCustomerOwnerStatsBO(0, 0));
    }

    /**
     * 获得候选人的片区绑定列表。没有绑定片区时，返回空列表
     */
    public List<Integer> getAreaBindings(Long userId) {
        return areaBindingsMap.getOrDefault(userId, java.util.Collections.emptyList());
    }

    /**
     * 某个候选人被分配成功一个客户后，批内持有计数 +1
     */
    public void onAssigned(Long userId) {
        CrmCustomerOwnerStatsBO stats = ownerStatsMap.computeIfAbsent(userId, k -> new CrmCustomerOwnerStatsBO(0, 0));
        stats.setTotalCount(stats.getTotalCount() + 1);
    }

    /**
     * 生成可解释的分配原因
     */
    public String formatReason(String template, Object... args) {
        return StrUtil.format(template, args);
    }

}
