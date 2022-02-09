package dyna.net.portal.integrateService;

public class IssueandValidateTicketWSProxy implements IssueandValidateTicketWS
{
	private String						_endpoint					= null;
	private IssueandValidateTicketWS	issueandValidateTicketWS	= null;

	public IssueandValidateTicketWSProxy()
	{
		_initIssueandValidateTicketWSProxy();
	}

	public IssueandValidateTicketWSProxy(String endpoint)
	{
		_endpoint = endpoint;
		_initIssueandValidateTicketWSProxy();
	}

	private void _initIssueandValidateTicketWSProxy()
	{
		try
		{
			issueandValidateTicketWS = (new IssueandValidateTicketWSImplServiceLocator())
					.getIssueandValidateTicketWSImplPort();
			if (issueandValidateTicketWS != null)
			{
				if (_endpoint != null)
					((javax.xml.rpc.Stub) issueandValidateTicketWS)._setProperty(
							"javax.xml.rpc.service.endpoint.address", _endpoint);
				else
					_endpoint = (String) ((javax.xml.rpc.Stub) issueandValidateTicketWS)
							._getProperty("javax.xml.rpc.service.endpoint.address");
			}

		}
		catch (javax.xml.rpc.ServiceException serviceException)
		{
		}
	}

	public String getEndpoint()
	{
		return _endpoint;
	}

	public void setEndpoint(String endpoint)
	{
		_endpoint = endpoint;
		if (issueandValidateTicketWS != null)
			((javax.xml.rpc.Stub) issueandValidateTicketWS)._setProperty("javax.xml.rpc.service.endpoint.address",
					_endpoint);

	}

	public IssueandValidateTicketWS getIssueandValidateTicketWS()
	{
		if (issueandValidateTicketWS == null)
			_initIssueandValidateTicketWSProxy();
		return issueandValidateTicketWS;
	}

	public String getModulusForXMLRSA() throws java.rmi.RemoteException
	{
		if (issueandValidateTicketWS == null)
			_initIssueandValidateTicketWSProxy();
		return issueandValidateTicketWS.getModulusForXMLRSA();
	}

	public String getValidatedUser(byte[] arg0, String arg1, byte[] arg2)
			throws java.rmi.RemoteException, InvalidSOKException
	{
		if (issueandValidateTicketWS == null)
			_initIssueandValidateTicketWSProxy();
		return issueandValidateTicketWS.getValidatedUser(arg0, arg1, arg2);
	}

	public String getValidatedAD(String arg0, String arg1)
			throws java.rmi.RemoteException
	{
		if (issueandValidateTicketWS == null)
			_initIssueandValidateTicketWSProxy();
		return issueandValidateTicketWS.getValidatedAD(arg0, arg1);
	}

	public String getValidatedUserInBase64(String arg0, String arg1, String arg2)
			throws java.rmi.RemoteException, InvalidSOKException
	{
		if (issueandValidateTicketWS == null)
			_initIssueandValidateTicketWSProxy();
		return issueandValidateTicketWS.getValidatedUserInBase64(arg0, arg1, arg2);
	}

	public String getPublicExponentForXMLRSA() throws java.rmi.RemoteException
	{
		if (issueandValidateTicketWS == null)
			_initIssueandValidateTicketWSProxy();
		return issueandValidateTicketWS.getPublicExponentForXMLRSA();
	}

	public PubKeyInfo getPubKeyInfo() throws java.rmi.RemoteException
	{
		if (issueandValidateTicketWS == null)
			_initIssueandValidateTicketWSProxy();
		return issueandValidateTicketWS.getPubKeyInfo();
	}

}