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

/* Written by Paul Pazandak, Object Services and Conulting, Inc. 
 * 
 * New since 1.0 Feb2002:
 * - Added RMI control path via RemotePM interface
 *   - Added rmi activation & mgmt into MultiJVM
 *   - Added classserver/classfileserver classes
 * - Added ability to manipulate global variable values via
 *     a file passed in thru the command line.
 *
 */

package com.objs.surveyor.probemeister.gui;


import com.objs.surveyor.probemeister.*;
import com.objs.surveyor.probemeister.probe.*;
import com.objs.surveyor.probemeister.runtime.*;
import com.objs.surveyor.probemeister.bytecoder.*;
import com.objs.surveyor.probemeister.instrumentation.*;
import java.io.FileReader;
import org.xml.sax.InputSource;


public class ProbeMeisterGUI {
    
//    public PMGUI gui;
    public MultiJVM gui;
    java.util.List classList = null;
    public RuntimeClassManager rtMgr;
    
    static public void main(String[] args) {

        if (args.length > 1) {
            for (int i=0; i<args.length; i++) {
                if (args[i].equals("-listen")) {                
                    if (++i < args.length) 
                        com.objs.surveyor.probemeister.Globals.globals().put(com.objs.surveyor.probemeister.GlobalVars.LISTEN_PORT_STR, args[i]);
                    else 
                        usage();
                } else
                if (args[i].equals("-gFile")) {                
                    if (++i < args.length) 
                        com.objs.surveyor.probemeister.Globals.globals().importGlobalSettings(args[i]);
                    else 
                        usage();
                }
                else if (args[i].equals("-rmi")) {                
                    if (++i < args.length) {
                        int port = Integer.parseInt(args[i]);
                        com.objs.surveyor.probemeister.Globals.globals().put(com.objs.surveyor.probemeister.GlobalVars.RMI_ACTIVE, Boolean.TRUE);
                        com.objs.surveyor.probemeister.Globals.globals().put(com.objs.surveyor.probemeister.GlobalVars.RMI_PORT, new Integer(port));
                    }
                    else 
                        usage();
                }
                else 
                    usage();
            }
        }


        ProbeMeisterGUI pm = new ProbeMeisterGUI();        
            
    }
    
    public static void usage() {
        
        System.out.println("Usage: Optional parameters");
        System.out.println("^t-listen <port number>  = to allow JVMs to connect to ProbeMeister");   
        System.out.println("^t-rmi    <port number>  = to allow rmi connections over the specified port");   
        System.out.println("^t-gFile  <filename>     = specify a global settings file to inport");   
    }
    
    public ProbeMeisterGUI() {
        
        System.out.println("**********************************************************");
        System.out.println("ProbeMeister 2002 Version 1.0");
        System.out.println();
        System.out.println("Object Services and Consulting, Inc.");
        System.out.println("http://www.objs.com");
        System.out.println("**********************************************************");

        try {

            rtMgr = RuntimeClassManager.getMgr();
            gui = new MultiJVM(this, "ProbeMeister 2002 Version 1.0");
            gui.setVisible(true);

//For debugging:
  //          TargetVMConnector tvm1 = TargetVMManager.getMgr().addTargetVM("app1", "9876", "dt_socket", false);
  //          TargetVMConnector tvm2 = TargetVMManager.getMgr().addTargetVM("app2", "7771", "dt_socket", false);


        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.severe("Error running program: "+e);
            e.printStackTrace();
        }
    }

    
    //Central point for gui access to underlying code
    
    public PMClass[] getClassList(TargetVMConnector _tvmc) {        
        return rtMgr.getClassList(_tvmc);
    }
    
    
    public PMClass getPMClass(Object _o, TargetVMConnector _tvmc) {        
        return rtMgr.getPMClass(_o, _tvmc);
    }
    
    public String[] getProbeTypeList() {     
        return rtMgr.getProbeTypeList();    
    }
    
    public boolean isProbeAStub(String _probeName) {
        return rtMgr.isProbeAStub(_probeName);    
    }
    
    /* Get actual class name from a .class file */
    public String getClassNameFromFile(String filename) {
        return rtMgr.getClassNameFromFile(filename);
    }

    /* This must be enabled before new classes or class modification actions
     * can be performed. Returns true if it was successful. Then, wait for
     * a breakpoint event to apply desired actions.
     */
    public boolean enableBreakpoint(TargetVMConnector _tvmc, boolean _b) {
        return _tvmc.handler().enableBreakpoint(_b);
    }

    /* Returns true if breakpointing is supported in this Target VM. 
     */
    public boolean canEnableBreakpoint(TargetVMConnector _tvmc) {
        return _tvmc.handler().canEnableBreakpoint();
    }

    ///////////////////////////////
    //Configuration Related Methods
    ///////////////////////////////
    /* returns current configuration as a String */
    public String getConfiguration(TargetVMConnector _tvcm) {
        return _tvcm.recorder().getCurrentSerializedSet();
    }
    
    /* returns current configuration as InstrumentationRecordSet */
    public MethodFilterList getProbedMethods(TargetVMConnector _tvcm) {
        return _tvcm.recorder().getCurrentClassMethods();
    }

    public void viewConfigurationFile(java.io.File _f) {
        InstrumentationRecordEditor.displayConfiguration(_f);
    }
    
    public void viewCurrentConfiguration(TargetVMConnector _tvmc) {
        InstrumentationRecordEditor.displayCurrentConfiguration(_tvmc);
    }

    public void saveConfiguration(TargetVMConnector _tvcm, java.io.File _file) {
        _tvcm.recorder().storeRecords(_file);
    }

    public InstrumentationRecordSet playConfiguration(TargetVMConnector _tvcm, InstrumentationRecordSet _configSet, com.sun.jdi.event.BreakpointEvent _evt) {
        return _tvcm.recorder().playConfiguration(_tvcm, _configSet, _evt);
    }


    public InstrumentationRecordSet playConfiguration(TargetVMConnector _tvcm, String _config, com.sun.jdi.event.BreakpointEvent _evt) 
        throws InstrumentationParsingException {
        return _tvcm.recorder().playConfiguration(_tvcm, _config, _evt);
    }

    public boolean configurationRequiresBreakpoint(java.io.File _config) 
        throws InstrumentationParsingException, java.io.FileNotFoundException {
        return InstrumentationRecorder.hasBreakpointActions(new InputSource(new FileReader(_config)));
    }

    public InstrumentationRecordSet playConfiguration(TargetVMConnector _tvcm, java.io.File _config, com.sun.jdi.event.BreakpointEvent _evt) 
        throws InstrumentationParsingException {
        return _tvcm.recorder().playConfiguration(_tvcm, _config, _evt);
    }

    public boolean deapplyConfiguration(TargetVMConnector _tvcm, String _config) {
        //_tvcm.
        return false;
    }

    public int getCurrentConfigurationSize(TargetVMConnector _tvcm) {
        return _tvcm.recorder().getSize();
    }

    
    
}
