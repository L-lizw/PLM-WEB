package dyna.app.service.brs.dcr;

import dyna.app.service.BusinessRuleService;
import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.bean.data.checkrule.CheckRule;
import dyna.common.bean.data.checkrule.ClassConditionData;
import dyna.common.bean.data.checkrule.ClassConditionDetailData;
import dyna.common.bean.data.checkrule.End2CheckRule;
import dyna.common.conf.ServiceDefinition;
import dyna.common.dto.wf.ProcAttach;
import dyna.common.exception.ServiceNotFoundException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.systemenum.RuleTypeEnum;
import dyna.net.service.brs.*;
import dyna.net.service.das.MSRM;
import dyna.net.service.data.SystemDataService;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Getter(AccessLevel.PROTECTED)
@Service
public class DCRImpl extends BusinessRuleService implements DCR
{
	@DubboReference
	private SystemDataService       systemDataService;

	@Autowired
	private AAS aas;
	@Autowired
	private BOAS boas;
	@Autowired
	private BOMS boms;
	@Autowired
	private DSS dss;
	@Autowired
	private EMM emm;
	@Autowired
	private MSRM msrm;
	@Autowired
	private WFI wfi;

	@Autowired
	private DataCheckRuleQueryStub	checkRuleQueryStub	= null;
	@Autowired
	private DataCheckStub			checkStub			= null;
	@Autowired
	private DataCheckRuleSaveStub	checkRuleSaveStub	= null;

	@Override
	public void init(ServiceDefinition serviceDefinition)
	{
		super.init(serviceDefinition);
		checkRuleQueryStub.init();
		DataCheckRuleFactory.init(this);
	}

	public AAS getAas()
	{
		return this.aas;
	}

	public BOMS getBoms()
	{
		return this.boms;
	}

	public BOAS getBoas()
	{
		return  this.boas;
	}

	public DSS getDss()
	{
		return this.dss;
	}

	public EMM getEmm()
	{
		return this.emm;
	}

	public WFI getWfi()
	{
		return this.getWfi();
	}

	protected SystemDataService getSystemDataService()
	{
		return this.systemDataService;
	}

	/**
	 * @return the DataCheckRuleQueryStub
	 */
	public DataCheckRuleQueryStub getDataCheckRuleQueryStub()
	{
		return this.checkRuleQueryStub;
	}

	/**
	 * @return the DataCheckStub
	 */
	public DataCheckStub getDataCheckStub()
	{
		return this.checkStub;
	}

	/**
	 * @return the DataCheckRuleSaveStub
	 */
	public DataCheckRuleSaveStub getDataCheckRuleSaveStub()
	{
		return this.checkRuleSaveStub;
	}

	/**
	 * 取得检查规则列表
	 * 
	 * @param ruleTypeEnum
	 *            规则类型
	 * @param ruleName
	 *            RuleType:
	 *            <ol>
	 *            Relation:Relation模板名<br>
	 *            BOM:BOM模板名<br>
	 *            WF:流程名<br>
	 *            OBJECTFIELD:字段名
	 *            </ol>
	 * @param end1ClassName
	 *            RuleType:
	 *            <ol>
	 *            Relation/BOM:end1类名<br>
	 *            WF:节点名<br>
	 *            OBJECTFIELD:主对象类名
	 *            </ol>
	 * @param end2ClassName
	 *            RuleType:
	 *            <ol>
	 *            Relation/BOM:end2类名<br>
	 *            WF:流程附件类名<br>
	 *            OBJECTFIELD:主对象上的Object字段选择类名
	 *            </ol>
	 * @return
	 * @throws ServiceRequestException
	 */
	public List<CheckRule> loadDataCheckConditionList(RuleTypeEnum ruleTypeEnum, String ruleName, String end1ClassName, String end2ClassName) throws ServiceRequestException
	{
		return this.getDataCheckRuleQueryStub().loadDataCheckConditionList(ruleTypeEnum, ruleName, end1ClassName, end2ClassName);
	}

	public CheckRule getCheckRuleByGuid(String ruleGuid) throws ServiceRequestException
	{
		return this.getDataCheckRuleQueryStub().getCheckRuleByGuid(ruleGuid);
	}

	public CheckRule getCheckRuleById(String ruleId) throws ServiceRequestException
	{
		return this.getDataCheckRuleQueryStub().getCheckRuleById(ruleId);
	}

	@Override
	public boolean check(ObjectGuid end1ObjectGuid, List<ObjectGuid> end2ObjectGuidList, String ruleName, RuleTypeEnum ruleType) throws ServiceRequestException
	{
		return this.getDataCheckStub().check(end1ObjectGuid, end2ObjectGuidList, ruleName, ruleType);
	}

	@Override
	public boolean check(String procrtGuid, String wfName, String actrtName, List<ProcAttach> attachList) throws ServiceRequestException
	{
		return this.getDataCheckStub().check(procrtGuid, wfName, actrtName, attachList);
	}

	@Override
	public boolean check(FoundationObject foundationObject) throws ServiceRequestException
	{
		return this.getDataCheckStub().check(foundationObject);
	}

	@Override
	public boolean check(String serviceTemplateName, FoundationObject foundationObject) throws ServiceRequestException
	{
		return this.getDataCheckStub().check(serviceTemplateName, foundationObject);
	}

	@Override
	public CheckRule saveRule(CheckRule rule) throws ServiceRequestException
	{
		return this.getDataCheckRuleSaveStub().saveRule(rule);
	}

	@Override
	public void deleteRule(CheckRule rule) throws ServiceRequestException
	{
		this.getDataCheckRuleSaveStub().deleteRule(rule);
	}

	@Override
	public ClassConditionData saveClassCondition(ClassConditionData classCondition, CheckRule rule, boolean end1) throws ServiceRequestException
	{
		return this.getDataCheckRuleSaveStub().saveClassCondition(classCondition, rule, end1);
	}

	@Override
	public void deleteClassCondition(ClassConditionData classCondition) throws ServiceRequestException
	{
		this.getDataCheckRuleSaveStub().deleteClassCondition(classCondition);
	}

	@Override
	public void deleteClassCondition(ClassConditionData classCondition, CheckRule rule) throws ServiceRequestException
	{
		this.getDataCheckRuleSaveStub().deleteClassCondition(classCondition, rule);
	}

	@Override
	public void deleteClassCondition(ClassConditionData classCondition, End2CheckRule rule) throws ServiceRequestException
	{
		this.getDataCheckRuleSaveStub().deleteClassCondition(classCondition, rule);
	}

	@Override
	public List<ClassConditionData> listClassConditionData() throws ServiceRequestException
	{
		return this.getDataCheckRuleQueryStub().listClassConditionData();
	}

	@Override
	public ClassConditionData getClassConditionData(String guid) throws ServiceRequestException
	{
		return this.getDataCheckRuleQueryStub().getClassConditionData(guid);
	}

	@Override
	public List<CheckRule> listDataCheckRule() throws ServiceRequestException
	{
		return this.getDataCheckRuleQueryStub().listDataCheckRule();
	}

	public List<CheckRule> listDataDoCheckRule() throws ServiceRequestException
	{
		return this.getDataCheckRuleQueryStub().listDataDoCheckRule();
	}

	@Override
	public List<CheckRule> listDataCheckRuleByType(RuleTypeEnum ruleType) throws ServiceRequestException
	{
		return this.getDataCheckRuleQueryStub().listDataCheckRuleByType(ruleType);
	}

	public List<CheckRule> listDataCheckRuleByType(RuleTypeEnum ruleType, boolean enabledDirectly) throws ServiceRequestException
	{
		return this.getDataCheckRuleQueryStub().listDataCheckRuleByType(ruleType, enabledDirectly);
	}

	@Override
	public End2CheckRule saveEnd2CheckRule(End2CheckRule rule) throws ServiceRequestException
	{
		return this.getDataCheckRuleSaveStub().saveEnd2CheckRule(rule);
	}

	@Override
	public void deleteEnd2CheckRule(String guid) throws ServiceRequestException
	{
		this.getDataCheckRuleSaveStub().deleteEnd2CheckRule(guid);
	}

	@Override
	public void deleteEnd2CheckRule(End2CheckRule rule) throws ServiceRequestException
	{
		this.getDataCheckRuleSaveStub().deleteEnd2CheckRule(rule);
	}

	@Override
	public List<End2CheckRule> listEnd2CheckRule(String masterGuid) throws ServiceRequestException
	{
		return this.getDataCheckRuleQueryStub().listEnd2CheckRule(masterGuid);
	}

	@Override
	public String getFieldValue(ClassConditionDetailData classConditionDetailData) throws ServiceRequestException
	{
		return this.getDataCheckRuleQueryStub().getConditionVal(classConditionDetailData.getClassName(), classConditionDetailData.getFieldName(),
				classConditionDetailData.getValue());
	}
}
