package dyna.app.service.brs.wfi.activity.app.application;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.wfi.WFIImpl;
import dyna.app.service.brs.wfi.activity.app.ActivityRuntimeApplication;
import dyna.app.service.brs.wfi.activity.app.ProcApp;
import dyna.app.service.brs.wfi.activity.app.ProcAppFactory;
import dyna.common.dto.wf.ActivityRuntime;
import dyna.common.exception.ServiceRequestException;
import dyna.common.systemenum.ActRuntimeModeEnum;
import dyna.common.systemenum.DecisionEnum;
import dyna.common.systemenum.WorkflowActivityType;
import dyna.common.systemenum.WorkflowApplicationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 应用类型节点相关操作
 * 
 * @author lizw
 *
 */
@Component
public class ApplyActivityApplication extends AbstractServiceStub<WFIImpl> implements ActivityRuntimeApplication
{

	@Override
	public void finishActivity(ActivityRuntime activity, DecisionEnum decide) throws ServiceRequestException
	{
		if (WorkflowApplicationType.CHANGE_STATUS.name().equalsIgnoreCase(activity.getApplicationName()))
		{
			this.stubService.getAttachStub().excuteERPWorkflow(activity.getProcessRuntimeGuid(), activity.getGuid());
		}
	}

	@Override
	public ActivityRuntime fireNextAcceptActivity(ActivityRuntime nextActRt) throws ServiceRequestException
	{
		ActivityRuntime returnActrt = null;
		if (nextActRt.getActMode() == ActRuntimeModeEnum.BYPASS)
		{
			returnActrt = this.stubService.getActivityRuntimeStub().fireNextAcceptActivity(nextActRt);
			return returnActrt;
		}
		ProcApp procApp = ProcAppFactory.getProcAppFactory().getProcApp(nextActRt.getApplicationName());
		boolean isSkip = false;
		if (procApp != null)
		{
			Object execute = procApp.execute(nextActRt);
			if (execute != null && WorkflowApplicationType.ACTION.name().equalsIgnoreCase(nextActRt.getApplicationName()))
			{
				if (execute instanceof Boolean && (Boolean) execute == true)
				{
					return nextActRt;
				}
			}
		}

		DecisionEnum decide = isSkip ? DecisionEnum.SKIP : DecisionEnum.ACCEPT;
		this.stubService.getActivityRuntimeStub().finishActivity(nextActRt, decide);

		returnActrt = this.stubService.getActivityRuntimeStub().fireNextAcceptActivity(nextActRt);

		return returnActrt;
	}

	@Override
	public ActivityRuntime fireRejectActivity(ActivityRuntime rejAct) throws ServiceRequestException
	{
		ProcApp procApp = ProcAppFactory.getProcAppFactory().getProcApp(rejAct.getApplicationName());
		boolean isSkip = false;
		if (procApp != null)
		{
			Object execute = procApp.execute(rejAct);
			if (execute != null && WorkflowApplicationType.ACTION.name().equalsIgnoreCase(rejAct.getApplicationName()))
			{
				if (execute instanceof Boolean && (Boolean) execute == true)
				{
					return rejAct;
				}
			}

			if (rejAct.getActType() == WorkflowActivityType.NOTIFY && execute != null)
			{
				if (execute instanceof Boolean && (Boolean) execute == true)
				{
					isSkip = true;
				}
			}

		}

		DecisionEnum decide = isSkip ? DecisionEnum.SKIP : DecisionEnum.REJECT;
		this.stubService.getActivityRuntimeStub().finishActivity(rejAct, decide);

		return null;
	}

	@Override
	public boolean performActivityRuntime(ActivityRuntime activity, DecisionEnum decide) throws ServiceRequestException
	{
		return true;
	}

}
