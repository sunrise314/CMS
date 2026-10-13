package cn.iocoder.yudao.module.crm.service.performance;

import cn.iocoder.yudao.module.crm.controller.admin.performance.vo.PerformanceRankRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.performance.vo.PerformanceTrendRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.performance.vo.PerformanceIndustryRespVO;

import java.util.List;

/**
 * CRM 业绩看板 Service
 *
 * @author mashangan
 */
public interface CrmPerformanceService {

    /**
     * 全量聚合（扫 crm_contract + crm_business + crm_customer → 写 crm_performance_snapshot）
     * 定时任务每日调一次
     *
     * @return 写入的快照行数
     */
    int aggregateAll();

    /** 销售业绩排行（按月） */
    List<PerformanceRankRespVO> rank(String month);

    /** 月度趋势（最近 N 个月） */
    List<PerformanceTrendRespVO> trend(int months);

    /** 行业分布（按月） */
    List<PerformanceIndustryRespVO> industry(String month);

}
