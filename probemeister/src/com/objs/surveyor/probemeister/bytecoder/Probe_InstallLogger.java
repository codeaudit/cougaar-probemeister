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

package com.objs.surveyor.probemeister.bytecoder;

import com.objs.surveyor.probemeister.probe.*;
import com.objs.probemeister.*;
import com.objs.surveyor.probemeister.instrumentation.InstrumentationRecord_Probe;
import com.objs.surveyor.probemeister.instrumentation.InstrumentationRecord;
import java.util.Hashtable;

/*
 * This is the probe type for setting up a Logger so probes emit messages to a remote location
 * Generally only to be installed once per VM, but nothing prohibits otherwise.
 */
public class Probe_InstallLogger  extends com.objs.surveyor.probemeister.bytecoder.BytecodeProbeType {
    
    String host=null;
    short portnum=0;
    String logger=null;    
    String port=null;
    String formatter=null;
    String logName=null;
    boolean usePM=false;
    
    int idCount;
    final String name="Collector Config"; //user visible name of ProbeType
    static Probe_InstallLogger pt = null;
    static {
        pt = new Probe_InstallLogger();
        pt.idCount = 0;
    }
    /* Return an instance of this class */
    public static ProbeType getStub() { return pt;}
    
    //Not publicly accessible   
    private Probe_InstallLogger() {
	}    
    
    /* Well clarified method name */
    public String getRegisteredProbeTypeName() {return name; }
    public String getName() {return name; }
    public String toString() {return name; }
    
    public void register(com.objs.surveyor.probemeister.probe.ProbeCatalog _cat) {

        _cat.registerType(this,"Configure Probe Listener",false,false,true,true,false);
    }
    
    /* Method for probe creation. Create a probe and inserts it into the
     * specified location.
     */
    public ProbeInterface generateProbe(Location _loc) {                
    
        if (!(_loc instanceof BytecodeLocation)) {
            com.objs.surveyor.probemeister.Log.out.warning("Probe_InstallLogger: Incorrect Location type used for bytecode probe creation.");
            return null;
        }
        BytecodeLocation bLoc = (BytecodeLocation)_loc;
        
        com.objs.surveyor.probemeister.Log.out.fine("Probe_InstallLogger: generateProbe");
        
              
        //Get data from the user...
        Probe_GetLoggerInitAttrsGUI.gui().setByteOffset(bLoc.getOffset());
        Probe_GetLoggerInitAttrsGUI.gui().setVisible(true);
        
        if (Probe_GetLoggerInitAttrsGUI.gui().userCancelled())
            return null; //user cancelled 

        usePM = Probe_GetLoggerInitAttrsGUI.gui().usePM();
            
        if (usePM) { //use host id of PM
            try {
                host = java.net.InetAddress.getLocalHost().getHostAddress();            
            } catch (Exception e) {
                host = "127.0.0.1";
            }
            logName = Probe_GetLoggerInitAttrsGUI.gui().getName();
            if (logName==null || logName.length()==0) {
        	    javax.swing.JOptionPane.showMessageDialog(null, "Name not supplied. Cancelling request.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);		    
                return null; //user did not customize
            }
        } else { //get host id
            host = Probe_GetLoggerInitAttrsGUI.gui().getHost();
            if (host==null || host.length()==0 || host.equals("ERROR")) {
        	    javax.swing.JOptionPane.showMessageDialog(null, "Host Name not supplied. Cancelling request.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);		    
                return null; //user did not customize
            }
        }
        
        port = Probe_GetLoggerInitAttrsGUI.gui().getPort();
        try {
            portnum = Short.parseShort(port);
        } catch (Exception e) {
        	javax.swing.JOptionPane.showMessageDialog(null, "Port number not supplied or not a valid number. Cancelling request.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);		    
            return null; //user did not customize
        }

        logger = Probe_GetLoggerInitAttrsGUI.gui().getLogger();        
        if (logger==null || logger.length()==0) {
            logger = "com.objs.ProbeMeister"; //use default
        	javax.swing.JOptionPane.showMessageDialog(null, "No Logger Name supplied, using default: "+logger, "Information", javax.swing.JOptionPane.INFORMATION_MESSAGE);		    
        }
        
        formatter = Probe_GetLoggerInitAttrsGUI.gui().getFormatter();        
        if (formatter!=null && logger.length()==0) //blank string, set to null
            formatter = null;

        int offset = Probe_GetLoggerInitAttrsGUI.gui().getByteOffset();
        bLoc.setOffset(offset);
    	
        SimpleProbe sp=null;
        try {
            StatementList sl = BytecodeInsertionMgr.createStatementList(bLoc);
            if (sl == null) return null;
            StatementFactory.createInitLoggerStmt(sl, bLoc.getMethodObject(), host, portnum, logger, formatter);
            //Embed line that includes parameter so it can be referenced when displaying probe info
            //StatementFactory.createMetadataStmt(sl, "v:"+s1+s);
            String id = "ListenerInitPID"+idCount++;
            String desc = "Configures Probe Listener System";
            sp = new SimpleProbe(id, desc, pt, sl, bLoc);
            
            //must add before probe is inserted so the server 
            //is running when the probe code gets executed
            if (usePM) { //set up PM Collector now that the probe was installed successfully
                try {
                    if (com.objs.probemeister.LoggerManager.loggerMgr() != null) {
                        LoggerWindow lw = LoggerManager.loggerMgr().addLogger(port, logName, logger);
                        
                        //com.objs.probemeister.PMLogServer ls = 
                        PMLogServer server = PMLogServer.addServer(portnum, lw, logName);
                        if (!server.isAlive()) //then start it
                            server.start();
                        javax.swing.JOptionPane.showMessageDialog(null, "The PM Data Collector has been started", "Information", javax.swing.JOptionPane.ERROR_MESSAGE);		    
                    } else                        
                        javax.swing.JOptionPane.showMessageDialog(null, "Error: Manager not found. Couldn't start collector.", "Information", javax.swing.JOptionPane.ERROR_MESSAGE);		    
                } catch (Exception e) {
                    javax.swing.JOptionPane.showMessageDialog(null, "The PM Data Collector could NOT be started", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);		    
                    com.objs.surveyor.probemeister.Log.out.warning("The PM Data Collector could NOT be started. Exception was "+e);                            	
                }
            }

            InstrumentationRecord ir = new InstrumentationRecord_Probe(id, desc, pt, bLoc, getParamsMap());
            //This could be a separate method call            
            if (BytecodeInsertionMgr.insertProbe(sp, ir)) {
                com.objs.surveyor.probemeister.Log.out.fine("Probe_InstallListener: Added probe.");
                //register logger name
                try {
                    bLoc.getClassMgr().vmConnector().getLoggerNames().addLoggerName(logger);
                } catch (Exception e) {
                    com.objs.surveyor.probemeister.Log.out.warning("Error adding listener name.");
                }
            }
            else
                return null;            
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.WARNING, "Error creating probe.", e);
            return null;
        }
        
        return sp;
    }

    
    //Creates and inserts a probe (from InstrumentationRecord)
    public ProbeInterface generateProbe(String _id, String _desc, BytecodeLocation _loc, java.util.Map _params) {                
  
        //Get attrs from the map of params...
        setParamsMap(_params);
        if (portnum == 0) { //invalid port num
            com.objs.surveyor.probemeister.Log.out.warning("Error creating Probe_InstallListener probe. Port not valid:"+port);        
            return null;
        }
        
        SimpleProbe sp=null;
        try {
            if (usePM) { //set up PM Collector now that the probe was installed successfully
                try {
//                        String vmname = null;
//                        try {
//                            if (bLoc.getClassMgr() != null)
//                                vmname = bLoc.getClassMgr().vmConnector().getName();
//                        } catch(Exception e){}
                        
                    if (com.objs.probemeister.LoggerManager.loggerMgr() != null) {
                        LoggerWindow lw = com.objs.probemeister.LoggerManager.loggerMgr().addLogger(port, logName, logger);
                        
                        //com.objs.probemeister.PMLogServer ls = 
                        PMLogServer server = PMLogServer.addServer(portnum, lw, logName);
                        if (!server.isAlive()) //then start it
                            server.start();
                        javax.swing.JOptionPane.showMessageDialog(null, "The PM Data Collector has been started", "Information", javax.swing.JOptionPane.ERROR_MESSAGE);		    
                    } else                        
                        javax.swing.JOptionPane.showMessageDialog(null, "Error: ListenerManager not found. Couldn't start collector.", "Information", javax.swing.JOptionPane.ERROR_MESSAGE);		    
                } catch (Exception e) {
                    javax.swing.JOptionPane.showMessageDialog(null, "The PM Data Collector could NOT be started", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);		    
                    com.objs.surveyor.probemeister.Log.out.warning("The PM Data Collector could NOT be started. Exception was "+e);                            	
                }
            }

            StatementList sl = BytecodeInsertionMgr.createStatementList(_loc);
            StatementFactory.createInitLoggerStmt(sl, _loc.getMethodObject(), host, portnum, logger, formatter);
            //Embed line that includes parameter so it can be referenced when displaying probe info
            //StatementFactory.createMetadataStmt(sl, "v:"+s1+s);
            String id = "CollectorInitPID"+idCount++;
            sp = new SimpleProbe(id, _desc, pt, sl, _loc);

            //Still need to generate a record of this action
            InstrumentationRecord ir = new InstrumentationRecord_Probe(id, _desc, pt, _loc, _params);
            //This could be a separate method call            
            if (BytecodeInsertionMgr.insertProbe(sp, ir)) {
                //register logger name
                try {
                    _loc.getClassMgr().vmConnector().getLoggerNames().addLoggerName(logger);
                } catch (Exception e) {
                    com.objs.surveyor.probemeister.Log.out.warning("Error adding logger name.");
                }
            }                
            com.objs.surveyor.probemeister.Log.out.fine("Probe_InstallListener: Added probe.");
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.warning("Error creating Probe_InstallListener probe.");
            e.printStackTrace();
            return null;
        }
        
        return sp;
    }
    
    
    /* probe rehydration - just add water */
    public ProbeInterface regenerateProbe(DehydratedProbe _dp) {
        return null;
    }

    //Return list of customized attr-values as result of call to customizeStub()
    //This in effect serializes the specific attrs used by the stub
    public java.util.Map getParamsMap() {

       java.util.Hashtable h = new java.util.Hashtable(4);
       h.put("Host", host);
       h.put("Port", port);
       h.put("Logger", logger);
       h.put("Formatter", formatter);
       String use = usePM ? "TRUE" : "FALSE" ;
       h.put("UsePMCollector", use);
       h.put("PMCollectorName", logName);
       return h;
    }
    
    //Set values from Map as if customizeStub was called
    //This in effect deserializes the specific attrs used by the stub
    public void setParamsMap(java.util.Map _map) {
        
        if (_map == null) {
            return;
        }

        host = (String)_map.get("Host");
        port = (String)_map.get("Port");
        try {
            portnum = Short.parseShort(port);
        } catch (Exception e) {
            portnum = 0;
        }
        logger = (String)_map.get("Logger");
        formatter = (String)_map.get("Formatter");
        String use = (String)_map.get("UsePMCollector");
        logName = (String)_map.get("PMCollectorName");
        usePM = Boolean.valueOf(use).booleanValue();
    }


    /* used to process encoded parameters for display to user. */
    public String[] prettyPrintParamList(String[] _params) {

        //The params will include all internal constant strings...
        //For this probe type, only the print string to be output 
        //will be in this list.
        if (_params==null) return null;
        String[] ps = new String[_params.length];
        
        for (int i=0; i<_params.length; i++) {
            ps[i]= "Probe prints: " + _params[i];
        }
        return ps;
    }

}