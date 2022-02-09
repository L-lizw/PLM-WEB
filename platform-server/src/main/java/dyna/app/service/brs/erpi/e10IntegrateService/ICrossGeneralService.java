/**
 * ICrossGeneralService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package dyna.app.service.brs.erpi.e10IntegrateService;

public interface ICrossGeneralService extends java.rmi.Remote {
    public String invokeSrv(String xml) throws java.rmi.RemoteException;
    public String callbackSrv(String xml) throws java.rmi.RemoteException;
    public String syncProd(String xml) throws java.rmi.RemoteException;
    public String invokeMdm(String xml) throws java.rmi.RemoteException;
}
