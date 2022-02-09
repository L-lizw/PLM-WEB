package dyna.data.startinit;

import dyna.net.service.data.RelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/1/17
 **/
@Component
public class RelationServiceRunner implements CommandLineRunner
{
	@Autowired
	private RelationService relationService;

	@Override public void run(String... args) throws Exception
	{
		relationService.init();
	}
}
