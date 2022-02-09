package dyna.app.service.brs.ppms;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.async.AsyncImpl;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.log.DynaLogger;
import dyna.net.service.brs.PPMS;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/1/28
 **/
@Component
public class PPMSAsyncStub extends AbstractServiceStub<PPMSImpl>
{
	protected void deleteProject(ObjectGuid projectOObjectGuid)
	{
		try
		{
			this.stubService.deleteAllTask(projectOObjectGuid);
			// 如果改项目被任务关联，需要清空任务的关联项目的值
			this.stubService.clearRelationProject(projectOObjectGuid);
		}
		catch (Throwable e)
		{
			DynaLogger.error("run delete all task:", e);
		}
	}
}
