package cn.iocoder.yudao.module.crm.controller.admin.customer.vo.assign;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * CRM 客户公海智能分配片区绑定 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - CRM 客户公海智能分配片区绑定 Response VO")
@Data
@Accessors(chain = true)
public class CrmCustomerAssignAreaRespVO {

    @Schema(description = "用户编号", example = "1")
    private Long userId;

    @Schema(description = "片区编号列表", example = "[110000, 110100]")
    private List<Integer> areaIds;

}
