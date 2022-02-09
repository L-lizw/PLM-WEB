/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: SLC System Logical Configuration Implement(系统配置服务)
 * caogc 2010-11-08
 */
package dyna.app.service.brs.slc;

import dyna.app.service.BusinessRuleService;
import dyna.common.dto.ConfigRuleBOLM;
import dyna.common.dto.ConfigRuleRevise;
import dyna.common.exception.ServiceRequestException;
import dyna.net.service.brs.EDAP;
import dyna.net.service.brs.EMM;
import dyna.net.service.brs.SLC;
import dyna.net.service.data.SystemDataService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * SLC System Logical Configuration Implement(系统配置服务)
 *
 * @author caogc
 */
@Service public class SLCImpl extends BusinessRuleService implements SLC
{

	@DubboReference private SystemDataService  systemDataService;
	@Autowired private      ConfigRuleBOLMStub configRuleBOLMStub ;
	@Autowired private      RuleReviseStub     ruleReviseStub     ;

	protected SystemDataService getSystemDataService()
	{
		return this.systemDataService;
	}

	/**
	 * @return the configRuleBOLMStub
	 */
	public ConfigRuleBOLMStub getConfigRuleBOLMStub()
	{
		return this.configRuleBOLMStub;
	}

	/**
	 * @return the ruleReviseStub
	 */
	protected RuleReviseStub getRuleReviseStub()
	{
		return this.ruleReviseStub;
	}

	protected synchronized EMM getEMM() throws ServiceRequestException
	{
		try
		{
			return this.getRefService(EMM.class);
		}
		catch (Exception e)
		{
			throw new ServiceRequestException(null, e.getMessage(), e.fillInStackTrace());
		}

	}

	protected synchronized EDAP getEDAP() throws ServiceRequestException
	{
		try
		{
			return this.getRefService(EDAP.class);
		}
		catch (Exception e)
		{
			throw new ServiceRequestException(null, e.getMessage(), e.fillInStackTrace());
		}

	}

	@Override public void deleteConfigRuleRevise(String guid) throws ServiceRequestException
	{
		this.getRuleReviseStub().deleteConfigRuleRevise(guid);
	}

	@Override public ConfigRuleRevise getConfigRuleRevise() throws ServiceRequestException
	{
		return this.getRuleReviseStub().getConfigRuleRevise();
	}

	@Override public ConfigRuleRevise saveConfigRuleRevise(ConfigRuleRevise configRuleRevise) throws ServiceRequestException
	{
		return this.getRuleReviseStub().saveConfigRuleRevise(configRuleRevise);
	}

	@Override public void deleteConfigRuleBOLM(String configRuleBOLMGuid) throws ServiceRequestException
	{
		this.getConfigRuleBOLMStub().deleteConfigRuleBOLM(configRuleBOLMGuid);
	}

	@Override public ConfigRuleBOLM getConfigRuleBOLMOnlyCurrent(String bmGuid, String boGuid) throws ServiceRequestException
	{
		return this.getConfigRuleBOLMStub().getConfigRuleBOLMContainParent(bmGuid, boGuid, true);
	}

	@Override public ConfigRuleBOLM saveConfigRuleBOLM(ConfigRuleBOLM configRuleBOLM, String bmGuid) throws ServiceRequestException
	{
		return this.getConfigRuleBOLMStub().saveConfigRuleBOLM(configRuleBOLM, bmGuid);
	}

	@Override public ConfigRuleBOLM getConfigRuleBOLMContainParent(String boGuid) throws ServiceRequestException
	{
		String currentBizModelGuid = this.getUserSignature().getLoginGroupBMGuid();
		return this.getConfigRuleBOLMStub().getConfigRuleBOLMContainParent(currentBizModelGuid, boGuid, true);
	}
}
