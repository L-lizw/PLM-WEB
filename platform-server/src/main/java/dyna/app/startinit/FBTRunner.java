package dyna.app.startinit;

import dyna.net.service.brs.FBTS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;

/**
 * @author Lizw
 * @date 2022/2/8
 **/
@Order(8)
public class FBTRunner implements CommandLineRunner
{
	@Autowired
	private FBTS fbt;

	@Override public void run(String... args) throws Exception
	{
		fbt.init();
	}
}
