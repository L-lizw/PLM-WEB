package dyna.app.startinit;

import dyna.net.service.brs.UECS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/2/8
 **/
@Order(11)
@Component
public class UECSRunner implements CommandLineRunner
{
	@Autowired
	private UECS uecs;

	@Override public void run(String... args) throws Exception
	{
		uecs.init();
	}
}
