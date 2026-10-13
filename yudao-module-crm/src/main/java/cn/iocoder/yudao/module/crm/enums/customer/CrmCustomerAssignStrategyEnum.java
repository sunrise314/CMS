package cn.iocoder.yudao.module.crm.enums.customer;

import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * CRM 客户公海智能分配策略枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum CrmCustomerAssignStrategyEnum {

    /**
     * 负载均衡优先：把客户分给当前持有客户数最少的销售
     */
    LOAD_BALANCE(1, "负载均衡优先"),
    /**
     * 成单率优先：把客户分给当前成交率最高的销售
     */
    DEAL_RATE(2, "成单率优先"),
    /**
     * 片区优先：把客户分给绑定了客户所在片区的销售，无匹配时回退负载均衡
     */
    AREA(3, "片区优先");

    /**
     * 策略值
     */
    private final Integer strategy;
    /**
     * 策略名
     */
    private final String name;

    public static CrmCustomerAssignStrategyEnum valueOf(Integer strategy) {
        return ArrayUtil.firstMatch(item -> item.getStrategy().equals(strategy), values());
    }

    public static boolean isValid(Integer strategy) {
        return Arrays.stream(values()).anyMatch(item -> item.getStrategy().equals(strategy));
    }

}
