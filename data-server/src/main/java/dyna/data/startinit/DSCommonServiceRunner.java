package dyna.data.startinit;

import dyna.net.service.data.DSCommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/1/17
 **/
@Component
public class DSCommonServiceRunner implements CommandLineRunner
{
	@Autowired
	private DSCommonService dsCommonService;

	@Override public void run(String... args) throws Exception
	{
		dsCommonService.init();
	}
}
