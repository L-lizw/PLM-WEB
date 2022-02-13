/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: CSAImpl Common Service Access implementation
 * Wanglei 2010-9-6
 */
package dyna.app.service.das.csa;

import dyna.app.service.DataAccessService;
import dyna.common.bean.signature.Signature;
import dyna.common.exception.AuthorizeException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.util.StringUtils;
import dyna.net.security.CredentialManager;
import dyna.net.security.signature.ModuleSignature;
import dyna.net.security.signature.SignatureFactory;
import dyna.net.service.das.CSA;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Common Service Access implementation
 * 
 * @author Wanglei
 * 
 */
@Service
public class CSAImpl extends DataAccessService implements CSA
{

	@Autowired
	private CSAStub csaStub;

	protected CSAStub getCsaStub()
	{
		return this.csaStub;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dyna.net.service.das.CSA#registerSFTP()
	 */
	@Override
	public String registerDSS() throws ServiceRequestException
	{
		return this.csaStub.registerDSS();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dyna.net.service.das.CSA#registerSessionManager()
	 */
	@Override
	public String registerSessionManager() throws ServiceRequestException
	{
		return this.csaStub.registerSessionManager();
	}

}
