package dyna.data.sqlbuilder;

import dyna.common.SearchConditionImpl;
import dyna.common.bean.model.code.CodeItem;
import dyna.common.dto.model.cls.ClassField;
import dyna.common.exception.ServiceNotFoundException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.systemenum.DataExceptionEnum;
import dyna.common.systemenum.FieldTypeEnum;
import dyna.common.util.SetUtils;
import dyna.dbcommon.exception.DynaDataExceptionAll;
import dyna.net.service.data.DSCommonService;
import dyna.net.service.data.model.ClassModelService;
import dyna.net.service.data.model.CodeModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dyna.common.bean.data.FoundationObjectImpl.separator;

@Component
public class CommonSqlBuilder
{
	@Autowired ClassModelService classModelService;
	@Autowired CodeModelService  codeModelService;
	@Autowired DSCommonService   dsCommonService;
	/**
	 * @param searchCondition
	 * @return
	 * @throws ServiceNotFoundException
	 * @throws ServiceRequestException
	 */
	public  String getSqlOrderBy(String mainTableAlias, String classificationAlias, SearchConditionImpl searchCondition, boolean isOnlySystemField)
			throws ServiceRequestException
	{
		if (SetUtils.isNullList(searchCondition.getOrderMapList()))
		{
			return "";
		}
		String className = searchCondition.getObjectGuid().getClassName();
		StringBuilder orderClauseSb = new StringBuilder();
		List<String> tableNameList = new ArrayList<>();
		String sysTableName = dsCommonService.getTableName(className);
		for (Map<String, Boolean> orderMap : searchCondition.getOrderMapList())
		{
			// code、multcode、classification、status等类型按照guid排序
			String fieldName = (String) orderMap.keySet().toArray()[0];
			ClassField field = classModelService.getClassObject(className).getField(fieldName);
			// 如果是系统字段
			if (isOnlySystemField && field != null && field.isSystem())
			{
				orderClauseSb.append(mainTableAlias).append(".").append(fieldName, 0, fieldName.lastIndexOf('$') > -1 ? fieldName.lastIndexOf('$') : fieldName.length());
				if (!orderMap.get(fieldName))
				{
					orderClauseSb.append(" DESC");
				}
				orderClauseSb.append(",");
			}
			// 分类上的字段，其字段名由分类group名和字段名用“#”连接而成
			else if (fieldName.contains(separator) && fieldName.split("\\" + separator).length == 2)
			{
				String classificationItem = searchCondition.getClassification();
				CodeItem codeItem = codeModelService.getCodeItemByGuid(classificationItem);
				boolean isAsc = orderMap.get(fieldName);

				fieldName = fieldName.split("\\" + separator)[1];
				field = codeItem.getField(fieldName);
				if (field == null)
				{
					throw new DynaDataExceptionAll(fieldName, null, DataExceptionEnum.DS_NO_FIELD);
				}

				FieldTypeEnum fieldType = field.getType();
				String columnName = classificationAlias + "." + field.getColumnName();

				orderClauseSb.append(getOrderBySql(columnName, fieldType, isAsc));
			}
			else if (!isOnlySystemField)
			{
				// 如果不是系统字段
				if (field == null)
				{
					throw new DynaDataExceptionAll(fieldName, null, DataExceptionEnum.DS_NO_FIELD);
				}

				String tableName = dsCommonService.getTableName(className, fieldName);
				if (!sysTableName.equals(tableName) && !tableNameList.contains(tableName))
				{
					tableNameList.add(tableName);
				}

				orderClauseSb.append(dsCommonService.getFieldColumn(mainTableAlias, className + "." + fieldName));
				if (!orderMap.get(fieldName))
				{
					orderClauseSb.append(" DESC");
				}
				orderClauseSb.append(",");
			}
		}
		return orderClauseSb.substring(0, orderClauseSb.lastIndexOf(","));
	}

	/**
	 * 取得指定字段的排序sql
	 *
	 * @param columnName
	 *            字段对应的列名
	 * @param fieldType
	 *            字段类型
	 * @param isAsc
	 *            是否升序排序
	 * @return
	 */
	private static String getOrderBySql(String columnName, FieldTypeEnum fieldType, boolean isAsc)
	{
		StringBuilder orderClauseSb = new StringBuilder();
		if (fieldType.equals(FieldTypeEnum.OBJECT))
		{
			if (columnName.endsWith("$"))
			{
				columnName = columnName.substring(0, columnName.indexOf("$"));
			}

			orderClauseSb.append(columnName).append("$master");
			if (!isAsc)
			{
				orderClauseSb.append(" DESC");
			}
			orderClauseSb.append(",");
		}

		orderClauseSb.append(columnName);
		if (!isAsc)
		{
			orderClauseSb.append(" DESC");
		}
		orderClauseSb.append(",");

		return orderClauseSb.toString();
	}
}
