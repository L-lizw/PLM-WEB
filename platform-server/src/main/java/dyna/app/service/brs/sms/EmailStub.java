/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: EmailStub
 * WangLHB Feb 23, 2012
 */
package dyna.app.service.brs.sms;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.helper.ServiceRequestExceptionWrap;
import dyna.common.dto.EmailServer;
import dyna.common.exception.DynaDataException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.util.SetUtils;
import dyna.net.service.data.SystemDataService;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author WangLHB
 *
 */
@Component
public class EmailStub extends AbstractServiceStub<SMSImpl>
{

	protected EmailServer saveEmailServer(EmailServer emailServer) throws ServiceRequestException
	{
		SystemDataService sds = this.stubService.getSystemDataService();
		try
		{
			emailServer.put(EmailServer.CREATE_USER_GUID, this.stubService.getOperatorGuid());
			emailServer.put(EmailServer.UPDATE_USER_GUID, this.stubService.getOperatorGuid());

			sds.save(emailServer);
			return this.getEmailServer();
		}
		catch (DynaDataException e)
		{
			throw ServiceRequestExceptionWrap.createByDynaDataException(this.stubService, e);
		}
	}

	protected EmailServer getEmailServer() throws ServiceRequestException
	{
		EmailServer server = null;
		SystemDataService sds = this.stubService.getSystemDataService();
		Map<String, Object> filter = new HashMap<String, Object>();
		try
		{
			List<EmailServer> serverList = sds.query(EmailServer.class, filter);
			if (!SetUtils.isNullList(serverList))
			{
				server = serverList.get(0);
			}
		}
		catch (DynaDataException e)
		{
			throw ServiceRequestExceptionWrap.createByDynaDataException(this.stubService, e);
		}

		return server;
	}

}
