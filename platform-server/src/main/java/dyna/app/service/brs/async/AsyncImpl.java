package dyna.app.service.brs.async;

import dyna.app.conf.AsyncConfig;
import dyna.app.service.BusinessRuleService;
import dyna.common.SearchCondition;
import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.bean.data.StructureObject;
import dyna.common.bean.signature.Signature;
import dyna.common.bean.sync.AnalysisTask;
import dyna.common.bean.sync.ListBOMTask;
import dyna.common.bean.track.TrackerBuilder;
import dyna.app.conf.yml.ConfigurableServerImpl;
import dyna.common.dto.DataRule;
import dyna.common.dto.Folder;
import dyna.common.dto.Mail;
import dyna.common.dto.aas.Group;
import dyna.common.dto.template.wft.WorkflowTemplateScopeRTInfo;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import dyna.common.systemenum.LanguageEnum;
import dyna.net.security.signature.UserSignature;
import dyna.net.service.brs.*;
import dyna.net.service.das.JSS;
import dyna.net.service.das.MSRM;
import dyna.net.service.data.RelationService;
import dyna.net.service.data.SystemDataService;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Lizw
 * @date 2022/1/27 Async
 **/
@Getter(AccessLevel.PACKAGE)
@Service
public class AsyncImpl extends BusinessRuleService implements Async
{
	@DubboReference private SystemDataService systemDataService;
	@DubboReference private RelationService   relationService;

	@Lazy
	@Autowired
	private AAS     aas;
	@Lazy
	@Autowired
	private BOAS    boas;
	@Lazy
	@Autowired
	private BOMS    boms;
	@Lazy
	@Autowired
	private BRM     brm;
	@Lazy
	@Autowired
	private EDAP    edap;
	@Lazy
	@Autowired
	private EMM     emm;
	@Lazy
	@Autowired
	private JSS     jss;
	@Lazy
	@Autowired
	private MSRM    msrm;
	@Lazy
	@Autowired
	private POS     pos;
	@Lazy
	@Autowired
	private SMS     sms;

	@Lazy
	@Autowired private AASAsyncStub  aasAsyncStub;
	@Lazy
	@Autowired private BOASAsyncStub boasAsyncStub;
	@Lazy
	@Autowired private BOMAsyncStub bomAsyncStub;
	@Lazy
	@Autowired private EDAPAsyncStub edapStub;
	@Lazy
	@Autowired
	private JSSAsyncStub    jssAsyncStub;
	@Lazy
	@Autowired private POSAsyncStub          posStub;
	@Lazy
	@Autowired private PPMSAsyncStub       ppmsAsyncStub;
	@Lazy
	@Autowired private SMSAsyncStub     smsStub;
	@Lazy
	@Autowired private SystemAsyncStub        systemStub;

	@Autowired private ConfigurableServerImpl configurableServer;


	@org.springframework.scheduling.annotation.Async(AsyncConfig.MULTI_THREAD_QUEUED_TASK)
	@Override public void saveGroupTree(Group group, UserSignature userSignature)
	{
		DynaLogger.debug("QueuedTaskScheduler Scheduled [Class]SaveGroupTreeScheduledTask , Scheduled Task Start...");
		this.getAasAsyncStub().saveGroupTree(group, userSignature);
		DynaLogger.debug("QueuedTaskScheduler Scheduled [Class]SaveGroupTreeScheduledTask , Scheduled Task End...");
	}

	@org.springframework.scheduling.annotation.Async(AsyncConfig.MULTI_THREAD_QUEUED_TASK)
	@Override public void systemTrack(TrackerBuilder builder, Signature signature, Method method, Object[] parameters, Object result)
	{
		this.getSystemStub().systemTrack(builder, signature, method, parameters, result, configurableServer.getLanguage());
	}

	@org.springframework.scheduling.annotation.Async(AsyncConfig.MULTI_THREAD_QUEUED_TASK)
	@Override public void deleteReferenceAsync(ObjectGuid objectGuid, String exceptionParameter)
	{
		DynaLogger.debug("QueuedTaskScheduler Scheduled [Class]DeletetScheduledTask , Scheduled Task Start...");
		this.getBoasAsyncStub().deleteReference(objectGuid, exceptionParameter);
		DynaLogger.debug("QueuedTaskScheduler Scheduled [Class]DeletetScheduledTask , Scheduled Task End...");
	}


	@org.springframework.scheduling.annotation.Async(AsyncConfig.SELECT_CONNECT)
	@Override public CompletableFuture<List<StructureObject>> listObjectOfRelationAsync(ObjectGuid end1, String templateName, SearchCondition searchCondition,
			SearchCondition end2SearchCondition, DataRule dataRule)
	{
		return this.getBoasAsyncStub().listObjectOfRelation(end1, templateName, searchCondition, end2SearchCondition, dataRule);
	}

	@org.springframework.scheduling.annotation.Async(AsyncConfig.MULTI_THREAD_QUEUED_TASK)
	@Override public void updateHasEnd2Flg(ObjectGuid end1ObjectGuid, String relationTemplateGuid)
	{
		DynaLogger.debug("QueuedTaskScheduler Scheduled [Class]UpdateScheduledTask , Scheduled Task Start...");
		this.getBoasAsyncStub().updateHasEnd2Flg(end1ObjectGuid, relationTemplateGuid);
		DynaLogger.debug("QueuedTaskScheduler Scheduled [Class]UpdateScheduledTask , Scheduled Task End...");
	}

	@org.springframework.scheduling.annotation.Async(AsyncConfig.MULTI_THREAD_QUEUED_TASK)
	@Override public CompletableFuture<AnalysisTask> listBOMAndRelation(ObjectGuid objectGuid, boolean isCheckcl, boolean isbom, boolean isrelation,
			List<WorkflowTemplateScopeRTInfo> listScopeRT)
	{
		return this.getBoasAsyncStub().listBOMAndRelation(objectGuid,isCheckcl, isbom, isrelation, listScopeRT);
	}

	@org.springframework.scheduling.annotation.Async(AsyncConfig.SELECT_CONNECT)
	@Override public CompletableFuture<List<ObjectGuid>> checkConnect(ObjectGuid end1, String templateName, boolean isBom)
	{
		return this.getBomAsyncStub().CheckConnect(end1, templateName, isBom);
	}

	@org.springframework.scheduling.annotation.Async(AsyncConfig.MULTI_THREAD_QUEUED_TASK)
	@Override public void deleteReplaceData(ObjectGuid objectGuid, String exceptionParameter, List<FoundationObject> end1List, String bmGuid, boolean deleteReplace)
	{
		DynaLogger.debug("QueuedTaskScheduler Scheduled [Class]DeletetScheduledTask , Scheduled Task Start...");
		this.getBomAsyncStub().deleteReplaceData(objectGuid, exceptionParameter, end1List, bmGuid, deleteReplace);
		DynaLogger.info("QueuedTaskScheduler Scheduled [Class]DeletetScheduledTask , Scheduled Task End...");
	}


	@org.springframework.scheduling.annotation.Async(AsyncConfig.SELECT_CONNECT)
	@Override public CompletableFuture<ListBOMTask> listBOM(ObjectGuid end1, String templateName, SearchCondition searchCondition, SearchCondition end2SearchCondition,
			DataRule dataRule, int level)
	{
		return this.getBomAsyncStub().listBOM(end1, templateName, searchCondition, end2SearchCondition ,dataRule, level);
	}

	@org.springframework.scheduling.annotation.Async(AsyncConfig.SELECT_CONNECT)
	@Override public CompletableFuture<ListBOMTask> listBOMForAllTemplate(ObjectGuid end1, SearchCondition end2SearchCondition, DataRule dataRule)
	{
		return this.getBomAsyncStub().listBOMForAllTemplate(end1, end2SearchCondition, dataRule);
	}

	@org.springframework.scheduling.annotation.Async(AsyncConfig.MULTI_THREAD_QUEUED_TASK)
	@Override public void updateUHasBOM(List<FoundationObject> end1List, String bmGuid) throws ServiceRequestException
	{
		DynaLogger.debug("QueuedTaskScheduler Scheduled [Class]UpdateScheduledTask , Scheduled Task Start...");
		this.getBomAsyncStub().updateUHasBOM(end1List, bmGuid);
		DynaLogger.debug("QueuedTaskScheduler Scheduled [Class]UpdateScheduledTask , Scheduled Task End...");
	}

	@org.springframework.scheduling.annotation.Async(AsyncConfig.MULTI_THREAD_QUEUED_TASK) @Override public void saveFolderTree(Folder folder, boolean isDelete,
			UserSignature userSignature)
	{
		DynaLogger.debug("QueuedTaskScheduler Scheduled [Class]SaveFolderTreeScheduledTask , Scheduled Task Start...");
		this.getEdapStub().saveFolderTree(folder, isDelete, userSignature);
		DynaLogger.debug("QueuedTaskScheduler Scheduled [Class]SaveFolderTreeScheduledTask , Scheduled Task End...");
	}

	@org.springframework.scheduling.annotation.Async(AsyncConfig.MULTI_THREAD_QUEUED_TASK)
	@Override public void deleteHistory(String userGuid)
	{
		DynaLogger.debug("BIViewHisDeleteScheduledTask Scheduled [Class]DeletetScheduledTask , Scheduled Task Start...");
		this.getPosStub().deleteHistory(userGuid);
		DynaLogger.debug("BIViewHisDeleteScheduledTask Scheduled [Class]DeletetScheduledTask , Scheduled Task End...");
	}

	@org.springframework.scheduling.annotation.Async(AsyncConfig.MULTI_THREAD_QUEUED_TASK)
	@Override public void saveSearchByCondition(SearchCondition condition, String searchGuid) throws ServiceRequestException
	{
		this.getPosStub().saveSearchByCondition(condition,searchGuid);
	}

	@org.springframework.scheduling.annotation.Async(AsyncConfig.MULTI_THREAD_QUEUED_TASK)
	@Override public void deleteProject(ObjectGuid projectOObjectGuid)
	{
		DynaLogger.debug("QueuedTaskScheduler Scheduled [Class]DeletetScheduledTask , Scheduled Task Start...");
		this.getPpmsAsyncStub().deleteProject(projectOObjectGuid);
		DynaLogger.debug("QueuedTaskScheduler Scheduled [Class]DeletetScheduledTask , Scheduled Task End...");
	}


	@org.springframework.scheduling.annotation.Async(AsyncConfig.MAIL_SEND)
	@Override public void sendMail(Mail mail, List<String> toUserGuidList, LanguageEnum languageEnum)
	{
		DynaLogger.debug("SMS MultiThreadQueued Scheduled [Class]MailScheduledTask , Scheduled Task Start...");
		this.getSmsStub().sendMail(mail, toUserGuidList, languageEnum);
		DynaLogger.debug("SMS MultiThreadQueued Scheduled [Class]MailScheduledTask , Scheduled Task End...");
	}

	@org.springframework.scheduling.annotation.Async(AsyncConfig.MULTI_THREAD_QUEUED_TASK)
	@Override public void deleteJobByType(String jobID)
	{
		DynaLogger.info("JSS Scheduled [Class]ScheduledTaskDeleteJobByTypeImpl[" + jobID + "], Scheduled Task Start...");
		this.getJssAsyncStub().deleteJobByType(jobID);
		DynaLogger.info("JSS Scheduled [Class]ScheduledTaskDeleteJobByTypeImpl[" + jobID + "], Scheduled Task End...");
	}

}
