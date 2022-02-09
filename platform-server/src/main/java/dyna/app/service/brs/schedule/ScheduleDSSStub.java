package dyna.app.service.brs.schedule;

import dyna.app.service.AbstractServiceStub;
import dyna.common.bean.data.SystemObject;
import dyna.common.dto.DSSFileTrans;
import dyna.common.log.DynaLogger;
import dyna.net.service.data.SystemDataService;
import org.acegisecurity.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lizw
 * @date 2022/2/6
 **/
@Component
public class ScheduleDSSStub extends AbstractServiceStub<ScheduleServiceImpl>
{
	protected void deleteFileTrans()
	{
		try
		{
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());

			SystemDataService systemDataService = this.stubService.getSystemDataService();
			Map<String, Object> filter = new HashMap<String, Object>();
			c.add(Calendar.YEAR, -1);

			filter.put(SystemObject.CREATE_TIME, c.getTime());
			systemDataService.delete(DSSFileTrans.class, filter, "deleteTranFiles");
		}
		catch (Throwable e)
		{
			DynaLogger.error("run Obsolete:", e);
		}
		finally
		{
			SecurityContextHolder.clearContext();

		}
	}
}
