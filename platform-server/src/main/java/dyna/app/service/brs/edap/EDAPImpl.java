/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: EDAPImpl 企业目录访问协议
 * Wanglei 2010-8-13
 */
package dyna.app.service.brs.edap;

import dyna.app.conf.AsyncConfig;
import dyna.app.service.BusinessRuleService;
import dyna.common.dto.Folder;
import dyna.common.dto.acl.SaAclFolderLibConf;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import dyna.net.security.signature.UserSignature;
import dyna.net.service.brs.*;
import dyna.net.service.das.MSRM;
import dyna.net.service.data.AclService;
import dyna.net.service.data.FolderService;
import dyna.net.service.data.InstanceService;
import dyna.net.service.data.SystemDataService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 企业目录访问协议
 *
 * @author Wanglei
 */
@Service public class EDAPImpl extends BusinessRuleService implements EDAP
{
	@DubboReference private AclService        aclService;
	@DubboReference private FolderService     folderService;
	@DubboReference private InstanceService   instanceService;
	@DubboReference private SystemDataService systemDataService;

	@Autowired private Async async;

	@Autowired private EDAPAsyncStub edapStub;
	@Autowired private FolderStub    folderStub;
	@Autowired private LibraryStub   libraryStub;

	protected AclService getAclService()
	{
		return this.aclService;
	}

	protected FolderService getFolderService()
	{
		return this.folderService;
	}

	protected InstanceService getInstanceService()
	{
		return this.instanceService;
	}

	protected SystemDataService getSystemDataService()
	{
		return this.systemDataService;
	}

	protected Async getAsync()
	{
		return this.async;
	}

	protected EDAPAsyncStub getEdapStub()
	{
		return this.edapStub;
	}

	public FolderStub getFolderStub()
	{
		return this.folderStub;
	}

	public LibraryStub getLibraryStub()
	{
		return this.libraryStub;
	}

	protected synchronized AAS getAAS() throws ServiceRequestException
	{
		try
		{
			return this.getRefService(AAS.class);
		}
		catch (Exception e)
		{
			throw new ServiceRequestException(null, e.getMessage(), e.fillInStackTrace());
		}

	}

	protected synchronized BOMS getBOMS() throws ServiceRequestException
	{
		try
		{
			return this.getRefService(BOMS.class);
		}
		catch (Exception e)
		{
			throw new ServiceRequestException(null, e.getMessage(), e.fillInStackTrace());
		}

	}

	protected synchronized SMS getSMS() throws ServiceRequestException
	{
		try
		{
			return this.getRefService(SMS.class);
		}
		catch (Exception e)
		{
			throw new ServiceRequestException(null, e.getMessage(), e.fillInStackTrace());
		}

	}

	protected synchronized MSRM getMSRM() throws ServiceRequestException
	{
		try
		{
			return this.getRefService(MSRM.class);
		}
		catch (Exception e)
		{
			throw new ServiceRequestException(null, e.getMessage(), e.fillInStackTrace());
		}

	}

	protected synchronized ACL getACL() throws ServiceRequestException
	{
		try
		{
			return this.getRefService(ACL.class);
		}
		catch (Exception e)
		{
			throw new ServiceRequestException(null, e.getMessage(), e.fillInStackTrace());
		}

	}

	protected synchronized EMM getEMM() throws ServiceRequestException
	{
		try
		{
			return this.getRefService(EMM.class);
		}
		catch (Exception e)
		{
			throw new ServiceRequestException(null, e.getMessage(), e.fillInStackTrace());
		}

	}

	protected synchronized SLC getSLC() throws ServiceRequestException
	{
		try
		{
			return this.getRefService(SLC.class);
		}
		catch (Exception e)
		{
			throw new ServiceRequestException(null, e.getMessage(), e.fillInStackTrace());
		}

	}

	protected synchronized BOAS getBOAS() throws ServiceRequestException
	{
		try
		{
			return this.getRefService(BOAS.class);
		}
		catch (Exception e)
		{
			throw new ServiceRequestException(null, e.getMessage(), e.fillInStackTrace());
		}

	}

	protected synchronized EOSS getEOSS() throws ServiceRequestException
	{
		try
		{
			return this.getRefService(EOSS.class);
		}
		catch (Exception e)
		{
			throw new ServiceRequestException(null, e.getMessage(), e.fillInStackTrace());
		}

	}

	@Override public void deleteFolder(String folderGuid) throws ServiceRequestException
	{
		this.getFolderStub().deleteFolder(folderGuid);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.EDAS#getFolder(java.lang.String)
	 */
	@Override public Folder getFolder(String folderGuid) throws ServiceRequestException
	{
		return this.getFolderStub().getFolder(folderGuid, true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.EDAP#getRootMyFolder()
	 */
	@Override public Folder getRootMyFolder() throws ServiceRequestException
	{
		return this.getFolderStub().getRootMyFolder();
	}

	@Override public Folder getRootPrivateFolderByUser(String userGuid, String userId) throws ServiceRequestException
	{
		return this.getFolderStub().getRootPrivateFolderByUser(userGuid, userId);
	}

	@Override public SaAclFolderLibConf getSaAclFolderLibConf(String folderGuid) throws ServiceRequestException
	{
		return this.getFolderStub().getSaAclFolderLibConf(folderGuid);
	}

	@Override public List<Folder> listLibrary() throws ServiceRequestException
	{
		return this.getLibraryStub().listLibrary();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.EDAS#listSubFolder(java.lang.String)
	 */
	@Override public List<Folder> listSubFolder(String folderGuid) throws ServiceRequestException
	{
		return this.getFolderStub().listSubFolder(folderGuid, true);
	}

	@Override public Folder saveFolder(Folder folder) throws ServiceRequestException
	{
		return this.getFolderStub().saveFolder(folder);
	}

	@Override public SaAclFolderLibConf saveSaAclFolderLibConf(SaAclFolderLibConf saAclFolderLibConf) throws ServiceRequestException
	{
		return this.getFolderStub().saveSaAclFolderLibConf(saAclFolderLibConf);
	}

	@Override public List<Folder> listLibraryByUserGroup() throws ServiceRequestException
	{
		return this.getLibraryStub().listLibraryByUserGroup();
	}

	@Override public Folder setLibraryValid(String libraryGuid, boolean isValid) throws ServiceRequestException
	{
		return this.getLibraryStub().setValidFlag(libraryGuid, isValid);
	}

	@Override public Long getMainDataNumberByLib(String libraryGuid) throws ServiceRequestException
	{
		return this.getLibraryStub().getMainDataNumberByLib(libraryGuid);
	}

	@Override public Long getVersionDataNumberByLib(String libraryGuid) throws ServiceRequestException
	{
		return this.getLibraryStub().getVersionDataNumberByLib(libraryGuid);
	}

	@Override public Folder getDefaultLibraryByUserGroup() throws ServiceRequestException
	{
		return this.getLibraryStub().getDefaultLibraryByUserGroup();
	}

	@Override public Folder getDefaultRootFolderByUserGroup(String groupGuid) throws ServiceRequestException
	{
		return this.getFolderStub().getDefaultRootFolderByUserGroup(groupGuid);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.EDAP#listAllRootFolderHasAcl()
	 */
	@Override public List<Folder> listAllRootFolderHasAcl() throws ServiceRequestException
	{
		return this.getFolderStub().listAllRootFolderHasAcl();
	}

	@Override public List<Folder> listSubFolderNoCheckACL(String folderGuid) throws ServiceRequestException
	{
		return this.getFolderStub().listSubFolder(folderGuid, false);
	}

	@org.springframework.scheduling.annotation.Async(AsyncConfig.MULTI_THREAD_QUEUED_TASK) @Override public void saveFolderTree(Folder folder, boolean isDelete,
			UserSignature userSignature)
	{
		DynaLogger.debug("QueuedTaskScheduler Scheduled [Class]SaveFolderTreeScheduledTask , Scheduled Task Start...");
		this.edapStub.saveFolderTree(folder, isDelete, userSignature);
		DynaLogger.debug("QueuedTaskScheduler Scheduled [Class]SaveFolderTreeScheduledTask , Scheduled Task End...");
	}
}
