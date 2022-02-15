package dyna.data.common.typehandler;

import dyna.common.util.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Lizw
 * @date 2022/2/15
 **/
public class NumberTypeHandler implements TypeHandler<Integer>
{
	@Override public void setParameter(PreparedStatement ps, int i, Integer parameter, JdbcType jdbcType) throws SQLException
	{
		ps.setInt(i, parameter == null ? 0 : parameter);
	}

	@Override public Integer getResult(ResultSet rs, String columnName) throws SQLException
	{
		return rs.getInt(columnName);
	}

	@Override public Integer getResult(ResultSet rs, int columnIndex) throws SQLException
	{
		return rs.getInt(columnIndex);
	}

	@Override public Integer getResult(CallableStatement cs, int columnIndex) throws SQLException
	{
		return cs.getInt(columnIndex);
	}
}
