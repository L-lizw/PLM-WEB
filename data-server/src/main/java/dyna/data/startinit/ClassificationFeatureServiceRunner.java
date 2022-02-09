package dyna.data.startinit;

import dyna.net.service.data.model.ClassificationFeatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/1/17
 **/
@Component
public class ClassificationFeatureServiceRunner  implements CommandLineRunner
{
	@Autowired
	private ClassificationFeatureService classificationFeatureService;

	@Override public void run(String... args) throws Exception
	{
		classificationFeatureService.init();
	}
}
