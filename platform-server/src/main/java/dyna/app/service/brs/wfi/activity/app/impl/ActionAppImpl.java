/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: ChangPhaseAppImpl
 * Wanglei 2010-11-11
 */
package dyna.app.service.brs.wfi.activity.app.impl;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.wfi.WFIImpl;
import dyna.app.service.brs.wfi.activity.app.ProcApp;
import dyna.app.service.helper.ServiceRequestExceptionWrap;
import dyna.common.bean.data.input.InputObjectWrokflowActionImpl;
import dyna.common.dto.wf.ActivityRuntime;
import dyna.common.dto.wf.ProcessRuntime;
import dyna.common.exception.DynaDataException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * 
 */
@Component
public class ActionAppImpl extends AbstractServiceStub<WFIImpl> implements ProcApp
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see dyna.app.service.brs.wfi.app.ProcApp#invoke(dyna.common.bean.data.system.ActivityRuntime)
	 */
	@Override
	public Boolean execute(ActivityRuntime activity) throws ServiceRequestException
	{
		String procRtGuid = activity.getProcessRuntimeGuid();
		ProcessRuntime processRuntime = this.stubService.getProcessRuntime(procRtGuid);
		boolean isMust = this.stubService.getEoss().isMustExecuteWorkflowActionFromUI(processRuntime.getName(),
				activity.getName());
		if (isMust)
		{
			return true;
		}

		InputObjectWrokflowActionImpl inputObject = new InputObjectWrokflowActionImpl(processRuntime.getGuid(),
				processRuntime.getName(), activity.getGuid(), activity.getName());
		try
		{
			this.stubService.getEoss().executeWorkflowAction(inputObject);
		}
		// 执行此段代码时，工作流中不能存在有UI操作的脚本，不然可能引起工作流中断。
		catch (DynaDataException e)
		{
			DynaLogger.error(e.getMessage());
			throw ServiceRequestExceptionWrap.createByDynaDataException(this.stubService, e);
		}
		catch (Exception e)
		{
			DynaLogger.error(e.getMessage());
			if (e instanceof ServiceRequestException)
			{
				throw (ServiceRequestException) e;
			}
			else
			{
				throw ServiceRequestException.createByException("ID_APP_INST_EVENT_EXCEPTION", e);
			}
		}

		return null;
	}

}
