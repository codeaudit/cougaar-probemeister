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
import com.objs.surveyor.probemeister.instrumentation.InstrumentationRecord;
import com.objs.surveyor.probemeister.instrumentation.InstrumentationRecord_Stub;

/*
 * This is the probe stub skeleton. Create subclasses of this class and override
 * getNextID() and customizeStub() routines. Also create a static routine that instantiates
 * the single instance of this class. Override the prettyPrintParamList() routine if you 
 * add metadata parameters so they are processed properly.
 */
public abstract class Stub_BytecodeSkeleton  extends com.objs.surveyor.probemeister.bytecoder.BytecodeProbeType {
    
    protected int idCount=0;
    protected String name="**StubSkeleton-NameNotSet**"; //user visible name of ProbeType
    //protected static Stub_BytecodeSkeleton pt = null; //for access from outside classes
    protected Stub_BytecodeSkeleton myStub = null; //for access from parent
    protected String clsName="";
    protected String signature="";
    protected String stubDesc="";
    //Copy into each subclass so proper subtype is instantiated.
/*
    static {
        pt = new Stub_BytecodeSkeleton();
        idCount = 0;
        clsName = "";
    }
*/    
    //Not publicly accessible   
    /*
     *
     * @param _userVisibleStubname - Name of stub that the user will see - no spaces in this name
     * @param _clsName - Name of the subclass
     * @param _sig - Signature of the Stub being created to validate against the ProbePlug
     * @param _desc - User visible description of the stub
     *
     */
    protected Stub_BytecodeSkeleton(String _userVisibleStubname, String _clsName, String _sig, String _desc ) {
        this.clsName = _clsName;
        this.signature = _sig;
        this.stubDesc = _desc;
        this.name = _userVisibleStubname;
        
        com.objs.surveyor.probemeister.Log.out.fine("Stub_BytecodeSkeleton() called by: "+_clsName);
	}   
        
    /* Well clarified method name */
    public String getRegisteredProbeTypeName() {return name; }
    protected void setRegisteredProbeTypeName(String _name) { name = _name; }
    public String getName() {return name; }
    public String toString() {return name; }
    
    /* Override this method to return an instance of your stub. */
    public static ProbeType getStub() {return null;}
    
    public void register(com.objs.surveyor.probemeister.probe.ProbeCatalog _cat) {
        _cat.registerType(myStub,myStub.stubDesc,true,false,true,true,false);
    }
    
    /* 
     * Helps the user locate a ProbePlug to attach to this Stub.
     */
    public ProbePlugEntry findPlug(Location _loc) {                
  
        if (!(_loc instanceof BytecodeLocation)) {
            com.objs.surveyor.probemeister.Log.out.warning(clsName+": Incorrect Location type used for bytecode probe creation.");
            return null;
        }
        
        com.objs.surveyor.probemeister.Log.out.finest(clsName+": findPlug");

/////???System.out.println("******Stub_BytecodeSkeleton:: ProbePlug Catalog DISABLED ********");
/////if (true) return null;
        ProbePlugSelectorGUI gui = new ProbePlugSelectorGUI((java.awt.Frame)null);
        ProbePlugCatalog cat = ProbePlugCatalog.getCataloger();
        //Get all compatible plugs. The sig below was copied from StatementFactory.createStubProbe_ArgsCallStmt
        ProbePlugEntry[] pe  = cat.findCompatiblePlugs(signature);
        com.objs.surveyor.probemeister.Log.out.info("Looking for plugs for "+signature+". Found plugs ***** ===> "+pe.length);        
        if (pe != null && pe.length >0) {
            //if stub already has plug name, then see if it's in this list
            //if so, use it
////            this.
            gui.setPlugsList(pe);
        }
        else { //no plugs found so we cannot install this one. Notify user & return null.
        	javax.swing.JOptionPane.showMessageDialog(null, "No Compatible ProbePlugs found.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);		    
            return null;
        }
        //*********************************************
        //Present GUI to establish connection to a plug
        //*********************************************
        gui.setModal(true); // keep it around
        gui.setVisible(true); //show gui
        //After gui is hid again, control returns here...
        ProbePlugEntry plug = gui.getSelectedPlug();
        if (plug == null) { //no plug selected
        	javax.swing.JOptionPane.showMessageDialog(null, "No Probe Plug selected, cannot install stub.", "Notice", javax.swing.JOptionPane.INFORMATION_MESSAGE);		    
            return null;
        }
        
        return plug;
    }
        
    /* Method for probe creation. Create a probe and inserts it into the
     * specified location.
     */
    public ProbeInterface generateProbe(Location _loc) {                
        
        ProbePlugEntry plug = findPlug(_loc);
        if (plug == null) return null;
        
        SimpleStubProbe sp=null;
        try {
            BytecodeLocation bLoc = (BytecodeLocation)_loc;
            StatementList sl = BytecodeInsertionMgr.createStatementList(bLoc);

            //This method allows a subtype of this class to insert the actual bytecode
            //unique to this stub;
            String id = myStub.getNextID();
            boolean yes = customizeStub(sl, plug, bLoc, id);
            if (!yes) 
                return null;
                
///            StatementFactory.createProbeWithArgsCallStmt(sl, plug.getClassName(), plug.getMethodName(),
///                    bLoc.getMethodObject());
            //Embed line that includes parameter so it can be referenced when displaying probe info
            StatementFactory.createMetadataStmt(sl, "sc:"+plug.getClassName());
            StatementFactory.createMetadataStmt(sl, "sm:"+plug.getMethodName());
            sp = new SimpleStubProbe(id, myStub.stubDesc, myStub, sl, (BytecodeLocation)_loc);

            InstrumentationRecord ir = new InstrumentationRecord_Stub(id, myStub.stubDesc, myStub, bLoc, this.getParamsMap(), plug.getParamsMap());
            
            //This could be a separate method call
            if (BytecodeInsertionMgr.insertProbe(sp, ir))
                com.objs.surveyor.probemeister.Log.out.fine(clsName+": Added probe.");
            else
                com.objs.surveyor.probemeister.Log.out.warning("Probe Insertion Failed.");
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.warning(clsName+": Error creating probe...");
            com.objs.surveyor.probemeister.Log.out.warning("Stub Name: "+ myStub.getClass().getName());
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.WARNING,"Probe Creation Error", e);
            return null;
        }
        
        return sp;
    }

    /* Method for probe creation via deserialization. Create a probe and inserts it into the
     * specified location.
     *
     * _params contains the attributes generated using the customize() routine the first time
     * the Stub was created. This allows us to bypass any GUI since we already know the attribute
     * values needed for customization.
     */
    public ProbeInterface generateProbe(String _id, String _desc, BytecodeLocation _loc, java.util.Map _params, ProbePlugEntry _plug) {
        
        if (_plug == null) return null;
        
        SimpleStubProbe sp=null;
        try {
            BytecodeLocation bLoc = _loc;
            StatementList sl = BytecodeInsertionMgr.createStatementList(bLoc);

            //This method allows a subtype of this class to insert the actual bytecode
            //unique to this stub;
            customizeStub(sl, _plug, bLoc, _params, _id);
            
///            StatementFactory.createProbeWithArgsCallStmt(sl, plug.getClassName(), plug.getMethodName(),
///                    bLoc.getMethodObject());
            //Embed line that includes parameter so it can be referenced when displaying probe info
            StatementFactory.createMetadataStmt(sl, "sc:"+_plug.getClassName());
            StatementFactory.createMetadataStmt(sl, "sm:"+_plug.getMethodName());
            //String id = myStub.getNextID(); //gen new id - use ID that was passed in.
            sp = new SimpleStubProbe(_id, _desc, myStub, sl, bLoc);

            InstrumentationRecord ir = new InstrumentationRecord_Stub(_id, _desc, myStub, bLoc, this.getParamsMap(), _plug.getParamsMap());
            
            //This could be a separate method call
            if (BytecodeInsertionMgr.insertProbe(sp, ir))
                com.objs.surveyor.probemeister.Log.out.fine(clsName+": Added probe.");                
            else
                com.objs.surveyor.probemeister.Log.out.warning("Probe Insertion Failed.");
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.WARNING, clsName+": Error creating probe...", e);
            com.objs.surveyor.probemeister.Log.out.warning("Stub Name: "+ myStub.getClass().getName());
            return null;
        }
        
        return sp;
    }
    
    
    /*
     * Override this routine to produce a customized event ID
     */
    protected String getNextID() { return "StubSkeleton"+idCount++; }

    /*
     * Override this routine to add the stub-specific bytecode to the StatementList.
     * Use the StatementFactory to create the statements.
     */
    protected boolean customizeStub(StatementList _sl, ProbePlugEntry _plug, 
                            BytecodeLocation _bLoc, String _id)  
                            throws StatementListPreparedException { 
        //Customized code goes here in overriden subclass
        return true;
    }
    
    /* This method takes the customized attribute values. The map contains attr-value pairs
     * corresponding to the Map object generated via the getParamsMap() call
     *
     */
    protected void customizeStub(StatementList _sl, ProbePlugEntry _plug, 
                            BytecodeLocation _bLoc, java.util.Map _params, String _id)  throws StatementListPreparedException { 
        //Customized code goes here in overriden subclass
    }
    
    /* probe rehydration - just add water */
    public ProbeInterface regenerateProbe(DehydratedProbe _dp) {
        return null;
    }

    /* used to process encoded parameters for display to user. */
    public String[] prettyPrintParamList(String[] _params) {

        //The params will include all internal constant strings...
        if (_params==null) return null;
        String[] ps = new String[_params.length];
        
        for (int i=0; i<_params.length; i++) {
            if (_params[i].startsWith("sc:"))  //then what follows is the class name of the probe plug
                ps[i]= "This stub calls the probe plug class: "+_params[i].substring(3);
            else if (_params[i].startsWith("sm:"))  //then what follows is the method name of the probe plug
                ps[i]= "This stub calls the probe plug method: "+_params[i].substring(3);
            else
                ps[i] = _params[i] + " <unidentified param>";
        }
        return ps;
    }

    //Return list of customized attr-values as result of call to customizeStub()
    //This in effect serializes the specific attrs used by the stub
    public java.util.Map getParamsMap() {
        
       //e.g. java.util.Hashtable h = new java.util.Hashtable(1);
       //e.g. h.put("className", className);
       //e.g. return h;
       return null;
    }
    
    //Set values from Map as if customizeStub was called
    //This in effect deserializes the specific attrs used by the stub
    public void setParamsMap(java.util.Map _map) {
        
        if (_map == null) {
            return;
        }
        
        //e.g. className = (String)_map.get("className");
    }
    
}