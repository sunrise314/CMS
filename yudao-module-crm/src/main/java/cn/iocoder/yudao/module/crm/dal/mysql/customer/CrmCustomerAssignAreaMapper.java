package cn.iocoder.yudao.module.crm.dal.mysql.customer;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerAssignAreaDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 客户公海智能分配片区绑定 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CrmCustomerAssignAreaMapper extends BaseMapperX<CrmCustomerAssignAreaDO> {

    default List<CrmCustomerAssignAreaDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<CrmCustomerAssignAreaDO>()
                .eq(CrmCustomerAssignAreaDO::getUserId, userId));
    }

    default List<CrmCustomerAssignAreaDO> selectListByUserIds(Collection<Long> userIds) {
        return selectList(new LambdaQueryWrapperX<CrmCustomerAssignAreaDO>()
                .in(CrmCustomerAssignAreaDO::getUserId, userIds));
    }

    default void deleteByUserId(Long userId) {
        delete(new LambdaQueryWrapperX<CrmCustomerAssignAreaDO>()
                .eq(CrmCustomerAssignAreaDO::getUserId, userId));
    }

}
