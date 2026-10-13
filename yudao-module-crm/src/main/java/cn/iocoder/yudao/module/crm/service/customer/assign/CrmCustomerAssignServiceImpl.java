package cn.iocoder.yudao.module.crm.service.customer.assign;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.assign.CrmCustomerAssignAreaSaveReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.assign.CrmCustomerAssignRecordPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.assign.CrmCustomerAssignRuleSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerAssignAreaDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerAssignRecordDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerAssignRuleDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.mysql.customer.CrmCustomerAssignAreaMapper;
import cn.iocoder.yudao.module.crm.dal.mysql.customer.CrmCustomerAssignRecordMapper;
import cn.iocoder.yudao.module.crm.dal.mysql.customer.CrmCustomerAssignRuleMapper;
import cn.iocoder.yudao.module.crm.dal.mysql.customer.CrmCustomerMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.customer.CrmCustomerAssignStrategyEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.service.permission.CrmOwnerRecordService;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmOwnerRecordCreateReqBO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CUSTOMER_ASSIGN_CANDIDATE_INVALID;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CUSTOMER_ASSIGN_STRATEGY_INVALID;

/**
 * 客户公海智能分配 Service 实现类
 *
 * 回收 - 分配流水线的"分配"半程：客户掉入公海后，由这里按策略分给人，
 * 替代原版"手动抢单"导致的忙闲不均
 *
 * @author 芋道源码
 */
@Service
@Slf4j
@Validated
public class CrmCustomerAssignServiceImpl implements CrmCustomerAssignService {

    @Resource
    private CrmCustomerAssignRuleMapper assignRuleMapper;
    @Resource
    private CrmCustomerAssignAreaMapper assignAreaMapper;
    @Resource
    private CrmCustomerAssignRecordMapper assignRecordMapper;
    @Resource
    private CrmCustomerMapper customerMapper;
    @Resource
    private CrmPermissionService permissionService;
    @Resource
    private CrmOwnerRecordService ownerRecordService;
    @Resource
    private AdminUserApi adminUserApi;

    /**
     * 策略集合，key = 策略值
     */
    private Map<Integer, CrmCustomerAssignStrategy> strategyMap;

    @Resource
    private List<CrmCustomerAssignStrategy> strategyList;

    @PostConstruct
    public void initStrategyMap() {
        strategyMap = CollectionUtils.convertMap(strategyList, strategy -> strategy.getType().getStrategy());
    }

    // ==================== 规则配置 ====================

    @Override
    public CrmCustomerAssignRuleDO getAssignRule() {
        return assignRuleMapper.selectOne();
    }

    @Override
    public void saveAssignRule(CrmCustomerAssignRuleSaveReqVO saveReqVO) {
        // 1.1 校验策略合法
        if (!CrmCustomerAssignStrategyEnum.isValid(saveReqVO.getStrategy())) {
            throw exception(CUSTOMER_ASSIGN_STRATEGY_INVALID);
        }
        // 1.2 启用时，候选人必须有配置，且都真实存在
        List<Long> candidateUserIds = parseCandidateUserIds(saveReqVO.getCandidateUserIds());
        if (Boolean.TRUE.equals(saveReqVO.getEnabled()) && CollUtil.isEmpty(candidateUserIds)) {
            throw exception(CUSTOMER_ASSIGN_CANDIDATE_INVALID);
        }
        if (CollUtil.isNotEmpty(candidateUserIds)) {
            adminUserApi.validateUserList(candidateUserIds);
        }
        // 2. 存在则更新，否则插入
        CrmCustomerAssignRuleDO dbRule = getAssignRule();
        CrmCustomerAssignRuleDO rule = BeanUtils.toBean(saveReqVO, CrmCustomerAssignRuleDO.class);
        if (dbRule != null) {
            assignRuleMapper.updateById(rule.setId(dbRule.getId()));
            return;
        }
        assignRuleMapper.insert(rule);
    }

    // ==================== 片区配置 ====================

    @Override
    public List<CrmCustomerAssignAreaDO> getAssignAreaListByUserId(Long userId) {
        return assignAreaMapper.selectListByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAssignArea(CrmCustomerAssignAreaSaveReqVO saveReqVO) {
        // 1. 校验用户存在
        adminUserApi.validateUser(saveReqVO.getUserId());
        // 2. 全删全插
        assignAreaMapper.deleteByUserId(saveReqVO.getUserId());
        if (CollUtil.isEmpty(saveReqVO.getAreaIds())) {
            return;
        }
        List<CrmCustomerAssignAreaDO> areas = saveReqVO.getAreaIds().stream().map(areaId ->
                        new CrmCustomerAssignAreaDO().setUserId(saveReqVO.getUserId()).setAreaId(areaId))
                .collect(Collectors.toList());
        assignAreaMapper.insertBatch(areas);
    }

    // ==================== 分配记录 ====================

    @Override
    public PageResult<CrmCustomerAssignRecordDO> getAssignRecordPage(CrmCustomerAssignRecordPageReqVO pageReqVO) {
        return assignRecordMapper.selectPage(pageReqVO);
    }

    // ==================== 智能分配流水线 ====================

    @Override
    public int autoAssignCustomerPool() {
        // 1.1 读取分配规则，未启用直接返回
        CrmCustomerAssignRuleDO rule = getAssignRule();
        if (rule == null || !Boolean.TRUE.equals(rule.getEnabled())) {
            return 0;
        }
        List<Long> candidateUserIds = parseCandidateUserIds(rule.getCandidateUserIds());
        if (CollUtil.isEmpty(candidateUserIds)) {
            return 0;
        }
        // 1.2 查询公海中待分配的客户
        List<CrmCustomerDO> customers = customerMapper.selectListByPoolForAssign();
        if (CollUtil.isEmpty(customers)) {
            return 0;
        }
        // 1.3 构建批次上下文：统计只查一次库，批内共享（分出去一个，内存计数 +1）
        CrmCustomerAssignContext context = buildAssignContext(candidateUserIds);
        // 1.4 获得策略；负载均衡作为片区策略的兜底
        CrmCustomerAssignStrategy strategy = strategyMap.get(rule.getStrategy());
        if (strategy == null) {
            log.error("[autoAssignCustomerPool][策略({}) 不存在，请检查配置]", rule.getStrategy());
            return 0;
        }
        CrmCustomerAssignStrategy fallbackStrategy = strategyMap.get(CrmCustomerAssignStrategyEnum.LOAD_BALANCE.getStrategy());
        // 2. 逐个分配。每个客户独立事务（CAS 抢占），单个失败不影响整批
        int count = 0;
        for (CrmCustomerDO customer : customers) {
            try {
                if (getSelf().assignCustomer(customer, strategy, fallbackStrategy, context)) {
                    count++;
                }
            } catch (Throwable e) {
                log.error("[autoAssignCustomerPool][客户({}) 智能分配异常]", customer.getId(), e);
            }
        }
        return count;
    }

    /**
     * 构建分配批次上下文
     *
     * @param candidateUserIds 候选人用户编号列表
     * @return 上下文
     */
    private CrmCustomerAssignContext buildAssignContext(List<Long> candidateUserIds) {
        // 1.1 持有客户统计（总数 + 成交数）
        Map<Long, CrmCustomerOwnerStatsBO> statsMap = new HashMap<>();
        for (Map<String, Object> row : customerMapper.selectOwnerStatsList(candidateUserIds)) {
            Long userId = Long.valueOf(String.valueOf(row.get("ownerUserId")));
            long totalCount = row.get("totalCount") == null ? 0 : ((Number) row.get("totalCount")).longValue();
            long dealCount = row.get("dealCount") == null ? 0 : ((Number) row.get("dealCount")).longValue();
            statsMap.put(userId, new CrmCustomerOwnerStatsBO(totalCount, dealCount));
        }
        // 1.2 片区绑定
        Map<Long, List<Integer>> areaBindingsMap = new HashMap<>();
        for (CrmCustomerAssignAreaDO areaDO : assignAreaMapper.selectListByUserIds(candidateUserIds)) {
            areaBindingsMap.computeIfAbsent(areaDO.getUserId(), k -> new ArrayList<>()).add(areaDO.getAreaId());
        }
        // 1.3 昵称，用于生成可解释的分配原因
        Map<Long, String> nicknameMap = CollectionUtils.convertMap(adminUserApi.getUserList(candidateUserIds),
                AdminUserRespDTO::getId, AdminUserRespDTO::getNickname);
        return new CrmCustomerAssignContext(candidateUserIds, statsMap, areaBindingsMap, nicknameMap);
    }

    /**
     * 分配单个客户
     *
     * 需要 public 修饰 + getSelf() 代理调用，保证 @Transactional 生效
     * （CGLIB 事务增强只拦截 public 方法，protected 会静默失效，导致 CAS 更新和权限创建不在同一事务）
     *
     * @param customer         客户（来自公海）
     * @param strategy         分配策略
     * @param fallbackStrategy 兜底策略（负载均衡）
     * @param context          批次上下文
     * @return 是否分配成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean assignCustomer(CrmCustomerDO customer, CrmCustomerAssignStrategy strategy,
                                     CrmCustomerAssignStrategy fallbackStrategy, CrmCustomerAssignContext context) {
        // 1. 策略选人。片区策略选不出人时，回退负载均衡，避免客户滞留公海
        CrmCustomerAssignStrategyResult result = strategy.selectOwner(customer, context);
        if (result.getOwnerUserId() == null && strategy.getType() != CrmCustomerAssignStrategyEnum.LOAD_BALANCE
                && fallbackStrategy != null) {
            CrmCustomerAssignStrategyResult fallbackResult = fallbackStrategy.selectOwner(customer, context);
            result = new CrmCustomerAssignStrategyResult(fallbackResult.getOwnerUserId(),
                    result.getReason() + "；" + fallbackResult.getReason());
        }
        if (result.getOwnerUserId() == null) {
            log.warn("[assignCustomer][客户({}) 分配失败：{}]", customer.getId(), result.getReason());
            return false;
        }
        // 2. CAS 抢占：仅当客户仍处于公海（owner_user_id 为空）时才写负责人。
        //    与手动领取并发时，只有一个事务能更新成功，另一个影响行数为 0，直接放弃
        int updateCount = customerMapper.updateOwnerUserIdByIdIfPool(customer.getId(), result.getOwnerUserId());
        if (updateCount == 0) {
            log.info("[assignCustomer][客户({}) 已被其他事务认领，跳过]", customer.getId());
            return false;
        }
        // 3. 创建负责人数据权限
        permissionService.createPermission(new CrmPermissionCreateReqBO()
                .setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType()).setBizId(customer.getId())
                .setUserId(result.getOwnerUserId()).setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));
        // 4. 记录负责人变更历史：公海 → 新负责人
        ownerRecordService.createOwnerRecord(new CrmOwnerRecordCreateReqBO()
                .setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType()).setBizId(customer.getId())
                .setPreOwnerUserId(customer.getOwnerUserId()).setPostOwnerUserId(result.getOwnerUserId()));
        // 5. 分配留痕：为什么分给 TA
        assignRecordMapper.insert(new CrmCustomerAssignRecordDO().setCustomerId(customer.getId())
                .setCustomerName(customer.getName()).setPreOwnerUserId(customer.getOwnerUserId())
                .setPostOwnerUserId(result.getOwnerUserId())
                .setStrategy(strategy.getType().getStrategy()).setReason(result.getReason()));
        // 6. 批内计数 +1，同批后续客户的分配依然准确
        context.onAssigned(result.getOwnerUserId());
        return true;
    }

    /**
     * 解析候选人用户编号列表（逗号分隔）
     */
    private List<Long> parseCandidateUserIds(String candidateUserIds) {
        if (StrUtil.isBlank(candidateUserIds)) {
            return Collections.emptyList();
        }
        return StrUtil.splitTrim(candidateUserIds, ',').stream()
                .map(Long::valueOf).collect(Collectors.toList());
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     */
    private CrmCustomerAssignServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
