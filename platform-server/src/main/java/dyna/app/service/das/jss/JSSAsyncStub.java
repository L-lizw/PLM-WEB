package dyna.app.service.das.jss;

import dyna.app.service.AbstractServiceStub;
import dyna.app.conf.yml.ConfigurableJSSImpl;
import dyna.app.service.brs.async.AsyncImpl;
import dyna.common.conf.JobDefinition;
import dyna.common.log.DynaLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/2/4
 **/
@Component
public class JSSAsyncStub extends AbstractServiceStub<JSSImpl>
{
	@Autowired
	private ConfigurableJSSImpl configurableJSS;

	protected void deleteJobByType(String jobID)
	{
		try
		{
			JobDefinition jobDefinition = configurableJSS.getJobDelfinition(jobID);
			int timeOut = jobDefinition.getTimeOut();
			if (timeOut > 0)
			{
				this.stubService.deleteTimeoutJobs( jobID, timeOut);
			}
		}
		catch (Exception e)
		{
			DynaLogger.error("delete job error: " + e, e);
		}
	}
}
