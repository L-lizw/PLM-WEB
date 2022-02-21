/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: DynaServiceConfig
 * Wanglei 2010-3-30
 */
package dyna.data.conf.yml;

import dyna.common.conf.ServiceDefinition;
import dyna.common.util.SetUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

/**
 * 读取服务配置信息, 并缓存, 供服务调用查询
 * 
 * @author Lizw
 * 
 */
@Data
@Component
@ConfigurationProperties(prefix = "dataservice")
public class ConfigurableServiceImpl
{
	private Map<String, ServiceDefinition> services;


	/**
	 * 服务的数量
	 * 
	 * @return
	 */
	public int getServiceDefinitionSize()
	{
		return this.services.size();
	}

	/**
	 * 枚举配置文件中的所有服务定义
	 * 
	 * @return 返回枚举
	 */
	public Iterator<ServiceDefinition> getServiceDefinitions()
	{
		return this.services.values().iterator();
	}

	/**
	 * 获取指定的服务定义
	 * 
	 * @param serviceID
	 * @return
	 */
	public ServiceDefinition getServiceDefinition(String serviceID)
	{
		return this.services.get(serviceID);
	}

	public ServiceDefinition getServiceDefinitionByclass(String className)
	{
		if(!SetUtils.isNullMap(services))
		{
			for(ServiceDefinition serviceDefinition:services.values())
			{
				if(serviceDefinition.getName().equals(className))
				{
					return serviceDefinition;
				}
			}
		}
		return null;
	}



}
