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
import java.util.Map;
import com.objs.surveyor.probemeister.instrumentation.InstrumentationRecord;
import com.objs.surveyor.probemeister.instrumentation.InstrumentationRecord_Probe;

/*
 * This is the probe type for inserting a string to print when invoked.
 */
public class ZTestProbeType  extends com.objs.surveyor.probemeister.bytecoder.BytecodeProbeType {
    
    int idCount;
    final String name="ZTestProbeType"; //user visible name of ProbeType
    static ZTestProbeType pt = null;
    static {
        pt = new ZTestProbeType();
        pt.idCount = 0;
    }
    
    public static ProbeType getStub() { return pt;}
    
    //Not publicly accessible   
    private ZTestProbeType() {
		//{{INIT_CONTROLS
		//}}
	}    
    
    /* Well clarified method name */
    public String getRegisteredProbeTypeName() {return name; }
    public String getName() {return name; }
    public String toString() {return name; }
    
    public void register(com.objs.surveyor.probemeister.probe.ProbeCatalog _cat) {

        _cat.registerType(this,"Calls the TestProbe print method",false,false,true,true,false);
    }
    
    /* Method for probe creation. Create a probe and inserts it into the
     * specified location.
     */
    public ProbeInterface generateProbe(Location _loc) {                
  
        if (!(_loc instanceof BytecodeLocation)) {
            System.out.println("ZTestProbeType: Incorrect Location type used for bytecode probe creation.");
            return null;
        }
        
        System.out.println("ZTestProbeType: generateProbe");
              
        //Get a string from the user...
        String s1 = "ProbeString:";

///        System.out.println("ZTestProbeType: Asking user for String:");

///    	String s = javax.swing.JOptionPane.showInputDialog("Enter a string for this probe to print:");		    
        SimpleProbe sp=null;
        try {
            StatementList sl = BytecodeInsertionMgr.createStatementList((BytecodeLocation)_loc);
            StatementFactory.createStubProbe_SimpleCallStmt(sl, "TestProbe", "print");
            //Embed line that includes parameter so it can be referenced when displaying probe info
            //StatementFactory.createMetadataStmt(sl, "v:"+s1+s);
            String id = "SP"+idCount++;
            String desc = "This probe calls TestProbe";
            sp = new SimpleProbe(id, desc, pt, sl, (BytecodeLocation)_loc);
            
            InstrumentationRecord ir = new InstrumentationRecord_Probe(id, desc, pt, (BytecodeLocation)_loc, (Map)null);

            //This could be a separate method call
            BytecodeInsertionMgr.insertProbe(sp, ir);
            System.out.println("ZTestProbeType: Added probe.");
        } catch (Exception e) {
            System.out.println("Error creating probe.");
            e.printStackTrace();
            return null;
        }
        
        return sp;
    }


    //Creates and inserts a probe (from InstrumentationRecord)
    public ProbeInterface generateProbe(String _id, String _desc, BytecodeLocation _loc, java.util.Map _params) {                
  
        System.out.println("ZTestProbeType: generateProbe");
        //Get a string from the user...
        String s1 = "ProbeString:";

        SimpleProbe sp=null;
        try {
            StatementList sl = BytecodeInsertionMgr.createStatementList(_loc);
            StatementFactory.createStubProbe_SimpleCallStmt(sl, "TestProbe", "print");
            //Embed line that includes parameter so it can be referenced when displaying probe info
            //StatementFactory.createMetadataStmt(sl, "v:"+s1+s);
            
            String id = "SP"+idCount++; //generate a new id, to ensure no replicates.
            sp = new SimpleProbe(id, _desc, pt, sl, _loc);

            //Still need to generate a record of this action
            InstrumentationRecord ir = new InstrumentationRecord_Probe(id, _desc, pt, _loc, (Map)null);
            //This could be a separate method call            
            BytecodeInsertionMgr.insertProbe(sp, ir);
            com.objs.surveyor.probemeister.Log.out.fine("PrintStringProbeType: Added probe.");
        } catch (Exception e) {
            System.out.println("Error creating probe.");
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