package dyna.app.service.brs.cpb;

import dyna.app.service.BusinessRuleService;
import dyna.app.service.brs.boas.BOASImpl;
import dyna.common.SearchCondition;
import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.bean.data.StructureObject;
import dyna.common.bean.data.configparamter.*;
import dyna.common.bean.data.foundation.ViewObject;
import dyna.common.bean.data.structure.BOMStructure;
import dyna.common.conf.ServiceDefinition;
import dyna.common.dto.DataRule;
import dyna.common.dto.model.bmbo.BOInfo;
import dyna.common.exception.ServiceNotFoundException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import dyna.common.systemenum.ConfigParameterTableType;
import dyna.common.util.EnvUtils;
import dyna.common.util.StringUtils;
import dyna.net.service.brs.*;
import dyna.net.service.das.JSS;
import dyna.net.service.das.MSRM;
import dyna.net.service.data.InstanceService;
import dyna.net.service.data.RelationService;
import dyna.net.service.data.SystemDataService;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.dubbo.config.annotation.DubboReference;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Getter(AccessLevel.PROTECTED)
@Service public class CPBImpl extends BusinessRuleService implements CPB
{
	private DetailPositionEnum detailPosition = DetailPositionEnum.Right;
	private int                columns        = 10;

	@DubboReference private InstanceService   instanceService;
	@DubboReference private RelationService   relationService;
	@DubboReference private SystemDataService systemDataService;

	@Autowired private Async async;
	@Autowired private BOAS boas;
	@Autowired private BOMS boms;
	@Autowired private EDAP edap;
	@Autowired private EMM emm;
	@Autowired private JSS jss;
	@Autowired private MSRM msrm;

	@Autowired private CPBStub            cpbStub;
	@Autowired private OrderConfigureStub orderConfigureStub;
	@Autowired private DrivenStub         drivenStub;
	@Autowired private ConfigCheckStub    configCheckStub;
	@Autowired private DrivenHistoryStub  drivenHistoryStub;
	@Autowired private ConfigQueryStub    configQueryStub;
	@Autowired private VarCalculateStub   varCalculateStub;

	public DocumentMark exportDocument;

	@Override public void init(ServiceDefinition serviceDefinition)
	{
		super.init(serviceDefinition);
		this.initParam();
		this.initExportConfigFile();
	}

	private void initExportConfigFile()
	{
		File file = new File(EnvUtils.getConfRootPath() + "conf" + File.separator + DocumentMark.FILENAME);
		if (file.exists())
		{
			if (exportDocument == null || exportDocument.isChanged(file.lastModified()))
			{
				try
				{
					Document document = new SAXBuilder().build(file);
					exportDocument = new DocumentMark(document, file.lastModified());
				}
				catch (JDOMException e)
				{
					DynaLogger.info(e);
					e.printStackTrace();
				}
				catch (IOException e)
				{
					DynaLogger.info(e);
					e.printStackTrace();
				}
			}
		}
	}

	private void initParam()
	{
		String value = this.getServiceDefinition().getParam().get("columns");
		if (!StringUtils.isNullString(value))
		{
			columns = Integer.parseInt(value);
		}
		String position = this.getServiceDefinition().getParam().get("position");
		if (!StringUtils.isNullString(position))
		{
			detailPosition = DetailPositionEnum.getEnumByValue(position);
		}
	}

	protected InstanceService getInstanceService()
	{
		return this.instanceService;
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

	protected BOMS getBoms()
	{
		return this.boms;
	}

	public CPBStub getCPBStub()
	{
		return this.cpbStub;
	}

	public OrderConfigureStub getOrderConfigureStub()
	{
		return this.orderConfigureStub;
	}

	public ConfigQueryStub getConfigQueryStub()
	{
		return this.configQueryStub;
	}

	public ConfigCheckStub getConfigCheckStub()
	{
		return this.configCheckStub;
	}

	public VarCalculateStub getVarCalculateStub()
	{
		return varCalculateStub;
	}

	public DrivenStub getDrivenStub()
	{
		return this.drivenStub;
	}

	public DrivenHistoryStub getDrivenHistoryStub()
	{
		return this.drivenHistoryStub;
	}

	@Override public List<DynamicColumnTitle> listColumnTitles(ObjectGuid objectGuid, ConfigParameterTableType tableTypeEnum, Date ruleTime) throws ServiceRequestException
	{
		return this.getConfigQueryStub().listColumnTitles(objectGuid, tableTypeEnum, ruleTime);
	}

	@Override public List<TableOfList> listTableOfListData(ObjectGuid objectGuid, ConfigParameterTableType tableTypeEnum, Date ruleTime) throws ServiceRequestException
	{
		return this.getConfigQueryStub().listTableOfListData(objectGuid, tableTypeEnum, ruleTime);
	}

	@Override public List<TableOfMark> listTableOfMarkData(ObjectGuid objectGuid, Date ruleTime) throws ServiceRequestException
	{
		return this.getConfigQueryStub().listTableOfMarkData(objectGuid, ruleTime);
	}

	@Override public List<TableOfList> listAllList(ObjectGuid objectGuid, Date ruleTime) throws ServiceRequestException
	{
		return this.getConfigQueryStub().listAllList(objectGuid, ruleTime);
	}

	@Override public List<TableOfGroup> listTableOfGroup(ObjectGuid objectGuid, Date ruleTime) throws ServiceRequestException
	{
		return this.getConfigQueryStub().listTableOfGroup(objectGuid, ruleTime);
	}

	@Override public List<TableOfRegion> listTableOfRegion(ObjectGuid objectGuid, ConfigParameterTableType tableTypeEnum, Date ruleTime) throws ServiceRequestException
	{
		return this.getConfigQueryStub().listTableOfRegion(objectGuid, tableTypeEnum, ruleTime);
	}

	@Override public List<TableOfExpression> listTableOfExpression(ObjectGuid objectGuid, Date ruleTime) throws ServiceRequestException
	{
		return this.getConfigQueryStub().listTableOfExpression(objectGuid, ruleTime);
	}

	@Override public List<TableOfParameter> listTableOfParameterData(ObjectGuid objectGuid, Date ruleTime) throws ServiceRequestException
	{
		return this.getConfigQueryStub().listTableOfParameter(objectGuid, null, ruleTime);
	}

	@Override public List<TableOfParameter> listTableOfParameter(ObjectGuid objectGuid, String gNumber, Date ruleTime) throws ServiceRequestException
	{
		return this.getConfigQueryStub().listTableOfParameter(objectGuid, gNumber, ruleTime);
	}

	@Override public List<TableOfInputVariable> listTableOfInputVariable(ObjectGuid objectGuid, Date ruleTime) throws ServiceRequestException
	{
		return this.getConfigQueryStub().listTableOfInputVariable(objectGuid, ruleTime);
	}

	@Override public List<TableOfGroup> saveTableOfGroup(ObjectGuid end1ObjectGuid, Date ruleTime, List<DynamicColumnTitle> columnTitleList, List<TableOfGroup> groupList,
			List<TableOfGroup> deleteListList, List<DynamicColumnTitle> deleteColumnTitleList) throws ServiceRequestException
	{
		return this.getCPBStub().saveTableOfGroup(end1ObjectGuid, ruleTime, groupList, columnTitleList, deleteListList, deleteColumnTitleList);
	}

	@Override public List<TableOfList> saveTableOfList(ObjectGuid end1ObjectGuid, Date ruleTime, ConfigParameterTableType tableType, List<DynamicColumnTitle> columnTitleList,
			List<TableOfList> tableOfListList, List<TableOfList> deleteLineList, List<DynamicColumnTitle> deleteColumnTitleList) throws ServiceRequestException
	{
		return this.getCPBStub().saveTableOfList(end1ObjectGuid, ruleTime, tableType, tableOfListList, columnTitleList, deleteLineList, deleteColumnTitleList);
	}

	@Override public List<TableOfExpression> saveTableOfExpression(ObjectGuid end1ObjectGuid, Date ruleTime, List<TableOfExpression> tableOfExpressionList,
			List<TableOfExpression> deleteColumnList) throws ServiceRequestException
	{
		return this.getCPBStub().saveTableOfExpression(end1ObjectGuid, ruleTime, tableOfExpressionList, deleteColumnList);
	}

	@Override public List<TableOfInputVariable> saveTableOfInputVariable(ObjectGuid end1ObjectGuid, Date ruleTime, List<TableOfInputVariable> tableOfInputVariableList,
			List<TableOfInputVariable> deleteColumnList) throws ServiceRequestException
	{
		return this.getCPBStub().saveTableOfInputVariable(end1ObjectGuid, ruleTime, tableOfInputVariableList, deleteColumnList);
	}

	@Override public List<TableOfMark> saveTableOfMark(ObjectGuid end1ObjectGuid, Date ruleTime, List<TableOfMark> tableOfMarkList, List<TableOfMark> deleteColumnList)
			throws ServiceRequestException
	{
		return this.getCPBStub().saveTableOfMark(end1ObjectGuid, ruleTime, tableOfMarkList, deleteColumnList);
	}

	@Override public List<TableOfParameter> saveTableOfParameter(ObjectGuid end1ObjectGuid, Date ruleTime, List<DynamicColumnTitle> columnTitleList,
			List<TableOfParameter> tableOfParameterList, List<TableOfParameter> deleteLineList, List<DynamicColumnTitle> deleteColumnTitleList) throws ServiceRequestException
	{
		return this.getCPBStub().saveTableOfParameter(end1ObjectGuid, ruleTime, tableOfParameterList, columnTitleList, deleteLineList, deleteColumnTitleList);
	}

	@Override public List<TableOfRegion> saveTableOfRegion(ObjectGuid end1ObjectGuid, Date ruleTime, ConfigParameterTableType tableType, List<DynamicColumnTitle> columnTitleList,
			List<TableOfRegion> tableOfRegionList, List<TableOfRegion> deleteLineList, List<DynamicColumnTitle> deleteColumnTitleList) throws ServiceRequestException
	{
		return this.getCPBStub().saveTableOfRegion(end1ObjectGuid, ruleTime, tableType, tableOfRegionList, columnTitleList, deleteLineList, deleteColumnTitleList);
	}

	@Override public DrivenResult drivenTestByConfigRules(FoundationObject end1, SearchCondition strucSearchCondition, SearchCondition end2SearchCondition, DataRule dataRule,
			String gNumber, String lNumbers, String inptVarriables) throws ServiceRequestException
	{
		return this.getDrivenStub().drivenTestByConfigRules(end1, strucSearchCondition, end2SearchCondition, dataRule, gNumber, lNumbers, inptVarriables);
	}

	@Override public TableOfList getDefaultL00Number(ObjectGuid objectGuid, Date ruleTime) throws ServiceRequestException
	{
		return this.getConfigQueryStub().getDefaultL00Number(objectGuid, ruleTime);
	}

	@Override public void saveTestHistory(TestHistory testHistory) throws ServiceRequestException
	{
		this.getDrivenHistoryStub().saveTestHistory(testHistory);
	}

	@Override public List<TestHistory> listTestHistory(TestHistory history) throws ServiceRequestException
	{
		return this.getDrivenHistoryStub().listTestHistory(history);
	}

	@Override public TestHistory getTestHistory(TestHistory history) throws ServiceRequestException
	{
		return this.getDrivenHistoryStub().getTestHistory(history);
	}

	@Override public void deleteTestHistory(TestHistory history) throws ServiceRequestException
	{
		this.getDrivenHistoryStub().deleteTestHistory(history);
	}

	@Override public List<String> checkAllValuesOfTabs(ObjectGuid end1ObjectGuid, DataRule dataRule) throws ServiceRequestException
	{
		return this.getConfigCheckStub().checkAllValuesOfTabs(end1ObjectGuid, dataRule);
	}

	@Override public void copyConfigData(ObjectGuid destObjectGuid, ObjectGuid origObjectGuid, Date ruleTime, List<ConfigParameterTableType> tableTypeList)
			throws ServiceRequestException
	{
		this.getCPBStub().copyConfigData(destObjectGuid, origObjectGuid, ruleTime, tableTypeList);
	}

	@Override public boolean isConfigTableCanBeCopy(String masterGuid) throws ServiceRequestException
	{
		return this.getCPBStub().isConfigTableCanBeCopy(masterGuid);
	}

	@Override public List<BOInfo> listOrderBoinfo() throws ServiceRequestException
	{
		return this.getOrderConfigureStub().listOrderBoinfo();
	}

	@Override public void saveOrderBoinfo(List<BOInfo> value) throws ServiceRequestException
	{
		this.getOrderConfigureStub().saveOrderBoinfo(value);
	}

	@Override public List<DynamicOfColumn> listAllVariableOfLTable(ObjectGuid objectGuid, Date ruleTime, String lNumbers) throws ServiceRequestException
	{
		return this.getConfigQueryStub().listAllVariableOfLTable(objectGuid, ruleTime, lNumbers);
	}

	@Override public List<String> checkAllValuesOfTabs(FoundationObject end1, DataRule dataRule, List<StructureObject> end2StrucList, ConfigVariable configVariable)
			throws ServiceRequestException
	{
		return this.getConfigCheckStub().checkAllValuesOfTabs(end1, dataRule, end2StrucList, configVariable);
	}

	@Override public List<StructureObject> listStructureObject(ObjectGuid end1ObjectGuid, DataRule dataRule, String viewName, SearchCondition strucSearchCondition,
			SearchCondition end2SearchCondition) throws ServiceRequestException
	{
		return this.getConfigQueryStub().listStructureObject(end1ObjectGuid, viewName, dataRule, strucSearchCondition, end2SearchCondition);
	}

	@Override public List<StructureObject> listStructureObject(ObjectGuid end1ObjectGuid, DataRule dataRule, List<String> gNumberList, SearchCondition strucSearchCondition,
			SearchCondition end2SearchCondition) throws ServiceRequestException
	{
		return this.getConfigQueryStub().listStructureObject(end1ObjectGuid, dataRule, gNumberList, strucSearchCondition, end2SearchCondition);
	}

	@Override public void deleteRelation(ObjectGuid viewObjectGuid) throws ServiceRequestException
	{
		this.getCPBStub().deleteRelation(viewObjectGuid);
	}

	@Override public List<List<String>> check4Order(ObjectGuid objectGuid, DataRule dataRule, boolean allCheck) throws ServiceRequestException
	{
		return this.getOrderConfigureStub().check4Order(objectGuid, dataRule, allCheck);
	}

	@Override public List<StructureObject> driveResult4Order(FoundationObject item, FoundationObject draw, DataRule dataRule, SearchCondition itemSearchCondition)
			throws ServiceRequestException
	{
		return this.getOrderConfigureStub().driveResult4Order(item, draw, dataRule, itemSearchCondition);
	}

	@Override public FoundationObject saveDrivenTestResult(ObjectGuid end1ObjectGuid, SearchCondition strucSearchCondition, SearchCondition end2SearchCondition, DataRule dataRule,
			String gNumber, String lNumbers, String inptVarriables) throws ServiceRequestException
	{
		return this.getDrivenStub().saveDrivenTestResult(end1ObjectGuid, strucSearchCondition, end2SearchCondition, dataRule, gNumber, lNumbers, inptVarriables);
	}

	@Override public FoundationObject getDrawInstanceByOrderDetail(String drawNo, Date ruleTime) throws ServiceRequestException
	{
		return this.getOrderConfigureStub().getDrawInstanceByOrderDetail(drawNo, ruleTime);
	}

	@Override public FoundationObject saveObject(FoundationObject foundationObject) throws ServiceRequestException
	{
		return ((BOASImpl) this.getBoas()).getFSaverStub().saveObject(foundationObject, true, true, false, null, true, true, false);
	}

	@Override public void unlink(StructureObject structureObject) throws ServiceRequestException
	{
		this.getOrderConfigureStub().unlink(structureObject);
	}

	@Override public List<BOMStructure> listBOM(FoundationObject item, SearchCondition searchCondition, DataRule dataRule, String origGNumber) throws ServiceRequestException
	{
		return this.getCPBStub().listBOM(item, searchCondition, dataRule, origGNumber);
	}

	@Override public FoundationObject getDrawInstanceByItem(FoundationObject item, Date ruleTime) throws ServiceRequestException
	{
		return this.getOrderConfigureStub().getDrawInstanceByItem(item, ruleTime);
	}

	@Override public void deleteAllConfig(ObjectGuid objectGuid) throws ServiceRequestException
	{
		this.getCPBStub().deleteAllConfig(objectGuid);
	}

	@Override public FoundationObject getDrawing(ObjectGuid objectGuid, Date ruleTime) throws ServiceRequestException
	{
		return this.getCPBStub().getDrawing(objectGuid, ruleTime);
	}

	@Override public ConfigVariable buildConfigVariable(FoundationObject instance, Date ruleTime) throws ServiceRequestException
	{
		return this.getCPBStub().buildConfigVariable(instance, ruleTime);
	}

	@Override public StructureObject linkNoCheckCycle(ViewObject viewObject, FoundationObject end1FoundationObject, ObjectGuid end2FoundationObjectGuid,
			StructureObject structureObject) throws ServiceRequestException
	{
		return this.getCPBStub().link(viewObject, end1FoundationObject, end2FoundationObjectGuid, structureObject, true, null);
	}

	@Override public DrivenResult drivenTestByConfigRulesAll(FoundationObject end1, SearchCondition strucSearchCondition, SearchCondition end2SearchCondition, DataRule dataRule,
			String gNumber, String lNumbers, String inptVarriables) throws ServiceRequestException
	{
		return this.getDrivenStub().drivenTestByConfigRulesAll(end1, strucSearchCondition, end2SearchCondition, dataRule, gNumber, lNumbers, inptVarriables);
	}

	@Override public DetailPositionEnum getDetailPosition() throws ServiceRequestException
	{
		return this.detailPosition;
	}

	@Override public int getResultVariableNumber() throws ServiceRequestException
	{
		return this.columns;
	}

	@Override public List<TableOfMultiCondition> listTableOfMultiVariable(ObjectGuid objectGuid, Date ruleTime) throws ServiceRequestException
	{
		return this.getConfigQueryStub().listTableOfMultiVariable(objectGuid, ruleTime);
	}

	@Override public List<TableOfMultiCondition> saveTableOfMultiVariable(ObjectGuid end1ObjectGuid, Date ruleTime, List<TableOfMultiCondition> tableOfMultiConditionList,
			List<TableOfMultiCondition> deleteLineList) throws ServiceRequestException
	{
		return this.getCPBStub().saveTableOfMultiVariable(end1ObjectGuid, ruleTime, tableOfMultiConditionList, deleteLineList);
	}

	@Override public DocumentMark getExportConfigFile() throws ServiceRequestException
	{
		this.initExportConfigFile();
		return this.exportDocument;
	}

	@Override public void deleteConfigByParam(ObjectGuid objectGuid, List<ConfigParameterTableType> tableTypes) throws ServiceRequestException
	{
		this.getCPBStub().deleteConfigByParam(objectGuid, tableTypes);
	}

	@Override public String getClassFieldOfG(FoundationObject instance, List<String> fieldNameList, Date ruleTime) throws ServiceRequestException
	{
		return this.getCPBStub().getClassFieldOfG(instance, fieldNameList, ruleTime);
	}

	@Override public void saveOrderPara(FoundationObject contract, ObjectGuid orderObjectGuid, String gNumber, String lNumbers, String inputVariables)
			throws ServiceRequestException
	{
		this.getOrderConfigureStub().saveOrderParameter(contract, orderObjectGuid, gNumber, lNumbers, inputVariables);
	}

}
