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
import com.objs.surveyor.probemeister.probe.*;
import com.objs.surveyor.probemeister.instrumentation.InstrumentationRecord_Probe;
import com.objs.surveyor.probemeister.instrumentation.InstrumentationRecord;
import java.util.Hashtable;

/*
 * This is the probe type for calling a method via retrospection to eliminate early NoClassDefFound Exceptions
 */
public class CallMethodByNameProbeType  extends com.objs.surveyor.probemeister.bytecoder.BytecodeProbeType {
    
    int idCount;
    final String name="CallMethodByName"; //user visible name of ProbeType
    static CallMethodByNameProbeType pt = null;
    static {
        pt = new CallMethodByNameProbeType();
        pt.idCount = 0;
    }
    /* Return an instance of this class */
    public static ProbeType getStub() { return pt;}
    
    //Not publicly accessible   
    private CallMethodByNameProbeType() {
		//{{INIT_CONTROLS
		//}}
	}    
    
    /* Well clarified method name */
    public String getRegisteredProbeTypeName() {return name; }
    public String getName() {return name; }
    public String toString() {return name; }
    
    public void register(com.objs.surveyor.probemeister.probe.ProbeCatalog _cat) {

        _cat.registerType(this,"Calls a method via reflection",false,false,true,true,false);
    }
    
    /* Method for probe creation. Create a probe and inserts it into the
     * specified location.
     */
    public ProbeInterface generateProbe(Location _loc) {                
  
        if (!(_loc instanceof BytecodeLocation)) {
            com.objs.surveyor.probemeister.Log.out.warning("CallMethodByNameProbeType: Incorrect Location type used for bytecode probe creation.");
            return null;
        }
        
                    com.objs.surveyor.probemeister.Log.out.fine("CallMethodByNameProbeType: generateProbe");
              
        //Get the class name and method name from the user...

        String className = null;
        String methodName = null;
        
        Probe_GetClassNMethodGUI.gui().setVisible(true, "Calls the no-arg STATIC method via retrosp. w/exception handling");
        className = Probe_GetClassNMethodGUI.gui().getClassName();
        methodName = Probe_GetClassNMethodGUI.gui().getMethodName();
        int result = Probe_GetClassNMethodGUI.gui().getResult();
        boolean validateMeth = Probe_GetClassNMethodGUI.gui().validateMethod();
        
        if (result==0 || className == null || methodName == null || 
            className.length()==0 || methodName.length() == 0) {
        
    	    javax.swing.JOptionPane.showMessageDialog(null,"User cancelled or required fields not supplied.");		    
            return null;        
        }

        BytecodeLocation bLoc = (BytecodeLocation)_loc;        
        
        if (validateMeth) {
            if (!Bytecoder.classMethodExists(bLoc.classMgr.vmConnector(), className, methodName, true, true)) {
    	        javax.swing.JOptionPane.showMessageDialog(null,"The specified class and static method don't exist.");		    
                return null;        
            }
        }        
        
        SimpleProbe sp=null;
        try {
            StatementList sl = BytecodeInsertionMgr.createStatementList(bLoc);
        
            StatementFactory.create_CallByRetrospectionStmt(sl, bLoc.getMethodObject(),
                        className, methodName);        
            
            //Embed line that includes parameter so it can be referenced when displaying probe info
            String id = "CMBN"+idCount++;
            String desc = "This probe calls "+className+"."+methodName+"()";
            sp = new SimpleProbe(id, desc, pt, sl, bLoc);
            
            //Create hash to place parameters for IR
            Hashtable paramsList = new Hashtable(2);
            paramsList.put("classname", className);
            paramsList.put("methodname", methodName);
            InstrumentationRecord ir = new InstrumentationRecord_Probe(id, desc, pt, bLoc, paramsList);
            //This could be a separate method call            
            if (BytecodeInsertionMgr.insertProbe(sp, ir))
               com.objs.surveyor.probemeister.Log.out.fine("CallMethodByNameProbeType: Added probe.");
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
        String s1 = "ProbeString:";
        String className  = (String)_params.get("className");
        String methodName = (String)_params.get("methodName");

        SimpleProbe sp=null;
        try {
            BytecodeLocation bLoc = (BytecodeLocation)_loc;
            StatementList sl = BytecodeInsertionMgr.createStatementList(bLoc);

            if (bLoc.getMethodObject()==null) return null;
            
            StatementFactory.create_CallByRetrospectionStmt(sl, bLoc.getMethodObject(),
                        className, methodName);        

            //Embed line that includes parameter so it can be referenced when displaying probe info
            //StatementFactory.createMetadataStmt(sl, "v:"+s1+s);
            String id = "CMBN"+idCount++;
            sp = new SimpleProbe(id, _desc, pt, sl, bLoc);

            //Still need to generate a record of this action
            InstrumentationRecord ir = new InstrumentationRecord_Probe(id, _desc, pt, bLoc, _params);
            //This could be a separate method call            
            BytecodeInsertionMgr.insertProbe(sp, ir);
            com.objs.surveyor.probemeister.Log.out.fine("CallMethodByNameProbeType: Added probe.");
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.WARNING, "Error creating probe.", e);
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