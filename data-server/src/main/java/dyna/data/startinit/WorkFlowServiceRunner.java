package dyna.data.startinit;

import dyna.net.service.data.WorkFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/1/17
 **/
@Component
public class WorkFlowServiceRunner implements CommandLineRunner
{
	@Autowired
	private WorkFlowService workFlowService;

	@Override public void run(String... args) throws Exception
	{
		workFlowService.init();
	}
}
