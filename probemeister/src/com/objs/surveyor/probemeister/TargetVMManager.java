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
 */

package com.objs.surveyor.probemeister;
import com.objs.surveyor.probemeister.event.EventHandler;
import com.objs.surveyor.probemeister.event.VMEventNotifier;
import java.util.Vector;
import java.util.Enumeration;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.event.*;

public class TargetVMManager {

    static private TargetVMMgrListener[] vmMgrListeners=null;
    static private TargetVMManager mgr = null;
    static {
        mgr = new TargetVMManager(); 
    }    
    static public TargetVMManager getMgr() {return mgr;}
    
    private TargetVMManager() {
    
        vmList = new Vector();
        //get ListenPort
        String portStr = (String) Globals.globals().get(GlobalVars.LISTEN_PORT_STR);        
        if (portStr != null) {
            ListenForVMs vmListenerThread = new ListenForVMs(this, portStr);        
            vmListenerThread.start();
            com.objs.surveyor.probemeister.Log.out.info("ListenForVMs thread started...");
        } else
            com.objs.surveyor.probemeister.Log.out.info("ListenForVMs thread Not started. No -listen command line argument found.");
//TEST
//        printAllConnectors();
    }
    private Vector vmList;
    
    public void printAllConnectors() {
        java.util.List connectors = Bootstrap.virtualMachineManager().allConnectors();
        java.util.Iterator iter = connectors.iterator();
        while (iter.hasNext()) {
            Connector c = (Connector)iter.next();
            System.out.println("Connector: "+c.name());
            System.out.println("           "+c.description());
        }
    }

    /*  @param name Name of the target VM
     *  @param address Address of the PC running the target VM
     *  @param port connection port 
     *  Return new TargetVMConnector if successful
     */
    public TargetVMConnector addTargetVM(String _name, String _address, String _port) 
    throws VMAddressInUseException {

        //Set 0 length values to null
        if (_port!=null && _port.length()==0) _port = null;
        if (_address!=null && _address.length()==0) _address = null;
    
        if (_port == null && _address != null) { //try dt_shmem connector
            return addTargetVM(_name, _address, "dt_shmem", false);
        } 
        else if (_address == null && _port != null ) {
            return addTargetVM(_name, _port, "dt_socket", false);
        }
        else if (_address != null && _port != null ) {
            return addTargetVM(_name, _address + ":" + _port, "dt_socket", false);
        }   
        else {
            com.objs.surveyor.probemeister.Log.out.warning("TargetVMManager::addTargetVM Error no address or port provided!");
            return null;
        }
    }
    
    
    /*  @param name Name of the target VM
     *  @param address Address of the target VM
     *  @param transport String defining transport mechanism to use [dt_shmem | dt_socket]
     *  @param reattach Reattach if this connector is already in use, dropping current connector
     *  Return new TargetVMConnector if successful
     */
    public TargetVMConnector addTargetVM(String _name, String _address, String _transport, boolean _reattach) 
        throws VMAddressInUseException {
    
        if (!addressInUse(_address)) {
            TargetVMConnector tvm=null;
            try {
                tvm = new TargetVMConnector(_name, _address, _transport, _reattach);
            } catch (Exception e) {
                com.objs.surveyor.probemeister.Log.out.warning("Exception while creating adding new target VM: \n"+e);
                //e.printStackTrace();
            }
            
            if (tvm != null) {
                vmList.add(tvm);
                com.objs.surveyor.probemeister.Log.out.fine("New TargetVMConnector added----------------------------------");

                //Let all listeners know about new TVM
                announceNewVM(tvm);                            

                //Further initialize the connection
                tvm.initVMConnection();                
            }
            
            printCurrentVMs();
            return tvm;
        }
        else throw new VMAddressInUseException("The address "+_address+" is already in use by another TargetVMConnector");
    }

    //Add VM - already exists
    public TargetVMConnector addTargetVM(VMData _vmd) {
        //throws VMAddressInUseException {
    
            TargetVMConnector tvm=null;
            tvm = new TargetVMConnector(_vmd);
            
            vmList.add(tvm);
            com.objs.surveyor.probemeister.Log.out.fine("New TargetVMConnector added----------------------------------");
            
            //Let all listeners know about new TVM
            announceNewVM(tvm);            
            
            //Further initialize the connection
            tvm.initVMConnection();                
            
            printCurrentVMs();
                
            return tvm;
    }
    
    
    public void printCurrentVMs() {
        java.util.List l = Bootstrap.virtualMachineManager().connectedVirtualMachines();
        com.objs.surveyor.probemeister.Log.out.info("There are currently "+l.size()+" connected VM(s)");        
    }
    
    public boolean addressInUse(String _addr) {
        
        Enumeration e = vmList.elements();
        while (e.hasMoreElements()) {
            TargetVMConnector tvm = (TargetVMConnector)e.nextElement();
            if (tvm.getAddress().equals(_addr))
                return true;
        }
        return false;
    }

    public TargetVMConnector getTargetVMConnectorByAddress(String _addr) {
        
        Enumeration e = vmList.elements();
        while (e.hasMoreElements()) {
            TargetVMConnector tvm = (TargetVMConnector)e.nextElement();
            if (tvm.getAddress().equals(_addr))
                return tvm;
        }
        return null;
    }


    public TargetVMConnector getTargetVMConnectorByVM(VirtualMachine _vm) {
        
        Enumeration e = vmList.elements();
        while (e.hasMoreElements()) {
            TargetVMConnector tvm = (TargetVMConnector)e.nextElement();
            if (tvm.vm().equals(_vm))
                return tvm;
        }
        return null;
    }
    
    public TargetVMConnector getTargetVMConnectorByName(String _name) {
        
        Enumeration e = vmList.elements();
        while (e.hasMoreElements()) {
            TargetVMConnector tvm = (TargetVMConnector)e.nextElement();
            if (tvm.getName().equals(_name))
                return tvm;
        }
        return null;
    }
    
    public boolean removeVMConnector(TargetVMConnector _tvm) {
        if (vmList.removeElement(_tvm)) {
            _tvm.emitVMEvent(TargetVMConnector.VMDISCONNECTED, null);
            com.objs.surveyor.probemeister.Log.out.fine("TargetVMManager:: VM with address("+_tvm.getAddress()+") has been removed.");
            return true;
        } else
            return false;
    }
    
    public void vmDropped(TargetVMConnector _tvm) {     
        com.objs.surveyor.probemeister.Log.out.fine("TargetVMManager::Lost VM Connection["+_tvm.getAddress()+"]...removing");
        this.removeVMConnector(_tvm);
    }

    
//*************************************************
//***This needs to propagate to the GUI some how...
//*************************************************
//    public void vmStopped(TargetVMConnector _tvm) {     
//        System.out.println("TargetVMManager::VM Connection["+_tvm.getAddress()+"] stopped...");
//    }
 
 
     public synchronized void addVMMgrListener(TargetVMMgrListener _l) {
        com.objs.surveyor.probemeister.Log.out.finer("---------->Adding TargetVMMgrListener");
        if (_l == null) return;
        if (vmMgrListeners==null) {
            vmMgrListeners = new TargetVMMgrListener[1];
            vmMgrListeners[0]=_l;
        } else { //grow list
            TargetVMMgrListener temp[] = new TargetVMMgrListener[vmMgrListeners.length+1];
            int i=0;
            for (; i<vmMgrListeners.length; i++){
                temp[i]=vmMgrListeners[i];
            }
            temp[i]=_l;
            vmMgrListeners = temp;
        }
    }

    public synchronized void removeVMMgrListener(TargetVMMgrListener _l) {
        if (_l == null) return;
        if (vmMgrListeners==null) {return;} 
        else { //shrink list IF listener found
            boolean listenerFound = false;
            TargetVMMgrListener temp[] = new TargetVMMgrListener[vmMgrListeners.length-1];
            for (int i=0, j=0; i<vmMgrListeners.length; i++, j++){
                if (j == vmMgrListeners.length-1 && !listenerFound) break; //at end, not there, exit
                if (vmMgrListeners[j] == _l) {j++; listenerFound=true; continue;} //don't copy this over (essentially, remove it)
                temp[i]=vmMgrListeners[j];
            }
            if (listenerFound) vmMgrListeners = temp;
        }
    }

    static final int NEWVMWAITING = 0;
    
    void emitVMEvent(int _evt) {

        if (vmMgrListeners == null) {
            com.objs.surveyor.probemeister.Log.out.warning("---------->No vm mgr listeners...");
            return;        
        }
        com.objs.surveyor.probemeister.Log.out.finest("---------->Handling vm mgr listener event...");/*
        switch (_evt) {
            case NEWVMWAITING: 
                System.out.println("---------->New VM Waiting Event");
                for (int i=0; i< vmMgrListeners.length; i++) 
                    vmMgrListeners[i].newVMWaiting(new TargetVMMgrEvent(this));
                break;
                
            default: ;
*/            
        }        
    
    //**************************
    //TargetVMMgrListener Events
    //**************************
    private void newVMWaiting(VirtualMachine _vm) {
        if (vmMgrListeners == null) return;
        com.objs.surveyor.probemeister.Log.out.finest("---------->New VM Waiting Event");
        for (int i=0; i< vmMgrListeners.length; i++) 
            vmMgrListeners[i].newVMWaiting(new VMData("Unidentified", "Unknown", _vm));        
    }
    private void announceNewVM(TargetVMConnector _tvm) {
        if (vmMgrListeners == null) return;
        com.objs.surveyor.probemeister.Log.out.finest("---------->New VM Added Event");
        for (int i=0; i< vmMgrListeners.length; i++) 
            vmMgrListeners[i].newTargetVM(new VMTVMData(_tvm, _tvm.getName(), _tvm.getAddress()));        
    }
 
    
    class ListenForVMs extends Thread {
    /* listen for connection from target vm */
    
        TargetVMManager mgr;
        String portNum;
        com.sun.jdi.connect.ListeningConnector listener =null;
        java.util.Map connectorArgs = null;
        
        public ListenForVMs(TargetVMManager _mgr, String _portNum) {
            mgr = _mgr;
            portNum = _portNum;
        }
    
        public void run() {
            
            //Get Listening Connectors... look for socket connector
            java.util.List lcs = Bootstrap.virtualMachineManager().listeningConnectors();
            com.objs.surveyor.probemeister.Log.out.finer("Listening - # of listener connectors = "+lcs.size());
            for (int i=0;i<lcs.size();i++) {
                com.sun.jdi.connect.ListeningConnector l = (com.sun.jdi.connect.ListeningConnector)lcs.get(1); 
                if (l.transport().toString().equals("dt_socket")) { // we found one
                    listener = l;
                }
            }
            if (listener == null) {
                com.objs.surveyor.probemeister.Log.out.severe("Cannot find listening connector. Cannot accept connections.");
                return;
            }
            
            connectorArgs = listener.defaultArguments();
            try {
                Connector.Argument ca = (Connector.Argument)connectorArgs.get("port");
                if (ca != null) // then let's change the default port #
                    ca.setValue(portNum);
                else {
                    com.objs.surveyor.probemeister.Log.out.warning("Cannot modify default port number.");
                }
                    
                String can = listener.supportsMultipleConnections()? "CAN" : "CANNOT";
                com.objs.surveyor.probemeister.Log.out.finest("Listening - "+can+" listen to multiple connections...");
                String retAddress = listener.startListening(connectorArgs);
                com.objs.surveyor.probemeister.Log.out.info("Listening at address: " + retAddress);

            } catch (java.io.IOException ioe) {
                //ioe.printStackTrace();
                com.objs.surveyor.probemeister.Log.out.severe("\n Error -- address may be in use, cannot listen for applications.");
                return;
            } catch (com.sun.jdi.connect.IllegalConnectorArgumentsException icae) {
                //icae.printStackTrace();
                com.objs.surveyor.probemeister.Log.out.severe("\n Internal debugger error. Illegal Arguments, cannot listen for applications.");
                return;
            }
            while (true) { //keep listening
                try {
                    VirtualMachine vm = listener.accept(connectorArgs);
                    com.objs.surveyor.probemeister.Log.out.info("*** New connection accepted...");
                    mgr.newVMWaiting(vm);
                } catch (java.io.IOException ioe) {
                    //ioe.printStackTrace();
                    com.objs.surveyor.probemeister.Log.out.severe("\n Accepting connection: Unable to attach to target VM.");
                    return;
                } catch (com.sun.jdi.connect.IllegalConnectorArgumentsException icae) {
                    //icae.printStackTrace();
                    com.objs.surveyor.probemeister.Log.out.severe("\n Accepting connection: Internal debugger error.");
                    return;
                } catch (Exception e) {
                    com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.SEVERE,"Accepting connection: Exception occurred.",e);
                    return;
                }
            }
        }

        
        //attempts to clear control of port on app termination
        public void finalize() { 
            try {
                if (listener != null)
                    listener.stopListening(connectorArgs);
            } catch (Exception e) {}
        }
        
    }
    
}