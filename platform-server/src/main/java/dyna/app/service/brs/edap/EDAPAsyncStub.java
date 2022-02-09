package dyna.app.service.brs.edap;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.async.AsyncImpl;
import dyna.app.service.brs.edap.EDAPImpl;
import dyna.common.dto.Folder;
import dyna.common.dto.TreeDataRelation;
import dyna.common.log.DynaLogger;
import dyna.common.util.StringUtils;
import dyna.net.security.signature.UserSignature;
import dyna.net.service.brs.EDAP;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 * @date 2022/1/28
 **/
@Component
public class EDAPAsyncStub extends AbstractServiceStub<EDAPImpl>
{

	protected void saveFolderTree(Folder folder, boolean isDelete, UserSignature userSignature)
	{
		try
		{
			if (isDelete)
			{
				TreeDataRelation relation = new TreeDataRelation();
				relation.setDataGuid(folder.getGuid());
				relation.setSubDataGuid(folder.getGuid());
				relation.setDataType(TreeDataRelation.DATATYPE_FOLDER);
				this.stubService.getSystemDataService().delete(TreeDataRelation.class, relation, "deleteBy");
			}
			else
			{
				TreeDataRelation relation = new TreeDataRelation();
				relation.setDataGuid(folder.getGuid());
				relation.setSubDataGuid(folder.getGuid());
				relation.setDataType(TreeDataRelation.DATATYPE_FOLDER);
				relation.setCreateUserGuid(userSignature.getUserGuid());
				this.stubService.getSystemDataService().save(relation);

				String parentFolderGuid = folder.getParentGuid();
				while (StringUtils.isGuid(parentFolderGuid))
				{
					Folder parentFolder = this.stubService.getFolderStub().getFolder(parentFolderGuid, userSignature.getUserGuid(),
							userSignature.getLoginGroupGuid(), userSignature.getLoginRoleGuid(), false);
					if (parentFolder != null)
					{
						relation = new TreeDataRelation();
						relation.setDataGuid(parentFolder.getGuid());
						relation.setSubDataGuid(folder.getGuid());
						relation.setDataType(TreeDataRelation.DATATYPE_FOLDER);
						relation.setCreateUserGuid(userSignature.getUserGuid());
						this.stubService.getSystemDataService().save(relation);
						parentFolderGuid = parentFolder.getParentGuid();
					}
					else
					{
						parentFolderGuid = null;
					}
				}
			}
		}
		catch (Throwable e)
		{
			DynaLogger.error("run send mail:", e);
		}
	}
}
