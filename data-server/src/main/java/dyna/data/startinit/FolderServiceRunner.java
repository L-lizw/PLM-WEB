package dyna.data.startinit;

import dyna.net.service.data.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/1/17
 **/
@Component
public class FolderServiceRunner implements CommandLineRunner
{
	@Autowired
	private FolderService folderService;

	@Override public void run(String... args) throws Exception
	{
		folderService.init();
	}
}
