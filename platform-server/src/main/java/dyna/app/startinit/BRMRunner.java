package dyna.app.startinit;

import dyna.net.service.brs.BRM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/2/8
 **/
@Component
@Order(4)
public class BRMRunner implements CommandLineRunner
{
	@Autowired
	private BRM     brm;

	@Override public void run(String... args) throws Exception
	{
		brm.init();
	}
}
