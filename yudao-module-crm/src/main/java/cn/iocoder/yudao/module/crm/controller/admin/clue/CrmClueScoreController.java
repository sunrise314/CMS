package cn.iocoder.yudao.module.crm.controller.admin.clue;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueScoreDO;
import cn.iocoder.yudao.module.crm.service.clue.score.CrmClueScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * CRM 线索评分 Controller
 *
 * @author mashangan
 */
@Tag(name = "管理后台 - CRM 线索评分")
@RestController
@RequestMapping("/crm/clue-score")
@Validated
public class CrmClueScoreController {

    @Resource
    private CrmClueScoreService clueScoreService;

    @PostMapping("/score-one")
    @Operation(summary = "对单条线索执行评分")
    @Parameter(name = "clueId", description = "线索 ID", required = true)
    public CommonResult<CrmClueScoreDO> scoreOne(@RequestParam("clueId") Long clueId) {
        return success(clueScoreService.scoreOne(clueId, "MANUAL"));
    }

    @PostMapping("/score-all")
    @Operation(summary = "批量评分所有未转化线索")
    public CommonResult<Integer> scoreAll() {
        return success(clueScoreService.scoreAll("MANUAL"));
    }

    @GetMapping("/current")
    @Operation(summary = "查某个线索当前最新评分 + 明细")
    @Parameter(name = "clueId", description = "线索 ID", required = true)
    public CommonResult<CrmClueScoreService.ScoreView> getCurrent(@RequestParam("clueId") Long clueId) {
        return success(clueScoreService.getCurrentScore(clueId));
    }

}
