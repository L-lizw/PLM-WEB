package dyna.app.service.brs.dss.tracked;

import dyna.app.core.track.impl.DefaultTrackerRendererImpl;
import dyna.common.bean.track.Tracker;
import dyna.common.dto.DSSFileInfo;
import dyna.common.exception.ServiceNotFoundException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import dyna.net.service.brs.DSS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class TRDSSFile extends DefaultTrackerRendererImpl
{
	@Autowired
	private DSS dss;
	/*
	 * (non-Javadoc)
	 * 
	 * @see dyna.app.core.track.impl.DefaultTrackerRendererImpl#getHandledObject(dyna.app.core.track.Tracker)
	 */
	@Override
	public String getHandledObject(Tracker tracker)
	{
		DSSFileInfo fileInfo = null;
		try
		{
			Object[] params = tracker.getParameters();
			fileInfo = dss.getFile((String) params[0]);
			if (fileInfo != null)
			{
				return this.getFileName(fileInfo);
			}
		}
		catch (ServiceRequestException e)
		{
			DynaLogger.info("write log error:" + e.getMessage());
		}
		return super.getHandledObject(tracker);
	}

	protected String getFileName(DSSFileInfo fileInfo)
	{
		if (fileInfo == null)
		{
			return null;
		}
		return fileInfo.getName();
	}
}
