/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: 活动操作分支
 * Wanglei 2010-11-5
 */
package dyna.app.service.brs.wfi.activity.app.application;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.wfi.WFIImpl;
import dyna.app.service.brs.wfi.activity.app.ActivityRuntimeApplication;
import dyna.common.dto.wf.ActivityRuntime;
import dyna.common.exception.ServiceRequestException;
import dyna.common.systemenum.DecisionEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 开始节点相关操作
 * 
 * @author lizw
 *
 */
@Component
public class BeginActivityApplication extends AbstractServiceStub<WFIImpl> implements ActivityRuntimeApplication
{
	@Override
	public void finishActivity(ActivityRuntime activity, DecisionEnum decide) throws ServiceRequestException
	{
		// TODO Auto-generated method stub
		this.stubService.getProcessRuntimeStub().runProcess(activity.getProcessRuntimeGuid());
	}

	@Override
	public ActivityRuntime fireNextAcceptActivity(ActivityRuntime activity) throws ServiceRequestException
	{
		// TODO Auto-generated method stub
		this.stubService.getProcessRuntimeStub().holdonProcess(activity.getProcessRuntimeGuid());

		return null;
	}

	@Override
	public ActivityRuntime fireRejectActivity(ActivityRuntime activity) throws ServiceRequestException
	{
		// TODO Auto-generated method stub
		this.stubService.getProcessRuntimeStub().holdonProcess(activity.getProcessRuntimeGuid());

		return null;
	}

	@Override
	public boolean performActivityRuntime(ActivityRuntime activity, DecisionEnum decide) throws ServiceRequestException
	{
		// TODO Auto-generated method stub
		return true;
	}

}
