package dyna.app.startinit;

import dyna.net.service.brs.EMM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/2/8
 **/
@Order(7)
@Component
public class EMMRunner implements CommandLineRunner
{
	@Autowired
	private EMM emm;

	@Override public void run(String... args) throws Exception
	{
		emm.init();
	}
}
