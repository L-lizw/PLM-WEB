/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: ConfigLoaderFactory
 * Wanglei 2010-11-26
 */
package dyna.common.conf.loader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Wanglei
 */
@Component
public class ConfigLoaderFactory
{
	@Autowired
	private  ConfigLoaderClientImpl            clientLoader   = null;
	@Autowired
	private  ConfigLoaderDSServerImpl          dsserverLoader = null;
	@Autowired
	private  ConfigLoaderMSRImpl               msrLoader      = null;
	@Autowired
	private  ConfigLoaderConnToDSImpl   connToDSLoader = null;

	public  ConfigLoaderClientImpl getLoader4Client()
	{
		return clientLoader;
	}

	public  ConfigLoaderDSServerImpl getLoader4DSServer()
	{
		return dsserverLoader;
	}

	public  ConfigLoaderMSRImpl getLoader4MSR()
	{
		return msrLoader;
	}

	public  ConfigLoaderConnToDSImpl getLoader4ConnToDS()
	{
		return connToDSLoader;
	}


}
