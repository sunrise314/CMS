package cn.iocoder.yudao.module.crm.dal.mysql.clue;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueScoreDetailDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * CRM 线索评分明细 Mapper
 *
 * @author mashangan
 */
@Mapper
public interface CrmClueScoreDetailMapper extends BaseMapperX<CrmClueScoreDetailDO> {

    default List<CrmClueScoreDetailDO> selectByScoreId(Long scoreId) {
        return selectList(CrmClueScoreDetailDO::getScoreId, scoreId);
    }

}
