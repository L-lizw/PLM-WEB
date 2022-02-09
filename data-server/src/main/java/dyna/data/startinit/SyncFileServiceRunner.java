package dyna.data.startinit;

import dyna.net.syncfile.SyncFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/1/17
 **/
@Component
public class SyncFileServiceRunner implements CommandLineRunner
{
	//TODO
//	@Autowired
	private SyncFileService syncFileService;

	@Override public void run(String... args) throws Exception
	{
//		syncFileService.init();
	}
}
