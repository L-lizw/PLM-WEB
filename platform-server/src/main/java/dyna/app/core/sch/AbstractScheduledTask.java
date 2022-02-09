/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: AbstractScheduledTask
 * Wanglei 2011-4-22
 */
package dyna.app.core.sch;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.das.jss.JSSImpl;

import java.util.Date;

/**
 * @author Wanglei
 * 
 */
public abstract class AbstractScheduledTask extends AbstractServiceStub<JSSImpl> implements PriorityScheduledTask, Comparable<PriorityScheduledTask>
{
	private int		priority	= DEFAULT_PRIORITY;
	private Date	date		= new Date();

	/*
	 * (non-Javadoc)
	 * 
	 * @see dyna.app.core.sch.ScheduledTask#getPriority()
	 */
	@Override
	public int getPriority()
	{
		return this.priority;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dyna.app.core.sch.ScheduledTask#setPriority(int)
	 */
	@Override
	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	@Override
	public Date getCreateTime()
	{
		return date;
	}

	@Override
	public void setCreateTime(Date date)
	{
		this.date = date;
	}

	@Override
	public int compareTo(PriorityScheduledTask o)
	{
		if (getPriority() < o.getPriority())
		{
			return 1;
		}
		else if (getPriority() > o.getPriority())
		{
			return -1;
		}
		else if (this.getCreateTime()==null)
		{
			if (o.getCreateTime()==null)
			{
				return 0;
			}
			else
			{
				return 1;
			}
		}
		else if (o.getCreateTime()==null)
		{
			return -1;
		}
		else if (this.getCreateTime().getTime()<o.getCreateTime().getTime())
		{
			return -1;
		}
		else if (this.getCreateTime().getTime()==o.getCreateTime().getTime())
		{
			return 0;
		}
		else
		{
			return 1;
		}
	}
}
