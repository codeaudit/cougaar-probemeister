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
 * @(#)EventHandler.java	1.31 00/02/02
 *
 * Copyright 1998-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
/*
 * Copyright (c) 1997-1999 by Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 * 
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

//package com.sun.tools.example.debug.tty;
package com.objs.surveyor.probemeister.event;

import com.objs.surveyor.probemeister.*;
import com.objs.surveyor.probemeister.probe.ProbeInterface;
import com.objs.surveyor.probemeister.TargetVMConnector;
import com.objs.surveyor.probemeister.bytecoder.CallMethodByNameProbeType;
import com.objs.surveyor.probemeister.bytecoder.BytecodeLocation;

import com.objs.surveyor.probemeister.instrumentation.*;


import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.EventRequest;

import java.io.PrintStream;
import java.util.StringTokenizer;
import java.util.Collection;
import java.util.Iterator;
import java.io.File;
import java.io.FileReader;

import org.xml.sax.InputSource;


public class EventHandler implements Runnable {

    String bkptFileName = (String)Globals.globals().get(GlobalVars.BREAKPOINT_CLASS_NAME);
    //EventNotifier notifier;
    Thread thread;
    volatile boolean connected = true;
    boolean completed = false;
    String shutdownMessage;
    boolean stopOnVMStart;
    ThreadInfoMgr threadInfoMgr = null;

    EventRequestManager erm = null;

    boolean vmHasStarted = false;
    ReferenceType mainClass = null; //set to the main class of the target VM's application
    Method mainMethod = null;
    com.sun.jdi.request.ClassPrepareRequest allClassPrepsRequest = null;
    com.sun.jdi.request.ClassPrepareRequest lookingForMainClassPrepRequest = null;

    //Breakpoint Variables ------------------------------------------------
    public com.sun.jdi.request.ClassPrepareRequest breakpointClassPrepRequest = null;
    public com.sun.jdi.request.BreakpointRequest breakpointerRequest = null;
    public com.sun.jdi.request.BreakpointRequest mainBreakpointRequest = null; 

    boolean firstBreakpointOccurred = false;
    boolean watchForBreakPoint = true;
    boolean breakpointClassInstalled = false;
    boolean breakpointProbeInstalled = false;
    boolean breakpointClassPrepared = false;
    ClassType breakpointClass = null;
//    String breakpointClassName = "com.objs.surveyor.probemeister.bytecoder.util.Breakpointer";
    String breakpointClassName = "OBJS_Breakpointer";
    BytecodeLocation mainMethodLocation = null;    

    //temp
        int count = 0;

    TargetVMConnector targetVM = null;

    ClassEventNotifier classNotifier = null;
    VMEventNotifier vmNotifier = null;
    MethodEventNotifier methodNotifier = null;
    ThreadEventNotifier threadNotifier = null;
    ExceptionEventNotifier exceptionNotifier = null;
    DebugEventNotifier debugNotifier = null;
    
    EventSet currentEventSet = null;
        
    public EventHandler(VMEventNotifier notifier) {
        
        com.objs.surveyor.probemeister.Log.out.fine("Event Handler started...");
        if (notifier != null) {
            setVMEventNotifier(notifier);
            targetVM = (TargetVMConnector) notifier;   
        }
        this.thread = new Thread(this, "event-handler"); 
        this.thread.start();
        threadInfoMgr = new ThreadInfoMgr(targetVM, this);

        //Suspend all threads between ClassPrepareRequests
        //so we can locate the Main() class & insert a breakpoint
        erm = targetVM.vm().eventRequestManager();     
        
        //Request to listen for all preps -- after we find main()
        allClassPrepsRequest = erm.createClassPrepareRequest();
        allClassPrepsRequest.setSuspendPolicy(EventRequest.SUSPEND_NONE);
    
        //At the start listen for the first non JDK class... this should be main()
        lookingForMainClassPrepRequest = erm.createClassPrepareRequest();
        lookingForMainClassPrepRequest.addClassExclusionFilter("com.sun.*");
        lookingForMainClassPrepRequest.addClassExclusionFilter("java*");
        lookingForMainClassPrepRequest.addClassExclusionFilter("sun*");
        lookingForMainClassPrepRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
        lookingForMainClassPrepRequest.enable();
        
        //set up a special ClassPrepareRequest to watch for preparation of the BreakPointer class
        breakpointClassPrepRequest = erm.createClassPrepareRequest();
        breakpointClassPrepRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
        breakpointClassPrepRequest.addClassFilter(breakpointClassName); 
        breakpointClassPrepRequest.enable();
        
    }

    //Methods to set the notifiers to be called when VM events occur.
    // ** Each notifier must control its own subscription to events **

    public void setClassEventNotifier(ClassEventNotifier n) { classNotifier = n; n.initNotifier();}
    public void setVMEventNotifier(VMEventNotifier n) { vmNotifier = n; n.initNotifier();}
    public void setMethodEventNotifier(MethodEventNotifier n) { methodNotifier = n; n.initNotifier();}
    public void setThreadEventNotifier(ThreadEventNotifier n) { threadNotifier = n; n.initNotifier();}
    public void setExceptionEventNotifier(ExceptionEventNotifier n) { exceptionNotifier = n; n.initNotifier();}
    public void setDebugEventNotifier(DebugEventNotifier n) { debugNotifier = n; n.initNotifier();}

    synchronized void shutdownEventHandler() {
        connected = false;  // force run() loop termination
        thread.interrupt();
        while (!completed) {
            try {wait();} catch (InterruptedException exc) {}
        }
    }
    

    public void run() { 
        
        //test
int x=0;
        
        if (targetVM.vm().canRequestVMDeathEvent() ) {
            com.sun.jdi.request.VMDeathRequest vmdr =  erm.createVMDeathRequest();
            vmdr.enable();
            //Core.vm().eventRequestManager().createVMDeathRequest().enable();
        }
        
        java.util.List tsRequests = targetVM.vm().eventRequestManager().threadStartRequests();
        int size = tsRequests.size();
        for (int i = 0; i<size; i++) {
            EventRequest er = (EventRequest) tsRequests.get(i);
            String str = er.toString();
            com.objs.surveyor.probemeister.Log.out.finest("ER#"+i+": "+str);   
        }
        
        EventQueue queue = targetVM.vm().eventQueue();
        while (connected) {
            try {
                EventSet eventSet = queue.remove();
                //System.out.println("Got Event Set, policy = " + eventSet.suspendPolicy());
                //System.out.println("EventRequest.SUSPEND_ALL = "+EventRequest.SUSPEND_ALL);
                EventIterator it = eventSet.eventIterator();
                boolean lastEvent = false;
                boolean suspended=false;
                Event event = null;
                boolean resumeIt = true;
                while (it.hasNext()) {
                    event = it.nextEvent();

                    lastEvent = it.hasNext();
                    suspended = (eventSet.suspendPolicy() == EventRequest.SUSPEND_ALL) ||
                            (eventSet.suspendPolicy() == EventRequest.SUSPEND_EVENT_THREAD);
                    this.targetVM.suspended(suspended);
                    ///resumeStoppedApp |= !handleEvent(event, suspended, lastEvent );
                    //handleEvent returns FALSE if app should not be resumed
                    resumeIt &= handleEvent(event, suspended, lastEvent );
                }

/*                if (eventSet.suspendPolicy() == EventRequest.SUSPEND_ALL)
                    System.out.println("EventHandler:: ALL THREADS WERE SUSPENDED...");
                else if (eventSet.suspendPolicy() == EventRequest.SUSPEND_EVENT_THREAD)
                    System.out.println("EventHandler:: ONLY EVENT THREAD WAS SUSPENDED...");
 */               
    
                
                if (suspended && resumeIt) {//if all handlers are done & don't need it to be                     
                                //suspended any longer (they should return TRUE), then resume it.
                    eventSet.resume();                 
                    com.objs.surveyor.probemeister.Log.out.fine("EventHandler:: RESUMED.");
                    this.targetVM.suspended(false);
/*                    
                  if (event instanceof BreakpointEvent) {                    
                    BreakpointEvent be = (BreakpointEvent)event;
                    if (be.thread().isAtBreakpoint())
                        System.out.println("EventHandler:: at breakpoint.");                    
                    else
                        System.out.println("EventHandler:: Not at breakpoint.");                    
                  }
*/                  
                    //printThreadStatus(true);                    
                }                        


//******************************************************************************
//Make sure that this variable is accurately maintained in ALL event handlers...
//******************************************************************************
//                if (targetVM.suspended()) { //if STILL suspended, throw a VMinterrupted event
               //This will happen only if handleEvent returns FALSE... this just let's us know.
               if (this.targetVM.allThreadsStopped()) {
                    setCurrentThread(eventSet);
//                    System.out.println("EventHandler:: app interrupted...");
//                    System.out.println("               event = "+event.getClass().getName()); 
               }
            } catch (InterruptedException exc) { 
                // Do nothing. Any changes will be seen at top of loop.
            } catch (VMDisconnectedException discExc) {
                handleDisconnectedException();
                break;
            }
        }
        synchronized (this) {
            completed = true;
            notifyAll();
        }
    }

    public void printThreadStatus(boolean _resume) {
        java.util.List threads = this.targetVM.vm().allThreads();
        ThreadReference tr;
        Iterator iter = threads.iterator();
        boolean allStopped = true;
        final int maxStatusStrLen = 25;
        final String spaces = "                                                  ";
        com.objs.surveyor.probemeister.Log.out.fine("EventHandler:: THREAD STATUS");
        while (iter.hasNext()) {
            tr = (ThreadReference)iter.next();
            //status
            int status = tr.status();
            String str="";
            switch (status) {
            case(ThreadReference.THREAD_STATUS_UNKNOWN):
                str = "THREAD_STATUS_UNKNOWN";
                break;
            case(ThreadReference.THREAD_STATUS_ZOMBIE):
                str = "THREAD_STATUS_ZOMBIE";
                break;
            case(ThreadReference.THREAD_STATUS_RUNNING):
                str = "THREAD_STATUS_RUNNING";
                break;
            case(ThreadReference.THREAD_STATUS_SLEEPING):
                str = "THREAD_STATUS_SLEEPING";
                break;
            case(ThreadReference.THREAD_STATUS_MONITOR):
                str = "THREAD_STATUS_MONITOR";
                break;
            case(ThreadReference.THREAD_STATUS_WAIT):
                str = "THREAD_STATUS_WAIT";
                break;
            case(ThreadReference.THREAD_STATUS_NOT_STARTED):
                str = "THREAD_STATUS_NOT_STARTED";
                break;
            default:
                str = "NONE";
                break;
            }
            String s = (tr.isSuspended() ? "-S-" : "");
            String b = (tr.isAtBreakpoint() ? "-B-" : "");
            String r = "";
            if (_resume) { //resume only if not at a breakpoint.
                if (tr.isSuspended() && !tr.isAtBreakpoint()) {
                    tr.resume();
                    r = "resumed";
                }
            }
            int trunc = (tr.name().length() > 29) ? 29 : tr.name().length();
            String o = (tr.name().substring(0,trunc-1));
            o += (spaces.substring(0, 30-o.length()));
            o += (str);
            o += (spaces.substring(0, (maxStatusStrLen+5)-str.length()));
            o += (s+b);                        
            o += (spaces.substring(0, (8)-(s.length()+b.length())));
            com.objs.surveyor.probemeister.Log.out.info(o+r);                        
        }
    }        
        
        
    private boolean handleEvent(Event event, boolean suspended, boolean lastEvent) {
        //notifier.receivedEvent(event);

        if (event instanceof ExceptionEvent) {
            return exceptionEvent(event, suspended, lastEvent);
        } else if (event instanceof BreakpointEvent) {            
            return breakpointEvent(event, suspended, lastEvent);
        } else if (event instanceof WatchpointEvent) {
            return fieldWatchEvent(event, suspended, lastEvent);
        } else if (event instanceof StepEvent) {
            return stepEvent(event, suspended, lastEvent);
        } else if (event instanceof MethodEntryEvent) {
            return methodEntryEvent(event, suspended, lastEvent);
        } else if (event instanceof MethodExitEvent) {
            return methodExitEvent(event, suspended, lastEvent);
        } else if (event instanceof ClassPrepareEvent) {
            return classPrepareEvent(event, suspended, lastEvent);
        } else if (event instanceof ClassUnloadEvent) {
            return classUnloadEvent(event, suspended, lastEvent);
        } else if (event instanceof ThreadStartEvent) {
            return threadStartEvent(event, suspended, lastEvent);
        } else if (event instanceof ThreadDeathEvent) {
            return threadDeathEvent(event, suspended, lastEvent);
        } else if (event instanceof VMStartEvent) {
            return vmStartEvent(event, suspended, lastEvent);
        } else {
            handleExitEvent(event);
            return true; //doesn't matter since resuming threads should be moot at this pt.
        }
    }

    private boolean vmDied = false;
    private boolean handleExitEvent(Event event) {
        if (event instanceof VMDeathEvent) {
            vmDied = true;
            vmDeathEvent(event);
        } else if (event instanceof VMDisconnectEvent) {
            connected = false;
            if (!vmDied) {
                vmDisconnectEvent(event);
            }
            //this.targetVM.shutdown(shutdownMessage); //NO - notified via vmIsconnectEvent()
        } else {
            com.objs.surveyor.probemeister.Log.out.severe("------------> EventHandler:: Unexpected Event Type: "+event.getClass().toString());
            Thread.currentThread().dumpStack();
            throw new InternalError("Unexpected event type");
        }
        return true; //doesn't matter what we return for exit events
    }

    synchronized public ReferenceType getMainClass() { return mainClass; }
    synchronized private void setMainClass(ReferenceType _rt) {
        if (true) { 
            //ODD*** It used to be that these classes had a classloader, now it's null!
            //_rt.classLoader() != null) { // then we MAY have found the main class
            com.objs.surveyor.probemeister.Log.out.finest("------------> EventHandler:: Setting main class");        
            java.util.List mainMeths = _rt.methodsByName("main", "([Ljava/lang/String;)V");
            if (mainMeths.size() > 0) { // then we've found the main class!      
                mainClass = _rt;        
                mainMethod = (Method)mainMeths.get(0);


                //Prep for Install of Breakpointer class
                if (installBreakpoint()) {
                    breakpointProbeInstalled = this.instrumentMainForBreakpointer();                    
                }
/*                
                //Apply configuration file. If 'targetClass' is null then set to start up class, IF global string is set
                
                String config = (String)Globals.globals().get(GlobalVars.DEPLOY_CONFIG_AT_STARTUP);
                if (config != null && config.length()>0) {
                    com.objs.surveyor.probemeister.Log.out.info("EventHandler:: Deploying configuration ("+config+") into main() class.");                        
                    try {
                        File f = new File(config);
                        if (!f.exists())
                            com.objs.surveyor.probemeister.Log.out.warning("EventHandler:: Looking for config file:"+f.getCanonicalPath());                        
                        else {    
                            InstrumentationRecordSet irs = InstrumentationRecorder.deserialize(new InputSource(new FileReader(f)));
                            if (irs.getSize()>0) {
                                for (int i=0; i<irs.getSize(); i++) {
                                    InstrumentationRecord ir = irs.getRecord(i);
                                    if (ir.getClassName()!=null && ir.getClassName()=="*STARTUP_CLASS*")
                                        ir.setClassName(mainClass.name());
                                }
                                InstrumentationRecorder.playConfiguration(this.targetVM, irs, null);
                            }
                        }
                    } catch (Exception e) {
                        com.objs.surveyor.probemeister.Log.out.warning("EventHandler:: Exception applying config to main"+e);                        
                    }
                }
 */               
                //Set so Breakpoint class can be installed & we can read the command line args
                //This MUST be done AFTER the config file mods to the class, o.w. the breakpt request will be deleted.
                setInitialBreakPoint(mainMeths);                
                
            }
        }         
    }
    
    
    
    
    
    private void setVMHasStarted() {
        this.vmHasStarted = true;
        com.objs.surveyor.probemeister.Log.out.fine("------------> EventHandler:: VM Has Started");        
////        genericClassPrepRequest.enable();
    }
    
    // Called by ClassPrepareEvent() when mainClass != null
    /* This method is called once the Main() class is found so a 
     * breakpoint can be set.
     */
    private void setInitialBreakPoint(java.util.List _mainMeths) {
        Location loc = mainMethod.location();

        com.objs.surveyor.probemeister.Log.out.fine("------------> EventHandler:: creating breakpt request at: "+loc);
        watchForBreakPoint=true;
        mainBreakpointRequest= erm.createBreakpointRequest(loc); //create breakpoint in main()            
        mainBreakpointRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);            
        mainBreakpointRequest.enable();


        com.sun.jdi.request.BreakpointRequest bpr = erm.createBreakpointRequest(loc); //create breakpoint in main()            
        bpr.setSuspendPolicy(EventRequest.SUSPEND_ALL);            
//        bpr.enable();
        
        
        //Insert probe to call Breakpointer. Must be done before targetVM is resumed
        //yet after mainClass is found.
        //instrumentMainForBreakpointer();
        
    }
    
       
    /* Sets a breakpoint at the specified location */
    public void setBreakpoint(Location _loc) {
        com.objs.surveyor.probemeister.Log.out.fine("------------> EventHandler:: creating breakpt request at: "+_loc);
        com.sun.jdi.request.BreakpointRequest pt = erm.createBreakpointRequest(_loc); //create breakpoint in main()            
        pt.setSuspendPolicy(EventRequest.SUSPEND_ALL);            
        pt.enable();
        //watchForBreakPoint=true;        
    }
    

    synchronized void handleDisconnectedException() {
        /*
         * A VMDisconnectedException has happened while dealing with
         * another event. We need to flush the event queue, dealing only
         * with exit events (VMDeath, VMDisconnect) so that we terminate
         * correctly.
         */
        EventQueue queue = this.targetVM.vm().eventQueue();
        while (connected) {
            try {
                EventSet eventSet = queue.remove();
                EventIterator iter = eventSet.eventIterator();
                while (iter.hasNext()) {
                    Event event = (Event)iter.next();
                    if ((event instanceof VMDeathEvent) || (event instanceof VMDisconnectEvent)) 
                        handleExitEvent(event);
                }
            } catch (InterruptedException exc) {
                // ignore
            }
        }
this.targetVM.vmDisconnectEvent(null);
    }

    private ThreadReference eventThread(Event event) {
        if (event instanceof ClassPrepareEvent) {
            return ((ClassPrepareEvent)event).thread();
        } else if (event instanceof LocatableEvent) {
            return ((LocatableEvent)event).thread();
        } else if (event instanceof ThreadStartEvent) {
            return ((ThreadStartEvent)event).thread();
        } else if (event instanceof ThreadDeathEvent) {
            return ((ThreadDeathEvent)event).thread();
        } else if (event instanceof VMStartEvent) {
            return ((VMStartEvent)event).thread();
        } else {
            return null;
        }
    }

    private void setCurrentThread(EventSet set) {
        ThreadReference thread;
        if (set.size() > 0) {
            /*
             * If any event in the set has a thread associated with it, 
             * they all will, so just grab the first one. 
             */
            Event event = (Event)set.iterator().next(); // Is there a better way?
            thread = eventThread(event);
        } else {
            thread = null;
        }
        setCurrentThread(thread);
    }

    private void setCurrentThread(ThreadReference thread) {
        threadInfoMgr.invalidateAll();
        threadInfoMgr.setCurrentThread(thread); 
    }



    // ***************
    // COVERAGE EVENTS
    // ***************

    private boolean methodEntryEvent(Event event, boolean suspended, boolean lastEvent)  {
        MethodEntryEvent me = (MethodEntryEvent)event;
        if (methodNotifier != null) 
            methodNotifier.methodEntryEvent(me, suspended, lastEvent);
        return true;
    }

    private boolean methodExitEvent(Event event, boolean suspended, boolean lastEvent)  {
        MethodExitEvent me = (MethodExitEvent)event;
        if (methodNotifier != null) 
            methodNotifier.methodExitEvent(me, suspended, lastEvent);
        return true;
    }


    // ************
    // DEBUG EVENTS
    // ************

    private boolean fieldWatchEvent(Event event, boolean suspended, boolean lastEvent)  {
        WatchpointEvent fwe = (WatchpointEvent)event;
        if (debugNotifier != null) 
            debugNotifier.fieldWatchEvent(fwe, suspended, lastEvent);
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////

    private boolean breakpointEvent(Event event, boolean suspended, boolean lastEvent)  {
        BreakpointEvent be = (BreakpointEvent)event;
        
        
        firstBreakpointOccurred = true;
        
        //System.out.println("breakpoint event location = "+be.location());        

        if (!breakpointClassInstalled && installBreakpoint()) {
            //Migrate Breakpointer class to Target VM...           
            if (this.getMainClass()!= null) { //we cannot proceed
                try {
                    java.io.InputStream bkptFileIS = this.getClass().getResourceAsStream("/"+bkptFileName);
                    com.objs.surveyor.probemeister.Log.out.fine("breakpoint management===> Looking for file in: "+bkptFileName);        
                    breakpointClass = (ClassType)this.targetVM.exportClassToTargetVM((ClassType)this.getMainClass(), be.thread(), bkptFileIS, breakpointClassName);
                    com.objs.surveyor.probemeister.Log.out.fine("breakpoint management===> Successfully installed breakpoint class.");        
                    breakpointClassInstalled = true; 
                    
                    mainBreakpointRequest.disable();
                    
                    //instrumentMainForBreakpointer(); //must instrument BEFORE this method runs!
                } catch (DuplicateClassException e) { //This should be OK
                    com.objs.surveyor.probemeister.Log.out.warning("breakpoint management===> Could not install class. Class already exists in target VM. Should be OK.");        
                } catch (Exception e) { //THis is a breakpointer stopper...
                    com.objs.surveyor.probemeister.Log.out.warning("breakpoint management===> Could not install class. Exception was: \n"+e);        
                    e.printStackTrace();
                }
            } else
                com.objs.surveyor.probemeister.Log.out.warning("breakpoint management===> Could not install breakpoint class. No mainClass identified");                    
        } 
        //TEST - this code would keep the breakpointer thread interrupted.
        //else {
        //    return false;                        
        // }
            
        
        //Announce breakpoint to all listeners, so updates can be applied if desired
        //In theory we should be able to get the two items we need for applying class
        //updates ( the classType, and the thread reference) from the breakpoint event
        //via location().declaringType, and thread().
        boolean resumeIt = this.targetVM.emitVMEvent(TargetVMConnector.VMBREAKPOINT , be);
        
        //Should this be AFTER the thread has already been resumed above? Probably not.
        if (debugNotifier != null) 
            return debugNotifier.breakpointEvent(be, suspended, lastEvent) && resumeIt;
        else
            return (true && resumeIt);
    }
    
    //How breakpointing is installed:
    //It can only occur if we can insert a breakpoint into a method to stop the target VM
    //In general, the only chance we have is in main(), which we grab when the VM is attached.
    //The target VM must have been started with suspend=y. Since main() will only run once,
    //we need to insert an artifical method whose behavior we know & can count on/control
    //-- we then insert a breakpoint request on this method, so we can get a breakpoint
    //whenever we need to migrate a class or redefine one. We use a new class called
    //Breakpointer to do this. It is a thread, and simply sleeps(). We set the breakpoint
    //on the sleep() method.
    //
    //Once we find main(), we:
    //1. Insert probe in main() that will invoke Breakpointer so it gets referenced/loaded
    //2. Define breakpoint in Target VM at main(), offset 0. Resume. Breakpoint is hit.
    //    This is done in connection with ClassPrepareEvent(), which first sees main()
    //4. Transfer Breakpointer class to the target VM (can ONLY happen at a breakpoint)
    //5. Define ClassPrepareRequest & wait for Breakpointer to be prepared() by the target VM
    //6. Once Breakpointer is loaded, define Breakpoint request on the breakpoint() method, byteoffset 0
    //7. enable request or disable to control breakpointing in target VM.
    
    /*
     * After the Breakpointer class is migrated the JVM won't apparently prepare
     * it until it's needed. So we simply install a probe to reference/init 
     * Breakpointer at the start of main().
     */
    public boolean instrumentMainForBreakpointer() {
        

        if (mainMethod == null || getMainClass() == null) return false;
        
        CallMethodByNameProbeType cmpt = (CallMethodByNameProbeType)CallMethodByNameProbeType.getStub();
        java.util.Hashtable params = new java.util.Hashtable(2);
        params.put("className", this.breakpointClassName);
        params.put("methodName", "init");
        mainMethodLocation = new BytecodeLocation(this.targetVM.getClassMgr(), (ClassType)getMainClass(), mainMethod, 0);

        ProbeInterface pi = cmpt.generateProbe("SystemInstalled", "Installed to activate Breakpointer class.", 
                    mainMethodLocation, params);
        if (pi == null) return false;                    
        com.objs.surveyor.probemeister.Log.out.fine("GENERATED Breakpointer probe...");
        return true;
    }
    
    Boolean installBreakpoint = null; //default
    private boolean installBreakpoint() {
        if (installBreakpoint == null){}
            installBreakpoint = (Boolean)Globals.globals().get(GlobalVars.INSTALL_BREAKPOINT_BOOLEAN);
        return installBreakpoint.booleanValue();
    }

    int breakpointCount = 0;
    /* Controls breakpointing in the target VM. Returns FALSE if Breakpointer not operable.
     * Uses a basic count flag & disables breakpoints when the count reaches 0;
     */
    public boolean enableBreakpoint(boolean _b) {
        
        if (!canEnableBreakpoint()) return false;
        
        try {
            if (_b) {
                breakpointerRequest.enable();
                breakpointCount++;
            }
            else {
                if (--breakpointCount == 0)
                    breakpointerRequest.disable();
            }
        } catch (com.sun.jdi.InternalException ie) {
            //We assume it is a JDWP Error 41 - cannot find breakpoint event - it
            //was thrown away by the JDK(?). SO, create a new one and proceed...
            //    com.objs.surveyor.probemeister.Log.out.severe("enableBreakpoint() exception "+ ie);                
            
            breakpointerRequest = erm.createBreakpointRequest(breakpointRequestLocation);
            breakpointerRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD); //ALL);
            //Exception should NOT happen again
            
            try {
            if (_b) {
                breakpointerRequest.enable();
                breakpointCount++;
            }
            else {
                if (--breakpointCount == 0)
                    breakpointerRequest.disable();
            }
            } catch (Exception e) {
                com.objs.surveyor.probemeister.Log.out.severe("enableBreakpoint() exception occurred twice: "+ e);                
                return false;
            }
        }
        
        return true;
    }

    /* Returns FALSE if Breakpointer not operable. O.w. returns TRUE.
     */
    public boolean canEnableBreakpoint() {
        return (breakpointProbeInstalled && breakpointClassInstalled);
    }


    Location breakpointRequestLocation = null ;
    /* Called once the Breakpointer class has been prepared... from the ClassPreparedEvent method.
        * It defines a watch point (breakpoint request) to facilitate breakpoint control
        */
    private void prepareBreakpointerRequest(ReferenceType _rt) {

        //Set up breakpoint request
        try {
            java.util.List bkptMeths = _rt.methodsByName("breakpoint", "()V");
            if (bkptMeths.size() > 0) { //then we found it.
                Method bkptMethod = (Method)bkptMeths.get(0);
                breakpointRequestLocation = bkptMethod.location();
                breakpointerRequest = erm.createBreakpointRequest(breakpointRequestLocation);
                breakpointerRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD); //ALL);
                //enableBreakpoint(true);
                breakpointClassPrepared = true;
                //System.out.println("breakpoint management===> Breakpoint Request enabled");        
            }
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.warning("breakpoint management===> Could not enable breakpoint request. Exception was: \n"+e);        
            e.printStackTrace();
        }         
    }

    ////////////////////////////////////////////////////////////////////////////////
    
    private boolean stepEvent(Event event, boolean suspended, boolean lastEvent)  {
        StepEvent se = (StepEvent)event;
        if (debugNotifier != null) 
            debugNotifier.stepEvent(se, suspended, lastEvent);
        return true;
    }

    // ************
    // CLASS EVENTS
    // ************
  
    //Listen from the start, ow we might miss the main() class.
//    public void startListeningForNewClasses() {
//        allClassPrepsRequest.enable();
//    }
    
    /*
     * We always listen for new class prepare events so we can let the gui know
     * that there are more classes available to probe. Listen until we find
     * the main class, then disable the request.
     *
     * **The current filter only listens for non-Sun/JDK classes**
     */
    private boolean classPrepareEvent(Event event, boolean suspended, boolean lastEvent)  {
        ClassPrepareEvent cle = (ClassPrepareEvent)event;
              
        //System.out.println("ClassPrepareEvent for "+ cle.referenceType().name());              
        //Let the connector know that a new class has been loaded
        //if (this.targetVM.hasVMStartedRunning()) 
        this.targetVM.newClassLoaded(cle.referenceType());

        //Grab the main class -- only possible if the VM has started with suspend=y
        if (vmHasStarted) {
            if (getMainClass() == null) {
                com.objs.surveyor.probemeister.Log.out.fine("VM has started, looking for main()");              
                setMainClass(cle.referenceType());                
                //Now modify the request so it doesn't suspend any threads anymore
                                
                if (getMainClass() != null) { //we found main, so listen but no need to interrupt anymore
                    lookingForMainClassPrepRequest.disable(); // turn off
                    //Listen for ALL classes so we can update the gui as classes are loaded
                    allClassPrepsRequest.enable(); //now listen for all classes
                }
            }
        }
//            } else { //look for the breakpointer class being prepared
            if (!breakpointClassPrepared && installBreakpoint()) {
                ReferenceType rt = cle.referenceType();
                if (rt.name().equals(breakpointClassName)) {                    
                    com.objs.surveyor.probemeister.Log.out.fine("==================Breakpointer class PREPARED");
                    breakpointClassPrepRequest.disable();
                    prepareBreakpointerRequest(rt);
                    
                } else ;
////                    System.out.println("==================PREPARED class is "+rt.name());                    
            }
//        }
        
        //Should we resume BEFORE we call this?? Probably not...
        if (classNotifier != null) {
             return classNotifier.classPrepareEvent(cle, suspended, lastEvent);
        }
        else
            return true; //resume thread

//        if (!Core.specList.resolve(cle)) {
//            Core.errorln("\nStopping due to deferred breakpoint errors.\n");
//            return true;
//        } else {
//            return false;
//        }
    }

    private boolean classUnloadEvent(Event event, boolean suspended, boolean lastEvent)  {
        ClassUnloadEvent cue = (ClassUnloadEvent)event;
        if (classNotifier != null) 
            classNotifier.classUnloadEvent(cue, suspended, lastEvent);
        return false;
    }

    // ****************
    // EXCEPTION EVENTS
    // ****************

    private boolean exceptionEvent(Event event, boolean suspended, boolean lastEvent) {
        ExceptionEvent ee = (ExceptionEvent)event;
        if (exceptionNotifier != null) 
            exceptionNotifier.exceptionEvent(ee, suspended, lastEvent);
        return true;
    }

    // *************
    // THREAD EVENTS
    // *************

    private boolean threadDeathEvent(Event event, boolean suspended, boolean lastEvent) {
        ThreadDeathEvent tde = (ThreadDeathEvent)event;
        threadInfoMgr.removeThread(tde.thread());
        if (threadNotifier != null) 
            threadNotifier.threadDeathEvent(tde, suspended, lastEvent);
        return false;
    }

    private boolean threadStartEvent(Event event, boolean suspended, boolean lastEvent) {
        ThreadStartEvent tse = (ThreadStartEvent)event;
        threadInfoMgr.addThread(tse.thread());
        if (threadNotifier != null) 
            threadNotifier.threadStartEvent(tse, suspended, lastEvent);
        return false;
    }

    // *********
    // VM EVENTS
    // *********
    /* Occurs when VM starts with suspend=y, otherwise this event is not emitted
     * by the VM.
     */
    private boolean vmStartEvent(Event event, boolean suspended, boolean lastEvent)  {
        VMStartEvent se = (VMStartEvent)event;     
        setVMHasStarted();
        return vmNotifier.vmStartEvent(se, suspended, lastEvent);
    }

    public boolean vmDeathEvent(Event event) {
        //shutdownMessage = "\nThe application exited";
        vmNotifier.vmDeathEvent((VMDeathEvent)event);
        shutdownEventHandler();
        return true;
    }

    public boolean vmDisconnectEvent(Event event) {
        //shutdownMessage = "\nThe application has been disconnected";
        vmNotifier.vmDisconnectEvent((VMDisconnectEvent)event);
        shutdownEventHandler();    
        return true;
    }
}
