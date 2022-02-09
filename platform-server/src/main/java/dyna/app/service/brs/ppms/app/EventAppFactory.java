/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: EventAppFactory
 * WangLH 2012-06-26
 */
package dyna.app.service.brs.ppms.app;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * 
 */
@Component
public class EventAppFactory
{
	@Autowired
	private final Map<String, EventApp>	APP_MAP	= new HashMap<String, EventApp>();

	public EventApp getEventApp(String appName)
	{
		return this.APP_MAP.get(appName);
	}

}
