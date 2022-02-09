package dyna.data.startinit;

import dyna.net.service.data.ECService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/1/17
 **/
@Component
public class ECServiceRunner implements CommandLineRunner
{
	@Autowired
	private ECService ecService;

	@Override public void run(String... args) throws Exception
	{
		ecService.init();
	}
}
