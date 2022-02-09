package dyna.app.startinit;

import dyna.app.service.das.jss.ScheduledTaskJobPollingImpl;
import dyna.app.util.QuartzUtil;
import dyna.app.conf.yml.ConfigurableJSSImpl;
import dyna.common.conf.JobDefinition;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/2/1
 **/
@Component
public class ScheduleStartUpRunner implements CommandLineRunner
{
	@Autowired
	private Scheduler                   schedulerTimerTask;
	@Autowired
	private ScheduledTaskJobPollingImpl jobPollingTask;

	@Autowired
	private ConfigurableJSSImpl         configurableJSS;

	@Override public void run(String... args) throws Exception
	{
		JobDefinition jobDelfinition = configurableJSS.getJobDelfinition("");
		QuartzUtil.createScheduleJob(schedulerTimerTask, jobDelfinition);
	}
}
