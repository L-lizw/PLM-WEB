/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: TRFoundationImpl
 * Wanglei 2011-11-14
 */
package dyna.app.server.core.track.impl;

import dyna.app.server.context.ApplicationServerContext;
import dyna.app.service.brs.boas.BOASImpl;
import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.bean.data.foundation.BOMView;
import dyna.common.bean.data.foundation.ViewObject;
import dyna.common.bean.track.Tracker;
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
@Component @Scope("prototype") public class TRViewLinkImpl extends TRFoundationImpl
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
		StringBuffer sb = new StringBuffer();

		sb.append("END1:");

		Object[] params = tracker.getParameters();

		((BOASImpl) boas).setSignature(serverContext.getSystemInternalSignature());
		String templateName = null;
		if (params != null && params.length > 0)
		{
			if (params[0] instanceof ObjectGuid)
			{
				try
				{
					ViewObject relation = boas.getRelation((ObjectGuid) params[0]);
					if (relation != null)
					{
						ObjectGuid end1ObjectGuid = relation.getEnd1ObjectGuid();
						if (end1ObjectGuid != null)
						{
							FoundationObject objectByGuid = boas.getObjectByGuid(end1ObjectGuid);
							String renderFoundation = this.renderFoundation(objectByGuid);
							sb.append(StringUtils.convertNULLtoString(renderFoundation));
						}
						templateName = relation.getName();
					}

				}
				catch (ServiceRequestException e)
				{
					DynaLogger.info("write log error,End1:" + e.getMessage());
				}
			}
			else if (params[0] instanceof ViewObject)
			{
				ViewObject relation = (ViewObject) params[0];
				if (relation != null)
				{
					ObjectGuid end1ObjectGuid = relation.getEnd1ObjectGuid();
					if (end1ObjectGuid != null)
					{
						try
						{
							FoundationObject objectByGuid = boas.getObjectByGuid(end1ObjectGuid);
							String renderFoundation = this.renderFoundation(objectByGuid);
							sb.append(StringUtils.convertNULLtoString(renderFoundation));
						}
						catch (ServiceRequestException e)
						{
							DynaLogger.info("write log error,End1:" + e.getMessage());
						}
					}
					templateName = relation.getName();
				}
			}
			else if (params[0] instanceof BOMView)
			{
				BOMView relation = (BOMView) params[0];
				if (relation != null)
				{
					ObjectGuid end1ObjectGuid = relation.getEnd1ObjectGuid();
					if (end1ObjectGuid != null)
					{
						try
						{
							FoundationObject objectByGuid = boas.getObjectByGuid(end1ObjectGuid);
							String renderFoundation = this.renderFoundation(objectByGuid);
							sb.append(StringUtils.convertNULLtoString(renderFoundation));
						}
						catch (ServiceRequestException e)
						{
							DynaLogger.info("write log error,End1:" + e.getMessage());
						}
					}
					templateName = relation.getName();
				}
			}
		}

		sb.append(", END2:");
		FoundationObject end2FoundationObject = this.getEnd2FoundationObject(params, boas);
		sb.append(StringUtils.convertNULLtoString(this.renderFoundation(end2FoundationObject)));

		sb.append(", TEMPLATENAME:");
		sb.append(StringUtils.convertNULLtoString(templateName));

		return sb.toString();
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
