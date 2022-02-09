package dyna.app.service.brs.schedule;

import dyna.app.service.AbstractServiceStub;
import dyna.common.log.DynaLogger;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/2/4
 **/
@Component
public class ScheduleSMSStub extends AbstractServiceStub<ScheduleServiceImpl>
{
	protected void clearMail()
	{
		try
		{
			this.stubService.getSMS().clearMailByConfig();
		}
		catch (Throwable e)
		{
			DynaLogger.error("run ClearMailScheduled:", e);
		}
	}
}
