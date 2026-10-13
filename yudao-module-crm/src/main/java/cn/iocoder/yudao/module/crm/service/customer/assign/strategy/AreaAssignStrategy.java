package cn.iocoder.yudao.module.crm.service.customer.assign.strategy;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.enums.customer.CrmCustomerAssignStrategyEnum;
import cn.iocoder.yudao.module.crm.service.customer.assign.CrmCustomerAssignContext;
import cn.iocoder.yudao.module.crm.service.customer.assign.CrmCustomerAssignStrategy;
import cn.iocoder.yudao.module.crm.service.customer.assign.CrmCustomerAssignStrategyResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 片区优先策略：把客户分给绑定了客户所在片区的销售
 *
 * 匹配规则：沿客户所在地的区域链（区县 → 市 → 省）向上匹配，命中"最具体"片区的候选人获胜；
 * 区域链上的任何一层命中都算匹配，例如绑定【浙江省】可以接到【杭州市】的客户。
 * 没有候选人绑定该片区的客户，返回空结果，由调用方回退到负载均衡策略，避免客户滞留公海
 *
 * @author 芋道源码
 */
@Component
public class AreaAssignStrategy implements CrmCustomerAssignStrategy {

    @Override
    public CrmCustomerAssignStrategyEnum getType() {
        return CrmCustomerAssignStrategyEnum.AREA;
    }

    @Override
    public CrmCustomerAssignStrategyResult selectOwner(CrmCustomerDO customer, CrmCustomerAssignContext context) {
        // 1. 客户未填写所在地，片区策略无从下手
        if (customer.getAreaId() == null) {
            return new CrmCustomerAssignStrategyResult(null,
                    StrUtil.format("客户【{}】未填写所在地，片区策略无法匹配", customer.getName()));
        }
        // 2. 计算客户所在地的区域链：区县 → 市 → 省 →（...），depth 越大代表越靠近省级
        Area area = AreaUtils.getArea(customer.getAreaId());
        if (area == null) {
            return new CrmCustomerAssignStrategyResult(null,
                    StrUtil.format("客户【{}】所在地编号({})非法，片区策略无法匹配", customer.getName(), customer.getAreaId()));
        }
        String customerAreaPath = AreaUtils.format(customer.getAreaId(), "/");
        Map<Integer, Integer> areaDepthMap = new HashMap<>(); // 区域编号 -> 链上深度（越大越粗粒度）
        int depth = 0;
        while (area != null) {
            areaDepthMap.put(area.getId(), depth++);
            area = area.getParent();
        }
        // 3. 在候选人中找片区绑定，取命中"最具体"（区县级 > 市级 > 省级）的人
        Long bestUserId = null;
        int bestDepth = -1;
        int bestBindingAreaId = 0;
        for (Long userId : context.getCandidateUserIds()) {
            for (Integer bindingAreaId : context.getAreaBindings(userId)) {
                Integer bindingDepth = areaDepthMap.get(bindingAreaId);
                if (bindingDepth != null && bestDepth < bindingDepth) {
                    bestDepth = bindingDepth;
                    bestUserId = userId;
                    bestBindingAreaId = bindingAreaId;
                }
            }
        }
        // 4. 没有候选人绑定该片区，回退负载均衡
        if (bestUserId == null) {
            return new CrmCustomerAssignStrategyResult(null,
                    StrUtil.format("客户所在地【{}】没有候选人绑定片区，回退负载均衡", customerAreaPath));
        }
        // 5. 生成可解释的分配原因
        String ownerNickname = context.getNickname(bestUserId);
        String bindingAreaPath = AreaUtils.format(bestBindingAreaId, "/");
        String reason = StrUtil.format("片区优先：客户所在地【{}】命中【{}】绑定的片区【{}】",
                customerAreaPath, ownerNickname, bindingAreaPath);
        return new CrmCustomerAssignStrategyResult(bestUserId, reason);
    }

}
