/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: ServiceStub 服务分支
 * Wanglei 2011-3-24
 */
package dyna.app.service;

import dyna.app.server.context.ApplicationServerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 * 服务分支
 * 
 * @author Lizw
 * 
 */
public abstract class AbstractServiceStub<T extends DataAccessService>
{
	@Autowired
	protected ApplicationServerContext serverContext	;
	@Autowired
	protected T                        stubService		 ;
}
