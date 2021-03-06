/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: DynaDAService
 * Wanglei 2010-3-26
 */
package dyna.app.service;

import dyna.app.server.core.SignableAdapter;
import dyna.app.service.das.jss.ScheduledTaskUdSessionImpl;
import dyna.common.bean.signature.Signable;
import dyna.common.bean.signature.Signature;
import dyna.common.bean.signature.Transactional;
import dyna.common.conf.ServiceDefinition;
import dyna.common.exception.AuthorizeException;
import dyna.common.exception.ServiceNotFoundException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import dyna.common.util.StringUtils;
import dyna.net.security.CredentialManager;
import dyna.net.security.signature.SignatureFactory;
import dyna.net.security.signature.UserSignature;
import dyna.net.service.ApplicationService;
import dyna.net.service.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * @author Wanglei
 */
public abstract class DataAccessService extends SignableAdapter implements Transactional, ApplicationService
{
	@Autowired private   CredentialManager       credentialManager;

	private ServiceDefinition serviceDefinition;

	private String moduleID          = null;
	private String serviceCredential = null;

	private String transactionId = null;

	private Map<Class<? extends ApplicationService>, ApplicationService> referenceServiceMap = null;

	private boolean isRefered = false;

	private final Object syncObject = new Object();

	private long activeSessionTime = 0;

	private boolean isActive = false;

	private final String serviceUID = StringUtils.generateRandomUID(32);

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.app.core.Signable#clearSignature()
	 */
	@Override public void clearSignature()
	{
		this.releaseRefService();
		super.clearSignature();
		this.cleartTransactionId();
	}

	public String getOperatorGuid() throws ServiceRequestException
	{
		return this.getUserSignature().getUserGuid();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.app.core.Signable#setSignature(dyna.net.security.signature.Signature)
	 */
	@Override public synchronized void setSignature(Signature signature) throws IllegalStateException
	{
		if (!this.isActive && signature != null)
		{
			DynaLogger.error("Service is Release , please check code", new IllegalStateException("Service is Release , please check code"));
		}
		super.setSignature(signature);
		if (this.isRefered == false && referenceServiceMap != null)
		{
			for (Iterator<ApplicationService> iter = this.referenceServiceMap.values().iterator(); iter.hasNext(); )
			{
				ApplicationService service = iter.next();
				if (service == this)
				{
					continue;
				}

				if (service instanceof Signable)
				{
					((Signable) service).setSignature(signature);
				}
			}
		}

	}

	public String getServiceCredential()
	{
		return this.serviceCredential;
	}

	public ServiceDefinition getServiceDefinition()
	{
		return this.serviceDefinition;
	}

	public String getServiceModuleID()
	{
		return this.moduleID;
	}

	public UserSignature getUserSignature() throws ServiceRequestException
	{
		if (this.signature instanceof UserSignature)
		{
			return ((UserSignature) this.signature);
		}
		throw ServiceRequestException.createByException(AuthorizeException.ID_PER_DENIED, new AuthorizeException("unauthorized signature"));
	}

	/**
	 * @param serviceDef
	 */
	@Override
	public void init(ServiceDefinition serviceDef)
	{
		this.serviceDefinition = serviceDef;
		this.moduleID = serviceDef.getName();

		this.serviceCredential = credentialManager.getModuleCredential(this.moduleID);
		if (StringUtils.isNullString(this.serviceCredential))
		{
			this.serviceCredential = UUID.randomUUID().toString();
			credentialManager.setModuleCredential(this.moduleID, this.serviceCredential);
			credentialManager.bind(this.serviceCredential, SignatureFactory.createSignature(this.moduleID));
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.app.core.pool.Poolable#makeObject(java.lang.Object[])
	 */
	//	@Override
	//	public void initObject(Object... initArgs) throws Exception
	//	{
	//		this.init((ServiceDefinition) initArgs[1]);
	//	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.app.core.pool.Poolable#passivateObject()
	 */
	//	@Override
	//	public void passivateObject() throws Exception
	//	{
	//		this.isActive = false;
	//		this.clearSignature();
	//	}

	private void releaseRefService()
	{
		synchronized (this.syncObject)
		{
			if (this.isRefered || this.referenceServiceMap == null)
			{
				this.isRefered = false;
				this.referenceServiceMap = null;
				return;
			}

			ApplicationService service = null;
			for (Iterator<ApplicationService> iter = this.referenceServiceMap.values().iterator(); iter.hasNext(); )
			{
				service = iter.next();
				if (service.equals(this))
				{
					continue;
				}
				if (service instanceof Signable)
				{
					((Signable) service).clearSignature();
				}
				//todo
				//				this.serviceContext.releaseService(service);
			}
			this.referenceServiceMap.clear();
			this.referenceServiceMap = null;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.app.core.pool.Poolable#validateObject()
	 */
	//	@Override
	//	public boolean validateObject()
	//	{
	//		return true;
	//	}

	@Override public String getFixedTransactionId()
	{
		return this.transactionId;
	}

	@Override public synchronized void setFixedTransactionId(String transactionId)
	{
		this.transactionId = transactionId;
	}

	@Override public String newTransactionId()
	{
		String newTransacationId = StringUtils.generateRandomUID(32);
		this.setFixedTransactionId(newTransacationId);
		return newTransacationId;
	}

	private synchronized void cleartTransactionId()
	{
		this.transactionId = null;
	}

	public void activeSession(boolean isUserUpdate)
	{
		if (activeSessionTime == 0)
		{
			activeSessionTime = System.currentTimeMillis();
		}
		else if (System.currentTimeMillis() - activeSessionTime > 30000)
		{
			(new ScheduledTaskUdSessionImpl(this.getSignature().getCredential(), isUserUpdate)).run();
			activeSessionTime = System.currentTimeMillis();
		}
	}

	//	@Override
	//	public String getObjectUID()
	//	{
	//		return this.serviceUID;
	//	}



}
