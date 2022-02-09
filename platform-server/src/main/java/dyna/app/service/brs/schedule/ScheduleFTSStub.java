package dyna.app.service.brs.schedule;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.fts.FTSImpl;
import dyna.common.log.DynaLogger;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/1/30
 **/
@Component
public class ScheduleFTSStub extends AbstractServiceStub<ScheduleServiceImpl>
{
	protected void checkTransformQueue()
	{
		try
		{
			this.stubService.getFTS().transformQueueCheck();
		}
		catch (Throwable e)
		{
			DynaLogger.error("run QueueCheck:", e);
		}
	}
}
