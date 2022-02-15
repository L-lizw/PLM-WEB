package dyna.data.common.typehandler;

import dyna.common.util.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Lizw
 * @date 2022/2/15
 **/
@MappedTypes({String.class})
@MappedJdbcTypes({JdbcType.VARCHAR})
public class StringTypeHandler implements TypeHandler<String>
{

	@Override public void setParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException
	{
		ps.setString(i, StringUtils.isNullString(parameter)? null: parameter);
	}

	@Override public String getResult(ResultSet rs, String columnName) throws SQLException
	{
		return rs.getString(columnName);
	}

	@Override public String getResult(ResultSet rs, int columnIndex) throws SQLException
	{
		return rs.getString(columnIndex);
	}

	@Override public String getResult(CallableStatement cs, int columnIndex) throws SQLException
	{
		return cs.getString(columnIndex);
	}
}
