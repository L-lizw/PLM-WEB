/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: DataService服务的实现
 * XiaSheng 2010-3-17
 * JinagHL 2011-5-5
 */
package dyna.data.service.wf;

import dyna.common.dto.wf.ActivityRuntime;
import dyna.common.dto.wf.ProcAttach;
import dyna.common.dto.wf.ProcessRuntime;
import dyna.common.exception.DynaDataException;
import dyna.common.systemenum.WorkflowActivityType;
import dyna.data.service.DataRuleService;
import dyna.net.service.data.DSCommonService;
import dyna.net.service.data.SystemDataService;
import dyna.net.service.data.WorkFlowService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * DataService服务的实现
 * 
 * @author XiaSheng
 */
@DubboService
public class WorkFlowServiceImpl extends DataRuleService implements WorkFlowService
{
	@Autowired DSCommonService   dsCommonService;
	@Autowired SystemDataService systemDataService;
	@Autowired
	private    WorkflowStub      workflowStub			= null;

	protected DSCommonService getDsCommonService(){return this.dsCommonService; }

	protected SystemDataService getSystemDataService(){return this.systemDataService; }

	protected WorkflowStub getWorkflowStub()
	{
		return this.workflowStub;
	}

	@Override
	public void deleteUnExistsAttach(String procRtGuid)
	{
		this.getWorkflowStub().deleteUnExistsAttach(procRtGuid);
	}

	@Override
	public List<ProcAttach> listRevisionInWF(String masterGuid, String classGuid)
	{
		return this.getWorkflowStub().listAllRevisionInWF(masterGuid, classGuid);
	}

	@Override
	public ActivityRuntime getActivityRuntime(String actRtGuid)
	{
		return this.getWorkflowStub().getActivityRuntime(actRtGuid);
	}

	@Override
	public ActivityRuntime getBeginActivityRuntime(String procRtGuid)
	{
		return this.getWorkflowStub().getActivityRuntimeByType(procRtGuid, WorkflowActivityType.BEGIN);
	}

	@Override
	public ActivityRuntime getEndActivityRuntime(String procRtGuid)
	{
		return this.getWorkflowStub().getActivityRuntimeByType(procRtGuid, WorkflowActivityType.END);
	}

	@Override
	public void deleteProcess(ProcessRuntime processRuntime) throws DynaDataException
	{
		this.getWorkflowStub().deleteProcess(processRuntime);
	}
}
