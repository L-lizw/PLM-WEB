package dyna.data.startinit;

import dyna.net.service.data.DSToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/1/17
 **/
@Component
public class DSToolServiceRunner implements CommandLineRunner
{
	@Autowired
	private DSToolService dsToolService;

	@Override public void run(String... args) throws Exception
	{
		dsToolService.init();
	}
}
