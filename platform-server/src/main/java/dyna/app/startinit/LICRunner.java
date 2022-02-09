package dyna.app.startinit;

import dyna.net.service.brs.LIC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/2/8
 **/
@Order(9)
@Component
public class LICRunner implements CommandLineRunner
{
	@Autowired
	private LIC lic;

	@Override public void run(String... args) throws Exception
	{
		lic.init();
	}
}
