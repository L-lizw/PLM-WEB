/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: ServiceConfigBean
 * Qiuxq 2012-4-24
 */
package dyna.common.conf;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.scheduling.support.CronSequenceGenerator;

import java.io.Serializable;



/**
 * Job类型配置定义
 * 
 * @author Qiuxq
 * 
 */
@Getter
@Setter
public class JobDefinition implements Serializable
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 5423549735424960218L;

	private String		id				;

	private String		name				;

	private String		msrId		;
	
	private boolean		isSingleThread		= true;

	private int		    priority			= 0;

	private String		executorClassName	;

	private Class<?>	executorClass		;
	
	private String		schedulerID		;
	
	private int		    timeOut			= 0;

	private String      cron            ;

	/**
	 * @return the executorClass
	 * @throws ClassNotFoundException 
	 */
	public Class<?> getExecutorClass() throws ClassNotFoundException
	{
		if (this.executorClass == null)
		{
			this.executorClass = Class.forName(executorClassName);
		}
		return executorClass;
	}

}
