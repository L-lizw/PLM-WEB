/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: UnlockAppImpl
 * Wanglei 2010-11-11
 */
package dyna.app.service.brs.wfi.activity.app.impl;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.wfi.WFIImpl;
import dyna.app.service.brs.wfi.activity.app.ProcApp;
import dyna.common.dto.wf.ActivityRuntime;
import dyna.common.exception.ServiceRequestException;
import org.springframework.stereotype.Component;

/**
 * @author Wanglei
 * 
 */
@Component
public class UnlockAppImpl extends AbstractServiceStub<WFIImpl> implements ProcApp
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see dyna.app.service.brs.wfi.app.ProcApp#invoke(dyna.common.bean.data.system.ActivityRuntime)
	 */
	@Override
	public Object execute(ActivityRuntime activity) throws ServiceRequestException
	{
		String procRtGuid = activity.getProcessRuntimeGuid();
		this.stubService.getLockStub().unlock(procRtGuid);
		return null;
	}

}
