/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: TrackerDescriptionFixedImpl
 * Wanglei 2011-11-11
 */
package dyna.app.server.core.track.impl;

import dyna.common.bean.track.Tracker;
import dyna.common.bean.track.TrackerDescription;
import dyna.common.util.StringUtils;

/**
 * @author Lizw
 *
 */
public class TrackerDescriptionFixedImpl implements TrackerDescription
{

	private String	jobDesc	= null;

	protected TrackerDescriptionFixedImpl(String desc)
	{
		this.setJobDesc(desc);
	}

	public void setJobDesc(String desc)
	{
		this.jobDesc = desc;
	}

	@Override
	public String getDescription(Tracker tracker)
	{
		return StringUtils.convertNULLtoString(this.jobDesc);
	}

}
