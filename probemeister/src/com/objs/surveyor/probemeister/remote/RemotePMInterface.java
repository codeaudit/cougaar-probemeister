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


public interface RemotePMInterface extends Remote {
 
    /* string pairs composed of class name and method name & signature */    
    
    //May also need to accept ProbeType argument to limit the kind of probes displayed to the used.
    //Or more easily pop up window stating what kind of probe to use... eh...    
	//public void filterClassesByList(String _dataPanelName, String[][] _list) throws RemoteException;

    /* Returns list of active VMs using the assigned datapanel names */
    public String[] getDataPanelNames() throws RemoteException;
    
    /* Request ProbeMeister to attach to the specified application
     * Returns name of connection.
     */
    public String attachToJVM(String name, String addr, String port) throws RemoteException;

    /* invoked to submit a gauge deplyment request for the PM user to process */
    public String sendGaugeDeployRequest(GaugeRequestInterface gri) throws RemoteException;
    
}
