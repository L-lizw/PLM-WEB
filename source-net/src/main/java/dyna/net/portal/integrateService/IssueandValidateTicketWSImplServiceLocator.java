/**
 * IssueandValidateTicketWSImplServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package dyna.net.portal.integrateService;

import dyna.common.util.StringUtils;

public class IssueandValidateTicketWSImplServiceLocator extends org.apache.axis.client.Service implements
		IssueandValidateTicketWSImplService
{

	// Use to get a proxy class for IssueandValidateTicketWSImplPort
	private String	IssueandValidateTicketWSImplPort_address		= "http://10.20.86.137:8081/cas-web/WebService/IssueandValidateTicketWS";

	// The WSDD service name defaults to the port name.
	private String	IssueandValidateTicketWSImplPortWSDDServiceName	= "IssueandValidateTicketWSImplPort";

	public IssueandValidateTicketWSImplServiceLocator()
	{
	}

	public IssueandValidateTicketWSImplServiceLocator(org.apache.axis.EngineConfiguration config)
	{
		super(config);
	}

	public IssueandValidateTicketWSImplServiceLocator(String wsdlLoc, javax.xml.namespace.QName sName)
			throws javax.xml.rpc.ServiceException
	{
		super(wsdlLoc, sName);
	}

	public IssueandValidateTicketWSImplServiceLocator(String address)
	{
		if (!StringUtils.isNullString(address))
		{
			this.IssueandValidateTicketWSImplPort_address = address;
		}
	}

	public String getIssueandValidateTicketWSImplPortAddress()
	{
		return IssueandValidateTicketWSImplPort_address;
	}

	public String getIssueandValidateTicketWSImplPortWSDDServiceName()
	{
		return IssueandValidateTicketWSImplPortWSDDServiceName;
	}

	public void setIssueandValidateTicketWSImplPortWSDDServiceName(String name)
	{
		IssueandValidateTicketWSImplPortWSDDServiceName = name;
	}

	public IssueandValidateTicketWS getIssueandValidateTicketWSImplPort() throws javax.xml.rpc.ServiceException
	{
		java.net.URL endpoint;
		try
		{
			endpoint = new java.net.URL(IssueandValidateTicketWSImplPort_address);
		}
		catch (java.net.MalformedURLException e)
		{
			throw new javax.xml.rpc.ServiceException(e);
		}
		return getIssueandValidateTicketWSImplPort(endpoint);
	}

	public IssueandValidateTicketWS getIssueandValidateTicketWSImplPort(java.net.URL portAddress)
			throws javax.xml.rpc.ServiceException
	{
		try
		{
			IssueandValidateTicketWSImplServiceSoapBindingStub _stub = new IssueandValidateTicketWSImplServiceSoapBindingStub(
					portAddress, this);
			_stub.setPortName(getIssueandValidateTicketWSImplPortWSDDServiceName());
			return _stub;
		}
		catch (org.apache.axis.AxisFault e)
		{
			return null;
		}
	}

	public void setIssueandValidateTicketWSImplPortEndpointAddress(String address)
	{
		IssueandValidateTicketWSImplPort_address = address;
	}

	/**
	 * For the given interface, get the stub implementation.
	 * If this service has no port for the given interface,
	 * then ServiceException is thrown.
	 */
	public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException
	{
		try
		{
			if (IssueandValidateTicketWS.class.isAssignableFrom(serviceEndpointInterface))
			{
				IssueandValidateTicketWSImplServiceSoapBindingStub _stub = new IssueandValidateTicketWSImplServiceSoapBindingStub(
						new java.net.URL(IssueandValidateTicketWSImplPort_address), this);
				_stub.setPortName(getIssueandValidateTicketWSImplPortWSDDServiceName());
				return _stub;
			}
		}
		catch (Throwable t)
		{
			throw new javax.xml.rpc.ServiceException(t);
		}
		throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  "
				+ (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
	}

	/**
	 * For the given interface, get the stub implementation.
	 * If this service has no port for the given interface,
	 * then ServiceException is thrown.
	 */
	public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface)
			throws javax.xml.rpc.ServiceException
	{
		if (portName == null)
		{
			return getPort(serviceEndpointInterface);
		}
		String inputPortName = portName.getLocalPart();
		if ("IssueandValidateTicketWSImplPort".equals(inputPortName))
		{
			return getIssueandValidateTicketWSImplPort();
		}
		else
		{
			java.rmi.Remote _stub = getPort(serviceEndpointInterface);
			((org.apache.axis.client.Stub) _stub).setPortName(portName);
			return _stub;
		}
	}

	public javax.xml.namespace.QName getServiceName()
	{
		return new javax.xml.namespace.QName("http://webservice.dsc.com/", "IssueandValidateTicketWSImplService");
	}

	private java.util.HashSet	ports	= null;

	public java.util.Iterator getPorts()
	{
		if (ports == null)
		{
			ports = new java.util.HashSet();
			ports.add(new javax.xml.namespace.QName("http://webservice.dsc.com/", "IssueandValidateTicketWSImplPort"));
		}
		return ports.iterator();
	}

	/**
	 * Set the endpoint address for the specified port name.
	 */
	public void setEndpointAddress(String portName, String address)
			throws javax.xml.rpc.ServiceException
	{

		if ("IssueandValidateTicketWSImplPort".equals(portName))
		{
			setIssueandValidateTicketWSImplPortEndpointAddress(address);
		}
		else
		{ // Unknown Port Name
			throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
		}
	}

	/**
	 * Set the endpoint address for the specified port name.
	 */
	public void setEndpointAddress(javax.xml.namespace.QName portName, String address)
			throws javax.xml.rpc.ServiceException
	{
		setEndpointAddress(portName.getLocalPart(), address);
	}

}
