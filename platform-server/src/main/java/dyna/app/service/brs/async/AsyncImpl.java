package dyna.app.service.brs.async;

import dyna.app.conf.AsyncConfig;
import dyna.app.service.BusinessRuleService;
import dyna.app.service.brs.pos.POSAsyncStub;
import dyna.app.service.brs.sms.SMSAsyncStub;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.bean.signature.Signature;
import dyna.common.bean.sync.AnalysisTask;
import dyna.common.bean.track.TrackerBuilder;
import dyna.app.conf.yml.ConfigurableServerImpl;
import dyna.common.dto.Mail;
import dyna.common.dto.template.wft.WorkflowTemplateScopeRTInfo;
import dyna.common.log.DynaLogger;
import dyna.common.systemenum.LanguageEnum;
import dyna.net.service.brs.*;
import dyna.net.service.das.JSS;
import dyna.net.service.das.MSRM;
import dyna.net.service.data.RelationService;
import dyna.net.service.data.SystemDataService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Lizw
 * @date 2022/1/27 Async
 **/
@Service
public class AsyncImpl extends BusinessRuleService implements Async
{
	@DubboReference private SystemDataService systemDataService;

	@Autowired private SystemAsyncStub        systemStub;
	@Autowired private ConfigurableServerImpl configurableServer;

	protected SystemDataService getSystemDataService()
	{
		return this.systemDataService;
	}

	protected SystemAsyncStub getSystemStub()
	{
		return this.systemStub;
	}

	@org.springframework.scheduling.annotation.Async(AsyncConfig.MULTI_THREAD_QUEUED_TASK)
	@Override public void systemTrack(TrackerBuilder builder, Signature signature, Method method, Object[] parameters, Object result)
	{
		this.getSystemStub().systemTrack(builder, signature, method, parameters, result, configurableServer.getLanguage());
	}

}
