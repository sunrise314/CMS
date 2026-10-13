package cn.iocoder.yudao.module.crm.dal.mysql.clue;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueScoreDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * CRM 线索评分结果 Mapper
 *
 * @author mashangan
 */
@Mapper
public interface CrmClueScoreMapper extends BaseMapperX<CrmClueScoreDO> {

    /**
     * 查某个线索当前最新版本的评分
     */
    default CrmClueScoreDO selectLatestByClueId(Long clueId) {
        return selectOne(new LambdaQueryWrapperX<CrmClueScoreDO>()
                .eq(CrmClueScoreDO::getClueId, clueId)
                .orderByDesc(CrmClueScoreDO::getVersion)
                .last("LIMIT 1"));
    }

    /**
     * 查某个线索下一个可用版本号
     */
    default int selectNextVersion(Long clueId) {
        CrmClueScoreDO latest = selectLatestByClueId(clueId);
        return latest == null ? 1 : latest.getVersion() + 1;
    }

}
