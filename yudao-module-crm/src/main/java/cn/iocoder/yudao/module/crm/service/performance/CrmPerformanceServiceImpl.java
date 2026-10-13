package cn.iocoder.yudao.module.crm.service.performance;

import cn.iocoder.yudao.module.crm.controller.admin.performance.vo.PerformanceIndustryRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.performance.vo.PerformanceRankRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.performance.vo.PerformanceTrendRespVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.performance.CrmPerformanceSnapshotDO;
import cn.iocoder.yudao.module.crm.dal.mysql.performance.CrmPerformanceSnapshotMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CRM 业绩看板 ServiceImpl
 *
 * 数据源：crm_contract（已审批通过、按 owner_user_id + audit_status=3 筛选）
 * 聚合方式：按销售 + 月份做全量快照（避免实时计算扫大表）
 *
 * @author mashangan
 */
@Service
@Slf4j
public class CrmPerformanceServiceImpl implements CrmPerformanceService {

    @Resource
    private CrmPerformanceSnapshotMapper snapshotMapper;

    // 从芋道 Module 直接依赖
    // 芋道有 contractMapper，但通过 Service 调用即可

    // ==================== 对外 API ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int aggregateAll() {
        // 1. 清所有快照（全量刷新策略）
        snapshotMapper.delete(new LambdaQueryWrapper<CrmPerformanceSnapshotDO>()
                .gt(CrmPerformanceSnapshotDO::getSnapshotDate, LocalDate.of(2000, 1, 1)));
        // 2. 写空数据占位——真实项目会从 crm_contract/crm_business 聚合
        //    这里先插示例数据保证看板能跑起来
        List<CrmPerformanceSnapshotDO> demos = buildDemoSnapshots();
        for (CrmPerformanceSnapshotDO s : demos) {
            snapshotMapper.insert(s);
        }
        log.info("[aggregateAll] 完成，写入 {} 条快照（示例数据）", demos.size());
        return demos.size();
    }

    @Override
    public List<PerformanceRankRespVO> rank(String month) {
        // month 格式 "YYYY-MM"
        List<CrmPerformanceSnapshotDO> rows = snapshotMapper.selectList(
                new LambdaQueryWrapper<CrmPerformanceSnapshotDO>()
                        .eq(CrmPerformanceSnapshotDO::getDimension, "MONTH")
                        .eq(CrmPerformanceSnapshotDO::getDimensionKey, month));
        // 聚合按 owner_user_id 汇总
        Map<Long, AggRow> byUser = new HashMap<>();
        for (CrmPerformanceSnapshotDO r : rows) {
            byUser.computeIfAbsent(r.getOwnerUserId(), k -> new AggRow())
                    .merge(r);
        }
        // 排序
        List<Map.Entry<Long, AggRow>> sorted = byUser.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().contractAmount, a.getValue().contractAmount))
                .collect(Collectors.toList());
        int rank = 1;
        List<PerformanceRankRespVO> result = new ArrayList<>();
        for (Map.Entry<Long, AggRow> e : sorted) {
            PerformanceRankRespVO vo = new PerformanceRankRespVO();
            vo.setUserId(e.getKey());
            vo.setUserName("销售#" + e.getKey());  // 真实实现查 admin_user_api
            vo.setContractCount(e.getValue().contractCount);
            vo.setContractAmount(e.getValue().contractAmount);
            vo.setBusinessCount(e.getValue().businessCount);
            vo.setCustomerCount(e.getValue().customerCount);
            vo.setRank(rank++);
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<PerformanceTrendRespVO> trend(int months) {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusMonths(months).withDayOfMonth(1);
        List<CrmPerformanceSnapshotDO> rows = snapshotMapper.selectList(
                new LambdaQueryWrapper<CrmPerformanceSnapshotDO>()
                        .eq(CrmPerformanceSnapshotDO::getDimension, "MONTH")
                        .ge(CrmPerformanceSnapshotDO::getSnapshotDate, from));
        // 按月份汇总
        Map<String, AggRow> byMonth = new TreeMap<>();
        for (CrmPerformanceSnapshotDO r : rows) {
            byMonth.computeIfAbsent(r.getDimensionKey(), k -> new AggRow()).merge(r);
        }
        List<PerformanceTrendRespVO> result = new ArrayList<>();
        for (Map.Entry<String, AggRow> e : byMonth.entrySet()) {
            PerformanceTrendRespVO vo = new PerformanceTrendRespVO();
            vo.setMonth(e.getKey());
            vo.setContractCount(e.getValue().contractCount);
            vo.setContractAmount(e.getValue().contractAmount);
            vo.setBusinessCount(e.getValue().businessCount);
            vo.setCustomerCount(e.getValue().customerCount);
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<PerformanceIndustryRespVO> industry(String month) {
        List<CrmPerformanceSnapshotDO> rows = snapshotMapper.selectList(
                new LambdaQueryWrapper<CrmPerformanceSnapshotDO>()
                        .eq(CrmPerformanceSnapshotDO::getDimension, "MONTH")
                        .eq(CrmPerformanceSnapshotDO::getDimensionKey, month));
        Map<String, AggRow> byInd = new HashMap<>();
        for (CrmPerformanceSnapshotDO r : rows) {
            byInd.computeIfAbsent(r.getDimensionKey(), k -> new AggRow()).merge(r);
        }
        long total = byInd.values().stream().mapToLong(a -> a.contractAmount).sum();
        List<PerformanceIndustryRespVO> result = new ArrayList<>();
        for (Map.Entry<String, AggRow> e : byInd.entrySet()) {
            PerformanceIndustryRespVO vo = new PerformanceIndustryRespVO();
            vo.setIndustryId(e.getKey());
            vo.setContractCount(e.getValue().contractCount);
            vo.setContractAmount(e.getValue().contractAmount);
            vo.setPercent(total > 0 ? (int) (e.getValue().contractAmount * 100 / total) : 0);
            result.add(vo);
        }
        result.sort((a, b) -> Long.compare(b.getContractAmount(), a.getContractAmount()));
        return result;
    }

    // ==================== 私有 ====================

    private static class AggRow {
        int contractCount;
        long contractAmount;
        int businessCount;
        int customerCount;
        void merge(CrmPerformanceSnapshotDO r) {
            contractCount += r.getContractCount();
            contractAmount += r.getContractAmount();
            businessCount += r.getBusinessCount();
            customerCount += r.getCustomerCount();
        }
    }

    /** 示例数据：造 3 个销售、4 个月的月度快照 */
    private List<CrmPerformanceSnapshotDO> buildDemoSnapshots() {
        List<CrmPerformanceSnapshotDO> list = new ArrayList<>();
        LocalDate today = LocalDate.now();
        String[] months = new String[4];
        for (int i = 3; i >= 0; i--) {
            months[3 - i] = today.minusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        long[][] amounts = {
                // user 1,2,3  ×  4 months
                {300000L, 450000L, 380000L, 520000L},  // user 1
                {200000L, 260000L, 310000L, 290000L},  // user 2
                {150000L, 180000L, 120000L, 220000L},  // user 3
        };
        int[][] contracts = {
                {3, 5, 4, 6},
                {2, 3, 4, 3},
                {2, 2, 1, 3},
        };
        int[][] businesses = {
                {8, 12, 10, 15},
                {6, 8, 9, 7},
                {5, 6, 4, 8},
        };
        int[][] customers = {
                {5, 8, 7, 10},
                {4, 5, 6, 5},
                {3, 4, 3, 5},
        };
        for (int u = 0; u < 3; u++) {
            for (int m = 0; m < 4; m++) {
                String month = months[m];
                LocalDate monthStart = LocalDate.parse(month + "-01");
                list.add(CrmPerformanceSnapshotDO.builder()
                        .snapshotDate(today)
                        .ownerUserId((long) (u + 1))
                        .dimension("MONTH")
                        .dimensionKey(month)
                        .contractCount(contracts[u][m])
                        .contractAmount(amounts[u][m])
                        .businessCount(businesses[u][m])
                        .customerCount(customers[u][m])
                        .build());
                // 行业维度（key 带月份，避免唯一键冲突）
                list.add(CrmPerformanceSnapshotDO.builder()
                        .snapshotDate(today)
                        .ownerUserId((long) (u + 1))
                        .dimension("INDUSTRY")
                        .dimensionKey((u + 1) + "-" + month)
                        .contractCount(contracts[u][m])
                        .contractAmount(amounts[u][m])
                        .businessCount(businesses[u][m])
                        .customerCount(customers[u][m])
                        .build());
            }
        }
        return list;
    }

}
