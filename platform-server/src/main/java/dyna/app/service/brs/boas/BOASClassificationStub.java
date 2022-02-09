package dyna.app.service.brs.boas;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.emm.EMMClassificationStub;
import dyna.app.service.helper.decorate.DecoratorFactory;
import dyna.common.SearchCondition;
import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.dto.cfm.ClassficationFeature;
import dyna.common.dto.model.cls.ClassField;
import dyna.common.dto.model.code.CodeObjectInfo;
import dyna.common.exception.DecorateException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.systemenum.FieldTypeEnum;
import dyna.common.util.SetUtils;
import dyna.common.util.StringUtils;
import dyna.net.service.brs.EMM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class BOASClassificationStub extends AbstractServiceStub<BOASImpl>
{

	@Autowired
	private DecoratorFactory decoratorFactory;

	protected void makeClassificationFoundation(FoundationObject foundationObject, SearchCondition searchCondition, String bmGuid, String iterationId)
			throws ServiceRequestException
	{
		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put(EMMClassificationStub.FOUNDATION_GUID, searchCondition.getObjectGuid().getGuid());
		if (!StringUtils.isNullString(iterationId))
		{
			filter.put("ITERATIONID", iterationId);
		}
		List<ClassficationFeature> listClassficationFeature = this.stubService.getEMM().listClassficationFeature(foundationObject.getObjectGuid().getClassGuid());
		if (!SetUtils.isNullList(listClassficationFeature))
		{
			for (ClassficationFeature feature : listClassficationFeature)
			{
				CodeObjectInfo codeObject = this.stubService.getEMM().getCode(feature.getClassificationfk());
				if (codeObject != null && codeObject.isHasFields() && !StringUtils.isNullString(codeObject.getBaseTableName()))
				{
					if (StringUtils.isNullString(iterationId))
					{
						filter.put("TABLENAME", "CF_" + codeObject.getBaseTableName());
					}
					else
					{
						filter.put("TABLENAME", "CF_" + codeObject.getBaseTableName() + "_I");
					}
					List<FoundationObject> classfiationList = this.stubService.getSystemDataService().query(FoundationObject.class, filter, "selectCFGuid");
					if (!SetUtils.isNullList(classfiationList))
					{
						FoundationObject classificationFoundation = this.getClassificationFoundation(searchCondition.getObjectGuid(),
								(String) classfiationList.get(0).get("CLASSIFICATIONITEMGUID"), iterationId, true, searchCondition, bmGuid);
						if (classificationFoundation != null)
						{
							foundationObject.addClassification(classificationFoundation, false);
						}
					}
					else if (feature.isMaster())
					{
						if (!StringUtils.isNullString(foundationObject.getClassificationGuid()))
						{
							FoundationObject classificationFoundation = this.stubService.newClassificationFoundation(foundationObject.getClassificationGuid(), null);
							foundationObject.addClassification(classificationFoundation, false);
						}
					}
				}
			}
		}
	}

	private FoundationObject getClassificationFoundation(ObjectGuid objectGuid, String classificationItemGuid, String iterationId, boolean decorator,
			SearchCondition searchCondition, String bmGuid) throws ServiceRequestException
	{
		String sessionId = this.stubService.getSignature().getCredential();
		FoundationObject classification = this.stubService.getInstanceService().queryForClassification(objectGuid.getGuid(), iterationId, classificationItemGuid, sessionId);
		if (decorator)
		{
			this.decorateClassification(classification, bmGuid, classificationItemGuid);
		}
		return classification;
	}

	public void decorateClassification(FoundationObject classification, String bmGuid, String classificationItemGuid) throws ServiceRequestException
	{
		if (classification != null)
		{
			EMM emm = this.stubService.getEMM();
			try
			{
				List<ClassField> listClassificationField = emm.listClassificationField(classificationItemGuid);
				Set<String> codeFieldSet = new HashSet<String>();
				Set<String> objectFieldSet = new HashSet<String>();
				if (!SetUtils.isNullList(listClassificationField))
				{
					for (ClassField field : listClassificationField)
					{
						if (field.getType() == FieldTypeEnum.CODE || field.getType() == FieldTypeEnum.MULTICODE)
						{
							codeFieldSet.add(field.getName().toUpperCase());
						}
						else if (field.getType() == FieldTypeEnum.OBJECT)
						{
							objectFieldSet.add(field.getName().toUpperCase());
						}
					}
				}
				if (!SetUtils.isNullSet(objectFieldSet))
				{
					decoratorFactory.decorateClassificaitonObject(objectFieldSet, classification, emm, bmGuid, null);
					decoratorFactory.ofd.decorateWithField(objectFieldSet, classification, emm, this.stubService.getSignature().getCredential(), true);
				}
				if (!SetUtils.isNullSet(codeFieldSet))
				{
					decoratorFactory.decorateFoundationObjectCode(codeFieldSet, classification, emm, bmGuid);
				}
			}
			catch (DecorateException e)
			{
				throw ServiceRequestException.createByDecorateException(e);
			}

		}
	}

}
