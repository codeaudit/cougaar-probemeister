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

/*
 * This is a probe stub. It passes the arguments of the instrumented method to 
 * the selected ProbePlug.
 */
public class Stub_PassMethodArgs extends Stub_BytecodeSkeleton  {
 
    String name="**StubSkeleton-NameNotSet**"; //user visible name of ProbeType
    protected static Stub_PassMethodArgs stub = null;
    public static ProbeType getStub() {return stub;}
    
    static final String sig = "(Lcom/objs/surveyor/probemeister/probe/GenericArgumentArray;)V";
    
    static {
        stub = new Stub_PassMethodArgs("PassMethodArgsStub", "Stub_PassMethodArgs", sig,
                    "This stub probe passes the methods arguments to a plug");
    }
    
    /*
     *
     * @param _userVisibleStubname - Name of stub that the user will see - no spaces in this name
     * @param _clsName - Name of the subclass
     * @param _sig - Signature of the Stub being created to validate against the ProbePlug
     * @param _desc - User visible description of the stub
     *
     */
    protected Stub_PassMethodArgs(String _userVisibleStubname, String _clsName, String _sig, String _desc ) {
        super(_userVisibleStubname, _clsName, _sig, _desc );
        myStub = this; //assign value to super abstract class
	}    
                   
    /*
     * Override this routine to produce a customized event ID
     */
    protected String getNextID() { return "PassMethodArgsStub"+idCount++; }

    /*
     * Override this routine to add the stub-specific bytecode to the StatementList.
     * Use the StatementFactory to create the statements.
     */
    protected void customizeStub(StatementList _sl, ProbePlugEntry _plug, 
                            BytecodeLocation _bLoc) throws StatementListPreparedException { 
        //Customized code goes here in overriden subclass
        //Insert Statement to pass Method Args.
        StatementFactory.createStubProbe_ArgsCallStmt(_sl, _plug.getClassName(), _plug.getMethodName(),
                _bLoc.getMethodObject());
        
    }
    
    
    /* This method takes the customized attribute values. The map contains attr-value pairs
     * corresponding to the Map object generated via the getParamsMap() call
     *
     */
    protected void customizeStub(StatementList _sl, ProbePlugEntry _plug, 
                            BytecodeLocation _bLoc, java.util.Map _params)  throws StatementListPreparedException { 
        //Customized code goes here in overriden subclass
        
        //No params needed. Ignore _params arg (?)
        
        com.objs.surveyor.probemeister.Log.out.fine("Stub_PassMethodArgs::customizeStub...");
                
        StatementFactory.createStubProbe_ArgsCallStmt(_sl, _plug.getClassName(), _plug.getMethodName(), 
                _bLoc.getMethodObject());
        
    }
    
    
    
    /* Used to process encoded parameters for display to user. 
     * This stub has no additional parameters, so we don't overide it.
     */
//    public String[] prettyPrintParamList(String[] _params) {
//    }
}