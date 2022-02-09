package dyna.app.startinit;

import dyna.net.service.brs.BOAS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/2/8
 **/
@Order(3)
@Component
public class BOASRunner implements CommandLineRunner
{
	@Autowired
	private BOAS boas;

	@Override public void run(String... args) throws Exception
	{
		boas.init();
	}
}
