package dyna.data.startinit;

import dyna.data.common.AutoScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/1/23
 **/
@Order(2)
@Component
public class DynaAutoScanRunner implements CommandLineRunner
{

	@Autowired
	private AutoScan autoScan;

	@Override public void run(String... args) throws Exception
	{
		autoScan.init();
	}
}
