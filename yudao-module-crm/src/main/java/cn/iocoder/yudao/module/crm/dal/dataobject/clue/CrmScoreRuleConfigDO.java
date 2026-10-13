package cn.iocoder.yudao.module.crm.dal.dataobject.clue;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * CRM 评分规则权重配置 DO
 *
 * 运营可调整，改完不用重启应用（Service 里 @PostConstruct + 定时刷新）。
 * 5 条规则的 weight 加起来 = 100。
 *
 * @author mashangan
 */
@TableName("crm_score_rule_config")
@KeySequence("crm_score_rule_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmScoreRuleConfigDO extends BaseDO {

    /** 主键 */
    @TableId
    private Long id;
    /** 规则代码 SOURCE / INDUSTRY / LEVEL / CONTACT / ACTIVITY */
    private String ruleCode;
    /** 规则中文名 */
    private String ruleName;
    /** 权重 */
    private Integer weight;
    /** 是否启用 */
    private Boolean enabled;
    /** 备注 */
    private String remark;

}
