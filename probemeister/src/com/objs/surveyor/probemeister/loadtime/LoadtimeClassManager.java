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

package com.objs.surveyor.probemeister.loadtime;

import com.objs.surveyor.probemeister.*;
import com.objs.surveyor.probemeister.event.ClassEventNotifier;
import com.objs.surveyor.probemeister.instrumentation.*;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
//import com.sun.jdi.connect.*;
import java.util.List;
import java.util.Vector;
import java.util.HashSet;
import java.util.Iterator;

public class LoadtimeClassManager implements ClassEventNotifier {

    static boolean debug = true;

    ClassPrepareRequest cpr = null;
    ClassUnloadRequest  cur = null;
    TargetVMConnector targetVM;
    
    Vector queue;
    
    /* Indicator to tell when first event comes thru */ 
    boolean firstRun = true;

    public LoadtimeClassManager(TargetVMConnector _tvmc) {

        targetVM = _tvmc;
        //Notify handler to forward Class events to this mgr
        targetVM.handler().setClassEventNotifier(this);
        queue = new Vector(10);

    }


    //ClassEventNotifier Interface methods
    //* Prints out the class and its methods //
    public boolean classPrepareEvent(ClassPrepareEvent e, boolean suspended, boolean lastEvent){ 

        //See if there's anything to process
        if (queue.size() == 0) return true; //No, so return.
        
        ReferenceType type = e.referenceType();        
        boolean appliedProbe = handleClassLoadedEvent(type);
        
        //if (appliedProbe) {
        //    int sp = cpr.suspendPolicy();
        //    String stat = cpr.isEnabled() ? "Enabled" : "Disabled" ;
        //    targetVM.handler().printThreadStatus(false);
         
        //    com.objs.surveyor.probemeister.Log.out.info("policy = "+sp+"  stat="+stat);
        //    return false;
        //}
        //    return false; //true; //resume thread.
        //else
            return true;
    }
    
    
    public boolean classUnloadEvent(ClassUnloadEvent e, boolean suspended, boolean lastEvent){ 
        String clsname = e.className();
        com.objs.surveyor.probemeister.Log.out.fine("*** ClassUnloadEvent ***    Classname = "+ clsname); 
        return true; //resume thread.
    }

    /*
     * Sets up to receive class prepare events from VM.
     * Call activateManager(true) to control modification 
     */
    public void initNotifier() {

        ClassPrepareRequest c = createClassPrepareRequest();
        c.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        
        //Add just as a ping that this is actually working...
        c.addClassFilter("java.io.File");
        setClassPrepareRequest(c);
        
    }

    // ** End ClassEventNotifier methods

    /*
     * Turns on & off loadtime class modification. Automatic modification of classes 
     * at loadtime requires that a configuration set be provided which specifies the 
     * classes to modify and the probes to insert. Nothing occurs if a configuration
     * set has not yet been specified.
     */
    public void activateManager(boolean _modifyOn) {
        if (cpr != null) {
            if (_modifyOn)        
                cpr.enable();
            else
                cpr.disable();
        }
    }

    /*
     * This method returns a ClassPrepareRequest, which is then customized via
     * its own interface. Then it must be submitted via setClassPrepareRequest()
     * to register it. To activate it, call activateManager(true).
     */
    protected ClassPrepareRequest createClassPrepareRequest() {
        return targetVM.vm().eventRequestManager().createClassPrepareRequest();
    }
    
    protected void setClassPrepareRequest(ClassPrepareRequest _cpr) {
        cpr = _cpr;
        //Enable(activate) the request - ** Done via activateManager()
        //cpr.setSuspendPolicy(suspendPolicy); //EventRequest.SUSPEND_ALL);
    }


    /*
     * The list passed in contains the set of probes to (possibly) install & activate
     * AS THE CLASSES ARE BEING LOADED. This method MUST be called before the
     * probed application starts otherwise the LoadtimeClassManager will not see the
     * classes being loaded.
     *
     * The configuration set describes the classes to modify and the probes to insert.
     * After calling this method, call activateManager(true) to turn on loadtime
     * modification.
     *
     * To modify the classes after they have been loaded use the RuntimeClassManager
     */
    public void addConfigurationSetToApply(InstrumentationRecordSet _set) {

        //extract class names to produce filter list
        HashSet cnames = new HashSet();
        int i = _set.getSize();
        
        //Add the class names to the hash list.
        for (int x = 0; x<i; x++) {        
            cnames.add(_set.getRecord(x).getClassName());     
            addRecordToApply(_set.getRecord(x), false);
        }
        
        //Now grab the list (no duplicates now since we used a Hash Map)
        Iterator iter = cnames.iterator();
        while (iter.hasNext())
            addFilter((String)iter.next());
        
    }

    public void addRecordToApply(InstrumentationRecord _rec) {
        addRecordToApply(_rec, true);   
    }

    public void addRecordToApply(InstrumentationRecord _rec, boolean _addFilter) {
        
        if (_addFilter)
            addFilter(_rec.getClassName());
        
        //Add config set to list
        queue.addElement(_rec);
    }

    /* This class "auto" enables the event request when new filters are added
     */
    private void addFilter(String _filter) {        
        activateManager(false);
        cpr.addClassFilter(_filter);
        com.objs.surveyor.probemeister.Log.out.info("*** LoadtimeClassManager : watching for "+ _filter); 
        activateManager(true);
    }


    /*
     * Handles modification of a class. First, checks to see if there
     * is one or more probes that need to be installed, o.w. does nothing.
     *
     */
    boolean handleClassLoadedEvent(ReferenceType cType) {

        boolean appliedProbe = false; 
        //Get class name
        String cName = cType.name();
        
        //if (cName.endsWith("File"))
        //    System.out.println("***\n"+cName+" Class loaded\n***");

        //See if class name is in the configuration set (maybe more than once). If so,
        //get probe information.
        
        Vector appliedRecs = new Vector();
        int qsize = queue.size();
        for (int i=0; i<qsize; i++) {
            InstrumentationRecord ir = (InstrumentationRecord)queue.get(i);
            if (ir != null && ir.getClassName().equals(cName)) { //then apply & remove
                ir.reapplyAction(targetVM);
                com.objs.surveyor.probemeister.Log.out.info("*** LoadtimeClassManager applied probe to = "+ cName); 
                appliedRecs.addElement(ir);
                appliedProbe = true;
            }
        }
        
        //Now remove the applied records from the queue
        if (appliedRecs.size()>0)
            queue.removeAll(appliedRecs);     
            
        if (queue.size() == 0)
            activateManager(false); //turn off class prepare request

        return appliedProbe;
    }

}
