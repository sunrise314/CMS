package cn.iocoder.yudao.module.crm.service.clue.score;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * CRM 线索评分 — LLM 语义评分服务
 *
 * 调用 OpenAI 兼容接口（默认 DeepSeek）对线索备注等非结构化文本打分。
 * 设计原则：
 *   1. 未配置 api-key 或 enabled=false → isReady()=false，主流程自动跳过
 *   2. HTTP 失败 / 解析失败 / 超时 → 抛异常，由调用方 catch 后降级为纯规则分
 *   3. 只输出严格 JSON，便于程序解析；温度调低保证稳定性
 *
 * @author mashangan
 */
@Service
@Slf4j
public class CrmClueLlmScoreService {

    @Value("${crm.score.llm.enabled:false}")
    private boolean enabled;
    @Value("${crm.score.llm.base-url:https://api.deepseek.com}")
    private String baseUrl;
    @Value("${crm.score.llm.api-key:}")
    private String apiKey;
    @Value("${crm.score.llm.model:deepseek-chat}")
    private String model;
    @Value("${crm.score.llm.timeout-ms:30000}")
    private int timeoutMs;
    /** LLM 分在总分中的占比（0~100），其余由规则分承担 */
    @Value("${crm.score.llm.weight:30}")
    private int weightPercent;

    /** 是否具备调用条件（配置齐全） */
    public boolean isReady() {
        return enabled && apiKey != null && !apiKey.trim().isEmpty();
    }

    public int getWeightPercent() {
        return weightPercent;
    }

    /** LLM 评分结果 */
    public static class LlmScore {
        private final int score;
        private final String reason;
        public LlmScore(int score, String reason) {
            this.score = score;
            this.reason = reason;
        }
        public int getScore() { return score; }
        public String getReason() { return reason; }
    }

    /**
     * 对单条线索执行 LLM 语义评分
     *
     * @return 评分结果；服务未就绪返回 null；调用失败抛异常（调用方负责降级）
     */
    public LlmScore score(CrmClueDO clue) {
        if (!isReady()) {
            return null;
        }
        String content = callChat(buildSystemPrompt(), buildUserPrompt(clue));
        return parseScore(content);
    }

    // ==================== 提示词 ====================

    private String buildSystemPrompt() {
        return "你是 B2B 销售团队的线索质量评估助手。根据线索信息评估其成交潜力，输出 0-100 的整数分。\n"
                + "评估重点：备注中透露的购买信号（预算、时间表、决策权、竞品对比、明确需求描述）。\n"
                + "备注信息越具体、信号越强，分数越高；备注为空或无实质内容时给中性偏低的分数。\n"
                + "严格只输出 JSON，不要输出任何其他文字，格式：{\"score\": 0-100整数, \"reason\": \"一句话理由\"}";
    }

    private String buildUserPrompt(CrmClueDO clue) {
        StringBuilder sb = new StringBuilder();
        sb.append("线索名称：").append(nullSafe(clue.getName())).append('\n');
        sb.append("客户来源代码：").append(clue.getSource() == null ? "未填写" : clue.getSource()).append('\n');
        sb.append("行业代码：").append(clue.getIndustryId() == null ? "未填写" : clue.getIndustryId()).append('\n');
        sb.append("客户等级代码：").append(clue.getLevel() == null ? "未定级" : clue.getLevel()).append('\n');
        sb.append("备注原文：").append(nullSafe(clue.getRemark())).append('\n');
        sb.append("跟进状态：").append(Boolean.TRUE.equals(clue.getFollowUpStatus()) ? "已跟进" : "未跟进").append('\n');
        return sb.toString();
    }

    private String nullSafe(String s) {
        return s == null || s.trim().isEmpty() ? "（无）" : s;
    }

    // ==================== HTTP 调用 ====================

    private String callChat(String systemPrompt, String userPrompt) {
        JSONObject req = new JSONObject();
        req.set("model", model);
        req.set("temperature", 0.2);
        req.set("max_tokens", 200);
        req.set("messages", new JSONObject[]
                {
                        msg("system", systemPrompt),
                        msg("user", userPrompt)
                });

        String url = baseUrl + "/chat/completions";
        try (HttpResponse resp = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(req.toString())
                .timeout(timeoutMs)
                .execute()) {
            if (resp.getStatus() != 200) {
                throw new IllegalStateException("LLM 接口返回 " + resp.getStatus() + ": " + resp.body());
            }
            JSONObject body = JSONUtil.parseObj(resp.body());
            return body.getJSONArray("choices").getJSONObject(0)
                    .getJSONObject("message").getStr("content");
        }
    }

    private JSONObject msg(String role, String content) {
        JSONObject m = new JSONObject();
        m.set("role", role);
        m.set("content", content);
        return m;
    }

    // ==================== 结果解析 ====================

    /**
     * 解析 LLM 输出。容错处理：
     *   1. 剥掉 markdown 代码围栏（有的模型爱加 ```json）
     *   2. 截取第一个 { 到最后一个 } 之间的内容
     *   3. score 越界钳到 [0,100]
     */
    private LlmScore parseScore(String content) {
        if (content == null) {
            throw new IllegalStateException("LLM 返回内容为空");
        }
        String text = content.trim()
                .replace("```json", "").replace("```", "").trim();
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new IllegalStateException("LLM 输出中找不到 JSON: " + text);
        }
        JSONObject json = JSONUtil.parseObj(text.substring(start, end + 1));
        int score = json.getInt("score", 0);
        score = Math.max(0, Math.min(100, score));
        String reason = json.getStr("reason", "");
        return new LlmScore(score, reason);
    }

}
