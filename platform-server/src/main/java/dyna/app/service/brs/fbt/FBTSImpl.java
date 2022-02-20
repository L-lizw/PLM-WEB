/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: FBTSImpl
 * wangweixia 2012-9-7
 */
package dyna.app.service.brs.fbt;

import dyna.app.service.BusinessRuleService;
import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.conf.ServiceDefinition;
import dyna.common.dto.*;
import dyna.common.exception.ServiceRequestException;
import dyna.common.util.StringUtils;
import dyna.net.service.brs.*;
import dyna.net.service.data.SystemDataService;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * File Browser Tools Service 文件浏览工具服务
 *
 * @author Lizw
 */
@Getter(AccessLevel.PROTECTED)
@Service public class FBTSImpl extends BusinessRuleService implements FBTS
{
	@DubboReference private SystemDataService systemDataService;

	@Autowired
	private AAS aas;
	@Autowired
	private EMM emm;
	@Autowired
	private BOAS boas;
	@Autowired
	private DSS dss;
	private CAD cad;

	@Autowired private FoConfigStub  foConfigStub      ;
	@Autowired private FoItemStub    foItemStub        ;
	@Autowired private FoSubjectStub foSubjectStub     ;
	@Autowired private FbtStub       fbtStub           ;
	@Autowired private WebFbtStub    webFbtStub        ;
	private static     String        webSerVerInfo     = null;
	private static     String        webServerFilePath = null;


	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.app.service.DataAccessService#init()
	 */
	@Override public void init(ServiceDefinition serviceDefinition)
	{
		super.init(serviceDefinition);
		this.webSerVerInfo = this.getServiceDefinition().getParam().get("WebServer");
		this.webServerFilePath = this.getServiceDefinition().getParam().get("WebServerFilePath");
		if ((StringUtils.isNullString(webServerFilePath) == false) && (webServerFilePath.endsWith(System.getProperty("file.separator")) == false))
		{
			webServerFilePath = webServerFilePath + System.getProperty("file.separator");
		}
	}

	protected SystemDataService getSystemDataService()
	{
		return this.systemDataService;
	}

	/**
	 * @return the foConfigStub
	 */
	protected FoConfigStub getFoConfigStub()
	{
		return this.foConfigStub;

	}

	/**
	 * @return the foItemStub
	 */
	protected FoItemStub getFoItemStub()
	{
		return this.foItemStub;
	}

	/**
	 * @return the foSubjectStub
	 */
	protected FoSubjectStub getFoSubjectStub()
	{
		return this.foSubjectStub;

	}

	/**
	 * @return the webFbtStub
	 */
	protected WebFbtStub getWebFbtStub()
	{
		return this.webFbtStub;

	}

	/**
	 * @return the foConfigStub
	 */
	protected FbtStub getFbtStub()
	{
		return this.fbtStub;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.FBTS#batchSaveFileOpenConfig(java.util.List, java.util.List, java.util.List)
	 */
	@Override public void batchSaveFileOpenConfig(List<FileOpenConfig> addFileOpenConfigList, List<FileOpenConfig> updateFileOpenConfigList,
			List<FileOpenConfig> deleteFileOpenConfigList) throws ServiceRequestException
	{
		this.getFoConfigStub().batchSaveFileOpenConfig(addFileOpenConfigList, updateFileOpenConfigList, deleteFileOpenConfigList);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.FBTS#listFileOpenConfig()
	 */
	@Override public List<FileOpenConfig> listFileOpenConfig() throws ServiceRequestException
	{

		return this.getFoConfigStub().listFileOpenConfig();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.FBTS#getFileOpenConfigByGuid(java.lang.String)
	 */
	@Override public FileOpenConfig getFileOpenConfigByGuid(String guid) throws ServiceRequestException
	{

		return this.getFoConfigStub().getFileOpenConfigByGuid(guid);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.FBTS#saveSubjectAndItem(dyna.common.bean.data.system.FileOpenSubject, java.util.List,
	 * java.util.List)
	 */
	@Override public void saveSubjectAndItem(FileOpenSubject fileOpenSubject, List<FileOpenItem> fileOpenItemList) throws ServiceRequestException
	{
		this.getFbtStub().saveSubjectAndItem(fileOpenSubject, fileOpenItemList);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.FBTS#deleteFileOpenSubject(java.lang.String)
	 */
	@Override public void deleteFileOpenSubject(String subjectGuid) throws ServiceRequestException
	{
		this.getFoSubjectStub().deleteFileOpenSubject(subjectGuid);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.FBTS#listRootFileOpenSubject()
	 */
	@Override public List<FileOpenSubject> listRootFileOpenSubject() throws ServiceRequestException
	{

		return this.getFoSubjectStub().listRootFileOpenSubject();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.FBTS#listSubFileOpenSubject(java.lang.String, boolean)
	 */
	@Override public List<FileOpenSubject> listSubFileOpenSubject(String FileOpenSubjectGuid, boolean isCascade) throws ServiceRequestException
	{

		return this.getFoSubjectStub().listSubFileOpenSubject(FileOpenSubjectGuid);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.FBTS#listFileOpenItemBySubject(java.lang.String)
	 */
	@Override public List<FileOpenItem> listFileOpenItemBySubject(String FileOpenSubjectGuid) throws ServiceRequestException
	{

		return this.getFoItemStub().listFileOpenItemBySubject(FileOpenSubjectGuid);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.FBTS#listFileOpenConfigBySuffix(dyna.common.bean.data.FoundationObject,
	 * java.lang.String)
	 */
	@Override public List<FileOpenConfig> listFileOpenConfigBySuffix(FoundationObject foundationObject, String suffix) throws ServiceRequestException
	{

		return this.getFbtStub().listFileOpenConfigBySuffix(foundationObject, suffix);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.FBTS#listDSSFileTransByConfigGuid(dyna.common.bean.data.FoundationObject,
	 * java.lang.String, dyna.common.bean.data.system.DSSFileInfo)
	 */
	@Override public List<DSSFileTrans> listDSSFileTransByConfigGuid(FoundationObject foundationObject, String guid, DSSFileInfo file) throws ServiceRequestException
	{

		return this.getFbtStub().listDSSFileTransByConfigGuid(foundationObject, guid, file);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.FBTS#isFileCompareEnabled()
	 */
	@Override public boolean isFileCompareEnabled() throws ServiceRequestException
	{
		return this.getWebFbtStub().isFileCompareEnabled();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.FBTS#getFileCompareConfig()
	 */
	@Override public FileOpenConfig getFileCompareConfig() throws ServiceRequestException
	{
		return this.getWebFbtStub().getFileCompareConfig();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.FBTS#getWebFileViewInfo(dyna.common.bean.data.FoundationObject, java.lang.String,
	 * dyna.common.bean.data.system.DSSFileInfo)
	 */
	@Override public String getWebFileViewInfo(FoundationObject foundationObject, String guid, DSSFileInfo file) throws ServiceRequestException
	{
		return this.getWebFbtStub().getWebFileViewInfo(foundationObject, guid, file);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.FBTS#getWebFileCompareInfo(java.lang.String, dyna.common.bean.data.ObjectGuid,
	 * dyna.common.bean.data.system.DSSFileInfo, dyna.common.bean.data.ObjectGuid,
	 * dyna.common.bean.data.system.DSSFileInfo)
	 */
	@Override public String getWebFileCompareInfo(String guid, ObjectGuid fileObjectGuid1, DSSFileInfo file1, ObjectGuid fileObjectGuid2, DSSFileInfo file2)
			throws ServiceRequestException
	{
		return this.getWebFbtStub().getWebFileCompareInfo(guid, fileObjectGuid1, file1, fileObjectGuid2, file2);
	}

	/**
	 * @return the webSerVerInfo
	 */
	public String getWebSerVerInfo()
	{
		return webSerVerInfo;
	}

	/**
	 * @return the webServerFilePath
	 */
	public String getWebServerFilePath()
	{
		return webServerFilePath;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.FBTS#listBIFileByConfigGuid(java.lang.String, dyna.common.bean.data.system.DSSFileInfo)
	 */
	@Override public List<DSSFileTrans> listBIFileByFileGuid(String guid) throws ServiceRequestException
	{

		return this.getFbtStub().listBIFileByFileGuid(guid);
	}

}
