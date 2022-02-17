/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: ServerContext
 * Wanglei 2010-3-26
 */
package dyna.app.server.context;

import dyna.app.server.core.i18n.NLSManager;
import dyna.app.server.core.lic.License;
import dyna.app.server.core.lic.LicenseDaemon;
import dyna.app.server.core.lic.LicenseException;
import dyna.app.server.core.lic.LicenseManager;
import dyna.app.server.core.track.TrackerManager;
import dyna.common.conf.ConfigurableConnToDSImpl;
import dyna.app.conf.yml.ConfigurableServerImpl;
import dyna.common.context.AbstractSvContext;
import dyna.common.dto.Session;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import dyna.common.systemenum.ApplicationTypeEnum;
import dyna.common.systemenum.ConnectionModeEnum;
import dyna.common.systemenum.ServiceStateEnum;
import dyna.common.util.EnvUtils;
import dyna.common.util.FileUtils;
import dyna.common.util.SetUtils;
import dyna.common.util.StringUtils;
import dyna.net.dispatcher.sync.ServiceStateChangeReactor;
import dyna.net.security.CredentialManager;
import dyna.net.security.signature.SignatureFactory;
import dyna.net.security.signature.UserSignature;
import dyna.net.service.brs.LIC;
import dyna.net.service.data.DSCommonService;
import dyna.net.service.data.SystemDataService;
import dyna.net.syncfile.download.Downloader;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.management.ManagementFactory;
import java.util.*;

/**
 * 服务器上下文的实现
 *
 * @author Lizw
 */
@Service public class ApplicationServerContextImpl extends AbstractSvContext implements ApplicationServerContext
{
	private static final long serialVersionUID = -5782779160183354988L;

	private static final Map<String, Long> SESSION_UDT_MAP = new HashMap<String, Long>();

	@DubboReference private DSCommonService   dsCommonService;
	@DubboReference private SystemDataService systemDataService;

	@Autowired private ConfigurableServerImpl    svConfig;
	@Autowired private TrackerManager            trackerManager;
	@Autowired private NLSManager                nlsManager;
	@Autowired private ServiceStateChangeReactor sscReactor;
	@Autowired private CredentialManager         credentialManager;
	@Autowired private LicenseManager            licenseManager;

	private LicenseDaemon licenseDaemon   = null;
	private UserSignature sysIntSignature = null;

	private boolean isDebugMode = false;

	public ApplicationServerContextImpl()
	{
		super("DynaTeam Server Context");
	}

	@Override public void init() throws Exception
	{
		List<String> args = ManagementFactory.getRuntimeMXBean().getInputArguments();
		for (String arg : args)
		{
			if (arg.startsWith("-Xrunjdwp") || arg.startsWith("-agentlib:jdwp"))
			{
				isDebugMode = true;
				break;
			}
		}

		// load config from file
		DynaLogger.info("<" + this.svConfig.getId() + ": " + this.svConfig.getDescription() + ">");

		// license manager initialize.
		licenseManager.testLicenseValid(this.svConfig.getIp());

		this.initLicenseDaemon(licenseManager);

		Session ssn = dsCommonService.getSystemInternal();

		if (ssn != null && ssn.getLanguageEnum() != this.svConfig.getLanguage())
		{
			ssn.setLanguageEnum(this.svConfig.getLanguage());
			systemDataService.save(ssn);
		}

		this.sysIntSignature = (UserSignature) SignatureFactory.createSignature(ssn.getUserId(), ssn.getUserName(), ssn.getUserGuid(), //
				ssn.getLoginGroupId(), ssn.getLoginGroupName(), ssn.getLoginGroupGuid(), //
				ssn.getLoginRoleId(), ssn.getLoginRoleName(), ssn.getLoginRoleGuid(), //
				ssn.getIpAddress(), ssn.getAppType(), ssn.getLanguageEnum(), ssn.getBizModelGuid(), ssn.getBizModelName());
		this.credentialManager.bind(ssn.getGuid(), this.sysIntSignature);

		this.nlsManager.loadStringRepository();

		DynaLogger.info("Server initialized successfully.");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.app.server.context.ServerContext#getSystemInternalSessionId()
	 */
	@Override public UserSignature getSystemInternalSignature()
	{
		return this.sysIntSignature;
	}

	@Override public boolean isDebugMode()
	{
		return this.isDebugMode;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.app.server.context.ServerContext#getServerConfig()
	 */
	@Override public ConfigurableServerImpl getServerConfig()
	{
		return this.svConfig;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.common.context.AbstractContext#setServiceState(dyna.common.systemenum.ServiceStateEnum)
	 */
	@Override public synchronized ServiceStateEnum setServiceState(ServiceStateEnum serviceState)
	{
		ServiceStateEnum newState = super.setServiceState(serviceState);
		if (newState != serviceState)
		{
			return newState;
		}

		if (newState == ServiceStateEnum.WAITING)
		{
			try
			{
				DynaLogger.info("start to refresh cache...");

				newState = super.setServiceState(ServiceStateEnum.SYNCHRONIZE);

				ConfigurableConnToDSImpl connToDS = null;
				//todo
				//				DataServer.getRepositoryBean(ConfigLoaderFactory.class).getLoader4ConnToDS().getConfigurable();
				File iconFile = new File(EnvUtils.getRootPath() + "tmp/icon.zip");
				FileUtils.makeDirectories(iconFile.getAbsolutePath(), true);
				if (connToDS.getClientMode() == ConnectionModeEnum.DISTRIBUTED)
				{
					Downloader downloader = null;
					//doto
					//					DataServer.getSyncFileService().getDownloader(TransferEnum.DOWNLOAD_ICON_DS);
					FileOutputStream out = null;
					try
					{
						out = new FileOutputStream(iconFile);
						downloader.download(out);
					}
					finally
					{
						if (out != null)
						{
							out.close();
						}
					}
				}
				else
				{
					FileUtils.compress(new File(EnvUtils.getRootPath() + "/conf/icon"), iconFile);
				}
				File reportFile = new File(EnvUtils.getRootPath() + "tmp/report.zip");
				FileUtils.makeDirectories(reportFile.getAbsolutePath(), true);
				if (connToDS.getClientMode() == ConnectionModeEnum.DISTRIBUTED)
				{
					Downloader downloader = null;
					//todo
					//					DataServer.getSyncFileService().getDownloader(TransferEnum.DOWNLOAD_REPORT_DS);
					FileOutputStream out = null;
					try
					{
						out = new FileOutputStream(reportFile);
						downloader.download(out);
					}
					finally
					{
						if (out != null)
						{
							out.close();
						}
					}
					if (reportFile.length() > 0)
					{
						File tempDir = FileUtils.newFileEscape(EnvUtils.getRootPath() + "tmp/report");
						//todo
						//						FileUtils.decompress(EnvUtils.getRootPath() + "tmp/report.zip", tempDir);

						// 删除下载的压缩文件
						FileUtils.deleteFile(EnvUtils.getRootPath() + "tmp/report.zip");

						// 删除原报表文件目录，并创建新的文件目录
						String modelReportPath = EnvUtils.getRootPath() + "/conf/report";
						File modelReportDir = FileUtils.newFileEscape(modelReportPath);
						FileUtils.deleteFile(modelReportDir);
						if (FileUtils.makeDirectories(modelReportPath, false))
						{
							// 将解压的临时文件夹中文件目录复制到报表文件目录
							FileUtils.copyFile(FileUtils.newFileEscape(EnvUtils.getRootPath() + "tmp/report/report"), modelReportDir, false);
						}

						// 删除临时文件夹
						FileUtils.deleteFile(tempDir);
					}
					else
					{
						FileUtils.deleteFile(EnvUtils.getRootPath() + "tmp/report.zip");
					}
				}

				DynaLogger.info("end refresh cache.");
			}
			catch (Exception e)
			{
				DynaLogger.error("error occurs during refresh cache.", e);
				return ServiceStateEnum.INVALID;
			}
		}

		return newState;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.app.server.context.ServerContext#setSscReactor(dyna.net.dispatcher.sync.ServiceStateChangeReactor)
	 */
	@Override public void setSscReactor(ServiceStateChangeReactor sscReactor)
	{
		this.sscReactor = sscReactor;
	}

	@Override public ServiceStateChangeReactor getSscReactor()
	{
		return this.sscReactor;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.app.server.context.ServerContext#getLicenseDaemon()
	 */
	@Override public LicenseDaemon getLicenseDaemon()
	{
		return this.licenseDaemon;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.app.server.context.ServerContext#getTrackerManager()
	 */
	@Override public TrackerManager getTrackerManager()
	{
		return this.trackerManager;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.app.server.context.ServerContext#getNLSManager()
	 */
	@Override public NLSManager getNLSManager()
	{
		return this.nlsManager;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.app.server.context.ServerContext#getCredentialManager()
	 */
	@Override public CredentialManager getCredentialManager()
	{
		return this.credentialManager;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.app.server.context.ServerContext#getSessionUpdateTime(java.lang.String)
	 */
	@Override public boolean shouldUpdateSessionTime(String sessionId, long updateTime)
	{
		Long ret = SESSION_UDT_MAP.get(sessionId);
		if (ret == null || updateTime - ret > 120000)
		{
			SESSION_UDT_MAP.put(sessionId, updateTime);
			return ret == null ? false : true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.app.server.context.ServerContext#removeSessionUpdateTime(java.lang.String)
	 */
	@Override public void removeSessionUpdateTime(String sessionId)
	{
		SESSION_UDT_MAP.remove(sessionId);
	}

	private void initLicenseDaemon(final LicenseManager licenseManager)
	{
		this.licenseDaemon = new LicenseDaemon()
		{

			@Override public boolean hasLicence(String moduleName)
			{
				String aProduct = "DT-" + moduleName.toUpperCase();
				String pCode = licenseManager.getProductCode(aProduct);

				if (StringUtils.isNullString(pCode))
				{
					return false;
				}

				String mCode = licenseManager.getModuleCode(aProduct);
				String vCode = licenseManager.getVersionCode(aProduct);

				List<License> licTable = licenseManager.getLicenseTable();
				int i = this.findLicense(licTable, pCode, mCode, vCode);
				if (i >= 0)
				{
					return true;
				}

				return false;
			}

			private synchronized int findLicense(String moduleName)
			{
				String aProduct = "DT-" + moduleName.toUpperCase();
				String pCode = licenseManager.getProductCode(aProduct);

				if (StringUtils.isNullString(pCode))
				{
					return -1;
				}

				String mCode = licenseManager.getModuleCode(aProduct);
				String vCode = licenseManager.getVersionCode(aProduct);

				List<License> licTable = licenseManager.getLicenseTable();
				return this.findLicense(licTable, pCode, mCode, vCode);

			}

			private synchronized int findLicense(List<License> licTable, String productCode, String moduleCode, String versionCode)
			{
				for (int i = 0; i < licTable.size(); i++)
				{
					if (licTable.get(i).testFindSlot(productCode, moduleCode, versionCode))
					{
						return i;
					}
				}
				return -1;
			}

			@Override public synchronized boolean requestLicense(LIC lic, String moduleName) throws LicenseException
			{
				if ("WEB".equalsIgnoreCase(moduleName))
				{
					if (!this.hasLicence(moduleName))
					{
						throw new LicenseException("No license grant for " + moduleName);
					}

					if (!this.resetLicense(lic))
					{
						throw new LicenseException("request license failed: " + moduleName);
					}

					List<License> licTable = licenseManager.getLicenseTable();
					License license = licTable.get(findLicense(moduleName));
					if (!license.increaseUse())
					{
						throw new LicenseException("Maximum user limited by license, max: " + license.getAvailable() + ", in use: " + license.getInUse());
					}

				}
				else
				{
					moduleName = "BASE";
					if (!this.hasLicence(moduleName))
					{
						throw new LicenseException("No license grant for " + moduleName);
					}

					if (!this.resetLicense(lic))
					{
						throw new LicenseException("request license failed: " + moduleName);
					}

					List<License> licTable = licenseManager.getLicenseTable();
					int webindex = findLicense("WEB");
					License weblicense = null;
					if (webindex > -1)
					{
						weblicense = licTable.get(webindex);
					}
					int webwxindex = findLicense("WEBWX");
					License webwxlicense = null;
					if (webwxindex > -1)
					{
						webwxlicense = licTable.get(webwxindex);
					}
					for (License license : licTable)
					{
						if (license != weblicense && license != webwxlicense)
						{
							if (!license.increaseUse())
							{
								throw new LicenseException("Maximum user limited by license, max: " + license.getAvailable() + ", in use: " + license.getInUse());
							}
						}
					}
				}
				return true;
			}

			@Override public synchronized void releaseLicense(String moduleName)
			{
				List<License> licTable = licenseManager.getLicenseTable();
				int webindex = findLicense("WEB");
				License weblicense = null;
				if (webindex > -1)
				{
					weblicense = licTable.get(webindex);
				}
				int webwxindex = findLicense("WEBWX");
				License webwxlicense = null;
				if (webwxindex > -1)
				{
					webwxlicense = licTable.get(webwxindex);
				}
				if ("WEB".equalsIgnoreCase(moduleName))
				{
					if (weblicense != null)
					{
						weblicense.decreaseUse();
					}
				}
				else
				{
					for (License license : licTable)
					{
						if (license != weblicense && license != webwxlicense)
						{
							license.decreaseUse();
						}
					}
				}
			}

			@Override public int[] getLicenseInUse(LIC lic) throws ServiceRequestException
			{
				this.resetLicense(lic);
				List<License> licTable = licenseManager.getLicenseTable();
				if (SetUtils.isNullList(licTable))
				{
					return new int[] { 0, 0 };
				}
				else
				{
					int webindex = findLicense("WEB");
					if (webindex > -1)
					{
						return new int[] { licTable.get(0).getInUse(), licTable.get(webindex).getInUse() };
					}
					else
					{
						return new int[] { licTable.get(0).getInUse(), 0 };
					}
				}
			}

			@Override public int[] getLicenseNode() throws ServiceRequestException
			{
				List<License> licTable = licenseManager.getLicenseTable();
				if (SetUtils.isNullList(licTable))
				{
					return new int[] { 0, 0 };
				}
				else
				{
					int webindex = findLicense("WEB");
					if (webindex > -1)
					{
						return new int[] { licTable.get(0).getAvailable(), licTable.get(webindex).getAvailable() };
					}
					else
					{
						return new int[] { licTable.get(0).getAvailable(), 0 };
					}
				}
			}

			@Override public String getLicenseModules() throws ServiceRequestException
			{
				return licenseManager.getProductList();
			}

			@Override public synchronized boolean resetLicense(LIC lic)
			{
				try
				{
					List<Session> occupant = lic.listLicensedOccupant();
					List<License> licTable = licenseManager.getLicenseTable();
					if (SetUtils.isNullList(occupant))
					{
						for (License license : licTable)
						{
							license.resetInUse(0);
						}
					}
					else
					{
						int size = 0;
						int websize = 0;
						for (Session oSession : occupant)
						{
							if (oSession.getAppType() == ApplicationTypeEnum.WEB)
							{
								websize++;
							}
							else
							{
								size++;
							}
						}
						int webindex = findLicense("WEB");
						License weblicense = null;
						if (webindex > -1)
						{
							weblicense = licTable.get(webindex);
						}
						int webwxindex = findLicense("WEBWX");
						License webwxlicense = null;
						if (webwxindex > -1)
						{
							webwxlicense = licTable.get(webwxindex);
						}
						for (License license : licTable)
						{
							if (weblicense == license)
							{
								license.resetInUse(websize);
							}
							else if (webwxlicense != license)
							{
								license.resetInUse(size);
							}

						}

					}
					return true;
				}
				catch (ServiceRequestException e)
				{
					DynaLogger.error(e.getMessage(), e);
				}
				return false;
			}

			@Override public long[] getLicensePeriod()
			{
				if (SetUtils.isNullList(licenseManager.getLicenseTable()))
				{
					return null;
				}
				else
				{
					License license = licenseManager.getLicenseTable().get(0);
					return new long[] { license.getStartDate(), license.getEndDate() };
				}
			}

			@Override public String getSystemIdentification()
			{
				return licenseManager.getSystemIdentification();
			}

			@Override public boolean isVM()
			{
				return licenseManager.isVM();
			}
		};
	}

}
