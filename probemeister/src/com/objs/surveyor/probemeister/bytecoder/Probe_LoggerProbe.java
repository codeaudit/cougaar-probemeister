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
import com.objs.surveyor.probemeister.instrumentation.InstrumentationRecord_Probe;
import com.objs.surveyor.probemeister.instrumentation.InstrumentationRecord;
import java.util.Hashtable;

/*
 * This is the probe type for setting up a Logger so probes emit messages to a remote location
 * Generally only to be installed once per VM, but nothing prohibits otherwise.
 */
public class Probe_LoggerProbe  extends com.objs.surveyor.probemeister.bytecoder.BytecodeProbeType {
    
    String logger=null;    
    String msg = null;
    String level = null;
    String emit = null;
    
    int idCount;
    final String name="Data Collector"; //user visible name of ProbeType
    static Probe_LoggerProbe pt = null;
    static {
        pt = new Probe_LoggerProbe();
        pt.idCount = 0;
    }
    /* Return an instance of this class */
    public static ProbeType getStub() { return pt;}
    
    //Not publicly accessible   
    private Probe_LoggerProbe() {
	}    
    
    /* Well clarified method name */
    public String getRegisteredProbeTypeName() {return name; }
    public String getName() {return name; }
    public String toString() {return name; }
    
    public void register(com.objs.surveyor.probemeister.probe.ProbeCatalog _cat) {

        _cat.registerType(this,"Emit Method Arguments via Logger",false,false,true,true,false);
    }
    
    /* Method for probe creation. Create a probe and inserts it into the
     * specified location.
     */
    public ProbeInterface generateProbe(Location _loc) {                
    
        if (!(_loc instanceof BytecodeLocation)) {
            com.objs.surveyor.probemeister.Log.out.warning("Probe_LoggerProbe: Incorrect Location type used for bytecode probe creation.");
            return null;
        }
        BytecodeLocation bLoc = (BytecodeLocation)_loc;
        
        com.objs.surveyor.probemeister.Log.out.fine("Probe_LoggerProbe: generateProbe");
              
              
        //Get data from the user...
        boolean innerClass = (bLoc.getClassName().indexOf('$')>0);
        boolean innerStatic =  bLoc.getClassType().isStatic();
        boolean methodStatic = bLoc.getMethod().isStatic();


        //Get current set of Logger names used in this VM
        java.util.Iterator iNames = null;
        try {
            iNames = bLoc.getClassMgr().vmConnector().getLoggerNames().getIterator();
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.warning("Error retrieving logger names.");
            //e.printStackTrace();
        }

        if (iNames != null)
            Probe_GetLoggerAttrsGUI.gui().setLoggerNames(iNames);        
        else {
        	javax.swing.JOptionPane.showMessageDialog(null, "No Loggers have been defined. Cannot add logger probe.", "Information", javax.swing.JOptionPane.INFORMATION_MESSAGE);		    
        	return null;
        }
        Probe_GetLoggerAttrsGUI.gui().enableOuterThis(!methodStatic && innerClass && !innerStatic);
        //If not an inner class & it's static then there is no THIS
        Probe_GetLoggerAttrsGUI.gui().enableThis(!methodStatic);
        //Probe_GetLoggerAttrsGUI.gui().setOuterThis(false);        
        Probe_GetLoggerAttrsGUI.gui().setByteOffset(bLoc.getOffset());
        Probe_GetLoggerAttrsGUI.gui().setVisible(true);

        if (Probe_GetLoggerAttrsGUI.gui().userCancelled())
            return null;
            
        //Get user input
        logger = Probe_GetLoggerAttrsGUI.gui().getLogger();        
        if (logger==null || logger.length()==0) {
            logger = "com.objs.ProbeMeister"; //use default
        	javax.swing.JOptionPane.showMessageDialog(null, "No Logger Name supplied, using default: "+logger, "Information", javax.swing.JOptionPane.INFORMATION_MESSAGE);		    
        }

        msg = Probe_GetLoggerAttrsGUI.gui().getMsg();        
        level = Probe_GetLoggerAttrsGUI.gui().getLevel();                
        emit = Probe_GetLoggerAttrsGUI.gui().getAlsoEmit();              
        
        int offset = Probe_GetLoggerAttrsGUI.gui().getByteOffset();
        bLoc.setOffset(offset);
    	
        SimpleProbe sp=null;
        try {
            StatementList sl = BytecodeInsertionMgr.createStatementList(bLoc);
            if (sl == null) return null;
        
            if (emit == null) //just emit msg
                StatementFactory.createLoggerCallStmt(sl, level, msg, logger);
            else if (emit.equals("ARGS"))                
                StatementFactory.createLoggerWithArgsCallStmt(sl, 
                        bLoc.getMethodObject(), level, msg, logger); 
            else if (emit.equals("THIS")||emit.equals("OUTERTHIS")) {                
                boolean outerThis = false;
                if (emit.equals("OUTERTHIS")) 
                    outerThis = true;
                StatementFactory.createLoggerWithTHISCallStmt(sl, level, msg, logger, outerThis);
            }
            
            //Embed line that includes parameter so it can be referenced when displaying probe info
            //StatementFactory.createMetadataStmt(sl, "v:"+s1+s);
            String id = "CollectorPID"+idCount++;
            String desc = "Data Collector Probe";
            sp = new SimpleProbe(id, desc, pt, sl, bLoc);
            
            InstrumentationRecord ir = new InstrumentationRecord_Probe(id, desc, pt, bLoc, getParamsMap());
            //This could be a separate method call            
            if (BytecodeInsertionMgr.insertProbe(sp, ir)) {
                com.objs.surveyor.probemeister.Log.out.fine("Probe_LoggerProbe: Added probe.");
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
        
        SimpleProbe sp=null;
        try {
            StatementList sl = BytecodeInsertionMgr.createStatementList(_loc);
            
            if (emit == null) //just emit msg
                StatementFactory.createLoggerCallStmt(sl, level, msg, logger);
            else if (emit.equals("ARGS"))                
                StatementFactory.createLoggerWithArgsCallStmt(sl, 
                        _loc.getMethodObject(), level, msg, logger); 
            else if (emit.equals("THIS")||emit.equals("OUTERTHIS")) {                
                boolean outerThis = false;
                if (emit.equals("OUTERTHIS")) 
                    outerThis = true;
                StatementFactory.createLoggerWithTHISCallStmt(sl, level, msg, logger, outerThis);
            }

            //Embed line that includes parameter so it can be referenced when displaying probe info
            //StatementFactory.createMetadataStmt(sl, "v:"+s1+s);
            String id = "CollectorPID"+idCount++;
            sp = new SimpleProbe(id, _desc, pt, sl, _loc);

            //Still need to generate a record of this action
            InstrumentationRecord ir = new InstrumentationRecord_Probe(id, _desc, pt, _loc, _params);
            //This could be a separate method call            
            BytecodeInsertionMgr.insertProbe(sp, ir);
            com.objs.surveyor.probemeister.Log.out.fine("Probe_LoggerProbe: Added probe.");
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.warning("Error creating Probe_LoggerProbe probe.");
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

       java.util.Hashtable h = new java.util.Hashtable(1);
       if (logger != null)
           h.put("Logger", logger);
       h.put("Level", level);
       if (msg != null)
           h.put("Message", msg);
       if (emit != null)
           h.put("AlsoEmit", emit);

       return h;
    }
    
    //Set values from Map as if customizeStub was called
    //This in effect deserializes the specific attrs used by the stub
    public void setParamsMap(java.util.Map _map) {
        
        if (_map == null) {
            return;
        }
        
        logger = (String)_map.get("Logger");
        if (logger == null) logger="NULL PARAM";

        level  = (String)_map.get("Level");
        if (level == null) logger="SEVERE";

        msg    = (String)_map.get("Message");
        if (msg == null) logger="NULL PARAM";

        emit   = (String)_map.get("AlsoEmit");
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