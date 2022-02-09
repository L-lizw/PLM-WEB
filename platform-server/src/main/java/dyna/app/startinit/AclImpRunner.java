package dyna.app.startinit;

import dyna.app.service.brs.acl.ACLImpl;
import dyna.net.service.brs.ACL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/2/8
 **/
@Order(2)
@Component
public class AclImpRunner implements CommandLineRunner
{
	@Autowired
	private ACL acl;

	@Override public void run(String... args) throws Exception
	{
		acl.init();
	}
}
