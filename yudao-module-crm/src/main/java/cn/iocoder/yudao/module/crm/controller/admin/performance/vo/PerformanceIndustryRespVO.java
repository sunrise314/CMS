package cn.iocoder.yudao.module.crm.controller.admin.performance.vo;

import lombok.Data;

/**
 * 行业业绩分布 VO
 *
 * @author mashangan
 */
@Data
public class PerformanceIndustryRespVO {

    /** 行业代码 */
    private String industryId;
    /** 行业合同数 */
    private Integer contractCount;
    /** 行业合同金额（分） */
    private Long contractAmount;
    /** 占总金额比例（0-100） */
    private Integer percent;

}
