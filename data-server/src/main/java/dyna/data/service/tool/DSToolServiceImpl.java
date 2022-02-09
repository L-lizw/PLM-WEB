package dyna.data.service.tool;

import dyna.common.bean.data.FoundationObject;
import dyna.common.exception.ServiceRequestException;
import dyna.data.service.DataRuleService;
import dyna.net.service.data.DSCommonService;
import dyna.net.service.data.DSToolService;
import dyna.net.service.data.InstanceService;
import dyna.net.service.data.SystemDataService;
import dyna.net.service.data.model.ClassModelService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@DubboService
public class DSToolServiceImpl extends DataRuleService implements DSToolService
{
	@Autowired ClassModelService classModelService;
	@Autowired DSCommonService   dsCommonService;
	@Autowired InstanceService   instanceService;
	@Autowired SystemDataService systemDataService;
	@Autowired
	private    DSToolStub        toolStub;

	protected ClassModelService classModelService(){return this.classModelService; }

	protected DSCommonService getDsCommonService(){return this.dsCommonService; }

	protected InstanceService getInstanceService(){return this.instanceService; }

	protected SystemDataService getSystemDataService(){return this.systemDataService; }

	protected DSToolStub getDSToolStub()
	{
		return this.toolStub;
	}

	@Override
	public void clearErrDataInMast(String className) throws ServiceRequestException
	{
		this.getDSToolStub().clearErrDataInMast(className);
	}

	@Override
	public void changeFoundationStatus(Map<String, Object> parameterMap) throws ServiceRequestException
	{
		this.getDSToolStub().changeFoundationStatus(parameterMap);
	}

	@Override
	public void refreshMergeFieldValue(FoundationObject foundationObject, String fieldName) throws ServiceRequestException
	{
		this.getDSToolStub().refreshMergeFieldValue(foundationObject, fieldName);
	}
}
