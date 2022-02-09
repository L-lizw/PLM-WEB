/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: TRPerfActImpl
 * Wanglei 2011-11-21
 */
package dyna.app.service.brs.wfi.track;

import dyna.app.core.track.impl.DefaultTrackerRendererImpl;
import dyna.app.server.context.ApplicationServerContext;
import dyna.app.service.brs.wfi.WFIImpl;
import dyna.common.bean.track.Tracker;
import dyna.common.dto.wf.ActivityRuntime;
import dyna.common.exception.ServiceRequestException;
import dyna.common.systemenum.DecisionEnum;
import dyna.net.service.brs.WFI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * 
 */
@Component
@Scope("prototype")
public class TRPerfActImpl extends DefaultTrackerRendererImpl
{
	@Autowired
	private ApplicationServerContext    serverContext;

	@Autowired
	private WFI     wfi;

	@Override
	public String getHandledObject(Tracker tracker)
	{
		Object[] parameters = tracker.getParameters();
		if (parameters != null && parameters.length > 3)
		{
			try
			{
				((WFIImpl) wfi).setSignature(serverContext.getSystemInternalSignature());
				ActivityRuntime activityRuntime = wfi.getActivityRuntime((String) parameters[0]);
				if (activityRuntime != null)
				{
					return activityRuntime.getTitle(serverContext.getSystemInternalSignature().getLanguageEnum())
							+ "|" + ((DecisionEnum) parameters[2]).name() + ":" + parameters[1];
				}
				else
				{
					return parameters[0] + "|" + ((DecisionEnum) parameters[2]).name() + ":" + parameters[1];
				}
			}
			catch (ServiceRequestException e)
			{
				e.printStackTrace();
			}
		}
		return super.getHandledObject(tracker);

	}

}
