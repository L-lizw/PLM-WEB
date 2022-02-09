/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: NotifyAppImpl
 * Wanglei 2010-11-11
 */
package dyna.app.service.brs.wfi.activity.app.impl;

import java.util.List;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.wfi.WFIImpl;
import dyna.app.service.brs.wfi.activity.app.ProcApp;
import dyna.common.dto.wf.ActivityRuntime;
import dyna.common.dto.wf.Performer;
import dyna.common.exception.ServiceRequestException;
import dyna.common.systemenum.MailMessageType;
import dyna.common.util.SetUtils;
import org.springframework.stereotype.Component;

/**
 * @author Wanglei
 * 
 */
@Component
public class NotifyAppImpl extends AbstractServiceStub<WFIImpl> implements ProcApp
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see dyna.app.service.brs.wfi.app.ProcApp#invoke(dyna.common.bean.data.system.ActivityRuntime)
	 */
	@Override
	public Object execute(ActivityRuntime activity) throws ServiceRequestException
	{
		// send mail
		String actRtGuid = activity.getGuid();
		// String procRtGuid = activity.getProcessRuntimeGuid();

		List<Performer> listAllPerformer = this.stubService.listPerformer(actRtGuid);
		// this.stubService.getPerformerStub().sendMail(listAllPerformer, procRtGuid, actRtGuid);
		this.stubService.getNoticeStub().sendPerformMail(listAllPerformer, activity, MailMessageType.WORKFLOWNOTIFY);
		Boolean isSkip = false;
		if (SetUtils.isNullList(listAllPerformer))
		{
			isSkip = true;
		}
		return isSkip;
	}

}
