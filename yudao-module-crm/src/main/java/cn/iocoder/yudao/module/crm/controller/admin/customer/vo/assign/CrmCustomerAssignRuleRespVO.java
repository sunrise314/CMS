package cn.iocoder.yudao.module.crm.controller.admin.customer.vo.assign;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * CRM 客户公海智能分配规则 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - CRM 客户公海智能分配规则 Response VO")
@Data
public class CrmCustomerAssignRuleRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "是否启用智能分配", example = "true")
    private Boolean enabled;

    @Schema(description = "分配策略", example = "1")
    private Integer strategy;

    @Schema(description = "候选人用户编号列表，逗号分隔", example = "1,2,3")
    private String candidateUserIds;

}
