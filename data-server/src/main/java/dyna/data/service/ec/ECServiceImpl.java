package dyna.data.service.ec;

import dyna.common.bean.data.ObjectGuid;
import dyna.common.exception.ServiceRequestException;
import dyna.data.service.DataRuleService;
import dyna.net.service.data.DSCommonService;
import dyna.net.service.data.ECService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DubboService
public class ECServiceImpl extends DataRuleService implements ECService
{
	@Autowired DSCommonService dsCommonService;
	@Autowired
	private    DSECStub        ecStub;

	protected DSCommonService getDsCommonService(){return this.getDsCommonService(); }

	protected DSECStub getECStub()
	{
		return this.ecStub;
	}

	/**
	 * 批量解除ECO锁定
	 *
	 * @param ecoObjectGuid
	 * @param classNameList
	 * @throws ServiceRequestException
	 */
	@Override
	public void unlockByECO(ObjectGuid ecoObjectGuid, List<String> classNameList) throws ServiceRequestException
	{
		this.getECStub().unlockByECO(ecoObjectGuid, classNameList);
	}
}
