package dyna.data.startinit;

import dyna.net.service.data.model.ClassModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/1/17
 **/
@Component
public class ClassModelServiceRunner implements CommandLineRunner
{
	@Autowired
	private ClassModelService classModelService;

	@Override public void run(String... args) throws Exception
	{
		classModelService.init();
	}
}
