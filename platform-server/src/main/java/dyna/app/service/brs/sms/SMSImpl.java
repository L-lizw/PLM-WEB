/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: 系统邮件相关服务的实现
 * caogc 2010-8-20
 */
package dyna.app.service.brs.sms;

import dyna.app.conf.AsyncConfig;
import dyna.app.service.BusinessRuleService;
import dyna.app.service.brs.async.SMSAsyncStub;
import dyna.common.SearchCondition;
import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.dto.*;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import dyna.common.systemenum.LanguageEnum;
import dyna.common.systemenum.MailCategoryEnum;
import dyna.common.systemenum.MailMessageType;
import dyna.net.service.brs.*;
import dyna.net.service.das.MSRM;
import dyna.net.service.data.DSCommonService;
import dyna.net.service.data.RelationService;
import dyna.net.service.data.SystemDataService;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * System Mail Service implementation
 *
 * @author Lizw
 */
@Getter(AccessLevel.PROTECTED)
@Service public class SMSImpl extends BusinessRuleService implements SMS
{
	@DubboReference private DSCommonService   dsCommonService;
	@DubboReference private RelationService   relationService;
	@DubboReference private SystemDataService systemDataService;

	@Autowired
	private AAS aas;
	@Autowired private Async async;
	@Autowired
	private BOAS boas;
	@Autowired
	private DSS dss;
	@Autowired
	private EMM emm;
	@Autowired
	private MSRM msrm;
	@Autowired
	private POS pos;
	@Autowired
	private WFI wfi;

	@Autowired private MailStub         mailStub         ;
	@Autowired private MailInboxStub    mailInboxStub    ;
	@Autowired private MailWorkFlowStub mailworkflowstub ;
	@Autowired private MailSentStub     mailSentStub     ;
	@Autowired private MailTrashStub    mailTrashStub    ;
	@Autowired private MailUpdaterStub  mailUpdaterStub  ;
	@Autowired private EmailStub        emailStub        ;


	protected DSCommonService getDsCommonService()
	{
		return this.dsCommonService;
	}

	protected RelationService getRelationService()
	{
		return this.relationService;
	}

	protected SystemDataService getSystemDataService()
	{
		return this.systemDataService;
	}

	protected Async getAsync()
	{
		return this.async;
	}

	@Override public void clearTrash() throws ServiceRequestException
	{
		this.getMailTrashStub().clearTrash();
	}

	@Override public void deleteInbox(List<String> mailGuidList) throws ServiceRequestException
	{
		this.getMailUpdaterStub().moveToTrash(mailGuidList);
	}

	@Override public void deleteSent(List<String> mailGuidList) throws ServiceRequestException
	{
		this.getMailUpdaterStub().moveToTrash(mailGuidList);
	}

	@Override public void deleteTrash(List<String> mailGuidList) throws ServiceRequestException
	{
		this.getMailUpdaterStub().deleteMail(mailGuidList);
	}

	@Override public Mail getMail(String mailGuid) throws ServiceRequestException
	{
		return this.getMailStub().getMail(mailGuid);
	}

	/**
	 * @return the mailInboxStub
	 */
	protected MailInboxStub getMailInboxStub()
	{
		return this.mailInboxStub;
	}

	/**
	 * @return the mailInboxStub
	 */
	protected MailWorkFlowStub getMailWorkFlowStub()
	{
		return this.mailworkflowstub;
	}

	/**
	 * @return the emailStub
	 */
	protected EmailStub getEmailStub()
	{
		return this.emailStub;
	}

	/**
	 * @return the mailSentStub
	 */
	public MailSentStub getMailSentStub()
	{
		return this.mailSentStub;
	}

	/**
	 * @return the mailStub
	 */
	protected MailStub getMailStub()
	{
		return this.mailStub;
	}

	/**
	 * @return the mailTrashStub
	 */
	protected MailTrashStub getMailTrashStub()
	{
		return this.mailTrashStub;
	}

	/**
	 * @return the mailUpdaterStub
	 */
	protected MailUpdaterStub getMailUpdaterStub()
	{
		return this.mailUpdaterStub;
	}

	@Override public List<Mail> listInbox(SearchCondition searchCondition) throws ServiceRequestException
	{
		return this.getMailInboxStub().listInbox(searchCondition, null);
	}

	@Override public List<Mail> listSent(SearchCondition searchCondition) throws ServiceRequestException
	{
		return this.getMailSentStub().listSent(searchCondition);
	}

	@Override public List<Mail> listTrash(SearchCondition searchCondition) throws ServiceRequestException
	{
		return this.getMailTrashStub().listTrash(searchCondition);
	}

	@Override public void restoreFromTrash(List<String> mailGuidList) throws ServiceRequestException
	{
		this.getMailTrashStub().restoreFromTrash(mailGuidList);
	}

	// @Override
	// public void sendMailToGroup(String subject, String content, MailCategoryEnum mailCategoryEnum,
	// List<ObjectGuid> objectGuidList, List<String> toGroupIdList) throws ServiceRequestException
	// {
	// this.getMailSentStub().sendMailToGroup(subject, content, mailCategoryEnum, objectGuidList, toGroupIdList);
	//
	// }

	@Override public void sendMailToUsers(String subject, String content, MailCategoryEnum mailCategoryEnum, List<ObjectGuid> objectGuidList, List<String> toUserIdList,
			MailMessageType messageType) throws ServiceRequestException
	{
		this.getMailSentStub().sendMailToUsers(subject, content, mailCategoryEnum, objectGuidList, toUserIdList, messageType);
	}

	@Override public void sendMailToUser(String subject, String content, MailCategoryEnum mailCategoryEnum, List<ObjectGuid> objectGuidList, String toUserId,
			MailMessageType messageType) throws ServiceRequestException
	{
		this.getMailSentStub().sendMailToUser(subject, content, mailCategoryEnum, objectGuidList, toUserId, messageType);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.SMS#sendMailToUser(java.lang.String, java.lang.String, java.lang.String,
	 * java.util.List)
	 */
	@Override public List<DSSFileTrans> sendMail4Report(String subject, String content, MailCategoryEnum mailCategoryEnum, String toUserId, List<DSSFileInfo> fileList)
			throws ServiceRequestException
	{
		return this.getMailSentStub().sendMail4Report(subject, content, mailCategoryEnum, toUserId, fileList);
	}

	@Override public void setAsProcessed(List<String> mailGuidList) throws ServiceRequestException
	{
		this.getMailUpdaterStub().setAsProcessed(mailGuidList);
	}

	@Override public void setAsRead(List<String> mailGuidList) throws ServiceRequestException
	{
		this.getMailUpdaterStub().setAsRead(mailGuidList);
	}

	// @Override
	// public void setInboxCategory(List<String> mailGuidList, MailCategoryEnum mailCategoryEnum)
	// throws ServiceRequestException
	// {
	// this.getMailUpdaterStub().setInboxCategory(mailGuidList, mailCategoryEnum);
	// }

	@Override public void setAsNotRead(List<String> mailGuidList) throws ServiceRequestException
	{
		this.getMailUpdaterStub().setAsNotRead(mailGuidList);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.SMS#getMailAttachment(java.lang.String, dyna.common.bean.data.ObjectGuid)
	 */
	@Override public FoundationObject getMailAttachment(String mailGuid, ObjectGuid objectGuid) throws ServiceRequestException
	{
		return this.getMailStub().getMailAttachment(mailGuid, objectGuid);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.SMS#saveEmailServer(dyna.common.bean.data.system.EmailServer)
	 */
	@Override public EmailServer saveEmailServer(EmailServer emailServer) throws ServiceRequestException
	{
		return this.getEmailStub().saveEmailServer(emailServer);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.SMS#getEmailServer()
	 */
	@Override public EmailServer getEmailServer() throws ServiceRequestException
	{
		return this.getEmailStub().getEmailServer();
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see dyna.net.service.brs.SMS#sendMailToRIG(java.lang.String, java.lang.String,
	// * dyna.common.systemenum.MailCategoryEnum, java.util.List, java.util.List)
	// */
	// @Override
	// public void sendMailToRIG(String subject, String content, MailCategoryEnum mailCategoryEnum, List<ObjectGuid>
	// objectGuidList, List<String> toRIGList)
	// throws ServiceRequestException
	// {
	// this.getMailSentStub().sendMailToRIG(subject, content, mailCategoryEnum, objectGuidList, toRIGList);
	//
	// }

	// /*
	// * (non-Javadoc)
	// *
	// * @see dyna.net.service.brs.SMS#sendMailToNotifier(java.lang.String, java.lang.String,
	// * dyna.common.systemenum.MailCategoryEnum, java.util.List, java.util.List)
	// */
	// @Override
	// public void sendMailToNotifier(String subject, String content, MailCategoryEnum mailCategoryEnum,
	// List<ObjectGuid> objectGuidList, List<Notifier> notifierList) throws ServiceRequestException
	// {
	// this.getMailSentStub().sendMailToNotifier(subject, content, mailCategoryEnum, objectGuidList, notifierList);
	// }

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.SMS#getNumberOfUnreadNotice()
	 */
	@Override public int getCountOfUnreadNotice() throws ServiceRequestException
	{
		return this.getMailInboxStub().getCountOfUnreadNotice();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.SMS#listMailOfWorkFlow(dyna.common.SearchCondition)
	 */
	@Override public List<MailWorkFlow> listMailOfWorkFlow(SearchCondition searchCondition) throws ServiceRequestException
	{
		return this.getMailWorkFlowStub().listMailOfWorkFlow(searchCondition);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.SMS#clearMailByConfig()
	 */
	@Override public void clearMailByConfig() throws ServiceRequestException
	{
		this.getMailTrashStub().clearMailByConfig();
	}

	/**
	 * 工作流通知
	 *
	 * @param toUserGuidList
	 * @param fromUseGuid
	 * @param processGuid
	 * @param activityGuid
	 * @param contents
	 * @param title
	 * @param category
	 * @throws ServiceRequestException
	 */
	public void sendMail4WorkFlow(List<String> toUserGuidList, String fromUseGuid, String processGuid, String activityGuid, String contents, String title,
			MailMessageType messageType, MailCategoryEnum category, Integer startNumber) throws ServiceRequestException
	{
		this.getMailSentStub().sendMail4WorkFlow(toUserGuidList, fromUseGuid, processGuid, activityGuid, contents, title , messageType, category, startNumber);
	}

}
