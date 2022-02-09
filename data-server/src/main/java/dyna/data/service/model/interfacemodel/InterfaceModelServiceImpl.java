package dyna.data.service.model.interfacemodel;

import dyna.common.bean.model.itf.InterfaceObject;
import dyna.common.dto.model.cls.ClassField;
import dyna.common.exception.ServiceRequestException;
import dyna.common.sync.TableIndexModel;
import dyna.common.systemenum.ModelInterfaceEnum;
import dyna.data.service.DataRuleService;
import dyna.net.service.data.model.InterfaceModelService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @author: duanll
 * @date: 2020年3月26日
 */
@DubboService
public class InterfaceModelServiceImpl extends DataRuleService implements InterfaceModelService
{
	@Autowired
	private InterfaceModelServiceStub modelStub;

	@Override
	public void init()
	{
		this.getModelStub().loadModel();
	}

	protected InterfaceModelServiceStub getModelStub()
	{
		return this.modelStub;
	}

	@Override
	public void reloadModel() throws ServiceRequestException
	{
		this.getModelStub().loadModel();
	}

	@Override
	public Map<String, InterfaceObject> getInterfaceMap()
	{
		return this.getModelStub().getInterfaceObjectMap();
	}

	@Override
	public InterfaceObject getInterface(ModelInterfaceEnum interfaceEnum)
	{
		return this.getModelStub().getInterface(interfaceEnum);
	}

	@Override
	public List<InterfaceObject> listSubInterface(ModelInterfaceEnum interfaceEnum)
	{
		return this.getModelStub().listSubInterface(interfaceEnum);
	}

	@Override
	public List<ClassField> listClassFieldOfInterface(ModelInterfaceEnum interfaceEnum)
	{
		return this.getModelStub().listClassFieldOfInterface(interfaceEnum.name());
	}

	@Override
	public Map<String, List<ClassField>> getInterfaceFieldMap()
	{
		return this.getModelStub().getInterfaceFieldMap();
	}

	@Override
	public Map<String, List<TableIndexModel>> getInterfaceIndexMap()
	{
		return this.getModelStub().getInterfaceIndexMap();
	}
}
