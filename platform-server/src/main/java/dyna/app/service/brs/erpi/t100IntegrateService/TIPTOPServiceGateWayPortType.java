/**
 * TIPTOPServiceGateWayPortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package dyna.app.service.brs.erpi.t100IntegrateService;

public interface TIPTOPServiceGateWayPortType extends java.rmi.Remote {
    public String invokeSrv(String request) throws java.rmi.RemoteException;
    public String invokeMdm(String request) throws java.rmi.RemoteException;
    public String syncProd(String request) throws java.rmi.RemoteException;
    public String callbackSrv(String request) throws java.rmi.RemoteException;
}
