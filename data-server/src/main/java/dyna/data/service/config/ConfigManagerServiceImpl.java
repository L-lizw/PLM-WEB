package dyna.data.service.config;

import dyna.common.bean.data.ObjectGuid;
import dyna.common.exception.ServiceRequestException;
import dyna.common.systemenum.ModelInterfaceEnum;
import dyna.data.service.DataRuleService;
import dyna.net.service.data.ConfigManagerService;
import dyna.net.service.data.DSCommonService;
import dyna.net.service.data.RelationService;
import dyna.net.service.data.model.BusinessModelService;
import dyna.net.service.data.model.ClassModelService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class ConfigManagerServiceImpl extends DataRuleService implements ConfigManagerService
{
	@Autowired
	private RelationService relationService;
	@Autowired
	private DSCommonService dsCommonService;
	@Autowired
	private BusinessModelService businessModelService;
	@Autowired
	private ClassModelService    classModelService;
	@Autowired
	private ConfigManagerStub    configManagerStub;

	protected RelationService getRelationService(){return this.relationService; }

	protected DSCommonService getDsCommonService(){return this.dsCommonService; }

	protected BusinessModelService getBusinessModelService(){return this.businessModelService; }

	protected ClassModelService getClassModelService(){return this.classModelService; }

	protected ConfigManagerStub getConfigManagerStub()
	{
		return this.configManagerStub;
	}

	/**
	 * 当end1发布时，同时发布其配置数据
	 * 所有已发布但是下一个版本数据为Y，且有效的数据失效，所有未发布的数据发布。
	 *
	 * @param masterGuid
	 * @param foundationId
	 * @param interfaceEnum
	 * @param sessionId
	 * @throws ServiceRequestException
	 */
	@Override
	public void releaseConfigTable(String masterGuid, String foundationId, ModelInterfaceEnum interfaceEnum, String sessionId) throws ServiceRequestException
	{
		this.getConfigManagerStub().releaseConfigTable(masterGuid, foundationId, interfaceEnum, sessionId);
	}

	/**
	 * 当end1删除时，同时删除其配置数据
	 * 所有已发布但是下一个版本数据为Y，且有效的数据失效，所有未发布的数据发布。
	 *
	 * @param masterGuid
	 * @param isMaster
	 * @param interfaceEnum
	 * @param sessionId
	 * @throws ServiceRequestException
	 */
	@Override
	public void deleteConfigTable(String masterGuid, boolean isMaster, ModelInterfaceEnum interfaceEnum, String sessionId) throws ServiceRequestException
	{
		this.getConfigManagerStub().deleteConfigTableData(masterGuid, isMaster, interfaceEnum, sessionId);
	}

	@Override
	public void changeOwnerContractOfContent(ObjectGuid contractObjectGuid, String templateGuid, String sessionId) throws ServiceRequestException
	{
		this.getConfigManagerStub().changeOwnerContractOfContent(contractObjectGuid, templateGuid, sessionId);
	}

	@Override
	public void clearItemOfContent(ObjectGuid contractObjectGuid, ObjectGuid itemObjectGuid, String templateGuid, String sessionId) throws ServiceRequestException
	{
		this.getConfigManagerStub().clearItemOfContent(contractObjectGuid, itemObjectGuid, templateGuid, sessionId);
	}
}
