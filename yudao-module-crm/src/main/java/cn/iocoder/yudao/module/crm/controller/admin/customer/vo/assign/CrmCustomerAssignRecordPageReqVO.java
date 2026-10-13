package cn.iocoder.yudao.module.crm.controller.admin.customer.vo.assign;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * CRM 客户公海智能分配记录分页 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - CRM 客户公海智能分配记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class CrmCustomerAssignRecordPageReqVO extends PageParam {

    @Schema(description = "客户编号", example = "1")
    private Long customerId;

    @Schema(description = "新负责人用户编号", example = "1")
    private Long postOwnerUserId;

}
