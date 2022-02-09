/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: CommonReportStub
 * Wanglei 2011-12-21
 */
package dyna.app.service.brs.srs;

import dyna.app.report.DynaReportBuilderFactory;
import dyna.app.report.GenericDynaReportBuilder;
import dyna.app.report.ReportConfiguration;
import dyna.app.report.ReportDataProvider;
import dyna.app.service.AbstractServiceStub;
import dyna.common.Criterion;
import dyna.common.SearchCondition;
import dyna.common.bean.data.DynaObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.dto.Queue;
import dyna.common.dto.Search;
import dyna.common.dto.model.cls.ClassInfo;
import dyna.common.exception.DynaDataException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import dyna.common.systemenum.*;
import dyna.common.util.DateFormat;
import dyna.common.util.SetUtils;
import dyna.common.util.StringUtils;
import dyna.customization.report.ReportBuilder;
import dyna.net.service.brs.SRS;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author Wanglei
 *
 */
@Component
public class CommonReportStub extends AbstractServiceStub<SRSImpl>
{

	public static final String REPORT_PACKAGE_BASE = "dyna.customization.report";
	public static final String	JOIN_CHAR	= "\r\n";


	/**
	 * 获取报表创建器
	 * 
	 * @param classGuid
	 *            类guid
	 * @param className
	 *            类名
	 * @param bomScriptFileName
	 *            报表UI名
	 * @return
	 */
	protected ReportBuilder getReportBuilder(String classGuid, String className, String bomScriptFileName) throws ServiceRequestException
	{
		String clasPackage = className;
		if (StringUtils.isNullString(clasPackage))
		{
			ClassInfo classInfo = this.stubService.getEMM().getClassByGuid(classGuid);
			clasPackage = classInfo.getName();
		}
		String reportBuilderClassName = REPORT_PACKAGE_BASE + "." + clasPackage + "." + bomScriptFileName;
		try
		{
			@SuppressWarnings("rawtypes")
			Class reportBuilderClass = null;
			try
			{
				reportBuilderClass = Class.forName(reportBuilderClassName);
			}
			catch (ClassNotFoundException e)
			{
				throw new ServiceRequestException("ID_APP_INVALID_REPORT_BUILDER", e.getMessage(), e);
			}

			if (!ReportBuilder.class.isAssignableFrom(reportBuilderClass))
			{
				throw new ServiceRequestException("ID_APP_INVALID_REPORT_BUILDER", "invalid report builder: " + reportBuilderClassName);
			}

			return (ReportBuilder) reportBuilderClass.newInstance();
		}
		catch (Exception e)
		{
			throw new ServiceRequestException("ID_APP_REPORT_BUILDER_INIT_FAILED", e.getLocalizedMessage(), e);
		}
	}

	protected Map<String, Object> buildReport(ReportBuilder builder, Object... params) throws ServiceRequestException
	{
		builder.setReportParameters(params);
		Map<String, Object> data = null;
		try
		{
			builder.setCreatorId(this.stubService.getUserSignature().getUserId());
			data = builder.build(this.stubService);
		}
		catch (Exception e)
		{
			if (e instanceof ServiceRequestException)
			{
				throw (ServiceRequestException) e;
			}

			throw new ServiceRequestException("ID_APP_BOM_REPORT_ERROR", e.getLocalizedMessage(), e);
		}

		if (builder.isNotifyCreator())
		{
			builder.notifyCreator(this.stubService);
		}
		return data;
	}

	protected <T extends DynaObject> void buildGenericReport(ReportConfiguration configuration, ReportDataProvider<T> provider) throws ServiceRequestException
	{
		GenericDynaReportBuilder reportBuilder = DynaReportBuilderFactory.createGenericDynaReportBuilder();
		try
		{
			reportBuilder.generateReport(configuration, provider, null);
		}
		catch (Exception e)
		{
			DynaLogger.error(e.getMessage(), e);
			throw new ServiceRequestException("ID_APP_GENERIC_REPORT_FAILED", "report error: " + e.getMessage());
		}
	}

	protected void createReportGenericPreSearchJob(String preSearchGuid, List<String> searchParameters, List<String> foundationGuid, ReportTypeEnum exportFileType)
			throws ServiceRequestException
	{
		LanguageEnum lang = this.stubService.getUserSignature().getLanguageEnum();
		Queue queueJob = new Queue();
		queueJob.setName("PreSearch");
		queueJob.setExecutorClass("dyna.app.service.brs.srs.job.PreSearchRepeortJob");
		queueJob.setIsSinglethRead("Y");

		queueJob.setFielda(preSearchGuid);
		queueJob.setFieldb(this.stubService.buildListToString(searchParameters));

		queueJob.setFieldc(exportFileType.toString());
		queueJob.setFieldd(
				this.stubService.getUserSignature().getUserId() + this.stubService.JOIN_CHAR + this.stubService.getUserSignature().getLoginGroupId() + this.stubService.JOIN_CHAR + this.stubService.getUserSignature().getLoginRoleId() + this.stubService.JOIN_CHAR
						+ lang.getId());

		if (null != foundationGuid)
		{
			int size = foundationGuid.size();
			int tempSize = 0;

			if (size <= 90)
			{
				queueJob.setFielde(foundationGuid.toString().replace(" ", "").substring(1, foundationGuid.toString().replace(" ", "").length() - 1));
			}
			else
			{
				queueJob.setFielde(this.returnValue(0, 90, foundationGuid));
				tempSize = size - 90;
				if (tempSize <= 90)
				{
					queueJob.setFieldf(this.returnValue(90, size, foundationGuid));
				}
				else
				{
					queueJob.setFieldf(this.returnValue(90, 180, foundationGuid));
					tempSize = tempSize - 90;
					if (tempSize <= 90)
					{
						queueJob.setFieldg(this.returnValue(180, size, foundationGuid));
					}
					else
					{
						queueJob.setFieldg(this.returnValue(180, 270, foundationGuid));
						tempSize = tempSize - 90;
						if (tempSize <= 90)
						{
							queueJob.setFieldh(this.returnValue(270, size, foundationGuid));
						}
						else
						{
							queueJob.setFieldh(this.returnValue(270, 360, foundationGuid));
							tempSize = tempSize - 90;
							if (tempSize <= 90)
							{
								queueJob.setFieldi(this.returnValue(360, size, foundationGuid));
							}
							else
							{
								queueJob.setFieldi(this.returnValue(360, 450, foundationGuid));
								tempSize = tempSize - 90;
								if (tempSize <= 90)
								{
									queueJob.setFieldj(this.returnValue(450, size, foundationGuid));
								}
								else
								{
									queueJob.setFieldj(this.returnValue(450, 540, foundationGuid));
									tempSize = tempSize - 90;
									if (tempSize <= 90)
									{
										queueJob.setFieldk(this.returnValue(540, size, foundationGuid));
									}
									else
									{
										queueJob.setFieldk(this.returnValue(540, 630, foundationGuid));
										tempSize = tempSize - 90;
										if (tempSize <= 90)
										{
											queueJob.setFieldl(this.returnValue(630, size, foundationGuid));
										}
										else
										{
											queueJob.setFieldl(this.returnValue(630, 720, foundationGuid));
											tempSize = tempSize - 90;
											if (tempSize <= 90)
											{
												queueJob.setFieldm(this.returnValue(720, size, foundationGuid));
											}
											else
											{
												queueJob.setFieldm(this.returnValue(720, 810, foundationGuid));
												tempSize = tempSize - 90;
												if (tempSize <= 90)
												{
													queueJob.setFieldn(this.returnValue(810, size, foundationGuid));
												}
												else
												{
													queueJob.setFieldn(this.returnValue(810, 900, foundationGuid));
													tempSize = tempSize - 90;
													if (tempSize <= 90)
													{
														queueJob.setFieldo(this.returnValue(900, size, foundationGuid));
													}
													else
													{
														queueJob.setFieldo(this.returnValue(900, 990, foundationGuid));
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		queueJob.setServerID(this.serverContext.getServerConfig().getId());
		queueJob.setJobGroup(JobGroupEnum.REPORT);
		this.stubService.getJSS().createJob(queueJob);
	}

	private String returnValue(int beginSize, int endSize, List<String> foundationGuid)
	{
		List<String> tempFoundationGuid = foundationGuid.subList(beginSize, endSize);
		return tempFoundationGuid.toString().replace(" ", "").substring(1, tempFoundationGuid.toString().replace(" ", "").length() - 1);
	}

	public void createReportGenericProductSummaryObjectJob(ObjectGuid productObjectGuid, SearchCondition searchCondition, String relationTemplateName,
			ReportTypeEnum exportFileType, String bomReportTemplateName, String reportName, String pagesize, String reportpath) throws ServiceRequestException
	{
		LanguageEnum lang = this.stubService.getUserSignature().getLanguageEnum();
		Queue queueJob = new Queue();
		if (StringUtils.isNullString(pagesize))
		{
			pagesize = "20";
		}
		queueJob.setName("Summary");
		queueJob.setExecutorClass("dyna.app.service.brs.srs.job.SummaryReportJob");
		queueJob.setIsSinglethRead("Y");

		queueJob.setFielda(productObjectGuid.toString());

		queueJob.setFieldb(
				this.stubService.getUserSignature().getUserId() + this.stubService.JOIN_CHAR + this.stubService.getUserSignature().getLoginGroupId() + SRS.JOIN_CHAR + this.stubService.getUserSignature().getLoginRoleId() + this.stubService.JOIN_CHAR
						+ lang.getId());
		queueJob.setFieldc(this.stubService.buildListToString(searchCondition.getBOGuidList()));
		queueJob.setFieldd(this.stubService.buildListToString(searchCondition.getClassificationGuidList()));
		queueJob.setFielde(searchCondition.getSearchValue());

		List<Map<String, Boolean>> orderMapList = searchCondition.getOrderMapList();
		if (null != orderMapList && orderMapList.size() > 0)
		{
			StringBuilder builder = new StringBuilder();
			for (Map<String, Boolean> orderMap : orderMapList)
			{
				if (builder.length() != 0)
				{
					builder.append(this.stubService.JOIN_CHAR);
				}
				builder.append(orderMap.toString());
			}
			queueJob.setFieldf(builder.toString().length() == 0 ? null : builder.toString());
		}

		ModelInterfaceEnum inter = null;
		queueJob.setFieldg(inter == null ? null : inter.toString());
		queueJob.setFieldh(0 + this.stubService.JOIN_CHAR + 5000 + this.stubService.JOIN_CHAR + "1");
		queueJob.setFieldi(exportFileType.toString() + this.stubService.JOIN_CHAR + bomReportTemplateName + this.stubService.JOIN_CHAR + reportName + this.stubService.JOIN_CHAR + pagesize + this.stubService.JOIN_CHAR + reportpath);
		queueJob.setFieldj(relationTemplateName);
		queueJob.setFieldk(searchCondition.getObjectGuid().getClassName());
		if (!SetUtils.isNullList(searchCondition.getCriterionList()))
		{
			for (Criterion criter : searchCondition.getCriterionList())
			{
				if (criter.getKey().equalsIgnoreCase(SystemClassFieldEnum.CLASSGUID.getName()))
				{
					queueJob.setFieldl(criter.getKey());
					queueJob.setFieldm(criter.getValue() == null ? null : criter.getValue().toString());
				}
				else if (criter.getKey().equalsIgnoreCase(SystemClassFieldEnum.CLASSIFICATION.getName()))
				{
					queueJob.setFieldn(criter.getKey());
					queueJob.setFieldo(criter.getValue() == null ? null : criter.getValue().toString());
				}
			}
		}
		queueJob.setServerID(this.serverContext.getServerConfig().getId());
		queueJob.setJobGroup(JobGroupEnum.REPORT);

		this.stubService.getJSS().createJob(queueJob);
	}

	protected void createReportGenericInstacnceObjectJob(String uiName, ReportTypeEnum exportFileType, List<ObjectGuid> objectGuidList, SearchCondition reportCondition)
			throws ServiceRequestException
	{
		LanguageEnum lang = this.stubService.getUserSignature().getLanguageEnum();
		Queue queueJob = new Queue();
		queueJob.setName("FoundationReport");
		queueJob.setExecutorClass("dyna.app.service.brs.srs.job.GenericReportJob");
		queueJob.setIsSinglethRead("Y");

		if (!SetUtils.isNullList(objectGuidList))
		{
			// 当批量导出多个对象时，客户端队列通知中需要显示第一个对象+“,...”，因此，需要区分是多个对象还是一个对象
			if (objectGuidList.size() == 1)
			{
				queueJob.setFielda(objectGuidList.get(0).toString());
			}
			else
			{
				queueJob.setFielda(objectGuidList.get(0).toString() + ";" + objectGuidList.get(1).toString());
			}
		}

		String loginInfo =
				this.stubService.getUserSignature().getUserId() + this.stubService.JOIN_CHAR + this.stubService.getUserSignature().getLoginGroupId() + this.stubService.JOIN_CHAR + this.stubService.getUserSignature().getLoginRoleId() + this.stubService.JOIN_CHAR
						+ lang.getId();

		queueJob.setFieldb(loginInfo + this.stubService.JOIN_CHAR + uiName + this.stubService.JOIN_CHAR + exportFileType.toString());

		this.addAllListToQueue(queueJob, objectGuidList);

		queueJob.setServerID(this.serverContext.getServerConfig().getId());
		queueJob.setJobGroup(JobGroupEnum.REPORT);
		this.stubService.getJSS().createJob(queueJob);
	}

	private void addAllListToQueue(Queue queueJob, List<ObjectGuid> objectGuidList)
	{
		if (objectGuidList != null)
		{
			// 字段长度为4000，guid长度为32，分割字符“\r\n”长度为4，最多保存(32+4)*111=3996个长度的字符串
			int MAX_SIZE = 111;
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < objectGuidList.size(); i++)
			{
				if (builder.length() != 0)
				{
					builder.append(JOIN_CHAR);
				}
				builder.append(objectGuidList.get(i).getGuid());

				if (i <= 1 * MAX_SIZE)
				{
					queueJob.setFieldc(builder.toString());
					if (i == 1 * MAX_SIZE)
					{
						builder.setLength(0);
					}
				}
				if (i > 1 * MAX_SIZE && i <= 2 * MAX_SIZE)
				{
					queueJob.setFieldd(builder.toString());
					if (i == 2 * MAX_SIZE)
					{
						builder.setLength(0);
					}
				}
				else if (i > 2 * MAX_SIZE && i <= 3 * MAX_SIZE)
				{
					queueJob.setFielde(builder.toString());
					if (i == 3 * MAX_SIZE)
					{
						builder.setLength(0);
					}
				}
				else if (i > 3 * MAX_SIZE && i <= 4 * MAX_SIZE)
				{
					queueJob.setFieldf(builder.toString());
					if (i == 4 * MAX_SIZE)
					{
						builder.setLength(0);
					}
				}
				else if (i > 4 * MAX_SIZE && i <= 5 * MAX_SIZE)
				{
					queueJob.setFieldg(builder.toString());
					if (i == 5 * MAX_SIZE)
					{
						builder.setLength(0);
					}
				}
				else if (i > 5 * MAX_SIZE && i <= 6 * MAX_SIZE)
				{
					queueJob.setFieldh(builder.toString());
					if (i == 6 * MAX_SIZE)
					{
						builder.setLength(0);
					}
				}
				else if (i > 6 * MAX_SIZE && i <= 7 * MAX_SIZE)
				{
					queueJob.setFieldi(builder.toString());
					if (i == 7 * MAX_SIZE)
					{
						builder.setLength(0);
					}
				}
				else if (i > 7 * MAX_SIZE && i <= 8 * MAX_SIZE)
				{
					queueJob.setFieldj(builder.toString());
					if (i == 8 * MAX_SIZE)
					{
						builder.setLength(0);
					}
				}
				else if (i > 8 * MAX_SIZE && i <= 9 * MAX_SIZE)
				{
					queueJob.setFieldk(builder.toString());
					if (i == 9 * MAX_SIZE)
					{
						builder.setLength(0);
					}
				}
				else if (i > 9 * MAX_SIZE && i <= 10 * MAX_SIZE)
				{
					queueJob.setFieldl(builder.toString());
					if (i == 10 * MAX_SIZE)
					{
						builder.setLength(0);
					}
				}
				else if (i > 10 * MAX_SIZE && i <= 11 * MAX_SIZE)
				{
					queueJob.setFieldm(builder.toString());
					if (i == 11 * MAX_SIZE)
					{
						builder.setLength(0);
					}
				}
				else if (i > 11 * MAX_SIZE && i <= 12 * MAX_SIZE)
				{
					queueJob.setFieldn(builder.toString());
					if (i == 12 * MAX_SIZE)
					{
						builder.setLength(0);
					}
				}
				else if (i > 12 * MAX_SIZE && i <= 13 * MAX_SIZE)
				{
					queueJob.setFieldo(builder.toString());
					if (i == 13 * MAX_SIZE)
					{
						builder.setLength(0);
					}
				}
				else if (i > 14 * MAX_SIZE)
				{
					break;
				}
			}
		}
	}


	protected  void reportGenericECScript(String uiName, List<ObjectGuid> objectGuidList, ReportTypeEnum exportFileType, SearchCondition reportCondition, boolean isScript,
			boolean isMail) throws ServiceRequestException
	{
		LanguageEnum lang = this.stubService.getUserSignature().getLanguageEnum();
		Queue queueJob = new Queue();
		queueJob.setName("ECReport");
		queueJob.setExecutorClass("dyna.app.service.brs.srs.job.ECReportJob");
		queueJob.setIsSinglethRead("Y");
		queueJob.setType("ECReport");

		if (!SetUtils.isNullList(objectGuidList))
		{
			// 当批量导出多个对象时，客户端队列通知中需要显示第一个对象+“,...”，因此，需要区分是多个对象还是一个对象
			if (objectGuidList.size() == 1)
			{
				queueJob.setFielda(objectGuidList.get(0).toString());
			}
			else
			{
				queueJob.setFielda(objectGuidList.get(0).toString() + ";" + objectGuidList.get(1).toString());
			}
		}

		String loginInfo = "admin" + JOIN_CHAR + "ADMINISTRATOR" + JOIN_CHAR + "ADMINISTRATOR" + JOIN_CHAR + lang.getId();
		queueJob.setFieldb(loginInfo + JOIN_CHAR + uiName + JOIN_CHAR + exportFileType.name() + JOIN_CHAR + (isScript ? "Y" : "N") + JOIN_CHAR + (isMail ? "Y" : "N"));

		this.addAllECListToQueue(queueJob, objectGuidList);

		queueJob.setServerID(this.serverContext.getServerConfig().getId());
		queueJob.setJobGroup(JobGroupEnum.REPORT);
		this.stubService.getJSS().createJob(queueJob);
	}

	private void addAllECListToQueue(Queue queueJob, List<ObjectGuid> objectGuidList)
	{
		Map<String, List<ObjectGuid>> Map = new HashMap<String, List<ObjectGuid>>();
		if (objectGuidList != null)
		{
			for (ObjectGuid objectGuid : objectGuidList)
			{
				String className = objectGuid.getClassName();
				if (className == null)
				{
					try
					{
						className = this.stubService.getEMM().getClassByGuid(objectGuid.getGuid()).getName();
					}
					catch (ServiceRequestException e)
					{
						e.printStackTrace();
					}
				}
				if (className == null)
				{
					continue;
				}
				if (Map.get(className) == null)
				{
					Map.put(className, new ArrayList<ObjectGuid>());
				}
				Map.get(className).add(objectGuid);
			}
		}

		List<String> tmpGuidList = new ArrayList<String>();
		for (String className : Map.keySet())
		{
			List<ObjectGuid> objectGuidList_ = Map.get(className);

			// 字段长度为4000，guid长度为32，分割字符“\r\n”长度为4,再减去类名的长度加上分隔符“$”，即为存储总长度
			int MAX_SIZE = (4000 - className.length() - 1);
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < objectGuidList_.size(); i++)
			{
				// 字符串的长度+分割字符“\r\n”长度+32位guid长度大于可容纳的最大长度MAX_SIZE，则分割
				if (builder.length() + 36 > MAX_SIZE)
				{
					tmpGuidList.add(builder.toString());
					builder.setLength(0);
				}

				if (builder.length() > 0)
				{
					builder.append(JOIN_CHAR);
				}
				if (builder.length() == 0)
				{
					builder.append(className + "$");
				}
				builder.append(objectGuidList_.get(i).getGuid());

				if (i == objectGuidList_.size() - 1)
				{
					tmpGuidList.add(builder.toString());
				}
			}
		}

		for (int i = 0; i < tmpGuidList.size(); i++)
		{
			if (i == 0)
			{
				queueJob.setFieldc(tmpGuidList.get(i));
			}
			else if (i == 1)
			{
				queueJob.setFieldd(tmpGuidList.get(i));
			}
			else if (i == 2)
			{
				queueJob.setFielde(tmpGuidList.get(i));
			}
			else if (i == 3)
			{
				queueJob.setFieldf(tmpGuidList.get(i));
			}
			else if (i == 4)
			{
				queueJob.setFieldg(tmpGuidList.get(i));
			}
			else if (i == 5)
			{
				queueJob.setFieldh(tmpGuidList.get(i));
			}
			else if (i == 6)
			{
				queueJob.setFieldi(tmpGuidList.get(i));
			}
			else if (i == 7)
			{
				queueJob.setFieldj(tmpGuidList.get(i));
			}
			else if (i == 8)
			{
				queueJob.setFieldk(tmpGuidList.get(i));
			}
			else if (i == 9)
			{
				queueJob.setFieldl(tmpGuidList.get(i));
			}
			else if (i == 10)
			{
				queueJob.setFieldm(tmpGuidList.get(i));
			}
			else if (i == 11)
			{
				queueJob.setFieldn(tmpGuidList.get(i));
			}
			else if (i == 12)
			{
				queueJob.setFieldo(tmpGuidList.get(i));
			}
			else
			{
				break;
			}
		}
	}

	 protected void reportAdvancedSearchObjectJob(SearchCondition searchCondition) throws ServiceRequestException
	 {
		 LanguageEnum lang = this.stubService.getUserSignature().getLanguageEnum();
		 Queue queueJob = new Queue();
		 queueJob.setName("AdvancedSearch");
		 queueJob.setExecutorClass("dyna.app.service.brs.srs.job.AdvancedSearchJob");
		 queueJob.setIsSinglethRead("Y");
		 queueJob.setFieldj(
				 this.stubService.getUserSignature().getUserId() + JOIN_CHAR + this.stubService.getUserSignature().getLoginGroupId() + JOIN_CHAR + this.stubService.getUserSignature().getLoginRoleId() + JOIN_CHAR
						 + lang.getId());

		 try
		 {
			 List<Criterion> criterionList = searchCondition.getCriterionList();
			 StringBuffer condition = new StringBuffer();

			 // 拼凑检索条件到firstCondition中
			 for (Criterion criterion : criterionList)
			 {
				 Object value = criterion.getValue();

				 if (value instanceof Date)
				 {
					 value = DateFormat.formatYMDHMS((Date) value);
				 }

				 if (value != null && !"".equals(value))
				 {
					 value = value.toString().replace(",", Search.comma).replace(";", Search.semicolon);
				 }

				 condition.append(criterion.getConjunction());
				 condition.append(",");
				 condition.append(criterion.getKey());
				 condition.append(",");
				 condition.append(criterion.getOperateSignEnum());
				 condition.append(",");
				 condition.append(StringUtils.convertNULLtoString(value));
				 condition.append(",");
				 condition.append(criterion.getFieldOrignType().name());
				 condition.append(";");

			 }
			 String firstCondition = null;
			 String secondCondition = null;
			 firstCondition = condition.toString();

			 // 如果大于1024Byte 则把大于1024byte的字符串放到secondCondition中
			 if (condition.length() > Search.maxByte)
			 {
				 secondCondition = condition.substring(Search.maxByte);
				 firstCondition = condition.substring(0, Search.maxByte);
			 }
			 queueJob.setFieldb(firstCondition);
			 if (null != secondCondition)
			 {
				 queueJob.setFieldc(secondCondition);
			 }

			 boolean hasSubFolders = searchCondition.hasSubFolders();
			 boolean isCheckOutOnly = searchCondition.isCheckOutOnly();
			 boolean isIncludeOBS = searchCondition.isIncludeOBS();
			 AdvancedQueryTypeEnum searchTypeEnum = searchCondition.getSearchType();
			 String searchType = searchTypeEnum == null ? AdvancedQueryTypeEnum.NORMAL.name() : searchTypeEnum.name();

			 String revisiontype = "";
			 if (searchCondition.getSearchRevisionTypeEnum() == null)
			 {
				 revisiontype = SearchRevisionTypeEnum.ISHISTORYREVISION.getType();
			 }
			 else
			 {
				 revisiontype = searchCondition.getSearchRevisionTypeEnum().getType();
			 }

			 queueJob.setFieldd(hasSubFolders + JOIN_CHAR + isCheckOutOnly + JOIN_CHAR + isIncludeOBS + JOIN_CHAR + revisiontype + JOIN_CHAR + searchType);

			 boolean caseSensitive = searchCondition.caseSensitive();
			 boolean isOwnerOnly = searchCondition.isOwnerOnly();
			 boolean isOwnerGroupOnly = searchCondition.isOwnerGroupOnly();
			 boolean isAdvanced = searchCondition.isAdvanced();

			 queueJob.setFielde(caseSensitive + JOIN_CHAR + isOwnerOnly + JOIN_CHAR + isOwnerGroupOnly + JOIN_CHAR + isAdvanced);

			 String userProductBOGuid = searchCondition.getUserProductBOGuid();
			 String parentGuid = searchCondition.getParentGuid();

			 String mpdelInterface = null;
			 String BOMTemplateId = searchCondition.getBOMTemplateId();
			 String relationTemplateId = searchCondition.getRelationTemplateId();
			 queueJob.setFieldf(userProductBOGuid + JOIN_CHAR + parentGuid + JOIN_CHAR + mpdelInterface + JOIN_CHAR + BOMTemplateId + JOIN_CHAR + relationTemplateId);

			 ObjectGuid object = searchCondition.getObjectGuid();
			 String classguid = object.getClassGuid();
			 String classname = object.getClassName();
			 String foldguid = searchCondition.getFolder().getGuid();
			 queueJob.setFieldg(classguid + JOIN_CHAR + classname + JOIN_CHAR + foldguid);
			 if (null != searchCondition.listResultUINameList())
			 {
				 String uiName = searchCondition.listResultUINameList().toString();
				 queueJob.setFieldh(uiName.substring(1, uiName.length() - 1));
			 }
		 }
		 catch (DynaDataException e)
		 {
			 e.printStackTrace();
			 throw ServiceRequestException.createByException("ID_APP_SERVER_EXCEPTION", e);
		 }
		 catch (Exception e)
		 {
			 e.printStackTrace();
			 if (e instanceof ServiceRequestException)
			 {
				 throw (ServiceRequestException) e;
			 }
			 else
			 {
				 throw ServiceRequestException.createByException("ID_APP_SERVER_EXCEPTION", e);
			 }
		 }
		 queueJob.setServerID(this.serverContext.getServerConfig().getId());
		 queueJob.setJobGroup(JobGroupEnum.REPORT);
		 this.stubService.getJSS().createJob(queueJob);
	 }


}
