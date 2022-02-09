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
import dyna.common.systemenum.SystemStatusEnum;
import dyna.common.systemenum.ppms.TaskStatusEnum;
import dyna.common.systemenum.ppms.TaskTypeEnum;
import dyna.common.systemenum.ppms.WarningEvent;
import dyna.common.util.DateFormat;
import dyna.common.util.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author WangLHB
 *         任务超期
 */
@Component(WarningEvent.T_COMPLETE_DELAY)
public class TCompleteDelayImpl extends AbstractEventApp
{

	public TCompleteDelayImpl()
	{
		super(WarningEvent.WarningEventEnum.T_COMPLETE_DELAY);
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

		if (util.getPlanFinishTime() == null)
		{
			return isSend;
		}
		if (taskFoundationObject.getStatus() != SystemStatusEnum.RELEASE)
		{
			return isSend;
		}

		if (util.getTaskStatusEnum() == TaskStatusEnum.RUN)
		{
			// 差距时间>=间隔天数
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			String value = DateFormat.getDifferenceDay(c.getTime(), util.getPlanFinishTime()) + "";
			if (!StringUtils.isNullString(rule.getInterval()) && !StringUtils.isNullString(value))
			{
				Double gapTime = Double.valueOf(value);
				Double confTime = Double.valueOf(rule.getInterval());
				if (gapTime >= confTime)
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
		return DateFormat.getDifferenceDay(c.getTime(), util.getPlanFinishTime()) + "";
	}
}
