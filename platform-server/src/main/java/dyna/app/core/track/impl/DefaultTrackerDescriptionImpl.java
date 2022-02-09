/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: DefaultTrackerDescriptionImpl
 * Wanglei 2011-11-14
 */
package dyna.app.core.track.impl;

import dyna.common.bean.track.Tracker;
import dyna.common.bean.track.TrackerDescription;

import java.lang.reflect.Method;

/**
 * @author Lizw
 *
 */
public class DefaultTrackerDescriptionImpl implements TrackerDescription
{

	/* (non-Javadoc)
	 * @see dyna.app.core.track.TrackerDescription#getDescription(dyna.app.core.track.Tracker)
	 */
	@Override
	public String getDescription(Tracker tracker)
	{
		Method method = tracker.getMethod();
		if (method == null)
		{
			return "<unknown>";
		}
		return method.getName();
	}

}
