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
public interface GaugeRequestLocationInterface extends Remote {
 
    public String getVMName() throws RemoteException;
    public String getClassName() throws RemoteException;
    public String getMethodName() throws RemoteException; 
    /* Returns the location where the probe is to be inserted
     * 0 denotes at the start of the method, -1 at the end
     * and any valid positive # is the instruction # where the
     * probe will be inserted
     */
    public int getLocation() throws RemoteException;    
    //public String toString();
    public void set(String _vm, String _cls, String _meth) throws RemoteException;
    public void set(String _vm, String _cls, String _meth, int location) throws RemoteException;

    public void setVMName(String _vm) throws RemoteException;
    public void setClassName(String _cls) throws RemoteException;
    public void setMethodName(String _meth) throws RemoteException;
    public void setLocation(int _loc) throws RemoteException;


}
