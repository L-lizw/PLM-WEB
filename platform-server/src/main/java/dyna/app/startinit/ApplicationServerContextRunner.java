package dyna.app.startinit;

import dyna.app.server.context.ApplicationServerContext;
import dyna.common.Version;
import dyna.common.log.DynaLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/2/8
 **/
@Order(1)
@Component
public class ApplicationServerContextRunner implements CommandLineRunner
{

	@Autowired
	private ApplicationServerContext applicationServerContext;

	@Override public void run(String... args) throws Exception
	{
		DynaLogger.setAppLog();

		DynaLogger.info("***************************************************");
		DynaLogger.info("************* " + Version.getProductName() + " " + Version.getVersionInfo() + "**********");
		DynaLogger.info("************* " + Version.getCopyRight() + " ****************");
		DynaLogger.info("***************************************************");
		DynaLogger.info("Starting Server[" + this.getClass().getSimpleName() + "]");;

		DynaLogger.info("Server initialize...");
		applicationServerContext.init();
	}
}
