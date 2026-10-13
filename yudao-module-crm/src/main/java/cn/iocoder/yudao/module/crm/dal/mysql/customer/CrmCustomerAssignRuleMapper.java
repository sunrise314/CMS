package cn.iocoder.yudao.module.crm.dal.mysql.customer;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerAssignRuleDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户公海智能分配规则 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CrmCustomerAssignRuleMapper extends BaseMapperX<CrmCustomerAssignRuleDO> {

    default CrmCustomerAssignRuleDO selectOne() {
        return selectOne(new LambdaQueryWrapperX<CrmCustomerAssignRuleDO>().last("LIMIT 1"));
    }

}
