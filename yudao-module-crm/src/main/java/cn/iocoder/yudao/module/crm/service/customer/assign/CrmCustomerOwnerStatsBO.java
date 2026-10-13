package cn.iocoder.yudao.module.crm.service.customer.assign;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 销售持有客户的统计 BO
 *
 * @author 芋道源码
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrmCustomerOwnerStatsBO {

    /**
     * 持有客户总数
     */
    private long totalCount;
    /**
     * 其中成交客户数
     */
    private long dealCount;

    /**
     * 成交率，保留两位小数。持有客户数为 0 时，成交率为 0
     */
    public double getDealRate() {
        if (totalCount <= 0) {
            return 0.0;
        }
        return (double) dealCount / totalCount;
    }

}
