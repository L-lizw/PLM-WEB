/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: TRProcessImpl
 * Wanglei 2011-11-21
 */
package dyna.app.service.brs.wfi.track;

import dyna.app.core.track.impl.DefaultTrackerRendererImpl;
import dyna.app.server.context.ApplicationServerContext;
import dyna.app.service.brs.wfi.WFIImpl;
import dyna.common.bean.model.wf.template.WorkflowTemplateVo;
import dyna.common.bean.track.Tracker;
import dyna.common.dto.wf.ProcessRuntime;
import dyna.common.exception.ServiceNotFoundException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import dyna.common.util.StringUtils;
import dyna.net.service.brs.WFI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * 
 */
@Component
@Scope("prototype")
public class TRProcessImpl extends DefaultTrackerRendererImpl
{

	@Autowired
	private ApplicationServerContext serverContext;
	@Autowired
	private WFI                      wfi;
	/*
	 * (non-Javadoc)
	 * 
	 * @see dyna.app.core.track.impl.DefaultTrackerRendererImpl#getHandledObject(dyna.app.core.track.Tracker)
	 */
	@Override
	public String getHandledObject(Tracker tracker)
	{
		try
		{

			if ("createProcess".equalsIgnoreCase(tracker.getMethod().getName()))
			{
				((WFIImpl) wfi).setSignature(serverContext.getSystemInternalSignature());

				WorkflowTemplateVo workflowTemplateVo = wfi.getWorkflowTemplateDetail((String) tracker.getParameters()[0]);
				if (workflowTemplateVo != null)
				{
					String title = workflowTemplateVo.getTemplate().getWorkflowTemplateInfo()
							.getTitle(serverContext.getSystemInternalSignature().getLanguageEnum());
					if (tracker.getResult() != null && tracker.getResult() instanceof ProcessRuntime)
					{
						title = StringUtils.convertNULLtoString(title) + "|" + StringUtils.convertNULLtoString(((ProcessRuntime) tracker.getResult()).getDescription());
					}
					return title;
				}
			}
			else if ("recallProcessRuntime".equalsIgnoreCase(tracker.getMethod().getName()))
			{
				((WFIImpl) wfi).setSignature(serverContext.getSystemInternalSignature());

				ProcessRuntime processRuntime = wfi.getProcessRuntime((String) tracker.getParameters()[0]);
				return StringUtils
						.convertNULLtoString(processRuntime.getWFTemplateTitle(serverContext.getSystemInternalSignature().getLanguageEnum())) + "|"
						+ StringUtils.convertNULLtoString(processRuntime.getDescription());
			}
			else if ("resumeProcess".equalsIgnoreCase(tracker.getMethod().getName()))
			{
				((WFIImpl) wfi).setSignature(serverContext.getSystemInternalSignature());

				ProcessRuntime processRuntime = wfi.getProcessRuntime((String) tracker.getParameters()[0]);

				return StringUtils
						.convertNULLtoString(processRuntime.getWFTemplateTitle(serverContext.getSystemInternalSignature().getLanguageEnum())) + "|"
						+ StringUtils.convertNULLtoString(processRuntime.getDescription());
			}
			else if ("deleteProcess".equalsIgnoreCase(tracker.getMethod().getName()))
			{
				ProcessRuntime processRuntime = (ProcessRuntime) tracker.getParameters()[0];
				if (processRuntime != null)
				{
					// ProcessRuntime processRuntime = wfi.getProcessRuntime((String) tracker.getParameters()[0]);
					return StringUtils
							.convertNULLtoString(processRuntime.getWFTemplateTitle(serverContext.getSystemInternalSignature().getLanguageEnum()))
							+ "|" + StringUtils.convertNULLtoString(processRuntime.getDescription());
				}
			}

		}
		catch (ServiceRequestException e)
		{
			e.printStackTrace();
		}

		return super.getHandledObject(tracker);
	}
}
