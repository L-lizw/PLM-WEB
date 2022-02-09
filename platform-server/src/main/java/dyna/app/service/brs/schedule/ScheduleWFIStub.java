package dyna.app.service.brs.schedule;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.helper.ServiceRequestExceptionWrap;
import dyna.common.bean.model.wf.template.WorkflowTemplateAct;
import dyna.common.dto.aas.User;
import dyna.common.dto.aas.UserAgent;
import dyna.common.dto.template.wft.WorkflowTemplateActInfo;
import dyna.common.dto.wf.ActivityRuntime;
import dyna.common.exception.DynaDataException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import dyna.common.systemenum.DecisionEnum;
import dyna.common.systemenum.MailCategoryEnum;
import dyna.common.systemenum.MailMessageType;
import dyna.common.systemenum.OverTimeActionEnum;
import dyna.common.util.DateFormat;
import dyna.common.util.SetUtils;
import dyna.common.util.StringUtils;
import dyna.net.service.data.SystemDataService;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.*;

/**
 * @author Lizw
 * @date 2022/1/30
 **/
@Component
public class ScheduleWFIStub extends AbstractServiceStub<ScheduleServiceImpl>
{

	 protected void runNoticeAction4Workflow()
	 {
	 	try
	    {
		    this.stubService.getWFI().runNoticeAction();
	    }
	    catch (Throwable e)
	    {
		    DynaLogger.error("run Notice:", e);
	    }
	 }

	protected void runOverTimeAction()
	{
		try
		{
			String ACTRTGUID = "ACTRTGUID";
			SystemDataService sds = this.stubService.getSystemDataService();
			List<WorkflowTemplateActInfo> workflowActivityList = new ArrayList<>();
			Date currentTime = DateFormat.parse(DateFormat.formatYMD(new Date()), DateFormat.PTN_YMD);
			try
			{
				Map<String, Object> searchConditionMap = new HashMap<>();

				List<ActivityRuntime> resultList = sds.query(ActivityRuntime.class, searchConditionMap, "selectAllDeferredActrt");
				List<ActivityRuntime> actrtList = new ArrayList<>();
				if (!SetUtils.isNullList(resultList))
				{
					for (ActivityRuntime activity : resultList)
					{
						if (activity.getDeadline() != null)
						{
							Date deadLine = new Date(DateFormat.formatYMD(activity.getDeadline()));
							if (currentTime.compareTo(deadLine) >= 0)
							{
								actrtList.add(activity);
							}
						}
					}
				}

				if (!SetUtils.isNullList(actrtList))
				{
					for (ActivityRuntime actrt : actrtList)
					{
						String actrtName = actrt.getActrtName();
						String wfTemplateGuid = (String) actrt.get("WFTEMPLATEGUID");

						WorkflowTemplateActInfo wfTemplate = this.stubService.getWFI().getWorkflowTemplateActSetInfo(wfTemplateGuid, actrtName);
						if (wfTemplate != null)
						{
							workflowActivityList.add(wfTemplate);
						}
					}
				}
			}
			catch (DynaDataException e)
			{
				throw ServiceRequestExceptionWrap.createByDynaDataException(this.stubService, e);
			}

			if (workflowActivityList != null)
			{
				for (WorkflowTemplateActInfo templateAct : workflowActivityList)
				{
					String overTimeAction = templateAct.getOverTimeAction();
					String actRtGuid = (String) templateAct.get(ACTRTGUID);

					OverTimeActionEnum overTimeActionEnum = OverTimeActionEnum.getEnum(overTimeAction);
					switch (overTimeActionEnum)
					{
					case COMPLETE:
						this.autoPerform(actRtGuid, DecisionEnum.ACCEPT);
						break;
					case SKIP:
						this.autoPerform(actRtGuid, DecisionEnum.SKIP);
						break;
					case WAIT:
						break;
					default:
						break;
					}
				}
			}
		}
		catch (Throwable e)
		{
			DynaLogger.error("run OverTimeAction:", e);
		}

	}

	private void autoPerform(String actRtGuid, DecisionEnum decise) throws ServiceRequestException
	{
		ActivityRuntime activityRuntime = this.stubService.getWFI().getActivityRuntime(actRtGuid);

		List<User> listNotFinishPerformer = this.stubService.getWFI().listNotFinishPerformer(actRtGuid);
		if (!SetUtils.isNullList(listNotFinishPerformer))
		{
			for (User user : listNotFinishPerformer)
			{
				if (activityRuntime != null && !activityRuntime.isFinished())
				{
					this.stubService.getWFI().performActivityRuntime(actRtGuid, null, decise, null, user.getGuid(), null, false);
				}
				else
				{
					break;
				}
			}

		}
		else
		{
			this.stubService.getWFI().performActivityRuntime(actRtGuid, null, decise, null, null, null, false);
		}
	}

	protected void runOverTimeAgent()
	{
		SystemDataService sds = this.stubService.getSystemDataService();
		List<UserAgent> userAgentList = null;
		try
		{
			Map<String, Object> searchConditionMap = new HashMap<>();
			searchConditionMap.put("ISOVERTIME", "Y");

			userAgentList = sds.query(UserAgent.class, searchConditionMap);

			if (!SetUtils.isNullList(userAgentList))
			{
				for (UserAgent agent : userAgentList)
				{
					this.stubService.getAAS().obsoleteUserAgent(agent);
					String mailContent = this.formatMailContent(agent);
					try
					{
						if (mailContent != null)
						{
							this.stubService.getSMS().sendMailToUser(this.formatMsg("ID_CLIENT_AGENT_INVALID_MAIL_SEND_SUBJECT", this.stubService.getUserSignature().getUserName()),
									mailContent, MailCategoryEnum.INFO, null, agent.getAgentGuid(), MailMessageType.AGENTNOTIFY);
						}
					}
					catch (ServiceRequestException e)
					{
						this.stubService.getSMS().sendMailToUser(this.formatMsg("ID_CLIENT_AGENT_MAIL_SEND_FAILED", null),
								e.getMessage() == null ? e.getCause().getMessage() : e.getMessage(), MailCategoryEnum.INFO, null, agent.getPrincipalGuid(),
								MailMessageType.AGENTNOTIFY);
					}
				}
			}
		}
	catch (Throwable e)
	{
		DynaLogger.error("run OverTimeAction:", e);
	}
	}

	private String formatMailContent(UserAgent agent)
	{
		if (agent != null)
		{
			String fromUser = agent.getPrincipalName();
			return MessageFormat.format("ID_CLIENT_AGENT_MAIL_CONTENT_INV", fromUser);
		}
		return null;
	}

	private String formatMsg(String msgId, String val) throws ServiceRequestException
	{
		String msg = this.stubService.getMSRM().getMSRString(msgId, this.stubService.getUserSignature().getLanguageEnum().toString());
		if (StringUtils.isNullString(val))
		{
			return msg;
		}
		return MessageFormat.format(msg, val);
	}

}
