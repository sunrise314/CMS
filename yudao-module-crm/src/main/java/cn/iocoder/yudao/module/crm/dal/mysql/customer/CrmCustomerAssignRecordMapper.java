package cn.iocoder.yudao.module.crm.dal.mysql.customer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.assign.CrmCustomerAssignRecordPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerAssignRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户公海智能分配记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CrmCustomerAssignRecordMapper extends BaseMapperX<CrmCustomerAssignRecordDO> {

    default PageResult<CrmCustomerAssignRecordDO> selectPage(CrmCustomerAssignRecordPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<CrmCustomerAssignRecordDO>()
                .eqIfPresent(CrmCustomerAssignRecordDO::getCustomerId, pageReqVO.getCustomerId())
                .eqIfPresent(CrmCustomerAssignRecordDO::getPostOwnerUserId, pageReqVO.getPostOwnerUserId())
                .orderByDesc(CrmCustomerAssignRecordDO::getId));
    }

}
