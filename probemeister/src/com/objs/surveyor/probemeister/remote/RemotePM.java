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
 
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.RMISecurityManager;
import java.rmi.server.UnicastRemoteObject;

import com.objs.surveyor.probemeister.gui.MultiJVM;
import com.objs.surveyor.probemeister.gui.TargetVMDataPanel;
import com.objs.surveyor.probemeister.TargetVMManager;

public class RemotePM extends UnicastRemoteObject
    implements RemotePMInterface {

    MultiJVM multi = null;
    
    public RemotePM(MultiJVM _multi) throws RemoteException {        
        super();
        multi = _multi;
    }
 
    /*
     * Request for ProbeMeister to attach to the specified running JVM
     * name = user friendly name
     * address = ip address. Leave as null if local connection
     * port = The JDI port the application has specified in the command line args
     * <p>Note the example:
     *    java -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,address=8888,suspend=n 
     *
     * <b>server</b> must be "y"<p>
     * <b>address</b> is the port number
     *
     * @return string name used to identify this JVM connection (name + : + address + : + port)
     * If address is null, then it returns (name + : + port)
     * Returns <b>null</b> if unsuccessful.
     */
    public String attachToJVM(String name, String addr, String port) 
        throws RemoteException {
        
	    try {
    	    TargetVMManager.getMgr().addTargetVM(name, addr, port);			        
        } catch (Exception e) {
    	    return null;
        }
        return name+":"+((addr!=null) ? addr+":" : "") +port;
        
    }
 
    public String[] getDataPanelNames() 
     throws RemoteException {
        
        String[] names=null;
        TargetVMDataPanel[] tabs = multi.getAllDataPanels();        
        //System.out.println("getDataPanelNames");
        if (tabs != null) {
            //System.out.println("getDataPanelNames -- got panels: "+tabs.length);
            names = new String[tabs.length];
            for (int i=0; i<tabs.length; i++) {    
                TargetVMDataPanel tab = tabs[i];
                //System.out.println("getDataPanelNames -- returning tabname = "+tab.getAppName());
                names[i]=tab.getAppName()+":"+tab.getAppAddr(); //could also consider appAddr...
            }
        }
        return names;
    }

    private TargetVMDataPanel getDataPanelForName(String _name) 
     throws RemoteException {
        
        TargetVMDataPanel[] tabs = multi.getAllDataPanels();        
        if (tabs != null) {
            for (int i=0; i<tabs.length; i++) {    
                TargetVMDataPanel tab = tabs[i];
                String appName = tab.getAppName() +":"+ tab.getAppAddr();
System.out.println("getDataPanelForName -- comparing _name="+_name+" and vm="+appName);
                if (appName != null && appName.equals(_name))
                    return tab;
            }
        }
        return null;
    }
    
    
    /* invoked to submit a gauge deplyment request for the PM user to process 
     * @return A string description of any error. Null if no error.
     */
    public String sendGaugeDeployRequest(GaugeRequestInterface _gr)
     throws RemoteException {                                        
        if (_gr == null) return "Gauge request is null.";
        
        TargetVMDataPanel dp = this.getDataPanelForName(_gr.getVMName());    
        if (dp == null) return "No Such VM found.";
        
        //send localized object in request to gui
        dp.processGaugeDeployRequest(new GaugeRequest(_gr));
//        dp.processGaugeDeployRequest(_gr);
        return null;
    
    }
    
}
