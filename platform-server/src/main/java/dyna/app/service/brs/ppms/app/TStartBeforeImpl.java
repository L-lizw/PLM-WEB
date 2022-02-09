/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: CompleteDelayImpl
 * WangLHB Jun 27, 2012
 */
package dyna.app.service.brs.ppms.app;

import java.util.Calendar;
import java.util.Date;

import dyna.app.service.brs.ppms.PPMSImpl;
import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ppms.EarlyWarning;
import dyna.common.bean.data.ppms.PPMFoundationObjectUtil;
import dyna.common.exception.ServiceRequestException;
import dyna.common.systemenum.ppms.TaskStatusEnum;
import dyna.common.systemenum.ppms.TaskTypeEnum;
import dyna.common.systemenum.ppms.WarningEvent;
import dyna.common.util.DateFormat;
import dyna.common.util.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author WangLHB
 *         任务启动前通知
 */
@Component(WarningEvent.T_START_BEFORE)
public class TStartBeforeImpl extends AbstractEventApp
{

	public TStartBeforeImpl()
	{
		super(WarningEvent.WarningEventEnum.T_START_BEFORE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dyna.app.service.brs.ppm.app.EventApp#isSendNotice(dyna.app.service.brs.ppm.PPMImpl,
	 * dyna.common.bean.data.FoundationObject, dyna.common.bean.data.FoundationObject,
	 * dyna.common.bean.data.ppm.PMWarningRule)
	 */
	@Override
	public boolean isSendNotice(PPMSImpl ppm, FoundationObject projectFoundationObject, FoundationObject taskFoundationObject, EarlyWarning rule) throws ServiceRequestException
	{

		if (taskFoundationObject == null)
		{
			return false;
		}

		boolean isSend = false;
		PPMFoundationObjectUtil util = new PPMFoundationObjectUtil(taskFoundationObject);

		if (util.getTaskTypeEnum() == TaskTypeEnum.SUMMARY)
		{
			return isSend;
		}

		if (util.getPlanStartTime() == null)
		{
			return isSend;
		}

		if (util.getTaskStatusEnum() == TaskStatusEnum.INI)
		{
			// 0=<差距时间=<间隔天数
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			String compareDate = DateFormat.getDifferenceDay(util.getPlanStartTime(), c.getTime()) + "";
			if (!StringUtils.isNullString(rule.getInterval()) && !StringUtils.isNullString(compareDate))
			{
				Double gapTime = Double.valueOf(compareDate);
				Double confTime = Double.valueOf(rule.getInterval());
				if (gapTime >= 0 && gapTime <= confTime)
				{
					isSend = true;
				}
				else
				{
					isSend = false;
				}
			}
		}
		return isSend;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dyna.app.service.brs.ppms.app.AbstractEventApp#getDelayPercent(dyna.app.service.brs.ppms.PPMSImpl,
	 * dyna.common.bean.data.FoundationObject, dyna.common.bean.data.FoundationObject,
	 * dyna.common.bean.data.ppms.EarlyWarning)
	 */
	@Override
	public Object getDelayPercent(PPMSImpl ppms, FoundationObject projectFoundationObject, FoundationObject taskFoundationObject, EarlyWarning rule)
	{
		PPMFoundationObjectUtil util = new PPMFoundationObjectUtil(taskFoundationObject);
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		return DateFormat.getDifferenceDay(util.getPlanStartTime(), c.getTime()) + "";
	}
}
