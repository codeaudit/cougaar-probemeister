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

public class GaugeRequestLocation extends UnicastRemoteObject
    implements GaugeRequestLocationInterface {

    String vm;
    String clsName;
    String method;
    //probe insertion point
    int location; // 0 = at start;   -1 = at end;   <x> = at instruction # 
    
    public GaugeRequestLocation(String _vm, String _cls, String _meth) 
    throws RemoteException {
        this(_vm, _cls, _meth, 0);
    }

    public GaugeRequestLocation(String _vm, String _cls, String _meth, int _loc) 
    throws RemoteException {
        vm = _vm;
        clsName = _cls;
        method = _meth;
        location = _loc;
    }

    /* Needed all because we cannot override the toString() method.
     * Thus, when we get a remote GaugeRequestLocationInterface object
     * we simply make a local copy that can override the toString()
     * method - needed for proper disply in gui JLists...
     */
    public GaugeRequestLocation(GaugeRequestLocationInterface _grli) 
    throws RemoteException {
        vm = _grli.getVMName();
        clsName = _grli.getClassName();
        method = _grli.getMethodName();
        location = _grli.getLocation();
    }


    public String getVMName() throws RemoteException  { return vm; }
    public String getClassName() throws RemoteException { return clsName; }
    public String getMethodName() throws RemoteException { return method; }
    public int getLocation() throws RemoteException  { return location; }

    public void setVMName(String _vm) throws RemoteException  { vm = _vm; }
    public void setClassName(String _cls) throws RemoteException { clsName = _cls; }
    public void setMethodName(String _meth) throws RemoteException { method = _meth; }
    public void setLocation(int _loc) throws RemoteException  { location = _loc; }

    
    
    public void set(String _vm, String _cls, String _meth) throws RemoteException {
        set(_vm, _cls, _meth, 0);
    }        
    public void set(String _vm, String _cls, String _meth, int _loc) throws RemoteException {
        vm = _vm;
        clsName = _cls;
        method = _meth;
        location = _loc;
    }        
    public String toString() {return clsName + " : " + method+":"+location; }
}