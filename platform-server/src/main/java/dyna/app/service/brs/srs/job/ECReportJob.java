/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: BOMReportJob
 * cuilei 2012-6-25
 */
package dyna.app.service.brs.srs.job;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.aas.AASImpl;
import dyna.app.service.brs.srs.SRSImpl;
import dyna.app.service.das.jss.JSSImpl;
import dyna.app.service.das.jss.JobExecutor;
import dyna.app.service.das.jss.JobResult;
import dyna.common.dto.Queue;
import dyna.common.dto.aas.User;
import dyna.common.log.DynaLogger;
import dyna.common.systemenum.LanguageEnum;
import dyna.common.systemenum.MailCategoryEnum;
import dyna.common.systemenum.ReportTypeEnum;
import dyna.common.util.StringUtils;
import dyna.net.service.brs.AAS;
import dyna.net.service.brs.SMS;
import dyna.net.service.brs.SRS;
import dyna.net.service.das.MSRM;
import dyna.net.service.data.SystemDataService;
import dyna.net.spi.ServiceProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationBeanFactoryMetadata;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 脚本中导出EC报表使用
 * 
 * @author cuilei
 * 
 */
@Component
public class ECReportJob extends AbstractServiceStub<SRSImpl> implements JobExecutor
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


		Map<String, List<String>> guidListMap = this.getAllGuidList(job);
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

		boolean isSucceed = true;
		boolean isScriptB = false;
		boolean isMailB = false;
		try
		{
			if (!StringUtils.isNullString(isScript) && "Y".equals(isScript))
			{
				isScriptB = true;
			}
			if (!StringUtils.isNullString(isMail) && "Y".equals(isMail))
			{
				isMailB = true;
			}
			this.stubService.reportGenericECHelp(exportFileType, guidListMap, null, isScriptB, job.getGuid(), isMailB);
		}
		catch (Throwable e)
		{
			DynaLogger.error(e.getMessage(),e);
			isSucceed = false;
		}
		finally
		{
			AAS aas = this.stubService.getAAS();
			aas.logout();
		}

		return isSucceed ? null : JobResult.failed("ec report failed!", false);
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
		String session = ((AASImpl) this.stubService.getAAS()).getLoginStub().login(fieldb[0], fieldb[1], fieldb[2], LanguageEnum.getById(fieldb[3]));

		SMS sms = this.stubService.getSMS();
		MSRM msrm = this.stubService.getMSRM();

		User jobCreator = this.getUserByGuid(job.getCreateUserGuid());

		String message = msrm.getMSRString("ID_APP_JSS_JOB_FAIL_SERVER_RESTART", lang.toString());
		String errMsg = " ec report " + msrm.getMSRString("ID_APP_EXPORT_REPORT_ERROR", lang.toString());

		try
		{
			sms.sendMail4Report(message, errMsg, MailCategoryEnum.ERROR, jobCreator.getUserId(), null);
		}
		finally
		{
			AAS aas = this.stubService.getAAS();
			aas.logout();
		}

		return JobResult.failed(message);
	}

	private User getUserByGuid(String userGuid)
	{
		SystemDataService sds = this.stubService.getSystemDataService();

		Map<String, Object> paramClass = new HashMap<String, Object>();
		paramClass.put("GUID", userGuid);

		return sds.queryObject(User.class, paramClass);
	}

	private Map<String, List<String>> getAllGuidList(Queue job)
	{
		Map<String, List<String>> guidMap = new HashMap<String, List<String>>();

		if (StringUtils.isNullString(job.getFieldc()) == false)
		{
			this.rebuildGuidMap(guidMap, job.getFieldc());
		}
		if (StringUtils.isNullString(job.getFieldd()) == false)
		{
			this.rebuildGuidMap(guidMap, job.getFieldd());
		}
		if (StringUtils.isNullString(job.getFielde()) == false)
		{
			this.rebuildGuidMap(guidMap, job.getFielde());
		}
		if (StringUtils.isNullString(job.getFieldf()) == false)
		{
			this.rebuildGuidMap(guidMap, job.getFieldf());
		}
		if (StringUtils.isNullString(job.getFieldg()) == false)
		{
			this.rebuildGuidMap(guidMap, job.getFieldg());
		}
		if (StringUtils.isNullString(job.getFieldh()) == false)
		{
			this.rebuildGuidMap(guidMap, job.getFieldh());
		}
		if (StringUtils.isNullString(job.getFieldi()) == false)
		{
			this.rebuildGuidMap(guidMap, job.getFieldi());
		}
		if (StringUtils.isNullString(job.getFieldj()) == false)
		{
			this.rebuildGuidMap(guidMap, job.getFieldj());
		}
		if (StringUtils.isNullString(job.getFieldk()) == false)
		{
			this.rebuildGuidMap(guidMap, job.getFieldk());
		}
		if (StringUtils.isNullString(job.getFieldl()) == false)
		{
			this.rebuildGuidMap(guidMap, job.getFieldl());
		}
		if (StringUtils.isNullString(job.getFieldm()) == false)
		{
			this.rebuildGuidMap(guidMap, job.getFieldm());
		}
		if (StringUtils.isNullString(job.getFieldn()) == false)
		{
			this.rebuildGuidMap(guidMap, job.getFieldn());
		}
		if (StringUtils.isNullString(job.getFieldo()) == false)
		{
			this.rebuildGuidMap(guidMap, job.getFieldo());
		}

		return guidMap;
	}

	private void rebuildGuidMap(Map<String, List<String>> guidMap, String value)
	{
		if (value.indexOf("$") == -1)
		{
			return;
		}

		String className = value.split("\\$")[0];
		if (guidMap.get(className) == null)
		{
			guidMap.put(className, new ArrayList<String>());
		}
		guidMap.get(className).addAll(this.stubService.rebuildStrToList(value.split("\\$")[1]));
	}
}
