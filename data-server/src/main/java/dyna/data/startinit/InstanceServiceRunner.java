package dyna.data.startinit;

import dyna.net.service.data.InstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/1/17
 **/
@Component
public class InstanceServiceRunner implements CommandLineRunner
{
	@Autowired
	private InstanceService instanceService;

	@Override public void run(String... args) throws Exception
	{
		instanceService.init();
	}
}
