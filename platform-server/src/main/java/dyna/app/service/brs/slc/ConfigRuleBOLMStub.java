/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: ConfigRuleBOLMStub
 * Wanglei 2011-4-1
 */
package dyna.app.service.brs.slc;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.helper.ServiceRequestExceptionWrap;
import dyna.common.bean.data.SystemObject;
import dyna.common.dto.ConfigRuleBOLM;
import dyna.common.dto.ConfigRuleBOPhaseSet;
import dyna.common.dto.model.bmbo.BMInfo;
import dyna.common.dto.model.bmbo.BOInfo;
import dyna.common.dto.model.cls.ClassInfo;
import dyna.common.dto.model.lf.LifecycleInfo;
import dyna.common.dto.template.bom.BOMTemplateInfo;
import dyna.common.exception.DynaDataException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.util.SetUtils;
import dyna.common.util.StringUtils;
import dyna.net.service.data.SystemDataService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 规则配置的实现类（包含：生效规则、失效规则、入库规则）
 * 
 * @author caogc
 * 
 */
@Component
public class ConfigRuleBOLMStub extends AbstractServiceStub<SLCImpl>
{

	protected void init()
	{
	}

	protected void deleteConfigRuleBOLM(String configRuleBOLMGuid) throws ServiceRequestException
	{
		this.stubService.getSystemDataService().delete(ConfigRuleBOLM.class, configRuleBOLMGuid);
	}

	protected List<ConfigRuleBOLM> listConfigRuleBOLM() throws ServiceRequestException
	{
		Map<String, Object> filter = new HashMap<>();
		SystemDataService sds = this.stubService.getSystemDataService();
		List<ConfigRuleBOLM> returnList=new LinkedList<>();
		List<ConfigRuleBOLM> configRuleBOLMList = sds.query(ConfigRuleBOLM.class, filter);
		if (!SetUtils.isNullList(configRuleBOLMList))
		{
			for (ConfigRuleBOLM bolm : configRuleBOLMList)
			{
				if (StringUtils.isGuid(bolm.getGuid()))
				{
					if (isShare(bolm))
					{
						filter.clear();
						filter.put(ConfigRuleBOPhaseSet.BOLM_GUID, bolm.getGuid());
						List<ConfigRuleBOPhaseSet> boPhaseSetList = sds.query(ConfigRuleBOPhaseSet.class, filter);
						bolm.setBoPhaseSetList(boPhaseSetList);
					}
				}
			}
		}
		return configRuleBOLMList;
	}

	private boolean isShare(ConfigRuleBOLM bolm) throws ServiceRequestException
	{
		BMInfo bminfo = this.stubService.getEmm().getSharedBizModel();
		BOInfo bo = this.stubService.getEmm().getBizObject(bminfo.getGuid(), bolm.getBOGuid());
		return bo!=null;
	}

	protected ConfigRuleBOLM getConfigRuleBOLMByBOGuid(String boguid) throws ServiceRequestException
	{
		Map<String, Object> filter = new HashMap<>();
		boguid=getShareBO(boguid);
		
		
		filter.put(ConfigRuleBOLM.BOGUID, boguid);
		SystemDataService sds = this.stubService.getSystemDataService();

		List<ConfigRuleBOLM> configRuleBOLMList = sds.query(ConfigRuleBOLM.class, filter);
		if (!SetUtils.isNullList(configRuleBOLMList))
		{
			ConfigRuleBOLM bolm = configRuleBOLMList.get(0);
			if (StringUtils.isGuid(bolm.getGuid()))
			{
				filter.clear();
				filter.put(ConfigRuleBOPhaseSet.BOLM_GUID, bolm.getGuid());
				List<ConfigRuleBOPhaseSet> boPhaseSetList = sds.query(ConfigRuleBOPhaseSet.class, filter);
				bolm.setBoPhaseSetList(boPhaseSetList);
			}
			return bolm;
		}
		return null;
	}

	private String getShareBO(String boguid) throws ServiceRequestException 
	{
		BOInfo bo = this.stubService.getEmm().getCurrentBizObjectByGuid(boguid);
		if (bo==null)
		{
			return boguid;
		}
		BMInfo bminfo = this.stubService.getEmm().getSharedBizModel();
		bo = this.stubService.getEmm().getBoInfoByNameAndBM(bminfo.getGuid(), bo.getBOName());
		if (bo==null)
		{
			return boguid;
		}
		return bo.getGuid();
	}

	public ConfigRuleBOLM getConfigRuleBOLMContainParent(String bmGuid, String boGuid, boolean isEditColne) throws ServiceRequestException
	{
		ConfigRuleBOLM configRuleBOLM = null;
		BOInfo boInfo = null;
		if ("ALL".equalsIgnoreCase(bmGuid) || this.stubService.getEmm().getSharedBizModel().getGuid().equalsIgnoreCase(bmGuid))
		{
			bmGuid = this.stubService.getEmm().getSharedBizModel().getGuid();
			boInfo = this.stubService.getEmm().getBizObject(bmGuid, boGuid);
			if (boInfo == null)
			{
				throw new ServiceRequestException("ID_APP_BUSINESS_OBJECT_NOT_EXIST", "boGuid not found :" + boGuid, null, boGuid);
			}
		}
		else 
		{
			boInfo = this.stubService.getEmm().getBizObject(bmGuid, boGuid);
			if (boInfo == null)
			{
				throw new ServiceRequestException("ID_APP_BUSINESS_OBJECT_NOT_EXIST", "boGuid not found :" + boGuid, null, boGuid);
			}
			String shareBmGuid = this.stubService.getEmm().getSharedBizModel().getGuid();
			BOInfo sboInfo = this.stubService.getEmm().getBoInfoByNameAndBM(shareBmGuid,boInfo.getBOName());
			if (boInfo!=null)
			{
				bmGuid=shareBmGuid;
				boInfo=sboInfo;
			}
			
		}

		BOInfo parentBO = boInfo;
		Map<String, Object> searchConditionMap = new HashMap<>();
		while (configRuleBOLM == null && parentBO != null)
		{
			configRuleBOLM = this.getConfigRuleBOLMByBOGuid(parentBO.getGuid());
			if (configRuleBOLM == null)
			{
				if (StringUtils.isNullString(parentBO.getParentBOGuid()))
				{
					parentBO = null;
				}
				else
				{
					parentBO = this.stubService.getEmm().getBizObject(bmGuid, parentBO.getParentBOGuid());
				}
			}
			searchConditionMap.clear();
		}

		if (configRuleBOLM == null)
		{
			configRuleBOLM = new ConfigRuleBOLM();
			configRuleBOLM.setBoGuid(boGuid);
		}

		// 继承
		if (!boGuid.equalsIgnoreCase(configRuleBOLM.getBOGuid()))
		{
			ClassInfo classInfo = this.stubService.getEmm().getClassByGuid(boInfo.getClassGuid());
			ClassInfo parentClassInfo = this.stubService.getEmm().getClassByGuid(boInfo.getClassGuid());
			LifecycleInfo lifecycleInfo = this.stubService.getEmm().getLifecycleInfoByGuid(classInfo.getLifecycle());
			LifecycleInfo lifecycleInfoParent = this.stubService.getEmm().getLifecycleInfoByGuid(parentClassInfo.getLifecycle());
			boolean isLifecycleColne = true;
			if (lifecycleInfo != null && lifecycleInfoParent != null && lifecycleInfo.getGuid().equals(lifecycleInfoParent.getGuid()))
			{
				isLifecycleColne = false;
			}

			if (isLifecycleColne || isEditColne)
			{
				ConfigRuleBOLM clone = (ConfigRuleBOLM) configRuleBOLM.clone();
				if (lifecycleInfo != null && lifecycleInfoParent != null && lifecycleInfo.getGuid().equals(lifecycleInfoParent.getGuid()))
				{
					clone.setBoPhaseSetList(configRuleBOLM.getBoPhaseSetList());
				}

				clone.setGuid(null);
				clone.setBoGuid(boGuid);
				clone.setIsFromParent(true);
				configRuleBOLM = clone;
			}
		}

		return configRuleBOLM;
	}

	protected ConfigRuleBOLM saveConfigRuleBOLM(ConfigRuleBOLM configRuleBOLM, String bmGuid) throws ServiceRequestException
	{
		ConfigRuleBOLM retConfigRuleBOLM = new ConfigRuleBOLM();

		if (configRuleBOLM.getBOGuid() == null || "".equals(configRuleBOLM.getBOGuid()))
		{
			return null;
		}

		try
		{

//			this.stubService.getTransactionManager().startTransaction(this.stubService.getFixedTransactionId());

			if (BOMTemplateInfo.ALL.equalsIgnoreCase(bmGuid) || this.stubService.getEmm().getSharedBizModel().getGuid().equalsIgnoreCase(bmGuid))
			{
				bmGuid = this.stubService.getEmm().getSharedBizModel().getGuid();
			}
			else
			{
				BOInfo bo = this.stubService.getEmm().getBizObject(bmGuid,configRuleBOLM.getBOGuid());
				if (bo==null)
				{
					return null;
				}
				String shareBmGuid = this.stubService.getEmm().getSharedBizModel().getGuid();
				bo = this.stubService.getEmm().getBoInfoByNameAndBM(shareBmGuid,bo.getBOName());
				if (bo!=null)
				{
					bmGuid=shareBmGuid;
					configRuleBOLM.setBoGuid(bo.getGuid());
				}
			}
			String configRuleBOLMGuid = configRuleBOLM.getGuid();

			String operatorGuid = this.stubService.getOperatorGuid();

			configRuleBOLM.put(SystemObject.UPDATE_USER_GUID, operatorGuid);

			if (!StringUtils.isGuid(configRuleBOLMGuid))
			{
				configRuleBOLM.put(SystemObject.CREATE_USER_GUID, operatorGuid);
			}

			String result = this.stubService.getSystemDataService().save(configRuleBOLM);
			if (StringUtils.isGuid(result))
			{
				configRuleBOLMGuid = result;
				configRuleBOLM.setGuid(result);
			}
			else
			{
				configRuleBOLMGuid = configRuleBOLM.getGuid();
			}

			// 设置BO phase 对应的版本规则，EC模板
			this.saveConfigRuleBOPhaseSet(configRuleBOLM.getBoPhaseSetList(), configRuleBOLMGuid);

//			this.stubService.getTransactionManager().commitTransaction();

			retConfigRuleBOLM.sync(configRuleBOLM);

			return retConfigRuleBOLM;
		}
		catch (DynaDataException e)
		{
//			this.stubService.getTransactionManager().rollbackTransaction();
			throw ServiceRequestExceptionWrap.createByDynaDataException(this.stubService, e);
		}
		catch (Exception e)
		{
//			this.stubService.getTransactionManager().rollbackTransaction();
			if (e instanceof ServiceRequestException)
			{
				throw (ServiceRequestException) e;
			}
			else
			{
				throw ServiceRequestException.createByException("ID_APP_SERVER_EXCEPTION", e);
			}
		}
	}

	private void saveConfigRuleBOPhaseSet(List<ConfigRuleBOPhaseSet> boPhaseSetList, String configRuleBOLMGuid) throws ServiceRequestException
	{
		if (StringUtils.isNullString(configRuleBOLMGuid))
		{
			return;
		}

		Map<String, Object> filter = new HashMap<>();
		filter.put(ConfigRuleBOPhaseSet.BOLM_GUID, configRuleBOLMGuid);
		this.stubService.getSystemDataService().delete(ConfigRuleBOPhaseSet.class, filter, "deleteWithBolmguid");

		if (!SetUtils.isNullList(boPhaseSetList))
		{

			for (ConfigRuleBOPhaseSet phaseSet : boPhaseSetList)
			{
				if (phaseSet == null)
				{
					continue;
				}
				phaseSet.setBOLMGuid(configRuleBOLMGuid);
				String result = this.stubService.getSystemDataService().save(phaseSet);
				if (StringUtils.isGuid(result))
				{
					phaseSet.setGuid(result);
				}
			}
		}
	}

}
