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
 * This is the probe type for inserting a string to print, and executing a return instruction.
 * Could be used to avoid execution of (sections of) a method.
 */
public class ReturnInstructionProbeType  extends com.objs.surveyor.probemeister.bytecoder.BytecodeProbeType {
    
    int idCount;
    final String name="ReturnInstruction"; //user visible name of ProbeType
    static ReturnInstructionProbeType pt = null;
    static {
        pt = new ReturnInstructionProbeType();
        pt.idCount = 0;
    }
    /* Return an instance of this class */
    public static ProbeType getStub() { return pt;}
    
    //Not publicly accessible   
    private ReturnInstructionProbeType() {
		//{{INIT_CONTROLS
		//}}
	}    
    
    /* Well clarified method name */
    public String getRegisteredProbeTypeName() {return name; }
    public String getName() {return name; }
    public String toString() {return name; }
    
    public void register(com.objs.surveyor.probemeister.probe.ProbeCatalog _cat) {

        _cat.registerType(this,"Prints a string & executes a return",false,false,true,true,false);
    }
    
    /* Method for probe creation. Create a probe and inserts it into the
     * specified location.
     */
    public ProbeInterface generateProbe(Location _loc) {                
  
        if (!(_loc instanceof BytecodeLocation)) {
            com.objs.surveyor.probemeister.Log.out.warning("ReturnInstructionProbeType: Incorrect Location type used for bytecode probe creation.");
            return null;
        }
        
        com.objs.surveyor.probemeister.Log.out.fine("ReturnInstructionProbeType: generateProbe");
              
        //Get a string from the user...
        String s2 = "ReturnInstructionProbeType:";

        com.objs.surveyor.probemeister.Log.out.fine("ReturnInstructionProbeType: Asking user for String:");

    	String s = javax.swing.JOptionPane.showInputDialog("Enter a string for this probe to print:");		    
    	
        System.out.println("**** ReturnInstructionProbeType s = "+s);
        
        
        if (s== null) return null; //user cancelled
        s = (s.length()==0) ? "" : s2+s;
        
        SimpleProbe sp=null;
        try {
            StatementList sl = BytecodeInsertionMgr.createStatementList((BytecodeLocation)_loc);
            if (sl == null) return null;
            StatementFactory.createReturnStmt(sl, s);
            //Embed line that includes parameter so it can be referenced when displaying probe info
            //StatementFactory.createMetadataStmt(sl, "v:"+s);
            String id = "RIPT"+idCount++;
            String desc = "This probe simply prints a string";
            sp = new SimpleProbe(id, desc, pt, sl, (BytecodeLocation)_loc);
            
            //Create hash to place parameters for IR
            Hashtable paramsList = new Hashtable(1);
            if (s.length()>0) 
                paramsList.put("PARAM1", s);
            InstrumentationRecord ir = new InstrumentationRecord_Probe(id, desc, pt, (BytecodeLocation)_loc, paramsList);
            //This could be a separate method call            
            if (BytecodeInsertionMgr.insertProbe(sp, ir)) {
                com.objs.surveyor.probemeister.Log.out.fine("ReturnInstructionProbeType: Added probe.");
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
  
        //Get print string from the map of params...
        String s2 = "ReturnInstructionProbeType:";
        String s = (String)_params.get("PARAM1");   
        s = (s==null) ? "" : s2+s;

        SimpleProbe sp=null;
        try {
            StatementList sl = BytecodeInsertionMgr.createStatementList(_loc);
            StatementFactory.createReturnStmt(sl, s);
            //Embed line that includes parameter so it can be referenced when displaying probe info
            //StatementFactory.createMetadataStmt(sl, "v:"+s);
            String id = "RIPT"+idCount++;
            sp = new SimpleProbe(id, _desc, pt, sl, _loc);

            //Still need to generate a record of this action
            InstrumentationRecord ir = new InstrumentationRecord_Probe(id, _desc, pt, _loc, _params);
            //This could be a separate method call            
            BytecodeInsertionMgr.insertProbe(sp, ir);
            com.objs.surveyor.probemeister.Log.out.fine("ReturnInstructionProbeType: Added probe.");
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.warning("Error creating probe.");
            e.printStackTrace();
            return null;
        }
        
        return sp;
    }
    
    
    /* probe rehydration - just add water */
    public ProbeInterface regenerateProbe(DehydratedProbe _dp) {
        return null;
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