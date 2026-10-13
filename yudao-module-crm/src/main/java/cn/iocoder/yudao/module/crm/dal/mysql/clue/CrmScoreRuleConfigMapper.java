package cn.iocoder.yudao.module.crm.dal.mysql.clue;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmScoreRuleConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * CRM 评分规则配置 Mapper
 *
 * @author mashangan
 */
@Mapper
public interface CrmScoreRuleConfigMapper extends BaseMapperX<CrmScoreRuleConfigDO> {

    default List<CrmScoreRuleConfigDO> selectEnabled() {
        return selectList(CrmScoreRuleConfigDO::getEnabled, true);
    }

}
