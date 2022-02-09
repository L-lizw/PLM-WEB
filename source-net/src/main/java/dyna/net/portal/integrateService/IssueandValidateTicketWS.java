/**
 * IssueandValidateTicketWS.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package dyna.net.portal.integrateService;

public interface IssueandValidateTicketWS extends java.rmi.Remote
{
	public String getModulusForXMLRSA() throws java.rmi.RemoteException;

	public String getValidatedUser(byte[] arg0, String arg1, byte[] arg2)
			throws java.rmi.RemoteException, InvalidSOKException;

	public String getValidatedAD(String arg0, String arg1)
			throws java.rmi.RemoteException;

	public String getValidatedUserInBase64(String arg0, String arg1, String arg2)
			throws java.rmi.RemoteException, InvalidSOKException;

	public String getPublicExponentForXMLRSA() throws java.rmi.RemoteException;

	public PubKeyInfo getPubKeyInfo() throws java.rmi.RemoteException;
}
