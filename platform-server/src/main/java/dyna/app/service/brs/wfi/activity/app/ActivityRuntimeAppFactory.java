package dyna.app.service.brs.wfi.activity.app;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.wfi.WFIImpl;
import dyna.app.service.brs.wfi.activity.app.application.*;
import dyna.common.systemenum.WorkflowActivityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ActivityRuntimeAppFactory extends AbstractServiceStub<WFIImpl>
{
	private final Map<WorkflowActivityType, ActivityRuntimeApplication> APP_MAP = new HashMap<WorkflowActivityType, ActivityRuntimeApplication>();

	@Autowired
	private EndActivityApplication endActivityApplication;
	@Autowired
	private BeginActivityApplication beginActivityApplication;
	@Autowired
	private ApplyActivityApplication applyActivityApplication;
	@Autowired
	private ManualActivityApplication manualActivityApplication;
	@Autowired
	private NotifyActivityApplication notifyActivityApplication;
	@Autowired
	private RouteActivityApplication routeActivityApplication;
	@Autowired
	private SubProcessActivityApplication subProcessActivityApplication;

	public void init()
	{
		this.addActivityApplication(WorkflowActivityType.END, endActivityApplication);
		this.addActivityApplication(WorkflowActivityType.BEGIN, beginActivityApplication);
		this.addActivityApplication(WorkflowActivityType.APPLICATION, applyActivityApplication);
		this.addActivityApplication(WorkflowActivityType.MANUAL, manualActivityApplication);
		this.addActivityApplication(WorkflowActivityType.NOTIFY, notifyActivityApplication);
		this.addActivityApplication(WorkflowActivityType.ROUTE, routeActivityApplication);
		this.addActivityApplication(WorkflowActivityType.SUB_PROCESS, subProcessActivityApplication);
	}

	public ActivityRuntimeApplication getActivityApplication(WorkflowActivityType type)
	{
		return this.APP_MAP.get(type);
	}

	public void addActivityApplication(WorkflowActivityType type, ActivityRuntimeApplication actrtApp)
	{
		this.APP_MAP.put(type, actrtApp);
	}
}
