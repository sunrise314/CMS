package cn.iocoder.yudao.module.crm.controller.admin.customer;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.assign.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerAssignAreaDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerAssignRecordDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerAssignRuleDO;
import cn.iocoder.yudao.module.crm.service.customer.assign.CrmCustomerAssignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * CRM 客户公海智能分配 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - CRM 客户公海智能分配")
@RestController
@RequestMapping("/crm/customer-assign")
@Validated
public class CrmCustomerAssignController {

    @Resource
    private CrmCustomerAssignService customerAssignService;

    @GetMapping("/rule")
    @Operation(summary = "获得公海智能分配规则")
    @PreAuthorize("@ss.hasPermission('crm:customer:query')")
    public CommonResult<CrmCustomerAssignRuleRespVO> getAssignRule() {
        CrmCustomerAssignRuleDO rule = customerAssignService.getAssignRule();
        return success(BeanUtils.toBean(rule, CrmCustomerAssignRuleRespVO.class));
    }

    @PutMapping("/rule")
    @Operation(summary = "保存公海智能分配规则")
    @PreAuthorize("@ss.hasPermission('crm:customer:update')")
    public CommonResult<Boolean> saveAssignRule(@Valid @RequestBody CrmCustomerAssignRuleSaveReqVO saveReqVO) {
        customerAssignService.saveAssignRule(saveReqVO);
        return success(true);
    }

    @GetMapping("/area/list")
    @Operation(summary = "获得用户绑定的片区列表")
    @Parameter(name = "userId", description = "用户编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('crm:customer:query')")
    public CommonResult<CrmCustomerAssignAreaRespVO> getAssignAreaList(@RequestParam("userId") Long userId) {
        List<CrmCustomerAssignAreaDO> areas = customerAssignService.getAssignAreaListByUserId(userId);
        return success(new CrmCustomerAssignAreaRespVO().setUserId(userId)
                .setAreaIds(convertList(areas, CrmCustomerAssignAreaDO::getAreaId)));
    }

    @PutMapping("/area")
    @Operation(summary = "保存用户绑定的片区列表")
    @PreAuthorize("@ss.hasPermission('crm:customer:update')")
    public CommonResult<Boolean> updateAssignArea(@Valid @RequestBody CrmCustomerAssignAreaSaveReqVO saveReqVO) {
        customerAssignService.updateAssignArea(saveReqVO);
        return success(true);
    }

    @PostMapping("/trigger")
    @Operation(summary = "手动触发一轮公海智能分配")
    @PreAuthorize("@ss.hasPermission('crm:customer:update')")
    public CommonResult<Integer> triggerAssign() {
        return success(customerAssignService.autoAssignCustomerPool());
    }

    @GetMapping("/record/page")
    @Operation(summary = "获得公海智能分配记录分页")
    @PreAuthorize("@ss.hasPermission('crm:customer:query')")
    public CommonResult<PageResult<CrmCustomerAssignRecordRespVO>> getAssignRecordPage(@Valid CrmCustomerAssignRecordPageReqVO pageReqVO) {
        PageResult<CrmCustomerAssignRecordDO> pageResult = customerAssignService.getAssignRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, CrmCustomerAssignRecordRespVO.class));
    }

}
