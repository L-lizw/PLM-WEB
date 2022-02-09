/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: NLSManager
 * Wanglei 2011-11-11
 */
package dyna.app.core.i18n;

import dyna.common.conf.ConfigurableMSRImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 多语言管理器
 * 
 * @author Wanglei
 * 
 */
@Component
public class NLSManager
{
	private static final Map<String, Map<String, String>>	msrCache	= new HashMap<String, Map<String, String>>();

	@Autowired
	private ConfigurableMSRImpl configurableMSR;

	public void loadStringRepository()
	{
		msrCache.putAll(configurableMSR.getConfig());
		configurableMSR.clearConfig();
	}

	public Map<String, String> getMSRMap(String locale)
	{
		return msrCache.get(locale);
	}

	public String getMSRString(String id, String locale)
	{
		Map<String, String> msrMap = this.getMSRMap(locale);
		if (msrMap == null)
		{
			return null;
		}
		return msrMap.get(id);
	}
}
