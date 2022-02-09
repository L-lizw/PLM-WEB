/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: ServiceConfigBean
 * Wanglei 2010-3-30
 */
package dyna.common.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 服务配置定义
 * 
 * @author Wanglei
 * 
 */
@Data
public class ServiceDefinition
{

	private String		servcieID				;

	private String		servcieName				;

	private String		servcieDescription		;

	private Map<String, String> param;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(this.servcieName);
		sb.append("[" + this.servcieID + "]");
		return sb.toString();
	}

}


