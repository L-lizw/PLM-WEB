/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: ServerContextListenerJobPollingImpl
 * Wanglei 2011-11-9
 */
package dyna.app.service.das.jss;

import dyna.app.service.AbstractServiceStub;
import dyna.app.util.QuartzUtil;
import dyna.app.conf.yml.ConfigurableJSSImpl;
import dyna.app.conf.yml.ConfigurableServiceImpl;
import dyna.app.util.SpringUtil;
import dyna.common.conf.JobDefinition;
import dyna.common.conf.ServiceDefinition;
import dyna.common.dto.Queue;
import dyna.common.dto.aas.User;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import dyna.common.systemenum.JobStatus;
import dyna.common.systemenum.MailCategoryEnum;
import dyna.common.systemenum.MailMessageType;
import dyna.common.util.SetUtils;
import dyna.common.util.StringUtils;
import dyna.net.service.brs.AAS;
import dyna.net.service.brs.SMS;
import dyna.net.service.das.MSRM;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作任务轮询监听器, 添加轮询机制
 *
 * @author Lizw
 */
@Component
public class ServerContextListenerJobPollingImpl extends AbstractServiceStub<JSSImpl> implements ApplicationListener<ContextRefreshedEvent>
{
	private final static String DEFAULT_JOB_POLLING_TIME  = "0/30 * * * * *";
	private final static String DEFAULT_JOB_GETERP_STATUS = "* 0/2 * * * *";

	@Autowired
	private ConfigurableServiceImpl     configurableService;
	@Autowired
	private ConfigurableJSSImpl         configurableJSS;

	@Autowired
	@Qualifier("timerScheduler")
	private Scheduler                   timerScheduler;

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.app.server.context.ServerContextListener#contextInitialized(dyna.app.server.context.ServerContext,
	 * dyna.app.server.context.ServiceContext)
	 */
	@Override public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent)
	{
		ServiceDefinition erpServiceDefinition = configurableService.getServiceDefinition("ERPI");
		String hasERPExceutingStatus = erpServiceDefinition.getParam().get("hasERPExceutingStatus");

		try
		{
			List<JobStatus> statuslist = new ArrayList<JobStatus>();
			statuslist.add(JobStatus.RUNNING);
			statuslist.add(JobStatus.ERPEXECUTING);
			List<Queue> finishJobList = this.stubService.listJob(null, false, statuslist);
			if (!SetUtils.isNullList(finishJobList))
			{
				//todo
				String str = "";
//				this.stubService.getMSRM().getMSRString("ID_APP_JSS_JOB_RUN_FAIL", serverContext.getSystemInternalSignature().getLanguageEnum().getId());
				String msg = "";
//				this.stubService.getMSRM().getMSRString("ID_APP_JSS_JOB_FAIL_SERVER_RESTART", serverContext.getSystemInternalSignature().getLanguageEnum().getId());
				for (Queue job : finishJobList)
				{
					job.setJobStatus(JobStatus.FAILED);
					job.setResult(msg);
					this.stubService.saveJob(job);
					try
					{
						String clsName = job.getExecutorClass();
						if (StringUtils.isNullString(clsName))
						{
							this.notifyCreator(job, MailCategoryEnum.ERROR, str + msg);
						}
						else
						{
							Class<?> jobExecutorClass = Class.forName(clsName);
							Object newInstance = jobExecutorClass.newInstance();
							if (newInstance instanceof JobExecutor)
							{
								JobExecutor jobExecutor = (JobExecutor) newInstance;
								job.setResult(jobExecutor.serverPerformFail(job).getMessage());
								this.stubService.saveJob(job);
							}
							else
							{
								this.notifyCreator(job, MailCategoryEnum.ERROR, str + msg);
							}
						}
					}
					catch (Exception e)
					{
						this.notifyCreator(job, MailCategoryEnum.ERROR, str + msg);
					}
				}
			}
		}
//		catch (ServiceNotFoundException e)
//		{
//			e.printStackTrace();
//		}
		catch (ServiceRequestException e)
		{
			e.printStackTrace();
		}

		JobDefinition jobPolling = configurableJSS.getJobDelfinition("JobPolling");
		if(StringUtils.isNullString(jobPolling.getCron()))
		{
			jobPolling.setCron(DEFAULT_JOB_POLLING_TIME);
		}

		QuartzUtil.createScheduleJob(timerScheduler, jobPolling);

		JobDefinition jobGetERPStaus = configurableJSS.getJobDelfinition("JobGetERPStaus");
		if(StringUtils.isNullString(jobGetERPStaus.getCron()))
		{
			jobGetERPStaus.setCron(DEFAULT_JOB_GETERP_STATUS);
		}

		if ("true".equalsIgnoreCase(hasERPExceutingStatus))
		{
			QuartzUtil.createScheduleJob(timerScheduler, jobGetERPStaus);
		}

		addDeleteTask();
	}

	private void addDeleteTask()
	{
		JobDefinition taskDeleteJob = configurableJSS.getJobDelfinition("TaskDeleteJob");
		QuartzUtil.createScheduleJob(timerScheduler, taskDeleteJob);
	}

	private void notifyCreator(Queue job, MailCategoryEnum category, String msg)
	{
		if (StringUtils.isNullString(msg))
		{
			return;
		}

		try
		{
			User user = this.stubService.getAAS().getUser(job.getCreateUserGuid());
			MSRM msr = this.stubService.getMsrm();
			String title = "";
			JobDefinition jobDefinition = this.stubService.getJobDefinition(job.getExecutorClass());
			if (StringUtils.isNullString(jobDefinition.getMsrId()))
			{
				title = msr.getMSRString(jobDefinition.getMsrId(), this.stubService.getUserSignature().getLanguageEnum().toString());
			}
			if (StringUtils.isNullString(title))
			{
				title = jobDefinition.getName();
			}
			if (StringUtils.isNullString(title))
			{
				title = job.getType();
			}
			this.stubService.getSms().sendMailToUser(job.getType(), msg, category, null, user.getUserId(), MailMessageType.JOBNOTIFY);
		}
		catch (Exception e)
		{
			DynaLogger.error("notify job creator failed: " + e);
		}
	}
}
