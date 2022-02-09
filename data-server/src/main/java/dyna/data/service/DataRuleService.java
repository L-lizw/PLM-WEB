package dyna.data.service;

import dyna.common.conf.ServiceDefinition;
import dyna.net.service.Service;

public abstract class DataRuleService implements Service
{
	protected ServiceDefinition serviceDef = null;

	@Override public void init()
	{

	}

	@Override
	public void setServiceDefinition(ServiceDefinition serviceDef)
	{
		this.serviceDef = serviceDef;
	}

}
