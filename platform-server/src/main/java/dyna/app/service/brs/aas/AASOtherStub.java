package dyna.app.service.brs.aas;

import dyna.app.service.AbstractServiceStub;
import dyna.common.Version;
import dyna.common.exception.ServiceRequestException;
import dyna.net.security.signature.ModuleSignature;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/2/9
 **/
@Component
public class AASOtherStub extends AbstractServiceStub<AASImpl>
{
	 protected boolean checkClientVersion(String clientSum, String clientType, boolean isDebug) throws ServiceRequestException
	 {
		 if (this.serverContext.isDebugMode() || isDebug)
		 {
			 if (this.stubService.getSignature() instanceof ModuleSignature)
			 {
				 ((ModuleSignature) this.stubService.getSignature()).setVersionValidate(true);
			 }
			 return true;
		 }
		 boolean checkClientVersionSum = Version.getVersionInfo().equalsIgnoreCase(clientSum);
		 if (!checkClientVersionSum)
		 {
			 throw new ServiceRequestException("ID_APP_INVALID_CLIENT_VERSION", "Client Version inconsistencies", null, Version.getVersionInfo(), "");
		 }

		 if (this.stubService.getSignature() instanceof ModuleSignature)
		 {
			 ((ModuleSignature) this.stubService.getSignature()).setVersionValidate(true);
		 }
		 else
		 {
			 throw new ServiceRequestException("ID_APP_INVALID_CLIENT_VERSION", "Client Version inconsistencies", null, Version.getVersionInfo(), "");
		 }

		 return checkClientVersionSum;
	 }

}
