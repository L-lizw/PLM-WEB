package dyna.app.service;

import dyna.app.server.context.ApplicationServerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author Lizw
 * @date 2022/2/1
 **/
public abstract class AbstractQuartzJobStub<T extends DataAccessService> extends QuartzJobBean
{
	@Autowired
	protected ApplicationServerContext serverContext ;
	@Autowired
	protected T                        stubService    ;
}
