package dyna.app.service.das.jss;

import dyna.app.service.AbstractServiceStub;
import dyna.common.conf.JobDefinition;
import dyna.common.log.DynaLogger;
import dyna.common.util.SetUtils;
import dyna.net.service.das.JSS;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Lizw
 * @date 2022/1/26
 **/
@Component
public class JobExecuteStub  extends AbstractServiceStub<JSSImpl>
{

	protected void deleteJobByType(String jobType)
	{
		DynaLogger.info("JSS Scheduled [Class]ScheduledTaskDeleteJobByTypeImpl[" + jobType + "], Scheduled Task Start...");
		try
		{
			this.stubService.setSignature(this.serverContext.getSystemInternalSignature());
			this.stubService.newTransactionId();

			JobDefinition jobDefinition = this.stubService.getJobDefinitionByType(jobType);
			int timeOut = jobDefinition.getTimeOut();
			if (timeOut > 0)
			{
				this.stubService.deleteTimeoutJobs(jobType, timeOut);
			}
		}
		catch (Exception e)
		{
			DynaLogger.error("delete job error: " + e, e);
		}
		finally
		{
			DynaLogger.info("JSS Scheduled [Class]ScheduledTaskDeleteJobByTypeImpl[" + jobType + "], Scheduled Task End...");
		}
	}

	protected void deleteJob()
	{
		DynaLogger.info("JSS Scheduled [Class]ScheduledTaskDeleteJobImpl , Scheduled Task Start...");
		try
		{
			this.stubService.setSignature(this.serverContext.getSystemInternalSignature());
			this.stubService.newTransactionId();

			List<JobDefinition> jobQueueTypeList = this.stubService.getJobQueueTypeList();
			if (!SetUtils.isNullList(jobQueueTypeList))
			{
				for (JobDefinition jobDefinition : jobQueueTypeList)
				{
					this.stubService.getAsync().deleteJobByType(jobDefinition.getId());
				}
			}
		}
		catch (Exception e)
		{
			DynaLogger.error("delete job error: " + e, e);
		}
		finally
		{
			DynaLogger.info("JSS Scheduled [Class]ScheduledTaskDeleteJobImpl , Scheduled Task End...");
		}
	}
}
