package dyna.app.startinit;

import dyna.net.service.brs.DCR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/2/8
 **/
@Order(6)
@Component
public class DCRRunner implements CommandLineRunner
{
	@Autowired
	private DCR dcr;

	@Override public void run(String... args) throws Exception
	{
		dcr.init();
	}
}
