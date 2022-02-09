package dyna.data.sqlbuilder;

import dyna.common.bean.data.ObjectGuid;
import dyna.common.bean.model.cls.ClassObject;
import dyna.common.exception.ServiceRequestException;
import dyna.common.sqlbuilder.plmdynamic.SqlParamData;
import dyna.common.sqlbuilder.plmdynamic.select.DynamicSelectParamData;
import dyna.common.systemenum.FieldTypeEnum;
import dyna.common.util.StringUtils;
import dyna.dbcommon.util.DSCommonUtil;
import dyna.net.service.data.DSCommonService;
import dyna.net.service.data.model.ClassModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ClassInstanceGetSqlBuilder
{

	@Autowired private ClassModelService classModelService;
	@Autowired private DSCommonService   dsCommonService;

	@Autowired private MutiTableSelectUtil mutiTableSelectUtil;

	public  DynamicSelectParamData buildInstanceSearchParamData(ObjectGuid objectGuid, String sessionId) throws ServiceRequestException
	{

		DynamicSelectParamData paramData = new DynamicSelectParamData();
		String className = objectGuid.getClassName();
		ClassObject classInfo;
		if (StringUtils.isNull(className))
		{
			classInfo = classModelService.getClassObjectByGuid(objectGuid.getClassGuid());
			className = classInfo.getName();
		}
		else
		{
			classInfo = classModelService.getClassObject(className);
		}
		String tableName = dsCommonService.getTableName(classInfo.getName());

		Map<String, String> relationTableMap = new HashMap<>();
		List<String> relationTableList = new ArrayList<>();
		String selectSql = mutiTableSelectUtil.getSqlSelectByFeildList(sessionId, className, "f", null, classInfo.getFieldList(), relationTableList, relationTableMap);
		String whereSql = "f.guid=?";
		List<SqlParamData> paramList = new ArrayList<>();
		paramList.add(new SqlParamData("guid", objectGuid.getGuid(), DSCommonUtil.getJavaTypeOfField(FieldTypeEnum.STRING)));
		paramData.setTableName(tableName + " f");
		paramData.setFieldSql(selectSql);
		paramData.setWhereSql(whereSql);
		paramData.setWhereParamList(paramList);
		paramData.setJoinTableList(relationTableList);
		paramData.setJoinTableMap(relationTableMap);
		return paramData;
	}
}
