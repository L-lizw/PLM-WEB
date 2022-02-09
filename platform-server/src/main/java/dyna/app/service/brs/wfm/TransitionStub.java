/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: ActivitiyStub
 * Wanglei 2011-4-1
 */
package dyna.app.service.brs.wfm;

import dyna.app.service.AbstractServiceStub;
import dyna.common.bean.model.wf.WorkflowProcess;
import dyna.common.dto.model.wf.WorkflowProcessInfo;
import dyna.common.dto.model.wf.WorkflowTransitionInfo;
import dyna.common.exception.ServiceRequestException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Wanglei
 *
 */
@Component
public class TransitionStub extends AbstractServiceStub<WFMImpl>
{

	protected List<WorkflowTransitionInfo> listTransition(String procGuid, String procName) throws ServiceRequestException
	{
		if (procName == null && procGuid != null)
		{
			WorkflowProcessInfo process = this.stubService.getProcessModelInfoByGuid(procGuid);
			procName = process == null ? null : process.getName();
		}
		if (procName == null)
		{
			return null;
		}
		WorkflowProcess process = this.stubService.getWfModelCacheStub().getWorkflowProcessByName(procName);
		List<WorkflowTransitionInfo> wfTransitionList = process.listAllTransition();

		return wfTransitionList;
	}

}
