package cn.iocoder.yudao.module.crm.dal.mysql.performance;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.performance.CrmPerformanceSnapshotDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * CRM 业绩快照 Mapper
 *
 * @author mashangan
 */
@Mapper
public interface CrmPerformanceSnapshotMapper extends BaseMapperX<CrmPerformanceSnapshotDO> {

    /** 删指定日期之前的快照（全量刷新用） */
    default int deleteBySnapshotDateBefore(LocalDate before) {
        return delete(CrmPerformanceSnapshotDO::getSnapshotDate, before);
    }

    /** 查某销售某月的月度快照（看板按月查） */
    default List<CrmPerformanceSnapshotDO> selectMonthlyByOwnerAndRange(
            Long ownerUserId, LocalDate fromDate, LocalDate toDate) {
        return selectList(new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<CrmPerformanceSnapshotDO>()
                .eq(CrmPerformanceSnapshotDO::getOwnerUserId, ownerUserId)
                .eq(CrmPerformanceSnapshotDO::getDimension, "MONTH")
                .between(CrmPerformanceSnapshotDO::getSnapshotDate, fromDate, toDate));
    }

}
