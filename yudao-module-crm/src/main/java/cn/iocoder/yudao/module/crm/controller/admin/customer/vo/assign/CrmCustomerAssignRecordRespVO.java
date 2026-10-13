package cn.iocoder.yudao.module.crm.controller.admin.customer.vo.assign;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * CRM 客户公海智能分配记录 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - CRM 客户公海智能分配记录 Response VO")
@Data
public class CrmCustomerAssignRecordRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "客户编号", example = "1")
    private Long customerId;

    @Schema(description = "客户名称", example = "XX 科技")
    private String customerName;

    @Schema(description = "原负责人用户编号，空代表来自公海", example = "1")
    private Long preOwnerUserId;

    @Schema(description = "新负责人用户编号", example = "2")
    private Long postOwnerUserId;

    @Schema(description = "分配策略", example = "1")
    private Integer strategy;

    @Schema(description = "分配原因", example = "负载均衡：【张三】当前持有 2 个客户，为候选人中最少")
    private String reason;

    @Schema(description = "分配时间")
    private LocalDateTime createTime;

}
