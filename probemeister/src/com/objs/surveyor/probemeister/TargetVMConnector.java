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

import com.objs.surveyor.probemeister.event.VMEventNotifier;
import com.objs.surveyor.probemeister.event.EventHandler;
import com.objs.surveyor.probemeister.bytecoder.TargetVMConnector_ClassMgr;
import com.objs.surveyor.probemeister.instrumentation.*;
import com.objs.surveyor.probemeister.probe.ProbePlugCatalog;
import com.objs.surveyor.probemeister.loadtime.LoadtimeClassManager;

import com.objs.probemeister.LoggerNames;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import com.sun.jdi.connect.*;

import java.util.*;
import java.io.*;
import java.io.FileReader;

import org.xml.sax.InputSource;


public class TargetVMConnector implements VMEventNotifier {

    public TargetVMConnector targetVM = null;
    public EventHandler handler = null;
    private InstrumentationRecorder recorder = null;
    private String name;
    private String address;
    private VMConnection connection;
    private boolean resumeVMOnStart = false; //when VM starts up, if suspend=y, the VM is suspended
    private boolean vmSuspended=false;
    private java.util.Vector vmClassesList=null;
    private TargetVMListener[] vmListeners=null;
    private String transport = null;
    private boolean reattach = false;
    private boolean vmStartedRunning = false; //set to true when vm moves into VMSTARTED_RUNNING state
    private boolean vmIsAClient = false; //true if target VM is created using ListeningConnector
    private VMData vmData = null; //used during initialization
    private boolean triedToGetCommandLineParams = false;
    private TargetVMCLParams vmParams = null;
    private ClassType sysClass = null;
    
    //contains list of logger names as defined by the user
    private LoggerNames logNames;
    
    private int lastState = -1; //keeps track of the last state this VM is in. Specifically,
                                //whether the last event was a VMSTARTED_INTERRUPTED event
    
    private TargetVMConnector_ClassMgr classMgr;
    
    Vector forceClassLoadList = new java.util.Vector();
    Hashtable forceClassLoadedList = new java.util.Hashtable();
    LoadtimeClassManager loadtimeMgr = null;
    public LoadtimeClassManager getLoadtimeManager() { return loadtimeMgr; }
    
    static {
       /* Start event handler
        * immediately, telling it (through arg 2) to stop on the 
        * VM start event.
        */
        //Core.core = new Core();
        //Core.handler = new EventHandler(Core.cor, true);
    }     


    //***********
    // MOVE TO OTHER CLASSES
    //***********

/*
    public void breakpointEvent(BreakpointEvent e){ System.out.println("event #8");}
    public void fieldWatchEvent(WatchpointEvent e){ System.out.println("event #9");}
    public void stepEvent(StepEvent e){ System.out.println("event #10");}
    public void exceptionEvent(ExceptionEvent e){ System.out.println("event #11");}
    public void methodEntryEvent(MethodEntryEvent e){ System.out.println("event #12");}
    public void methodExitEvent(MethodExitEvent e){ System.out.println("event #13");}

    public void receivedEvent(Event event){ System.out.println("event #15");}
*/    
    public EventHandler handler() {return handler;}
    public TargetVMConnector_ClassMgr getClassMgr() {return classMgr;}

    public TargetVMConnector(String _name, String _appAddr, String _transport, boolean _reattach) 
       {
        com.objs.surveyor.probemeister.Log.out.info("Initializing TargetVM connection...");
        vmIsAClient = false; //VM is a server, we're attaching to it.
        
        //Register shudown hook to safely terminate communication with TargetVM
        //should the user use a control-C on this appl.
        java.lang.Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
        
        name = _name; 
        targetVM = this;
        address = _appAddr;
        transport = _transport;
        reattach = _reattach;
        
        recorder = new InstrumentationRecorder(this);
        
      }

      public TargetVMConnector(VMData _vmd) {
        
        com.objs.surveyor.probemeister.Log.out.info("Adding Waiting TargetVM connection...");
        vmIsAClient = true; // the target VM is attaching to us.
        name = _vmd.getName(); 
        targetVM = this;
        address = _vmd.getAddress();
        vmData = _vmd;

        //Register shudown hook to safely terminate communication with TargetVM
        //should the user use a control-C on this appl.
        java.lang.Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));

        recorder = new InstrumentationRecorder(this);
        
      }
        
      void initVMConnection()  {
        
        
        if (vmIsAClient) { //we're the server, accept the connection
        
            //classMgr = new TargetVMConnector_ClassMgr(this);
            if (vmData != null && vmData.vm() != null) {
                this.connection = new VMConnection(vmData.vm());
            } else {
                emitVMEvent(VMDISCONNECTED, null);                     
                com.objs.surveyor.probemeister.Log.out.warning("TargetVMConnector:: connection failed to: "+address);
                return;
            }                
        }
        else { // we're the client, make the connection
        
            //Initiate connection to VM
            try {
                boolean connected = attachToApp(address, transport, reattach);
                if (!connected) { //
                    emitVMEvent(VMDISCONNECTED, null);                     
                    com.objs.surveyor.probemeister.Log.out.warning("TargetVMConnector:: connection failed to: "+address);
                    return;
                }
                
                if (!connection().isOpen()) {
                    emitVMEvent(VMDISCONNECTED, null);                     
                    com.objs.surveyor.probemeister.Log.out.warning("TargetVMConnector:: connection failed to: "+address);
                    return;
                }
            } catch (Exception e) {
                emitVMEvent(VMDISCONNECTED, null);                     
                com.objs.surveyor.probemeister.Log.out.warning("Exception in initConnection: \n");
                e.printStackTrace();
                return;
            }
        }

        classMgr = new TargetVMConnector_ClassMgr(this);
        emitVMEvent(VMCONNECTED, null);
        
        sysClass = getSystemClass();


        //Register for appropriate VM events from handler
        handler = new EventHandler(this); 
        
        //Create LoadtimeManager -- Can must be instantiated after the EventHandler
        loadtimeMgr = new LoadtimeClassManager(this);
        //handler.setClassEventNotifier(loadtimeMgr);
        //Apply config file before any classes are loaded
        applyConfigFile();
       
        //Assign TargetVMCLParams to hold params we get from the TargetVM
        vmParams = new TargetVMCLParams();

        //System.out.println("canUnrestrictedlyRedefineClasses() = "+ vm().canUnrestrictedlyRedefineClasses());
        
        if (allThreadsStopped()) { //we should see a VMStart event
            com.objs.surveyor.probemeister.Log.out.finest("..........ALL STOPPED");
        }
        else {//we won't see an event, issue a VMStart_Running event
            com.objs.surveyor.probemeister.Log.out.finest("..........VM RUNNING");
            emitVMEvent(VMSTARTED_RUNNING, null);            
        }
        
        logNames = new LoggerNames();
        
    }
    
    private void applyConfigFile() {

        //Apply configuration file. If 'targetClass' is null then set to start up class, IF global string is set

        String config = (String)Globals.globals().get(GlobalVars.DEPLOY_CONFIG_AT_STARTUP);
        if (config != null && config.length()>0) {
            com.objs.surveyor.probemeister.Log.out.info("TargetVMConnector:: Deploying configuration ("+config+") into main() class.");                        
            try {
                File f = new File(config);
                if (!f.exists())
                    com.objs.surveyor.probemeister.Log.out.warning("TargetVMConnector:: Looking for config file:"+f.getCanonicalPath());                        
                else {    
                    InstrumentationRecordSet irs = InstrumentationRecorder.deserialize(new InputSource(new FileReader(f)));
                    if (irs.getSize()>0) {
                        for (int i=0; i<irs.getSize(); i++) {
                            InstrumentationRecord ir = irs.getRecord(i);
//                            if (ir.getClassName()!=null && ir.getClassName()=="*STARTUP_CLASS*")
//                                ir.setClassName(mainClass.name());
                        }
                        InstrumentationRecorder.playConfiguration(this, irs, null);
                    }
                }
            } catch (Exception e) {
                com.objs.surveyor.probemeister.Log.out.warning("TargetVMConnector:: Exception applying config to main"+e);                        
                e.printStackTrace();
            }
        }
    }        
      
      
      
    public LoggerNames getLoggerNames() { return logNames; }

    /* Find the java.lang.System class */
    ClassType getSystemClass() {     
        return (ClassType)findRefType("java.lang.System");
    }

     /* Find the java.lang.System class */
    public ReferenceType findRefType(String _n) {
     
        java.util.List cl = this.getVMClasses();
        //Look for System
        ReferenceType ref;
        for (int i=0; i<cl.size(); i++) {
            ref = (ReferenceType)cl.get(i);
            if (ref.name().equals(_n)) {
                return ref;
            }
        }
        return null;
    }

 
    /* Invoked when the Target VM has contacted us. */
//    public TargetVMConnector(String _name, String _appAddr, String _transport) 
//      throws Exception {
//        System.out.println("Initializing TargetVM connection...");

//        name = _name; 
 //       targetVM = this;
   //     address = _appAddr;
        
        //Initiate connection to VM
/*        attachToApp(_appAddr, _transport, _reattach);
        
        if (connection().isOpen()) {
            // Connection opened when attachToApp is called
             
             System.out.println("***Connection is open***");
             
            //Register for appropriate VM events from handler
            handler = _handler;
            handler.addListener(this);
        }
*/
    //}
    
    public boolean allThreadsStopped() {
        List threads = vm().allThreads();
        ThreadReference tr;
        Iterator iter = threads.iterator();
        boolean allStopped = true;
        while (iter.hasNext()) {
            try {
                tr = (ThreadReference)iter.next();
                if (!tr.isSuspended()) {
                    allStopped = false;
                    break;
                }
            //If collected then the thread must not be active anymore... but report this anyway    
            } catch (com.sun.jdi.ObjectCollectedException oce) {
                com.objs.surveyor.probemeister.Log.out.fine("ObjectCollectedException occurred in allThreadsStopped");                
            }
        }
        return allStopped;
    }        
    
    public void finalize() {
        if (vm()!=null)
            vm().resume(); //don't leave targetVM in suspended state
            vm().dispose(); //don't leave targetVM in suspended state
    }
    
    public String getAddress() {return address;}
    public String getName()    {return name;}
    private void setAddress(String _a) {address = _a;}
    private void setName(String _n)    {name = _n;}

    public java.util.List getVMClasses() {
        if (vmClassesList==null) vmClassesList= new Vector(getLoadedClasses());
        return vmClassesList;
    }

    void addVMClass(ClassType _ct) {
//String n = _ct.name();
//if (n.startsWith("Sim")||n.startsWith("com.o")){
//System.out.print("*********Added new class: "+ _ct.name());
//Thread.currentThread().dumpStack();
//    }
//else System.out.print("..:..");
        getVMClasses().add(_ct);
    }
    
    
    /* True when VM has ever entered into VMStarted_Running state */
    public boolean hasVMStartedRunning() {return vmStartedRunning;}
    
    //Called after any new classes are loaded
    public void newClassLoaded(ReferenceType _rt) { 
        //Propagate notification to GUI
        if (_rt instanceof ClassType) {
            this.addVMClass((ClassType)_rt);
            emitVMEvent(CLASSLISTUPDATED, (ClassType)_rt);    
        }
    }


    /* Default is true */
    public void setResumeVMOnStart(boolean _b) {resumeVMOnStart=_b;}

    public void suspended(boolean _b) {vmSuspended = _b;}
    public boolean suspended() {return vmSuspended;}

    private static boolean supportsSharedMemory() {
        List connectors = Bootstrap.virtualMachineManager().allConnectors();
        Iterator iter = connectors.iterator();
        while (iter.hasNext()) {
            Connector connector = (Connector)iter.next();
            if (connector.transport().name().equals("dt_shmem")) {
                return true;
            }
        }
        return false;
    }

    private static String addressToSocketArgs(String address) {
        int index = address.indexOf(':');
        if (index != -1) {
            String hostString = address.substring(0, index);
            String portString = address.substring(index + 1);
            return "hostname=" + hostString + ",port=" + portString;
        } else {
            return "port=" + address;
        }
    }


    public VMConnection connection() {
        return connection;
    }

    /* Causes the target VM to terminate execution. */
    public void terminateVM() {
        vm().exit(0);
    }

    /* Disconnect from VM, but leave it running. Probes remain active in VM. */
    public void disconnectVM() {
        vm().dispose();
    }

    /* Resume execution in VM. */
    public void resumeVM() {
        try {
            com.objs.surveyor.probemeister.Log.out.finest("****************************** going to resume VM...");                
            vm().resume();
            com.objs.surveyor.probemeister.Log.out.finest("****************************** Resumed VM!!!!!");                
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.warning("****************************** Caught exception on resume!!!!! -->"+e);                
        }
        
        //If the last state was VMSTARTED_INTERRUPTED, the new state is VMSTARTED_RUNNING
        if (lastState == VMSTARTED_INTERRUPTED) {
            emitVMEvent(this.VMSTARTED_RUNNING, null);
            lastState = -1; //we only need this once
        }
        else //We're coming out of a runtime VMINTERRUPTED state
            emitVMEvent(this.VMRESUMED, null);
    }

    public VirtualMachine vm() {
        if (this.connection != null)
            try {
            return this.connection.vm();
            } catch(Exception e) {
                return null;
            }
        else 
            return null;
    }

    //public static void main(String argv[]) {
    /*  @param address Address of the target VM
     *  @param transport String defining transport mechanism to use [dt_shmem | dt_socket]
     *  @param reattach Reattach if this connector is already in use, dropping current connector
     *  Return TRUE if successful
     */
    boolean attachToApp(String address, String transport, boolean reattach) {
    
        if (this.connection != null) { // then we're detaching & reattaching to a new app)
            if (!reattach) return false;
            shutdown("Closing connection to current application... reattaching to new one.");
        }
    
        int traceFlags = VirtualMachine.TRACE_NONE;
        boolean launchImmediately = false;
        String connectSpec = null;
  
           /*
            * Use the shared memory attach if it's
            * available; otherwise, use sockets. Build a connect 
            * specification string based on this decision.
            */
        if (supportsSharedMemory() && transport.equals("dt_shmem")) {
            connectSpec = "com.sun.jdi.SharedMemoryAttach:name=" + 
                            address;
        } else if (transport.equals("dt_socket")) {
            String hostNport = addressToSocketArgs(address);
            connectSpec = "com.sun.jdi.SocketAttach:" + hostNport;
        } else { //no go
            com.objs.surveyor.probemeister.Log.out.warning("TargetVMConnector: Cannot find suitable transport to connect to Target VM");
            return false;            
        }

        try {
            if (! connectSpec.endsWith(",")) {
                connectSpec += ","; // (Bug ID 4285874)
            }
            //init(connectSpec, launchImmediately, traceFlags);
            this.connection = new VMConnection(connectSpec, traceFlags);
            
            if (connection == null) return false;
            
            if (!this.connection.isLaunch() || launchImmediately) {
                this.connection.open();
/////                this.connection.vm().resume(); //in case target is suspended
            }
            
            com.objs.surveyor.probemeister.Log.out.info("***Connected to Target VM***");
            return true;
            
        } catch(Exception e) {                
            com.objs.surveyor.probemeister.Log.out.warning("Internal exception:  "+e);
            this.connection = null; //ensure it is null
            return false;
        }
    }

    public void shutdown() {
        shutdown(null);
    }

    public void shutdown(String message) {
        com.objs.surveyor.probemeister.Log.out.fine("Shutdown called on connector.");
        if (connection != null) {
            try {
                connection.disposeVM();
            } catch (VMDisconnectedException e) {
                // Shutting down after the VM has gone away. This is
                // not an error, and we just ignore it. 
            }
        }
        if (message != null) {
            com.objs.surveyor.probemeister.Log.out.info(message);
        }
    }

    void fatalError(String msg) {
        com.objs.surveyor.probemeister.Log.out.warning("Fatal error: "+msg);
        shutdown();
    }


    /*
     * Called from EventHandler for initialization.
     */
    public void initNotifier() {
        
    }
    public boolean vmStartEvent(VMStartEvent e, boolean suspended, boolean lastEvent){
        com.objs.surveyor.probemeister.Log.out.fine("VM started["+this.getAddress()+"] ");
        if (suspended) {                        
            if (resumeVMOnStart) {
                //this.vm().resume(); -- return TRUE instead
                this.suspended(false);
                emitVMEvent(VMSTARTED_RUNNING, null);
                lastState = -1;
                return true; // we handled this event
            } else {
                this.suspended(true);
                lastState = VMSTARTED_INTERRUPTED;
                return emitVMEvent(VMSTARTED_INTERRUPTED, null);
                //return true; // we handled this event
            }
        } else {
            emitVMEvent(VMSTARTED_RUNNING, null);
            lastState = -1;
        }
        return true;
    }
    public void vmDeathEvent(VMDeathEvent e){
        if (this.vm() != null) {
            shutdown("Target VM["+this.getAddress()+"] has been terminated...");
            TargetVMManager.getMgr().vmDropped(this); //notify mgr
            
            com.objs.surveyor.probemeister.Log.out.fine("VM death["+this.getAddress()+"] ");
        }
        emitVMEvent(VMDISCONNECTED, null);
        com.objs.surveyor.probemeister.Log.out.info("VM disconnected["+this.getAddress()+"] ");        
    }
    public void vmDisconnectEvent(VMDisconnectEvent e){
        emitVMEvent(VMDISCONNECTED, null);
        com.objs.surveyor.probemeister.Log.out.fine("VM disconnected["+this.getAddress()+"] ");        
        if (this.vm() != null) {
            shutdown("Target VM["+this.getAddress()+"] connection has disconnected...");
            TargetVMManager.getMgr().vmDropped(this); //notify mgr
        }
    }        
    public boolean vmInterrupted(){ 
  
//Not sure we want this conditional o.w. we won't know how to handle
//the event (e.g. return true or false)
//        if (lastState != VMSTARTED_INTERRUPTED) { //already waiting...
            com.objs.surveyor.probemeister.Log.out.warning("VM interrupted["+this.getAddress()+"] Alert User...");
            return emitVMEvent(VMINTERRUPTED, null);
//        }
        
    }


    private ArrayType getBytesArrayClass() {
        List classes = vm().allClasses();
        Iterator iter2 = classes.iterator();
        while (iter2.hasNext()) {
            ReferenceType rt = (com.sun.jdi.ReferenceType)iter2.next();
            if (rt.name().equals("byte[]"))
                return (ArrayType)rt;   
        }
        com.objs.surveyor.probemeister.Log.out.warning("%%%%defining class - didn't find byte[] class");
        return null;
    }        
        
    /* Returns TRUE if the class _cls has been loaded into the target VM 
     * The class may or may not have been prepared by the VM yet.
     */
    public boolean isClassLoaded(String _cls) {
        
        List classes = this.getVMClasses();
        for (int i=0; i<classes.size(); i++) {
            if (((ReferenceType)classes.get(i)).name().equals(_cls))
                return true;
        }
        return false;        
    }
        
    /*  This method exports a class file to the target VM. This may be
     *  required when support classes necessary for probing do not exist in the
     *  target VM.
     *
     *  @param _mainClass is the main class of the application. Since this is used to
     *              establish a breakpoint (we actually need the ref to the classloader), 
                    **** this method should ONLY be called after a VMStarted event ****. 
     *              The EventHandler identifies this class.
     *  @param _thread is the main thread of the running application
     *  @param _classFile is the File object for the class to be exported
     *  @return the ReferenceType of the exported class
     */
    public ReferenceType exportClassToTargetVM(ClassType _mainClass, ThreadReference _thread, File _classFile, String _className) 
        throws DuplicateClassException, NotAtBreakpointException {

        //Create InputStream from which to read class bytes
        FileInputStream classIS = null;
        try {
            classIS =new FileInputStream(_classFile);
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.warning("%%%%defining class - could not find class file"+_classFile.getPath());
            e.printStackTrace();
            return null;
        }

        return exportClassToTargetVM(_mainClass, _thread, classIS, _className);
    }
        
    /*  This method exports a class file to the target VM. This may be
     *  required when support classes necessary for probing do not exist in the
     *  target VM.
     *
     *  @param _mainClass is the main class of the application. Since this is used to
     *              establish a breakpoint (we actually need the ref to the classloader), 
                    **** this method should ONLY be called after a VMStarted event ****. 
     *              The EventHandler identifies this class.
     *  @param _thread is the main thread of the running application
     *  @param _classIS is the InputStream for the class to be exported
     *  @return the ReferenceType of the exported class
     */
    public ReferenceType exportClassToTargetVM(ClassType _mainClass, ThreadReference _thread, InputStream _classIS, String _className) 
        throws DuplicateClassException, NotAtBreakpointException {

        if (! _thread.isAtBreakpoint()) 
            throw new NotAtBreakpointException();
            
        //Get the instance of ClassLoader that loaded the main class
        ClassLoaderReference classLoaderRef = _mainClass.classLoader();
        
        if (classLoaderRef == null) {
            com.objs.surveyor.probemeister.Log.out.warning("*********classLoaderRef is NULL for "+_mainClass.name()+"!************");
            return null;
        } else
            com.objs.surveyor.probemeister.Log.out.fine("*********classLoaderRef is NOT NULL for "+_mainClass.name()+"!************");
        
        //Make sure this class isn't already over there... otherwise redefineClass must be used.
        //The VM will o.w. throw an exception.
        List classes = this.getVMClasses();
        if (isClassLoaded(_className) ) {            
            com.objs.surveyor.probemeister.Log.out.warning("%%%%defining class--> DUPLICATE CLASS FOUND: "+_className); 
            throw new DuplicateClassException();
        }
        
        //Get the classLoader's refType so we can lookup the "defineClass" method
        ReferenceType classLoaderRefType = classLoaderRef.referenceType();                        
        com.sun.jdi.ClassType classLoaderClassType = (com.sun.jdi.ClassType)classLoaderRefType;
        //find defineClass method                  
        com.sun.jdi.Method loadClassMeth = classLoaderClassType.concreteMethodByName("defineClass","(Ljava/lang/String;[BII)Ljava/lang/Class;");

        //Load class file & send bytes to VM
        if (loadClassMeth != null) {

            //Load bytes from class file
            byte[] fileBytes = com.objs.surveyor.probemeister.bytecoder.Bytecoder.loadClassFromInputStream(_classIS);
            ArrayType bytesType = getBytesArrayClass();
            ArrayReference classBytes = bytesType.newInstance(fileBytes.length);
            try {
                for (int i=0; i<fileBytes.length; i++)
                    classBytes.setValue(i,vm().mirrorOf(fileBytes[i]));
            } catch (Exception e) {
                com.objs.surveyor.probemeister.Log.out.warning("%%%%defining class - could not initialize byte array...");
                e.printStackTrace();
                return null; 
            }

            //Prepare arguments for defineClass invocation
            java.util.Vector v = new java.util.Vector(); //arguments container
            v.addElement(vm().mirrorOf(_className));
            v.addElement(classBytes);
            v.addElement(vm().mirrorOf(0));
            v.addElement(vm().mirrorOf(fileBytes.length));

            //Invoke defineClass() method, tranferring bytes to targetVM
            com.sun.jdi.Value result = null;
            try {
                result = classLoaderRef.invokeMethod(_thread,loadClassMeth,v,0);
                ReferenceType refT = ((ClassObjectReference)result).reflectedType();
                com.objs.surveyor.probemeister.Log.out.fine("%%%%defining class--> result :: Class name is: "+refT.name());               
                //vm().resume(); //doesn't appear to work, looks like eventSet.resume() is required
                //First update the class list in the gui
                //this.updateClassesList();
/*                this.newClassesLoaded();
                //Now check & see if new class is there!
                classes = this.getVMClasses();
                Iterator iter2 = classes.iterator();
                while (iter2.hasNext()) {
                    ReferenceType rt = (com.sun.jdi.ReferenceType)iter2.next();
                    if (rt.name().equals(_classFile)) {
                        System.out.println("%%%%defining class--> FOUND NEW CLASS");  
                        //RECORD ACTION
                        this.recorder.recordAction(new InstrumentationRecord_NewClass(_classFile.getAbsolutePath(), _className));
                        break;
                    }
                }
*/                //gui.gui.setClassList(getLoadedEditableClasses().toArray());
                return refT;
            } catch(InvocationException ie) {
                //System.out.println("%%%%defining class -----------------------------");
                com.objs.surveyor.probemeister.Log.out.warning("%%%%defining class -- exception:\n"+ie+" -- "+ie.getMessage());
                //ie.printStackTrace();
                com.objs.surveyor.probemeister.Log.out.warning("Object ref = "+ie.exception());
                com.objs.surveyor.probemeister.Log.out.warning("getCause() = "+ie.getCause());
                //System.out.println("%%%%defining class -----------------------------");
            } catch(com.sun.jdi.InvalidTypeException e) {
                com.objs.surveyor.probemeister.Log.out.warning("%%%%defining class -- exception:\n"+e+" -- "+e.getMessage());
                e.printStackTrace();
            } catch(com.sun.jdi.ClassNotLoadedException cne) {
                com.objs.surveyor.probemeister.Log.out.warning("%%%%defining class -- exception:\n"+cne+" -- "+cne.getMessage());
                cne.printStackTrace();
            } catch(com.sun.jdi.IncompatibleThreadStateException itse) {
                com.objs.surveyor.probemeister.Log.out.warning("%%%%defining class -- exception:\n"+itse+" -- "+itse.getMessage());
                itse.printStackTrace();
            }

            //thread.resume();
            com.objs.surveyor.probemeister.Log.out.fine("%%%%defining class - result = "+result);
        } else 
            com.objs.surveyor.probemeister.Log.out.warning("%%%%defining class - Could not find defineClass method...");
        return null;
    }
        
    /*
     *
     * @return List of com.sun.jdi.ReferenceType objects. Each call invalidates
     * the last list of objects, so it should only be called once.
     */
    public List getLoadedClasses() {
        List l=null;
        try {
            l = vm().allClasses(); 
        } catch(Exception e) {
            com.objs.surveyor.probemeister.Log.out.severe("getLoadedClasses() caused exception: "+e);
        }
        return l;
    } //list of ReferenceType objects
    
    /* Returns list of classes that are not abstract or simple type.
     * List will include inner classes.
     */
    public List getLoadedEditableClasses() {
        
        //TargetVMConnector tvm = TargetVMManager.getMgr().getTargetVMConnectorByName("app1"); 
        List l = null;
        //System.out.println("Getting classes for "+this.name);
        l = getVMClasses();
        
        Vector editable = new Vector(50, 50);
        Iterator iter = l.iterator();
        while (iter.hasNext()) {            
            ReferenceType rt = (ReferenceType)iter.next();
            if (rt instanceof ClassType)
                editable.add(rt);
        }
        return editable;
    }

    public synchronized void addVMListener(TargetVMListener _l) {
        com.objs.surveyor.probemeister.Log.out.finer("---------->Adding TargetVMListener");
        if (_l == null) return;
        if (vmListeners==null) {
            vmListeners = new TargetVMListener[1];
            vmListeners[0]=_l;
        } else { //grow list
            TargetVMListener temp[] = new TargetVMListener[vmListeners.length+1];
            int i=0;
            for (; i<vmListeners.length; i++){
                temp[i]=vmListeners[i];
            }
            temp[i]=_l;
            vmListeners = temp;
        }
    }

    public synchronized void removeVMListener(TargetVMListener _l) {
        if (_l == null) return;
        if (vmListeners==null) {return;} 
        else { //shrink list IF listener found
            boolean listenerFound = false;
            TargetVMListener temp[] = new TargetVMListener[vmListeners.length-1];
            for (int i=0, j=0; i<vmListeners.length; i++, j++){
                if (j == vmListeners.length-1 && !listenerFound) break; //at end, not there, exit
                if (vmListeners[j] == _l) {j++; listenerFound=true; continue;} //don't copy this over (essentially, remove it)
                temp[i]=vmListeners[j];
            }
            if (listenerFound) vmListeners = temp;
        }
    }

    public InstrumentationRecorder recorder() { return recorder; }

    //This method is called when the TargetVM has reached a breakpoint, so that
    //class redefinitions and new classes can be added. Probe insertions can occur
    //at any point.
    public void processConfigRequests(ClassType _mainClass, ThreadReference _eventThread) {
     
com.objs.surveyor.probemeister.Log.out.info("TargetVMConnector::Processing Config Request - NOT DEVELOPED YET.");        
///        dd; //so the debugger always hits this... during development
    }




     

    public static final int CLASSLISTUPDATED = 0;
    public static final int VMCONNECTED = 1;
    public static final int VMDISCONNECTED = 2;
    public static final int VMINTERRUPTED = 3;
    public static final int VMRESUMED = 4;
    public static final int VMSTARTED_RUNNING = 5;
    public static final int VMSTARTED_INTERRUPTED = 6;
    public static final int VMBREAKPOINT = 7;
    public static final int VMINSTRUMENTED = 8;
    public static final int VMPARAMSUPDATED = 9;

    /*
     *  The state machine is:
     *      CONNECTED - when connection is successfully opened to targetVM
     *      VMSTARTED_INTERRUPTED - Immediately following CONNECTED, if started suspended.
     *          Once the VM is resumed, the next state after this is VMSTARTED_RUNNING
     *      VMSTARTED_RUNNING - Immediately following CONNECTED, if started NOT suspended.
     *  If the VM enters VMINTERRUPTED, either from the user or an event request that suspends 
     *  execution, a VMRESUMED event may be generated by the user or application
     *  VMDISCONNECTED is generated by the user (via the GUI) or targetVM.
     *
     * @return TRUE (if it is a Breakpoint event, then return true IF handler is 
     *    done processing event & thread/app can be resumed).
     */
    public boolean emitVMEvent(int _evt, Object _source) {

        if (vmListeners == null) {
            com.objs.surveyor.probemeister.Log.out.warning("---------->No vm listeners...");
            return true;        
        }
        //System.out.println("---------->Handling vm listener event...");
        boolean result = true;
        switch (_evt) {
            case CLASSLISTUPDATED: 
                //System.out.println("---------->classListUpdated event");
                for (int i=0; i< vmListeners.length; i++) 
                    vmListeners[i].classListUpdated(new TargetVMEvent(this, _source));//source = ClassType object
                break;
            case VMCONNECTED: 
                for (int i=0; i< vmListeners.length; i++) 
                    vmListeners[i].vmConnected(new TargetVMEvent(this));
                break;
            case VMDISCONNECTED: 
                for (int i=0; i< vmListeners.length; i++) 
                    vmListeners[i].vmDisconnected(new TargetVMEvent(this));
                break;
            case VMINTERRUPTED: 
                result = true;
                for (int i=0; i< vmListeners.length; i++) 
                    result = result && vmListeners[i].vmInterrupted(new TargetVMEvent(this));
                return result;
            case VMRESUMED: 
                for (int i=0; i< vmListeners.length; i++) 
                    vmListeners[i].vmResumed(new TargetVMEvent(this));
                break;
            case VMSTARTED_RUNNING: 
                vmStartedRunning = true;
                //this.handler().startListeningForNewClasses();
                com.objs.surveyor.probemeister.Log.out.fine("---------->vm started running event ["+vmListeners.length+"]");
                for (int i=0; i< vmListeners.length; i++) 
                    vmListeners[i].vmStartedRunning(new TargetVMEvent(this));
                break;
            case VMSTARTED_INTERRUPTED: 
                com.objs.surveyor.probemeister.Log.out.finest("---------->vm started interrupted event ["+vmListeners.length+"]");
                result = true;
                for (int i=0; i< vmListeners.length; i++) 
                    result = result && vmListeners[i].vmStartedInterrupted(new TargetVMEvent(this));
                return result;
            case VMBREAKPOINT: //source = breakpoint event
                result = true;
                if (!triedToGetCommandLineParams)
                    try {
                        retrieveCommandLineParams(_source);
                    } catch(NotAtBreakpointException nbe){}
                com.objs.surveyor.probemeister.Log.out.finest("---------->vm  breakpoint event - num listeners = ["+vmListeners.length+"]");
                //Process any force load class requests
//this.processForceLoads(new TargetVMEvent(this, _source));
                
                //Emit event to all other listeners...
                for (int i=0; i< vmListeners.length; i++) 
                    result = result && vmListeners[i].vmBreakpointEvent(new TargetVMEvent(this, _source));
                return result;
            case VMINSTRUMENTED: //source = InstrumentationRecord
                for (int i=0; i< vmListeners.length; i++) 
                    vmListeners[i].vmInstrumentedEvent(new TargetVMEvent(this, _source));
                break;
            case VMPARAMSUPDATED: //invoked when command line params are imported 
                for (int i=0; i< vmListeners.length; i++) 
                    vmListeners[i].vmUpdatedVMParamsEvent(new TargetVMEvent(this, this.vmParams));
                break;
            default: ;
        }        
        return true; //resume thread/app
    }


    /* 
     * This method retrieves all command line parameters passed into
     * the target JVM using the "-D" prefix.
     * 
     * Call only when there is a breakpoint, otherwise exception is thrown
     */
    void retrieveCommandLineParams(Object _source) throws NotAtBreakpointException {
    final boolean debug=false;
    
        com.objs.surveyor.probemeister.Log.out.info("***retrieveCommandLineParams:: started");
                
        triedToGetCommandLineParams = true;                
                
        //Validate breakpoint state 
        BreakpointEvent be = (BreakpointEvent)_source;
        if (be == null ) {
            com.objs.surveyor.probemeister.Log.out.severe("retrieveCommandLineParams:: NotAtBreakpoint exception thrown. event is null");
            throw new NotAtBreakpointException();
        }

        if (!be.thread().isAtBreakpoint()) {
            com.objs.surveyor.probemeister.Log.out.severe("retrieveCommandLineParams:: NotAtBreakpoint exception thrown. Not at breakpoint");
            throw new NotAtBreakpointException();
        }
            
            
        //Get current thread         
        ThreadReference thr = ((BreakpointEvent)_source).thread();                
        
        //Get system class - o.w. do not continue
        if (sysClass == null) { 
            com.objs.surveyor.probemeister.Log.out.severe("retrieveCommandlineParams::Cannot get commandline params, system class not identified.");
            return;
        }

        try { // make sure sysClass hasn't been garbage collected by touching any method.
            String ss = sysClass.name();
        } catch (com.sun.jdi.ObjectCollectedException oce) {
            sysClass = null;
            List l = getVMClasses();
            int x = l.size();
            for (int i = 0; i< x; i++) {
                if (((ReferenceType)(l.get(i))).name().equals("java.lang.System")) {
                    sysClass = (ClassType)l.get(i);
                    com.objs.surveyor.probemeister.Log.out.finest("retrieveCommandlineParams:: sysClass found ...again.");
                    break;
                }
            }
            if (sysClass == null) { 
                com.objs.surveyor.probemeister.Log.out.severe("retrieveCommandlineParams::Cannot get commandline params, system class not identified.");
                return;
            }
        }
                
        com.objs.surveyor.probemeister.Log.out.finest("retrieveCommandLineParams:: found system class, invoking getProperty()");

        //Find getProperty() method handle
        com.sun.jdi.Method meth = sysClass.concreteMethodByName("getProperty",
                                        "(Ljava/lang/String;)Ljava/lang/String;");

        com.sun.jdi.Value result = null;
        
        //Get list of possible param strings that might be on the command-line
        String[] pNames = vmParams.getParamNames();
        
        java.util.Vector v = new java.util.Vector();
        int count = 0;
        for (int i=0; i<pNames.length; i++) {
            
            v.add(this.vm().mirrorOf(pNames[i]));
            
            try {
                result = sysClass.invokeMethod(be.thread(),meth,v,0); //invoke it!
                if (result != null) {
                    if (result instanceof StringReference) { //then add to our list
                        vmParams.put(pNames[i], ((StringReference)result).value());
                        count++;
                    }
                }
            } catch(Exception e) {
                com.objs.surveyor.probemeister.Log.out.severe("TargetVMConnector::retrieveCommandLineParams -- exception:\n"+e);
                e.printStackTrace();
            }
            v.removeAllElements(); //rather than creating a new Vector each time            
        }
        
        
        
        if (count>0) { // update all interested parties
            
            //update this VM's name/addr
            String t_addr = vmParams.get(TargetVMCLParams.APPADDR);
            String t_name = vmParams.get(TargetVMCLParams.APPNAME);
            if (t_name != null) this.setName(t_name);
            if (t_addr != null) this.setAddress(t_addr);
            
            emitVMEvent(this.VMPARAMSUPDATED,null);            
        } else
            com.objs.surveyor.probemeister.Log.out.info("retrieveCommandLineParams:: No CL args found.");
        
        
    }

    /* Support class, used to handle Control-C events that might leave the target 
     * VM in a corrupt or suspended state. THis should be called by the Runtime class
     * if Cntl-C is pressed, but not nec. if a (unix-like)kill is used.
     */
     class ShutdownThread extends Thread {
         TargetVMConnector tvmc;
         ShutdownThread(TargetVMConnector _tvmc) {
             this.tvmc = _tvmc;
         }
         public void run() {
            if (tvmc != null)
                if (tvmc.vm() != null) {
                    try {
                        tvmc.vm().resume();
                        tvmc.vm().dispose();
                    } catch(Exception e) {
                        try {tvmc.vm().dispose();} catch(Exception ee){}
                    }
                }
         }
     }
    
    /* Adds InstrumentationRecord to queue to be applied when a class is loaded */
    public void queueConfigRecord(InstrumentationRecord _rec) {
        
        //queuedConfigs.add(_rec);
        //enableClass
        
    }


     private void processForceLoads(TargetVMEvent _e) {
        if (forceClassLoadList.size()==0)
            return;
        else {
            //Copy current list into new list, so any new additions don't
            //get wiped out when we're done
            synchronized(forceClassLoadList) {
                forceLoadClass(_e, forceClassLoadList.toArray());
                forceClassLoadList.removeAllElements();
            }
        }
     }

     /* Returns true if we can force load & item is added to list */
     public boolean addClassToForceLoad(String _cls) { 
        if (!this.handler().canEnableBreakpoint())
            return false;
        forceClassLoadList.addElement(_cls);
        this.handler().enableBreakpoint(true);
        return true;
     }
     /* Returning null indicates that this class was not yet force loaded
      * FALSE indicates that forcing failed.
      */
     public Boolean wasClassForceLoaded(String _cls) { return (Boolean)forceClassLoadedList.get(_cls); }

     private void forceLoadClass(TargetVMEvent _e, Object[] _clsList) { 

            
            final boolean debug=false;        
            com.objs.surveyor.probemeister.Log.out.fine("forceLoadClass called");
                    
            //Validate breakpoint state 
            BreakpointEvent be = (BreakpointEvent)_e.getSource();
            if (be == null ) {
                com.objs.surveyor.probemeister.Log.out.severe("forceLoadClass:: NotAtBreakpoint exception thrown. event is null");
                return; //throw new NotAtBreakpointException();
            }

            if (!be.thread().isAtBreakpoint()) {
                com.objs.surveyor.probemeister.Log.out.severe("forceLoadClass:: NotAtBreakpoint exception thrown. Not at breakpoint");
                return; //throw new NotAtBreakpointException();
            }
                
                
            //Get current thread         
            ThreadReference thr = be.thread();                
            
            ClassType classClass = null;
            sysClass = null;
            List l = getVMClasses();
            int x = l.size();
            for (int i = 0; i< x; i++) {
                if (((ReferenceType)(l.get(i))).name().equals("java.lang.Class")) {
                    classClass = (ClassType)l.get(i);
                    com.objs.surveyor.probemeister.Log.out.finest("forceLoadClass:: class Class found ...again.");
                    break;
                }
            }
            if (classClass == null) { 
                com.objs.surveyor.probemeister.Log.out.severe("forceLoadClass::Cannot forceLoadClass, cannot find class Class.");
                return;
            }
                    
            //Find getProperty() method handle
            com.sun.jdi.Method meth = classClass.concreteMethodByName("getName",
                                            "(Ljava/lang/String;)Ljava/lang/Class;");

            com.sun.jdi.Value result = null;
            String clsName=null;
            java.util.Vector args = new java.util.Vector();
            for (int pos=0; pos<_clsList.length; pos++) {
                try {
                    clsName = (String)_clsList[pos];
                    if (clsName==null || clsName.length()<1)
                        continue; //nothing to process
                    args.add(this.vm().mirrorOf(clsName));
                    result = classClass.invokeMethod(be.thread(),meth,args,0); //invoke it!
                    if (result != null) { //add entry to results
                        forceClassLoadedList.put(clsName, Boolean.TRUE); //succeeded
                    }
                } catch(Exception e2) {
                    com.objs.surveyor.probemeister.Log.out.severe("TargetVMConnector::forceLoadClass -- exception:\n"+e2);
                    e2.printStackTrace();
                    forceClassLoadedList.put(clsName, Boolean.FALSE); //failed, add result
                }
                args.removeAllElements(); //reuse this object
            }                        

            //Call again, just in case more classes were added since we started
            processForceLoads(_e);

        }




/* Left over
            System.out.println("Thread Start Requests");
            java.util.List tsRequests = Env.vm().eventRequestManager().threadStartRequests();
            int size = tsRequests.size();
            for (int i = 0; i<size; i++) {
                EventRequest er = (EventRequest) tsRequests.get(i);
                String str = er.toString();
                if (er.isEnabled())
                    str += "[enabled]";
                System.out.println("ER#"+i+": "+str+"... disabling");   
                er.disable();
            }

            System.out.println("threadDeathRequests Requests");
            tsRequests = Env.vm().eventRequestManager().threadDeathRequests();
            size = tsRequests.size();
            for (int i = 0; i<size; i++) {
                EventRequest er = (EventRequest) tsRequests.get(i);
                String str = er.toString();
                if (er.isEnabled())
                    str += "[enabled]";
                System.out.println("ER#"+i+": "+str+"... disabling");   
                er.disable();
            }
  */               




}

