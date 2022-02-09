package dyna.data.startinit;

import dyna.net.service.data.SystemDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/1/17
 **/
@Order(3)
@Component
public class SystemDataServiceRunner implements CommandLineRunner
{
	@Autowired
	private SystemDataService systemDataService;

	@Override public void run(String... args) throws Exception
	{
		systemDataService.init();
	}
}
