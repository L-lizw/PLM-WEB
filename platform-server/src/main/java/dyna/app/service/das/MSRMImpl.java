/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: MSRMImpl
 * Wanglei 2010-7-1
 */
package dyna.app.service.das;

import java.text.MessageFormat;
import java.util.Map;

import dyna.app.server.context.ApplicationServerContext;
import dyna.app.service.DataAccessService;
import dyna.common.util.StringUtils;
import dyna.net.service.das.MSRM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Wanglei
 *
 */
@Service
public class MSRMImpl extends DataAccessService implements MSRM
{
	@Autowired
	private ApplicationServerContext serverContext;

	/*
	 * (non-Javadoc)
	 * 
	 * @see dyna.app.service.das.MSRM#getMSRString(java.lang.String, java.lang.String)
	 */
	@Override
	public String getMSRString(String id, String locale)
	{
		String message = this.serverContext.getNLSManager().getMSRString(id, locale);
		return message == null ? id : message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dyna.app.service.das.MSRM#getMSRMap(java.lang.String)
	 */
	@Override
	public Map<String, String> getMSRMap(String locale)
	{
		return this.serverContext.getNLSManager().getMSRMap(locale);
	}

	@Override
	public String getMSRString(String id, String locale, Object... arguments)
	{
		String pattern = getMSRString(id, locale);
		if (StringUtils.isNullString(pattern))
		{
			return null;
		}
		return MessageFormat.format(pattern, arguments);
	}
}
