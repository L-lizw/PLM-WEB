package dyna.app.startinit;

import dyna.net.service.brs.CPB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/2/8
 **/
@Order(5)
@Component
public class CPBRunner implements CommandLineRunner
{
	@Autowired
	private CPB cpb;

	@Override public void run(String... args) throws Exception
	{
		cpb.init();
	}
}
