package dyna.data.startinit;

import dyna.net.service.data.model.CodeModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/1/17
 **/
@Component
public class CodeModelServiceRunner implements CommandLineRunner
{
	@Autowired
	private CodeModelService codeModelService;

	@Override public void run(String... args) throws Exception
	{
		codeModelService.init();
	}
}
