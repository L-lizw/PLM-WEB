package dyna.app.service.das.jss;

import dyna.app.service.AbstractQuartzJobStub;
import dyna.common.conf.JobDefinition;
import dyna.common.dto.Queue;
import dyna.common.log.DynaLogger;
import dyna.common.util.SetUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScheduledTaskDeleteJobImpl  extends AbstractQuartzJobStub<JSSImpl> implements JobCallback
{

	@Override
	protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException
	{
		DynaLogger.info("JSS Scheduled [Class]ScheduledTaskDeleteJobImpl , Scheduled Task Start...");
		try
		{
			List<JobDefinition> jobQueueTypeList = this.stubService.getJobQueueTypeList();
			if (!SetUtils.isNullList(jobQueueTypeList))
			{
				for (JobDefinition jobDefinition : jobQueueTypeList)
				{
					this.stubService.deleteJobByType(jobDefinition.getJobID());
				}
			}
		}
		catch (Exception e)
		{
			DynaLogger.error("delete job error: " + e, e);
		}

		DynaLogger.info("JSS Scheduled [Class]ScheduledTaskDeleteJobImpl , Scheduled Task End...");

	}

	@Override
	public void beforePerform(Queue job)
	{
		// do nothing
	}

	@Override
	public void afterPerform(Queue job)
	{
		// do nothing
	}

}
