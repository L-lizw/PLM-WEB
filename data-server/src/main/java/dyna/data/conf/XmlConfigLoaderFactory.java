/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: ConfigLoaderFactory
 * Wanglei 2010-11-26
 */
package dyna.data.conf;

import dyna.common.conf.loader.ConfigLoaderConnToDSImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Wanglei
 * 
 */
@Component
public class XmlConfigLoaderFactory
{
	@Autowired
	private        ConfigLoaderConnToDSImpl   connToDSLoader = null;

	public  ConfigLoaderConnToDSImpl getLoader4ConnToDS()
	{
		return connToDSLoader;
	}

	public void init()
	{
		this.connToDSLoader.load();
	}

}
