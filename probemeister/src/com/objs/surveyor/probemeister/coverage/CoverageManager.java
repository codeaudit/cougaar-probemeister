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

package com.objs.surveyor.probemeister.coverage;

import com.objs.surveyor.probemeister.event.MethodEventNotifier;
import com.objs.surveyor.probemeister.*;

import com.sun.jdi.Method;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
//import com.sun.jdi.connect.*;

import java.util.*;
import java.io.*;


public class CoverageManager implements MethodEventNotifier {


    static boolean debug = true;

    MethodEntryRequest entry = null;
    MethodExitRequest  exit = null;
    TargetVMConnector tvmc;
    
    public CoverageManager(TargetVMConnector _tvmc) {
        
        //Notify handler to forward Method events to this mgr
        _tvmc.handler().setMethodEventNotifier(this);
        tvmc= _tvmc;
    }

    //*************************************************
    // *** MethodEventNotifier Interface methods ***

    public void methodEntryEvent(MethodEntryEvent e, boolean suspended, boolean lastEvent) {

        Method meth = e.method();
        com.objs.surveyor.probemeister.Log.out.entering(meth.name(), meth.declaringType().name());
        handleMethodEntryEvent(meth);
        
    }
    
    public void methodExitEvent(MethodExitEvent e, boolean suspended, boolean lastEvent) {

        Method meth = e.method();
        com.objs.surveyor.probemeister.Log.out.exiting(meth.name(), meth.declaringType().name());
        handleMethodExitEvent(meth);

    }
    
    public void initNotifier() {
    }

    //*********************************************************
 
    /*
     * Turns on & off coverage manager. The manager is used to invoke
     * probes when a method is entered and/or exited. It requires that
     * a configuration set be provided which specifies the classes to 
     * watch for and the probes to invoke. Nothing occurs if a configuration
     * set has not yet been specified.
     */
    public void activateManager(boolean modifyOn) {
        if (entry != null) {
            if (modifyOn)        
                entry.enable();
            else
                entry.disable();
        }
        if (exit != null) {
            if (modifyOn)        
                exit.enable();
            else
                exit.disable();
        }
    }
    
    
    
    /*
     * This method returns a MethodEntryRequest, which is then customized via
     * its own interface. Then it must be submitted via setMethodEntryRequest()
     * to register it. To activate it, call activateManager(true).
     */
    protected MethodEntryRequest createMethodEntryRequest() {
        return tvmc.vm().eventRequestManager().createMethodEntryRequest();
    }
    
    protected void setMethodEntryRequest(MethodEntryRequest _entry) {
        entry = _entry;
        //entry.setSuspendPolicy(suspendPolicy); //EventRequest.SUSPEND_ALL);
    }

    /*
     * This method returns a MethodExitRequest, which is then customized via
     * its own interface. Then it must be submitted via setMethodExitRequest()
     * to register it. To activate it, call activateManager(true).
     */
    protected MethodExitRequest createMethodExitRequest() {
        return tvmc.vm().eventRequestManager().createMethodExitRequest();
    }
    
    protected void setMethodExitRequest(MethodExitRequest _exit) {
        exit = _exit;
        //exit.setSuspendPolicy(suspendPolicy); //EventRequest.SUSPEND_ALL);
    }
    

    /*
     * The configuration set describes the classes/methods watch list and the probes 
     * to invoke. No class modification takes place.
     *
     * After calling this method, call activateManager(true) to turn on coverage-based
     * modification.
     *
     */
    public void setConfigSet(List probesFromConfigSet) {
        
        //*** FILL IN ONCE THE STRUCTURE OF THE CONFIGURATION SET HAS BEEN
        //*** DEFINED.        
        
        // - create a MethodEntryRequest & MethodExitRequest
        MethodEntryRequest _entry = createMethodEntryRequest();
        MethodExitRequest  _exit  = createMethodExitRequest();

        // - examine classes in config set & produce a set of filters
        //   separating entry and exit

        // - add the filters to each set
        
        // - set MethodEntryRequest & MethodExitRequest
        setMethodExitRequest(_exit);
        setMethodEntryRequest(_entry);
    }


    /*
     * Handles invocation of probes on method entry as specified in config set. First, checks to 
     * see if there is one or more probes that need to be invoked, o.w. does nothing.
     *
     */
    void handleMethodEntryEvent(Method meth) {

        //Get method name
        String mName = meth.name();

        //See if method name is in the configuration set (maybe more than once). If so,
        //get probe information.
        
        //Iterate through invocation of probes, calling the methods to invoke them
                

    }

    /*
     * Handles invocation of probes on method exit as specified in config set. First, checks to 
     * see if there is one or more probes that need to be invoked, o.w. does nothing.
     *
     */
    void handleMethodExitEvent(Method meth) {

        //Get method name
        String mName = meth.name();

        //See if method name is in the configuration set (maybe more than once). If so,
        //get probe information.
        
        //Iterate through invocation of probes, calling the methods to invoke them
                

    }
    
    
}