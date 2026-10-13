package cn.iocoder.yudao.module.crm.controller.admin.performance.vo;

import lombok.Data;

/**
 * 销售业绩排行 VO
 *
 * @author mashangan
 */
@Data
public class PerformanceRankRespVO {

    /** 销售 ID */
    private Long userId;
    /** 销售姓名 */
    private String userName;
    /** 合同数 */
    private Integer contractCount;
    /** 合同金额（分） */
    private Long contractAmount;
    /** 商机数 */
    private Integer businessCount;
    /** 客户数 */
    private Integer customerCount;
    /** 排名 */
    private Integer rank;

}
