package dyna.app.startinit;

import dyna.net.service.brs.WFI;
import dyna.net.service.brs.WFM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/2/8
 **/
@Order(12)
@Component
public class WFIRunner implements CommandLineRunner
{
	@Autowired
	private WFI wfi;

	@Override public void run(String... args) throws Exception
	{
		wfi.init();
	}
}
