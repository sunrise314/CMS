package cn.iocoder.yudao.module.crm.controller.admin.performance.vo;

import lombok.Data;

/**
 * 月度业绩趋势 VO
 *
 * @author mashangan
 */
@Data
public class PerformanceTrendRespVO {

    /** 月份 YYYY-MM */
    private String month;
    /** 合同数 */
    private Integer contractCount;
    /** 合同金额（分） */
    private Long contractAmount;
    /** 商机数 */
    private Integer businessCount;
    /** 客户数 */
    private Integer customerCount;

}
