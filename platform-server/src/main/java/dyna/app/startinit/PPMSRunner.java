package dyna.app.startinit;

import dyna.net.service.brs.PPMS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/2/8
 **/
@Order(10)
@Component
public class PPMSRunner implements CommandLineRunner
{
	@Autowired
	private PPMS ppms;
	@Override public void run(String... args) throws Exception
	{
		ppms.init();
	}
}
