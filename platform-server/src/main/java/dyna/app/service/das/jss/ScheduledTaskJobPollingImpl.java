/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: ScheduledTaskJobPollingImpl
 * Wanglei 2011-11-8
 */
package dyna.app.service.das.jss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import dyna.app.conf.AsyncConfig;
import dyna.app.server.core.sch.PriorityScheduledTask;
import dyna.app.service.AbstractQuartzJobStub;
import dyna.common.dto.Queue;
import dyna.common.log.DynaLogger;
import dyna.common.util.SetUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 工作轮询任务
 * 
 * @author Lizw
 * 
 */
@Component
public class ScheduledTaskJobPollingImpl extends AbstractQuartzJobStub<JSSImpl> implements JobCallback
{
	private static final List<String>	POLLING_JOBGUID_LIST	= Collections.synchronizedList(new ArrayList<String>());

	@Autowired
	private AsyncConfig asyncConfig;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.TimerTask#run()
	 */
	@Override
	protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException
	{
		// DynaLogger.info("JSS Scheduled [Class]ScheduledTaskJobPollingImpl , Scheduled Task Start...");
		try
		{
			Map<String, Object> condition = new HashMap<String, Object>();
			condition.put(Queue.SERVER_ID, this.serverContext.getServerConfig().getId());
			List<Queue> waitingJobList = this.stubService.listWaitingJob(condition);
			if (SetUtils.isNullList(waitingJobList) == false)
			{
				String jobObjectGuid = null;
				for (Queue job : waitingJobList)
				{
					jobObjectGuid = job.getGuid();
					if (POLLING_JOBGUID_LIST.contains(jobObjectGuid))
					{
						continue;
					}

					POLLING_JOBGUID_LIST.add(jobObjectGuid);
					String sid = this.stubService.getJobDefinition(job.getExecutorClass()).getSchedulerID();
					if (this.asyncConfig.getScheduler(sid) != null)
					{
						PriorityScheduledTask task = new ScheduledTaskRunJobImpl(this.serverContext, job, this);
						task.setPriority(job.getPriority().intValue());
						task.setCreateTime(job.getCreateTime());
						this.asyncConfig.getScheduler(sid).addTask(task);
					}
				}
			}
		}
		catch (Exception e)
		{
			DynaLogger.error("polling job error: " + e, e);
		}
		// DynaLogger.info("JSS Scheduled [Class]ScheduledTaskJobPollingImpl , Scheduled Task End...");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dyna.app.service.brs.jss.JobCallback#beforePerform(dyna.common.bean.data.FoundationObject)
	 */
	@Override
	public void beforePerform(Queue job)
	{
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dyna.app.service.brs.jss.JobCallback#afterPerform(dyna.common.bean.data.FoundationObject)
	 */
	@Override
	public void afterPerform(Queue job)
	{
		POLLING_JOBGUID_LIST.remove(job.getGuid());
	}

}
