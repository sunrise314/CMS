package cn.iocoder.yudao.module.crm.controller.admin.performance;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.crm.controller.admin.performance.vo.PerformanceIndustryRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.performance.vo.PerformanceRankRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.performance.vo.PerformanceTrendRespVO;
import cn.iocoder.yudao.module.crm.service.performance.CrmPerformanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * CRM 业绩看板 Controller
 *
 * @author mashangan
 */
@Tag(name = "管理后台 - CRM 业绩看板")
@RestController
@RequestMapping("/crm/performance")
@Validated
public class CrmPerformanceController {

    @Resource
    private CrmPerformanceService performanceService;

    @PostMapping("/aggregate")
    @Operation(summary = "手动触发全量聚合（定时任务每日自动调）")
    public CommonResult<Integer> aggregate() {
        return success(performanceService.aggregateAll());
    }

    @GetMapping("/rank")
    @Operation(summary = "销售业绩排行")
    @Parameter(name = "month", description = "月份 YYYY-MM", example = "2026-10")
    public CommonResult<List<PerformanceRankRespVO>> rank(
            @RequestParam("month") String month) {
        return success(performanceService.rank(month));
    }

    @GetMapping("/trend")
    @Operation(summary = "月度业绩趋势")
    @Parameter(name = "months", description = "最近 N 个月", example = "6")
    public CommonResult<List<PerformanceTrendRespVO>> trend(
            @RequestParam(value = "months", defaultValue = "6") Integer months) {
        return success(performanceService.trend(months));
    }

    @GetMapping("/industry")
    @Operation(summary = "行业业绩分布")
    @Parameter(name = "month", description = "月份 YYYY-MM", example = "2026-10")
    public CommonResult<List<PerformanceIndustryRespVO>> industry(
            @RequestParam("month") String month) {
        return success(performanceService.industry(month));
    }

}
