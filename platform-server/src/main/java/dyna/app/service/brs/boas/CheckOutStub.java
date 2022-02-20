/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: CheckOutStub
 * Wanglei 2011-3-30
 */
package dyna.app.service.brs.boas;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.emm.ClassStub;
import dyna.app.service.brs.emm.EMMImpl;
import dyna.app.service.helper.Constants;
import dyna.app.service.helper.ServiceRequestExceptionWrap;
import dyna.app.service.helper.decorate.DecoratorFactory;
import dyna.common.SearchCondition;
import dyna.common.SearchConditionFactory;
import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.bean.data.StructureObject;
import dyna.common.bean.data.foundation.ViewObject;
import dyna.common.dto.model.cls.ClassField;
import dyna.common.dto.model.cls.ClassInfo;
import dyna.common.dto.model.ui.UIObjectInfo;
import dyna.common.dto.template.relation.RelationTemplateInfo;
import dyna.common.exception.DecorateException;
import dyna.common.exception.DynaDataException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.systemenum.ModelInterfaceEnum;
import dyna.common.systemenum.RelationTemplateActionEnum;
import dyna.common.systemenum.UITypeEnum;
import dyna.common.util.SetUtils;
import dyna.net.service.brs.EMM;
import dyna.net.service.data.InstanceService;
import dyna.net.service.data.model.InterfaceModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Wanglei
 * 
 */
@Component
public class CheckOutStub extends AbstractServiceStub<BOASImpl>
{
	
	@Autowired
	private DecoratorFactory decoratorFactory;

	public FoundationObject checkOut(FoundationObject foundationObject, String checkOutUserGuid, boolean isCheckAuth) throws ServiceRequestException
	{
		FoundationObject retFoundationObject = null;

		try
		{
			// 执行检出操作
			retFoundationObject = this.checkOutNoCascade(foundationObject, checkOutUserGuid, isCheckAuth);

			decoratorFactory.decorateFoundationObject(null, retFoundationObject, this.stubService.getEmm(), this.stubService.getEmm().getCurrentBizModel().getGuid(), null);
			List<FoundationObject> list = new ArrayList<FoundationObject>();
			list.add(retFoundationObject);
			String sessionId = this.stubService.getSignature().getCredential();
			decoratorFactory.decorateFoundationObject(null, list, this.stubService.getEmm(), sessionId);

			EMM emm = this.stubService.getEmm();

			// 处理relation
			// 查找所有关联的ViewObject
			List<ViewObject> viewObjectList = this.stubService.listRelation(retFoundationObject.getObjectGuid());

			if (!SetUtils.isNullList(viewObjectList))
			{
				for (ViewObject viewObject : viewObjectList)
				{
					RelationTemplateInfo relationTemplate = this.stubService.getEmm()
							.getRelationTemplateById(viewObject.get(ViewObject.TEMPLATE_ID) == null ? "" : (String) viewObject.get(ViewObject.TEMPLATE_ID));
					if (relationTemplate == null)
					{
						continue;
					}

					if (RelationTemplateActionEnum.NONE.equals(relationTemplate.getCheckinTrigger()))
					{
						// 不处理end2
						continue;
					}
					else if (RelationTemplateActionEnum.CHECKOUT.equals(relationTemplate.getCheckoutTrigger()))
					{
						// 关联检入end2
						try
						{
							String structureClassName = relationTemplate.getStructureClassName();

							List<UIObjectInfo> uiObjectList = emm.listUIObjectInCurrentBizModel(structureClassName, UITypeEnum.FORM, true);
							SearchCondition searchCondition = null;
							if (!SetUtils.isNullList(uiObjectList))
							{
								searchCondition = SearchConditionFactory.createSearchConditionForStructure(structureClassName);
								for (UIObjectInfo uiObject : uiObjectList)
								{
									searchCondition.addResultUIObjectName(uiObject.getName());
								}
							}

							List<StructureObject> structureObjectList = this.stubService.listObjectOfRelation(viewObject.getObjectGuid(), searchCondition, null, null);
							if (!SetUtils.isNullList(structureObjectList))
							{
								for (StructureObject structureObject : structureObjectList)
								{
									try
									{
										FoundationObject end2FoundationObject = this.stubService.getObject(structureObject.getEnd2ObjectGuid());
										if (end2FoundationObject != null)
										{
											this.checkOut(end2FoundationObject, checkOutUserGuid, isCheckAuth);
										}
									}
									catch (Exception e)
									{
										continue;
									}
								}
							}
						}
						catch (Exception e)
						{
							continue;
						}
					}
				}

			}

			// DataServer.getTransactionManager().commitTransaction();
			// 20170301 去除再次查询
			// 获取检出后的对象
			// retFoundationObject = this.stubService.getObject(foundationObject.getObjectGuid());

		}
		catch (DynaDataException e)
		{
			// DataServer.getTransactionManager().rollbackTransaction();
			throw ServiceRequestExceptionWrap.createByDynaDataException(this.stubService, e);
		}
		catch (Exception e)
		{
			// DataServer.getTransactionManager().rollbackTransaction();
			if (e instanceof ServiceRequestException)
			{
				throw (ServiceRequestException) e;
			}
			else
			{
				throw ServiceRequestException.createByException("ID_APP_SERVER_EXCEPTION", e);
			}
		}
		finally
		{
		}

		return retFoundationObject;
	}

	public FoundationObject checkOutNoCascade(FoundationObject foundationObject, String checkOutUserGuid, boolean isCheckAuth) throws ServiceRequestException
	{

		String sessionId = this.stubService.getSignature().getCredential();

		try
		{

			// invoke checkout.before event script
			this.stubService.getEoss().executeCheckOutBeforeEvent(foundationObject);
			// 执行检出操作
			foundationObject = this.stubService.getInstanceService().checkout(foundationObject, checkOutUserGuid, Constants.isSupervisor(isCheckAuth, this.stubService), sessionId,
					this.stubService.getFixedTransactionId());

			ObjectGuid objectGuid = foundationObject.getObjectGuid();
			String bmGuid = this.stubService.getUserSignature().getLoginGroupBMGuid();

			EMM emm = this.stubService.getEmm();
			Set<String> fieldSet = this.getObjectFieldSet(objectGuid);

			decoratorFactory.decorateFoundationObject(fieldSet, foundationObject, emm, bmGuid, null);
			decoratorFactory.ofd.decorateWithField(fieldSet, foundationObject, emm, sessionId, false);

			// invoke checkout.after event script
			this.stubService.getEoss().executeCheckOutAfterEvent(foundationObject);
		}
		catch (DynaDataException e)
		{
			throw ServiceRequestExceptionWrap.createByDynaDataException(this.stubService, e);
		}
		catch (DecorateException e)
		{
			throw ServiceRequestException.createByDecorateException(e);
		}

		return foundationObject;
	}

	private Set<String> getObjectFieldSet(ObjectGuid objectGuid) throws ServiceRequestException
	{
		ClassStub.decorateObjectGuid(objectGuid, this.stubService);
		String bmGuid = this.stubService.getUserSignature().getLoginGroupBMGuid();
		List<UIObjectInfo> uiObjectList = ((EMMImpl) this.stubService.getEmm()).getUIStub().listUIObjectByBizModel(objectGuid.getClassName(), bmGuid, UITypeEnum.FORM, true);
		UIObjectInfo[] uiObjects = null;
		if (!SetUtils.isNullList(uiObjectList))
		{
			uiObjects = new UIObjectInfo[uiObjectList.size()];
			int i = 0;
			for (UIObjectInfo uiObject : uiObjectList)
			{
				uiObjects[i++] = uiObject;
			}
		}

		SearchCondition searchCondition = SearchConditionFactory.createSearchCondition(objectGuid, null, false);

		if (uiObjects != null)
		{
			for (UIObjectInfo uiObject : uiObjects)
			{
				if (uiObject == null)
				{
					continue;
				}
				searchCondition.addResultUIObjectName(uiObject.getName());
			}
		}

		ClassInfo classInfo = this.stubService.getEmm().getClassByName(objectGuid.getClassName());
		if (classInfo != null)
		{
			List<ModelInterfaceEnum> interfaceList = classInfo.getInterfaceList();
			if (interfaceList != null)
			{
				for (ModelInterfaceEnum modelInterfaceEnum : interfaceList)
				{
					List<ClassField> listClassFieldInInterface = this.stubService.getInterfaceModelService().listClassFieldOfInterface(modelInterfaceEnum);

					if (listClassFieldInInterface != null)
					{
						for (ClassField classField : listClassFieldInInterface)
						{
							searchCondition.addResultField(classField.getName());
						}
					}
				}
			}
		}

		return this.stubService.getEmm().getObjectFieldNamesInSC(searchCondition);
	}
}
