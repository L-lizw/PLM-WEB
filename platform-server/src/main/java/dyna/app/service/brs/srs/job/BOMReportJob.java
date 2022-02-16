/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: BOMReportJob
 * cuilei 2012-6-25
 */
package dyna.app.service.brs.srs.job;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.aas.AASImpl;
import dyna.app.service.brs.srs.SRSImpl;
import dyna.app.service.das.jss.JobExecutor;
import dyna.app.service.das.jss.JobResult;
import dyna.common.SearchCondition;
import dyna.common.SearchConditionFactory;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.bean.data.foundation.BOMView;
import dyna.common.dto.DataRule;
import dyna.common.dto.Queue;
import dyna.common.systemenum.LanguageEnum;
import dyna.common.systemenum.MailCategoryEnum;
import dyna.common.systemenum.ReportTypeEnum;
import dyna.common.systemenum.SystemStatusEnum;
import dyna.common.util.BooleanUtils;
import dyna.common.util.DateFormat;
import dyna.common.util.EnvUtils;
import dyna.net.service.brs.AAS;
import dyna.net.service.brs.BOMS;
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
public class BOMReportJob extends AbstractServiceStub<SRSImpl> implements JobExecutor
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see dyna.app.service.das.jss.JobExecutor#perform(dyna.app.service.das.jss.JSSImpl,
	 * dyna.common.bean.data.system.Queue)
	 */
	@Override
	public JobResult perform(Queue job) throws Exception
	{
		// TODO Auto-generated method stub
		String[] login = job.getFielde().split(SRS.JOIN_CHAR);
		String session = ((AASImpl) this.stubService.getAAS()).getLoginStub().login(login[0], login[1], login[2], LanguageEnum.getById(login[3]));

		BOMS boms = this.stubService.getBOMS();
		AAS aas = this.stubService.getAAS();

		String[] fielda = job.getFielda().split(SRS.JOIN_CHAR);
		ObjectGuid end1ObjectGuid = this.stubService.getObjectGuidByStr(fielda[0]);
		String viewName = fielda[1];
		
		BOMView bomview = boms.getBOMViewByEND1(end1ObjectGuid, viewName);
		ObjectGuid bomViewObjectGuid = bomview.getObjectGuid();

		DataRule dataRule = new DataRule();
		dataRule.setLocateTime(job.getFieldb() == null || job.getFieldb().equals("") ? null : DateFormat.parse(job.getFieldb(), DateFormat.PTN_TIMESTAMP));
		if (job.getFieldc() != null)
		{
			dataRule.setSystemStatus(SystemStatusEnum.valueOf(job.getFieldc()));
		}

		String[] fieldd = job.getFieldd().split(SRS.JOIN_CHAR);
		int level = Integer.parseInt(fieldd[0]);
		ReportTypeEnum exportFileType = ReportTypeEnum.valueOf(fieldd[1]);
		String bomReportName = fieldd[2];
		String exportType = fieldd[3].equals("null") ? null : fieldd[3];
		String levelStyle = fieldd[4].equals("null") ? null : fieldd[4];
		String groupStyle = fieldd[5].equals("null") ? null : fieldd[5];
		boolean isContainRepf = fieldd[6].equals("null") ? null : BooleanUtils.getBooleanByYN(fieldd[6]);

		SearchCondition searchCondition = null;
		if (job.getFieldo() != null)
		{
			ObjectGuid objectGuid = this.stubService.getObjectGuidByStr(job.getFieldo());
			searchCondition = SearchConditionFactory.createSearchCondition(objectGuid, null, false);
		}
		else
		{
			searchCondition = SearchConditionFactory.createSearchCondition4AllSubscription();
		}

		if (null != job.getFieldk())
		{
			searchCondition.setBOGuidList(this.stubService.rebuildStrToList(job.getFieldk()));
		}

		if (null != job.getFieldl())
		{
			searchCondition.setClassificationList(this.stubService.rebuildStrToList(job.getFieldl()));
		}

		if (null != job.getFieldm())
		{
			List<String> resultFieldList = this.stubService.rebuildStrToList(job.getFieldm());
			for (String sfName : resultFieldList)
			{
				searchCondition.addResultField(sfName);
			}
		}

		if (null != job.getFieldn())
		{
			searchCondition.setResultUINameList(this.stubService.rebuildStrToList(job.getFieldn()));
		}

		if (null != job.getFieldg())
		{
			String[] orderMaps = job.getFieldg().split(SRS.JOIN_CHAR);
			for (String orderMap : orderMaps)
			{
				String[] maps = orderMap.substring(1, orderMap.length() - 1).split("=");
				searchCondition.addOrder(maps[0].trim(), maps[1].trim().equals("true") ? true : false);
			}
		}

		String bomScriptFileName = null;
		if (job.getFieldj() != null)
		{
			String[] fieldj = job.getFieldj().split(SRS.JOIN_CHAR);
			searchCondition.setSearchValue("null".equals(fieldj[0]) ? null : fieldj[0]);

			if (fieldj.length >= 3 && !"null".equals(fieldj[2]))
			{
				bomScriptFileName = fieldj[2];
			}
		}

		List<String> classGuidList = new ArrayList<String>();
		if (job.getFieldf() != null)
		{
			classGuidList.addAll(this.stubService.rebuildStrToList(job.getFieldf()));
		}

		List<String> summaryFiledNameList = new ArrayList<String>();
		if (job.getFieldh() != null)
		{
			summaryFiledNameList.addAll(this.stubService.rebuildStrToList(job.getFieldh()));
		}

		String[] temp = job.getFieldi().split(SRS.JOIN_CHAR);
		String bomReportTemplateName = temp[0];
		String isExportAllLevel = temp[1];
		String pagesize = temp[2];
		String reportpath = temp[3];

		boolean flag = false;
		Map<String, Object> data = null;
		try
		{
			if (null != bomScriptFileName)
			{
				File file = new File(EnvUtils.getConfRootPath() + "dyna/customization/report/BOMView/" + bomScriptFileName + ".class");
				if (file.exists())
				{
					data = this.stubService.personalizedReportData(bomViewObjectGuid, level, searchCondition, bomScriptFileName, exportFileType, bomReportName, exportType, levelStyle,
							groupStyle);

					if (null == data)
					{
						return null;
					}
					else
					{
						flag = true;
					}
				}
				else
				{
					// 导出标准报表
					this.stubService.reportGenericBOM(bomViewObjectGuid, dataRule, level, searchCondition, exportFileType, bomReportName, exportType, levelStyle, groupStyle,
							bomReportTemplateName, isExportAllLevel, summaryFiledNameList, pagesize, reportpath, classGuidList, job.getGuid(), isContainRepf);
				}

			}
			else
			{
				// 导出标准报表
				this.stubService.reportGenericBOM(bomViewObjectGuid, dataRule, level, searchCondition, exportFileType, bomReportName, exportType, levelStyle, groupStyle,
						bomReportTemplateName, isExportAllLevel, summaryFiledNameList, pagesize, reportpath, classGuidList, job.getGuid(), isContainRepf);
			}
			if (flag)
			{
				// 导出定制化报表
				this.stubService.personalizedReport(data, bomReportName, bomReportTemplateName, bomViewObjectGuid, level, searchCondition, exportFileType, exportType, levelStyle,
						groupStyle, job.getGuid());
			}
		}
		finally
		{
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
		String[] login = job.getFieldj().split("-");
		LanguageEnum lang = LanguageEnum.getById(login[3]);
		String session = ((AASImpl) stubService.getAAS()).getLoginStub().login(login[0], login[1], login[2], lang);

		AAS aas = this.stubService.getAAS();
		SMS sms = this.stubService.getSMS();
		MSRM msrm = this.stubService.getMSRM();
		BOMS boms = this.stubService.getBOMS();

		ObjectGuid bomViewObjectGuid = this.stubService.getObjectGuidByStr(job.getFielda());
		BOMView bomView = boms.getBOMView(bomViewObjectGuid);

		String message = msrm.getMSRString("ID_APP_JSS_JOB_FAIL_SERVER_RESTART", lang.toString());
		String errMsg = bomView.getFullName() + " | " + msrm.getMSRString("ID_APP_EXPORT_REPORT_ERROR", lang.toString());

		try
		{
			sms.sendMail4Report(errMsg, errMsg, MailCategoryEnum.ERROR, login[0], null);
		}
		finally
		{
			aas.logout();
		}

		return JobResult.failed(message);
	}
}
