/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: ConfigurableMSRImpl
 * Qiuxq 2012-4-24
 */
package dyna.app.conf.yml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dyna.common.conf.JobDefinition;
import dyna.common.conf.SchedulerDefinition;
import dyna.common.util.SetUtils;
import dyna.common.util.StringUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 *
 */
@Data
@Component
@ConfigurationProperties(prefix = "jobqueueconfig")
public class ConfigurableJSSImpl
{
	private Map<String, JobDefinition> jobtype;

	private Map<String, SchedulerDefinition> schedulerMap;

	private Map<String, JobDefinition>	classmap	= new HashMap<String, JobDefinition>();

	/**
	 * @param sd
	 */
	public void addJobDefinition(JobDefinition sd)
	{
		if (!(StringUtils.isNullString(sd.getId())||StringUtils.isNullString(sd.getExecutorClassName())))
		{
			this.classmap.put(sd.getExecutorClassName(), sd);
		}
	}
	
	public List<JobDefinition> getJobDefinitionList()
	{
		return new ArrayList<JobDefinition>(jobtype.values());
	}
	
	public JobDefinition getJobDelfinition(String id)
	{
		return this.jobtype.get(id);
	}
	
	public JobDefinition getJobDelfinitionWithClassName(String className)
	{
		return this.classmap.get(className);
	}
	
	public SchedulerDefinition getSchedulerDefinition(String id)
	{
		return this.schedulerMap.get(id);
	}


}
