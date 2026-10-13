package cn.iocoder.yudao.module.crm.service.clue.score;

import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueScoreDO;

import java.util.List;

/**
 * CRM 线索评分 Service
 *
 * @author mashangan
 */
public interface CrmClueScoreService {

    /**
     * 对单条线索重新评分
     */
    CrmClueScoreDO scoreOne(Long clueId, String triggerType);

    /**
     * 批量评分（查所有未转化线索，逐一分）
     *
     * @return 本次评了多少条
     */
    int scoreAll(String triggerType);

    /**
     * 查某个线索当前最新评分 + 明细
     */
    ScoreView getCurrentScore(Long clueId);

    /**
     * 评分视图（主表 + 明细）
     */
    class ScoreView {
        public final CrmClueScoreDO score;
        public final List<cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueScoreDetailDO> details;
        public ScoreView(CrmClueScoreDO score, List<cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueScoreDetailDO> details) {
            this.score = score;
            this.details = details;
        }
    }

}
