package dyna.data.startinit;

import dyna.common.log.DynaLogger;
import dyna.net.service.data.AclService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/1/17
 **/
@Component
public class AclServiceRunner implements CommandLineRunner
{
	@Autowired
	private AclService aclService;

	@Override public void run(String... args) throws Exception
	{
		aclService.init();
		DynaLogger.info("Loading Service[" + "AclService" + "]  success!");
	}
}
