/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: ConfigurableServerImpl
 * Wanglei 2010-3-30
 */
package dyna.app.conf.yml;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dyna.common.bean.serv.DSServerBean;
import dyna.common.bean.serv.DSStorage;
import dyna.common.systemenum.LanguageEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * 读取服务器配置信息, 并保存, 供服务器获取初始化参数使用.
 * 
 * @author Wanglei
 * 
 */
@Data
@Component
@ConfigurationProperties(prefix = "plmserver")
public class ConfigurableServerImpl
{

	private static final int DEFAULT_TIMEOUT = 240;

	private String           id;
	private String           description;

	private String  ip;
	private Integer sessionTimeout           = DEFAULT_TIMEOUT;
	private Integer sessionPromptTime;
	private Integer threadPoolCount;
	private Integer threadPoolDelay;
	private Integer scheduledThreadPoolCount;

	private final Map<String, DSServerBean>	dsserverBeanMap				= new HashMap<String, DSServerBean>();
	private final Map<String, DSStorage>	storageMap					= new HashMap<String, DSStorage>();
	private LanguageEnum					lang						= LanguageEnum.EN;

	public void addDSServerBean(DSServerBean dsserverBean)
	{
		this.dsserverBeanMap.put(dsserverBean.getId(), dsserverBean);
	}

	public void addDSStorage(DSStorage storage)
	{
		if (this.storageMap.containsKey(storage.getId()))
		{
			return;
		}
		this.storageMap.put(storage.getId(), storage);
	}

	public DSServerBean getDSServerBean(String ftpId)
	{
		return this.dsserverBeanMap.get(ftpId);
	}

	public DSStorage getDSStorage(String storageId)
	{
		return this.storageMap.get(storageId);
	}

	/**
	 * @param sessionTimeout
	 *            the sessionTimeout to set
	 */
	public void setSessionTimeout(Integer sessionTimeout)
	{
		if (sessionTimeout != null && sessionTimeout.intValue() > 0)
		{
			this.sessionTimeout = sessionTimeout;
		}
	}

	/**
	 * @param lang
	 */
	public void setLanguage(LanguageEnum lang)
	{
		this.lang = lang;
	}

	/**
	 * @return the lang
	 */
	public LanguageEnum getLanguage()
	{
		return lang;
	}




}
