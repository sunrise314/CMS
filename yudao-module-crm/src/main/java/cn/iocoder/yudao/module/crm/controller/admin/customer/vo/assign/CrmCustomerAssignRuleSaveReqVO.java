package cn.iocoder.yudao.module.crm.controller.admin.customer.vo.assign;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * CRM 客户公海智能分配规则保存 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - CRM 客户公海智能分配规则保存 Request VO")
@Data
public class CrmCustomerAssignRuleSaveReqVO {

    @Schema(description = "是否启用智能分配", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否启用智能分配不能为空")
    private Boolean enabled;

    @Schema(description = "分配策略", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "分配策略不能为空")
    private Integer strategy;

    @Schema(description = "候选人用户编号列表，逗号分隔", example = "1,2,3")
    private String candidateUserIds;

}
