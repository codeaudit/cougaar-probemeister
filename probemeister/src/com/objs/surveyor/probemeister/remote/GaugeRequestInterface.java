/* 
 * <copyright> 
 *   
 *  Copyright 1999-2004 Object Services and Consulting, Inc. 
 *  under sponsorship of the Defense Advanced Research Projects 
 *  Agency (DARPA). 
 *  
 *  You can redistribute this software and/or modify it under the 
 *  terms of the Cougaar Open Source License as published on the 
 *  Cougaar Open Source Website (www.cougaar.org). 
 *  
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 *   
 * </copyright> 
 */ 



package com.objs.surveyor.probemeister.remote;

import java.rmi.Remote; 
import java.rmi.RemoteException; 


/* Describes location where a probe may be deployed */
public interface GaugeRequestInterface extends Remote {

    //Deploy Option
    public static final int REUSE_LAST_CONFIG = 0; //No user intervention, deploys immediately
                                      //  using last deployment (if one), o.w. 
                                      //  defaults to USER_CHOICE
    public static final int USER_CHOICE = 1; //Displays last deployment (if any), but allows user 
                                //  to choose a new location

    public void setGaugeID(String s) throws RemoteException ;
    public void setDeployID(String s) throws RemoteException ;
    public void setDeployDesc(String s) throws RemoteException ;
    public void setProbeName(String s) throws RemoteException;
    public void setPlugName(String s) throws RemoteException;
    public void setPlugClass(String s) throws RemoteException;
    public void setVMName(String s) throws RemoteException;
    public void setDeployOption(int i) throws RemoteException;
    public void setLocations(GaugeRequestLocationInterface[] grlocs) throws RemoteException;
    public void setDeployed(boolean _d) throws RemoteException;
    public void setProbeParams(java.util.Map _map) throws RemoteException;

    public String getGaugeID() throws RemoteException;
    public String getDeployID() throws RemoteException;
    public String getDeployDesc() throws RemoteException;
    public String getProbeName() throws RemoteException;
    public String getPlugName() throws RemoteException;
    public String getPlugClass() throws RemoteException;
    public String getVMName() throws RemoteException;
    public int getDeployOption() throws RemoteException;
    public GaugeRequestLocationInterface[] getLocations() throws RemoteException;
    public boolean getDeployed() throws RemoteException;
    public java.util.Map getProbeParams() throws RemoteException;

    public int getLocationForMethod(String _cls, String _meth) throws RemoteException;

//    public Object[] getLocations() throws RemoteException;
//    public String toString();

}
