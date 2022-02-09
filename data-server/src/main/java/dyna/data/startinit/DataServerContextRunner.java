package dyna.data.startinit;

import dyna.common.Version;
import dyna.common.log.DynaLogger;
import dyna.dbcommon.function.DatabaseFunctionFactory;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/1/16
 **/
@Order(1)
@Component
public class DataServerContextRunner implements CommandLineRunner
{
	@Autowired SqlSessionFactory sqlSessionFactory;

	@Override public void run(String... args) throws Exception
	{
		DynaLogger.info("***************************************************");
		DynaLogger.info("************* " + Version.getProductName() + " " + Version.getVersionInfo() + "**********");
		DynaLogger.info("************* " + Version.getCopyRight() + " ****************");
		DynaLogger.info("***************************************************");
		DynaLogger.info("Data Server initialize...");
		DynaLogger.print("\tConnecting to Database...");
		// mapper initialize.
		String configFile = "dm/mybatis-config.xml";

		DatabaseFunctionFactory.databaseType = sqlSessionFactory.getConfiguration().getDatabaseId();
		DynaLogger.println("[OK]");
	}
}
