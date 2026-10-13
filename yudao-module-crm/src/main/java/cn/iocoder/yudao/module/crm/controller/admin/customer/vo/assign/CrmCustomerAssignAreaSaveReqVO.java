package cn.iocoder.yudao.module.crm.controller.admin.customer.vo.assign;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * CRM 客户公海智能分配片区绑定保存 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - CRM 客户公海智能分配片区绑定保存 Request VO")
@Data
public class CrmCustomerAssignAreaSaveReqVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @Schema(description = "片区编号列表，空列表代表清空绑定", example = "[110000, 110100]")
    private List<Integer> areaIds;

}
