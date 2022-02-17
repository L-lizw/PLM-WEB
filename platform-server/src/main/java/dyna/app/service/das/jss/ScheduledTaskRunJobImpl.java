/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: ScheduledTaskRunJobImpl
 * Wanglei 2011-11-8
 */
package dyna.app.service.das.jss;

import dyna.app.server.context.ApplicationServerContext;
import dyna.app.util.SpringUtil;
import dyna.app.server.core.sch.AbstractScheduledTask;
import dyna.common.conf.JobDefinition;
import dyna.common.dto.Queue;
import dyna.common.dto.aas.User;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import dyna.common.systemenum.JobStatus;
import dyna.common.systemenum.MailCategoryEnum;
import dyna.common.systemenum.MailMessageType;
import dyna.common.util.DateFormat;
import dyna.common.util.StringUtils;
import dyna.net.service.brs.AAS;
import dyna.net.service.das.MSRM;

import java.util.Date;

/**
 * 队列工作任务执行线程
 * 
 * @author Wanglei
 * 
 */
public class ScheduledTaskRunJobImpl extends AbstractScheduledTask
{
	private ApplicationServerContext serverContext = null;
	private Queue                    job            = null;
	private JobCallback              callback       = null;

	public ScheduledTaskRunJobImpl(ApplicationServerContext serverContext, Queue job, JobCallback callback) throws Exception
	{
		this.serverContext = serverContext;
		this.job = job;
		this.callback = callback;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run()
	{
		DynaLogger.info("JSS Scheduled [Class]ScheduledTaskRunJobImpl , Scheduled Task Start...");

		JobExecutor jobExecutor = null;
		String credential = null;
		String tranId = null;
		long time = 0;
		// FoundationObject job = null;
		try
		{
			time = new Date().getTime();
			this.stubService.setSignature(this.serverContext.getSystemInternalSignature());
			credential = this.serverContext.getSystemInternalSignature().getCredential();
			tranId = this.stubService.newTransactionId();

			// job = jss.getJob(this.jobObjectGuid);
			Object temp = this.job.getJobStatus();
			JobStatus status = temp == null ? JobStatus.WAITING : (JobStatus) temp;
			if (JobStatus.WAITING != status)
			{
				return;
			}

			String clsName = this.job.getExecutorClass();
			if (StringUtils.isNullString(clsName))
			{
				throw new Exception("undefined executor for job");
			}
			Class<?> jobExecutorClass = Class.forName(clsName);
			Object newInstance = SpringUtil.getBean(jobExecutorClass);
			if (newInstance instanceof JobExecutor)
			{
				jobExecutor = (JobExecutor) newInstance;
			}
			else
			{
				throw new Exception("executor initializes failed");
			}

			if (this.callback != null)
			{
				this.callback.beforePerform(this.job);
			}

			this.job = this.stubService.getJob(this.job.getGuid());
			if (this.job.getJobStatus() != JobStatus.WAITING)
			{
				return;
			}
			this.job = this.stubService.setJobStatus(this.job, JobStatus.RUNNING);

			JobResult result = jobExecutor.perform(this.job);

			if (result == null)
			{
				result = JobResult.succeed(null);
			}

			this.job = this.stubService.getJob(this.job.getGuid());
			this.job.setResult(result.getMessage());
			this.job.setJobStatus(result.getStatus());
			this.job = this.stubService.saveJob(this.job);
			if (result.isSendNotify())
			{
				this.notifyCreator(this.job, MailCategoryEnum.INFO, result.getMessage());
			}

			//TODO
//			if (StringUtils.isNullString(tranId) == false && DataServer.getTransactionManager().getCountOfNotCommitTranscation() > 0)
			{
				DynaLogger.info("APP Transation Not Commit");
				DynaLogger.info("APP Start:" + DateFormat.format(new Date(time), "HH:mm:ss,SSS") + "\tcredential:" + credential + "\tmethod:ScheduledTaskRunJobImpl#run");
//				DataServer.getTransactionService().commitTransactionImmediately(tranId);
			}
		}
		catch (Throwable e)
		{
			if (this.job != null && this.stubService != null)
			{
				try
				{
					String result = StringUtils.convertNULLtoString(e.getMessage());

					this.job = this.stubService.getJob(this.job.getGuid());
					this.job.setResult(result);
					this.job.setJobStatus(JobStatus.FAILED);
					this.job = this.stubService.saveJob(this.job);
					String str = this.stubService.getMSRM().getMSRString("ID_APP_JSS_JOB_RUN_FAIL", this.serverContext.getSystemInternalSignature().getLanguageEnum().getId());
					this.notifyCreator(this.job, MailCategoryEnum.ERROR, str + result);
				}
				catch (ServiceRequestException e1)
				{
				}
			}
			DynaLogger.error("RunJob failed : " + this.job, e);
		}
		finally
		{
			//TODO
//			if (StringUtils.isNullString(tranId) == false && DataServer.getTransactionManager().getCountOfNotCommitTranscation() > 0)
			{
				DynaLogger.info("APP Transation Not Commit");
				DynaLogger.info("APP Start:" + DateFormat.format(new Date(time), "HH:mm:ss,SSS") + "\tcredential:" + credential + "\tmethod:ScheduledTaskRunJobImpl#run");
//				DataServer.getTransactionService().rollbackTransactionImmediately(tranId);
			}


			if (this.callback != null)
			{
				this.callback.afterPerform(this.job);
			}
			jobExecutor = null;
			DynaLogger.info("JSS Scheduled [Class]ScheduledTaskRunJobImpl , Scheduled Task End...");
		}

	}

	private void notifyCreator(Queue job, MailCategoryEnum category, String msg)
	{
		if (StringUtils.isNullString(msg))
		{
			return;
		}

		try
		{
			User user = this.stubService.getServiceInstance(AAS.class).getUser(job.getCreateUserGuid());
			MSRM msr = this.stubService.getServiceInstance(MSRM.class);
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
			this.stubService.getSMS().sendMailToUser(job.getType(), msg, category, null, user.getUserId(), MailMessageType.JOBNOTIFY);
		}
		catch (Exception e)
		{
			DynaLogger.error("notify job creator failed: " + e);
		}
	}

}
