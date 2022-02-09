package dyna.app.service.brs.mms;

import dyna.app.service.AbstractServiceStub;
import dyna.common.bean.configure.ProjectModel;
import dyna.common.exception.DynaDataException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.systemenum.ApplicationTypeEnum;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author duanll
 */
@Component
public class ModelDeployModifyStub extends AbstractServiceStub<MMSImpl>
{

	protected ProjectModel deploy(ProjectModel projectModel) throws ServiceRequestException
	{
		ProjectModel retProjectModel;
		try
		{
			boolean hasClassificationLicense = serverContext.getLicenseDaemon().hasLicence(ApplicationTypeEnum.CLS.name());
			retProjectModel = this.stubService.getSyncModelService().deploy(this.stubService.getSignature().getCredential(), projectModel, hasClassificationLicense);
		}
		catch (Exception e)
		{
			if (e instanceof DynaDataException)
			{
				throw ServiceRequestException.createByDynaDataException((DynaDataException) e);
			}
			String message = e.getCause() == null ? e.getMessage() : e.getCause().getMessage();
			throw new ServiceRequestException("ID_APP_DEPLOY_FAIL", message, e);
		}
		return retProjectModel;
	}

	protected void deployClassification(List<Map<String, String>> dataSource, String sessionId) throws ServiceRequestException
	{
		try
		{
			this.stubService.getSyncModelService().deployClassificationField(dataSource, sessionId);
		}
		catch (Exception e)
		{
			if (e instanceof DynaDataException)
			{
				throw ServiceRequestException.createByDynaDataException((DynaDataException) e);
			}
			String message = e.getCause() == null ? e.getMessage() : e.getCause().getMessage();
			throw new ServiceRequestException("ID_APP_DEPLOY_FAIL", message, e);
		}
	}

	protected boolean isModelSync(ProjectModel projectModel) throws ServiceRequestException
	{
		return this.stubService.getSyncModelService().isModelSync(projectModel);
	}

	protected boolean isDeployLock() throws ServiceRequestException
	{
		return this.stubService.getSyncModelService().isDeployLock(this.stubService.getSignature().getCredential());
	}
}
