package dyna.data.startinit;

import dyna.net.service.data.model.InterfaceModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/1/17
 **/
@Order(4)
@Component
public class InterfaceModelServiceRunner implements CommandLineRunner
{
	@Autowired
	private InterfaceModelService interfaceModelService;

	@Override public void run(String... args) throws Exception
	{
		interfaceModelService.init();
	}
}
