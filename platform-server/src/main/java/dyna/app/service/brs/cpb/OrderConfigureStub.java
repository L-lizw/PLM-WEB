package dyna.app.service.brs.cpb;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.emm.ClassStub;
import dyna.app.service.helper.ServiceRequestExceptionWrap;
import dyna.common.SearchCondition;
import dyna.common.SearchConditionFactory;
import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.bean.data.StructureObject;
import dyna.common.bean.data.configparamter.*;
import dyna.common.bean.data.foundation.ViewObject;
import dyna.common.dto.DataRule;
import dyna.common.dto.model.bmbo.BMInfo;
import dyna.common.dto.model.bmbo.BOInfo;
import dyna.common.dto.model.cls.ClassInfo;
import dyna.common.dto.model.code.CodeItemInfo;
import dyna.common.dto.model.ui.UIField;
import dyna.common.dto.model.ui.UIObjectInfo;
import dyna.common.dto.template.relation.RelationTemplateEnd2;
import dyna.common.dto.template.relation.RelationTemplateInfo;
import dyna.common.exception.DynaDataException;
import dyna.common.exception.ParameterizedException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.systemenum.*;
import dyna.common.util.*;
import dyna.net.service.data.SystemDataService;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class OrderConfigureStub extends AbstractServiceStub<CPBImpl>
{

	public List<BOInfo> listOrderBoinfo() throws ServiceRequestException
	{
		BMInfo bmInfo = this.stubService.getEmm().getCurrentBizModel();
		if (bmInfo != null && bmInfo.getGuid() != null)
		{
			SystemDataService sds = this.stubService.getSystemDataService();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(OrderSearchClass.BMGUID, bmInfo.getGuid());
			List<OrderSearchClass> list = sds.query(OrderSearchClass.class, map);
			if (!SetUtils.isNullList(list))
			{
				List<BOInfo> listBoInfo = new ArrayList<BOInfo>();
				for (OrderSearchClass orb : list)
				{
					BOInfo boinfo = this.stubService.getEmm().getBoInfoByNameAndBM(bmInfo.getGuid(), orb.getBoInfoName());
					if (boinfo != null)
					{
						listBoInfo.add(boinfo);
					}
				}
				return listBoInfo;
			}
		}
		return null;
	}

	public void saveOrderBoinfo(List<BOInfo> value) throws ServiceRequestException
	{
		SystemDataService sds = this.stubService.getSystemDataService();
		try
		{
			BMInfo bmInfo = this.stubService.getEmm().getCurrentBizModel();
//			DataServer.getTransactionManager().startTransaction(this.stubService.getFixedTransactionId());

			if (bmInfo != null && bmInfo.getGuid() != null)
			{
				// delete
				this.deleteOrderConfigure(bmInfo.getGuid());
				if (!SetUtils.isNullList(value))
				{
					for (BOInfo ordervalue : value)
					{
						OrderSearchClass orc = new OrderSearchClass();
						orc.setBMGuid(bmInfo.getGuid());
						orc.setBoInfoName(ordervalue.getName());
						sds.save(orc);
					}
				}
			}
//			DataServer.getTransactionManager().commitTransaction();
		}
		catch (DynaDataException e)
		{
//			DataServer.getTransactionManager().rollbackTransaction();
			throw ServiceRequestExceptionWrap.createByDynaDataException(this.stubService, e);
		}
		catch (Exception e)
		{
//			DataServer.getTransactionManager().rollbackTransaction();
			if (e instanceof ServiceRequestException)
			{
				throw (ServiceRequestException) e;
			}
			else
			{
				throw ServiceRequestException.createByException("ID_APP_SERVER_EXCEPTION", e);
			}
		}
	}

	private void deleteOrderConfigure(String bmGuid)
	{
		SystemDataService sds = this.stubService.getSystemDataService();
		if (StringUtils.isGuid(bmGuid))
		{
			Map<String, Object> filter = new HashMap<String, Object>();
			filter.put(OrderSearchClass.BMGUID, bmGuid);
			sds.delete(OrderSearchClass.class, filter, "delete");
		}
	}

	// ????????????????????????
	protected void saveDriveResult4Order(ObjectGuid objectGuid, DataRule dataRule, int level) throws ServiceRequestException
	{
		FoundationObject contract = this.stubService.getBoas().getObject(objectGuid);
		if (contract == null)
		{
			throw new ServiceRequestException("ID_DS_NO_DATA", "contract is not exist, guid='" + objectGuid.getGuid() + "'");
		}

		ViewObject viewObject = this.stubService.getBoas().getRelationByEND1(objectGuid, ConfigParameterConstants.CONFIG_PARAMETER_ORDELDETAIL_TEMPLATE_NAME);
		if (viewObject == null)
		{
			return;
		}

		// ???????????????????????????????????????
		List<FoundationObject> end2List = this.listOrderDetail(objectGuid, viewObject.getObjectGuid());
		if (SetUtils.isNullList(end2List))
		{
			return;
		}

		String templateName = ConfigParameterConstants.CONFIG_PARAMETER_RESULTRELATION_TEMPLATE_NAME;
		RelationTemplateInfo relationTemplate = this.stubService.getEmm().getRelationTemplateByName(objectGuid, templateName);
		if (relationTemplate == null)
		{
			throw new ServiceRequestException("ID_APP_NO_RELATION_TEMPLATE", "no relation template:" + templateName, null, templateName);
		}

		List<FoundationObject> itemList = new ArrayList<FoundationObject>();
		try
		{
//			DataServer.getTransactionManager().startTransaction(this.stubService.getFixedTransactionId());
			for (FoundationObject orderDetail : end2List)
			{
				FoundationObject item = this.checkOrderDetailItem(objectGuid, orderDetail, null, relationTemplate, viewObject.getCreateTime());
				if (item != null)
				{
					itemList.add(item);

					this.drivenByItem(item, itemList, dataRule, 0);
				}
			}
//			DataServer.getTransactionManager().commitTransaction();
		}
		catch (DynaDataException e)
		{
//			DataServer.getTransactionManager().rollbackTransaction();
			throw ServiceRequestExceptionWrap.createByDynaDataException(this.stubService, e);
		}
		catch (Exception e)
		{
//			DataServer.getTransactionManager().rollbackTransaction();
			if (e instanceof ServiceRequestException)
			{
				throw (ServiceRequestException) e;
			}
			else
			{
				throw ServiceRequestException.createByException("ID_APP_SERVER_EXCEPTION", e);
			}
		}
	}

	private void drivenByItem(FoundationObject item, List<FoundationObject> itemList, DataRule dataRule, int level) throws ServiceRequestException
	{
		FoundationObject draw = this.getDrawInstanceByItem(item, dataRule.getLocateTime());
		List<StructureObject> strucList_ = this.driveResult4Order(item, draw, dataRule, null);
		level++;
		if (!SetUtils.isNullList(strucList_))
		{
			for (StructureObject structureObject : strucList_)
			{
				FoundationObject item_ = structureObject.getEnd2UIObject();
				itemList.add(item);

				this.drivenByItem(item_, itemList, dataRule, level);
			}
		}
	}

	private void contractCheck(FoundationObject contract) throws ServiceRequestException
	{
	}

	protected List<List<String>> check4Order(ObjectGuid contractObjectGuid, DataRule dataRule, boolean allCheck) throws ServiceRequestException
	{
		String templateName = ConfigParameterConstants.CONFIG_PARAMETER_RESULTRELATION_TEMPLATE_NAME;
		RelationTemplateInfo relationTemplate = this.stubService.getEmm().getRelationTemplateByName(contractObjectGuid, templateName);
		if (relationTemplate == null)
		{
			throw new ServiceRequestException("ID_APP_NO_RELATION_TEMPLATE", "no relation template:" + templateName, null, templateName);
		}

		FoundationObject contract = this.stubService.getBoas().getObject(contractObjectGuid);
		if (contract == null)
		{
			throw new ServiceRequestException("ID_DS_NO_DATA", "contract is not exist, guid='" + contractObjectGuid.getGuid() + "'");
		}

		ViewObject viewObject = this.stubService.getBoas().getRelationByEND1(contractObjectGuid, ConfigParameterConstants.CONFIG_PARAMETER_ORDELDETAIL_TEMPLATE_NAME);
		if (viewObject == null)
		{
			return null;
		}

		// ???????????????????????????????????????
		List<FoundationObject> end2List = this.listOrderDetail(contractObjectGuid, viewObject.getObjectGuid());
		if (SetUtils.isNullList(end2List))
		{
			throw new ServiceRequestException("ID_APP_CONFIG_CONTRACT_HAVE_NO_ORDER", "contract have no order exist, contract no ='" + contract.getId() + "'", null,
					contract.getId());
		}

		// ??????????????????
		this.contractCheck(contract);

		List<StructureObject> orderBomList = this.listOrderBom(contract.getObjectGuid());

		ViewObject orderBOMView = this.stubService.getCPBStub().getViewObject(contractObjectGuid, templateName, true);

		List<List<String>> logList = new ArrayList<List<String>>();
		List<FoundationObject> itemList = new ArrayList<FoundationObject>();
		try
		{
//			DataServer.getTransactionManager().startTransaction(this.stubService.getFixedTransactionId());
			for (FoundationObject order : end2List)
			{
				ClassStub.decorateObjectGuid(order.getObjectGuid(), this.stubService);

				if (!allCheck)
				{
					String orderItem = (String) order.get(ConfigParameterConstants.ORDERITEM);
					if (StringUtils.isGuid(orderItem))
					{
						continue;
					}
				}

				// ??????
				this.orderCheck(order);

				// ??????????????????????????????
				FoundationObject draw = this.getDrawInstanceByOrderDetail(order.getId(), null);
				FoundationObject item = null;
				if (draw == null)
				{
					StructureObject struc = (StructureObject) order.get(StructureObject.STRUCTURE_CLASS_NAME);
					List<String> tmpList = new ArrayList<String>();
					tmpList.add(struc.getSequence());
					tmpList.add(order.getFullName());
					tmpList.add(this.getMessage("ID_APP_CONFIG_DRAW_NOT_EXIST", order.getId()));
					logList.add(tmpList);
				}
				else
				{
					// ???????????????????????????
					if (this.isEscape(contract, order))
					{
						continue;
					}

					order.put("NAME$", draw.get("NAME$"));// ????????????

					// ???????????????????????????
					Date ruleTime = null;
					if (dataRule == null)
					{
						if (!draw.isLatestRevision())
						{
							ruleTime = draw.getReleaseTime();
						}
					}
					else
					{
						ruleTime = dataRule.getLocateTime();
					}

					// ????????????????????????
					List<List<String>> logList_ = this.checkVariableOfOrder(order, draw, ruleTime);

					if (!SetUtils.isNullList(logList_))
					{
						logList.addAll(logList_);
						continue;
					}

					item = this.checkOrderDetailItem(contract.getObjectGuid(), order, draw, relationTemplate, null);
				}

				// ????????????
				if (item != null)
				{
					itemList.add(item);

					// ???????????????????????????
					this.linkDrawToItem(item, draw);

					// ????????????????????????
					this.updateOrder(contract, order, item);

					// ????????????????????????????????????????????????
					this.linkToContract(contract, order, item, orderBOMView, orderBomList);
				}
				else
				{
					StructureObject struc = (StructureObject) order.get(StructureObject.STRUCTURE_CLASS_NAME);
					List<String> tmpList = new ArrayList<String>();
					tmpList.add(struc.getSequence());
					tmpList.add(order.getFullName());
					tmpList.add(this.getMessage("ID_APP_CONFIG_HT_CHECK_ITEM_NOT_EXIST"));
					logList.add(tmpList);
				}
			}
//			DataServer.getTransactionManager().commitTransaction();
		}
		catch (DynaDataException e)
		{
//			DataServer.getTransactionManager().rollbackTransaction();
			throw ServiceRequestExceptionWrap.createByDynaDataException(this.stubService, e);
		}
		catch (Exception e)
		{
//			DataServer.getTransactionManager().rollbackTransaction();
			if (e instanceof ServiceRequestException)
			{
				List<String> logs = new ArrayList<String>();
				logs.add(" ");
				logs.add(e.getMessage());
				logs.add(this.stubService.getMsrm().getMSRString(((ParameterizedException) e).getMsrId(), this.stubService.getUserSignature().getLanguageEnum().toString(),
						((ServiceRequestException) e).getArgs()));
				logList.add(logs);
				throw (ServiceRequestException) e;
			}
			else
			{
				throw ServiceRequestException.createByException("ID_APP_SERVER_EXCEPTION", e);
			}
		}

		return logList;
	}

	private List<StructureObject> listOrderBom(ObjectGuid objectGuid) throws ServiceRequestException
	{
		String templateName = ConfigParameterConstants.CONFIG_PARAMETER_RESULTRELATION_TEMPLATE_NAME;
		RelationTemplateInfo relationTemplate = this.stubService.getEmm().getRelationTemplateByName(objectGuid, templateName);
		SearchCondition sc = SearchConditionFactory.createSearchConditionForStructure(relationTemplate.getStructureClassName());
		sc.addResultField("DATASEQ");

		List<StructureObject> list = this.stubService.getBoas().listObjectOfRelation(objectGuid, templateName, sc, null, null);
		if (list == null)
		{
			list = new ArrayList<StructureObject>();
		}
		return list;
	}

	private int getMaxSequenceOfBOM(List<StructureObject> orderBomList) throws ServiceRequestException
	{
		int maxSequence = 0;
		if (!SetUtils.isNullList(orderBomList))
		{
			for (StructureObject structureObject : orderBomList)
			{
				String sequence = structureObject.getSequence();
				if (NumberUtils.isNumeric(sequence))
				{
					int sequence_ = Integer.valueOf(sequence);
					if (maxSequence < sequence_)
					{
						maxSequence = sequence_;
					}
				}
			}
		}
		return maxSequence;
	}

	private void linkToContract(FoundationObject contract, FoundationObject order, FoundationObject item, ViewObject viewObject, List<StructureObject> orderBomList)
			throws ServiceRequestException
	{
		int maxSequence = this.getMaxSequenceOfBOM(orderBomList);

		String templateName = ConfigParameterConstants.CONFIG_PARAMETER_RESULTRELATION_TEMPLATE_NAME;
		RelationTemplateInfo relationTemplate = this.stubService.getEmm().getRelationTemplateByName(contract.getObjectGuid(), templateName);
		StructureObject structureObject = this.stubService.getBoas().newStructureObject(relationTemplate.getStructureClassGuid(), relationTemplate.getStructureClassName());
		structureObject.put("Quantity", order.get("Quantity"));// ????????????????????????????????????????????????-?????????????????????
		structureObject.setSequence(String.valueOf(++maxSequence));
		structureObject.put(ConfigParameterConstants.STRUC_ORDER_DETAIL, order.getObjectGuid().getGuid());
		structureObject.put(ConfigParameterConstants.STRUC_ORDER_DETAIL + "$CLASS", order.getObjectGuid().getClassGuid());
		structureObject.put(ConfigParameterConstants.STRUC_ORDER_DETAIL + "$MASTER", order.getObjectGuid().getMasterGuid());

		this.stubService.linkNoCheckCycle(viewObject, contract, item.getObjectGuid(), structureObject);

		orderBomList.add(structureObject);
	}

	/**
	 * ?????????????????????????????????
	 * 
	 * @param contract
	 * @param order
	 * @return
	 * @throws ServiceRequestException
	 */
	private boolean isEscape(FoundationObject contract, FoundationObject order) throws ServiceRequestException
	{
		return false;
	}

	/**
	 * ???????????????????????????????????????
	 * 
	 * @param order
	 * @return
	 * @throws ServiceRequestException
	 */
	private List<List<String>> orderCheck(FoundationObject order) throws ServiceRequestException
	{
		List<List<String>> logList = new ArrayList<List<String>>();
		// List<String> errList = new ArrayList<String>();
		// errList.add(struc.getSequence());
		// errList.add(order.getFullName());
		// errList.add(this.getMessage("ID_APP_CONFIG_ORDER_HT_VDA_ERROR", order.getId()));
		// logList.add(errList);
		return logList;
	}

	/**
	 * ??????L???????????????????????????
	 * 
	 * @param configVariable
	 * @param gNumber
	 * @param lNumbers
	 * @return
	 * @throws ServiceRequestException
	 */
	private String resetLNumber(ConfigVariable configVariable, String gNumber, String lNumbers) throws ServiceRequestException
	{
		List<String> allGroupList = new ArrayList<String>();
		List<TableOfList> listOfList = configVariable.getListOfList();
		if (!SetUtils.isNullList(listOfList))
		{
			for (TableOfList l : listOfList)
			{
				if (!StringUtils.isNullString(l.getGroupName()) && !allGroupList.contains(l.getGroupName()))
				{
					allGroupList.add(l.getGroupName());
				}
			}
		}

		if (!StringUtils.isNullString(lNumbers))
		{
			String[] tmpArr = lNumbers.toUpperCase().split("L");
			int arrSize = 0;
			for (String string : tmpArr)
			{
				if (!StringUtils.isNullString(string))
				{
					arrSize++;
				}
			}
			if (arrSize < allGroupList.size())
			{
				for (int i = 0; i < allGroupList.size() - arrSize; i++)
				{
					lNumbers = lNumbers + "L00";
				}
			}
		}

		// ?????????????????????
		return ConfigUtil.resetLNumbers(lNumbers);
	}

	/**
	 * ????????????????????????
	 * 
	 * @param order
	 * @param draw
	 * @param ruleTime
	 * @return
	 * @throws ServiceRequestException
	 */
	private List<List<String>> checkVariableOfOrder(FoundationObject order, FoundationObject draw, Date ruleTime) throws ServiceRequestException
	{
		List<List<String>> logList = new ArrayList<List<String>>();

		String gNumber = (String) order.get(ConfigParameterConstants.PARTNUMBER);
		String lNumbers = (String) order.get(ConfigParameterConstants.LNUMBER);
		String inputVariables = (String) order.get(ConfigParameterConstants.CONFIGPARAMETER);
		StructureObject struc = (StructureObject) order.get(StructureObject.STRUCTURE_CLASS_NAME);
		// ???????????????????????????
		ConfigCalculateVar configVariable = this.stubService.getCPBStub().buildConfigCalculateVar(draw, ruleTime);
		// ???????????????????????????(????????????P???????????????)
		inputVariables = ConfigUtil.resetInputVariablesForOrder(configVariable, gNumber, inputVariables);
		order.put(ConfigParameterConstants.CONFIGPARAMETER, inputVariables);
		// ??????L??????????????????L00??????
		lNumbers = this.resetLNumber(configVariable, gNumber, lNumbers);
		// ?????????????????????
		lNumbers = ConfigUtil.resetLNumbers(lNumbers);

		configVariable.setAllInput(gNumber, lNumbers, inputVariables);
		// ??????????????????
		inputVariables = configVariable.getRegularInputVarString();
		// ??????????????????
		List<String> tmpLogList = this.stubService.getConfigCheckStub().checkIputVar(draw.getId(), configVariable);

		if (!SetUtils.isNullList(tmpLogList))
		{
			for (String log : tmpLogList)
			{
				List<String> tmpList = new ArrayList<String>();
				tmpList.add(struc.getSequence());
				tmpList.add(order.getFullName());
				tmpList.add(log);
				logList.add(tmpList);
			}
		}
		return logList;
	}

	/**
	 * ????????????????????????????????????
	 * 
	 * @param item
	 * @param draw
	 * @throws ServiceRequestException
	 */
	private void linkDrawToItem(FoundationObject item, FoundationObject draw) throws ServiceRequestException
	{
		boolean isNewItem = item.get(ConfigParameterConstants.IS_NEW_ITEM) == null ? false : BooleanUtils.getBooleanByYN((String) item.get(ConfigParameterConstants.IS_NEW_ITEM));
		if (isNewItem)
		{
			String templateName_ = this.stubService.getDrivenStub().getCADTemplate(draw.getObjectGuid().getClassName());
			RelationTemplateInfo relationTemplate = this.stubService.getEmm().getRelationTemplateByName(item.getObjectGuid(), templateName_);
			if (relationTemplate == null)
			{
				throw new ServiceRequestException("ID_APP_NO_RELATION_TEMPLATE", "no relation template:" + templateName_, null, templateName_);
			}
			StructureObject structureObject = this.stubService.getBoas().newStructureObject(relationTemplate.getStructureClassGuid(), relationTemplate.getStructureClassName());

			ViewObject viewObject = this.stubService.getCPBStub().getViewObject(item.getObjectGuid(), templateName_, true);
			this.stubService.linkNoCheckCycle(viewObject, item, draw.getObjectGuid(), structureObject);
		}
	}

	/**
	 * ??????????????????
	 * 
	 * @param order
	 * @param item
	 * @throws ServiceRequestException
	 */
	private void updateOrder(FoundationObject contract, FoundationObject order, FoundationObject item) throws ServiceRequestException
	{
		if (item != null)
		{
			order.put(ConfigParameterConstants.ORDERITEM, item.getObjectGuid().getGuid());
			order.put(ConfigParameterConstants.ORDERITEM + "$CLASS", item.getObjectGuid().getClassGuid());
			order.put(ConfigParameterConstants.ORDERITEM + "$MASTER", item.getObjectGuid().getMasterGuid());

			this.stubService.saveObject(order);
		}
	}

	// ??????????????????
	protected void deleteContract(ObjectGuid contractObjectGuid) throws ServiceRequestException
	{
		FoundationObject contract = this.stubService.getBoas().getObject(contractObjectGuid);
		if (contract == null)
		{
			return;
		}

		try
		{
//			DataServer.getTransactionManager().startTransaction(this.stubService.getFixedTransactionId());
			// ????????????
			ViewObject viewObject = this.stubService.getBoas().getRelationByEND1(contractObjectGuid, ConfigParameterConstants.CONFIG_PARAMETER_ORDELDETAIL_TEMPLATE_NAME);
			if (viewObject != null)
			{
				// ???????????????????????????????????????
				List<FoundationObject> end2List = this.listOrderDetail(contractObjectGuid, viewObject.getObjectGuid());
				if (!SetUtils.isNullList(end2List))
				{
					// ??????????????????
					for (FoundationObject detail : end2List)
					{
						this.stubService.getBoas().deleteObject(detail);
					}
				}
				this.stubService.getBoas().deleteObject(viewObject);
			}

			// ??????BOM
			viewObject = this.stubService.getBoas().getRelationByEND1(contractObjectGuid, ConfigParameterConstants.CONFIG_PARAMETER_RESULTRELATION_TEMPLATE_NAME);
			if (viewObject != null)
			{
				this.stubService.getBoas().deleteObject(viewObject);
			}
//			DataServer.getTransactionManager().commitTransaction();
		}
		catch (DynaDataException e)
		{
//			DataServer.getTransactionManager().rollbackTransaction();
			throw ServiceRequestExceptionWrap.createByDynaDataException(this.stubService, e);
		}
		catch (Exception e)
		{
//			DataServer.getTransactionManager().rollbackTransaction();
			if (e instanceof ServiceRequestException)
			{
				throw (ServiceRequestException) e;
			}
			else
			{
				throw ServiceRequestException.createByException("ID_APP_SERVER_EXCEPTION", e);
			}
		}
	}

	private FoundationObject checkOrderDetailItem(ObjectGuid contractObjectGuid, FoundationObject orderDetail, FoundationObject draw, RelationTemplateInfo relationTemplate,
			Date ruleTime) throws ServiceRequestException
	{
		String partNumber = (String) orderDetail.get(ConfigParameterConstants.PARTNUMBER);
		String lNumbers = (String) orderDetail.get(ConfigParameterConstants.LNUMBER);
		String inputVars = (String) orderDetail.get(ConfigParameterConstants.CONFIGPARAMETER);

		// ????????????????????????
		if (draw == null)
		{
			draw = this.getDrawInstanceByOrderDetail(orderDetail.getId(), ruleTime);
		}
		// ????????????????????????
		return this.stubService.getDrivenStub().saveFoundationObject(draw, null, partNumber, lNumbers, inputVars);
	}

	private List<FoundationObject> listOrderDetail(ObjectGuid contractObjectGuid, ObjectGuid viewObjectGuid) throws ServiceRequestException
	{
		String templateName = ConfigParameterConstants.CONFIG_PARAMETER_ORDELDETAIL_TEMPLATE_NAME;
		RelationTemplateInfo relationTemplate = this.stubService.getEmm().getRelationTemplateByName(contractObjectGuid, templateName);
		if (relationTemplate == null)
		{
			throw new ServiceRequestException("ID_APP_NO_RELATION_TEMPLATE", "no relation template:" + templateName, null, templateName);
		}

		SearchCondition end2SearchCondition = this.getEnd2SearchCondition(relationTemplate);
		end2SearchCondition.addResultField(ConfigParameterConstants.PARTNUMBER);
		end2SearchCondition.addResultField(ConfigParameterConstants.LNUMBER);
		end2SearchCondition.addResultField(ConfigParameterConstants.CONFIGPARAMETER);

		// ???????????????????????????????????????
		return this.stubService.getConfigQueryStub().listFoundationObjectOfRelationWithoutDecorator(viewObjectGuid, null, end2SearchCondition, null, true);
	}

	private SearchCondition getEnd2SearchCondition(RelationTemplateInfo relationTemplate) throws ServiceRequestException
	{
		String end2ClassName = null;
		List<RelationTemplateEnd2> relationTemplateEnd2List = this.stubService.getRelationService().listRelationTemplateEnd2(relationTemplate.getGuid());
		if (!SetUtils.isNullList(relationTemplateEnd2List))
		{
			for (RelationTemplateEnd2 templateEnd2 : relationTemplateEnd2List)
			{
				String end2BOName = templateEnd2.getEnd2BoName();
				BOInfo end2BOInfo = this.stubService.getEmm().getCurrentBoInfoByName(end2BOName, true);
				if (end2BOInfo != null)
				{
					end2ClassName = end2BOInfo.getClassName();
					break;
				}
			}
		}
		SearchCondition end2SearchCondition = SearchConditionFactory.createSearchCondition4Class(end2ClassName, null, false);
		List<UIObjectInfo> uiObjectList = this.stubService.getEmm().listUIObjectInCurrentBizModel(end2ClassName, UITypeEnum.FORM, true);
		if (!SetUtils.isNullList(uiObjectList))
		{
			List<String> uiNameList = new ArrayList<String>();
			for (UIObjectInfo uiObject : uiObjectList)
			{
				uiNameList.add(uiObject.getName());
			}
			end2SearchCondition.setResultUINameList(uiNameList);
		}

		return end2SearchCondition;
	}

	/**
	 * ????????????????????????????????????
	 * 
	 * @param item
	 * @return
	 * @throws ServiceRequestException
	 */
	protected List<StructureObject> driveResult4Order(FoundationObject item, FoundationObject draw, DataRule dataRule, SearchCondition itemSearchCondition)
			throws ServiceRequestException
	{
		item = this.stubService.getBoas().getObject(item.getObjectGuid());
		RelationTemplateInfo relationTemplate = this.stubService.getEmm().getRelationTemplateByName(item.getObjectGuid(),
				ConfigParameterConstants.CONFIG_PARAMETER_RESULTRELATION_TEMPLATE_NAME);

		ClassStub.decorateObjectGuid(item.getObjectGuid(), this.stubService);
		if (draw == null)
		{
			draw = this.getDrawInstanceByItem(item, dataRule.getLocateTime());
		}

		// ??????????????????????????????
		if (draw == null)
		{
			return null;
		}

		if (SystemStatusEnum.RELEASE != draw.getStatus())
		{
			throw new ServiceRequestException("ID_APP_CONFIG_DRAW_NOT_RLS", "instance is not exist.", null, draw.getId());
		}

		if (dataRule == null)
		{
			dataRule = new DataRule();
			dataRule.setLocateTime(draw.getReleaseTime());
			dataRule.setSystemStatus(draw.getStatus());
		}
		if (draw.isLatestRevision())
		{
			dataRule.setLocateTime(null);
		}

		SearchCondition strucSearchCondition = this.buildStrucSearchCondition(draw.getObjectGuid());
		SearchCondition end2SearchCondition = this.buildEnd2SearchCondition(draw.getObjectGuid());

		String gNumber = (String) item.get(ConfigParameterConstants.PARTNUMBER);
		String lNumbers = (String) item.get(ConfigParameterConstants.LNUMBER);
		String inptVarriables = (String) item.get(ConfigParameterConstants.CONFIGPARAMETER);

		DrivenResult result = this.stubService.getDrivenStub().drivenByConfigRules(draw, strucSearchCondition, end2SearchCondition, dataRule, gNumber, lNumbers, inptVarriables);
		if (result != null)
		{
			if (!StringUtils.isNullString(result.getErrMsg()))
			{
				throw new ServiceRequestException(result.getErrMsg());
			}
			List<StructureObject> strucList = result.getStructureObjectList();
			if (!SetUtils.isNullList(strucList))
			{
				try
				{
//					DataServer.getTransactionManager().startTransaction(this.stubService.getFixedTransactionId());
					for (StructureObject struc : strucList)
					{
						String gNumber_ = (String) struc.get(ConfigParameterConstants.PARTNUMBER);
						String lNumbers_ = (String) struc.get(ConfigParameterConstants.LNUMBER);
						String inptVarriables_ = (String) struc.get(ConfigParameterConstants.CONFIGPARAMETER);

						FoundationObject end2 = struc.getEnd2UIObject();
						String isNoDrawingItem = (String) end2.get(ConfigParameterConstants.IS_NO_DRAWING_ITEM);
						if ("Y".equals(isNoDrawingItem))
						{
							String itemClassification = (String) draw.get(ConfigParameterConstants.ITEM_CLASSIFICATION);
							if (!StringUtils.isNullString(itemClassification))
							{
								CodeItemInfo codeItemInfo = this.stubService.getEmm().getCodeItem(itemClassification);
								if (codeItemInfo != null)
								{
									end2.put(ConfigParameterConstants.ITEM_CLASSIFICATION + "$NAME", codeItemInfo.getName());
									end2.put(ConfigParameterConstants.ITEM_CLASSIFICATION, codeItemInfo.getGuid());
								}
							}
						}

						ClassInfo classInfo = this.stubService.getEmm().getClassByGuid(end2.getObjectGuid().getClassGuid());
						if (classInfo.hasInterface(ModelInterfaceEnum.IManufacturingRule))
						{
							FoundationObject item_ = this.stubService.getDrivenStub().saveFoundationObject(end2, itemSearchCondition, gNumber_, lNumbers_, inptVarriables_);
							struc.setEnd2ObjectGuid(item_.getObjectGuid());
							struc.setEnd2UIObject(item_);

							// ???????????????????????????????????????
							this.linkDrawToItem(item_, end2);
						}

						// ???????????????
						struc.put("CLASSGUID$", relationTemplate.getStructureClassGuid());
						struc.put("CLASSNAME$", relationTemplate.getStructureClassName());
						struc.getObjectGuid().setClassGuid(relationTemplate.getStructureClassGuid());
						struc.getObjectGuid().setClassName(relationTemplate.getStructureClassName());
					}
//					DataServer.getTransactionManager().commitTransaction();
				}
				catch (DynaDataException e)
				{
//					DataServer.getTransactionManager().rollbackTransaction();
					throw ServiceRequestExceptionWrap.createByDynaDataException(this.stubService, e);
				}
				catch (Exception e)
				{
//					DataServer.getTransactionManager().rollbackTransaction();
					if (e instanceof ServiceRequestException)
					{
						throw (ServiceRequestException) e;
					}
					else
					{
						throw ServiceRequestException.createByException("ID_APP_SERVER_EXCEPTION", e);
					}
				}
				return strucList;
			}
		}

		return null;
	}

	private SearchCondition buildStrucSearchCondition(ObjectGuid objectGuid) throws ServiceRequestException
	{
		RelationTemplateInfo template = this.stubService.getEmm().getRelationTemplateByName(objectGuid, ConfigParameterConstants.CONFIG_PARAMETER_RELATION_TEMPLATE_NAME);
		SearchCondition condition = SearchConditionFactory.createSearchConditionForStructure(template.getStructureClassName());
		List<UIObjectInfo> uiObjectList = this.stubService.getEmm().listUIObjectInCurrentBizModel(template.getStructureClassName(), UITypeEnum.LIST, true);
		if (!SetUtils.isNullList(uiObjectList))
		{
			for (UIObjectInfo uiInfo : uiObjectList)
			{
				List<UIField> uiFeldList = this.stubService.getEmm().listUIFieldByUIGuid(uiInfo.getGuid());
				if (!SetUtils.isNullList(uiFeldList))
				{
					for (UIField uiField : uiFeldList)
					{
						condition.addResultField(uiField.getName());
					}
				}
			}
		}
		return condition;
	}

	private SearchCondition buildEnd2SearchCondition(ObjectGuid objectGuid) throws ServiceRequestException
	{
		RelationTemplateInfo template = this.stubService.getEmm().getRelationTemplateByName(objectGuid, ConfigParameterConstants.CONFIG_PARAMETER_RELATION_TEMPLATE_NAME);

		List<String> end2ClassNameList = new ArrayList<String>();
		List<RelationTemplateEnd2> relationTemplateEnd2List = this.stubService.getRelationService().listRelationTemplateEnd2(template.getGuid());
		if (!SetUtils.isNullList(relationTemplateEnd2List))
		{
			for (RelationTemplateEnd2 templateEnd2 : relationTemplateEnd2List)
			{
				String end2ClassName = this.stubService.getEmm().getCurrentBoInfoByName(templateEnd2.getEnd2BoName(), true).getClassName();
				end2ClassNameList.add(end2ClassName);
			}
		}

		if (SetUtils.isNullList(end2ClassNameList))
		{
			return null;
		}

		SearchCondition end2SearchCondition = SearchConditionFactory.createSearchCondition4MulitClassSearch(end2ClassNameList);
		for (String className : end2ClassNameList)
		{
			List<UIObjectInfo> uiObjectList = this.stubService.getEmm().listUIObjectInCurrentBizModel(className, UITypeEnum.LIST, true);
			if (!SetUtils.isNullList(uiObjectList))
			{
				for (UIObjectInfo uiInfo : uiObjectList)
				{
					List<UIField> uiFeldList = this.stubService.getEmm().listUIFieldByUIGuid(uiInfo.getGuid());
					if (!SetUtils.isNullList(uiFeldList))
					{
						for (UIField uiField : uiFeldList)
						{
							end2SearchCondition.addResultField(uiField.getName());
						}
					}
				}
			}
		}
		return end2SearchCondition;
	}

	/**
	 * ???????????????????????????????????????
	 * 
	 * @param drawNo
	 * @return
	 * @throws ServiceRequestException
	 */
	protected FoundationObject getDrawInstanceByOrderDetail(String drawNo, Date ruleTime) throws ServiceRequestException
	{
		ClassInfo classInfo = this.stubService.getEmm().getFirstLevelClassByInterface(ModelInterfaceEnum.IManufacturingRule, null);
		if (classInfo == null)
		{
			return null;
		}

		SearchCondition sc = SearchConditionFactory.createSearchCondition4Class(classInfo.getName(), null, true);
		sc.addFilter(SystemClassFieldEnum.ID.getName(), drawNo, OperateSignEnum.EQUALS);
		sc.addResultField(ConfigParameterConstants.MATCHEDCLASS);
		sc.setCaseSensitive(true);
		sc.setSearchRevisionTypeEnum(SearchRevisionTypeEnum.ISHISTORYREVISION);
		List<UIObjectInfo> uiObjectList = this.stubService.getEmm().listUIObjectInCurrentBizModel(classInfo.getName(), UITypeEnum.LIST, true);
		if (!SetUtils.isNullList(uiObjectList))
		{
			List<String> uiNameList = new ArrayList<String>();
			for (UIObjectInfo uiObject : uiObjectList)
			{
				uiNameList.add(uiObject.getName());
			}
			sc.setResultUINameList(uiNameList);
		}
		List<FoundationObject> list = this.stubService.getBoas().listObject(sc);
		if (!SetUtils.isNullList(list))
		{
			for (FoundationObject fo : list)
			{
				if (fo.getReleaseTime() == null)
				{
					continue;
				}
				if (ruleTime == null)
				{
					String latestRevision = (String) fo.get(SystemClassFieldEnum.LATESTREVISION.getName());
					if (!StringUtils.isNullString(latestRevision) && latestRevision.contains("r"))
					{
						return fo;
					}
				}
				else
				{
					Date nextRevisionReleaseTime = (Date) fo.get(SystemClassFieldEnum.NEXTREVISIONRLSTIME.getName());
					Integer revisionIdSequence = ((Number) fo.get(SystemClassFieldEnum.REVISIONIDSEQUENCE.getName())).intValue();
					// ?????????????????????????????????????????????????????????????????????
					if (DateFormat.compareDate(ruleTime, fo.getReleaseTime(), DateFormat.PTN_YMD) < 0 && revisionIdSequence == 1)
					{
						return fo;
					}
					else if (DateFormat.compareDate(ruleTime, fo.getReleaseTime(), DateFormat.PTN_YMD) >= 0
							&& (nextRevisionReleaseTime == null || DateFormat.compareDate(nextRevisionReleaseTime, ruleTime, DateFormat.PTN_YMD) >= 0))
					{
						return fo;
					}
				}
			}
		}
		return null;
	}

	/**
	 * ??????????????????????????????????????????????????????
	 * 
	 * @param structureObject
	 * @throws ServiceRequestException
	 */
	protected void unlink(StructureObject structureObject) throws ServiceRequestException
	{
		try
		{
//			DataServer.getTransactionManager().startTransaction(this.stubService.getFixedTransactionId());

			FoundationObject order = this.getOrderDetailFromStructure(structureObject);
			if (order != null)
			{
				order.put(ConfigParameterConstants.ORDERITEM, null);
				order.put(ConfigParameterConstants.ORDERITEM + "$CLASS", null);
				order.put(ConfigParameterConstants.ORDERITEM + "$MASTER", null);

				this.stubService.saveObject(order);
			}

			this.stubService.getBoas().unlink(structureObject);

//			DataServer.getTransactionManager().commitTransaction();
		}
		catch (DynaDataException e)
		{
//			DataServer.getTransactionManager().rollbackTransaction();
			throw ServiceRequestExceptionWrap.createByDynaDataException(this.stubService, e);
		}
		catch (Exception e)
		{
//			DataServer.getTransactionManager().rollbackTransaction();
			if (e instanceof ServiceRequestException)
			{
				throw (ServiceRequestException) e;
			}
			else
			{
				throw ServiceRequestException.createByException("ID_APP_SERVER_EXCEPTION", e);
			}
		}
	}

	private FoundationObject getOrderDetailFromStructure(StructureObject structureObject) throws ServiceRequestException
	{
		ClassStub.decorateObjectGuid(structureObject.getObjectGuid(), this.stubService);
		SearchCondition sc = SearchConditionFactory.createSearchConditionForStructure(structureObject.getObjectGuid().getClassName());
		sc.addResultField(ConfigParameterConstants.STRUC_ORDER_DETAIL);
		structureObject = this.stubService.getBoas().getStructureObject(structureObject.getObjectGuid(), sc);
		String orderDetailGuid = (String) structureObject.get(ConfigParameterConstants.STRUC_ORDER_DETAIL);
		if (StringUtils.isGuid(orderDetailGuid))
		{
			ObjectGuid orderObjectGuid = new ObjectGuid((String) structureObject.get(ConfigParameterConstants.STRUC_ORDER_DETAIL + "$CLASS"), null, orderDetailGuid, null);
			return this.stubService.getBoas().getObject(orderObjectGuid);
		}
		return null;
	}

	/**
	 * ???????????????????????????????????????
	 * 
	 * @param item
	 * @param ruleTime
	 * @return
	 * @throws ServiceRequestException
	 */
	protected FoundationObject getDrawInstanceByItem(FoundationObject item, Date ruleTime) throws ServiceRequestException
	{
		String classGuid = (String) item.get(ConfigParameterConstants.ORIGOBJ + "$CLASS");
		String masterGuid = (String) item.get(ConfigParameterConstants.ORIGOBJ + "$MASTER");
		if (!StringUtils.isGuid(masterGuid) || !StringUtils.isGuid(classGuid))
		{
			return null;
		}

		ObjectGuid drawObjectGuid = new ObjectGuid();
		drawObjectGuid.setClassGuid(classGuid);
		drawObjectGuid.setMasterGuid(masterGuid);

		FoundationObject drawing = this.stubService.getDrawing(drawObjectGuid, ruleTime);
		if (drawing == null)
		{
			return null;
		}
		return this.stubService.getBoas().getObject(drawing.getObjectGuid());
	}

	private String getMessage(String id, Object... agrs) throws ServiceRequestException
	{
		return this.stubService.getMsrm().getMSRString(id, this.stubService.getUserSignature().getLanguageEnum().getId(), agrs);
	}

	protected void saveOrderParameter(FoundationObject contract, ObjectGuid orderObjectGuid, String gNumber, String lNumbers, String inputVariables) throws ServiceRequestException
	{
		FoundationObject order = this.stubService.getBoas().getObject(orderObjectGuid);
		if (order == null)
		{
			throw new ServiceRequestException("ID_DS_NO_DATA", "order is not exist, guid='" + orderObjectGuid.getGuid() + "'");
		}

		String templateName = ConfigParameterConstants.CONFIG_PARAMETER_RESULTRELATION_TEMPLATE_NAME;
		RelationTemplateInfo relationTemplate = this.stubService.getEmm().getRelationTemplateByName(contract.getObjectGuid(), templateName);
		if (relationTemplate == null)
		{
			throw new ServiceRequestException("ID_APP_NO_RELATION_TEMPLATE", "no relation template:" + templateName, null, templateName);
		}

		try
		{
//			DataServer.getTransactionManager().startTransaction(this.stubService.getFixedTransactionId());

			ViewObject viewObject = this.stubService.getCPBStub().getViewObject(contract.getObjectGuid(), templateName, false);

			String itemMaster = (String) order.get(ConfigParameterConstants.ORDERITEM + "$MASTER");
			order.put(ConfigParameterConstants.ORDERITEM, null);
			order.put(ConfigParameterConstants.ORDERITEM + "$CLASS", null);
			order.put(ConfigParameterConstants.ORDERITEM + "$MASTER", null);
			order.put(ConfigParameterConstants.PARTNUMBER, gNumber);
			order.put(ConfigParameterConstants.LNUMBER, lNumbers);
			order.put(ConfigParameterConstants.CONFIGPARAMETER, inputVariables);
			this.stubService.saveObject(order);

			if (!StringUtils.isNullString(itemMaster) && viewObject != null)
			{
				List<StructureObject> list = this.stubService.getBoas().listObjectOfRelation(viewObject.getObjectGuid(), null, null, null);
				if (list != null)
				{
					for (StructureObject stru : list)
					{
						if (itemMaster.equalsIgnoreCase(stru.getEnd2ObjectGuid().getMasterGuid()))
						{
							this.stubService.getBoas().unlink(stru);
						}
					}
				}
			}

//			DataServer.getTransactionManager().commitTransaction();
		}
		catch (DynaDataException e)
		{
//			DataServer.getTransactionManager().rollbackTransaction();
			throw ServiceRequestExceptionWrap.createByDynaDataException(this.stubService, e);
		}
		catch (Exception e)
		{
//			DataServer.getTransactionManager().rollbackTransaction();
			if (e instanceof ServiceRequestException)
			{
				throw (ServiceRequestException) e;
			}
			else
			{
				throw ServiceRequestException.createByException("ID_APP_SERVER_EXCEPTION", e);
			}
		}
	}

}
