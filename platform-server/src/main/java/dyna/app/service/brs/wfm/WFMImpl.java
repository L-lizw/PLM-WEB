/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: WFIImpl
 * Wanglei 2010-11-4
 */
package dyna.app.service.brs.wfm;

import dyna.app.service.BusinessRuleService;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.conf.ServiceDefinition;
import dyna.common.dto.SystemWorkflowActivity;
import dyna.common.dto.model.bmbo.BOInfo;
import dyna.common.dto.model.cls.ClassInfo;
import dyna.common.dto.model.wf.*;
import dyna.common.dto.template.wft.WorkflowTemplateInfo;
import dyna.common.dto.wf.ApproveTemplate;
import dyna.common.dto.wf.ApproveTemplateDetail;
import dyna.common.dto.wf.GraphTransition;
import dyna.common.exception.AuthorizeException;
import dyna.common.exception.ServiceRequestException;
import dyna.net.service.brs.*;
import dyna.net.service.data.SystemDataService;
import dyna.net.service.data.model.ClassModelService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.util.List;

/**
 * WFM 实现类
 *
 * @author Lizw
 */
@Order(2)
@DubboService public class WFMImpl extends BusinessRuleService implements WFM
{
	@DubboReference private ClassModelService classModelService;
	@DubboReference private SystemDataService systemDataService;

	@Autowired private ActivitiyStub          activitiyStub       ;
	@Autowired private ApproveTemplateStub    processTemplateStub ;
	@Autowired private ProcessStub            processStub         ;
	@Autowired private TransitionStub         transitionStub      ;
	@Autowired private WFGraphStub            wfGraphStub         ;
	@Autowired private WorkflowModelCacheStub wfModelCacheStub    ;
	@Autowired private ScriptStub             scriptStub          ;

	@Override public void init(ServiceDefinition serviceDefinition)
	{
		super.init(serviceDefinition);
		this.getWfModelCacheStub().loadModel();
	}

	protected ClassModelService getClassModelService()
	{
		return this.classModelService;
	}

	protected SystemDataService getSystemDataService()
	{
		return this.systemDataService;
	}

	/**
	 * @return the activitiyStub
	 */
	public ActivitiyStub getActivitiyStub()
	{
		return this.activitiyStub;
	}

	/**
	 * @return the processTemplateStub
	 */
	protected ApproveTemplateStub getProcessTemplateStub()
	{
		return this.processTemplateStub;
	}

	/**
	 * @return the processStub
	 */
	public ProcessStub getProcessStub()
	{
		return this.processStub;
	}

	/**
	 * @return the transitionStub
	 */
	protected TransitionStub getTransitionStub()
	{
		return this.transitionStub;
	}

	/**
	 * @return the wfGraphStub
	 */
	protected WFGraphStub getWfGraphStub()
	{
		return this.wfGraphStub;
	}

	/**
	 * @return the wfGraphStub
	 */
	public WorkflowModelCacheStub getWfModelCacheStub()
	{
		return this.wfModelCacheStub;
	}

	public ScriptStub getWfScriptStub()
	{
		return this.scriptStub;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.app.service.BusinessRuleService#authorize(java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override public void authorize(Method method, Object... args) throws AuthorizeException
	{
		super.authorize(method, args);
	}

	protected synchronized EMM getEMM() throws ServiceRequestException
	{
		try
		{
			return this.getRefService(EMM.class);
		}
		catch (Exception e)
		{
			throw new ServiceRequestException(null, e.getMessage(), e.fillInStackTrace());
		}

	}

	public synchronized WFI getWFI() throws ServiceRequestException
	{
		try
		{
			return this.getRefService(WFI.class);
		}
		catch (Exception e)
		{
			throw new ServiceRequestException(null, e.getMessage(), e.fillInStackTrace());
		}
	}

	protected synchronized BOAS getBOAS() throws ServiceRequestException
	{
		try
		{
			return this.getRefService(BOAS.class);
		}
		catch (Exception e)
		{
			throw new ServiceRequestException(null, e.getMessage(), e.fillInStackTrace());
		}
	}

	protected synchronized AAS getAAS() throws ServiceRequestException
	{
		try
		{
			return this.getRefService(AAS.class);
		}
		catch (Exception e)
		{
			throw new ServiceRequestException(null, e.getMessage(), e.fillInStackTrace());
		}
	}

	protected synchronized BOMS getBOMS() throws ServiceRequestException
	{
		try
		{
			return this.getRefService(BOMS.class);
		}
		catch (Exception e)
		{
			throw new ServiceRequestException(null, e.getMessage(), e.fillInStackTrace());
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.WFM#getProcess(java.lang.String)
	 */
	@Override public WorkflowProcessInfo getProcessModelInfo(String procName) throws ServiceRequestException
	{
		return this.getProcessStub().getProcessInfo(procName);
	}

	@Override public List<WorkflowProcessInfo> listAllWorkflowInfo() throws ServiceRequestException
	{
		// TODO Auto-generated method stub
		return this.getProcessStub().listAllWorkflowProcessInfo();
	}

	@Override public List<String> listAllWorkflowName() throws ServiceRequestException
	{
		return this.getProcessStub().listAllWorkflowName();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.WFI#getWorkflowActivityByName(java.lang.String, java.lang.String)
	 */
	@Override public WorkflowActivityInfo getWorkflowActivityInfoByName(String procName, String actName) throws ServiceRequestException
	{
		return this.getActivitiyStub().getWorkflowActivityInfoByName(procName, actName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.WFI#getWorkflowBeginActivityByName(java.lang.String)
	 */
	@Override public WorkflowActivityInfo getWorkflowBeginActivityInfoByName(String procName) throws ServiceRequestException
	{
		return this.getActivitiyStub().getWorkflowBeginActivityInfoByName(procName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.WFM#listPerformableFromActivity(java.lang.String, java.lang.String)
	 */
	@Override public List<WorkflowActivityInfo> listAcceptaleFromActivity(String procName, String actName) throws ServiceRequestException
	{
		return this.getActivitiyStub().listAcceptaleFromActivity(procName, actName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.WFM#listActivity(java.lang.String)
	 */
	@Override public List<WorkflowActivityInfo> listAllActivityInfo(String wfprocguid, String procName) throws ServiceRequestException
	{
		return this.getActivitiyStub().listActivityInfo(wfprocguid, procName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.WFM#listPerformableActivity(java.lang.String)
	 */
	@Override public List<WorkflowActivityInfo> listPerformableActivity(String procName) throws ServiceRequestException
	{
		return this.getActivitiyStub().listPerformableActivity(procName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.WFM#listPerformableToActivity(java.lang.String, java.lang.String)
	 */
	@Override public List<WorkflowActivityInfo> listRejectableFromActivity(String procName, String actName) throws ServiceRequestException
	{
		return this.getActivitiyStub().listRejectableFromActivity(procName, actName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.WFI#listSortedPerformableActivity(java.lang.String)
	 */
	@Override public List<WorkflowActivityInfo> listSortedPerformableActivity(String procName) throws ServiceRequestException
	{
		return this.getActivitiyStub().listSortedPerformableActivity(procName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.WFI#listTransition(java.lang.String)
	 */
	@Override public List<WorkflowTransitionInfo> listTransitionInfo(String procGuid, String procName) throws ServiceRequestException
	{
		return this.getTransitionStub().listTransition(procGuid, procName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.WFI#listGraphTransition(java.lang.String)
	 */
	@Override public List<GraphTransition> listGraphTransition(String procName) throws ServiceRequestException
	{
		return this.getWfGraphStub().listGraphTransition(procName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.WFI#listAcceptaleFromActivityNotApplication(java.lang.String, java.lang.String)
	 */
	@Override public List<WorkflowActivityInfo> listAcceptaleFromActivityNotApplication(String procName, String actName) throws ServiceRequestException
	{
		return this.getWfGraphStub().listAcceptaleFromActivityNotApplication(procName, actName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.WFI#listActivityNotApplication(java.lang.String)
	 */
	@Override public List<Object> listActivityGraphInfo(String procName) throws ServiceRequestException
	{
		return this.getWfGraphStub().listActivityGraphInfo(procName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.WFI#listProcessTemplate(java.lang.String, java.lang.String)
	 */
	@Override public List<ApproveTemplate> listProcessTemplate(String procName, String perfGuid) throws ServiceRequestException
	{
		return this.getProcessTemplateStub().listProcessTemplate(procName, perfGuid);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.WFI#listProcessTemplateDetail(java.lang.String)
	 */
	@Override public List<ApproveTemplateDetail> listProcessTemplateDetail(String templateGuid) throws ServiceRequestException
	{
		return this.getProcessTemplateStub().listProcessTemplateDetail(templateGuid);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.WFI#listProcessTemplateDetailByActivityName(java.lang.String, java.lang.String)
	 */
	@Override public List<ApproveTemplateDetail> listProcessTemplateDetailByActivityName(String templateGuid, String actName) throws ServiceRequestException
	{
		return this.getProcessTemplateStub().listProcessTemplateDetail(templateGuid, actName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.WFI#batchSaveApproveTemplate(java.util.List, java.util.List)
	 */
	@Override public void batchSaveApproveTemplate(List<ApproveTemplate> addApproveTemplateList, List<ApproveTemplate> updateApproveTemplateList) throws ServiceRequestException
	{
		this.getProcessTemplateStub().batchSaveApproveTemplate(addApproveTemplateList, updateApproveTemplateList);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.WFI#batchDeleteApproveTemplate(java.util.List)
	 */
	@Override public void batchDeleteApproveTemplate(List<ApproveTemplate> deleteApproveTemlateList) throws ServiceRequestException
	{
		this.getProcessTemplateStub().batchDeleteApproveTemplate(deleteApproveTemlateList);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.WFI#listRunnableProcessTemplate(dyna.common.bean.data.ObjectGuid)
	 */
	@Override public List<WorkflowTemplateInfo> listRunnableProcessTemplate(ObjectGuid objectGuid) throws ServiceRequestException
	{
		return this.getProcessStub().listRunnableProcessTemplate(objectGuid);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.WFI#canApplyExecutorTemplate(java.lang.String, java.lang.String)
	 */
	@Override public boolean canApplyExecutorTemplate(String processGuid, String executorTemplateGuid) throws ServiceRequestException
	{
		return this.getProcessTemplateStub().canApplyExecutorTemplate(processGuid, executorTemplateGuid);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.WFI#listRunnableProcessTemplate(java.lang.String)
	 */
	@Override public List<WorkflowTemplateInfo> listRunnableProcessTemplate(String wfName) throws ServiceRequestException
	{
		return this.getProcessStub().listSubRunnableProcessTemplate(wfName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.EMM#listManualActivity(java.lang.String)
	 */
	@Override public List<SystemWorkflowActivity> listManualActivity(String procName) throws ServiceRequestException
	{
		return this.getProcessStub().listManualAndNotifyActivity(procName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.EMM#listClassByProcName(java.lang.String)
	 */
	@Override public List<ClassInfo> listClassByProcName(String procName) throws ServiceRequestException
	{
		return this.getProcessStub().listClassByProcName(procName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.EMM#listBOInfoByProcName(java.lang.String, java.lang.String)
	 */
	@Override public List<BOInfo> listBOInfoByProcName(String procName, String bmGuid) throws ServiceRequestException
	{

		return this.getProcessStub().listBOInfoByProcName(procName, bmGuid);
	}

	@Override public List<WorkflowPhaseChangeInfo> listPhaseChangeInfo(String processguid, String activityguid) throws ServiceRequestException
	{
		// TODO Auto-generated method stub
		return this.getActivitiyStub().listPhaseChange(processguid, activityguid);
	}

	@Override public List<WorkflowLifecyclePhaseInfo> listLifecyclePhaseInfo(String procguid, String procName) throws ServiceRequestException
	{
		// TODO Auto-generated method stub
		return this.getProcessStub().listLifecyclePhaseInfo(procguid, procName);
	}

	@Override public List<WorkflowActrtLifecyclePhaseInfo> listActrtPhaseChangeInfo(String procguid, String procName) throws ServiceRequestException
	{
		// TODO Auto-generated method stub
		return this.getActivitiyStub().listActrtPhaseChange(procguid, procName);
	}

	@Override public List<WorkflowActrtActionInfo> listActrtAction(String processGuid, String activityGuid) throws ServiceRequestException
	{
		return this.getActivitiyStub().listActrtAction(processGuid, activityGuid);
	}

	@Override public List<WorkflowEventInfo> listEventInfo(String procGuid, String procName) throws ServiceRequestException
	{
		// TODO Auto-generated method stub
		return this.getProcessStub().listWorkflowEventInfo(procGuid, procName);
	}

	@Override public List<WorkflowActrtStatusInfo> listStatusChangeInfo(String processguid, String activityguid) throws ServiceRequestException
	{
		// TODO Auto-generated method stub
		return this.getActivitiyStub().listStatusChang(processguid, activityguid);
	}

	@Override public WorkflowProcessInfo getProcessModelInfoByGuid(String procModelGuid) throws ServiceRequestException
	{
		// TODO Auto-generated method stub
		return this.getProcessStub().getProcessInfoByGuid(procModelGuid);
	}

	@Override public WorkflowActivityInfo getWorkflowActivityInfo(String procModelGuid, String wfActivityGuid) throws ServiceRequestException
	{
		// TODO Auto-generated method stub
		return this.getActivitiyStub().getWorkflowActivityInfo(procModelGuid, wfActivityGuid);
	}

	@Override public List<WorkflowProcessInfo> getPhaseProcessList(String lifecycleName, String phaseName) throws ServiceRequestException
	{
		// TODO Auto-generated method stub
		return this.getProcessStub().getPhaseProcessList(lifecycleName, phaseName);
	}
}
