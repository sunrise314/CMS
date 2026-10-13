package cn.iocoder.yudao.module.crm.job.customer;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.crm.service.customer.assign.CrmCustomerAssignService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 客户公海智能分配 Job
 *
 * 与 {@link CrmCustomerAutoPutPoolJob} 组成"回收 - 分配"流水线：
 * 先由回收 Job 把超时未跟进/未成交的客户掉入公海，再由本 Job 把公海客户按策略分给销售
 * 建议 cron 比回收 Job 晚几分钟，保证回收先执行
 *
 * @author 芋道源码
 */
@Component
public class CrmCustomerAutoAssignJob implements JobHandler {

    @Resource
    private CrmCustomerAssignService customerAssignService;

    @Override
    @TenantJob
    public String execute(String param) {
        int count = customerAssignService.autoAssignCustomerPool();
        return String.format("智能分配公海客户 %s 个", count);
    }

}
