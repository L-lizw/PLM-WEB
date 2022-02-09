/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: LicenseStub
 * Wanglei 2011-9-19
 */
package dyna.app.service.brs.lic;

import dyna.app.service.AbstractServiceStub;
import dyna.common.Version;
import dyna.common.dto.Session;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import dyna.common.systemenum.ApplicationTypeEnum;
import dyna.common.systemenum.LanguageEnum;
import dyna.common.util.SetUtils;
import dyna.common.util.StringUtils;
import dyna.net.security.signature.SignatureFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 有license信息相关的分支
 * 
 * @author Wanglei
 * 
 */
@Component
public class LicenseStub extends AbstractServiceStub<LICImpl>
{

	protected void init()
	{
		try
		{
			List<Session> sessionList = this.stubService.listSession();
			if (SetUtils.isNullList(sessionList))
			{
				return;
			}

			String sessionId = null;
			String userId = null;
			String userName = null;
			String userGuid = null;
			String groupId = null;
			String groupName = null;
			String loginGroupGuid = null;
			String roleId = null;
			String roleName = null;
			String loginRoleGuid = null;
			String ip = null;
			LanguageEnum lang = null;
			ApplicationTypeEnum appType = null;
			this.serverContext.getLicenseDaemon().resetLicense(this.stubService);
			for (Session session : sessionList)
			{
				sessionId = session.getGuid();
				userId = session.getUserId();
				userName = session.getUserName();
				userGuid = session.getUserGuid();
				groupId = session.getLoginGroupId();
				groupName = session.getLoginGroupName();
				loginGroupGuid = session.getLoginGroupGuid();
				roleId = session.getLoginRoleId();
				roleName = session.getLoginRoleName();
				loginRoleGuid = session.getLoginRoleGuid();
				ip = session.getIpAddress();
				lang = session.getLanguageEnum();
				appType = session.getAppType();
				String bizModelGuid = session.getBizModelGuid();
				String bizModelName = session.getBizModelName();

				this.serverContext.getCredentialManager().bind(sessionId,//
						SignatureFactory.createSignature(userId, userName, userGuid, //
								groupId, groupName, loginGroupGuid, //
								roleId, roleName, loginRoleGuid, //
								ip, appType, lang, bizModelGuid, bizModelName, null));
			}
		}
		catch (ServiceRequestException e)
		{
			DynaLogger.error(e.getMessage());
		}
	}

	protected int[] getLicenseOccupiedNode() throws ServiceRequestException
	{
		return this.serverContext.getLicenseDaemon().getLicenseInUse(this.stubService);
	}

	protected int[] getLicenseNode() throws ServiceRequestException
	{
		return this.serverContext.getLicenseDaemon().getLicenseNode();
	}

	protected String getLicenseModules() throws ServiceRequestException
	{
		return this.serverContext.getLicenseDaemon().getLicenseModules();
	}

	protected long[] getLicensePeriod() throws ServiceRequestException
	{
		return this.serverContext.getLicenseDaemon().getLicensePeriod();
	}

	protected String getVersionInfo() throws ServiceRequestException
	{
		return Version.getVersionInfo();
	}

	/**
	 * @return
	 */
	public String getSystemIdentification()
	{
		String id=this.serverContext.getLicenseDaemon().getSystemIdentification();
		boolean isVM=this.serverContext.getLicenseDaemon().isVM();
		if (StringUtils.isNullString(id))
		{
			if (isVM)
			{
				return "Unable to connect to Guard Service";
			}
			else
			{
				return id;
			}
		}
		else if (isVM)
		{
			return id +" (Guard Service Id)";
		}
		else
		{
			return id +" (Mac Address)";
		}
	}
}
