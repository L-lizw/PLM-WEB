/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: BOMReportStub
 * Wanglei 2011-12-21
 */
package dyna.app.service.brs.srs;

import dyna.app.report.*;
import dyna.app.service.AbstractServiceStub;
import dyna.common.SearchCondition;
import dyna.common.bean.data.DynaObject;
import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.bean.data.structure.BOMStructure;
import dyna.common.dto.DataRule;
import dyna.common.dto.Queue;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import dyna.common.systemenum.JobGroupEnum;
import dyna.common.systemenum.LanguageEnum;
import dyna.common.systemenum.ModelInterfaceEnum;
import dyna.common.systemenum.ReportTypeEnum;
import dyna.common.util.*;
import dyna.customization.report.ReportBuilder;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.util.JRFontNotFoundException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Wanglei
 * 
 */
@Component
public class BOMReportStub extends AbstractServiceStub<SRSImpl>
{
	public static final String	JOIN_CHAR	= "\r\n";

	protected Map<String, Object> reportBOM(ObjectGuid bomViewObjectGuid, int level, SearchCondition bomSearchCondition, String bomScriptFileName, ReportTypeEnum exportFileType,
			String bomReportName, String exportType, String levelStyle, String groupStyle) throws ServiceRequestException
	{
		ReportBuilder reportBuilder = this.stubService.getCommonReportStub()
				.getReportBuilder(bomViewObjectGuid.getClassGuid(), bomViewObjectGuid.getClassName(), bomScriptFileName);
		return this.stubService.getCommonReportStub().buildReport(reportBuilder, bomViewObjectGuid, level, bomSearchCondition, exportFileType, bomReportName, exportType,
				levelStyle, groupStyle);
	}

	protected File reportGenericBOM(ObjectGuid bomViewObjectGuid, DataRule dataRule, int level, SearchCondition bomSearchCondition, ReportTypeEnum exportFileType,
			LanguageEnum lang, String bomReportName, String exportType, String levelStyle, String groupStyle, String bomReportTemplateName, String isExportAllLevel,
			List<String> summaryFiledName, String pagesize, String reportpath, List<String> classGuids, boolean isContainRepf) throws ServiceRequestException
	{
		FoundationObject foundation = this.stubService.getBOAS().getObject(bomViewObjectGuid);
		ReportConfiguration configuration = new ReportConfiguration();
		configuration.setExportFileType(exportFileType);
		configuration.setExportToFilePath(GenericReportUtil.getFile(foundation.getId() + "_" + bomReportName, exportFileType));
		if (!StringUtils.isNullString(pagesize))
		{
			configuration.setPageCount(Integer.valueOf(pagesize));
		}

		String templateName = bomReportTemplateName.replace(".jrxml", "");
		File templateFile = null;
		if (!StringUtils.isNullString(exportType))
		{
			if (exportType.equals("bomtree"))
			{
				templateFile = new File(EnvUtils.getConfRootPath() + "conf/comment/report/bomReport/" + bomReportTemplateName);
			}
			else if (exportType.equals("bomlist"))
			{
				templateFile = new File(EnvUtils.getConfRootPath() + "conf/comment/report/bomReport/" + bomReportTemplateName);
			}
			else if (exportType.equals("group"))
			{
				templateFile = new File(EnvUtils.getConfRootPath() + "conf/comment/report/bomReport/" + bomReportTemplateName);
			}
		}
		else if (!StringUtils.isNullString(isExportAllLevel) || !SetUtils.isNullList(summaryFiledName))
		{
			templateFile = new File(EnvUtils.getConfRootPath() + "conf/comment/report/bomSummaryReport/" + bomReportTemplateName);
		}
		else
		{
			templateFile = new File(EnvUtils.getConfRootPath() + "conf/comment/report/bomReport/" + bomReportTemplateName);
		}

		ResolveReportTemplateField resolveReportTemplateField = new ResolveReportTemplateField();
		List<DetailColumnInfo> columnList = resolveReportTemplateField.getReportTemplateField(templateFile);
		List<ParameterColumnInfo> parameters = resolveReportTemplateField.getReportTemplateParameters(templateFile);

		Map<String, Object> otherParams = new HashMap<String, Object>();
		otherParams.put("level", level);
		otherParams.put("isExportAllLevel", isExportAllLevel);
		otherParams.put("summaryFiledName", summaryFiledName);
		otherParams.put("exportType", exportType);
		otherParams.put("levelStyle", levelStyle);
		otherParams.put("classGuids", classGuids);
		otherParams.put("isContainRepf", BooleanUtils.getBooleanStringYN(isContainRepf));

		GenericReportParams params = this.stubService.createGenericReportParamsWithService();
		params.setLang(lang);
		params.setUiObject(null);
		params.setHeaderColumnList(parameters);
		params.setDetailColumnList(columnList);
		params.setOtherParams(otherParams);

		configuration.setDetailColumnInfoList(columnList);
		ReportDataProvider<BOMStructure> provider = new ReportDataProviderGenericBOMImpl(bomViewObjectGuid, dataRule, bomSearchCondition, params);

		try
		{
			if (!SetUtils.isNullList(summaryFiledName))
			{
				configuration.setGroupFields(summaryFiledName);
			}
			genericBOM(exportType, groupStyle, provider, lang, configuration, templateName, summaryFiledName);
		}
		catch (ServiceRequestException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			DynaLogger.error(e);
			if (e instanceof JRFontNotFoundException)
			{
				String message = this.stubService.getMSRM().getMSRString("ID_APP_EXPORT_REPORT_ERROR_FONT", lang.toString());
				e = new Exception(message, e);

				throw new ServiceRequestException("ID_APP_EXPORT_REPORT_ERROR_FONT", "," + e.getMessage());
			}
			else if (e.getMessage().contains("Error evaluating expression"))
			{
				String message = e.getMessage();
				message = this.stubService.getMSRM().getMSRString("ID_APP_EXPORT_REPORT_ERROR", lang.toString()) + "("
						+ e.getMessage().substring(message.indexOf("{") + 1, message.length() - 1) + ")";
				e = new Exception(message, e);
				throw new ServiceRequestException("ID_APP_EXPORT_REPORT_ERROR", "," + e.getMessage());
			}
			else if (e.getCause() instanceof FileNotFoundException)
			{
				String message = this.stubService.getMSRM().getMSRString("ID_APP_NOTFOUND_REPORT_TEMPLATE", lang.toString());
				e = new Exception(message, e);
				throw new ServiceRequestException("ID_APP_NOTFOUND_REPORT_TEMPLATE", "," + e.getMessage());
			}
			else if (e.toString().contains("jasperreports"))
			{
				String message = this.stubService.getMSRM().getMSRString("ID_APP_EXPORT_REPORT_TEMPLATE_ERROR", lang.toString());
				e = new Exception(message, e);
				throw new ServiceRequestException("ID_APP_EXPORT_REPORT_TEMPLATE_ERROR", "," + e.getMessage());
			}
			else
			{
				String message = this.stubService.getMSRM().getMSRString("ID_APP_EXPORT_REPORT_ERROR", lang.toString());
				e = new Exception(message, e);
				throw new ServiceRequestException("ID_APP_EXPORT_REPORT_ERROR", "," + e.getMessage());
			}
		}

		return configuration.getExportToFilePath();
	}

	public void genericBOM(String exportType, String groupStyle, ReportDataProvider<BOMStructure> provider, LanguageEnum lang, ReportConfiguration configuration,
			String templateName, List<String> summaryFiledName) throws Exception
	{
		GenericDynaReportBuilder reportBuilder = new GenericDynaReportBuilderImpl();
		String newReportTemplateName = "";
		// if (templateName.equals("bom_report"))
		if (!StringUtils.isNullString(exportType))
		{
			if (exportType.equals("group"))
			{
				// 按照bo分组到报表数据
				if (groupStyle.equals("1"))
				{
					// newReportTemplateName = getReportTemplateNameBylang(lang,
					// templateName + "_bo_group_template");
					newReportTemplateName = getReportTemplateNameBylang(lang, templateName + "_template");
					List<String> list = new ArrayList<String>();
					list.add("END2.BOTITLE#");
					configuration.setGroupFields(list);
					reportBuilder.personalizedReport(new File(EnvUtils.getConfRootPath() + "conf/comment/report/" + newReportTemplateName), provider, configuration,
							provider.getHeaderParameter());
				}
				// 按照classification分组到报表数据
				else if (groupStyle.equals("2"))
				{
					// newReportTemplateName = getReportTemplateNameBylang(lang,
					// templateName
					// + "_classification_group_template");
					newReportTemplateName = getReportTemplateNameBylang(lang, templateName + "_template");
					List<String> list = new ArrayList<String>();
					list.add("END2.CLASSIFICATION#TITLE");
					configuration.setGroupFields(list);
					reportBuilder.personalizedReport(new File(EnvUtils.getConfRootPath() + "conf/comment/report/" + newReportTemplateName), provider, configuration,
							provider.getHeaderParameter());
				}
				// 按照bo、classification分组到报表数据
				else
				{
					// newReportTemplateName = getReportTemplateNameBylang(lang,
					// templateName
					// + "_bo_classification_group_template");
					newReportTemplateName = getReportTemplateNameBylang(lang, templateName + "_template");
					List<String> list = new ArrayList<String>();
					list.add("END2.BOTITLE#");
					list.add("END2.CLASSIFICATION#TITLE");
					configuration.setGroupFields(list);
					reportBuilder.personalizedReport(new File(EnvUtils.getConfRootPath() + "conf/comment/report/" + newReportTemplateName), provider, configuration,
							provider.getHeaderParameter());
				}

			}
			else
			{
				configuration.setGroupFields(summaryFiledName);
				newReportTemplateName = getReportTemplateNameBylang(lang, templateName + "_template");
				reportBuilder.personalizedReport(new File(EnvUtils.getConfRootPath() + "conf/comment/report/" + newReportTemplateName), provider, configuration,
						provider.getHeaderParameter());
			}
		}
		else
		{
			newReportTemplateName = getReportTemplateNameBylang(lang, templateName + "_template");
			configuration.setGroupFields(summaryFiledName);
			reportBuilder.personalizedReport(new File(EnvUtils.getConfRootPath() + "conf/comment/report/" + newReportTemplateName), provider, configuration,
					provider.getHeaderParameter());
		}

	}

	@SuppressWarnings("unused")
	private DetailColumnInfo createDetailColumnInfo(String title, String propertyName, Class<?> typeClass)
	{
		DetailColumnInfo ret = new DetailColumnInfo(title, typeClass, propertyName);
		ret.setValueDecorater(new ReportFieldValueDecorater() {

			@Override
			public Object getFieldValue(JRField field, DetailColumnInfo column, DynaObject object)
			{

				if (!field.getName().equals(column.getPropertyName()))
				{
					return (object.get(field.getName().replace("#", "$")) == null ? "" : object.get(field.getName().replace("#", "$")));
				}
				return (object.get(field.getName()) == null ? "" : object.get(field.getName()));
			}
		});
		return ret;
	}

	private String getReportTemplateNameBylang(LanguageEnum lang, String reportTemplateName)
	{

		String newReportTemplateName = "";
		if (lang.equals(LanguageEnum.ZH_CN))
		{
			newReportTemplateName = reportTemplateName.concat("_zh_cn.jrxml");
		}
		else if (lang.equals(LanguageEnum.ZH_TW))
		{
			newReportTemplateName = reportTemplateName.concat("_zh_tw.jrxml");
		}
		else if (lang.equals(LanguageEnum.EN))
		{
			newReportTemplateName = reportTemplateName.concat("_us_en.jrxml");
		}
		return newReportTemplateName;
	}

	protected void reportConfigBOMJob(ObjectGuid objectGuid, ObjectGuid drawObjectGuid, DataRule dataRule, ReportTypeEnum exportFileType, SearchCondition sc, int level)
			throws ServiceRequestException
	{
		LanguageEnum lang = this.stubService.getUserSignature().getLanguageEnum();
		Queue queueJob = new Queue();
		queueJob.setName("ConfigBOM");
		queueJob.setExecutorClass("dyna.app.service.brs.srs.job.ConfigBOMRepeortJob");
		queueJob.setIsSinglethRead("Y");

		StringBuffer resultFieldBuffer = new StringBuffer();
		StringBuffer uiNameBuffer = new StringBuffer();
		if (sc != null)
		{
			if (!SetUtils.isNullList(sc.getResultFieldList()))
			{
				for (String fieldName : sc.getResultFieldList())
				{
					if (resultFieldBuffer.length() > 0)
					{
						resultFieldBuffer.append(",");
					}
					resultFieldBuffer.append(fieldName);
				}
			}
			if (!SetUtils.isNullList(sc.listResultUINameList()))
			{
				for (String uiName : sc.listResultUINameList())
				{
					if (uiNameBuffer.length() > 0)
					{
						uiNameBuffer.append(",");
					}
					uiNameBuffer.append(uiName);
				}
			}
		}

		queueJob.setFielda(objectGuid.toString());

		if (level >= 1)
		{
			queueJob.setFieldb(String.valueOf(level));
		}

		queueJob.setFieldc(exportFileType.toString());
		queueJob.setFieldd(
				this.stubService.getUserSignature().getUserId() + JOIN_CHAR + this.stubService.getUserSignature().getLoginGroupId() + JOIN_CHAR + this.stubService.getUserSignature().getLoginRoleId() + JOIN_CHAR
						+ lang.getId());
		queueJob.setFielde(resultFieldBuffer.toString());
		queueJob.setFieldf(uiNameBuffer.toString());
		if (drawObjectGuid != null)
		{
			queueJob.setFieldg(drawObjectGuid.toString());
		}
		queueJob.setFieldh(dataRule.getSystemStatus().getId());
		if (dataRule.getLocateTime() != null)
		{
			queueJob.setFieldi(DateFormat.formatYMD(dataRule.getLocateTime()));
		}

		queueJob.setServerID(this.serverContext.getServerConfig().getId());
		queueJob.setJobGroup(JobGroupEnum.REPORT);
		this.stubService.getJSS().createJob(queueJob);
	}

	protected void createReportBOMJob(ObjectGuid end1ObjectGuid, String viewName, int level, SearchCondition bomSearchCondition, ReportTypeEnum exportFileType,
			String bomReportName, String exportType, String levelStyle, String groupStyle, String bomScriptFileName, String bomReportTemplateName, String isExportAllLevel,
			List<String> summaryFiledName, String pagesize, String reportpath, List<String> classGuids, boolean isContainRepf) throws ServiceRequestException
	{
		if (StringUtils.isNullString(pagesize))
		{
			pagesize = "50";
		}
		LanguageEnum lang = this.stubService.getUserSignature().getLanguageEnum();
		Queue queueJob = new Queue();

		queueJob.setName("BOM");
		queueJob.setExecutorClass("dyna.app.service.brs.srs.job.BOMReportJob");
		queueJob.setIsSinglethRead("Y");

		queueJob.setFielda(end1ObjectGuid.toString() + JOIN_CHAR + viewName);

		queueJob.setFieldd(Integer.toString(level) + JOIN_CHAR + exportFileType.toString() + JOIN_CHAR + bomReportName + JOIN_CHAR + exportType + JOIN_CHAR + levelStyle + JOIN_CHAR
				+ groupStyle + JOIN_CHAR + BooleanUtils.getBooleanStringYN(isContainRepf));
		queueJob.setFielde(
				this.stubService.getUserSignature().getUserId() + JOIN_CHAR + this.stubService.getUserSignature().getLoginGroupId() + JOIN_CHAR + this.stubService.getUserSignature().getLoginRoleId() + JOIN_CHAR
						+ lang.getId());

		if (bomSearchCondition != null)
		{
			List<Map<String, Boolean>> orderMapList = bomSearchCondition.getOrderMapList();
			if (null != orderMapList && orderMapList.size() > 0)
			{
				StringBuilder builder = new StringBuilder();
				for (Map<String, Boolean> orderMap : orderMapList)
				{
					if (builder.length() != 0)
					{
						builder.append(JOIN_CHAR);
					}
					builder.append(orderMap.toString());
				}
				queueJob.setFieldg(builder.toString().length() == 0 ? null : builder.toString());
			}

			queueJob.setFieldh(this.stubService.buildListToString(summaryFiledName));
			queueJob.setFieldf(this.stubService.buildListToString(classGuids));

			String modelInterface = null;
			queueJob.setFieldj(bomSearchCondition.getSearchValue() + JOIN_CHAR + modelInterface + JOIN_CHAR + bomScriptFileName);

			queueJob.setFieldi(
					bomReportTemplateName + JOIN_CHAR + (StringUtils.isNullString(isExportAllLevel) ? "0" : isExportAllLevel) + JOIN_CHAR + pagesize + JOIN_CHAR + reportpath);

			queueJob.setFieldk(this.stubService.buildListToString(bomSearchCondition.getBOGuidList()));
			queueJob.setFieldl(this.stubService.buildListToString(bomSearchCondition.getClassificationGuidList()));
			queueJob.setFieldm(this.stubService.buildListToString(bomSearchCondition.getResultFieldList()));
			queueJob.setFieldn(this.stubService.buildListToString(bomSearchCondition.listResultUINameList()));

			ObjectGuid objectGuid = bomSearchCondition.getObjectGuid();
			queueJob.setFieldo(objectGuid == null ? null : objectGuid.toString());
		}

		queueJob.setServerID(this.serverContext.getServerConfig().getId());
		queueJob.setJobGroup(JobGroupEnum.REPORT);
		this.stubService.getJSS().createJob(queueJob);
	}

	protected void createReportBOMJob(ObjectGuid bomViewObjectGuid, int level, SearchCondition bomSearchCondition, ReportTypeEnum exportFileType, String bomReportName,
			String exportType, String levelStyle, String groupStyle, String bomScriptFileName, String bomReportTemplateName) throws ServiceRequestException
	{
		LanguageEnum lang = this.stubService.getUserSignature().getLanguageEnum();
		Queue queueJob = new Queue();

		queueJob.setName("BOM");
		queueJob.setExecutorClass("dyna.app.service.brs.srs.job.BOMReportJob");
		queueJob.setIsSinglethRead("Y");

		queueJob.setFielda(bomViewObjectGuid.toString());

		queueJob.setFieldd(Integer.toString(level) + JOIN_CHAR + exportFileType.toString() + JOIN_CHAR + bomReportName + JOIN_CHAR + exportType + JOIN_CHAR + levelStyle + JOIN_CHAR
				+ groupStyle);

		queueJob.setFielde(
				this.stubService.getUserSignature().getUserId() + JOIN_CHAR + this.stubService.getUserSignature().getLoginGroupId() + JOIN_CHAR + this.stubService.getUserSignature().getLoginRoleId() + JOIN_CHAR
						+ lang.getId());

		if (null != bomSearchCondition)
		{
			queueJob.setFieldk(this.stubService.buildListToString(bomSearchCondition.getBOGuidList()));
			queueJob.setFieldl(this.stubService.buildListToString(bomSearchCondition.getClassificationGuidList()));
			queueJob.setFieldm(this.stubService.buildListToString(bomSearchCondition.getResultFieldList()));
			queueJob.setFieldn(this.stubService.buildListToString(bomSearchCondition.listResultUINameList()));

			List<Map<String, Boolean>> orderMapList = bomSearchCondition.getOrderMapList();
			if (null != orderMapList && orderMapList.size() > 0)
			{
				StringBuilder builder = new StringBuilder();
				for (Map<String, Boolean> orderMap : orderMapList)
				{
					if (builder.length() != 0)
					{
						builder.append(JOIN_CHAR);
					}
					builder.append(orderMap.toString());
				}
				queueJob.setFieldg(builder.length() == 0 ? null : builder.toString());
			}

			ObjectGuid objectGuid = bomSearchCondition.getObjectGuid();
			queueJob.setFieldo(objectGuid == null ? null : objectGuid.toString());

			ModelInterfaceEnum inter = null;
			String interfaceStr = inter == null ? null : inter.toString();
			queueJob.setFieldj(bomSearchCondition.getSearchValue() + JOIN_CHAR + interfaceStr + JOIN_CHAR + bomScriptFileName);
			queueJob.setFieldi(bomReportTemplateName + JOIN_CHAR + 0 + JOIN_CHAR + null);
		}
		queueJob.setServerID(this.serverContext.getServerConfig().getId());
		queueJob.setJobGroup(JobGroupEnum.REPORT);
		this.stubService.getJSS().createJob(queueJob);
	}

}