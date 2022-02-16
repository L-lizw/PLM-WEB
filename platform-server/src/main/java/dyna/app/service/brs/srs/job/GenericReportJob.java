/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: BOMReportJob
 * cuilei 2012-6-25
 */
package dyna.app.service.brs.srs.job;

import java.util.ArrayList;
import java.util.List;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.aas.AASImpl;
import dyna.app.service.brs.srs.SRSImpl;
import dyna.app.service.das.jss.JSSImpl;
import dyna.app.service.das.jss.JobExecutor;
import dyna.app.service.das.jss.JobResult;
import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.dto.Queue;
import dyna.common.systemenum.LanguageEnum;
import dyna.common.systemenum.MailCategoryEnum;
import dyna.common.systemenum.ReportTypeEnum;
import dyna.common.util.SetUtils;
import dyna.common.util.StringUtils;
import dyna.net.service.brs.AAS;
import dyna.net.service.brs.BOAS;
import dyna.net.service.brs.SMS;
import dyna.net.service.brs.SRS;
import dyna.net.service.das.MSRM;
import dyna.net.spi.ServiceProvider;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * 
 */
@Component
public class GenericReportJob extends AbstractServiceStub<SRSImpl> implements JobExecutor
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dyna.app.service.das.jss.JobExecutor#perform(dyna.app.service.das.jss
	 * .JSSImpl, dyna.common.bean.data.system.Queue)
	 */
	@Override
	public JobResult perform(Queue job) throws Exception
	{
		String[] fieldb = job.getFieldb().split(SRS.JOIN_CHAR);
		String session = ((AASImpl) this.stubService.getAAS()).getLoginStub().login(fieldb[0], fieldb[1], fieldb[2], LanguageEnum.getById(fieldb[3]));


		String tmpObjStr = job.getFielda();
		if (tmpObjStr.indexOf(";") != -1)
		{
			tmpObjStr = job.getFielda().split(";")[0];
		}
		ObjectGuid objectGuid = this.stubService.getObjectGuidByStr(tmpObjStr);

		String uiName = fieldb[4];

		List<String> guidList = this.getAllGuidList(job);

		ReportTypeEnum exportFileType = ReportTypeEnum.valueOf(fieldb[5]);

		String isScript = null;
		String isMail = null;

		if (fieldb.length > 6)
		{
			isScript = fieldb[6];
		}

		if (fieldb.length > 7)
		{
			isMail = fieldb[7];
		}

		try
		{
			if (!StringUtils.isNullString(isScript) && "Y".equals(isScript))
			{
				if (!StringUtils.isNullString(isMail) && "Y".equals(isMail))
				{
					this.stubService.reportGenericHelp(objectGuid.getClassName(), uiName, exportFileType, guidList, null, true, job.getGuid(), true);
				}
				else
				{
					this.stubService.reportGenericHelp(objectGuid.getClassName(), uiName, exportFileType, guidList, null, true, job.getGuid(), false);
				}
			}
			else
			{
				this.stubService.reportGenericHelp(objectGuid.getClassName(), uiName, exportFileType, guidList, null, false, job.getGuid(), false);
			}
		}
		finally
		{
			AAS aas = this.stubService.getAAS();
			aas.logout();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dyna.app.service.das.jss.JobExecutor#serverPerformFail(dyna.app.service.das.jss.JSSImpl,
	 * dyna.common.bean.data.system.Queue)
	 */
	@Override
	public JobResult serverPerformFail(Queue job) throws Exception
	{
		String[] fieldb = job.getFieldb().split(SRS.JOIN_CHAR);
		LanguageEnum lang = LanguageEnum.getById(fieldb[3]);
		String session = ((AASImpl) stubService.getAAS()).getLoginStub().login(fieldb[0], fieldb[1], fieldb[2], LanguageEnum.getById(fieldb[3]));

		SMS sms = this.stubService.getSMS();
		MSRM msrm = this.stubService.getMSRM();
		BOAS boas = this.stubService.getBOAS();

		String tmpObjStr = job.getFielda();
		if (tmpObjStr.indexOf(";") != -1)
		{
			tmpObjStr = job.getFielda().split(";")[0];
		}
		ObjectGuid objectGuid = this.stubService.getObjectGuidByStr(tmpObjStr);

		StringBuffer buffer = new StringBuffer();
		List<String> guidList = this.getAllGuidList(job);
		if (!SetUtils.isNullList(guidList))
		{
			for (String guid : guidList)
			{
				objectGuid.setGuid(guid);
				FoundationObject obj = boas.getObjectByGuid(objectGuid);
				FoundationObject object = boas.getObject(obj.getObjectGuid());
				if (null != object.getFullName())
				{
					buffer.append(this.reportTitle(object.getFullName(), lang, object) + " ");
				}
			}
		}

		String message = msrm.getMSRString("ID_APP_JSS_JOB_FAIL_SERVER_RESTART", lang.toString());

		try
		{
			sms.sendMail4Report(message, buffer.toString(), MailCategoryEnum.ERROR, fieldb[0], null);
		}
		finally
		{
			AAS aas = this.stubService.getAAS();
			aas.logout();
		}

		return JobResult.failed(message);
	}

	private String reportTitle(String name, LanguageEnum lang, FoundationObject object)
	{
		if (name.contains("\\") || name.contains("//") || name.contains(":") || name.contains("*") || name.contains("?") || name.contains("<") || name.contains(">")
				|| name.contains("|") || name.contains("\""))
		{
			name = StringUtils.getMsrTitle((String) object.get("BOTITLE$"), lang.getType());
		}

		return name;
	}

	private List<String> getAllGuidList(Queue job)
	{
		List<String> guidList = new ArrayList<String>();
		if (StringUtils.isNullString(job.getFieldc()) == false)
		{
			guidList.addAll(this.stubService.rebuildStrToList(job.getFieldc()));
		}
		if (StringUtils.isNullString(job.getFieldd()) == false)
		{
			guidList.addAll(this.stubService.rebuildStrToList(job.getFieldd()));
		}
		if (StringUtils.isNullString(job.getFielde()) == false)
		{
			guidList.addAll(this.stubService.rebuildStrToList(job.getFielde()));
		}
		if (StringUtils.isNullString(job.getFieldf()) == false)
		{
			guidList.addAll(this.stubService.rebuildStrToList(job.getFieldf()));
		}
		if (StringUtils.isNullString(job.getFieldg()) == false)
		{
			guidList.addAll(this.stubService.rebuildStrToList(job.getFieldg()));
		}
		if (StringUtils.isNullString(job.getFieldh()) == false)
		{
			guidList.addAll(this.stubService.rebuildStrToList(job.getFieldh()));
		}
		if (StringUtils.isNullString(job.getFieldi()) == false)
		{
			guidList.addAll(this.stubService.rebuildStrToList(job.getFieldi()));
		}
		if (StringUtils.isNullString(job.getFieldj()) == false)
		{
			guidList.addAll(this.stubService.rebuildStrToList(job.getFieldj()));
		}
		if (StringUtils.isNullString(job.getFieldk()) == false)
		{
			guidList.addAll(this.stubService.rebuildStrToList(job.getFieldk()));
		}
		if (StringUtils.isNullString(job.getFieldl()) == false)
		{
			guidList.addAll(this.stubService.rebuildStrToList(job.getFieldl()));
		}
		if (StringUtils.isNullString(job.getFieldm()) == false)
		{
			guidList.addAll(this.stubService.rebuildStrToList(job.getFieldm()));
		}
		if (StringUtils.isNullString(job.getFieldn()) == false)
		{
			guidList.addAll(this.stubService.rebuildStrToList(job.getFieldn()));
		}
		if (StringUtils.isNullString(job.getFieldo()) == false)
		{
			guidList.addAll(this.stubService.rebuildStrToList(job.getFieldo()));
		}

		return guidList;
	}
}
