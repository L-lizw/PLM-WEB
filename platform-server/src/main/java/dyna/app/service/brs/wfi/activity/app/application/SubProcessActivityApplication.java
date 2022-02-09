package dyna.app.service.brs.wfi.activity.app.application;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.wfi.WFIImpl;
import dyna.app.service.brs.wfi.activity.app.ActivityRuntimeApplication;
import dyna.common.dto.wf.ActivityRuntime;
import dyna.common.dto.wf.Performer;
import dyna.common.exception.ServiceRequestException;
import dyna.common.systemenum.ActRuntimeModeEnum;
import dyna.common.systemenum.DecisionEnum;
import dyna.common.util.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 子流程
 * 
 * @author lizw
 *
 */
@Component
public class SubProcessActivityApplication extends AbstractServiceStub<WFIImpl> implements ActivityRuntimeApplication
{

	@Override
	public void finishActivity(ActivityRuntime activity, DecisionEnum decide) throws ServiceRequestException
	{
		//do nothing
		return;
	}

	@Override
	public ActivityRuntime fireNextAcceptActivity(ActivityRuntime nextActRt) throws ServiceRequestException
	{
		ActivityRuntime returnActrt = null;

		List<Performer> listPerformer = this.stubService.listPerformer(nextActRt.getGuid());
		if (SetUtils.isNullList(listPerformer) && nextActRt.getActMode() == ActRuntimeModeEnum.BYPASS)
		{
			this.stubService.getActivityRuntimeStub().finishActivity(nextActRt, DecisionEnum.ACCEPT);
			returnActrt = this.stubService.getActivityRuntimeStub().fireNextAcceptActivity(nextActRt);
		}
		else
		{
			this.stubService.getActivityRuntimeStub().setAsCurrentActivity(nextActRt);

			// 执行人为空是，跳过活动节点
			if (SetUtils.isNullList(listPerformer))
			{
				this.stubService.performActivityRuntime(nextActRt.getGuid(), null, DecisionEnum.SKIP, null, null, null, false);
			}
		}
		return returnActrt;
	}

	@Override
	public ActivityRuntime fireRejectActivity(ActivityRuntime rejAct) throws ServiceRequestException
	{
		ActRuntimeModeEnum actMode = rejAct.getActMode();
		if (actMode != ActRuntimeModeEnum.BYPASS)
		{
			this.stubService.getActivityRuntimeStub().setAsCurrentActivity(rejAct);

			this.stubService.getActivityRuntimeStub().resetSubWorkflow(rejAct);

			this.stubService.getPerformerStub().resetPerformersOfActivityRutime(rejAct.getGuid());

			this.stubService.getActivityRuntimeStub().resetRelatedActivityRuntime(rejAct);

			// 拒绝后继续执行
			List<Performer> listPerformer = this.stubService.listPerformer(rejAct.getGuid());

			// 执行人为空是，跳过活动节点
			if (SetUtils.isNullList(listPerformer))
			{
				this.stubService.performActivityRuntime(rejAct.getGuid(), null, DecisionEnum.SKIP, null, null, null, false);
			}

		}
		return null;
	}

	@Override
	public boolean performActivityRuntime(ActivityRuntime activity, DecisionEnum decide) throws ServiceRequestException
	{
		return true;
	}

}
