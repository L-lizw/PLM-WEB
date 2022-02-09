package dyna.data.startinit;

import dyna.net.service.data.ConfigManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/1/17
 **/
@Component
public class ConfigManagerServiceRunner implements CommandLineRunner
{
	@Autowired
	private ConfigManagerService configManagerService;

	@Override public void run(String... args) throws Exception
	{
		configManagerService.init();
	}
}
