package dyna.data.service.sync;

import dyna.common.bean.configure.ProjectModel;
import dyna.common.exception.DynaDataException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.systemenum.FieldTypeEnum;
import dyna.data.service.DataRuleService;
import dyna.dbcommon.function.DatabaseFunctionFactory;
import dyna.net.service.data.DSCommonService;
import dyna.net.service.data.SyncModelService;
import dyna.net.service.data.SystemDataService;
import dyna.net.service.data.model.ClassModelService;
import dyna.net.service.data.model.CodeModelService;
import dyna.net.service.data.model.InterfaceModelService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DubboService public class SyncModelServiceImpl extends DataRuleService implements SyncModelService
{
	private static boolean initialized = false;

	@Autowired ClassModelService     classModelService;
	@Autowired CodeModelService      codeModelService;
	@Autowired DSCommonService       dsCommonService;
	@Autowired InterfaceModelService interfaceModelService;
	@Autowired SystemDataService     systemDataService;

	@Autowired private SDSModelStub     modelStub;
	@Autowired private CodeXMLLoadStub  codeXMLLoadStub;
	@Autowired private ClassXMLLoadStub classXMLLoadStub;

	@Override public void init()
	{
		if (initialized)
		{
			return;
		}

		// this.getModelStub().deleteModelerSession();

		initialized = true;
	}

	protected ClassModelService getClassModelService(){return this.classModelService; }

	protected CodeModelService getCodeModelService(){return this.codeModelService; }

	protected DSCommonService getDsCommonService(){return this.dsCommonService; }

	protected InterfaceModelService getInterfaceModelService(){return this.interfaceModelService; }

	protected SystemDataService getSystemDataService(){return this.systemDataService; }

	public SDSModelStub getModelStub()
	{
		return modelStub;
	}

	public CodeXMLLoadStub getClassificationModelLoadStub()
	{
		return codeXMLLoadStub;
	}

	public ClassXMLLoadStub getClassModelLoadStub()
	{
		return classXMLLoadStub;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.data.orm.SystemDataService#isModelSync(dyna.common.bean.configure.ProjectModel)
	 */
	@Override public boolean isModelSync(ProjectModel projectModel) throws DynaDataException
	{
		return this.getModelStub().isModelSync(projectModel);
	}

	@Override public String getCurrentLoginUser()
	{
		return this.getModelStub().getCurrentLoginUser();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.data.orm.SystemDataService#getCurrentSyncModel()
	 */
	@Override public ProjectModel getCurrentSyncModel() throws DynaDataException
	{
		return this.getModelStub().getSyncInfo();
	}

	/**
	 * 从数据库中生成模型文件
	 */
	@Override public ProjectModel makeModelFile(boolean hasClassificationLicense)
	{
		this.getModelStub().makeModelFile(hasClassificationLicense);
		return this.getModelStub().getSyncInfo();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.data.orm.SystemDataService#isDeployLock()
	 */
	@Override public boolean isDeployLock(String sessionId) throws DynaDataException
	{
		return this.getModelStub().isDeployLock(sessionId);
	}

	@Override public ProjectModel deploy(String sessionId, ProjectModel projectModel, boolean hasClassificationLicense) throws Exception
	{
		return this.getModelStub().deploy(sessionId, projectModel, hasClassificationLicense);
	}

	@Override public void deployClassificationField(List<Map<String, String>> dataSource, String sessionId) throws ServiceRequestException
	{
		this.getModelStub().deployClassificationField(dataSource, sessionId);

	}

	@Override public String getColumnDBType(FieldTypeEnum fieldTypeEnum, String fieldSize)
	{
		return DatabaseFunctionFactory.getColumnTypeFunction().getColumnType(fieldTypeEnum, fieldSize);
	}
}
