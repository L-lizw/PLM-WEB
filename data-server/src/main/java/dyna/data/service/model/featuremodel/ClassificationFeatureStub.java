package dyna.data.service.model.featuremodel;

import dyna.common.bean.data.SystemObject;
import dyna.common.bean.model.code.ClassficationFeatureItem;
import dyna.common.bean.model.code.CodeObject;
import dyna.common.dto.cfm.ClassficationFeature;
import dyna.common.dto.cfm.ClassficationFeatureItemInfo;
import dyna.common.dto.cfm.ClassificationNumberField;
import dyna.common.dto.model.cls.ClassField;
import dyna.common.dto.model.cls.ClassInfo;
import dyna.common.dto.model.code.CodeItemInfo;
import dyna.common.dto.model.code.CodeObjectInfo;
import dyna.common.exception.DynaDataException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import dyna.common.systemenum.FieldTypeEnum;
import dyna.common.systemenum.coding.CFMCodeRuleEnum;
import dyna.common.util.SetUtils;
import dyna.common.util.StringUtils;
import dyna.data.service.DSAbstractServiceStub;
import dyna.dbcommon.filter.FieldValueEqualsFilter;
import dyna.net.service.data.SystemDataService;
import dyna.net.service.data.model.CodeModelService;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ClassificationFeatureStub extends DSAbstractServiceStub<ClassificationFeatureServiceImpl>
{
	private static Map<String, ClassficationFeatureItem> ClassficationFeatureItemCache = new HashMap<>();

	protected void checkAndClearClassificationFeature(String userGuid) throws ServiceRequestException
	{
		String tranid = StringUtils.generateRandomUID(32);
		try
		{
			//TODO
//			this.stubService.getTransactionService().startTransaction(tranid);

			SystemDataService sds = this.stubService.getSystemDataService();
			List<ClassficationFeature> featurList = sds.listFromCache(ClassficationFeature.class, null);
			List<String> uncheckClassGuidList = new ArrayList<>();
			if (!SetUtils.isNullList(featurList))
			{
				for (ClassficationFeature feature : featurList)
				{
					ClassInfo classInfo = sds.get(ClassInfo.class, feature.getClassGuid());
					CodeObjectInfo classification = sds.get(CodeObjectInfo.class, feature.getClassificationfk());

					List<ClassficationFeatureItemInfo> itemList = sds.listFromCache(ClassficationFeatureItemInfo.class,
							new FieldValueEqualsFilter<>(ClassficationFeatureItemInfo.CLASSIFICATIONFEATTUREGUID, feature.getGuid()));

					if (classInfo == null || classification == null)
					{
						if (!SetUtils.isNullList(itemList))
						{
							for (ClassficationFeatureItemInfo itemInfo : itemList)
							{
								this.delClassificationFeatureItem(itemInfo.getGuid());
							}
						}
						DynaLogger.info("ClassficationFeature delete: " + feature);
						sds.delete(feature);
					}
					else
					{
						if (StringUtils.isNullString(classInfo.getClassification()) || classInfo.getClassification().equalsIgnoreCase(feature.getClassificationfk()) == false)
						{
							if (feature.isMaster())
							{
								feature.setIsmaster(false);
								sds.save(feature);
							}
						}
						else
						{
							if (!feature.isMaster())
							{
								feature.setIsmaster(true);
								sds.save(feature);
							}
						}
						if (feature.isMaster())
						{
							uncheckClassGuidList.add(feature.getClassGuid());
						}
						// 检查item记录的分类子项是否存在
						if (!SetUtils.isNullList(itemList))
						{
							for (ClassficationFeatureItemInfo itemInfo : itemList)
							{
								CodeItemInfo codeItemInfo = sds.get(CodeItemInfo.class, itemInfo.getClassificationItemGuid());
								if (codeItemInfo == null)
								{
									DynaLogger.info("FeatureItem delete: " + itemInfo);
									this.delClassificationFeatureItem(itemInfo.getGuid());
								}
							}
						}
					}
				}
			}
			List<ClassInfo> classInfoList = this.stubService.getClassModelService().listAllClass();
			if (classInfoList != null)
			{
				for (ClassInfo info : classInfoList)
				{
					if (StringUtils.isNullString(info.getSuperClassGuid()))
					{
						checkClassMasteFeature(info, uncheckClassGuidList, userGuid);
					}
				}
			}
//			this.stubService.getTransactionService().commitTransaction(tranid);

		}
		catch (DynaDataException e)
		{
//			this.stubService.getTransactionService().rollbackTransaction(tranid);
			throw e;
		}
		catch (Exception e)
		{
//			this.stubService.getTransactionService().rollbackTransaction(tranid);
			throw ServiceRequestException.createByException("ID_APP_SERVER_EXCEPTION", e);
		}
	}

	private void checkClassMasteFeature(ClassInfo info, List<String> uncheckClassGuidList, String userGuid)
	{
		if (StringUtils.isNullString(info.getClassification()))
		{
			List<ClassInfo> classInfoList = this.stubService.getClassModelService().listChildren(info.getGuid());
			if (classInfoList != null)
			{
				for (ClassInfo subinfo : classInfoList)
				{
					checkClassMasteFeature(subinfo, uncheckClassGuidList, userGuid);
				}
			}
		}
		else
		{
			if (uncheckClassGuidList.contains(info.getGuid()) == false)
			{
				CodeObject codeObjectByGuid = this.stubService.getCodeModelService().getCodeObject(info.getClassification());
				if (codeObjectByGuid != null)
				{
					ClassficationFeature feature = new ClassficationFeature();
					feature.setClassGuid(info.getGuid());
					feature.setIsmaster(true);
					feature.setClassificationfk(codeObjectByGuid.getGuid());
					feature.put(SystemObject.CREATE_USER_GUID, userGuid);
					feature.put(SystemObject.UPDATE_USER_GUID, userGuid);
					feature.setTitle(codeObjectByGuid.getTitle());
					feature.setClassificationName(codeObjectByGuid.getName());
					this.stubService.getSystemDataService().save(feature);
				}
			}
		}

	}

	protected ClassficationFeature getClassficationFeature(String guid)
	{
		return this.stubService.getSystemDataService().getObject(ClassficationFeature.class, guid);
	}

	protected List<ClassficationFeature> listAllClassficationFeature()
	{
		SystemDataService sds = this.stubService.getSystemDataService();
		CodeModelService cms = this.stubService.getCodeModelService();
		List<ClassficationFeature> featureList = sds.listFromCache(ClassficationFeature.class, null);
		if (featureList != null)
		{
			featureList.forEach(feature -> {
				CodeObject codeObjectInfo = cms.getCodeObjectByGuid(feature.getClassificationfk());
				if (codeObjectInfo != null)
				{
					feature.setTitle(codeObjectInfo.getTitle());
					feature.setClassificationName(codeObjectInfo.getName());
				}
			});
		}
		return featureList;
	}

	protected List<ClassficationFeature> listClassficationFeatureBySubClass(String classguid)
	{
		final List<String> classGuidList = new ArrayList<>();
		List<String> tempList = this.stubService.getClassModelService().listAllParentClassGuid(classguid);
		classGuidList.add(classguid);
		if (tempList != null)
		{
			classGuidList.addAll(tempList);
		}
		List<ClassficationFeature> featureList = this.listAllClassficationFeature();
		if (!SetUtils.isNullList(featureList))
		{
			featureList = featureList.stream().filter(feature -> classGuidList.contains(feature.getClassGuid())).collect(Collectors.toList());
		}
		return featureList;
	}

	protected List<ClassficationFeature> listAllSubClassficationFeature(String classGuid)
	{
		final List<String> classGuidList = new ArrayList<>();
		List<String> tempList = this.stubService.getClassModelService().listAllSubClassGuid(classGuid);
		classGuidList.add(classGuid);
		if (tempList != null)
		{
			classGuidList.addAll(tempList);
		}
		List<ClassficationFeature> featureList = this.listAllClassficationFeature();
		if (!SetUtils.isNullList(featureList))
		{
			featureList = featureList.stream().filter(feature -> classGuidList.contains(feature.getClassGuid())).collect(Collectors.toList());
		}
		return featureList;
	}

	protected boolean isMasterClassification(String classGuid, String classificationItemGuid) throws ServiceRequestException
	{
		String groupGuid = this.stubService.getCodeModelService().getCodeItemByGuid(classificationItemGuid).getCodeGuid();
		List<ClassficationFeature> featureList = this.listAllSubClassficationFeature(classGuid);
		if (featureList != null)
		{
			Optional<ClassficationFeature> optional = featureList.stream().filter(feature -> feature.isMaster() && groupGuid.equals(feature.getClassificationfk())).findFirst();
			return optional.isPresent();
		}
		return false;
	}

	protected List<ClassficationFeatureItemInfo> listAllSuperClassificationFeatureItem(String featureGuid, String classificationItemGuid) throws ServiceRequestException
	{
		List<String> parentGuidList = new ArrayList<>();
		List<String> tempList = this.stubService.getCodeModelService().listAllParentClassificationGuid(classificationItemGuid);
		parentGuidList.add(classificationItemGuid);
		if (tempList != null)
		{
			parentGuidList.addAll(tempList);
		}
		List<ClassficationFeatureItemInfo> featureItemList = this.stubService.getSystemDataService().listFromCache(ClassficationFeatureItemInfo.class,
				new FieldValueEqualsFilter<ClassficationFeatureItemInfo>(ClassficationFeatureItemInfo.CLASSIFICATIONFEATTUREGUID, featureGuid));

		if (featureItemList != null)
		{
			featureItemList = featureItemList.stream().filter(item -> parentGuidList.contains(item.getClassificationItemGuid())).collect(Collectors.toList());
		}

		if (featureItemList != null)
		{
			featureItemList.forEach(item -> item.setInherit(!classificationItemGuid.equals(item.getClassificationItemGuid())));
		}

		return featureItemList;
	}

	protected List<ClassficationFeatureItemInfo> listAllSubClassificationFeatureItem(String featureGuid, String classificationItemGuid) throws ServiceRequestException
	{
		List<String> subGuidList = new ArrayList<>();
		List<String> tempList = this.stubService.getCodeModelService().listAllSubClassificationGuid(classificationItemGuid);
		subGuidList.add(classificationItemGuid);
		if (tempList != null)
		{
			subGuidList.addAll(tempList);
		}
		List<ClassficationFeatureItemInfo> featureItemList = this.stubService.getSystemDataService().listFromCache(ClassficationFeatureItemInfo.class,
				new FieldValueEqualsFilter<ClassficationFeatureItemInfo>(ClassficationFeatureItemInfo.CLASSIFICATIONFEATTUREGUID, featureGuid));
		if (featureItemList != null)
		{
			featureItemList = featureItemList.stream().filter(item -> subGuidList.contains(item.getClassificationItemGuid())).collect(Collectors.toList());
		}
		if (featureItemList != null)
		{
			featureItemList.forEach(item -> item.setInherit(!classificationItemGuid.equals(item.getClassificationItemGuid())));
		}
		return featureItemList;
	}

	protected ClassficationFeatureItemInfo getClassificationFeatureItemInfo(String featureItemGuid) throws ServiceRequestException
	{
		if (ClassficationFeatureItemCache.containsKey(featureItemGuid))
		{
			return ClassficationFeatureItemCache.get(featureItemGuid).getInfo();
		}
		else
		{
			return this.stubService.getSystemDataService().getObject(ClassficationFeatureItemInfo.class, featureItemGuid);
		}
	}

	protected ClassficationFeatureItem getClassificationFeatureItem(String featureItemGuid) throws ServiceRequestException
	{
		if (ClassficationFeatureItemCache.containsKey(featureItemGuid) == false)
		{
			ClassficationFeatureItemInfo info = this.getClassificationFeatureItemInfo(featureItemGuid);
			if (info != null)
			{

				ClassficationFeatureItem item = new ClassficationFeatureItem(info, listClassificationNumberField(info, true));
				ClassficationFeatureItemCache.put(featureItemGuid, item);
			}
		}
		return ClassficationFeatureItemCache.get(featureItemGuid);
	}

	protected String saveClassificationFeatureItem(ClassficationFeatureItem item) throws DynaDataException, ServiceRequestException
	{
		String tranid = StringUtils.generateRandomUID(32);
		try
		{
			String itemguid = null;
			if (StringUtils.isNullString(item.getGuid()))
			{
				itemguid = this.stubService.getSystemDataService().save(item.getInfo());
				List<ClassificationNumberField> list = item.getFieldList();
				saveClassificationNumberField(itemguid, list);
			}
			else
			{
				itemguid = item.getGuid();
				ClassficationFeatureItem oldItem = this.getClassificationFeatureItem(itemguid);
				ClassficationFeatureItemCache.remove(itemguid);
				this.stubService.getSystemDataService().save(item.getInfo());
				List<ClassificationNumberField> oldlist = oldItem.getFieldList();
				if (!SetUtils.isNullList(oldlist))
				{
					for (ClassificationNumberField cnf : oldlist)
					{
						this.stubService.getSystemDataService().delete(cnf);
					}
				}
				List<ClassificationNumberField> list = item.getFieldList();
				saveClassificationNumberField(itemguid, list);
			}
//			this.stubService.getTransactionService().commitTransaction(tranid);
			return itemguid;
		}
		catch (DynaDataException e)
		{
//			this.stubService.getTransactionService().rollbackTransaction(tranid);
			throw e;
		}
		catch (ServiceRequestException e)
		{
//			this.stubService.getTransactionService().rollbackTransaction(tranid);
			throw e;
		}
		catch (Exception e)
		{
//			this.stubService.getTransactionService().rollbackTransaction(tranid);
			throw ServiceRequestException.createByException("ID_APP_SERVER_EXCEPTION", e);
		}
	}

	protected void saveClassificationNumberField(String itemguid, List<ClassificationNumberField> list) throws ServiceRequestException
	{
		if (!SetUtils.isNullList(list))
		{
			int i = 0;
			for (ClassificationNumberField cnf : list)
			{
				if (cnf.getClassField() != null)
				{
					if ((cnf.getClassField().getType() == FieldTypeEnum.CODE || cnf.getClassField().getType() == FieldTypeEnum.CODEREF
							|| cnf.getClassField().getType() == FieldTypeEnum.MULTICODE || cnf.getClassField().getType() == FieldTypeEnum.OBJECT
							|| cnf.getClassField().getType() == FieldTypeEnum.DATE || cnf.getClassField().getType() == FieldTypeEnum.DATETIME)
							&& StringUtils.isNullString(cnf.getTypeValue()))
					{
						throw new ServiceRequestException("ID_APP_CLASSIFICATION_TYPEVALUE_NULL", "type value is not null", null, "");
					}
				}
				if ((cnf.getType() == CFMCodeRuleEnum.DATE || cnf.getType() == CFMCodeRuleEnum.DATETIME) && StringUtils.isNullString(cnf.getTypeValue()))
				{
					throw new ServiceRequestException("ID_APP_CLASSIFICATION_TYPEVALUE_NULL", "type value is not null", null, "");
				}
				cnf.setCFeatureItemGuid(itemguid);
				cnf.setSequence(++i);
				if (!StringUtils.isNullString(cnf.getControlledNumberFieldGuid()))
				{
					cnf.setControlledNumberFieldGuid(cnf.getControlledNumberFieldGuid().replace("-", ""));
				}
				if (!StringUtils.isNullString(cnf.getGuid()))
				{
					cnf.setGuid(cnf.getGuid().replace("-", ""));
				}
				this.stubService.getSystemDataService().save(cnf, false);
			}
		}
	}

	protected void delClassificationFeatureItem(String featureItemGuid) throws DynaDataException, ServiceRequestException
	{
		ClassficationFeatureItem item = this.getClassificationFeatureItem(featureItemGuid);
		if (item != null)
		{
			String tranid = StringUtils.generateRandomUID(32);
			try
			{
//				this.stubService.getTransactionService().startTransaction(tranid);
				ClassficationFeatureItemCache.remove(featureItemGuid);
				ClassficationFeatureItemInfo info = item.getInfo();
				List<ClassificationNumberField> list = item.getFieldList();
				if (!SetUtils.isNullList(list))
				{
					for (ClassificationNumberField cnf : list)
					{
						this.stubService.getSystemDataService().delete(cnf);
					}
				}
				this.stubService.getSystemDataService().delete(info);
//				this.stubService.getTransactionService().commitTransaction(tranid);
			}
			catch (DynaDataException e)
			{
//				this.stubService.getTransactionService().rollbackTransaction(tranid);
				throw e;
			}
			catch (Exception e)
			{
//				this.stubService.getTransactionService().rollbackTransaction(tranid);
				throw ServiceRequestException.createByException("ID_APP_SERVER_EXCEPTION", e);
			}

		}
	}

	private List<ClassificationNumberField> listClassificationNumberField(ClassficationFeatureItemInfo info, boolean needClassField) throws ServiceRequestException
	{
		SystemDataService sds = this.stubService.getSystemDataService();

		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put(ClassificationNumberField.CFEATUREITEMGUID, info.getGuid());
		List<ClassificationNumberField> classificationNumberFieldList = sds.listFromCache(ClassificationNumberField.class,
				new FieldValueEqualsFilter<ClassificationNumberField>(ClassificationNumberField.CFEATUREITEMGUID, info.getGuid()));

		if (needClassField && !SetUtils.isNullList(classificationNumberFieldList))
		{
			String classGuid = null;
			CodeItemInfo codeitemInfo = null;
			if (StringUtils.isNullString(info.getClassificationFeatureGuid()))
			{
				classGuid = info.getClassGuid();
			}
			else
			{
				codeitemInfo = this.stubService.getCodeModelService().getCodeItemInfoByGuid(info.getClassificationItemGuid());

				ClassficationFeature classficationFeature = this.getClassficationFeature(info.getClassificationFeatureGuid());
				if (classficationFeature != null)
				{
					classGuid = classficationFeature.getClassGuid();
				}
			}
			ClassInfo classInfo = this.stubService.getClassModelService().getClassInfo(classGuid);
			for (ClassificationNumberField field : classificationNumberFieldList)
			{
				if (field.getType() == CFMCodeRuleEnum.FIELD)
				{
					if (field.isFormClass())
					{
						if (classInfo != null)
						{
							ClassField classField = this.stubService.getClassModelService().getField(classInfo.getName(), field.getFieldName());
							field.setClassField(classField);
						}
					}
					else
					{
						if (codeitemInfo != null)
						{
							ClassField classField = this.stubService.getCodeModelService().getFieldByItemGuid(codeitemInfo.getGuid(), field.getFieldName());
							field.setClassField(classField);
						}
					}
				}
			}
		}
		if (!SetUtils.isNullList(classificationNumberFieldList))
		{
			Collections.sort(classificationNumberFieldList, Comparator.comparing(ClassificationNumberField::getSequence));
		}
		return classificationNumberFieldList;
	}

	public List<ClassficationFeatureItemInfo> listAllClassficationFeatureItem()
	{
		return this.stubService.getSystemDataService().listFromCache(ClassficationFeatureItemInfo.class, null);
	}
}
