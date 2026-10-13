package cn.iocoder.yudao.module.crm.service.customer.assign;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.assign.CrmCustomerAssignAreaSaveReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.assign.CrmCustomerAssignRecordPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.assign.CrmCustomerAssignRuleSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerAssignAreaDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerAssignRecordDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerAssignRuleDO;

import java.util.List;

/**
 * 客户公海智能分配 Service 接口
 *
 * @author 芋道源码
 */
public interface CrmCustomerAssignService {

    /**
     * 获得公海智能分配规则
     *
     * @return 分配规则，可能为空（从未配置）
     */
    CrmCustomerAssignRuleDO getAssignRule();

    /**
     * 保存公海智能分配规则（单行配置，存在则更新）
     *
     * @param saveReqVO 规则信息
     */
    void saveAssignRule(CrmCustomerAssignRuleSaveReqVO saveReqVO);

    /**
     * 获得用户绑定的片区列表
     *
     * @param userId 用户编号
     * @return 片区绑定列表
     */
    List<CrmCustomerAssignAreaDO> getAssignAreaListByUserId(Long userId);

    /**
     * 保存用户绑定的片区列表（全删全插）
     *
     * @param saveReqVO 片区绑定信息
     */
    void updateAssignArea(CrmCustomerAssignAreaSaveReqVO saveReqVO);

    /**
     * 获得公海智能分配记录分页
     *
     * @param pageReqVO 分页查询
     * @return 分配记录分页
     */
    PageResult<CrmCustomerAssignRecordDO> getAssignRecordPage(CrmCustomerAssignRecordPageReqVO pageReqVO);

    /**
     * 执行一轮公海智能分配：把公海中的客户按策略分配给候选人
     *
     * @return 成功分配的客户数量
     */
    int autoAssignCustomerPool();

}
