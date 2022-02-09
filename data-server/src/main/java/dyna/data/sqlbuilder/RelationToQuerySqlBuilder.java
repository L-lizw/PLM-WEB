package dyna.data.sqlbuilder;

import dyna.common.SearchCondition;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.bean.data.StructureObject;
import dyna.common.dto.Session;
import dyna.common.exception.ServiceNotFoundException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.sqlbuilder.plmdynamic.SqlParamData;
import dyna.common.sqlbuilder.plmdynamic.select.DynamicSelectParamData;
import dyna.common.systemenum.FieldTypeEnum;
import dyna.common.util.StringUtils;
import dyna.dbcommon.util.DSCommonUtil;
import dyna.net.service.data.DSCommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RelationToQuerySqlBuilder
{

	@Autowired private DSCommonService dsCommonService;

	@Autowired private MutiTableCriterionUtil mutiTableCriterionUtil;
	@Autowired private MutiTableSelectUtil mutiTableSelectUtil;

	public  DynamicSelectParamData buildInstanceSearchParamData(String sessionId, ObjectGuid end2ObjectGuid, String templateName, String viewClassName, boolean isPrice,
			SearchCondition end1SearchCondition, SearchCondition struSearchCondition, boolean isViewHistory) throws ServiceNotFoundException, ServiceRequestException
	{

		Session session = dsCommonService.getSession(sessionId);
		DynamicSelectParamData paramData = new DynamicSelectParamData();

		String struClassname = struSearchCondition.getObjectGuid().getClassName();
		String end1Classname = end1SearchCondition.getObjectGuid().getClassName();
		String struTableName = dsCommonService.getTableName(struClassname);
		String end1tableName = dsCommonService.getTableName(end1Classname);
		String viewtableName = dsCommonService.getTableName(viewClassName);
		Map<String, String> relationTableMap = new HashMap<>();
		List<String> relationTableList = new ArrayList<>();
		List<SqlParamData> paramList = new ArrayList<>();

		paramData.setTableName(end1tableName + " f ");

		paramData.setJoinTableList(relationTableList);
		paramData.setJoinTableMap(relationTableMap);

		paramData.setWhereParamList(paramList);

		relationTableList.add(viewtableName + " v ");
		relationTableMap.put(viewtableName + " v ", "v.end1 = f.guid and v.md_name=?");
		paramList.add(new SqlParamData("md_name", templateName, DSCommonUtil.getJavaTypeOfField(FieldTypeEnum.STRING)));
		relationTableList.add(struTableName + " s ");
		relationTableMap.put(struTableName + " s ", "v.guid = s.viewfk");

		String end1Select = mutiTableSelectUtil.getFoundationSqlSelect("f", null, relationTableList, relationTableMap, end1SearchCondition, sessionId, end1Classname);
		String struSelect = mutiTableSelectUtil.getFoundationSqlSelect("s", StructureObject.STRUCTURE_DOLLAR_PREFIX, relationTableList, relationTableMap, struSearchCondition,
				sessionId, struClassname);
		if (!StringUtils.isNullString(struSelect))
		{
			end1Select = end1Select + "," + struSelect;
		}
		paramData.setFieldSql(end1Select);

		String end1WhereSql = mutiTableCriterionUtil.getSqlWhere(end1tableName, "f", null, relationTableList, relationTableMap, end1SearchCondition, sessionId, paramList);
		String struWhereSql = mutiTableCriterionUtil.getSqlWhere(struTableName, "s", null, relationTableList, relationTableMap, struSearchCondition, sessionId, paramList);

		String whereSql = end1WhereSql + " and " + struWhereSql;
		if (isPrice)
		{
			whereSql = whereSql + " and s.end2 = ?";
			paramList.add(new SqlParamData("end2", end2ObjectGuid.getGuid(), DSCommonUtil.getJavaTypeOfField(FieldTypeEnum.STRING)));
		}
		else
		{
			if (isViewHistory)
			{
				whereSql = whereSql + " and s.end2$master = ?";
			}
			else
			{
				whereSql = whereSql + " and f.latestrevision like '%m%'  and s.end2$master = ?";
			}
			paramList.add(new SqlParamData("end2", end2ObjectGuid.getMasterGuid(), DSCommonUtil.getJavaTypeOfField(FieldTypeEnum.STRING)));
		}
		paramData.setWhereSql(whereSql);
		return paramData;
	}
}
