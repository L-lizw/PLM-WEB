package dyna.data.sqlbuilder;

import dyna.common.FieldOrignTypeEnum;
import dyna.common.SearchCondition;
import dyna.common.bean.data.FoundationObjectImpl;
import dyna.common.bean.data.foundation.ViewObject;
import dyna.common.bean.model.cls.ClassObject;
import dyna.common.bean.model.code.CodeItem;
import dyna.common.bean.model.code.CodeObject;
import dyna.common.dto.Session;
import dyna.common.dto.model.cls.ClassField;
import dyna.common.exception.DynaDataException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.systemenum.*;
import dyna.common.util.StringUtils;
import dyna.dbcommon.exception.DynaDataExceptionAll;
import dyna.dbcommon.util.DSCommonUtil;
import dyna.net.service.data.DSCommonService;
import dyna.net.service.data.model.ClassModelService;
import dyna.net.service.data.model.CodeModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dyna.common.bean.data.FoundationObjectImpl.separator;

@Component
public class MutiTableSelectUtil
{

	@Autowired private DSCommonService   dsCommonService;
	@Autowired private CodeModelService  codeModelService;
	@Autowired private ClassModelService classModelService;

	public String getClassificationSqlSelect(String mainTableAlias, SearchCondition searchCondition, String sessionId, Map<String, String> classificationFieldMap,
			String className) throws ServiceRequestException
	{
		String classificationItem = searchCondition.getClassification();
		String masterClassification = null;
		if (StringUtils.isGuid(classificationItem))
		{
			CodeItem codeItem = codeModelService.getCodeItemByGuid(classificationItem);
			if (codeItem == null)
			{
				throw new DynaDataExceptionAll("query error ,the classification item is not exist. the classification item is " + classificationItem, null,
						DataExceptionEnum.DS_NOTEXISTS_CLASSIFICATIONITEM, classificationItem);
			}

			masterClassification = codeItem.getCodeGuid();
		}

		StringBuilder selectColumnSb = new StringBuilder();
		Session session = dsCommonService.getSession(sessionId);
		if (AdvancedQueryTypeEnum.CLASSIFICATION.equals(searchCondition.getSearchType()))
		{
			if (classificationItem != null && masterClassification != null)
			{
				CodeItem codeItem = codeModelService.getCodeItemByGuid(classificationItem);
				CodeObject codeObject = codeModelService.getCodeObjectByGuid(masterClassification);
				List<ClassField> fields = codeItem.getFieldList();

				if (!StringUtils.isNullString(codeObject.getBaseTableName()))
				{
					for (ClassField field : fields)
					{
						String fieldName = field.getName();
						if (StringUtils.isNullString(fieldName) || fieldName.startsWith("SEPARATOR$"))
						{
							continue;
						}

						String fieldKey = codeObject.getName() + separator + field.getName();
						String columnKey = mainTableAlias + field.getColumnName();
						if (classificationFieldMap != null)
						{
							classificationFieldMap.put(columnKey + separator + field.getType().toString(), fieldKey);
						}
						else
						{
							columnKey = fieldKey;
							classificationFieldMap = new HashMap<>();
						}

						if (field.getType().equals(FieldTypeEnum.OBJECT))
						{
							String columnName = mainTableAlias + "." + field.getColumnName();

							ClassField classField = new ClassField();
							classField.putAll(field);
							ObjectFieldTypeEnum fieldType = dsCommonService.getObjectFieldTypeOfField(classField, session.getBizModelName());
							if (fieldType != null)
							{
								String tmpFieldName = columnKey;
								if (fieldType == ObjectFieldTypeEnum.OBJECT)
								{
									selectColumnSb.append(columnName).append(" as ").append(tmpFieldName).append(",");
									selectColumnSb.append(columnName).append("$class as ").append(tmpFieldName).append("$class,");
									selectColumnSb.append(columnName).append("$master as ").append(tmpFieldName).append("$master,");
								}
								else
								{
									selectColumnSb.append(columnName).append(" as ").append(tmpFieldName).append(",");
								}
							}
						}
						else
						{
							selectColumnSb.append(mainTableAlias).append(".").append(field.getColumnName()).append(" AS ").append(columnKey).append(",");
						}
					}

					String classificationFieldName = SystemClassFieldEnum.CLASSIFICATION.getName();

					String columnKey = mainTableAlias + classificationFieldName;
					classificationFieldMap.put(columnKey + separator + "String", codeObject.getName() + separator + classificationFieldName);
					selectColumnSb.append(mainTableAlias).append(".CLASSIFICATIONITEMGUID").append(" AS ").append(columnKey).append(",");
				}
			}
		}

		if (selectColumnSb.toString().equals("") || selectColumnSb.lastIndexOf(",") == -1)
		{
			return "";
		}
		return selectColumnSb.toString().substring(0, selectColumnSb.lastIndexOf(","));
	}

	/**
	 * 
	 * @param relationTableList
	 * @param relationTableMap
	 * @param searchCondition
	 * @param sessionId
	 * @return
	 * @throws DynaDataException
	 */
	public  String getFoundationSqlSelect(String mainTableAlias, String columnPrefix, List<String> relationTableList, Map<String, String> relationTableMap,
			SearchCondition searchCondition, String sessionId, String className) throws ServiceRequestException
	{

		StringBuilder selectColumnSb = new StringBuilder();
		Session session = dsCommonService.getSession(sessionId);
		if (columnPrefix == null)
		{
			columnPrefix = "";
		}
		else
		{
			columnPrefix = columnPrefix.trim();
		}
		if (className != null)
		{
			List<String> fieldNameList = dsCommonService.getAllFieldsFromSC(searchCondition);
			if (fieldNameList == null)
			{
				return "";
			}

			ClassObject classObject = classModelService.getClassObject(className);
			if (classObject == null)
			{
				return "";
			}

			List<String> alreadyUsedFieldList = new ArrayList<>();
			for (String fieldName : fieldNameList)
			{
				if (fieldName.startsWith(FieldOrignTypeEnum.CLASSIFICATION + FoundationObjectImpl.separator))
				{
					continue;
				}

				// 排除ui中定义的分割线 fixed by wanglei
				if (StringUtils.isNullString(fieldName) || fieldName.startsWith("SEPARATOR$"))
				{
					continue;
				}

				if (fieldName.startsWith(FieldOrignTypeEnum.CLASS + FoundationObjectImpl.separator))
				{
					fieldName = fieldName.substring(6);
				}

				if (alreadyUsedFieldList.contains(fieldName))
				{
					continue;
				}
				alreadyUsedFieldList.add(fieldName);

				if (classObject.hasInterface(ModelInterfaceEnum.IBOMView) && fieldName.equalsIgnoreCase(ViewObject.ISPRECISE))
				{
					boolean isContainTemplateId = false;
					for (String fieldName_ : fieldNameList)
					{
						if (ViewObject.TEMPLATE_ID.equalsIgnoreCase(fieldName_))
						{
							isContainTemplateId = true;
							break;
						}
					}
					if (!isContainTemplateId)
					{
						selectColumnSb.append(mainTableAlias).append(".").append(ViewObject.TEMPLATE_ID).append(",");
					}
					continue;
				}

				ClassField field = classObject.getField(fieldName);
				if (field == null)
				{
					throw new DynaDataExceptionAll(fieldName, null, DataExceptionEnum.DS_NO_FIELD);
				}

				String columnName = dsCommonService.getFieldColumn(mainTableAlias, classObject.getName() + "." + field.getName());
				String tableName = dsCommonService.getTableName(classObject.getName(), field.getName());
				String sysTableName = dsCommonService.getTableName(classObject.getName());
				if (!sysTableName.equals(tableName))
				{
					String alias = DSCommonUtil.getTableAlias(mainTableAlias, tableName);
					if (!relationTableMap.containsKey(tableName + " " + alias))
					{
						relationTableList.add(tableName + " " + alias);
						relationTableMap.put(tableName + " " + alias, mainTableAlias + ".GUID=" + alias + ".FOUNDATIONFK(+)");
					}
				}
				if (field.getType().equals(FieldTypeEnum.OBJECT))
				{
					ObjectFieldTypeEnum fieldType = dsCommonService.getObjectFieldTypeOfField(className, fieldName, session.getBizModelName());
					if (fieldType != null)
					{
						String tmpFieldName = fieldName;
						if (fieldName.endsWith("$"))
						{
							tmpFieldName = fieldName.substring(0, fieldName.length() - 1);
						}
						if (fieldType == ObjectFieldTypeEnum.OBJECT)
						{
							selectColumnSb.append(columnName).append(" as ").append(columnPrefix).append(fieldName).append(",");
							selectColumnSb.append(columnName).append("$class as ").append(columnPrefix).append(tmpFieldName).append("$class,");
							selectColumnSb.append(columnName).append("$master as ").append(columnPrefix).append(tmpFieldName).append("$master,");
						}
						else
						{
							selectColumnSb.append(columnName).append(" as ").append(columnPrefix).append(fieldName).append(",");
						}
					}
				}
				else if (field.isSystem())
				{
					selectColumnSb.append(columnName).append(" AS ").append(columnPrefix).append(fieldName).append(",");
				}
				else if (field.isBuiltin())
				{
					if (columnPrefix.length() == 0)
					{
						selectColumnSb.append(columnName).append(",");
					}
					else
					{
						selectColumnSb.append(columnName).append(" AS ").append(columnPrefix).append(fieldName).append(",");
					}
				}
				else
				{
					selectColumnSb.append(columnName).append(" AS ").append(columnPrefix).append(fieldName).append(",");
				}
			}
		}

		if (selectColumnSb.toString().equals("") || selectColumnSb.lastIndexOf(",") == -1)
		{
			return "";
		}
		return selectColumnSb.toString().substring(0, selectColumnSb.lastIndexOf(","));
	}

	public  String getSqlSelectByFeildList(String sessionId, String className, String mainTableAlias, String columnPrefix, List<ClassField> fieldList,
			List<String> relationTableList, Map<String, String> relationTableMap) throws ServiceRequestException
	{
		StringBuilder selectColumnSb = new StringBuilder();
		Session session = dsCommonService.getSession(sessionId);
		if (columnPrefix == null)
		{
			columnPrefix = "";
		}
		else
		{
			columnPrefix = columnPrefix.trim();
		}
		if (className != null)
		{
			for (ClassField field : fieldList)
			{
				if (field == null)
				{
					continue;
				}
				String columnName = dsCommonService.getFieldColumn(mainTableAlias, className + "." + field.getName());
				String tableName = dsCommonService.getTableName(className, field.getName());
				String sysTableName = dsCommonService.getTableName(className);
				if (!sysTableName.equals(tableName))
				{
					String alias = DSCommonUtil.getTableAlias(mainTableAlias, tableName);
					if (!relationTableMap.containsKey(tableName + " " + alias))
					{
						relationTableList.add(tableName + " " + alias);
						relationTableMap.put(tableName + " " + alias, mainTableAlias + ".GUID=" + alias + ".FOUNDATIONFK(+)");
					}
				}

				String fieldName = field.getName();
				if (field.getType().equals(FieldTypeEnum.OBJECT))
				{
					ObjectFieldTypeEnum fieldType = dsCommonService.getObjectFieldTypeOfField(className, fieldName, session.getBizModelName());
					if (fieldType != null)
					{
						String tmpFieldName = fieldName;
						if (fieldName.endsWith("$"))
						{
							tmpFieldName = fieldName.substring(0, fieldName.length() - 1);
						}
						if (fieldType == ObjectFieldTypeEnum.OBJECT)
						{
							selectColumnSb.append(columnName).append(" as ").append(columnPrefix).append(fieldName).append(",");
							selectColumnSb.append(columnName).append("$class as ").append(columnPrefix).append(tmpFieldName).append("$class,");
							selectColumnSb.append(columnName).append("$master as ").append(columnPrefix).append(tmpFieldName).append("$master,");
						}
						else
						{
							selectColumnSb.append(columnName).append(" as ").append(columnPrefix).append(fieldName).append(",");
						}
					}
				}
				else if (field.isSystem())
				{
					selectColumnSb.append(columnName).append(" AS ").append(columnPrefix).append(fieldName).append(",");
				}
				else if (field.isBuiltin())
				{
					if (columnPrefix.length() == 0)
					{
						selectColumnSb.append(columnName).append(",");
					}
					else
					{
						selectColumnSb.append(columnName).append(" AS ").append(columnPrefix).append(fieldName).append(",");
					}
				}
				else
				{
					selectColumnSb.append(columnName).append(" AS ").append(columnPrefix).append(fieldName).append(",");
				}
			}
		}

		if (selectColumnSb.toString().equals("") || selectColumnSb.lastIndexOf(",") == -1)
		{
			return "";
		}
		return selectColumnSb.toString().substring(0, selectColumnSb.lastIndexOf(","));
	}

}
