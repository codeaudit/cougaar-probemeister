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
/*
 * GaugeRequestDialog.java
 *
 * Created on April 25, 2002, 9:24 AM
 */

/**
 *
 * @author  Administrator
 */
 
//com.objs.surveyor.probemeister.remote.GaugeRequestLocation

package com.objs.surveyor.probemeister.remote;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.RMISecurityManager;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
public class GaugeRequest extends UnicastRemoteObject
    implements GaugeRequestInterface {

    String gaugeID;
    String deployID;
    String deployDesc;
    String probeName;
    String plugName;
    String plugClass;
    String vmName;
    int deployOption;
    GaugeRequestLocationInterface[] locations;   
    Map probeParams;
    
    boolean deployed = false;

    /* Creates a gauge request. Cannot really auto-deploy (see deployOption) at this point since each
     * probe needs some customization. The gauge request would also need to include
     * the configuration information (an xml config string) to enable this.
     */
    public GaugeRequest(String _gaugeID, String _deployID, String _deployDesc, 
            String _probeName, String _plugClass, String _plugName, String _vmName, 
            GaugeRequestLocationInterface[] _locations, int _deployOption, 
            Map _probeParams)
        throws RemoteException {
        
        gaugeID = _gaugeID;
        deployID = _deployID;
        deployDesc = _deployDesc;
        probeName = _probeName;
        plugName = _plugName;
        plugClass = _plugClass;
        vmName = _vmName;
        locations = _locations;                                        
        deployOption = _deployOption;
        probeParams = _probeParams;
    }

    /* Needed all because we cannot override the toString() method.
     * Thus, when we get a remote GaugeRequestInterface object
     * we simply make a local copy that can override the toString()
     * method - needed for proper disply in gui JLists...
     */
    public GaugeRequest(GaugeRequestInterface _gri)
        throws RemoteException {
        
        gaugeID = _gri.getGaugeID();
        deployID = _gri.getDeployID();
        deployDesc = _gri.getDeployDesc();
        probeName = _gri.getProbeName();
        plugName = _gri.getPlugName();
        plugClass = _gri.getPlugClass();
        vmName = _gri.getVMName();
        probeParams = _gri.getProbeParams();

        GaugeRequestLocationInterface[] loco = _gri.getLocations();
        if (loco != null) {
            locations = new GaugeRequestLocationInterface[loco.length];     
            for (int i=0;i<loco.length;i++) 
                locations[i] = new GaugeRequestLocation(loco[i]);
        }
        deployOption = _gri.getDeployOption();
        
        deployed = _gri.getDeployed();
    }

    public void setGaugeID(String _gaugeID) throws RemoteException {gaugeID = _gaugeID;}
    public void setDeployID(String _deployID) throws RemoteException {deployID = _deployID;}
    public void setDeployDesc(String _deployDesc) throws RemoteException {deployDesc = _deployDesc;}
    public void setProbeName(String _probeName) throws RemoteException {probeName = _probeName;}
    public void setPlugName(String _plugName) throws RemoteException {plugName = _plugName;}
    public void setPlugClass(String _plugClass) throws RemoteException {plugClass = _plugClass;}
    public void setVMName(String _vmName) throws RemoteException {vmName = _vmName;}
    public void setLocations(GaugeRequestLocationInterface[] _locations) throws RemoteException {locations = _locations;}
    public void setDeployOption(int i) throws RemoteException { deployOption = i; }
    public void setDeployed(boolean _d) throws RemoteException { 
        deployed = _d; 
        }
    public void setProbeParams(java.util.Map _probeParams) throws RemoteException {
        probeParams = _probeParams;
    }        

    public String getGaugeID() throws RemoteException { return gaugeID;}
    public String getDeployID() throws RemoteException { return deployID;}
    public String getDeployDesc() throws RemoteException {return deployDesc;}
    public String getProbeName() throws RemoteException {return probeName;}
    public String getPlugName() throws RemoteException {return plugName;}
    public String getPlugClass() throws RemoteException {return plugClass;}
    public String getVMName() throws RemoteException {return vmName;}
    public GaugeRequestLocationInterface[] getLocations() throws RemoteException {return locations;}
    public int getDeployOption() throws RemoteException {return deployOption;}
    public boolean  getDeployed() throws RemoteException { return deployed; }
    public java.util.Map getProbeParams() throws RemoteException {return probeParams;}
    
    /* Returns the location to insert the probe, or 0 if one is not found. */
    public int getLocationForMethod(String _cls, String _meth) 
        throws RemoteException {
        try {
            com.objs.surveyor.probemeister.Log.out.fine("Searching locations for "+_cls+":"+_meth);
            if (locations == null) return -2; // default
            for (int i=0; i<locations.length; i++) {
                GaugeRequestLocationInterface grli = locations[i];
                if (grli.getClassName().equals(_cls) && grli.getMethodName().equals(_meth))
                    return grli.getLocation();
            }
        } catch (java.rmi.RemoteException re) {
            com.objs.surveyor.probemeister.Log.out.warning("ERROR: Searching locations for "+_cls+":"+_meth);
        }
        return -2; //
    }
    
    public String toString() { return gaugeID + " : " + deployDesc; }
    public String toString2() { return "\n"+super.toString()+"\n"+toString();}

}

            
