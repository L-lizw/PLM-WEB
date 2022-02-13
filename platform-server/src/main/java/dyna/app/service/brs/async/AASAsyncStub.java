package dyna.app.service.brs.async;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.aas.AASImpl;
import dyna.app.service.brs.async.AsyncImpl;
import dyna.common.dto.TreeDataRelation;
import dyna.common.dto.aas.Group;
import dyna.common.log.DynaLogger;
import dyna.common.util.SetUtils;
import dyna.net.security.signature.UserSignature;
import dyna.net.service.brs.AAS;
import org.acegisecurity.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Lizw
 * @date 2022/1/28
 **/
@Component
public class AASAsyncStub extends AbstractServiceStub<AsyncImpl>
{

	protected void saveGroupTree(Group group, UserSignature userSignature)
	{
		try
		{

			TreeDataRelation relation = new TreeDataRelation();
			relation.setDataGuid(group.getGuid());
			relation.setSubDataGuid(group.getGuid());
			relation.setDataType(TreeDataRelation.DATATYPE_GROUP);
			this.stubService.getSystemDataService().delete(TreeDataRelation.class,relation,"deleteBy");

			relation.setCreateUserGuid(userSignature.getUserGuid());
			this.stubService.getSystemDataService().save(relation);

			List<Group> allSubGroupList = this.stubService.getAas().listAllSubGroup(group.getGuid(), null, true);
			if (!SetUtils.isNullList(allSubGroupList))
			{
				for (Group subGroup : allSubGroupList)
				{
					relation = new TreeDataRelation();
					relation.setDataGuid(group.getGuid());
					relation.setSubDataGuid(subGroup.getGuid());
					relation.setDataType(TreeDataRelation.DATATYPE_GROUP);
					relation.setCreateUserGuid(userSignature.getUserGuid());
					this.stubService.getSystemDataService().save(relation);
				}
			}

			List<Group> allParentGroupList = this.stubService.getAas().listSuperGroup(group.getGuid(), null);
			if (!SetUtils.isNullList(allParentGroupList))
			{
				for (Group superGroup : allParentGroupList)
				{
					relation = new TreeDataRelation();
					relation.setDataGuid(superGroup.getGuid());
					relation.setSubDataGuid(group.getGuid());
					relation.setDataType(TreeDataRelation.DATATYPE_GROUP);
					relation.setCreateUserGuid(userSignature.getUserGuid());
					this.stubService.getSystemDataService().save(relation);
				}
			}
		}
		catch (Throwable e)
		{
			DynaLogger.error("run send mail:", e);
		}
	}
}
