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
 * Stub_PassMethodArgsThisAndString.java
 *
 * Created on February 10, 2003, 11:54 AM
 */

package com.objs.surveyor.probemeister.bytecoder;

import com.objs.surveyor.probemeister.probe.*;
import com.objs.surveyor.probemeister.TargetVMConnector;

/*
 * This is a probe stub. It passes the arguments of the instrumented method to 
 * the selected ProbePlug.
 */
public class Stub_PassMethodArgsThisAndString extends Stub_BytecodeSkeleton  {

    protected String name="**StubSkeleton-NameNotSet**"; //user visible name of ProbeType
    protected static Stub_PassMethodArgsThisAndString stub = null;
    public static ProbeType getStub() {return stub;}
    static final String stubName = "PassMethodArgsThisAndStringStub";
    
    static final String sig = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Thread;Lcom/objs/surveyor/probemeister/probe/GenericArgumentArray;Ljava/lang/Object;)V";
    
    String msg="";
    boolean outerThis = false; //default
    
    static {
        stub = new Stub_PassMethodArgsThisAndString(stubName, "Stub_PassMethodArgsThisAndString", sig,
                    "This stub probe passes the methods arguments, 'this', event type, subtype, a msg, and thread to a plug");
    }
    
    /*
     *
     * @param _userVisibleStubname - Name of stub that the user will see - no spaces in this name
     * @param _clsName - Name of the subclass
     * @param _sig - Signature of the Stub being created to validate against the ProbePlug
     * @param _desc - User visible description of the stub
     *
     */
    protected Stub_PassMethodArgsThisAndString(String _userVisibleStubname, String _clsName, String _sig, String _desc ) {
        super(_userVisibleStubname, _clsName, _sig, _desc );
        myStub = this; //assign value to super abstract class
	}    
                   
    /*
     * Override this routine to produce a customized event ID
     */
    protected String getNextID() { return "ArgsThisStub"+idCount++; }

    /*
     * Override this routine to add the stub-specific bytecode to the StatementList.
     * Use the StatementFactory to create the statements.
     */
    protected boolean customizeStub(StatementList _sl, ProbePlugEntry _plug, 
                                 BytecodeLocation _bLoc, String _id) 
                                 throws StatementListPreparedException { 
        //Customized code goes here in overriden subclass
        //Insert Statement to pass Method Args.
        
        //*********************************************
        //*** Ask user for event type, subtype, and msg.
        //**********************************************
        com.objs.surveyor.probemeister.Log.out.fine("Stub_PassMethodArgsThisAndString::customizeStub...");
        msg="";
        int offset = 0;
        
        boolean innerClass = (_bLoc.getClassName().indexOf('$')>0);
        boolean innerStatic =  _bLoc.getClassType().isStatic();
        boolean methodStatic = _bLoc.getMethod().isStatic();
        if (methodStatic) { //this is a static method!
        	javax.swing.JOptionPane.showMessageDialog(null, "You cannot use this probe in a static method (cannot emit 'this'). Cancelling request.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);		    
            return false; //user did not customize
        }

        Stub_GetUserStringAndThisGUI.gui().enableOuterThis(innerClass && !innerStatic);
        Stub_GetUserStringAndThisGUI.gui().setOuterThis(false);
        Stub_GetUserStringAndThisGUI.gui().setByteOffset(_bLoc.getOffset());
        Stub_GetUserStringAndThisGUI.gui().setVisible(true);
        
        msg = Stub_GetUserStringAndThisGUI.gui().getMsg();        
        if (msg==null || msg.length()==0) msg = "no message";
        offset = Stub_GetUserStringAndThisGUI.gui().getByteOffset();
        
        _bLoc.setOffset(offset);

        if (innerClass)
            outerThis = Stub_GetUserStringAndThisGUI.gui().getOuterThis();
        else
            outerThis = false;
        
        
        String vmName = "";
        try {
            TargetVMConnector vm = _bLoc.getClassMgr().vmConnector();
            vmName = vm.getName() +":"+ vm.getAddress();
        } catch (Exception e) {}

       
        StatementFactory.createStubProbe_ThisAndArgsCallWithMsgStmt(_sl, _plug.getClassName(), _plug.getMethodName(), 
                    _bLoc.getMethodObject(), msg, _id, stubName, vmName, outerThis);
        
        return true;
    }
    

    /* This method takes the customized attribute values. The map contains attr-value pairs
     * corresponding to the Map object generated via the getParamsMap() call
     *
     */
    protected void customizeStub(StatementList _sl, ProbePlugEntry _plug, 
                            BytecodeLocation _bLoc, java.util.Map _params,
                            String _id)  throws StatementListPreparedException { 
        //Customized code goes here in overriden subclass
        
        com.objs.surveyor.probemeister.Log.out.fine("Stub_PassMethodArgsThisAndString::customizeStub...");
        
        //*********************************************
        //*** Get the attrs from the params map
        //**********************************************
        setParamsMap(_params);
        
        String vmName = "";
        try {
            TargetVMConnector vm = _bLoc.getClassMgr().vmConnector();
            vmName = vm.getName() +":"+ vm.getAddress();
        } catch (Exception e) {}

        StatementFactory.createStubProbe_ThisAndArgsCallWithMsgStmt(_sl, _plug.getClassName(), _plug.getMethodName(), 
                    _bLoc.getMethodObject(), msg,
                    _id, stubName, vmName, outerThis);
/*        
System.out.println("******Stub_PassMethodArgsThisAndString");        
System.out.println("msg="+msg);        
System.out.println("vmName="+vmName);        
System.out.println("stubName="+stubName);        
System.out.println("plugClass="+_plug.getClassName());        
System.out.println("plugMeth="+_plug.getMethodName());   
System.out.println("bLoc Meth="+_bLoc.getMethod().name());        
System.out.println("******");        
*/
    }
    
    public java.util.Map getParamsMap() {

       java.util.Hashtable h = new java.util.Hashtable(1);
       h.put("msg", msg);
       h.put("outerThis", Boolean.toString(outerThis));
       return h;
    }
    
    //Set values from Map as if customizeStub was called
    //This in effect deserializes the specific attrs used by the stub
    public void setParamsMap(java.util.Map _map) {
        
        if (_map == null) {
            return;
        }
        
        msg = (String)_map.get("msg");
        String out = (String)_map.get("outerThis");
        outerThis = Boolean.valueOf(out).booleanValue();
    }
    
    /* Used to process encoded parameters for display to user. 
     * This stub has no additional parameters, so we don't overide it.
     */
//    public String[] prettyPrintParamList(String[] _params) {
//    }
}
