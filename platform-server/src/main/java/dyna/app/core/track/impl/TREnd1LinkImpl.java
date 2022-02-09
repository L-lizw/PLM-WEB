/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: TRFoundationImpl
 * Wanglei 2011-11-14
 */
package dyna.app.core.track.impl;

import dyna.app.server.context.ApplicationServerContext;
import dyna.app.service.brs.boas.BOASImpl;
import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.bean.track.Tracker;
import dyna.common.exception.ServiceNotFoundException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import dyna.common.util.StringUtils;
import dyna.net.service.brs.BOAS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 */
@Component @Scope("prototype") public class TREnd1LinkImpl extends TRFoundationImpl
{
	@Autowired private ApplicationServerContext serverContext;
	@Autowired private BOAS                     boas;

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.app.core.track.impl.DefaultTrackerRendererImpl#getHandledObject(dyna.app.core.track.Tracker)
	 */
	@Override public String getHandledObject(Tracker tracker)
	{
		// return this.renderFoundation(this.getRenderedFoundationObject(tracker));
		StringBuffer sb = new StringBuffer();

		sb.append("END1:");

		Object[] params = tracker.getParameters();

		((BOASImpl) boas).setSignature(serverContext.getSystemInternalSignature());
		FoundationObject end1FoundationObject = this.getEnd1FoundationObject(params, boas);
		sb.append(StringUtils.convertNULLtoString(this.renderFoundation(end1FoundationObject)));
		sb.append(", END2:");
		FoundationObject end2FoundationObject = this.getEnd2FoundationObject(params, boas);
		sb.append(StringUtils.convertNULLtoString(this.renderFoundation(end2FoundationObject)));

		sb.append(", TEMPLATENAME:");
		sb.append(StringUtils.convertNULLtoString(this.getTemplateName(params)));

		return sb.toString();
	}

	protected String getTemplateName(Object[] params)
	{
		if (params != null && params.length > 0)
		{
			if (params[3] instanceof String)
			{
				return params[3].toString();
			}
		}

		return null;
	}

	protected FoundationObject getEnd1FoundationObject(Object[] params, BOAS boas)
	{
		if (params != null && params.length > 0)
		{
			if (params[0] instanceof ObjectGuid)
			{
				try
				{
					return boas.getObjectByGuid((ObjectGuid) params[0]);
				}
				catch (ServiceRequestException e)
				{
					DynaLogger.info("write log error,End1:" + e.getMessage());
				}
			}
		}

		return null;
	}

	protected FoundationObject getEnd2FoundationObject(Object[] params, BOAS boas)
	{
		if (params != null && params.length > 1)
		{
			if (params[1] instanceof ObjectGuid)
			{
				try
				{
					return boas.getObjectByGuid((ObjectGuid) params[1]);
				}
				catch (ServiceRequestException e)
				{
					DynaLogger.info("write log error,End2:" + e.getMessage());
				}
			}
		}

		return null;
	}
}
