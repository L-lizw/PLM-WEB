package dyna.app.service.brs.erpi.dataExport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;

import dyna.app.service.brs.boas.BOASImpl;
import dyna.app.service.brs.bom.BOMSImpl;
import dyna.app.service.brs.emm.ClassStub;
import dyna.app.service.brs.erpi.ERPIImpl;
import dyna.app.service.brs.erpi.ERPTransferStub;
import dyna.app.service.helper.Constants;
import dyna.common.SearchCondition;
import dyna.common.SearchConditionFactory;
import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.bean.data.StructureObject;
import dyna.common.bean.data.SystemObject;
import dyna.common.bean.data.configparamter.ConfigParameterConstants;
import dyna.common.bean.data.foundation.BOMView;
import dyna.common.bean.data.foundation.ViewObject;
import dyna.common.bean.data.structure.BOMStructure;
import dyna.common.bean.erp.ERPFieldMapping;
import dyna.common.bean.erp.ERPFieldMappingTypeEnum;
import dyna.common.bean.erp.ERPParameter;
import dyna.common.bean.erp.ERPRestriction;
import dyna.common.bean.erp.ERPRestrictionOperatorEnum;
import dyna.common.dto.DataRule;
import dyna.common.dto.aas.Group;
import dyna.common.dto.aas.User;
import dyna.common.dto.erp.ERPServiceConfig;
import dyna.common.dto.erp.ERPTransferLog;
import dyna.common.dto.erp.tmptab.ERPtempData;
import dyna.common.dto.model.cls.ClassField;
import dyna.common.dto.model.cls.ClassInfo;
import dyna.common.dto.model.code.CodeItemInfo;
import dyna.common.dto.model.code.CodeObjectInfo;
import dyna.common.dto.model.lf.LifecyclePhaseInfo;
import dyna.common.dto.model.ui.UIObjectInfo;
import dyna.common.dto.template.bom.BOMTemplateInfo;
import dyna.common.exception.ServiceRequestException;
import dyna.common.systemenum.ERPE10OperationEnum;
import dyna.common.systemenum.FieldTypeEnum;
import dyna.common.systemenum.JobStatus;
import dyna.common.systemenum.ModelInterfaceEnum;
import dyna.common.systemenum.ReplaceRangeEnum;
import dyna.common.systemenum.ReplaceTypeEnum;
import dyna.common.systemenum.SystemClassFieldEnum;
import dyna.common.systemenum.SystemStatusEnum;
import dyna.common.systemenum.UITypeEnum;
import dyna.common.util.CharUtils;
import dyna.common.util.DateFormat;
import dyna.common.util.ReplaceSubstituteConstants;
import dyna.common.util.SetUtils;
import dyna.common.util.StringUtils;

/**
 * ????????????????????????????????????PLM??????????????????????????????XML??????
 * <p>
 * ?????????????????????????????????????????????SingleData????????????RecordeSet,????????????1???????????????????????????
 * <p>
 * ?????????XML?????????????????????ERP????????????????????????????????????????????????????????????
 * 
 * @author chega
 * 
 * @param <T>
 *            ??????ERP???????????????OperationEnum
 */
public abstract class IntegrateERP<T extends Enum<?>>
{
	/**
	 * Map??????????????????
	 */
	private final int														initialCapacity				= 150;
	/**
	 * ?????????Item???????????????????????????PLM????????????????????????Item
	 */
	public static final String												ERP_ITEM					= "item";
	/**
	 * ????????????
	 */
	public static final String												ERP_CF						= "classification";
	/**
	 * BOM??????
	 */
	public static final String												ERP_BOM						= "bom";
	/**
	 * ??????????????????3?????????????????? LocalR & ???????????? LocalS & ???????????? GlobalS
	 */
	public static final String												ERP_REPLACESUBSTITUTE		= "replacesubstitute";
	/**
	 * ?????????
	 */
	public static final String												ERP_AVL						= "avl";
	/**
	 * ????????????
	 */
	public static final String												ERP_MA						= "ma";
	/**
	 * ??????
	 */
	public static final String												ERP_ORGANS					= "organs";
	/**
	 * ???????????????
	 */
	public static final String												ERP_ITEM_GROUP				= "itemGroup";
	/**
	 * ??????
	 */
	public static final String												ERP_BRAND					= "brand";
	/**
	 * ??????
	 */
	public static final String												ERP_CURRENCY				= "currency";
	/**
	 * ERP??????
	 */
	public static final String												ERP_EMPLOYEE				= "erpEmployee";
	/**
	 * ??????
	 */
	public static final String												ERP_UNIT					= "unit";
	/**
	 * ???????????????
	 */
	public static final String												ERP_ITEM_SUPPLIER			= "itemSupplier";
	/**
	 * ??????
	 */
	public static final String												ERP_PROCESS					= "process";
	/**
	 * ????????????
	 */
	public static final String												ERP_ROUTING					= "routing";
	/**
	 * ????????????
	 */
	public static final String												ERP_FIELDGROUP				= "fieldgroup";
	/**
	 * ????????????
	 */
	public static final String												ERP_CODEITEM				= "codeitem";

	/**
	 * ?????????????????????
	 */
	public String															xmlPath;

	/**
	 * ????????????
	 */
	public static final String												RESERVED_DESIGNATORS		= "bmt06";
	/**
	 * ????????????
	 */
	public static final String												RESERVED_DATE				= "${Date}";
	/**
	 * ????????????
	 */
	public static final String												RESERVED_JOBID				= "${JobId}";
	/**
	 * Datakey(jobId+???????????????)
	 * 
	 * @see #RESERVED_JOBID
	 */
	public static final String												RESERVED_DATAKEY			= "${DataKey}";
	public static final String												RESERVED_XMLPACKAGENO		= "${XMLPackageNo}";
	public static final String												RESERVED_EMPTY				= "${Empty}";
	/**
	 * ?????????????????????(??????<b>saveTransferStatus</b>?????????????????????false????????????????????????N
	 */
	public static final String												RESERVED_HASSENT			= "${HasSent}";
	/**
	 * PLM??????UserId,???job????????????
	 */
	public static final String												RESERVED_USERID				= "${UserId}";
	/**
	 * ERP??????UserId,????????????xml????????????ERP??????
	 */
	public static final String												RESERVED_ERP_USERID			= "${ERPUserId}";
	/**
	 * ?????????
	 */
	public static final String												RESERVED_FACTORY			= "${Factory}";
	/**
	 * ?????????????????????
	 */
	public static final String												DELIMITER_DESIGNATOR		= ",";
	protected String														defaultDateFormat			= "yyyy-MM-dd HH:mm:ss";
	/**
	 * ?????????category??? item, ?????????????????? RecordSet1 RecordSet2...
	 */
	protected Map<String, List<RecordSet>>									dataMap;

	protected List<T>														operationList;
	/**
	 * ??????Map??????????????????????????????Map???key??????????????????Map???key??????catory??????item. <br/>
	 */
	protected Map<String, Map<String, Map<String, List<ERPFieldMapping>>>>	PLMColumnMap;
	/**
	 * ??????Map????????????category??? ??????Map??????????????????id???name???
	 */
	protected Map<String, Map<String, String>>								tableMap;
	protected ERPTransferStub<T>											stub;
	private ERPFieldComparator												fieldComparator;
	private List<ERPRestriction>											restrictionList;
	private final List<InvalidDataException>								dataErrorList				= new LinkedList<InvalidDataException>();
	private final List<String>												dataWarnList				= new LinkedList<String>();
	/**
	 * ?????????????????????????????????erp_transfer_log???????????? <br/>
	 * ???????????????????????????????????????objectguid?????????BOM??????????????????bomview???objectguid <br/>
	 * ???????????????item???bom. <br/>
	 * ???dataMap???????????????????????????????????????????????????????????????????????????tobeExportedGuidList??????????????????????????? <br/>
	 * ?????????category
	 */
	protected Map<String, LinkedList<ObjectGuid>>							tobeExportedGuidMap;
	private final Map<String, ERPParameter>									parameterMap				= new HashMap<String, ERPParameter>();
	private List<String>													categoryList;
	protected BOMView														bomView;

	/**
	 * ????????????
	 */
	protected Date															CURRENT_DATE				= null;
	protected Document														doc;
	/**
	 * ??????????????????,??????????????????????????????/?????????????????????.Job?????????????????????????????????<br/>
	 * key???ObjectGuid???toString()
	 */
	private Map<String, SystemObject>										itemCache;

	/**
	 * ????????????
	 * ????????????????????????????????????????????????(?????????????????????Job????????????????????????DB??????????????????)<br/>
	 * ??????map???key???category, ??????map???key???ObjectGuid???toString??? value??????????????????????????????1. ??????Map???????????????????????????O(1)???
	 */
	private Map<String, Map<String, Integer>>								uniqueCache;
	/**
	 * ?????????????????????
	 * 
	 * @see uniqueCache
	 */
	private Map<String, Map<String, Integer>>								duplicatedCache;
	/**
	 * ???????????????????????????ID
	 * key??????category
	 */
	private Map<String, List<String>>										uniqueItemIDMap;
	/**
	 * ?????????????????????????????????ID
	 * key??????category
	 */
	private Map<String, List<String>>										duplicatedItemIDMap;

	private ERPIImpl														stubService					= null;
	/**
	 * ??????Map??????????????????????????????Map???key??????catory(????????????????????????2???operation). <br/>
	 */
	protected Map<String, Map<String, List<ERPFieldMapping>>>				PLMCFColumnMap				= null;
	/**
	 * ???????????????????????????
	 */
	protected Map<String, Map<String, String>>								itemCFInfoMap				= new LinkedHashMap<String, Map<String, String>>();

	/**
	 * ??????end1????????????BOM??????????????????????????????????????????????????????ERP?????????????????????BOM??????
	 */
	protected boolean														isPrintEmptyBOMXML			= false;

	/**
	 * ERP????????????????????????????????????
	 */
	protected ERPFieldMappingContainer										erpFieldMappingContainer	= new ERPFieldMappingContainer();

	public IntegrateERP(ERPTransferStub<T> stub, Document doc)
	{
		this.stub = stub;
		this.doc = doc;
	}

	/**
	 * ?????????????????????????????????????????????xml??????????????????????????????????????????????????????????????????
	 * 
	 * @throws Exception
	 */
	protected void init() throws Exception
	{
		this.dataMap = new HashMap<String, List<RecordSet>>(this.initialCapacity);
		this.itemCache = new HashMap<String, SystemObject>(this.initialCapacity);
		this.uniqueCache = new HashMap<String, Map<String, Integer>>(this.initialCapacity);
		this.duplicatedCache = new HashMap<String, Map<String, Integer>>();
		this.uniqueItemIDMap = new HashMap<String, List<String>>(this.initialCapacity);
		this.duplicatedItemIDMap = new HashMap<String, List<String>>();
		this.PLMColumnMap = new HashMap<String, Map<String, Map<String, List<ERPFieldMapping>>>>();
		this.tableMap = new HashMap<String, Map<String, String>>();
		this.tobeExportedGuidMap = new HashMap<String, LinkedList<ObjectGuid>>(this.initialCapacity);
		this.categoryList = this.getAllCategory();
		this.initParameter();
		this.fieldComparator = new ERPFieldComparator();
		this.restrictionList = this.getRestrictionList();
		this.PLMCFColumnMap = new HashMap<String, Map<String, List<ERPFieldMapping>>>();
		this.stubService = stub.getStubService();
		this.operationList = new ArrayList<T>();
		CURRENT_DATE = new Date();
	}

	/**
	 * 
	 * @param end1Obj
	 *            null?????????????????????????????????????????????????????????ObjectGuid,??????end1Obj??????null????????????end1Obj???ObjectGuid,<br/>
	 *            ??????????????????????????????????????????BOM xml????????????BOMStructure?????????????????????end1Object???objectGuid?????????uniqueCache??????key?????? <br/>
	 *            ??????????????????????????????????????????????????????BOM xml???
	 * @return
	 */
	protected ObjectGuid getMockObjectGuid(FoundationObject end1Obj)
	{
		if (end1Obj == null)
		{
			return new ObjectGuid(String.valueOf(System.currentTimeMillis()), null, String.valueOf(System.currentTimeMillis()), null);
		}
		else
		{
			return end1Obj.getObjectGuid();
		}
	}

	/**
	 * ??????<b>objGuid</b>????????????????????????ObjectGuid.<br/>
	 * 
	 * @param objGuid
	 * @return
	 */
	protected boolean isMockObjectGuid(ObjectGuid objGuid)
	{
		if (objGuid.getClassGuid().matches("\\d+") && objGuid.getClassName() == null && objGuid.getClassGuid().matches("\\d+") && objGuid.getCommitFolderGuid() == null)
		{
			return true;
		}
		return false;
	}

	/**
	 * ??????????????????????????????ObjectGuid
	 * 
	 * @param str
	 * @return
	 */
	protected ObjectGuid parseObjectGuidFromString(String str)
	{
		String[] array = str.split("\\$", -1);
		ObjectGuid g = new ObjectGuid();
		g.setClassGuid(array[0]);
		g.setClassName(array[1]);
		g.setGuid(array[2]);
		g.setMasterGuid(array[3]);
		g.setIsMaster("true".equalsIgnoreCase(array[4]));
		g.setBizObjectGuid(array[5]);
		g.setCommitFolderGuid(array[6]);
		return g;
	}

	/**
	 * ???????????????????????????????????????????????????
	 * 
	 * @return
	 */
	protected boolean isTraverseAllInstanceIgnoringError()
	{
		return "true".equalsIgnoreCase(this.getParameter(null).getParamMap().get("traverseAllInstanceIgnoringError"));
	}

	/**
	 * ???????????????????????????????????????????????????false???${HasSent}???false
	 * 
	 * @return
	 */
	public boolean isSaveTransferStatus()
	{
		return "true".equalsIgnoreCase(this.getParameter(null).getParamMap().get("saveTransferStatus"));
	}

	/**
	 * ?????????xml?????????????????????BOM??????BOM???????????????BOM????????????????????????BOM????????????</br>
	 * ??????WF???SM???????????????BOM???????????????????????????????????????false
	 * 
	 * @return
	 */
	protected boolean isContinEmptyBOMXML()
	{
		return "true".equalsIgnoreCase(this.getParameter(null).getParamMap().get("containEmptyBOMXML"));
	}

	/**
	 * ??????????????????????????????BOM
	 * 
	 * @param obj
	 * @return
	 * @throws ServiceRequestException
	 */
	protected boolean hasBOM(FoundationObject obj) throws ServiceRequestException
	{
		CodeItemInfo item = null;
		if (obj.get(Constants.FIELD_UHASBOM) != null)
		{
			item = this.stub.getStubService().getEMM().getCodeItem((String) obj.get(Constants.FIELD_UHASBOM));
		}
		return item != null && "D".equalsIgnoreCase(item.getCode());
	}

	/**
	 * ??????BOM???????????????table name
	 * 
	 * @return
	 */
	protected abstract String getBOMHeaderTableName();

	/**
	 * ????????????????????????BOM???????????????????????????????????????????????????BOM??????????????????BOM?????????BOMView???null???????????????getFieldFromMap()????????????????????????????????????????????? <br/>
	 * ????????????PLMColumnMap??????????????????????????????????????????????????????category???bom????????????class?????????name?????????
	 * 
	 * @return
	 */
	protected String getArbitraryBOMClassName()
	{
		String BOMStructureClassName = null;
		if (this.PLMColumnMap.get(ERP_BOM) == null || this.PLMColumnMap.get(ERP_BOM).isEmpty())
		{
			Element bomElement = this.doc.getRootElement().getChild("category").getChild("bom");
			if (bomElement == null || bomElement.getChild("class") == null)
			{
				throw new IllegalArgumentException("can't find category configuration for bom");
			}
			BOMStructureClassName = bomElement.getChild("class").getAttributeValue("name");
		}
		else
		{
			BOMStructureClassName = this.PLMColumnMap.get(ERP_BOM).keySet().iterator().next();
		}
		if (BOMStructureClassName == null)
		{
			throw new IllegalArgumentException("can't find a class name for BOM configuration");
		}
		return BOMStructureClassName;
	}

	/**
	 * ????????????????????????
	 * 
	 * @return
	 */
	public boolean isSaveTempFile()
	{
		return "true".equalsIgnoreCase(this.getParameter(null).getParamMap().get("saveTempFile"));
	}

	/**
	 * ?????????????????????????????????????????????XML?????????
	 * ????????????????????????ID;???BOM??????????????????ID;??????????????????????????????ID;????????????????????????end1???ID(end1???ApproveItem????????????????????????)
	 */
	public boolean isPrintData4Comparison()
	{
		return "true".equalsIgnoreCase(this.getParameter(null).getParamMap().get("printData4Comparison"));
	}

	/**
	 * ??????category?????????????????????category?????????restriction
	 * 
	 * @param category
	 * @return
	 */
	private List<ERPRestriction> getRestrictionByCategory(String category)
	{
		List<ERPRestriction> resList = new ArrayList<ERPRestriction>();
		for (ERPRestriction res : this.restrictionList)
		{
			if (res.getCategoryList().contains(category))
			{
				resList.add(res);
			}
		}
		return resList;
	}

	/**
	 * ??????xml???????????????????????????restriction
	 * 
	 * @return
	 */
	private List<ERPRestriction> getRestrictionList()
	{
		List<ERPRestriction> list = Collections.emptyList();
		Element parentEle = this.doc.getRootElement().getChild("restrictions");
		if (parentEle == null)
		{
			return Collections.emptyList();
		}
		List<Element> restrictionListEle = parentEle.getChildren("restriction");
		list = new ArrayList<ERPRestriction>();
		Iterator<Element> restriction_ite = restrictionListEle.iterator();
		while (restriction_ite.hasNext())
		{
			Element ele = restriction_ite.next();
			list.add(new ERPRestriction(ele.getAttributeValue("field").trim(), ele.getAttributeValue("value").trim(), ERPRestrictionOperatorEnum.getOperatorBySign(ele
					.getAttributeValue("operator").trim()), ele.getAttributeValue("category").trim(), "true".equalsIgnoreCase(ele.getAttributeValue("skipOnFail"))));
		}
		return list;
	}

	public void getAllMergeDataWithChildren(FoundationObject foundationObject, BOMStructure bomStructure) throws Exception
	{
		this.generateAllData(foundationObject, bomStructure, false, null, 0, null);
		this.afterProcessDataList();
	}

	public void beforeProcessDataList4Merge()
	{
		this.beforeProcessDataList();
	}

	/**
	 * @see #generateAllData(FoundationObject, BOMStructure)
	 */
	public void getAllDataWithChildren(FoundationObject foundationObject, BOMStructure bomStructure) throws Exception
	{
		this.beforeProcessDataList();
		this.generateAllData(foundationObject, bomStructure, false, null, 0, null);
		this.afterProcessDataList();
	}

	/**
	 * ?????????????????????<br/>
	 * BOM?????????????????????????????? <br/>
	 * ??????????????????????????????????????????????????????????????????<b>?????????????????????????????????????????????InvalidDataException???????????????????????????</b>??? <br/>
	 * ????????????????????????
	 * 
	 * @param end1FoundationObj
	 * @param bomStructure
	 * @param templateName
	 * @param schema
	 * @throws ServiceRequestException
	 * @throws Exception
	 */
	protected void generateAllData(FoundationObject end1FoundationObj, BOMStructure bomStructure, boolean flag, String origGNumber, int level, DataRule dataRule) throws Exception
	{
		String className = end1FoundationObj.getObjectGuid().getClassName();
		List<FoundationObject> end2FoundationObjList = new ArrayList<FoundationObject>();
		List<BOMStructure> BOMStructureList = Collections.emptyList();
		String newOrigGnumber = origGNumber;
		String structureClass = null;
		BOMTemplateInfo bomTemplate = null;

		ClassInfo classInfo = this.stubService.getEMM().getClassByGuid(end1FoundationObj.getObjectGuid().getClassGuid());
		if (classInfo.hasInterface(ModelInterfaceEnum.INewProductOfConfigure)
				&& ConfigParameterConstants.CONFIG_PARAMETER_RESULTRELATION_TEMPLATE_NAME.equals(this.stub.templateName))
		{
			String partNumber = (String) end1FoundationObj.get(ConfigParameterConstants.PARTNUMBER);
			if (!StringUtils.isNullString(partNumber))
			{
				newOrigGnumber = partNumber;
			}

			BOMStructureList = this.stub.getStubService().getCPB().listBOM(end1FoundationObj, null, null, origGNumber);
			if (!SetUtils.isNullList(BOMStructureList))
			{
				for (int i = 0; i < BOMStructureList.size(); i++)
				{
					FoundationObject end2FoundationObject = (FoundationObject) this.getObject(BOMStructureList.get(i).getEnd2ObjectGuid());
					end2FoundationObjList.add(end2FoundationObject);
				}
			}
			else if (BOMStructureList == null)
			{
				BOMStructureList = new ArrayList<BOMStructure>();
			}
		}
		else
		{
			// ??????end1?????????????????????BOMView??????
			this.bomView = ((BOMSImpl) this.stub.getStubService().getBOMS()).getBomViewStub().getBOMViewByEND1(end1FoundationObj.getObjectGuid(), this.stub.templateName, false);
			if (this.bomView != null)
			{
				bomTemplate = this.stub.getStubService().getEMM().getBOMTemplateById((String) this.bomView.get(ViewObject.TEMPLATE_ID));
				if (bomTemplate != null)
				{
					structureClass = bomTemplate.getStructureClassName();
				}
				List<UIObjectInfo> uiObjectList = null;
				List<UIObjectInfo> uiObjectList1 = this.stub.getStubService().getEMM().listUIObjectInCurrentBizModel(structureClass, UITypeEnum.FORM, true);
				List<UIObjectInfo> uiObjectList2 = this.stub.getStubService().getEMM().listUIObjectInCurrentBizModel(structureClass, UITypeEnum.LIST, true);
				if (uiObjectList1 != null)
				{
					uiObjectList = uiObjectList1;
				}
				if (uiObjectList2 != null)
				{
					if (uiObjectList == null)
					{
						uiObjectList = uiObjectList2;
					}
					else
					{
						uiObjectList.addAll(uiObjectList2);
					}
				}
				SearchCondition searchCondition = SearchConditionFactory.createSearchConditionForBOMStructure(structureClass, uiObjectList);

				// ??????end1????????????BOMStructure
				BOMStructureList = ((BOMSImpl) this.stub.getStubService().getBOMS()).getBomStub().listBOM(this.bomView.getObjectGuid(), searchCondition, null, null);
				if (BOMStructureList == null)
				{
					BOMStructureList = Collections.emptyList();
				}
				for (int i = 0; i < BOMStructureList.size(); i++)
				{
					FoundationObject end2FoundationObject = (FoundationObject) this.getObject(BOMStructureList.get(i).getEnd2ObjectGuid());
					end2FoundationObjList.add(end2FoundationObject);
				}
			}
		}
		if (SetUtils.isNullList(this.operationList))
		{
			this.operationList = this.getOperationList(false);
		}
		for (int k = 0; k < this.operationList.size(); k++)
		{
			T operation = this.operationList.get(k);
			String category = this.getOperationCategory(operation);
			if (StringUtils.isNullString(category))
			{
				throw new IllegalArgumentException("no category attribute for " + this.operationList.get(k).name() + " in " + this.xmlPath);
			}
			try
			{
				if (ERP_ITEM.equals(category))
				{
					this.getItemData(end1FoundationObj, BOMStructureList, operation);
				}
				else if (ERP_BOM.equals(category))
				{
					this.getBOMData(end1FoundationObj, BOMStructureList, bomStructure, operation);
				}
				else if (ERP_REPLACESUBSTITUTE.equals(category))
				{
					this.getRSData(end1FoundationObj, BOMStructureList, end2FoundationObjList, bomTemplate, operation);
				}
				else if (ERP_MA.equals(category))
				{
					this.getMAData(end1FoundationObj, className, operation);
				}
			}
			catch (InvalidDataException e)
			{
				this.addDataError(e);
				if (!this.isTraverseAllInstanceIgnoringError())
				{
					return;
				}
			}
		}
		// ?????????BOM??????????????????????????????BOM?????????????????????
		if (this.stub.schema.isExpandBOM() && !SetUtils.isNullList(BOMStructureList))
		{
			for (int i = 0; i < end2FoundationObjList.size(); i++)
			{
				level++;
				this.generateAllData(end2FoundationObjList.get(i), BOMStructureList.get(i), flag, newOrigGnumber, level, dataRule);
			}
		}
	}

	/**
	 * ????????????????????????Schema??? ??????????????????BOM??????????????????????????????????????????????????????????????????BOM??????????????????ERP???????????????
	 * 
	 * @param end1FoundationObj
	 * @param BOMStructureList
	 * @param operation
	 * @throws ServiceRequestException
	 */
	public void getItemData(FoundationObject end1FoundationObj, List<BOMStructure> BOMStructureList, T operation) throws ServiceRequestException
	{
		RecordSet set = this.getEachItemData(end1FoundationObj, operation);
		this.addToDataMap(ERP_ITEM, set);
		if (this.stub.schema.getOperationList().contains(this.getCreateBOMOperationId()) && !this.stub.schema.isExpandBOM() && !SetUtils.isNullList(BOMStructureList))
		{
			if (set != null) // O23181
			{
				for (int i = 0; i < BOMStructureList.size(); i++)
				{
					ClassStub.decorateObjectGuid(BOMStructureList.get(i).getEnd2ObjectGuid(), this.stub.getStubService());
					this.addToDataMap(ERP_ITEM, this.getEachItemData((FoundationObject) this.getObject(BOMStructureList.get(i).getEnd2ObjectGuid()), operation));
				}
			}
		}
	}

	/**
	 * ???BOM??????
	 * 
	 * @param end1Obj
	 * @param BOMStructureList
	 * @param parentBOMStructure
	 * @param operation
	 * @throws ServiceRequestException
	 */
	protected void getBOMData(FoundationObject end1Obj, List<BOMStructure> BOMStructureList, BOMStructure parentBOMStructure, T operation) throws ServiceRequestException
	{
		// add2StampMap(ERP_BOM, end1Obj);
		this.addToDataMap(ERP_BOM, this.getEachBOMData(end1Obj, BOMStructureList, parentBOMStructure, operation));
	}

	/**
	 * ?????????????????????end1??????end2??????????????????<br/>
	 * ??????4????????????????????????????????????????????????????????????????????????
	 * 
	 * @param end1FoundationObj
	 * @param bomStructureList
	 * @param end2FoundationObjList
	 * @param bomTemplate
	 * @param operation
	 * @throws ServiceRequestException
	 */
	protected void getRSData(FoundationObject end1FoundationObj, List<BOMStructure> bomStructureList, List<FoundationObject> end2FoundationObjList, BOMTemplateInfo bomTemplate,
			T operation) throws ServiceRequestException
	{
		// ????????????3?????????
		// ????????????
		if (this.getOperationId(operation).equals(this.getCreateLocalSOperationId()))
		{
			for (BOMStructure bomStructure : bomStructureList)
			{
				List<FoundationObject> substituteList = this.stub
						.getStubService()
						.getBRM()
						.listReplaceDataByRang(end1FoundationObj.getObjectGuid(), bomStructure.getEnd2ObjectGuid(), null ,ReplaceRangeEnum.PART, ReplaceTypeEnum.TIDAI,
								bomTemplate.getName(), bomStructure.getBOMKey(), true, true);
				if (substituteList != null && substituteList.size() != 0)
				{
					Set<String> tempSet = new HashSet<String>();
					for (int j = substituteList.size() - 1; j > -1; j--)
					{
						FoundationObject object = substituteList.get(j);
						if (object != null && object.get(ReplaceSubstituteConstants.EffectiveDate) != null)
						{
							String tempKey = object.get(ReplaceSubstituteConstants.MasterItem + "$MASTER") + "-" + object.get(ReplaceSubstituteConstants.ComponentItem + "$MASTER")
									+ "-" + object.get(ReplaceSubstituteConstants.RSItem + "$MASTER");
							if (tempSet.contains(tempKey))
							{
								substituteList.remove(j);
							}
							else
							{
								tempSet.add(tempKey);
							}
						}
					}

					for (int j = 0; j < substituteList.size(); j++)
					{
						FoundationObject object = substituteList.get(j);
						if (object != null && object.get(ReplaceSubstituteConstants.EffectiveDate) != null)
						{
							if (object.get(ReplaceSubstituteConstants.MasterItem) == null)
							{
								object.put(ReplaceSubstituteConstants.MasterItem, end1FoundationObj.getObjectGuid().getGuid());
								object.put(ReplaceSubstituteConstants.MasterItem + ReplaceSubstituteConstants.ClassGuid, end1FoundationObj.getObjectGuid().getClassGuid());
							}
							ClassStub.decorateObjectGuid(substituteList.get(j).getObjectGuid(), this.stub.getStubService());
							RecordSet eachLocalS = this.getEachLocalS(substituteList.get(j), operation);
							this.addToDataMap(ERP_REPLACESUBSTITUTE, eachLocalS);
						}
					}
				}
			}
		}
		// ????????????
		else if (this.getOperationId(operation).equals(this.getCreateLocalROperationId()))
		{
			for (BOMStructure bomStructure : bomStructureList)
			{
				List<FoundationObject> substituteList = this.stub
						.getStubService()
						.getBRM()
						.listReplaceDataByRang(end1FoundationObj.getObjectGuid(), bomStructure.getEnd2ObjectGuid(), null ,ReplaceRangeEnum.PART, ReplaceTypeEnum.QUDAI,
								bomTemplate.getName(), bomStructure.getBOMKey(), true, true );
				if (substituteList != null && substituteList.size() != 0)
				{
					Set<String> tempSet = new HashSet<String>();
					for (int j = substituteList.size() - 1; j > -1; j--)
					{
						FoundationObject object = substituteList.get(j);
						if (object != null && object.get(ReplaceSubstituteConstants.EffectiveDate) != null)
						{
							String tempKey = object.get(ReplaceSubstituteConstants.MasterItem + "$MASTER") + "-" + object.get(ReplaceSubstituteConstants.ComponentItem + "$MASTER")
									+ "-" + object.get(ReplaceSubstituteConstants.RSItem + "$MASTER");
							if (tempSet.contains(tempKey))
							{
								substituteList.remove(j);
							}
							else
							{
								tempSet.add(tempKey);
							}
						}
					}

					for (int j = 0; j < substituteList.size(); j++)
					{
						FoundationObject object = substituteList.get(j);
						if (object != null && object.get(ReplaceSubstituteConstants.EffectiveDate) != null)
						{
							if (object.get(ReplaceSubstituteConstants.MasterItem) == null)
							{
								object.put(ReplaceSubstituteConstants.MasterItem, end1FoundationObj.getObjectGuid().getGuid());
								object.put(ReplaceSubstituteConstants.MasterItem + ReplaceSubstituteConstants.ClassGuid, end1FoundationObj.getObjectGuid().getClassGuid());
							}
							ClassStub.decorateObjectGuid(substituteList.get(j).getObjectGuid(), this.stub.getStubService());
							RecordSet eachLocalR = this.getEachLocalR(substituteList.get(j), operation);
							this.addToDataMap(ERP_REPLACESUBSTITUTE, eachLocalR);
						}
					}
				}
			}
		}
		// ????????????
		else if (this.getOperationId(operation).equals(this.getCreateGlobalSOperationId()))
		{
			// for (FoundationObject end2 : end2FoundationObjList)
			// {
			List<FoundationObject> substituteList = this.stub.getStubService().getBRM()
					.listReplaceDataByRang(null, end1FoundationObj.getObjectGuid(), null, ReplaceRangeEnum.GLOBAL, ReplaceTypeEnum.TIDAI, null, null, true, true );
			if (substituteList != null && substituteList.size() != 0)
			{
				Set<String> tempSet = new HashSet<String>();
				for (int j = substituteList.size() - 1; j > -1; j--)
				{
					FoundationObject object = substituteList.get(j);
					if (object != null && object.get(ReplaceSubstituteConstants.EffectiveDate) != null)
					{
						String tempKey = object.get(ReplaceSubstituteConstants.MasterItem + "$MASTER") + "-" + object.get(ReplaceSubstituteConstants.ComponentItem + "$MASTER")
								+ "-" + object.get(ReplaceSubstituteConstants.RSItem + "$MASTER");
						if (tempSet.contains(tempKey))
						{
							substituteList.remove(j);
						}
						else
						{
							tempSet.add(tempKey);
						}
					}
				}

				for (int j = 0; j < substituteList.size(); j++)
				{
					FoundationObject object = substituteList.get(j);
					if (object != null && object.get(ReplaceSubstituteConstants.EffectiveDate) != null)
					{
						ClassStub.decorateObjectGuid(substituteList.get(j).getObjectGuid(), this.stub.getStubService());
						RecordSet eachGlobalS = this.getEachGlobalS(substituteList.get(j), operation);
						this.addToDataMap(ERP_REPLACESUBSTITUTE, eachGlobalS);
					}
				}
			}
		}
	}

	/**
	 * ?????????????????????
	 */
	protected void getMAData(FoundationObject end1FoundationObj, String className, T operation) throws ServiceRequestException
	{
		String maTemplate = this.getMATemplateName();
		List<StructureObject> relationList = this.stub.getStubService().getBOAS().listObjectOfRelation(end1FoundationObj.getObjectGuid(), maTemplate, null, null, null);
		if (relationList != null && relationList.size() != 0)
		{
			// add2StampMap(ERP_MA, end1FoundationObj);
			for (int i = 0; i < relationList.size(); i++)
			{
				ClassStub.decorateObjectGuid(relationList.get(i).getObjectGuid(), this.stub.getStubService());
				this.addToDataMap(ERP_MA, this.getEachMA(relationList.get(i), ERP_MA, operation));
			}
		}
	}

	/**
	 * ??????xml??????????????????????????????<br/>
	 * 
	 */
	protected void initParameter()
	{
		if (this.doc.getRootElement().getChild("parameters") != null)
		{
			this.addParameter(null, this.getParameterByElement(this.doc.getRootElement().getChild("parameters")));
		}

		for (String category : this.categoryList)
		{
			Element categoryEle = this.doc.getRootElement().getChild("category").getChild(category);
			if (categoryEle.getChild("parameters") != null)
			{
				this.addParameter(category, this.getParameterByElement(categoryEle.getChild("parameters")));
			}
		}
	}

	/**
	 * ??????DOM??????Element????????????????????????
	 * 
	 * @param parametersEle
	 * @return
	 */
	private ERPParameter getParameterByElement(Element parametersEle)
	{
		Iterator<Element> it = parametersEle.getChildren().iterator();
		ERPParameter param = new ERPParameter();
		Element ele = null;
		while (it.hasNext())
		{
			ele = it.next();
			if (ele.getName().equals("param"))
			{
				param.getParamMap().put(ele.getAttributeValue("name"), ele.getAttributeValue("value"));
			}
		}
		return param;
	}

	/**
	 * ??????????????????, if <b>category</b> is null, means parameters directly contained
	 * in root.parameters node
	 * 
	 * @param category
	 * @return
	 */
	public ERPParameter getParameter(String category)
	{
		return this.parameterMap.get(category);
	}

	/**
	 * if <b>category</b> is null, means <b>param</b> is the root node
	 * parameters
	 * 
	 * @param category
	 * @param param
	 */
	protected void addParameter(String category, ERPParameter param)
	{
		this.parameterMap.put(category, param);
	}

	/**
	 * ??????xml?????????????????????category?????????????????????category,????????????
	 * 
	 * @return
	 */
	protected List<String> getAllCategory()
	{
		Element categoryEle = this.doc.getRootElement().getChild("category");
		if (categoryEle == null)
		{
			throw new IllegalArgumentException("no category config found in: " + this.xmlPath);
		}
		if (categoryEle.getChildren().size() == 0)
		{
			return Collections.emptyList();
		}
		List<String> list = new ArrayList<String>();
		Iterator<Element> it = this.doc.getRootElement().getChild("category").getChildren().iterator();
		while (it.hasNext())
		{
			list.add(it.next().getName());
		}
		return list;
	}

	/**
	 * ????????????????????????????????????????????????
	 */
	protected void beforeProcessDataList()
	{
		this.dataMap.clear();
		this.operationList.clear();
	}

	/**
	 * ????????????????????????????????????????????????????????????????????????
	 * 
	 * @throws ServiceRequestException
	 */
	protected void afterProcessDataList() throws ServiceRequestException
	{
		this.operationList.clear();
		this.printComparisonFile();
	}

	/**
	 * ??????????????????????????????dataErrorList?????????
	 * 
	 * @param e
	 */
	protected void addDataError(InvalidDataException e)
	{
		this.dataErrorList.add(e);
	}

	/**
	 * ?????????????????????????????????
	 * 
	 * @return
	 */
	public List<InvalidDataException> getDataErrorList()
	{
		return this.dataErrorList;
	}

	/**
	 * ????????????????????????dataWarnList?????????
	 * 
	 * @param str
	 */
	protected void addDataWarn(String str)
	{
		this.dataWarnList.add(str);
	}

	/**
	 * ??????????????????
	 * 
	 * @return
	 */
	public List<String> getDataWarnList()
	{
		return this.dataWarnList;
	}

	public Map<String, LinkedList<ObjectGuid>> getExportedGuidMap()
	{
		return this.tobeExportedGuidMap;
	}

	/**
	 * ????????????????????????
	 * 
	 * @return
	 */
	public String printDataErrorList()
	{
		if (this.dataErrorList.isEmpty())
		{
			return "";
		}
		StringBuilder sb = new StringBuilder("[ERROR]" + ERPTransferStub.LINE_SEPERATOR);
		for (InvalidDataException e : this.dataErrorList)
		{
			sb.append(e.getMessage()).append(ERPTransferStub.LINE_SEPERATOR);
		}
		return sb.toString();
	}

	/**
	 * this method is not used at current stage, because there is no need to show <b>warn</b> info to end user.
	 * 
	 * @return
	 */
	public String printDataWarnList()
	{
		if (this.dataWarnList.isEmpty())
		{
			return "";
		}
		StringBuilder sb = new StringBuilder("[Warning]" + ERPTransferStub.LINE_SEPERATOR);
		for (String e : this.dataWarnList)
		{
			sb.append(e).append(ERPTransferStub.LINE_SEPERATOR);
		}
		return sb.toString();
	}

	/**
	 * ????????????????????????????????????
	 * 
	 * @throws ServiceRequestException
	 */
	public void printComparisonFile() throws ServiceRequestException
	{

		if (this.isPrintData4Comparison())
		{
			Set<String> categorySet = new HashSet<String>();
			Iterator<String> it = this.uniqueCache.keySet().iterator();
			while (it.hasNext())
			{
				String category = it.next();
				categorySet.add(category);
				if (this.uniqueItemIDMap.get(category) == null)
				{
					this.uniqueItemIDMap.put(category, new LinkedList<String>());
				}
				for (String objGuid : this.uniqueCache.get(category).keySet())
				{
					ObjectGuid g = this.parseObjectGuidFromString(objGuid);
					SystemObject fo = this.getObject(g);
					if (ERP_ITEM.equals(category))
					{
						this.uniqueItemIDMap.get(category).add((String) fo.get(SystemClassFieldEnum.ID.getName()));
					}
					else if (ERP_BOM.equals(category))
					{
						ObjectGuid objectGuid = new ObjectGuid();
						objectGuid.setClassGuid((String) fo.get("END1$CLASS"));
						objectGuid.setGuid((String) fo.get("END1"));
						this.uniqueItemIDMap.get(category).add((String) this.getObject(objectGuid).get(SystemClassFieldEnum.ID.getName()));
					}
					else if (ERP_REPLACESUBSTITUTE.equals(category))
					{
						ObjectGuid objectGuid = new ObjectGuid(fo.get("ComponentITEM$CLASS").toString(), null, fo.get("ComponentItem").toString(), null);
						this.uniqueItemIDMap.get(category).add((String) this.getObject(objectGuid).get(SystemClassFieldEnum.ID.getName()));
					}
					else if (ERP_AVL.equals(category))
					{
						this.uniqueItemIDMap.get(category).add((String) fo.get(SystemClassFieldEnum.ID.getName()));
					}
				}
			}

			it = this.duplicatedCache.keySet().iterator();
			while (it.hasNext())
			{
				String category = it.next();
				if (this.duplicatedItemIDMap.get(category) == null)
				{
					this.duplicatedItemIDMap.put(category, new LinkedList<String>());
				}
				for (String objGuid : this.duplicatedCache.get(category).keySet())
				{
					ObjectGuid g = this.parseObjectGuidFromString(objGuid);
					SystemObject fo = this.getObject(g);
					if (ERP_ITEM.equals(category))
					{
						this.duplicatedItemIDMap.get(category).add((String) fo.get(SystemClassFieldEnum.ID.getName()));
					}
					else if (ERP_BOM.equals(category))
					{
						ObjectGuid objectGuid = new ObjectGuid();
						objectGuid.setClassGuid((String) fo.get("END1$CLASS"));
						objectGuid.setGuid((String) fo.get("END1"));
						this.duplicatedItemIDMap.get(category).add((String) this.getObject(objectGuid).get(SystemClassFieldEnum.ID.getName()));
					}
					else if (ERP_REPLACESUBSTITUTE.equals(category))
					{
						ObjectGuid objectGuid = new ObjectGuid(fo.get("ComponentITEM$CLASS").toString(), null, fo.get("ComponentItem").toString(), null);
						this.duplicatedItemIDMap.get(category).add((String) this.getObject(objectGuid).get(SystemClassFieldEnum.ID.getName()));
					}
					else if (ERP_AVL.equals(category))
					{
						this.duplicatedItemIDMap.get(category).add((String) fo.get(SystemClassFieldEnum.ID.getName()));
					}
				}
			}

			StringBuilder sb = new StringBuilder();
			it = categorySet.iterator();
			while (it.hasNext())
			{
				String category = it.next();
				StringBuilder sb_unique = new StringBuilder("unique ID list(count ").append(this.uniqueItemIDMap.get(category).size()).append("):")
						.append(ERPTransferStub.LINE_SEPERATOR);
				for (String s : this.uniqueItemIDMap.get(category))
				{
					sb_unique.append(s).append(ERPTransferStub.LINE_SEPERATOR);
				}
				StringBuilder sb_duplicated = new StringBuilder();
				if (this.duplicatedItemIDMap.get(category) != null)
				{
					sb_duplicated.append("duplicated ID list(count ").append(this.duplicatedItemIDMap.get(category).size()).append("):").append(ERPTransferStub.LINE_SEPERATOR);
					for (String s : this.duplicatedItemIDMap.get(category))
					{
						sb_duplicated.append(s).append(ERPTransferStub.LINE_SEPERATOR);
					}
				}
				sb.append(sb_unique).append(ERPTransferStub.LINE_SEPERATOR).append(sb_duplicated).trimToSize();
				this.stub.saveComparisonFile(this.stub.jobId + "_" + category + ".txt", sb.toString());
				sb.delete(0, sb.length());
			}
		}
	}

	/**
	 * ???????????????recordset?????????dataMap??????????????????????????????<br/>
	 * ????????????true,???????????????false
	 * 
	 * @param category
	 * @param recordSet
	 * @return
	 */
	protected boolean addToDataMap(String category, RecordSet recordSet)
	{
		if (recordSet == null || recordSet.isEmpty())
		{
			return false;
		}
		if (this.dataMap.get(category) == null)
		{
			this.dataMap.put(category, new ArrayList<RecordSet>(this.initialCapacity));
		}
		if (recordSet.getObjectGuid() != null)
		{
			if (this.uniqueCache.get(category) == null)
			{
				this.uniqueCache.put(category, new HashMap<String, Integer>(this.initialCapacity));
			}
			if (this.uniqueCache.get(category).containsKey(recordSet.getObjectGuid().toString()))
			{
				if (this.duplicatedCache.get(category) == null)
				{
					this.duplicatedCache.put(category, new HashMap<String, Integer>(this.initialCapacity));
				}
				this.duplicatedCache.get(category).put(recordSet.getObjectGuid().toString(), 1);
				this.addDataWarn("duplicated data: " + recordSet);
				return false;
			}
			this.uniqueCache.get(category).put(recordSet.getObjectGuid().toString(), 1);
		}
		this.dataMap.get(category).add(recordSet);
		return true;
	}

	/**
	 * ?????????????????????ObjectGuid?????????tobeExportedGuidMap??????????????????erp_transfer_log??????
	 */
	public void fillExportGuidMap()
	{
		if (!this.isSaveTransferStatus())
		{
			return;
		}
		this.tobeExportedGuidMap.clear();
		Iterator<String> category_it = this.dataMap.keySet().iterator();
		while (category_it.hasNext())
		{
			String category = category_it.next();
			if (this.tobeExportedGuidMap.get(category) == null)
			{
				this.tobeExportedGuidMap.put(category, new LinkedList<ObjectGuid>());
			}
			Iterator<RecordSet> recordset_it = this.dataMap.get(category).iterator();
			while (recordset_it.hasNext())
			{
				RecordSet set = recordset_it.next();
				if (!this.isMockObjectGuid(set.getObjectGuid()))
				{
					this.tobeExportedGuidMap.get(category).add(set.getObjectGuid());
				}
			}
		}
	}

	/**
	 * ??????????????????????????????????????????????????? <br/>
	 * ?????????????????????skipOnFail=true?????????????????????????????????true???<br/>
	 * ?????????????????????skipOnFail=false????????????<br/>
	 * ????????????????????????getObjectMapValue()??????????????????getObjectMapValue()??????????????????category?????????table????????? <br/>
	 * ???????????????????????????category?????????????????????true?????????????????????table??????
	 * 
	 * @return
	 * @param object
	 *            ???????????????FoundationObject?????????????????????BOMView
	 * @param structObj
	 * @param category
	 * @throws ServiceRequestException
	 */
	protected boolean checkObjectReturningSkip(SystemObject object, String category, T operation) throws ServiceRequestException
	{
		boolean skip = false;
		if (object == null)
		{
			return skip;
		}
		List<ERPRestriction> resList = this.getRestrictionByCategory(category);
		for (ERPRestriction res : resList)
		{
			ERPFieldMapping field = new ERPFieldMapping();
			field.setPLMField(res.getField());
			String value = this.getFieldValue(object, field, category, operation);
			if (!res.validate(value))
			{
				if (res.isSkipOnFail())
				{// ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
					skip = true;
					continue;
				}
				String contents = "";
				if (res.getField().equals("ISCHECKOUT$"))
				{

					contents = this.stub.getStubService().getMSRM().getMSRString("ID_APP_ERP_ISCHECKOUT", this.stub.lang.toString());
					throw new InvalidDataException(this.getFoundationObjectDesp(object) + "???" + contents);
					// throw new InvalidDataException(this.getFoundationObjectDesp(object) + "???" + value +
					// " is invalid for " + res.toString() + ", and not allowed to skip");
				}
				if (res.getField().equals("STATUS$"))
				{
					Element parentEle = this.doc.getRootElement().getChild("restrictions");
					List<Element> restrictionListEle = parentEle.getChildren("restriction");
					Iterator<Element> restriction_ite = restrictionListEle.iterator();
					List<String> valueList = null;
					while (restriction_ite.hasNext())
					{
						Element ele = restriction_ite.next();
						if (ele.getAttributeValue("field").trim().equals("STATUS$"))
						{
							valueList = Arrays.asList(ele.getAttributeValue("value").trim());
							;
						}
					}
					String message = "";
					for (int i = 0; i < valueList.size(); i++)
					{
						String[] values = valueList.get(i).split(",");
						for (int j = 0; j < values.length; j++)
						{
							message = message + this.stub.getStubService().getMSRM().getMSRString(SystemStatusEnum.getStatusEnum(values[j]).getMsrId(), this.stub.lang.toString())
									+ ",";
						}
					}
					contents = this.stub.getStubService().getMSRM().getMSRString("ID_APP_ERP_STATUS", this.stub.lang.toString());
					contents = MessageFormat.format(contents, message.substring(0, message.length() - 1),
							this.stub.getStubService().getMSRM().getMSRString(SystemStatusEnum.getStatusEnum(value).getMsrId(), this.stub.lang.toString()));
					throw new InvalidDataException(this.getFoundationObjectDesp(object) + "???" + contents);
				}
				if (res.getField().equals("${HasSent}"))
				{
					contents = this.stub.getStubService().getMSRM().getMSRString("ID_APP_ERP_HASSENT", this.stub.lang.toString());
					throw new InvalidDataException(this.getFoundationObjectDesp(object) + "???" + contents);
				}
			}
		}
		if (skip)
		{
			// ?????????????????????????????????dataWarnList???
			this.addDataWarn(this.getFoundationObjectDesp(object) + " is skipped");
		}
		return skip;
	}

	/**
	 * ?????????????????????
	 * 
	 * @param dataValue
	 * @param field
	 * @return
	 */
	protected void checkFieldValue(String dataValue, ERPFieldMapping field, SystemObject object)
	{
		if (dataValue == null)
		{
			dataValue = "";
		}
		boolean isMandatory = field.isMandatory();
		String ERPField = field.getERPField();
		int maxLength = field.getMaxLength();
		if ("".equals(dataValue))
		{
			if (isMandatory)
			{
				throw new InvalidDataException("ERPField: " + ERPField + " is mandatory, but the value is null in " + this.getFoundationObjectDesp(object));
			}
			else
			{
				return;
			}
		}
		if (!field.getDataType().validate(dataValue, field, this.getBooleanCombinationReg()))
		{
			throw new InvalidDataException("dataType validate error. data: " + dataValue + ", type: " + field.getDataType().getType());
		}
		if ((maxLength != 0) && (this.getDataLength(dataValue) > maxLength))
		{
			throw new InvalidDataException("ERPField: " + ERPField + "'s maxLength is " + maxLength + ", but the value is " + dataValue + " in "
					+ this.getFoundationObjectDesp(object));
		}
	}

	/**
	 * ????????????<b>object</b>???FULLNAME$
	 * 
	 * @param object
	 * @return
	 */
	public String getFoundationObjectDesp(SystemObject object)
	{
		return (String) object.get("FULLNAME$");
	}

	/**
	 * ??????<b>field</b>????????????????????????<b>SystemObject</b>
	 * 
	 * @param baseObj
	 * @param field
	 * @return
	 * @throws ServiceRequestException
	 */
	private SystemObject getTargetObject(SystemObject baseObj, JointField field) throws ServiceRequestException
	{
		SystemObject targetObject = baseObj;
		if (field.isEnd1Field())
		{
			targetObject = this.getObject(((StructureObject) baseObj).getEnd1ObjectGuid());
		}
		else if (field.isEnd2Field())
		{
			targetObject = this.getObject(((StructureObject) baseObj).getEnd2ObjectGuid());
		}
		return targetObject;
	}

	/**
	 * 
	 * @param baseObj
	 *            ???????????????????????????????????????????????????baseObj???StructureObject??????????????????????????????FoundationObject???
	 * @param fieldList
	 * @param category
	 * @param operation
	 * @return
	 * @throws ServiceRequestException
	 */
	protected Map<String, String> getObjectMapValue(SystemObject baseObj, List<ERPFieldMapping> fieldList, String category, T operation) throws ServiceRequestException
	{
		Map<String, String> returnMap = new LinkedHashMap<String, String>();
		if (fieldList == null)
		{
			return returnMap;
		}
		for (int j = 0; j < fieldList.size(); j++)
		{
			ERPFieldMapping field = fieldList.get(j);
			String dataValue = this.getFieldValue(baseObj, field, category, operation);
			this.checkFieldValue(dataValue, field, baseObj);
			returnMap.put(field.getERPField(), dataValue);
		}
		return returnMap;
	}

	/**
	 * ???????????????????????????????????????????????????????????????????????????
	 * 
	 * @param objectGuid
	 * @return
	 * @throws ServiceRequestException
	 */
	public SystemObject getObject(ObjectGuid objectGuid) throws ServiceRequestException
	{
		if (!this.itemCache.containsKey(objectGuid.toString()))
		{
			ClassInfo classInfo = this.stub.getStubService().getEMM().getClassByGuid(objectGuid.getClassGuid());
			if (classInfo.hasInterface(ModelInterfaceEnum.IStructureObject))
			{
				List<UIObjectInfo> uiObjectList = this.stub.getStubService().getEMM().listALLFormListUIObjectInBizModel(objectGuid.getClassName());
				SearchCondition searchCondition = SearchConditionFactory.createSearchConditionForBOMStructure(objectGuid.getClassName(), uiObjectList);
				StructureObject obj = this.stub.getStubService().getBOAS().getStructureObject(objectGuid, searchCondition);
				this.itemCache.put(objectGuid.toString(), obj);
			}
			else if (classInfo.hasInterface(ModelInterfaceEnum.IBOMStructure))
			{
				List<UIObjectInfo> uiObjectList = this.stub.getStubService().getEMM().listALLFormListUIObjectInBizModel(objectGuid.getClassName());
				SearchCondition searchCondition = SearchConditionFactory.createSearchConditionForBOMStructure(objectGuid.getClassName(), uiObjectList);
				BOMStructure obj = this.stub.getStubService().getBOMS().getBOM(objectGuid, searchCondition, null);
				this.itemCache.put(objectGuid.toString(), obj);
			}
			else
			{
				FoundationObject obj = this.getObjectWithoutDecorate(objectGuid);
				this.itemCache.put(objectGuid.toString(), obj);
			}
		}
		else
		{
			// DynaLogger.error("cache hit:" + this.getFoundationObjectDesp(this.itemCache.get(objectGuid.toString())));
		}
		return this.itemCache.get(objectGuid.toString());
	}

	/**
	 * ???????????????????????????????????????????????????????????????????????????
	 * 
	 * @return
	 * @throws ServiceRequestException
	 */
	protected String getFieldValue(SystemObject baseObj, ERPFieldMapping field, String category, T operation) throws ServiceRequestException
	{
		String resultValue = "";
		if (!StringUtils.isNullString(field.getPLMField()))
		{
			List<JointField> fieldList = JointField.getFiels(field.getPLMField());
			for (JointField jointField : fieldList)
			{
				SystemObject targetObj = this.getTargetObject(baseObj, jointField);

				ClassStub.decorateObjectGuid(targetObj.getObjectGuid(), this.stubService);
				String clazzName = targetObj.getObjectGuid().getClassName();
				Object dataValue = null;
				String fieldName = jointField.getClearFieldName();
				String delegateFieldName = jointField.getDelegateFieldName();
				if (this.isBuildinField(jointField.getFieldName()))
				{
					dataValue = this.getBuildinFieldValue(jointField.getName(), field, category, baseObj, operation);
				}
				else
				{
					ClassField classField = null;
					classField = this.stub.getStubService().getEMM().getFieldByName(clazzName, fieldName, true);
					if (classField == null)
					{
						throw new InvalidDataException("field not found: " + clazzName + "." + fieldName);
					}
					else
					{
						dataValue = this.getValue(fieldName, delegateFieldName, targetObj, classField.getType(), field, operation);
					}
				}
				resultValue += (dataValue == null ? "" : dataValue.toString());
			}
		}
		if (StringUtils.isNullString(resultValue))
		{
			resultValue = field.getDefaultValue();
		}
		if (resultValue.contains("\n"))
		{
			resultValue = resultValue.replace("\n", " ");
		}
		resultValue = this.formatValue(resultValue, field);
		return resultValue;
	}

	/**
	 * O20061/A.5-???????????????????????????????????? by haop
	 * ?????????????????????????????????User????????????
	 * 
	 * @param baseObj
	 * @param field
	 * @return
	 */
	private String getFieldValue2(SystemObject baseObj, ERPFieldMapping field)
	{
		String resultValue = "";
		if (!StringUtils.isNullString(field.getPLMField()))
		{
			List<JointField> fieldList = JointField.getFiels(field.getPLMField());
			for (JointField jointField : fieldList)
			{
				SystemObject targetObj;
				try
				{
					targetObj = this.getTargetObject(baseObj, jointField);
					Object dataValue = null;
					dataValue = targetObj.get(jointField.toString());
					resultValue += (dataValue == null ? "" : dataValue.toString());
				}
				catch (ServiceRequestException e)
				{
					e.printStackTrace();
				}
			}
		}
		if (StringUtils.isNullString(resultValue))
		{
			resultValue = field.getDefaultValue();
		}
		resultValue = this.formatValue(resultValue, field);
		return resultValue;
	}

	/**
	 * ???<b>object</b>?????????<field>fieldName, delegateFieldName</b>?????? <br/>
	 * ????????????????????????PLM???????????????????????????
	 * 
	 * @param fieldName
	 * @param delegateFieldName
	 * @param object
	 * @param type
	 *            PLM????????????
	 * @param field????????????
	 * @return
	 * @throws ServiceRequestException
	 */
	private Object getValue(String fieldName, String delegateFieldName, SystemObject object, FieldTypeEnum type, ERPFieldMapping field, T operation) throws ServiceRequestException
	{
		Object dataValue = "";
		// ??????????????????????????????????????????????????????""
		if (object.get(fieldName) == null || "".equals(object.get(fieldName)))
		{
			return dataValue;
		}
		if (FieldTypeEnum.BOOLEAN == type)
		{
			dataValue = object.get(fieldName);
		}
		else if (FieldTypeEnum.FLOAT == type)
		{
			dataValue = new BigDecimal(object.get(fieldName).toString());
		}
		else if (FieldTypeEnum.INTEGER == type)
		{
			dataValue = Integer.valueOf(object.get(fieldName).toString());
		}
		else if (FieldTypeEnum.STRING == type)
		{
			dataValue = object.get(fieldName);
			if ("LIFECYCLEPHASE$".equalsIgnoreCase(fieldName) && StringUtils.isGuid((String) dataValue))
			{
				LifecyclePhaseInfo info = this.stub.getStubService().getEMM().getLifecyclePhaseInfo(dataValue.toString());
				if (info == null)
				{
					dataValue = "";
				}
				else
				{
					if (delegateFieldName == null)
					{// ????????????delegateFieldName????????????name
						delegateFieldName = "name";
					}
					if ("name".equalsIgnoreCase(delegateFieldName))
					{
						dataValue = info.getName();
					}
					else if ("title".equalsIgnoreCase(delegateFieldName))
					{
						dataValue = StringUtils.getMsrTitle(info.getTitle(), this.stub.lang.getType());
					}
					else
					{
						throw new InvalidDataException("only name or title are allowed for LifecyclePhase.fieldName = " + fieldName);
					}
				}
			}
		}
		else if (FieldTypeEnum.CLASSIFICATION == type)
		{
			CodeItemInfo classification = this.stub.getStubService().getEMM().getCodeItem((String) object.get(fieldName));
			if (classification == null)
			{
				dataValue = "";
			}
			else
			{
				if (delegateFieldName == null)
				{// ????????????delegateFieldName????????????code
					delegateFieldName = "code";
				}
				if ("code".equalsIgnoreCase(delegateFieldName))
				{
					dataValue = classification.getCode();
				}
				else if ("title".equalsIgnoreCase(delegateFieldName))
				{
					dataValue = classification.getTitle(this.stub.lang);
				}
				else
				{
					throw new InvalidDataException("only code or title are allowed for Classification. fieldName = " + fieldName);
				}
			}
		}
		else if (FieldTypeEnum.CODE == type || FieldTypeEnum.CODEREF == type)
		{
			String codeGuid = (String) object.get(fieldName);
			if (StringUtils.isGuid(codeGuid))
			{
				dataValue = this.getCodeValue(codeGuid, delegateFieldName, fieldName);
			}
		}
		else if (FieldTypeEnum.MULTICODE == type)
		{
			String codeGuids = (String) object.get(fieldName);
			if (!StringUtils.isNullString(codeGuids))
			{
				List<String> codeGuidList = StringUtils.splitToListByStrs(codeGuids, ";");
				if (!SetUtils.isNullList(codeGuidList))
				{
					String tempValue = "";
					for (String codeGuid : codeGuidList)
					{
						tempValue = tempValue + this.getCodeValue(codeGuid, delegateFieldName, fieldName) + ";";
					}
					if (tempValue.length() > 0 && tempValue.endsWith(";"))
					{
						dataValue = tempValue.substring(0, tempValue.length() - 1);
					}
				}
			}
		}
		else if (FieldTypeEnum.DATE == type)
		{
			dataValue = DateFormat.format((Date) object.get(fieldName), field.getDateFormat());
		}
		else if (FieldTypeEnum.DATETIME == type)
		{
			dataValue = DateFormat.format((Date) object.get(fieldName), field.getDateFormat());
		}
		else if (FieldTypeEnum.OBJECT == type)
		{
			// ?????????Object??????????????????????????????????????????????????????
			String guid = (String) object.get(fieldName);
			String classGuid = (String) object.get(fieldName + "$CLASS");
			// O20061/A.5-???????????????????????????????????? by haop
			String className = object.getObjectGuid().getClassName();
			ClassField classField = this.stub.getStubService().getEMM().getFieldByName(className, fieldName, true);
			// ??????
			if (classField.getTypeValue().equalsIgnoreCase("IUser"))
			{
				User user = this.stub.getStubService().getAAS().getUser(guid);
				ERPFieldMapping fieldMapping = new ERPFieldMapping();
				if (StringUtils.isNullString(delegateFieldName))
				{// ????????????delegateFieldName????????????NAME??????
					delegateFieldName = "NAME";
				}
				fieldMapping.setPLMField(delegateFieldName);
				dataValue = this.getFieldValue2(user, fieldMapping);
			}
			// ?????????
			else if (classField.getTypeValue().equalsIgnoreCase("IGroup"))
			{
				Group group = this.stub.getStubService().getAAS().getGroup(guid);
				ERPFieldMapping fieldMapping = new ERPFieldMapping();
				if (StringUtils.isNullString(delegateFieldName))
				{// ????????????delegateFieldName????????????NAME??????
					delegateFieldName = "NAME";
				}
				fieldMapping.setPLMField(delegateFieldName);
				dataValue = this.getFieldValue2(group, fieldMapping);
			}
			// ??????
			else
			{
				ObjectGuid objectGuidFieldObject = new ObjectGuid();
				objectGuidFieldObject.setGuid(guid);
				objectGuidFieldObject.setClassGuid(classGuid);
				FoundationObject foundationObjectByField = (FoundationObject) this.getObject(objectGuidFieldObject);
				if (foundationObjectByField != null)
				{
					if (StringUtils.isNullString(delegateFieldName))
					{// ????????????delegateFieldName????????????ID$??????
						delegateFieldName = "ID$";
					}
					// ?????????????????????delegateFieldName??????????????????????????????????????????delegateFieldName?????????????????????????????????
					ERPFieldMapping fieldMapping = new ERPFieldMapping();
					fieldMapping.setPLMField(delegateFieldName);
					dataValue = this.getFieldValue(foundationObjectByField, fieldMapping, null, operation);
				}
			}
		}
		else
		{// ????????????
			dataValue = object.get(fieldName);
		}
		return dataValue;
	}

	private FoundationObject getObjectWithoutDecorate(ObjectGuid objectGuid) throws ServiceRequestException
	{
		return ((BOASImpl) this.stubService.getBOAS()).getFoundationStub().getObjectWithoutDecorate(objectGuid);
	}

	/**
	 * 
	 * @param codeGuid
	 * @param delegateFieldName
	 * @return
	 * @throws ServiceRequestException
	 */
	private String getCodeValue(String codeGuid, String delegateFieldName, String fieldName) throws ServiceRequestException
	{
		String resultValue = "";
		if (StringUtils.isGuid(codeGuid))
		{
			CodeItemInfo code = this.stub.getStubService().getEMM().getCodeItem(codeGuid);
			if (code == null)
			{
				resultValue = "";
			}
			else
			{
				if (delegateFieldName == null)
				{// ????????????delegateFieldName???????????????code
					delegateFieldName = "code";
				}
				if ("Code".equalsIgnoreCase(delegateFieldName))
				{
					resultValue = code.getCode();
				}
				else if ("title".equalsIgnoreCase(delegateFieldName))
				{
					resultValue = code.getTitle(this.stub.lang);
				}
				else
				{
					throw new InvalidDataException("only code or title are allowed for Code. fieldName = " + fieldName);
				}
			}
		}
		return resultValue;
	}

	/**
	 * ????????????
	 * 
	 * @param value
	 * @return
	 */
	protected String formatValue(Object value, ERPFieldMapping fieldMapping)
	{
		if (value == null || "".equals(value))
		{
			return "";
		}

		if (fieldMapping.getDataType() == ERPFieldMappingTypeEnum.NUMBER && fieldMapping.getPrecision() >= 0)
		{
			DecimalFormat decimalFormat = new DecimalFormat();
			decimalFormat.setMaximumFractionDigits(fieldMapping.getPrecision());
			decimalFormat.setRoundingMode(RoundingMode.HALF_EVEN);
			decimalFormat.setGroupingUsed(false);
			return decimalFormat.format(Double.parseDouble(value.toString()));
		}

		if (fieldMapping.getMaxLength() > 0 && this.getDataLength(value.toString()) > fieldMapping.getMaxLength() && fieldMapping.isTruncWhenLengthExceeds())
		{
			return this.ExceedsData(value, fieldMapping.isLengthExceedsOrientationisRight(), fieldMapping.getMaxLength());
		}

		return value.toString();
	}

	/**
	 * boolean????????????????????????????????? <br/>
	 * ????????????????????????|??????
	 * 
	 * @return
	 */
	protected String getBooleanCombinationReg()
	{
		return "(?i)[(true|false)(1|0)(T|F)(true|false)]";
	}

	/**
	 * ??????<b>fieldName</b>?????????????????????????????????????????????????????????null <br/>
	 * ?????????xml????????????????????? ??? ???????????????????????????????????????(?????????????????????????????????????????????????????????????????????)
	 * 
	 * @param fieldName
	 * @return
	 */
	protected String getBuildinFieldValue(String fieldName, ERPFieldMapping field, String category, SystemObject baseObj, T operation)
	{
		String dataValue = null;
		if (this.getParameter(null).getParamMap().get(fieldName) != null)
		{
			dataValue = this.getParameter(null).getParamMap().get(fieldName);
		}
		else
		{
			if (RESERVED_DATE.equalsIgnoreCase(fieldName))
			{
				if (RESERVED_EMPTY.equalsIgnoreCase(field.getDateFormat()))
				{
					return String.valueOf(CURRENT_DATE.getTime());
				}
				else
				{
					dataValue = DateFormat.format(CURRENT_DATE, field.getDateFormat());
				}
			}
			else if (RESERVED_JOBID.equalsIgnoreCase(fieldName))
			{
				dataValue = this.stub.jobId;
			}
			// ${DataKey} ??? ${XMLPackageNo} ????????????????????????????????????????????????????????????????????????????????????XML??????????????????????????????
			// else if (reservedDataKey.equalsIgnoreCase(fieldName))
			// {
			// dataValue = this.stub.getDataKey();
			// }
			// else if (reservedXMLPackageNo.equalsIgnoreCase(fieldName))
			// {
			// dataValue = this.stub.getXMLPackNo();
			// }
			else if (RESERVED_EMPTY.equalsIgnoreCase(fieldName))
			{
				dataValue = "";
			}
			else if (RESERVED_HASSENT.equalsIgnoreCase(fieldName))
			{
				if (!this.isSaveTransferStatus())
				{
					dataValue = "N";
				}
				else
				{
					Map<String, Object> param = new HashMap<String, Object>();
					if (ERP_BOM.equals(category))
					{
						param.put(ERPTransferLog.targetGuid, this.bomView.getGuid());
						param.put("CLASSGUID", this.bomView.getObjectGuid().getClassGuid());
						param.put(ERPTransferLog.iteration, this.bomView.getIterationId());
					}
					else
					{
						param.put(ERPTransferLog.targetGuid, baseObj.getGuid());
						param.put("CLASSGUID", baseObj.getObjectGuid().getClassGuid());
						param.put(ERPTransferLog.iteration, baseObj.get(SystemClassFieldEnum.ITERATIONID.getName()));
					}
					param.put(ERPTransferLog.ERPName, this.stub.ERPType.getProName());
					param.put(ERPTransferLog.ERPOpertion, this.getOperationId(operation));
					param.put(ERPTransferLog.category, category);
					try
					{
						List<ERPTransferLog> logList = this.stub.getStubService().getERPTransferLog(param);
						if (logList.isEmpty())
						{
							dataValue = "N";
						}
						else
						{
							dataValue = "Y";
						}
					}
					catch (ServiceRequestException e)
					{
						throw new InvalidDataException(e.getMessage());
					}
				}
			}
			else if (RESERVED_USERID.equalsIgnoreCase(fieldName))
			{
				return this.stub.userId;
			}
			else if (RESERVED_ERP_USERID.equalsIgnoreCase(fieldName))
			{
				return this.getParameter(null).getParamMap().get("user");
			}
			else if (RESERVED_FACTORY.equalsIgnoreCase(fieldName))
			{
				return this.stub.factory;
			}
			else if (RESERVED_DATAKEY.equalsIgnoreCase(fieldName))
			{
				return this.stub.getDataKey();
			}
			else
			{
				// ????????????????????????????????????????????????????????????????????????(${DataKey} ${XMLPackageNo} ${Designators}????????????????????????????????????????????????????????????????????????)
				dataValue = fieldName;
			}
		}
		return dataValue == null ? "" : dataValue;
	}

	/**
	 * ?????????????????????????????????????????????????????????????????????${XX}, ???${Date}??????????????????/??????
	 * 
	 * @param fieldName
	 * @return
	 */
	protected boolean isBuildinField(String fieldName)
	{
		return fieldName.matches("^\\$\\{\\w+\\}$");
	}

	/**
	 * ????????????Operation???Id
	 * 
	 * @return
	 */
	public abstract String getOperationId(T operation);

	/**
	 * ?????????????????????????????????Operation
	 * 
	 * @return
	 */
	public abstract String getOperationCrossServiceName(T operation);

	public abstract String getOperationName(T operation);

	/**
	 * ??????Operation???category
	 * 
	 * @return
	 */
	public abstract String getOperationCategory(T operation);

	public String getCreateItemOperationId()
	{
		return "createItem";
	}

	/**
	 * ?????????BOM?????????id
	 * 
	 * @return
	 */
	protected String getCreateBOMOperationId()
	{
		return "createBOM";
	}

	protected String getCreateLocalROperationId()
	{
		return "createLocalR";
	}

	protected String getCreateLocalSOperationId()
	{
		return "createLocalS";
	}

	protected String getCreateGlobalROperationId()
	{
		return "createGlobalR";
	}

	protected String getCreateGlobalSOperationId()
	{
		return "createGlobalS";
	}

	/**
	 * ??????????????????????????????????????????<br/>
	 * ???????????????PLM???????????????defaultValue???????????????true,??????????????????false
	 * 
	 * @param fieldList
	 * @param parentName
	 * @return
	 */
	protected boolean existSameField(List<ERPFieldMapping> fieldList, String ERPField)
	{
		ERPFieldMapping field = null;
		for (int i = 0; i < fieldList.size(); i++)
		{
			field = fieldList.get(i);
			if (field.getERPField().equals(ERPField))
			{// ???????????????????????????????????????
				return true;
			}
		}
		// ??????????????????????????????????????????????????????????????????
		return false;
	}

	/**
	 * ???xml?????????????????????????????????category???class??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
	 * 
	 * @param usedClassInfo
	 * @param erpClass
	 * @param pdmColumnList
	 * @throws ServiceRequestException
	 */
	private void getFieldListFromXML(ClassInfo PLMClass, String category, Map<String, List<ERPFieldMapping>> fieldMap) throws ServiceRequestException
	{
		// ??????????????????????????????
		if (PLMClass == null)
		{
			return;
		}

		List<Element> classEleList = this.doc.getRootElement().getChild("category").getChild(category).getChildren("class");
		if (!classEleList.isEmpty())
		{
			Iterator<Element> classIt = classEleList.iterator();
			Element classEle = null;
			while (classIt.hasNext())
			{
				classEle = classIt.next();
				if (classEle.getAttributeValue("name").equals(PLMClass.getName()))
				{
					Iterator<Element> tableIt = classEle.getChildren("table").iterator();
					Element tableEle = null;
					while (tableIt.hasNext())
					{
						tableEle = tableIt.next();
						String tableName = tableEle.getAttributeValue("name");
						Iterator<Element> fieldIt = tableEle.getChildren("mapping").iterator();
						Element mappingEle = null;
						while (fieldIt.hasNext())
						{
							mappingEle = fieldIt.next();
							if (fieldMap.get(tableName) == null)
							{
								fieldMap.put(tableName, new ArrayList<ERPFieldMapping>());
							}
							Map<String, String> paramMap = this.getMapByElement(mappingEle);
							if (!this.existSameField(fieldMap.get(tableName), paramMap.get("ERPField")))
							{

								fieldMap.get(tableName).add(this.getFieldMapping(paramMap));
							}
						}
						mappingEle = null;
						fieldIt = null;
					}
					break;
				}
			}
		}

		if (PLMClass.getSuperclass() == null)
		{
			return;
		}
		// ????????????????????????????????????????????????
		this.getFieldListFromXML(this.stub.getStubService().getEMM().getClassByName(PLMClass.getSuperclass()), category, fieldMap);
	}

	protected Map<String, String> getMapByElement(Element element)
	{
		Map<String, String> map = new HashMap<String, String>();
		Iterator<Element> it = element.getChildren("param").iterator();
		Element ele = null;
		while (it.hasNext())
		{
			ele = it.next();
			map.put(ele.getAttributeValue("name"), ele.getAttributeValue("value"));
		}
		return map;
	}

	/**
	 * xml???????????????????????????ERPFieldMapping
	 * 
	 * @param map
	 * @return
	 */
	protected ERPFieldMapping getFieldMapping(Map<String, String> map)
	{
		ERPFieldMapping mapping = new ERPFieldMapping();
		mapping.setPLMField(StringUtils.convertNULLtoString(map.get("PLMField")));
		mapping.setERPField(StringUtils.convertNULLtoString(map.get("ERPField")));
		mapping.setDefaultValue(StringUtils.convertNULLtoString(map.get("defaultValue")));
		mapping.setDescription(StringUtils.convertNULLtoString(map.get("description")));
		mapping.setKeyColumn("true".equalsIgnoreCase(StringUtils.convertNULLtoString(map.get("isKeyColumn"))));
		mapping.setMandatory("true".equalsIgnoreCase(StringUtils.convertNULLtoString(map.get("isMandatory"))));
		mapping.setShowDefault("true".equalsIgnoreCase(StringUtils.convertNULLtoString(map.get("isShowDefault"))));
		String dateFormat = StringUtils.convertNULLtoString(map.get("dateFormat"));
		if (StringUtils.isNullString(dateFormat))
		{
			mapping.setDateFormat(this.defaultDateFormat);
		}
		else
		{
			mapping.setDateFormat(dateFormat);
		}
		String maxLength = StringUtils.convertNULLtoString(map.get("maxLength"));
		mapping.setMaxLength("".equals(maxLength) ? 0 : Integer.parseInt(maxLength));
		mapping.setTruncWhenLengthExceeds("true".equalsIgnoreCase(map.get("truncWhenLengthExceeds")));
		mapping.setLengthExceedsOrientationisRight("right".equalsIgnoreCase(map.get("LengthExceedsOrientation")));
		String precision = StringUtils.convertNULLtoString(map.get("precision"));
		if (!"".equals(precision))
		{
			mapping.setPrecision(Integer.parseInt(precision));
		}
		String dataType = StringUtils.convertNULLtoString(map.get("dataType"));
		if (!"".equals(dataType))
		{
			mapping.setDataType(ERPFieldMappingTypeEnum.getEnumByType(dataType));
		}
		if (!this.validateERPFieldMapping(mapping))
		{
			throw new IllegalArgumentException("dyan.app.service.brs.erpi.dataExport.IntegrateERP#validateERPFieldMapping error");
		}
		return mapping;
	}

	/**
	 * ??????ERPFieldMapping???????????? <br/>
	 * 1: ERPField??????????????????<br/>
	 * 
	 * @return
	 */
	private boolean validateERPFieldMapping(ERPFieldMapping mapping)
	{
		if ("".equals(mapping.getERPField()))
		{
			throw new IllegalArgumentException("ERPField can't be null");
		}
		return true;
	}

	/**
	 * ????????????????????????category??????????????????????????????????????????????????????????????????xml??????????????????????????????
	 * 
	 * @param category
	 * @param className
	 * @param tableName
	 * @return
	 * @throws ServiceRequestException
	 */
	protected List<ERPFieldMapping> getFieldFromMap(String category, String className, String tableName) throws ServiceRequestException
	{
		if (this.PLMColumnMap.get(category) == null || this.PLMColumnMap.get(category).get(className) == null
				|| this.PLMColumnMap.get(category).get(className).get(tableName) == null || this.PLMColumnMap.get(category).get(className).get(tableName).isEmpty())
		{
			Map<String, List<ERPFieldMapping>> tableFieldMap = new HashMap<String, List<ERPFieldMapping>>();
			this.getFieldListFromXML(this.stub.getStubService().getEMM().getClassByName(className), category, tableFieldMap);
			if (SetUtils.isNullList(tableFieldMap.get(tableName)))
			{
				throw new IllegalArgumentException("no field mapping for " + category + "." + className + "." + tableName + " in " + this.xmlPath);
			}
			if (this.shuldSortFields())
			{
				Collections.sort(tableFieldMap.get(tableName), this.fieldComparator);
			}
			Map<String, Map<String, List<ERPFieldMapping>>> classFieldMap = new HashMap<String, Map<String, List<ERPFieldMapping>>>();
			classFieldMap.put(className, tableFieldMap);
			this.PLMColumnMap.put(category, classFieldMap);
		}
		return this.PLMColumnMap.get(category).get(className).get(tableName);
	}

	/**
	 * ???????????????????????????<br/>
	 * ???YF E10????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????<br/>
	 * ??????????????????????????????
	 */
	protected boolean shuldSortFields()
	{
		return false;
	}

	public Map<String, List<RecordSet>> getDataMap()
	{
		return this.dataMap;
	}

	/**
	 * ????????????????????????
	 * 
	 * @param category
	 * @return
	 */
	public List<RecordSet> getHeaderPackageOfData(String category)
	{
		int count = this.getRecordSetSizePerXMLPackage(category);
		if (count > this.getDataMap().get(category).size())
		{
			throw new IllegalStateException("out of data size(dataMap) in category: " + category);
		}
		Iterator<RecordSet> it = this.getDataMap().get(category).iterator();
		int temp = 0;
		List<RecordSet> subList = new ArrayList<RecordSet>();
		while (it.hasNext())
		{
			RecordSet rs = it.next();
			subList.add(rs);
			it.remove();
			temp++;
			if (temp >= count)
			{
				break;
			}
		}
		return subList;
	}

	/**
	 * ??????<b>operation</b>???????????????xml?????? <br/>
	 * ??????totalRecordSize???pageRecordSize??????pageCount????????????<br/>
	 * (totalRecordSize + pageRecordSize - 1) / pageRecordSize ???????????? (totalRecordSize - 1) / pageRecordSize + 1??????????????????
	 * ??????totalRecordSize=0,????????????????????????????????????1????????????????????????
	 * 
	 * @param operation
	 * @return
	 */
	public int getXMLPackageCount(T operation)
	{
		final String category = this.getOperationCategory(operation);
		if (category.equalsIgnoreCase(IntegrateERP.ERP_BOM))
		{
			int BOMBatchSize = this.getBOMBatchSize();
			if (SetUtils.isNullList(this.dataMap.get(category)))
			{
				return -1;
			}
			else if (BOMBatchSize == 0)
			{
				return 1;
			}
			else
			{
				return (this.dataMap.get(category).size() + BOMBatchSize - 1) / BOMBatchSize;
			}
		}
		else
		{
			int batchSize = this.getBatchSize();

			if (SetUtils.isNullList(this.dataMap.get(category)))
			{
				return -1;
			}
			else if (batchSize == 0)
			{
				return 1;
			}
			else
			{
				return (this.dataMap.get(category).size() + batchSize - 1) / batchSize;
			}
		}
	}

	/**
	 * ????????????xml??????????????????<br/>
	 * ???????????????????????????????????????????????????, return value is no more greater than <b>batchSize</b> or <b>BOMBatchSize</b>.
	 * 
	 * @param category
	 * @param remainingCount
	 * @return
	 */
	protected int getRecordSetSizePerXMLPackage(String category)
	{
		final int remainingCount = this.dataMap.get(category).size();
		// -1 means all recordsets are in one package
		int result = -1;
		if (ERP_BOM.equals(category))
		{
			int BOMBatchSize = this.getBOMBatchSize();
			result = BOMBatchSize > 0 ? BOMBatchSize : -1;
		}
		else
		{
			int batchSize = this.getBatchSize();
			result = batchSize > 0 ? batchSize : -1;
		}
		return (result == -1 || result >= remainingCount) ? remainingCount : result;
	}

	protected String getTableNameById(String category, String id)
	{
		return this.getTables(category).get(id);
	}

	/**
	 * ??????category??????tableMap, id???name???
	 * 
	 * @param category
	 * @return
	 */
	public Map<String, String> getTables(String category)
	{
		if (this.tableMap.isEmpty() || this.tableMap.get(category) == null || this.tableMap.get(category).isEmpty())
		{
			Element tableEle = this.doc.getRootElement().getChild("category").getChild(category).getChild("tables");
			if (tableEle == null)
			{
				return null;
			}
			Iterator<Element> tableIt = tableEle.getChildren("table").iterator();
			Element tempEle = null;
			Map<String, String> tableMap = new LinkedHashMap<String, String>();
			while (tableIt.hasNext())
			{
				tempEle = tableIt.next();
				tableMap.put(tempEle.getAttributeValue("id"), tempEle.getAttributeValue("name"));
			}
			tempEle = null;
			tableIt = null;
			this.tableMap.put(category, tableMap);
		}
		return this.tableMap.get(category);
	}

	/**
	 * PLM???ERP????????????xml?????????ERP?????????????????????????????????????????????????????????
	 * 
	 * @param operation
	 * @param totalCount
	 * @param index
	 * @return
	 * @throws UnknownHostException
	 */
	public abstract Document getCreateDataXML(T operation, int totalCount, int index) throws Exception;

	/**
	 * ??????<b>Schema</b>????????????operation
	 * 
	 * @param isMergeByCategory
	 *            ??????????????????category???operation
	 * @return
	 * @throws ServiceRequestException
	 */
	public abstract List<T> getOperationList(boolean isMergeByCategory) throws ServiceRequestException;

	/**
	 * xml?????????????????????
	 * 
	 * @return
	 */
	public String getXMLPath()
	{
		return this.xmlPath;
	}

	/**
	 * <b>RSItem</b>?????????????????????????????????????????????
	 * 
	 * @param rs
	 * @throws ServiceRequestException
	 */
	protected void addRSEnd2FoundationObject(FoundationObject rs, T operation) throws ServiceRequestException
	{
		// ObjectGuid objectGuid = new ObjectGuid(rs.get("RSITEM$CLASS").toString(), null, rs.get("RSItem").toString(),
		// null);
		ObjectGuid objectGuid = new ObjectGuid((String) rs.get(ReplaceSubstituteConstants.RSItem + ReplaceSubstituteConstants.ClassGuid), null, null,
				(String) rs.get(ReplaceSubstituteConstants.RSItem + ReplaceSubstituteConstants.MASTER), true, null);
		FoundationObject obj = (FoundationObject) this.getObject(objectGuid);
		this.addToDataMap(ERP_ITEM, this.getEachItemData(obj, this.getRSItemOpertion()));
	}

	/**
	 * ?????????????????????????????????????????????ERP
	 * 
	 * @return
	 */
	protected boolean shouldAddRSEnd2Item()
	{
		return this.stub.schema.isExportRSItem() && this.stub.schema.getOperationList().contains(this.getCreateItemOperationId());
	}

	/**
	 * ???????????????????????????<b>schema</b>?????????createItem
	 * 
	 * @return
	 * @throws ServiceRequestException
	 */
	private T getRSItemOpertion() throws ServiceRequestException
	{
		for (T operation : this.getOperationList(false))
		{
			if (this.getOperationId(operation).equals(this.getCreateItemOperationId()))
			{
				return operation;
			}
		}
		return null;
	}

	/**
	 * ???????????????????????????
	 * 
	 * @param end1Obj
	 * @param category
	 * @param className
	 * @return
	 * @throws ServiceRequestException
	 */
	protected RecordSet getEachItemData(FoundationObject end1Obj, T operation) throws ServiceRequestException
	{
		if (this.checkObjectReturningSkip(end1Obj, ERP_ITEM, operation))
		{
			return null;
		}
		ClassStub.decorateObjectGuid(end1Obj.getObjectGuid(), this.stubService);
		RecordSet recordSet = new RecordSet(end1Obj.getObjectGuid());
		Iterator<String> tableIt = this.getTables(ERP_ITEM).values().iterator();
		String tableName = null;
		// add2StampMap(ERP_ITEM, end1Obj);
		while (tableIt.hasNext())
		{
			tableName = tableIt.next();
			// fieldList??? ????????????table????????????????????????
			List<ERPFieldMapping> fieldList = this.getFieldFromMap(ERP_ITEM, end1Obj.getObjectGuid().getClassName(), tableName);
			TableRecordData tableRecord = new TableRecordData(this.getObjectMapValue(end1Obj, fieldList, ERP_ITEM, operation));
			recordSet.put(tableName, tableRecord);
		}
		return recordSet;
	}

	/**
	 * ???????????????BOM????????????
	 * 
	 * @param bomStructureList
	 * @param category
	 * @param className
	 * @return
	 * @throws ServiceRequestException
	 */
	protected RecordSet getEachBOMData(FoundationObject end1Obj, List<BOMStructure> bomStructureList, BOMStructure parentBOMStructure, T operation) throws ServiceRequestException
	{
		if (end1Obj != null && end1Obj.getStatus().equals(SystemStatusEnum.OBSOLETE)) // O23181
		{
			return null;
		}
		if (this.checkObjectReturningSkip(this.bomView, ERP_BOM, operation))
		{
			return null;
		}
		if (bomStructureList.isEmpty() && !this.isContinEmptyBOMXML())
		{
			if (!this.isPrintEmptyBOMXML)
			{
				return RecordSet.emptySet(this.getMockObjectGuid(null));
			}
		}

		boolean isConfigBOMExport = ConfigParameterConstants.CONFIG_PARAMETER_RESULTRELATION_TEMPLATE_NAME.equals(this.stub.templateName);
		ObjectGuid objGuid = bomStructureList.isEmpty() || isConfigBOMExport ? this.getMockObjectGuid(end1Obj) : this.bomView.getObjectGuid();
		RecordSet recordSet = new RecordSet(objGuid);
		String tableName = null;
		if (!bomStructureList.isEmpty())
		{
			Iterator<String> tableIt = this.getTables(ERP_BOM).values().iterator();
			while (tableIt.hasNext())
			{
				tableName = tableIt.next();
				TableRecordData tableRecord = new TableRecordData();
				for (int i = 0; i < bomStructureList.size(); i++)
				{
					ObjectGuid structureObjectGuid = bomStructureList.get(i).getObjectGuid();
					ClassStub.decorateObjectGuid(structureObjectGuid, this.stubService);
					List<ERPFieldMapping> fieldList = this.getFieldFromMap(ERP_BOM, structureObjectGuid.getClassName(), tableName);
					tableRecord.add(this.getObjectMapValue(bomStructureList.get(i), fieldList, ERP_BOM, operation));
				}
				recordSet.put(tableName, tableRecord);
			}
		}
		else if (this.hasBOM(end1Obj))
		{
			dealEmptyBom(recordSet, end1Obj, operation);
		}
		return recordSet;
	}

	protected void dealEmptyBom(RecordSet recordSet, FoundationObject end1Obj, T operation) throws ServiceRequestException
	{
		Iterator<String> tableIt = this.getTables(ERP_BOM).values().iterator();
		String className = this.getArbitraryBOMClassName();
		String tableName = null;
		while (tableIt.hasNext())
		{
			tableName = tableIt.next();
			this.getFieldFromMap(ERP_BOM, className, tableName);
		}

		tableName = this.getBOMHeaderTableName();
		TableRecordData tableRecord = new TableRecordData();
		List<ERPFieldMapping> fieldList = this.getFieldFromMap(ERP_BOM, this.getArbitraryBOMClassName(), tableName);
		fieldList = this.removeEnd1Prefix(fieldList);
		this.erpFieldMappingContainer.put(tableName, className, fieldList);
		tableRecord.add(this.getObjectMapValue(end1Obj, fieldList, ERP_BOM, operation));
		recordSet.put(tableName, tableRecord);
	}

	/**
	 * ???????????? ??????????????????
	 * 
	 * @return
	 * @throws ServiceRequestException
	 */
	protected RecordSet getEachLocalS(FoundationObject obj, T operation) throws ServiceRequestException
	{
		if (this.checkObjectReturningSkip(obj, ERP_REPLACESUBSTITUTE, operation))
		{
			return null;
		}
		if (this.shouldAddRSEnd2Item())
		{
			this.addRSEnd2FoundationObject(obj, operation);
		}
		ClassStub.decorateObjectGuid(obj.getObjectGuid(), this.stubService);
		// add2StampMap(ERP_REPLACESUBSTITUTE, obj);
		RecordSet recordSet = new RecordSet(obj.getObjectGuid());
		Iterator<String> tableIt = this.getTables(ERP_REPLACESUBSTITUTE).values().iterator();
		String className = obj.getObjectGuid().getClassName();
		String tableName = null;
		while (tableIt.hasNext())
		{
			tableName = tableIt.next();
			List<ERPFieldMapping> fieldList = this.getFieldFromMap(ERP_REPLACESUBSTITUTE, className, tableName);
			this.erpFieldMappingContainer.put(tableName, className, fieldList);
			TableRecordData tableRecord = new TableRecordData(this.getObjectMapValue(obj, fieldList, ERP_REPLACESUBSTITUTE, operation));
			recordSet.put(tableName, tableRecord);
		}
		String key = StringUtils.convertNULLtoString(obj.get(ReplaceSubstituteConstants.MasterItem + ReplaceSubstituteConstants.MASTER))
				+ StringUtils.convertNULLtoString(obj.get(ReplaceSubstituteConstants.ComponentItem + ReplaceSubstituteConstants.MASTER))
				+ StringUtils.convertNULLtoString(obj.get(ReplaceSubstituteConstants.RSType)) + StringUtils.convertNULLtoString(obj.get(ReplaceSubstituteConstants.Scope));
		recordSet.setGroupKey(key);
		key = key + StringUtils.convertNULLtoString(obj.get(ReplaceSubstituteConstants.RSItem + ReplaceSubstituteConstants.MASTER));
		recordSet.setItemKey(StringUtils.generateMd5(key));
		return recordSet;
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @param obj
	 * @param category
	 * @return
	 * @throws ServiceRequestException
	 */
	protected RecordSet getEachGlobalS(FoundationObject obj, T operation) throws ServiceRequestException
	{
		if (this.checkObjectReturningSkip(obj, ERP_REPLACESUBSTITUTE, operation))
		{
			return null;
		}
		if (this.shouldAddRSEnd2Item())
		{
			this.addRSEnd2FoundationObject(obj, operation);
		}
		ClassStub.decorateObjectGuid(obj.getObjectGuid(), this.stubService);
		// add2StampMap(ERP_REPLACESUBSTITUTE, obj);
		RecordSet data = new RecordSet(obj.getObjectGuid());
		Iterator<String> tableIt = this.getTables(ERP_REPLACESUBSTITUTE).values().iterator();
		String tableName = null;
		String className = obj.getObjectGuid().getClassName();
		while (tableIt.hasNext())
		{
			tableName = tableIt.next();
			List<ERPFieldMapping> fieldList = this.getFieldFromMap(ERP_REPLACESUBSTITUTE, className, tableName);
			this.erpFieldMappingContainer.put(tableName, className, fieldList);
			data.put(tableName, new TableRecordData(this.getObjectMapValue(obj, fieldList, ERP_REPLACESUBSTITUTE, operation)));
		}
		String key = StringUtils.convertNULLtoString(obj.get(ReplaceSubstituteConstants.MasterItem + ReplaceSubstituteConstants.MASTER))
				+ StringUtils.convertNULLtoString(obj.get(ReplaceSubstituteConstants.ComponentItem + ReplaceSubstituteConstants.MASTER))
				+ StringUtils.convertNULLtoString(obj.get(ReplaceSubstituteConstants.RSType)) + StringUtils.convertNULLtoString(obj.get(ReplaceSubstituteConstants.Scope));
		data.setGroupKey(key);
		key = key + StringUtils.convertNULLtoString(obj.get(ReplaceSubstituteConstants.RSItem + ReplaceSubstituteConstants.MASTER));
		data.setItemKey(StringUtils.generateMd5(key));
		return data;
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @param obj
	 * @param category
	 * @return
	 * @throws ServiceRequestException
	 */
	protected RecordSet getEachLocalR(FoundationObject obj, T operation) throws ServiceRequestException
	{
		if (this.checkObjectReturningSkip(obj, ERP_REPLACESUBSTITUTE, operation))
		{
			return null;
		}
		if (this.shouldAddRSEnd2Item())
		{
			this.addRSEnd2FoundationObject(obj, operation);
		}
		ClassStub.decorateObjectGuid(obj.getObjectGuid(), this.stubService);
		// add2StampMap(ERP_REPLACESUBSTITUTE, obj);
		RecordSet data = new RecordSet(obj.getObjectGuid());
		Iterator<String> tableIt = this.getTables(ERP_REPLACESUBSTITUTE).values().iterator();
		String tableName = null;
		String className = obj.getObjectGuid().getClassName();
		while (tableIt.hasNext())
		{
			tableName = tableIt.next();
			List<ERPFieldMapping> fieldList = this.getFieldFromMap(ERP_REPLACESUBSTITUTE, className, tableName);
			this.erpFieldMappingContainer.put(tableName, className, fieldList);
			data.put(tableName, new TableRecordData(this.getObjectMapValue(obj, fieldList, ERP_REPLACESUBSTITUTE, operation)));
		}
		String key = StringUtils.convertNULLtoString(obj.get(ReplaceSubstituteConstants.MasterItem + ReplaceSubstituteConstants.MASTER))
				+ StringUtils.convertNULLtoString(obj.get(ReplaceSubstituteConstants.ComponentItem + ReplaceSubstituteConstants.MASTER))
				+ StringUtils.convertNULLtoString(obj.get(ReplaceSubstituteConstants.RSType)) + StringUtils.convertNULLtoString(obj.get(ReplaceSubstituteConstants.Scope));
		data.setGroupKey(key);
		key = key + StringUtils.convertNULLtoString(obj.get(ReplaceSubstituteConstants.RSItem + ReplaceSubstituteConstants.MASTER));
		data.setItemKey(StringUtils.generateMd5(key));
		return data;
	}

	/**
	 * ????????????????????? <br/>
	 * ???????????????????????? Item ???ApprovalSheet????????????????????????????????????????????????????????????<b>structObj</b>?????????
	 * ?????????ApprovalSheet??????????????????Item??????
	 * 
	 * @param structObj
	 *            ApprovalSheet???Item??????????????????
	 * @param category
	 * @return
	 * @throws ServiceRequestException
	 */
	protected RecordSet getEachMA(StructureObject object, String category, T operation) throws ServiceRequestException
	{
		if (this.checkObjectReturningSkip(object, category, operation))
		{
			return null;
		}
		ClassStub.decorateObjectGuid(object.getObjectGuid(), this.stubService);
		RecordSet data = new RecordSet(object.getObjectGuid());
		Iterator<String> tableIt = this.getTables(category).values().iterator();
		String tableName = null;
		String className = object.getObjectGuid().getClassName();
		while (tableIt.hasNext())
		{
			tableName = tableIt.next();
			List<ERPFieldMapping> fieldList = this.getFieldFromMap(category, className, tableName);
			this.erpFieldMappingContainer.put(tableName, className, fieldList);
			data.put(tableName, new TableRecordData(this.getObjectMapValue(object, fieldList, category, operation)));
		}
		String key = StringUtils.convertNULLtoString(object.getEnd1ObjectGuid().getMasterGuid());
		data.setGroupKey(key);
		key = key + StringUtils.convertNULLtoString(object.getEnd2ObjectGuid().getMasterGuid());
		data.setItemKey(StringUtils.generateMd5(key));
		return data;
	}

	/**
	 * ?????????BOM???????????????????????????????????????0??????????????????
	 * 
	 * @return
	 */
	public int getBatchSize()
	{
		String batchSizeStr = this.getParameter(null).getParamMap().get("batchSize");
		if (!StringUtils.isNullString(batchSizeStr))
		{
			return Integer.parseInt(batchSizeStr);
		}
		else
		{
			return 0;
		}
	}

	/**
	 * ??????BOM??????????????????????????????0???????????????
	 * 
	 * @return
	 */
	public int getBOMBatchSize()
	{
		String BOMBatchSizeStr = this.getParameter(null).getParamMap().get("BOMBatchSize");
		if (!StringUtils.isNullString(BOMBatchSizeStr))
		{
			return Integer.parseInt(BOMBatchSizeStr);
		}
		else
		{
			return 0;
		}
	}

	public List<String> getCategoryList()
	{
		return this.categoryList;
	}

	/**
	 * ??????????????????
	 * 
	 * @return
	 */
	protected String getMATemplateName()
	{
		return this.getParameter(null).getParamMap().get("maTemplate");
	}

	/**
	 * ????????????
	 * 
	 * @return
	 */
	public int getOperationPollingTime()
	{
		String value = this.getParameter(null).getParamMap().get("operationPollingTime");
		if (StringUtils.isNullString(value))
		{
			return this.stub.getDefaultOperationPollingTime();
		}
		try
		{
			return Integer.parseInt(value);
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException("operationPollingTime is not a number: " + value);
		}
	}

	/**
	 * ??????Operation??????????????????
	 * 
	 * @return
	 */
	public int getOperationLiveTime()
	{
		String value = this.getParameter(null).getParamMap().get("operationLiveTime");
		if (StringUtils.isNullString(value))
		{
			return this.stub.getDefaultOperationPollingTime();
		}
		try
		{
			return Integer.parseInt(value);
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException("operationLiveTime is not a number: " + value);
		}
	}

	/**
	 * ?????????????????????????????????
	 */
	public void releaseResource()
	{
		this.bomView = null;
		this.categoryList.clear();
		this.dataErrorList.clear();
		this.dataWarnList.clear();
		this.tobeExportedGuidMap.clear();
		this.dataMap.clear();
		this.parameterMap.clear();
		this.PLMColumnMap.clear();
		this.restrictionList.clear();
		this.doc = null;
		this.tableMap.clear();
		this.itemCache.clear();
		this.uniqueCache.clear();
	}

	/**
	 * ????????????????????????end1$????????????end2$
	 * 
	 * @param fieldList
	 * @return
	 */
	protected List<ERPFieldMapping> convertEnd1ToEnd2Field(List<ERPFieldMapping> fieldList)
	{
		List<ERPFieldMapping> newFieldList = new ArrayList<ERPFieldMapping>(fieldList.size());
		ERPFieldMapping field = null;
		for (int i = 0; i < fieldList.size(); i++)
		{
			field = new ERPFieldMapping();
			if (!StringUtils.isNullString(field.getPLMField()) && field.getPLMField().matches("(?i)End2\\$.*"))
			{
				continue;
			}
			String PLMField = fieldList.get(i).getPLMField();
			field.setPLMField(PLMField.replaceAll("(?i)End1\\$", "End2\\$"));
			this.copyOtherFieldValue(field, fieldList.get(i));
			newFieldList.add(field);
		}
		return newFieldList;
	}

	/**
	 * ?????????????????????end1$??????
	 * 
	 * @param fieldList
	 * @return
	 */
	protected List<ERPFieldMapping> removeEnd1Prefix(List<ERPFieldMapping> fieldList)
	{
		List<ERPFieldMapping> newFieldList = new ArrayList<ERPFieldMapping>(fieldList.size());
		ERPFieldMapping field = null;
		for (int i = 0; i < fieldList.size(); i++)
		{
			field = new ERPFieldMapping();
			String PLMField = fieldList.get(i).getPLMField().replaceAll("(?i)End1\\$", "");
			if (PLMField.matches("(?i)End2\\$.*"))
			{
				PLMField = "";
			}
			field.setPLMField(PLMField);
			this.copyOtherFieldValue(field, fieldList.get(i));
			newFieldList.add(field);
		}
		return newFieldList;
	}

	/**
	 * ??????end2$??????
	 * 
	 * @param fieldList
	 * @return
	 */
	protected List<ERPFieldMapping> removeEnd2Prefix(List<ERPFieldMapping> fieldList)
	{
		List<ERPFieldMapping> newFieldList = new ArrayList<ERPFieldMapping>(fieldList.size());
		ERPFieldMapping field = null;
		for (int i = 0; i < fieldList.size(); i++)
		{
			field = new ERPFieldMapping();
			String PLMField = fieldList.get(i).getPLMField().replaceAll("(?i)End2\\$", "");
			if (PLMField.matches("(?i)End1\\$.*"))
			{
				PLMField = "";
			}
			field.setPLMField(PLMField);
			this.copyOtherFieldValue(field, fieldList.get(i));
			newFieldList.add(field);
		}
		return newFieldList;
	}

	private void copyOtherFieldValue(ERPFieldMapping newField, ERPFieldMapping oldField)
	{
		newField.setERPField(oldField.getERPField());
		newField.setDefaultValue(oldField.getDefaultValue());
		newField.setDescription(oldField.getDescription());
		newField.setDataType(oldField.getDataType());
		newField.setDateFormat(oldField.getDateFormat());
		newField.setKeyColumn(oldField.isKeyColumn());
		newField.setMandatory(oldField.isMandatory());
		newField.setMaxLength(oldField.getMaxLength());
		newField.setShowDefault(oldField.isShowDefault());
		newField.setTruncWhenLengthExceeds(oldField.isTruncWhenLengthExceeds());
		newField.setLengthExceedsOrientationisRight(oldField.isLengthExceedsOrientationisRight());
	}

	/**
	 * ???ERP??????????????????(???????????????????????????????????????????????????????????????E10,Tiptop)
	 * ??????ERP????????????????????????????????????
	 * 
	 * @param list
	 * @param factoryId
	 * @param serviceConfig
	 * @param contentList
	 * @return
	 * @throws Exception
	 */
	public List<String> getReturnList(List<FoundationObject> list, List<String> factoryId, ERPServiceConfig serviceConfig, List<String> contentList) throws Exception
	{
		return null;
	}

	public String getReturnList(List<FoundationObject> list, String factory, T operation) throws Exception
	{
		return null;
	}

	/**
	 * @param returnXML
	 * @param contentList
	 * @return
	 * @throws Exception
	 */
	public List<FoundationObject> getReturnList(List<String> returnXML, List<String> contentList) throws Exception
	{
		return null;
	}

	protected String getPriceXML(List<FoundationObject> list, List<String> factoryId, Map<String, List<String>> map) throws Exception
	{
		return null;
	}

	/**
	 * ????????????????????????category??????????????????????????????????????????????????????????????????xml??????????????????????????????
	 * (??????????????????)
	 * 
	 * @param category
	 * @param className
	 * @param tableName
	 * @return
	 * @throws ServiceRequestException
	 */
	protected List<ERPFieldMapping> getFieldFromMap(String category, String tableName) throws ServiceRequestException
	{
		if (this.PLMCFColumnMap.get(category) == null || this.PLMCFColumnMap.get(category).get(tableName) == null || this.PLMCFColumnMap.get(category).get(tableName).isEmpty())
		{
			Map<String, List<ERPFieldMapping>> tableFieldMap = new HashMap<String, List<ERPFieldMapping>>();
			this.getFieldListFromXML(category, tableFieldMap);
			if (SetUtils.isNullList(tableFieldMap.get(tableName)))
			{
				throw new IllegalArgumentException("no field mapping for " + category + "." + tableName + " in " + this.xmlPath);
			}
			if (this.shuldSortFields())
			{
				Collections.sort(tableFieldMap.get(tableName), this.fieldComparator);
			}
			this.PLMCFColumnMap.put(category, tableFieldMap);
		}
		return this.PLMCFColumnMap.get(category).get(tableName);
	}

	/**
	 * ???xml?????????????????????????????????category????????????????????????????????????????????????????????????
	 * 
	 * @param usedClassInfo
	 * @param erpClass
	 * @param pdmColumnList
	 * @throws ServiceRequestException
	 */
	private void getFieldListFromXML(String category, Map<String, List<ERPFieldMapping>> fieldMap) throws ServiceRequestException
	{
		Element categoryElement = this.doc.getRootElement().getChild("category").getChild(category);
		if (categoryElement != null)
		{
			Iterator<Element> tableIt = categoryElement.getChildren("table").iterator();
			Element tableEle = null;
			while (tableIt.hasNext())
			{
				tableEle = tableIt.next();
				String tableName = tableEle.getAttributeValue("name");
				Iterator<Element> fieldIt = tableEle.getChildren("mapping").iterator();
				Element mappingEle = null;
				while (fieldIt.hasNext())
				{
					mappingEle = fieldIt.next();
					if (fieldMap.get(tableName) == null)
					{
						fieldMap.put(tableName, new ArrayList<ERPFieldMapping>());
					}
					Map<String, String> paramMap = this.getMapByElement(mappingEle);
					if (!this.existSameField(fieldMap.get(tableName), paramMap.get("ERPField")))
					{
						fieldMap.get(tableName).add(this.getFieldMapping(paramMap));
					}
				}
				mappingEle = null;
				fieldIt = null;
			}
		}
	}

	// /**
	// *
	// * @param baseObj
	// * ???????????????????????????????????????????????????baseObj???StructureObject??????????????????????????????FoundationObject???
	// * @param fieldList
	// * @param category
	// * @param operation
	// * @return
	// * @throws ServiceRequestException
	// */
	// protected Map<String, String> getObjectMapValue(Object baseObj, List<ERPFieldMapping> fieldList,
	// String category, T operation) throws ServiceRequestException
	// {
	// Map<String, String> returnMap = new LinkedHashMap<String, String>();
	// if (fieldList == null)
	// {
	// return returnMap;
	// }
	// for (int j = 0; j < fieldList.size(); j++)
	// {
	// ERPFieldMapping field = fieldList.get(j);
	// String dataValue = this.getFieldValue(baseObj, field, category, operation);
	// this.checkFieldValue(dataValue, field, baseObj);
	// returnMap.put(field.getERPField(), dataValue);
	// }
	// return returnMap;
	// }

	/**
	 * ???????????????????????????ERP
	 * 
	 * @param codeGuidList
	 * @throws ServiceRequestException
	 */
	public void getAllCFDataWithChildren(List<String> codeGuidList) throws ServiceRequestException
	{
		this.beforeProcessDataList();
		this.generateAllData(codeGuidList);
	}

	/**
	 * ???????????????????????????ERP
	 * 
	 * @param codeGuidList
	 * @throws ServiceRequestException
	 */
	protected void generateAllData(List<String> codeGuidList) throws ServiceRequestException
	{
		this.stubService = this.stub.getStubService();
		CodeItemInfo codeItem = null;
		List<CodeItemInfo> codeItemList = new ArrayList<CodeItemInfo>();
		for (String codeguid : codeGuidList)
		{
			codeItem = this.stubService.getEMM().getCodeItem(codeguid);
			if (codeItem != null)
			{
				codeItemList.add(codeItem);
			}
		}
		List<T> operationList = this.getOperationList(false);
		for (int k = 0; k < operationList.size(); k++)
		{
			T operation = operationList.get(k);
			String category = this.getOperationCategory(operation);
			if (StringUtils.isNullString(category))
			{
				throw new IllegalArgumentException("no category attribute for " + operationList.get(k).name() + " in " + this.xmlPath);
			}
			try
			{
				if (category.equals(ERP_CODEITEM))
				{
					this.getCFfiedData(codeItemList, operation);
				}
				else if (category.equals(ERP_FIELDGROUP))
				{
					this.getCFGroupData(codeItemList, operation);
				}
			}
			catch (InvalidDataException e)
			{
				this.addDataError(e);
				if (!this.isTraverseAllInstanceIgnoringError())
				{
					return;
				}
			}
		}
	}

	/**
	 * ??????????????????????????????????????????
	 * 
	 * @param codeItemList
	 * @param operation
	 */
	protected void getCFfiedData(List<CodeItemInfo> codeItemList, T operation) throws ServiceRequestException
	{
	}

	/**
	 * ????????????????????????(????????????)
	 * 
	 * @param codeItemList
	 * @param operation
	 * @throws ServiceRequestException
	 */
	protected void getCFGroupData(List<CodeItemInfo> codeItemList, T operation) throws ServiceRequestException
	{
	}

	/**
	 * e10?????????codeitem?????????????????????nanme
	 * ?????????????????????ERP
	 * 
	 * @param item
	 * @throws ServiceRequestException
	 */
	protected String reNameCode(CodeItemInfo item) throws ServiceRequestException
	{
		CodeItemInfo pCode = item;
		CodeObjectInfo mCode = null;
		StringBuffer nameBuffer = new StringBuffer();
		List<String> nameList = new ArrayList<String>();
		nameList.add(pCode.getName());
		while (pCode.getParentGuid() != null)
		{
			pCode = this.stub.getStubService().getEMM().getCodeItem(pCode.getParentGuid());
			nameList.add(pCode.getName());
		}
		mCode = this.stub.getStubService().getEMM().getCode(pCode.getCodeGuid());
		nameList.add(mCode.getName());
		for (int i = nameList.size() - 1; i > -1; i--)
		{
			nameBuffer.append(nameList.get(i));
		}
		return nameBuffer.toString();
	}

	/**
	 * ??????codeObject
	 * 
	 * @param codeItem
	 * @return
	 * @throws ServiceRequestException
	 */
	protected CodeObjectInfo getCode(CodeItemInfo codeItem) throws ServiceRequestException
	{
		CodeObjectInfo mCode = null;
		if (codeItem != null && codeItem.getCodeGuid() != null)
		{
			mCode = this.stub.getStubService().getEMM().getCode(codeItem.getCodeGuid());
		}
		return mCode;
	}

	public Document getCreateCFDataXML(ERPE10OperationEnum operation, int count, int j)
	{
		return null;
	}

	protected int getERPcnSize()
	{
		if (this.getParameter(null).getParamMap().get("ERPcnSize") != null)
		{
			int cnSize = Integer.valueOf(this.getParameter(null).getParamMap().get("ERPcnSize"));
			if (cnSize > 0)
			{
				return cnSize;
			}
			else
			{
				throw new IllegalArgumentException("the ERPcnSize should larger than 0!");
			}
		}
		return 1;
	}

	protected int getDataLength(String dataValue)
	{
		if (dataValue == null)
		{
			return 0;
		}
		int cnSize = getERPcnSize();
		int size = 0;
		if (cnSize > 1)
		{
			for (char c : dataValue.toCharArray())
			{
				if (CharUtils.isChinese(c))
				{
					size = size + cnSize;
				}
				else
				{
					size = size + 1;
				}
			}
			return size;
		}
		return dataValue.length();
	}

	/**
	 * 
	 * @param value
	 * @param lengthExceedsOrientationisRight
	 *            true:??????????????????
	 * @param maxLength
	 * @return
	 */
	private String ExceedsData(Object value, boolean lengthExceedsOrientationisRight, int maxLength)
	{
		int cnSize = getERPcnSize();
		int size = 0;
		char[] charArray = value.toString().toCharArray();
		StringBuffer buffer = new StringBuffer();
		if (lengthExceedsOrientationisRight)
		{
			for (int i = charArray.length - 1; i >= 0; i--)
			{
				char c = charArray[i];
				if (CharUtils.isChinese(c))
				{
					size = size + cnSize;
				}
				else
				{
					size = size + 1;
				}
				if (size > maxLength)
				{
					break;
				}
				else
				{
					buffer.insert(0, c);
				}
			}
		}
		else
		{
			for (int i = 0; i < charArray.length; i++)
			{
				char c = charArray[i];
				if (CharUtils.isChinese(c))
				{
					size = size + cnSize;
				}
				else
				{
					size = size + 1;
				}
				if (size > maxLength)
				{
					break;
				}
				else
				{
					buffer.append(c);
				}
			}
		}
		return buffer.toString();
	}

	public String filter(String value)
	{
		if (value == null)
		{
			return null;
		}

		char content[] = new char[value.length()];
		value.getChars(0, value.length(), content, 0);
		StringBuffer result = new StringBuffer(content.length + 50);
		for (int i = 0; i < content.length; i++)
		{
			switch (content[i])
			{
			case 60: // '<'
				result.append("&lt;");
				break;

			case 62: // '>'
				result.append("&gt;");
				break;

			case 38: // '&'
				result.append("&amp;");
				break;

			case 34: // '"'
				result.append("&quot;");
				break;

			case 39: // '\''
				result.append("&#39;");
				break;

			default:
				result.append(content[i]);
				break;
			}
		}
		return result.toString();
	}

	/**
	 * ????????????</br>
	 * ?????????????????????/????????????
	 * 
	 * @param stampMap
	 * @param recordSets
	 * @throws ServiceRequestException
	 */
	public List<RecordSet> dealMAData(List<RecordSet> recordSets, boolean isExportAll) throws ServiceRequestException
	{
		List<RecordSet> result = new ArrayList<RecordSet>();
		Map<String, List<RecordSet>> groupedRecords = groupMARecordsByEnd1(recordSets);

		if (!SetUtils.isNullMap(groupedRecords))
		{
			for (String masterguid : groupedRecords.keySet())
			{
				List<RecordSet> recordList = groupedRecords.get(masterguid);
				if (!SetUtils.isNullList(recordList))
				{
					boolean isSend = false;
					for (RecordSet recordSet : recordList)
					{
						String itemkey = recordSet.getItemKey();
						String md5 = StringUtils.generateMd5(recordSet.toString());
						ERPtempData temp = new ERPtempData();
						temp.setITEMGUID(itemkey);
						temp.setSTAMP(md5);
						temp.setJOBGUID(stub.jobGUID);
						temp.setCATEGORY(ERP_MA);
						temp.put("TABLE", stub.tempTableName);
						try
						{
							this.stub.getStubService().insertTempData(temp);
							setActtype(recordSet, true);
							isSend = true;
						}
						catch (Exception e)
						{
							setActtype(recordSet, false);
							temp = this.getERPTempData(ERP_MA, itemkey);
							if (temp.getJOBSTATUS() == -1)
							{
								isSend = true;
								temp.setJOBGUID(stub.jobGUID);
								temp.put("TABLE", stub.tempTableName);
								this.stub.getStubService().updateTempData(temp);
							}
							else if (temp.getJOBSTATUS() == JobStatus.SUCCESSFUL.getValue())
							{
								if (md5.equalsIgnoreCase(temp.getSTAMP()))
								{

								}
								else
								{
									isSend = true;
									temp.setSTAMP(md5);
									temp.setJOBGUID(stub.jobGUID);
									temp.put("TABLE", stub.tempTableName);
									this.stub.getStubService().updateTempData(temp);
								}
							}
							else if (temp.getJOBSTATUS() == JobStatus.FAILED.getValue())
							{
								isSend = true;
								temp.setJOBGUID(stub.jobGUID);
								temp.put("TABLE", stub.tempTableName);
								this.stub.getStubService().updateTempData(temp);
							}
							else
							{
								isSend = true;
								if (!md5.equalsIgnoreCase(temp.getSTAMP()))
								{
									temp.setJOBGUID(stub.jobGUID);
									temp.put("TABLE", stub.tempTableName);
									this.stub.getStubService().updateTempData(temp);
								}
							}
						}
					}
					if (isSend || isExportAll)
					{
						result.addAll(recordList);
					}
				}

			}
		}
		return result;
	}

	/**
	 * ?????????</br>
	 * ???ERP????????????????????????T100?????????????????????????????????????????????????????????PLM????????????????????????????????????????????????</br>
	 * SM???WF???????????????????????????????????????????????????
	 * 
	 * @param stampMap
	 *            ??????????????????masterGuid--stamp
	 * @param recordSets
	 * @throws ServiceRequestException
	 */
	public List<RecordSet> dealReplaceSubstituteData(List<RecordSet> recordSets, boolean isExportAll) throws ServiceRequestException
	{
		List<RecordSet> result = new ArrayList<RecordSet>();
		Map<String, List<RecordSet>> groupedRecords = groupRSRecords(recordSets);
		if (!SetUtils.isNullMap(groupedRecords))
		{// ??????????????????????????????????????????????????????????????????????????????????????????
			for (String key : groupedRecords.keySet())
			{
				List<RecordSet> recordList = groupedRecords.get(key);
				if (!SetUtils.isNullList(recordList))
				{
					boolean isSend = false;
					for (RecordSet recordSet : recordList)
					{
						String itemkey = recordSet.getItemKey();
						String md5 = StringUtils.generateMd5(recordSet.toString());
						ERPtempData temp = new ERPtempData();
						temp.setITEMGUID(itemkey);
						temp.setSTAMP(md5);
						temp.setJOBGUID(stub.jobGUID);
						temp.setCATEGORY(ERP_REPLACESUBSTITUTE);
						temp.put("TABLE", stub.tempTableName);
						try
						{
							this.stub.getStubService().insertTempData(temp);
							setActtype(recordSet, true);
							isSend = true;
						}
						catch (Exception e)
						{
							setActtype(recordSet, false);
							temp = this.getERPTempData(ERP_REPLACESUBSTITUTE, itemkey);
							if (temp.getJOBSTATUS() == -1)
							{
								isSend = true;
								temp.setJOBGUID(stub.jobGUID);
								temp.put("TABLE", stub.tempTableName);
								this.stub.getStubService().updateTempData(temp);
							}
							else if (temp.getJOBSTATUS() == JobStatus.SUCCESSFUL.getValue())
							{
								if (md5.equalsIgnoreCase(temp.getSTAMP()))
								{

								}
								else
								{
									isSend = true;
									temp.setSTAMP(md5);
									temp.setJOBGUID(stub.jobGUID);
									temp.put("TABLE", stub.tempTableName);
									this.stub.getStubService().updateTempData(temp);
								}
							}
							else if (temp.getJOBSTATUS() == JobStatus.FAILED.getValue())
							{
								isSend = true;
								temp.setJOBGUID(stub.jobGUID);
								temp.put("TABLE", stub.tempTableName);
								this.stub.getStubService().updateTempData(temp);
							}
							else
							{
								isSend = true;
								if (!md5.equalsIgnoreCase(temp.getSTAMP()))
								{
									temp.setJOBGUID(stub.jobGUID);
									temp.put("TABLE", stub.tempTableName);
									this.stub.getStubService().updateTempData(temp);
								}
							}
						}
					}
					if (isSend || isExportAll)
					{
						result.addAll(recordList);
					}
				}
			}
		}
		return result;
	}

	/**
	 * ??????????????????????????????Guid+??????Guid?????????
	 * 
	 * @param recordSets
	 * @return
	 * @throws ServiceRequestException
	 */
	protected Map<String, List<RecordSet>> groupRSRecords(List<RecordSet> recordSets) throws ServiceRequestException
	{
		Map<String, List<RecordSet>> recordMap = new HashMap<String, List<RecordSet>>();
		List<RecordSet> tempList = null;
		if (!SetUtils.isNullList(recordSets))
		{
			for (RecordSet record : recordSets)
			{
				String key = record.getGroupKey();
				tempList = recordMap.get(key);
				if (tempList == null)
				{
					tempList = new ArrayList<RecordSet>();
				}
				tempList.add(record);
				recordMap.put(key, tempList);
			}
		}
		return recordMap;
	}

	/**
	 * ?????????????????????????????????????????????????????? </br>
	 * ?????????????????????????????????????????????????????????????????????
	 * 
	 * @param recordSets
	 * @return
	 * @throws ServiceRequestException
	 */
	protected Map<String, List<RecordSet>> groupMARecordsByEnd1(List<RecordSet> recordSets) throws ServiceRequestException
	{
		Map<String, List<RecordSet>> recordMap = new HashMap<String, List<RecordSet>>();
		List<RecordSet> tempList = null;
		if (!SetUtils.isNullList(recordSets))
		{
			for (RecordSet record : recordSets)
			{
				// ?????????????????????????????????
				// ?????????????????????????????????
				String key = record.getGroupKey();
				tempList = recordMap.get(key);
				if (tempList == null)
				{
					tempList = new ArrayList<RecordSet>();
				}
				tempList.add(record);
				recordMap.put(key, tempList);
			}
		}
		return recordMap;
	}

	protected StructureObject getStructureObject(ObjectGuid objectGuid) throws ServiceRequestException
	{
		StructureObject object = null;
		if (objectGuid != null)
		{
			SearchCondition condition = SearchConditionFactory.createSearchConditionForStructure(objectGuid.getClassName());
			object = this.stub.getStubService().getBOAS().getStructureObject(objectGuid, condition);
		}
		return object;
	}

	/**
	 * ?????????????????????????????????
	 * 
	 * @param tempDataList
	 * @param recordMap
	 * @param stampMap
	 * @param isBOM
	 * @return
	 * @throws ServiceRequestException
	 */

	private List<RecordSet> dealData(String category, List<RecordSet> recordSets, boolean isExportAll) throws ServiceRequestException
	{
		List<RecordSet> result = new ArrayList<RecordSet>();
		for (RecordSet recordSet : recordSets)
		{
			String key = getSetKeyMguid(recordSet, false);
			String md5 = StringUtils.generateMd5(recordSet.toString());
			ERPtempData temp = new ERPtempData();
			temp.setITEMGUID(key);
			temp.setSTAMP(md5);
			temp.setJOBGUID(stub.jobGUID);
			temp.setCATEGORY(category);
			temp.put("TABLE", stub.tempTableName);
			boolean isSend = false;
			try
			{
				this.stub.getStubService().insertTempData(temp);
				setActtype(recordSet, true);
				isSend = true;
			}
			catch (Exception e)
			{
				setActtype(recordSet, false);
				temp = this.getERPTempData(category, key);
				if (temp.getJOBSTATUS() == -1)
				{
					isSend = true;
					temp.setJOBGUID(stub.jobGUID);
					temp.put("TABLE", stub.tempTableName);
					this.stub.getStubService().updateTempData(temp);
				}
				else if (temp.getJOBSTATUS() == JobStatus.SUCCESSFUL.getValue())
				{
					if (md5.equalsIgnoreCase(temp.getSTAMP()))
					{
						isSend = false;
					}
					else
					{
						isSend = true;
						temp.setSTAMP(md5);
						temp.setJOBGUID(stub.jobGUID);
						temp.put("TABLE", stub.tempTableName);
						this.stub.getStubService().updateTempData(temp);
					}
				}
				else if (temp.getJOBSTATUS() == JobStatus.FAILED.getValue())
				{
					isSend = true;
					temp.setJOBGUID(stub.jobGUID);
					temp.put("TABLE", stub.tempTableName);
					this.stub.getStubService().updateTempData(temp);
				}
				else
				{
					isSend = true;
					if (!md5.equalsIgnoreCase(temp.getSTAMP()))
					{
						temp.setJOBGUID(stub.jobGUID);
						temp.put("TABLE", stub.tempTableName);
						this.stub.getStubService().updateTempData(temp);
					}
				}
			}
			if (isSend || isExportAll)
			{
				result.add(recordSet);
			}
		}
		return result;
	}

	/**
	 * ????????????</br>
	 * ???schema?????????????????????????????????????????????????????????
	 * 
	 * @throws ServiceRequestException
	 */
	public void compareWithDB(boolean isExportAll) throws ServiceRequestException
	{
		List<T> operationList = getOperationList(true);
		if (!SetUtils.isNullList(operationList))
		{
			for (T operation : operationList)
			{
				String category = getOperationCategory(operation);
				List<RecordSet> comparedData = new ArrayList<RecordSet>();
				List<RecordSet> recordSets = this.dataMap.get(category);
				if (!SetUtils.isNullList(recordSets))
				{
					if (ERP_MA.equals(category))
					{
						comparedData = dealMAData(recordSets, isExportAll);
					}
					else if (ERP_REPLACESUBSTITUTE.equals(category))
					{
						comparedData = dealReplaceSubstituteData(recordSets, isExportAll);
					}
					else
					{
						if (ERP_ITEM.equals(category))
						{
							comparedData = dealData(category, recordSets, isExportAll);
						}
						else
						{
							comparedData = dealData(category, recordSets, isExportAll);
						}
					}
					if (SetUtils.isNullList(comparedData))
					{
						this.dataMap.put(category, null);
					}
					else
					{
						this.dataMap.put(category, comparedData);
					}
				}
			}
		}
	}

	protected void setActtype(RecordSet recordSet, boolean b)
	{

	}

	private String getSetKeyMguid(RecordSet set, boolean isBOM) throws ServiceRequestException
	{
		ObjectGuid oguid = set.getObjectGuid();
		if (!isBOM)
		{
			return oguid.getMasterGuid();
		}
		ClassInfo info = stub.getStubService().getEMM().getClassByGuid(oguid.getClassGuid());
		if (info.hasInterface(ModelInterfaceEnum.IItem))
		{
			return oguid.getMasterGuid();
		}
		else if (info.hasInterface(ModelInterfaceEnum.IBOMView))
		{
			BOMView view = this.stub.getStubService().getBOMS().getBOMView(oguid);
			if (view != null)
			{
				return view.getEnd1ObjectGuid().getMasterGuid();
			}
		}
		return null;
	}

	/**
	 * ?????????????????????operation?????????????????????</br>
	 * ??????????????????????????????????????????????????????????????????????????????????????????????????????2????????????
	 * 
	 * @param isafter
	 *            ???????????????????????????
	 * @param map
	 * @return
	 * @throws ServiceRequestException
	 */
	public Map<String, Boolean> getOperationDataEmptyFlag(boolean isafter, Map<String, Boolean> map) throws ServiceRequestException
	{
		if (SetUtils.isNullMap(map))
		{
			map = new HashMap<String, Boolean>();
		}
		List<T> operationList = this.getOperationList(true);
		for (int k = 0; k < operationList.size(); k++)
		{
			T operation = operationList.get(k);
			String category = this.getOperationCategory(operation);
			if (StringUtils.isNullString(category))
			{
				throw new IllegalArgumentException("no category attribute for " + operationList.get(k).name() + " in " + this.xmlPath);
			}
			if (ERP_ITEM.equals(category))
			{
				if (this.dataMap.get(ERP_ITEM) == null || this.dataMap.get(ERP_ITEM).isEmpty())
				{
					setEmptyFlag(isafter, category, map);
				}
			}
			else if (ERP_BOM.equals(category))
			{
				if (this.dataMap.get(ERP_BOM) == null || this.dataMap.get(ERP_BOM).isEmpty())
				{
					setEmptyFlag(isafter, category, map);
				}
			}
			else if (ERP_REPLACESUBSTITUTE.equals(category))
			{
				if (this.dataMap.get(ERP_REPLACESUBSTITUTE) == null || this.dataMap.get(ERP_REPLACESUBSTITUTE).isEmpty())
				{
					setEmptyFlag(isafter, category, map);
				}
			}
			else if (ERP_MA.equals(category))
			{
				if (this.dataMap.get(ERP_MA) == null || this.dataMap.get(ERP_MA).isEmpty())
				{
					setEmptyFlag(isafter, category, map);
				}
			}
		}
		return map;
	}

	/**
	 * ????????????????????????true????????????????????????false,??????????????????category?????????
	 * 
	 * @param isafter
	 * @param category
	 * @param map
	 */
	private void setEmptyFlag(boolean isafter, String category, Map<String, Boolean> map)
	{
		if (!isafter)
		{
			map.put(category, true);
		}
		else
		{
			if (map.get(category) == null)
			{
				map.put(category, false);
			}
		}
	}

	protected ERPtempData getERPTempData(String category, String guid) throws ServiceRequestException
	{
		Map<String, Object> paraMap = new HashMap<String, Object>();
		paraMap.put("TABLE", this.stub.tempTableName);
		paraMap.put("CATEGORY", category);
		paraMap.put("ITEMGUID", guid);

		List<ERPtempData> list = stub.getStubService().listERPTempData(paraMap);
		if (SetUtils.isNullList(list))
		{
			return null;
		}
		return list.get(0);

	}
}

/**
 * ???????????????fieldName & delegateFieldName.
 * fieldName: ?????????????????????<br/>
 * delegateFieldName: ???fieldName???Object???Code????????????delegateFieldName???????????????????????????delegateFieldName???null???<br/>
 * 
 * <p/>
 * ?????????<br/>
 * +????????????????????????<br/>
 * |????????????????????????Object???Code???Classification?????????????????????Object,Code,Classification??????????????? ?????????Code,Classification??????????????????Code, title????????????
 * <br/>
 * end1$XX, end2$XX???????????????end1???end2??????XX??????<br/>
 * ????????????????????????????????????????????????????????????type|code+end1$type|title+end2$name ?????????type?????????code??? ?????? end1???type?????????title??? ??????
 * end2???name??????????????????????????????JointField????????????
 **/
class JointField
{
	public static final String	MERGE_SIGN		= "+";
	public static final String	DELEGATE_SIGN	= "|";

	private final String		fieldName;
	private final String		delegateFieldName;

	public JointField(String fieldName, String delegateFieldName)
	{
		this.fieldName = fieldName;
		this.delegateFieldName = delegateFieldName;
	}

	public String getFieldName()
	{
		return this.fieldName;
	}

	public String getDelegateFieldName()
	{
		return this.delegateFieldName;
	}

	public boolean isEnd1Field()
	{
		return this.getFieldName().matches("((?i)end1\\$).*");
	}

	public boolean isEnd2Field()
	{
		return this.getFieldName().matches("((?i)end2\\$).*");
	}

	/**
	 * ??????End1$, End2$????????????????????????
	 * 
	 * @return
	 */
	public String getClearFieldName()
	{
		if (this.isEnd1Field() || this.isEnd2Field())
		{
			return this.getFieldName().substring(5);
		}
		return this.getFieldName();
	}

	public String getName()
	{
		if (this.delegateFieldName != null)
		{
			return this.fieldName + DELEGATE_SIGN + this.delegateFieldName;
		}
		else
		{
			return this.fieldName;
		}
	}

	@Override
	public String toString()
	{
		return this.getName();
	}

	public static List<JointField> getFiels(String name)
	{
		List<JointField> fieldList = new ArrayList<JointField>();
		// + |???????????????????????????????????????
		String[] jointArr = name.split("\\" + MERGE_SIGN);
		for (String f : jointArr)
		{
			String[] sepArr = f.split("\\" + DELEGATE_SIGN);
			// sepArr.length????????????2????????????Object?????????????????????????????????Object??????????????????Code???????????????????????????????????????objectField|stock_unit|title
			// ?????????stock_unit|title??????????????????delegateField
			if (sepArr.length >= 2)
			{
				fieldList.add(new JointField(sepArr[0], f.substring(sepArr[0].length() + 1)));
			}
			else
			{
				fieldList.add(new JointField(sepArr[0], null));
			}
		}
		return fieldList;
	}
}

/**
 * ?????????<br/>
 * ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????? <br/>
 * ???????????????size==1,?????????BOM???????????????????????????size>1
 * 
 * @see LinkedList
 * @see Map
 * @author chega
 * 
 *         2012-12-27??????2:34:26
 * 
 */
class TableRecordData extends LinkedList<Map<String, String>>
{
	public final static TableRecordData	EMPTY_TABLE_RECORD	= new TableRecordData();

	public TableRecordData(Map<String, String> record)
	{
		super();
		if (record != null && !record.isEmpty())
		{
			this.add(record);
		}
	}

	public TableRecordData()
	{
		this(null);
	}

	@Override
	public boolean add(Map<String, String> e)
	{
		if (e == null || e.isEmpty())
		{
			return false;
		}
		if (!this.exist(e))
		{
			return super.add(e);
		}
		return false;
	}

	/**
	 * ?????????BOM????????????????????????BOMStructure?????????????????????????????????BOMStructure????????????????????????????????????
	 * ??????????????????????????????????????????????????????????????????????????????????????????<br/>
	 * ???????????????????????????BOM???????????????????????????true??????????????????????????????false
	 * 
	 * @param e
	 * @return
	 */
	public boolean exist(Map<String, String> e)
	{
		for (Map<String, String> r : this)
		{
			if (e.equals(r))
			{
				return true;
			}
			break;
		}
		return false;
	}

	/**
	 * @param compoundField
	 *            ?????????????????????????????????(ERPField)?????????????????????????????? <br/>
	 * <br/>
	 *            ?????????????????????map
	 * @return
	 */
	public String getCompoundFieldValue(String compoundField)
	{
		String[] fieldArr = compoundField.split("\\s*,\\s*");
		String compoundValue = "";
		for (String field : fieldArr)
		{
			compoundValue += "," + this.get(0).get(field);
		}
		return compoundValue.equals("") ? compoundValue : compoundValue.substring(1);
	}

	/**
	 * ?????????????????????????????????????????????E10?????????primaryKey????????????
	 * 
	 * @param compoundField
	 * @return
	 */
	public boolean existDuplicatedValueForFields(String compoundField)
	{
		int size = this.size();
		Set<String> set = new HashSet<String>();
		set.add(this.getCompoundFieldValue(compoundField));
		boolean exist = set.size() == size;
		set.clear();
		return exist;
	}
}

/**
 * ?????????????????????????????????????????????????????????(RecordSet)??? ??????ERP?????????????????????????????? ,????????????????????????BOM???????????????????????????<br/>
 * ??????RecordSet??????tableName???TableRecordData???????????????
 * 
 * @author chega
 * 
 *         2012-12-27??????2:39:26
 * @see Map
 * @see TableRecordData
 */
class RecordSet extends HashMap<String, TableRecordData>
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -504707286884283318L;

	/**
	 * ??????????????????????????????objectGuid,?????????bomview??????bomview???objectGuid???????????????????????????end1???ObjectGuid, ??????????????????????????????ObjectGuid
	 */
	private ObjectGuid			objectGuid			= null;

	private String				groupKey			= null;

	private String				itemKey				= null;

	public RecordSet()
	{
	}

	public RecordSet(ObjectGuid obejctGuid)
	{
		this.objectGuid = obejctGuid;
	}

	public static RecordSet emptySet(ObjectGuid objectGuid)
	{
		return new RecordSet(objectGuid);
	}

	public ObjectGuid getObjectGuid()
	{
		return this.objectGuid;
	}

	public String getGroupKey()
	{
		return groupKey;
	}

	public void setGroupKey(String groupKey)
	{
		this.groupKey = groupKey;
	}

	public String getItemKey()
	{
		return itemKey;
	}

	public void setItemKey(String itemKey)
	{
		this.itemKey = itemKey;
	}

	/**
	 * ????????????????????????
	 * 
	 * @param tableName
	 * @param stub
	 *            ??????????????????????????????dataKey???XMLPackageNo
	 * @return
	 * @see TableRecordData
	 */
	public TableRecordData getTableData(String tableName, ERPTransferStub<? extends Enum<?>> stub)
	{
		TableRecordData record = this.get(tableName);
		if (record == null)
		{
			return TableRecordData.EMPTY_TABLE_RECORD;
		}
		return this.replaceBuildinField(record, stub);
	}

	/**
	 * ????????? ${DataKey} ??? ${XMLPackageNo}????????????????????????
	 * 
	 * @param record
	 * @return
	 */
	private TableRecordData replaceBuildinField(TableRecordData record, ERPTransferStub<? extends Enum<?>> stub)
	{
		if (stub != null && stub.currentOpeIdx != -1 && stub.currentXMLPackIdx != 0)
		{
			for (Map<String, String> map : record)
			{
				Iterator<String> keyIt = map.keySet().iterator();
				String key = null;
				while (keyIt.hasNext())
				{
					key = keyIt.next();
					if (map.get(key).contains(IntegrateERP.RESERVED_DATAKEY))
					{
						map.put(key, map.get(key).replace(IntegrateERP.RESERVED_DATAKEY, stub.getDataKey()));
					}
					if (map.get(key).contains(IntegrateERP.RESERVED_XMLPACKAGENO))
					{
						map.put(key, map.get(key).replace(IntegrateERP.RESERVED_XMLPACKAGENO, stub.getXMLPackNo()));
					}
				}
			}
		}
		return record;
	}

	@Override
	public TableRecordData put(String tableName, TableRecordData tableRecord)
	{
		if (tableRecord.isEmpty())
		{
			return null;
		}
		if (this.get(tableName) != null)
		{
			throw new IllegalArgumentException("can't add TableRecordData with same tableName to RecordSet. tableName: " + tableName);
		}
		return super.put(tableName, tableRecord);
	}
}

class ERPFieldComparator implements Comparator<ERPFieldMapping>
{
	@Override
	public int compare(ERPFieldMapping o1, ERPFieldMapping o2)
	{
		return o1.getERPField().compareTo(o2.getERPField());
	}
}

/**
 * ????????????????????????????????????????????????????????????????????????????????????????????????????????????
 * 
 * @author chega
 *         2013-5-22
 */
class InvalidDataException extends RuntimeException
{
	InvalidDataException(String msg)
	{
		super(msg);
	}
}

/**
 * ??????ERPFieldMapping?????????<br/>
 * ?????????TableRecord??????ERPFieldMapping<br/>
 * ?????????????????????
 * 
 * @author zhoux
 * 
 */
class ERPFieldMappingContainer
{
	/**
	 * ???-ERP??????????????????<br/>
	 */
	private Map<String, List<ERPFieldMapping>>	fieldMappingMap	= new HashMap<String, List<ERPFieldMapping>>();
	/**
	 * ???-???Set??????<br/>
	 * ????????????ERPFieldMapping??????????????????????????????????????????????????????????????????????????????
	 */
	protected Map<String, Set<String>>			classCheckMap	= new HashMap<String, Set<String>>();
	/**
	 * ???-??????Set??????<br/>
	 * ????????????ERPFieldMapping????????????????????????
	 */
	protected Map<String, Set<String>>			fieldCheckMap	= new HashMap<String, Set<String>>();

	/**
	 * ??????????????????????????????ERP???????????????????????????????????????
	 * 
	 * @param tableName
	 *            ??????(????????????)
	 * @param className
	 *            ??????(????????????????????????????????????????????????)
	 * @param erpFieldMappingList
	 */
	public void put(String tableName, String className, List<ERPFieldMapping> erpFieldMappingList)
	{
		if (StringUtils.isNullString(tableName))
		{
			throw new IllegalArgumentException("tableName can not empty");
		}
		className = StringUtils.convertNULLtoString(className);
		Set<String> classNameSet = this.classCheckMap.get(tableName);
		if (classNameSet == null)
		{
			classNameSet = new HashSet<String>();
			this.classCheckMap.put(tableName, classNameSet);
		}
		if (classNameSet.add(className.toLowerCase()))
		{
			List<ERPFieldMapping> tableFieldList = this.fieldMappingMap.get(tableName);
			if (tableFieldList == null)
			{
				tableFieldList = new ArrayList<ERPFieldMapping>();
				this.fieldMappingMap.put(tableName, tableFieldList);
			}
			if (!SetUtils.isNullList(erpFieldMappingList))
			{
				Set<String> fieldNameSet = this.fieldCheckMap.get(tableName);
				if (fieldNameSet == null)
				{
					fieldNameSet = new HashSet<String>();
					this.fieldCheckMap.put(tableName, fieldNameSet);
				}
				for (ERPFieldMapping erpFieldMapping : erpFieldMappingList)
				{
					if (fieldNameSet.add(erpFieldMapping.getERPField()))
					{
						tableFieldList.add(erpFieldMapping);
					}
				}
			}
		}
	}

	/**
	 * ????????????????????????(????????????????????????????????????????????????)
	 * 
	 * @param tableName
	 * @return
	 */
	public List<ERPFieldMapping> get(String tableName)
	{
		return this.fieldMappingMap.get(tableName);
	}

	/**
	 * ????????????
	 */
	public void clear()
	{
		this.fieldMappingMap.clear();
		this.classCheckMap.clear();
		this.fieldCheckMap.clear();
	}

}