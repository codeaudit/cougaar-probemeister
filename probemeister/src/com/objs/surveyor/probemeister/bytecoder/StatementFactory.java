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
 * Object Services & Consulting, Inc. All Rights Reserved.
 */

package com.objs.surveyor.probemeister.bytecoder;

import com.objs.surveyor.probemeister.probe.ProbeInterface;
import com.objs.surveyor.probemeister.probe.CallableProbeInterface;
import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.classfile.Method;
import java.util.Vector;
import java.util.Enumeration;

/*
 * Contains a set of methods to create commonly used java instructions.
 * Requires to first create a new StatementList from the MethodObject, then
 * new instructions can be added and eventually inserted into the method.
 */ 
public final class StatementFactory { //no subclassing for security reasons
    
    //*** The factory should provide common methods WITHOUT wrapping the probes.
    //*** Wrapping should be performed at insertion & be independent of the
    //*** probe type's payload -- this will ensure that even badly formed payloads
    //*** are properly wrapped & identifiable.
    
//static final 
    static String arrayClass = "com.objs.surveyor.probemeister.probe.GenericArgumentArray";
    InstructionFactory IFactory;
        
    //* This attr is the ptr to the only StatementFactory instance
    private static StatementFactory factory=null;
    //This instance is used as a means of validation when calling the StatementList.
    //No other code has access to this attribute value, nor the ability to create
    //a StatementFactory.
    static {
        factory = new StatementFactory();
    }
        
    private StatementFactory() {
    
    }

    /*
     * Prepare StatementList for insertion by adding probe delimiters & identification
     * Called by the MethodObject.
     */
    static boolean prepareForInsertion(StatementList _sl, ProbeInterface _pi) { //throws StatementListPreparedException {
        
        //Do nothing if already prepared
        if (_sl.isPrepared()) return true; //throw new StatementListPreparedException();
        
        //Ensure that this code does not include probe startMarker or endMarker delimiters
        boolean ok = StatementFactory.validateUnpreparedStatementList(_sl);

        InstructionList pre_ins = new InstructionList();
        pre_ins.append(new LDC(_sl.cp().addString(ProbeInterface.startMarker)));
        pre_ins.append(new POP());
        pre_ins.append(new LDC(_sl.cp().addString(ProbeInterface.probeIDMarker+_pi.getProbeID())));
        pre_ins.append(new POP());
        pre_ins.append(new LDC(_sl.cp().addString(ProbeInterface.probeDescMarker+_pi.getProbeDesc())));
        pre_ins.append(new POP());
        pre_ins.append(new LDC(_sl.cp().addString(ProbeInterface.probeTypeMarker+_pi.getProbeTypeStr())));
        pre_ins.append(new POP());
        
        
        InstructionList post_ins = new InstructionList();
        post_ins.append(new LDC(_sl.cp().addString(ProbeInterface.endMarker)));
        post_ins.append(new POP()); //pop an int-sized value
        
        _sl.prependInstruction(pre_ins, factory);
        _sl.appendInstruction(post_ins, factory);
        
        return true;
    }
    
    
    /*
     * This method adds a metadata value to the probe, such as an attribute-value pair.
     */
    public static void createMetadataStmt(StatementList _sl, String _val) throws StatementListPreparedException {
        if (_sl.isPrepared()) throw new StatementListPreparedException();        

        _sl.appendInstruction(new LDC(_sl.cp().addString(_val)), factory);
        _sl.appendInstruction(new POP(), factory);
    }    
    
    /*
     * Println Instruction. 
     *
     */
    public static void createPrintlnStmt(StatementList _sl, String _s) 
       throws StatementListPreparedException {
       
        if (_sl.isPrepared()) throw new StatementListPreparedException();
        
        int out     = _sl.cp().addFieldref("java.lang.System", "out", "Ljava/io/PrintStream;");
        int println = _sl.cp().addMethodref("java.io.PrintStream", "println", "(Ljava/lang/String;)V");    
        
        _sl.appendInstruction(new GETSTATIC(out), factory);
        _sl.appendInstruction(new PUSH(_sl.cp(), _s), factory);
        _sl.appendInstruction(new INVOKEVIRTUAL(println), factory);
        if (_sl.getStackMax()<2)
            _sl.setStackMax(2); //for 2 args required by print stmt

    }    

    
    /*
     * Return Instruction. 
     * Prints a string before executing the return instruction.
     * If the string length = 0, then it will not print it.
     */
    public static void createReturnStmt(StatementList _sl, String _s) 
       throws StatementListPreparedException {
       
        if (_sl.isPrepared()) throw new StatementListPreparedException();
        
        if (_s.length()>0) { //Print string if included
            int out     = _sl.cp().addFieldref("java.lang.System", "out", "Ljava/io/PrintStream;");
            int println = _sl.cp().addMethodref("java.io.PrintStream", "println", "(Ljava/lang/String;)V");    

            _sl.appendInstruction(new GETSTATIC(out), factory);
            _sl.appendInstruction(new PUSH(_sl.cp(), _s), factory);
            _sl.appendInstruction(new INVOKEVIRTUAL(println), factory);
            if (_sl.getStackMax()<2)
                _sl.setStackMax(2); //for 2 args required by print stmt
        }
        _sl.appendInstruction(new RETURN() , factory);

    }    
    

  
//****************************************************************************
//Need a stub that generates this call: EvtName, SubEvtName, Thread, ???    
//(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Thread;[[Ljava/lang/String;)V    


    
    /*
     * Create an array of all the method's arguments, then push a reference to
     * this new local variable structure on the stack.
     *
     * @param _sl The StatementList in which to insert the required bytecode
     * @param themethod The method being instrumented
     * @param _localVarGenVector holds any local variables defined within this method. 
     *          This is required so the variables can be scoped after the final instructions
     *          are added. The type of the objects in this vector are LocalVariableGen. Scoping
     *          requires calling setEnd(InstructionHandle) on each, where the InsH is the
     *          final instruction where the variable is referenced.
     */
    static void pushMethodArgsToStack(StatementList _sl, MethodObject _theMethod) 
        throws StatementListPreparedException {

        int setBool=-1;
        int setByte=-1;
        int setInt=-1;
        int setFloat=-1;
        int setLong=-1;
        int setDouble=-1;
        int setChar=-1;
        int setShort=-1;
        int setObject=-1;        
        int setArrayObject=-1;        
        int arrayClass_newMethod =-1;

        if (_sl.isPrepared()) throw new StatementListPreparedException();

        //Indicate that we are referencing the local vars & therefore they need to 
        //be updated.
        _theMethod.setProbeReferencesMethodArgs(true);

        //1. Get handle to this method, then
        //2. Assemble arguments, then
        //3. Construct call to method
  
        //***************************************************************************
        //Generate instructions to call the probe -- with arguments
        //***************************************************************************
        int argumentArrayMethod=-1;
        InstructionList vv = new InstructionList();
    
        //Get the argument TYPES
            //1. Create a MethodGen object, then we'll have access to the signature
 /////           MethodGen methodGen = new MethodGen(theMethod.getBytecodeMethod(), theMethod.getClassObject().name(), _sl.cp());
            MethodGen methodGen = _theMethod.getMethodGen();
                
            Type[] types = methodGen.getArgumentTypes();
            int numArgs = types.length; //get the # of arguments
            String[] argNames;
            try {
                argNames = methodGen.getArgumentNames();
            } catch (NullPointerException npe) {
                argNames = new String[types.length];
                for (int i=0; i<argNames.length; i++) 
                    argNames[i] = new String("arg"+i);
            }
//System.out.println("ARGUMENTS **** "+argNames);
                
        //Get the argument NAMES & TYPES  - put into "varNames"                
        if (numArgs == 0)  {
            //throw new InstrumentationException("No Local Variable Found!!");
            com.objs.surveyor.probemeister.Log.out.fine("This method ("+methodGen.getName()+") has no arguments to pass!! Passing null object");
            //return false;
        }
                                        
            //Define the handle to the new() method to create a new array object
            arrayClass_newMethod = _sl.cp().addMethodref(
                arrayClass, "<init>", "(I)V");

            //CREATE NEW ARRAY FOR THE ARGUMENTS TO BE PASSED
            // 1. First, we create the array object - by creating a call to the
            //    new() method of the array object & passing the nec. arguments
            InstructionHandle startInstruction = 
                vv.append(new NEW(_sl.cp().addClass(arrayClass)));
            vv.append(new DUP());
            vv.append(new BIPUSH((byte)numArgs)); 
            vv.append(new INVOKESPECIAL(arrayClass_newMethod));
            
            //Create a unique name for the local variable
            String varName = "GAArray"+_sl.length();        
            //Create a new local variable to hold this reference
            LocalVariableGen arrayClass_newMethodVar = 
                    methodGen.addLocalVariable(varName,
                                    new ObjectType(arrayClass),
                                    startInstruction,
                                    startInstruction); //set the endInstruction later                    
            vv.append(new ASTORE(arrayClass_newMethodVar.getIndex()));

            LocalVariableGen[] locals = methodGen.getLocalVariables();
                    
//for (int z=0; z<locals.length; z++) {
//    System.out.println("--------- Local var"+z+" = "+locals[z].getName());
//}
                    
                    
            //This for loop processes each method argument. It bascially calls
            //the appropriate setxxx() method in the arrayClass class passing it
            //the current argument being processed. In this way it builds up an
            //array of the arguments which is then passed to the probe.                    
            boolean itsStatic = methodGen.isStatic();
            for (int i = 0; i<numArgs; i++) { //"<=numArgs" to include this in register 0

                //Determine the index in the LocalVariableTable for this argument
                Type   argType  = types[i];
                int    argIndex = factory.findArgByPos(locals, i, itsStatic);
                String argName  = locals[argIndex].getName(); //methodGen.getArgName(i);
                //System.out.println("PUSHMETHODARGS: For arg"+i+" using var named = "+locals[argIndex].getName());                                         
                        
                //*** COMMON to all *** ==> push the argument array we're going to pass 
                vv.append(new ALOAD(arrayClass_newMethodVar.getIndex()));
                        
                        
                //If it's an array, pass as object and pass primitive type so it can be recast
                if (argType instanceof ArrayType) {

                    vv.append(new ALOAD(argIndex)); //aload - pass ref.
                    vv.append(new LDC(_sl.cp().addString(argType.toString()))); //pass the array type as a string
                    if (setArrayObject == -1)
                        setArrayObject = _sl.cp().addMethodref(
                            arrayClass, "set", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)V");
                    argumentArrayMethod = setArrayObject; 
                    com.objs.surveyor.probemeister.Log.out.finest("1");
                }

                if (argType instanceof ObjectType) { //arg's type is a class 
                    vv.append(new ALOAD(argIndex)); //aload - pass ref.
                    if (setObject == -1)
                        setObject = _sl.cp().addMethodref(
                            arrayClass, "set", "(Ljava/lang/Object;Ljava/lang/String;)V");
                    argumentArrayMethod = setObject;
                    com.objs.surveyor.probemeister.Log.out.finest("2");
                }
                if (argType instanceof BasicType) {
                    if (argType.equals(Type.INT)) {
                        vv.append(new ILOAD(argIndex));
                        if (setInt == -1)
                            setInt = _sl.cp().addMethodref(
                                arrayClass, "set", "(ILjava/lang/String;)V");
                        argumentArrayMethod = setInt;
                    com.objs.surveyor.probemeister.Log.out.finest("3");
                            
                    }
                    else if (argType.equals(Type.SHORT)) {
                        vv.append(new ILOAD(argIndex));
                        if (setShort == -1)
                            setShort = _sl.cp().addMethodref(
                                arrayClass, "set", "(SLjava/lang/String;)V");
                        argumentArrayMethod = setShort;
                        com.objs.surveyor.probemeister.Log.out.finest("4");
                        
                    }
                    else if (argType.equals(Type.CHAR)) {
                        vv.append(new ILOAD(argIndex));
                        if (setChar == -1)
                            setChar = _sl.cp().addMethodref(
                                arrayClass, "set", "(CLjava/lang/String;)V");
                        argumentArrayMethod = setChar;
                        com.objs.surveyor.probemeister.Log.out.finest("5");                    
                    }
                    else if (argType.equals(Type.DOUBLE)) {
                        vv.append(new DLOAD(argIndex));
                        if (setDouble == -1)
                            setDouble = _sl.cp().addMethodref(
                                arrayClass, "set", "(DLjava/lang/String;)V");
                        argumentArrayMethod = setDouble;
                        com.objs.surveyor.probemeister.Log.out.finest("6");
                    }
                    else if (argType.equals(Type.FLOAT)) {
                        vv.append(new FLOAD(argIndex));
                        if (setFloat == -1)
                            setFloat = _sl.cp().addMethodref(
                                arrayClass, "set", "(FLjava/lang/String;)V");
                        argumentArrayMethod = setFloat;
                        com.objs.surveyor.probemeister.Log.out.finest("7");                        
                    }
                    else if (argType.equals(Type.LONG)) {
                        vv.append(new LLOAD(argIndex));
                        if (setLong == -1)
                            setLong = _sl.cp().addMethodref(
                                arrayClass, "set", "(JLjava/lang/String;)V");
                        argumentArrayMethod = setLong;
                        com.objs.surveyor.probemeister.Log.out.finest("8");                        
                    }
                    else if (argType.equals(Type.BOOLEAN)) {
                        vv.append(new LLOAD(argIndex));
                        if (setBool == -1)
                            setBool = _sl.cp().addMethodref(
                                arrayClass, "set", "(ZLjava/lang/String;)V");                                
                        argumentArrayMethod = setBool;
                        com.objs.surveyor.probemeister.Log.out.finest("9");                        
                    }
                    else if (argType.equals(Type.BYTE)) {
                        vv.append(new ILOAD(argIndex)); 
                        if (setByte == -1)
                            setByte = _sl.cp().addMethodref(
                                arrayClass, "set", "(BLjava/lang/String;)V");                                
                        argumentArrayMethod = setByte;
                        com.objs.surveyor.probemeister.Log.out.finest("10");                        
                    }
                }
                        
                //*** COMMON to all *** => push the variable name
                        
                //Find name of this argument, if it exists, ow. pass NULL as name
                int varNameIndex = -1;
                LocalVariableTable lvt = methodGen.getLocalVariableTable(methodGen.getConstantPool());
                if (lvt != null) {
                    LocalVariable lv = lvt.getLocalVariable(argIndex); // hope this works
                    if (lv != null)
                        varNameIndex = lv.getNameIndex();
                }
                if (varNameIndex==-1) //then load NULL
                    vv.append(new ACONST_NULL());
                else { // load ref (index) to the argument's name
/////                    vv.append(new LDC(varNameIndex));
                    int argpos = _sl.cp().addString(argName);
                    vv.append(new LDC(argpos));
                }
                //Append bytecode to invoke array method to add argument to array
                vv.append(new INVOKEVIRTUAL(argumentArrayMethod));


            }        
                
            // *** Put the new JBCI_ArgumentArray object on the stack
            vv.append(new ALOAD(arrayClass_newMethodVar.getIndex())); //aload - load argument to pass (created array object)  
            //Update the StatementList
            _sl.appendInstruction(vv, factory);
            //Adjust Max # of args (the total # passed to create the array).
            if (_sl.getStackMax()<numArgs+3)
                _sl.setStackMax(numArgs+3);
//**NUMARGS OR NUMARGS +1 or + 3???                

            //Add the only local var, so it can be scoped later in MethodObject
            _sl.addProbeVar(arrayClass_newMethodVar, factory);
            
            return;

        }
        

    /*
     * This Statement generates the code necessary to call a ProbePlug with the
     * arguments of the probed method.
     *
     * The signature required of compatible ProbePlugs is: 
     *      (Lcom/objs/surveyor/probemeister/probe/GenericArgumentArray;)V
     * @param _sl The StatementList in which to insert the required bytecode
     * @param _theMethod The method being instrumented
     * @param _probePlugClassName The ProbePlug class being called
     * @param _probePlugMethodName The method in the ProbePlug class being invoked
     */
    public static void createStubProbe_ArgsCallStmt(StatementList _sl, String _probePlugClassName, 
                                    String _probePlugMethodName, MethodObject _theMethod)
    throws StatementListPreparedException {

        if (_sl.isPrepared()) throw new StatementListPreparedException();

  // *** Should look up & ensure that GenericArgumentArray is in the classpath of the target VM ***
        //IF NOT, define class in target VM on the fly
        
        com.objs.surveyor.probemeister.Log.out.fine("Instrumenting: Passing method's arguments to probe plug.");
        
        pushMethodArgsToStack(_sl, _theMethod);
        
        int probeMethod = _sl.cp().addMethodref(_probePlugClassName, _probePlugMethodName,
                        "(Lcom/objs/surveyor/probemeister/probe/GenericArgumentArray;)V");
        //Invoke call to PlugProbe. 
        _sl.appendInstruction( (new INVOKESTATIC(probeMethod)), factory); //invoke call to probe method

    }    



    /*
     * Create a set of statements to call the specified probe 
     * with no arguments passed into the method. Useful for coverage &
     * event triggering.
     *
     * @param _sl The StatementList in which to insert the required bytecode
     * @param _probePlugMethodName The method being instrumented
     * @param _probePlugClassName The class being instrumented
     */
    public static void createStubProbe_SimpleCallStmt(StatementList _sl, 
                                    String _probePlugClassName, 
                                    String _probePlugMethodName) 
        throws StatementListPreparedException {

        if (_sl.isPrepared()) throw new StatementListPreparedException();

        final int probeMethod = _sl.cp().addMethodref(_probePlugClassName, 
            _probePlugMethodName, "()V");
            
        _sl.appendInstruction((new INVOKESTATIC(probeMethod)), factory); //invoke call to probe method
    }    


    /*
     * Create a set of statements to call the specified probe 
     * with no arguments passed into the method. Useful for coverage &
     * event triggering. Calls using retrospection. Useful to eliminate
     * NoClassDefFound exceptions if the called class is loaded after
     * the class is loaded.
     *
     * @param _sl The StatementList in which to insert the required bytecode
     & @param _theMethod The method being instrumented
     * @param _methodName The method to call
     * @param _className  The class to call
     */
    public static void create_CallByRetrospectionStmt(StatementList _sl, 
                                    MethodObject _theMethod,
                                    String _className, 
                                    String _methodName) 
        throws StatementListPreparedException {

        if (_sl.isPrepared()) throw new StatementListPreparedException();

            //Define ref to static Class.forName method            
            int forName = _sl.cp().addMethodref("java.lang.Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;");    
            int getMethod = _sl.cp().addMethodref("java.lang.Class", "getDeclaredMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");    
            int invoke = _sl.cp().addMethodref("java.lang.reflect.Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");    

            //try {
            InstructionHandle trystart = _sl.appendInstruction(new PUSH(_sl.cp(), _className), factory);
            _sl.appendInstruction(new INVOKESTATIC(forName), factory); //class on stack
            _sl.appendInstruction(new PUSH(_sl.cp(), _methodName), factory); //push method name
            _sl.appendInstruction(new ACONST_NULL(), factory); //push null 2nd arg
            _sl.appendInstruction(new INVOKEVIRTUAL(getMethod), factory); //invoke
            _sl.appendInstruction(new ACONST_NULL(), factory); //push null 2nd arg
            _sl.appendInstruction(new ACONST_NULL(), factory); //push null 3nd arg
            _sl.appendInstruction(new INVOKEVIRTUAL(invoke), factory); //invoke
            _sl.appendInstruction(new POP(), factory); //invoke
                        
            GOTO go = new GOTO(null); //to jump over exception -- set to trystart just
                    //so we can add it... ow. an exception occurs.
            InstructionHandle tryend = _sl.appendInstruction((BranchInstruction)(go), factory); 
            //catch() {
            //Exception Handler code - just print error notice & return
            int out     = _sl.cp().addFieldref("java.lang.System", "out", "Ljava/io/PrintStream;");
            int println = _sl.cp().addMethodref("java.io.PrintStream", "println", "(Ljava/lang/String;)V");    
            int printStacktrace = _sl.cp().addMethodref("java.lang.Exception", "printStackTrace", "()V");    
            
            //Print stack trace of exception that just occurred. the exception object will
            //be on the stack right now.
            MethodGen methodGen = _theMethod.getMethodGen();
            LocalVariableGen exceptionVar = 
                    methodGen.addLocalVariable("e",
                                    new ObjectType("java.lang.Exception"),
                                    null,
                                    null); //set the Instruction boundaries later                    
            int excInd = exceptionVar.getIndex();
            
            
            String error = "Exception (Above) Invoking ProbeMeister's Breakpointer Class";
            InstructionHandle handleException = 
                _sl.appendInstruction(new ASTORE(excInd), factory);            
            exceptionVar.setStart(handleException);
            
            _sl.appendInstruction(new ALOAD(exceptionVar.getIndex()), factory);                        
            _sl.appendInstruction(new INVOKEVIRTUAL(printStacktrace), factory);            
            _sl.appendInstruction(new GETSTATIC(out), factory);
            _sl.appendInstruction(new PUSH(_sl.cp(), error), factory);
            _sl.appendInstruction(new INVOKEVIRTUAL(println), factory);            

            //Now create a No-Op instruction for the try to jump over if no exception occurred.
            NOP noop = new NOP();
            InstructionHandle jumpTo = _sl.appendInstruction(noop, factory);
            go.setTarget(jumpTo);            
            exceptionVar.setEnd(jumpTo);
            
            //Add exception handler entry for try stmt in StatementList
            methodGen.addExceptionHandler(trystart, tryend, handleException, (new ObjectType("java.lang.Exception")));
       
            if (_sl.getStackMax()<3)
                _sl.setStackMax(3); //for 3 args required by invoke
                           
    }    


    /*
     * Create a set of statements to define a new logger with a SocketHandler.
     *
     * @param _sl The StatementList in which to insert the required bytecode
     & @param _theMethod The method being instrumented
     * @param _host The host socket ip address to send log reports to
     * @param _port  The port
     * @param _loggerName  The name of the logger 
     * @param _formatter A specialized java.util.logging.Formatter. Defaults to XMLFormatter if null.
     */
    public static void createInitLoggerStmt(StatementList _sl, MethodObject _theMethod,
                                            String _host, short _port, String _loggerName,
                                            String _formatter) 
        throws StatementListPreparedException {

        if (_sl.isPrepared()) throw new StatementListPreparedException();

            MethodGen methodGen = _theMethod.getMethodGen();
            
            //Define ref to static Class.forName method            
            int getLogger = _sl.cp().addMethodref("java.util.logging.Logger", "getLogger", "(Ljava/lang/String;)Ljava/util/logging/Logger;");    
            int useParentHandler = _sl.cp().addMethodref("java.util.logging.Logger", "setUseParentHandlers", "(Z)V");    
///            int socketHandler = _sl.cp().addMethodref("java.util.logging.SocketHandler", "SocketHandler", "(Ljava/lang/String;I)Ljava/util/logging/SocketHandler;");    
            int socketHandler = _sl.cp().addMethodref("java.util.logging.SocketHandler", "<init>", "(Ljava/lang/String;I)V");    
            int addHandler = _sl.cp().addMethodref("java.util.logging.Logger", "addHandler", "(Ljava/util/logging/Handler;)V");    

            //try {

            //Create SocketHandler variable
            LocalVariableGen socketHandlerVarGen = 
                    methodGen.addLocalVariable("OBJS_socketHandler",
                                    new ObjectType("java.util.logging.SocketHandler"),
                                    null,
                                    null); //set the end Instruction boundary later     
            int socketHandlerVar = socketHandlerVarGen.getIndex();
            
            //Create socket handler
            InstructionHandle trystart = 
            _sl.appendInstruction(new NEW(_sl.cp().addClass("java.util.logging.SocketHandler")), factory);
            _sl.appendInstruction(new DUP(), factory);
            pushStringOnStack(_sl, _host); //push host arg
            _sl.appendInstruction(new SIPUSH(_port), factory); //push port arg
            _sl.appendInstruction(new INVOKESPECIAL(socketHandler), factory); //create new instance 
            _sl.appendInstruction(new ASTORE(socketHandlerVar), factory); //store ref to new instance
            
            if (_formatter != null && _formatter.length()>0) { //then add a custom formatter
                //do we want to verify that the formatter class actually exists first?
               int newFormatter = _sl.cp().addMethodref(_formatter, "<init>", "()V");    
               int setFormatter = _sl.cp().addMethodref("java.util.logging.SocketHandler", "setFormatter", "(Ljava/util/logging/Formatter;)V");    
                                             
                _sl.appendInstruction(new ALOAD(socketHandlerVar), factory);
                _sl.appendInstruction(new NEW(_sl.cp().addClass(_formatter)), factory);
                _sl.appendInstruction(new DUP(), factory);
                _sl.appendInstruction(new INVOKESPECIAL(newFormatter), factory); //create new instance 
                _sl.appendInstruction(new INVOKEVIRTUAL(setFormatter), factory); //invoke setFormatter()
            }                

            pushStringOnStack(_sl, _loggerName);
            _sl.appendInstruction(new INVOKESTATIC(getLogger), factory); //class on stack
            _sl.appendInstruction(new ALOAD(socketHandlerVar), factory);
            _sl.appendInstruction(new INVOKEVIRTUAL(addHandler), factory); //invoke

            //set useParentHandler = false
            _sl.appendInstruction(new LDC(_sl.cp().addString(_loggerName)), factory);
            _sl.appendInstruction(new INVOKESTATIC(getLogger), factory); //get logger
            _sl.appendInstruction(new ICONST(0), factory); //push false arg
            _sl.appendInstruction(new INVOKEVIRTUAL(useParentHandler), factory); 
                     
                     
            GOTO go = new GOTO(null); //to jump over exception -- set to trystart just
                    //so we can add it... ow. an exception occurs.
            InstructionHandle tryend = _sl.appendInstruction((BranchInstruction)(go), factory); 

            //Set socketHandler var boundaries
            socketHandlerVarGen.setStart(trystart);
            socketHandlerVarGen.setEnd(tryend);
            
            //catch() {
            //Exception Handler code - just print error notice & return
            int out     = _sl.cp().addFieldref("java.lang.System", "out", "Ljava/io/PrintStream;");
            int println = _sl.cp().addMethodref("java.io.PrintStream", "println", "(Ljava/lang/String;)V");    
            int printStacktrace = _sl.cp().addMethodref("java.lang.Exception", "printStackTrace", "()V");    
            
            //Print stack trace of exception that just occurred. the exception object will
            //be on the stack right now.
            LocalVariableGen exceptionVar = 
                    methodGen.addLocalVariable("e",
                                    new ObjectType("java.lang.Exception"),
                                    null,
                                    null); //set the Instruction boundaries later                    
            int excInd = exceptionVar.getIndex();
            
            
            String error = "Exception Configuring JDK Logger...";
            InstructionHandle handleException = 
                _sl.appendInstruction(new ASTORE(excInd), factory);            
            exceptionVar.setStart(handleException);
            
            _sl.appendInstruction(new ALOAD(exceptionVar.getIndex()), factory);                        
            _sl.appendInstruction(new INVOKEVIRTUAL(printStacktrace), factory);            
            _sl.appendInstruction(new GETSTATIC(out), factory);
            _sl.appendInstruction(new PUSH(_sl.cp(), error), factory);
            _sl.appendInstruction(new INVOKEVIRTUAL(println), factory);            

            //Now create a No-Op instruction for the try to jump over if no exception occurred.
            NOP noop = new NOP();
            InstructionHandle jumpTo = _sl.appendInstruction(noop, factory);
            go.setTarget(jumpTo);            
            exceptionVar.setEnd(jumpTo);
            
            //Add exception handler entry for try stmt in StatementList
            methodGen.addExceptionHandler(trystart, tryend, handleException, (new ObjectType("java.lang.Exception")));
       
            if (_sl.getStackMax()<4)
                _sl.setStackMax(4); //for 3 args required by invoke
                           
    }    



    /*
     * Create a set of statements to invoke the basic Logger method log(Level, msg).
     * If the Level is not valid, "INFO" will be used.
     *
     * @param _sl The StatementList in which to insert the required bytecode
     * @param _level The uppercase log level name (see Level class)
     * @param _msg The message to send (method argument)
     * @param _loggerName The logger to send the log report to
     */
    public static void createLoggerCallStmt(StatementList _sl, 
                                    String _level, 
                                    String _msg,
                                    String _loggerName) 
        throws StatementListPreparedException {

        if (_sl.isPrepared()) throw new StatementListPreparedException();

        int logMethod = _sl.cp().addMethodref("java.util.logging.Logger", 
            "log", "(Ljava/util/logging/Level;Ljava/lang/String;)V");
        int getLogger = _sl.cp().addMethodref("java.util.logging.Logger", "getLogger", "(Ljava/lang/String;)Ljava/util/logging/Logger;");    
         
        String lev = getLogLevel(_level);
        int levelField = _sl.cp().addFieldref("java.util.logging.Level", lev, "Ljava/util/logging/Level;");
            
        pushStringOnStack(_sl, _loggerName);
        _sl.appendInstruction(new INVOKESTATIC(getLogger), factory); //class on stack

        _sl.appendInstruction(new GETSTATIC(levelField), factory);
        pushStringOnStack(_sl, _msg);
        _sl.appendInstruction((new INVOKEVIRTUAL(logMethod)), factory); //invoke call to probe method
        
        if (_sl.getStackMax()<3)
            _sl.setStackMax(3); //for 2 args required by print stmt
        
    }    

    /*
     * Create a set of statements to invoke the basic Logger method log(Level, msg, object).
     * If the Level is not valid, "INFO" will be used.
     * Will cause error if there is no outer this.
     *
     * @param _sl The StatementList in which to insert the required bytecode
     * @param _level The uppercase log level name (see Level class)
     * @param _msg The message to send (method argument)
     * @param _loggerName The logger to send the log report to
     * @param _innerThis Determines which THIS to push
     */
    public static void createLoggerWithTHISCallStmt(StatementList _sl, 
                                    String _level, 
                                    String _msg,
                                    String _loggerName,
                                    boolean _outerThis) 
        throws StatementListPreparedException {

        if (_sl.isPrepared()) throw new StatementListPreparedException();

        int logMethod = _sl.cp().addMethodref("java.util.logging.Logger", 
            "log", "(Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Object;)V");
        int getLogger = _sl.cp().addMethodref("java.util.logging.Logger", "getLogger", "(Ljava/lang/String;)Ljava/util/logging/Logger;");    
         
        //determine which Level to use
        String lev = getLogLevel(_level);
        int levelField = _sl.cp().addFieldref("java.util.logging.Level", lev, "Ljava/util/logging/Level;");

        pushStringOnStack(_sl, _loggerName);
        _sl.appendInstruction(new INVOKESTATIC(getLogger), factory); //class on stack

        _sl.appendInstruction(new GETSTATIC(levelField), factory);
        pushStringOnStack(_sl, _msg);
        if (!_outerThis)
            pushThisOnStack(_sl);
        else
            pushOuterThisOnStack(_sl);
            
        _sl.appendInstruction((new INVOKEVIRTUAL(logMethod)), factory); //invoke call to probe method
        
        if (_sl.getStackMax()<4)
            _sl.setStackMax(4); //for 2 args required by print stmt
        
    }    


    /* 
     * Ensures the string used is a valid logging level.
     * Returns "INFO" if not.
     */
    static String getLogLevel(String _level) {
        
        if (_level.equals("SEVERE")||_level.equals("WARNING")||
            _level.equals("CONFIG")||_level.equals("FINE")||
            _level.equals("FINER")||_level.equals("FINEST") )
            return _level;
        
        return "INFO";
    }

    /*
     * Call logger.log(level, msg, Object[]) where Object[] is the list of method args.
     * @param _sl The StatementList in which to insert the required bytecode
     * @param themethod The method being instrumented
     * @param _level The uppercase log level name (see Level class)
     * @param _msg The message to send (method argument)
     * @param _loggerName The logger to send the log report to
     */
    static void createLoggerWithArgsCallStmt(StatementList _sl, 
                    MethodObject _theMethod, String _level, String _msg, String _loggerName) 
        throws StatementListPreparedException {

        //handles
        int setArrayObject=-1;        
        int arrayClass_newMethod =-1;
        int int2String = -1;
        int c2String = -1;
        int f2String = -1;
        int j2String = -1;
        int d2String = -1;
        int z2String = -1;
        int logMethod = _sl.cp().addMethodref("java.util.logging.Logger", 
            "log", "(Ljava/util/logging/Level;Ljava/lang/String;[Ljava/lang/Object;)V");
        int getLogger = _sl.cp().addMethodref("java.util.logging.Logger", "getLogger", "(Ljava/lang/String;)Ljava/util/logging/Logger;");    
        
        
        if (_sl.isPrepared()) throw new StatementListPreparedException();

        //Indicate that we are referencing the local vars & therefore they need to 
        //be updated.
        _theMethod.setProbeReferencesMethodArgs(true);

        //1. Get handle to this method, then
        //2. Assemble arguments, then
        //3. Construct call to method
  
        //***************************************************************************
        //Generate instructions to call the probe -- with arguments
        //***************************************************************************
        int argumentArrayMethod=-1;
        InstructionList vv = new InstructionList();
    
        //Get the argument TYPES
            //1. Create a MethodGen object, then we'll have access to the signature
 /////           MethodGen methodGen = new MethodGen(theMethod.getBytecodeMethod(), theMethod.getClassObject().name(), _sl.cp());
            MethodGen methodGen = _theMethod.getMethodGen();
                
            Type[] types = methodGen.getArgumentTypes();
            int numArgs = types.length; //get the # of arguments
            String[] argNames;
            try {
                argNames = methodGen.getArgumentNames();
            } catch (NullPointerException npe) {
                argNames = new String[types.length];
                for (int i=0; i<argNames.length; i++) 
                    argNames[i] = new String("arg"+i);
            }
                
            //CREATE NEW ARRAY FOR THE ARGUMENTS TO BE PASSED
            // 1. First, we create the array object - by creating a call to the
            //    new() method of the array object & passing the nec. argume
            InstructionHandle startInstruction = 
                vv.append(new BIPUSH((byte)((numArgs*2)+2)));
                //Array format:
                //          "NUM_ARGS" 
                //          <value>
                //          [<argName>
                //          <value>]*

            vv.append(new ANEWARRAY(_sl.cp().addClass("java.lang.Object")));
            
            //(Try to) Create a unique name for the local variable
            String varName = "PMLogObjectArray"+_sl.length();        
            //Create a new local variable to hold this reference
            LocalVariableGen arrayClass_newMethodVar = 
                    methodGen.addLocalVariable(varName,
                                    new ObjectType("java.lang.Object"),
                                    startInstruction,
                                    startInstruction); //set the endInstruction later                    
            vv.append(new ASTORE(arrayClass_newMethodVar.getIndex()));

            LocalVariableGen[] locals = methodGen.getLocalVariables();
                    
//for (int z=0; z<locals.length; z++) {
//    System.out.println("--------- Local var"+z+" = "+locals[z].getName());
//}

            //First arg pair is NUM_ARGS and value
            
            vv.append(new ALOAD(arrayClass_newMethodVar.getIndex()));
            vv.append(new ICONST(0));
            vv.append(new LDC(_sl.cp().addString("NUM_ARGS")));
            vv.append(new AASTORE());

            vv.append(new ALOAD(arrayClass_newMethodVar.getIndex()));
            vv.append(new ICONST(1));
            vv.append(new LDC(_sl.cp().addString(""+numArgs)));
            vv.append(new AASTORE());
                    
            //This for loop processes each method argument. It bascially just
            //assigns slots for arg name & value. It also converts nonobjects 
            //to objects.
            boolean itsStatic = methodGen.isStatic();
            for (int i = 1; i<=numArgs; i++) { 

                //Determine the index in the LocalVariableTable for this argument
                Type   argType  = types[i-1];
                int    argIndex = factory.findArgByPos(locals, i-1, itsStatic);
                String argName  = locals[argIndex].getName(); //methodGen.getArgName(i);
                //System.out.println("PUSHMETHODARGS: For arg"+i+" using var named = "+locals[argIndex].getName());                                         

                //*** push the argument name
                vv.append(new ALOAD(arrayClass_newMethodVar.getIndex()));
                vv.append(new BIPUSH((byte)(i*2)));
                vv.append(new LDC(_sl.cp().addString(argName)));
                vv.append(new AASTORE());
                
                
                //*** Now push arg value                                
                vv.append(new ALOAD(arrayClass_newMethodVar.getIndex()));
                vv.append(new BIPUSH((byte)(i*2 +1)));
                        
                        
                //If it's an array, pass as object and pass primitive type so it can be recast
                if (argType instanceof ArrayType || argType instanceof ObjectType) {
                    vv.append(new ALOAD(argIndex)); //aload - pass ref.
                }
                else
                if (argType instanceof BasicType) {
                    if (argType.equals(Type.INT)||argType.equals(Type.SHORT)||argType.equals(Type.BYTE)) {
                        if (int2String == -1)
                            int2String = _sl.cp().addMethodref("java.lang.String", "valueOf", "(I)Ljava/lang/String;");    
                        vv.append(new ILOAD(argIndex));
                        vv.append(new INVOKESTATIC(int2String));
                    }
                    else if (argType.equals(Type.CHAR)) {
                        if (c2String == -1)
                            c2String = _sl.cp().addMethodref("java.lang.String", "valueOf", "(C)Ljava/lang/String;");    
                        vv.append(new ILOAD(argIndex));
                        vv.append(new INVOKESTATIC(c2String));
                    }
                    else if (argType.equals(Type.DOUBLE)) {
                        if (d2String == -1)
                            d2String = _sl.cp().addMethodref("java.lang.String", "valueOf", "(D)Ljava/lang/String;");    
                        vv.append(new DLOAD(argIndex));
                        vv.append(new INVOKESTATIC(d2String));
                    }
                    else if (argType.equals(Type.FLOAT)) {
                        if (f2String == -1)
                            f2String = _sl.cp().addMethodref("java.lang.String", "valueOf", "(F)Ljava/lang/String;");    
                        vv.append(new FLOAD(argIndex));
                        vv.append(new INVOKESTATIC(f2String));
                    }
                    else if (argType.equals(Type.LONG)) {
                        if (j2String == -1)
                            j2String = _sl.cp().addMethodref("java.lang.String", "valueOf", "(J)Ljava/lang/String;");    
                        vv.append(new LLOAD(argIndex));
                        vv.append(new INVOKESTATIC(j2String));
                    }
                    else if (argType.equals(Type.BOOLEAN)) {
                        if (z2String == -1)
                            z2String = _sl.cp().addMethodref("java.lang.String", "valueOf", "(Z)Ljava/lang/String;");    
                        vv.append(new LLOAD(argIndex));
                        vv.append(new INVOKESTATIC(z2String));
                    }
                }
                        
                //*** COMMON to all *** => Store the object in the array
                vv.append(new AASTORE());
            }        
                
            // *** Put the object[] on the stack to pass to the log method
            vv.append(new LDC(_sl.cp().addString(_loggerName)));
            vv.append(new INVOKESTATIC(getLogger)); //class on stack

            String lev = getLogLevel(_level);
            int levelField = _sl.cp().addFieldref("java.util.logging.Level", lev, "Ljava/util/logging/Level;");
            //LEVEL
            vv.append(new GETSTATIC(levelField));
            //MESSAGE
            vv.append(new LDC(_sl.cp().addString(_msg)));
            //OBJECT[]
            vv.append(new ALOAD(arrayClass_newMethodVar.getIndex())); //aload - load argument to pass (created array object)  
            //Invoke
            vv.append((new INVOKEVIRTUAL(logMethod))); //invoke call to probe method

            
            //Update the StatementList
            _sl.appendInstruction(vv, factory);
            //Adjust Max # of args (the total # passed to create the array).
            if (_sl.getStackMax()<5)
                _sl.setStackMax(5);

            //Add the only local var, so it can be scoped later in MethodObject
            _sl.addProbeVar(arrayClass_newMethodVar, factory);
            
            return;


    }




    /*
     * Create a set of statements to call the specified probe plug
     * with the event name, subevent name, and a message.
     *
     * @param _sl The StatementList in which to insert the required bytecode
     * @param _probePlugClassName  The class being called
     * @param _probePlugMethodName The method being called
     * @param _themethod The method being instrumented
     * @param _evtName EventName, apassed to the ProbePlug
     * @param _subEvt  Sub-EventName, passed to the ProbePlug
     * @param _msg A message passed to the ProbePlug
     * @param _probeID ID of probe stub
     * @param _stubName Name of probe stub
     * @param _vmName Name of VM
     *
     * ProbePlug should have the following signature:
     *  <b>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Thread;)V </b>
     */
    public static void createStubProbe_EventWithNoArgsCallStmt( StatementList _sl, String _probePlugClassName, 
                                    String _probePlugMethodName, 
                                    String _evtName, String _subEvt, String _msg, 
                                    String _probeID, String _stubName, String _vmName) 
        throws StatementListPreparedException {

        if (_sl.isPrepared()) throw new StatementListPreparedException();

        pushStringOnStack(_sl, _vmName);
        pushStringOnStack(_sl, _probeID);
        pushStringOnStack(_sl, _stubName);
        pushStringOnStack(_sl, _evtName); //push args
        pushStringOnStack(_sl, _subEvt);
        pushStringOnStack(_sl, _msg);
        pushThreadOnStack(_sl);

        final int probeMethod = _sl.cp().addMethodref(_probePlugClassName, 
            _probePlugMethodName, 
            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Thread;)V");

            _sl.appendInstruction((new INVOKESTATIC(probeMethod)), factory); //invoke call to probe method
            if (_sl.getStackMax()<9)
                _sl.setStackMax(9); //for 7 args + this invocation (class name, method name)
    }    

    /*
     * Create a set of statements to call the specified probe plug
     * with the event name, subevent name, message, Thread, and method arguments.
     *
     * @param _sl The StatementList in which to insert the required bytecode
     * @param _probePlugClassName  The class being called
     * @param _probePlugMethodName The method being called
     * @param _themethod The method being instrumented
     * @param _msg A message passed to the ProbePlug
     * @param _probeID ID of probe stub
     * @param _stubName Name of probe stub
     * @param _vmName Name of VM
     *
     * ProbePlug should have the following signature:
     *  <b>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Thread;Lcom/objs/surveyor/probemeister/probe/GenericArgumentArray;)V </b>
     */
    public static void createStubProbe_ArgsCallWithMsgStmt( 
                        StatementList _sl, 
                        String _probePlugClassName, 
                        String _probePlugMethodName, 
                        MethodObject _theMethod, 
                        String _msg, 
                        String _probeID, 
                        String _stubName, 
                        String _vmName) 
        throws StatementListPreparedException {

        if (_sl.isPrepared()) throw new StatementListPreparedException();

        pushStringOnStack(_sl, _vmName);
        pushStringOnStack(_sl, _probeID);
        pushStringOnStack(_sl, _stubName);
        pushStringOnStack(_sl, _theMethod.getBytecodeMethod().toString() );
        pushStringOnStack(_sl, _msg);
        pushThreadOnStack(_sl);
        Vector localVars = new Vector();
        pushMethodArgsToStack(_sl, _theMethod);

        final int probeMethod = _sl.cp().addMethodref(_probePlugClassName, 
            _probePlugMethodName, 
            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Thread;Lcom/objs/surveyor/probemeister/probe/GenericArgumentArray;)V");
        
        _sl.appendInstruction((new INVOKESTATIC(probeMethod)), factory); //invoke call to probe method
        _sl.setStackMax(10); //for 8 args required + this invocation (2)

    }    


    /*
     * Create a set of statements to call the specified probe plug
     * with the event name, subevent name, message, Thread, and method arguments.
     *
     * @param _sl The StatementList in which to insert the required bytecode
     * @param _probePlugClassName  The class being called
     * @param _probePlugMethodName The method being called
     * @param _themethod The method being instrumented
     * @param _msg A message passed to the ProbePlug
     * @param _probeID ID of probe stub
     * @param _stubName Name of probe stub
     * @param _vmName Name of VM
     *
     * ProbePlug should have the following signature:
     *  <b>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Thread;Lcom/objs/surveyor/probemeister/probe/GenericArgumentArray;Ljava/lang/Object;)V </b>
     */
    public static void createStubProbe_ThisAndArgsCallWithMsgStmt( 
                        StatementList _sl, 
                        String _probePlugClassName, 
                        String _probePlugMethodName, 
                        MethodObject _theMethod, 
                        String _msg, 
                        String _probeID, 
                        String _stubName, 
                        String _vmName, 
                        boolean _outerThis) 
        throws StatementListPreparedException {

        if (_sl.isPrepared()) throw new StatementListPreparedException();

        pushStringOnStack(_sl, _vmName);
        pushStringOnStack(_sl, _probeID);
        pushStringOnStack(_sl, _stubName);
        pushStringOnStack(_sl, _theMethod.getBytecodeMethod().toString() );
        pushStringOnStack(_sl, _msg);
        pushThreadOnStack(_sl);
        Vector localVars = new Vector();
        pushMethodArgsToStack(_sl, _theMethod);
        if (_outerThis) 
            pushOuterThisOnStack(_sl);
        else //push current context 'this' (which may be the inner class 'this')
            pushThisOnStack(_sl);
        
        final int probeMethod = _sl.cp().addMethodref(_probePlugClassName, 
            _probePlugMethodName, 
            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Thread;Lcom/objs/surveyor/probemeister/probe/GenericArgumentArray;Ljava/lang/Object;)V");
        
        _sl.appendInstruction((new INVOKESTATIC(probeMethod)), factory); //invoke call to probe method
        _sl.setStackMax(10); //for 8 args required + this invocation (2)

    }    

    
    /*
     * Create a set of statements to call the specified probe plug
     * with the event name, subevent name, message, Thread, and method arguments.
     *
     * @param _sl The StatementList in which to insert the required bytecode
     * @param _probePlugClassName  The class being called
     * @param _probePlugMethodName The method being called
     * @param _themethod The method being instrumented
     * @param _evtName EventName, apassed to the ProbePlug
     * @param _subEvt  Sub-EventName, passed to the ProbePlug
     * @param _msg A message passed to the ProbePlug
     * @param _probeID ID of probe stub
     * @param _stubName Name of probe stub
     * @param _vmName Name of VM
     *
     * ProbePlug should have the following signature:
     *  <b>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Thread;Lcom/objs/surveyor/probemeister/probe/GenericArgumentArray;)V </b>
     */
    public static void createStubProbe_EventWithArgsCallStmt( StatementList _sl, String _probePlugClassName, 
                                    String _probePlugMethodName, MethodObject _theMethod, 
                                    String _evtName, String _subEvt, String _msg, 
                                    String _probeID, String _stubName, String _vmName) 
        throws StatementListPreparedException {

        if (_sl.isPrepared()) throw new StatementListPreparedException();

        pushStringOnStack(_sl, _vmName);
        pushStringOnStack(_sl, _probeID);
        pushStringOnStack(_sl, _stubName);
        pushStringOnStack(_sl, _evtName); //push args
        pushStringOnStack(_sl, _subEvt);
        pushStringOnStack(_sl, _msg);
        pushThreadOnStack(_sl);
        Vector localVars = new Vector();
        pushMethodArgsToStack(_sl, _theMethod);

        final int probeMethod = _sl.cp().addMethodref(_probePlugClassName, 
            _probePlugMethodName, 
            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Thread;Lcom/objs/surveyor/probemeister/probe/GenericArgumentArray;)V");
        
        _sl.appendInstruction((new INVOKESTATIC(probeMethod)), factory); //invoke call to probe method
        _sl.setStackMax(10); //for 8 args required + this invocation (2)

    }    


    public static void createStubProbe_EventWithObjectCallStmt( StatementList _sl, String _probePlugClassName, 
                                    String _probePlugMethodName, MethodObject _theMethod, 
                                    String _evtName, String _subEvt, String _msg,
                                    String _probeID, String _stubName, String _vmName) 
        throws StatementListPreparedException {

        createStubProbe_EventWithObjectCallStmt( _sl, _probePlugClassName, 
                                    _probePlugMethodName, _theMethod, 
                                    _evtName, _subEvt, _msg,
                                    _probeID, _stubName, _vmName, false); 

    }

    /*
     * Create a set of statements to call the specified probe plug
     * with the event name, subevent name, message, Thread, and method arguments.
     *
     * @param _sl The StatementList in which to insert the required bytecode
     * @param _probePlugClassName  The class being called
     * @param _probePlugMethodName The method being called
     * @param _themethod The method being instrumented
     * @param _evtName EventName, apassed to the ProbePlug
     * @param _subEvt  Sub-EventName, passed to the ProbePlug
     * @param _msg A message passed to the ProbePlug
     * @param _probeID ID of probe stub
     * @param _stubName Name of probe stub
     * @param _vmName Name of VM
     * @param _outerThis TRUE if inserting into inner class and you want to
     *         pass the outer 'this' instead
     *
     * ProbePlug should have the following signature:
     *  <b>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Thread;Lcom/objs/surveyor/probemeister/probe/GenericArgumentArray;)V </b>
     */
    public static void createStubProbe_EventWithObjectCallStmt( StatementList _sl, String _probePlugClassName, 
                                    String _probePlugMethodName, MethodObject _theMethod, 
                                    String _evtName, String _subEvt, String _msg,
                                    String _probeID, String _stubName, String _vmName, boolean _outerThis) 
        throws StatementListPreparedException {

        if (_sl.isPrepared()) throw new StatementListPreparedException();

        pushStringOnStack(_sl, _vmName);
        pushStringOnStack(_sl, _probeID);
        pushStringOnStack(_sl, _stubName);
        pushStringOnStack(_sl, _evtName); //push args
        pushStringOnStack(_sl, _subEvt);
        pushStringOnStack(_sl, _msg);
        pushThreadOnStack(_sl);
        if (_outerThis) 
            pushOuterThisOnStack(_sl);
        else //push current context 'this' (which may be the inner class 'this')
            pushThisOnStack(_sl);

        final int probeMethod = _sl.cp().addMethodref(_probePlugClassName, 
            _probePlugMethodName, 
            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Thread;Ljava/lang/Object;)V");
        
        _sl.appendInstruction((new INVOKESTATIC(probeMethod)), factory); //invoke call to probe method
        _sl.setStackMax(10); //for 8 args required + (2) this invocation

    }    



    /*
     * Create a statement to load the specified string onto the stack
     *
     * @param _sl The StatementList in which to insert the required bytecode
     * @param _string A string passed to the ProbePlug
     * @return Signature component of argument pushed on stack
     */
    public static String pushStringOnStack( StatementList _sl, String _string ) 
        throws StatementListPreparedException {

        if (_sl.isPrepared()) throw new StatementListPreparedException();
            
        _sl.appendInstruction(new LDC(_sl.cp().addString(_string)), factory); //push String
        
        return "Ljava/lang/String;";
    }    


    /*
     * Create a statement to load "this" on the stack
     *
     * @param _sl The StatementList in which to insert the required bytecode
     * @return Signature component of argument pushed on stack
     */
    public static String pushThisOnStack( StatementList _sl ) 
        throws StatementListPreparedException {

        if (_sl.isPrepared()) throw new StatementListPreparedException();
            
        _sl.appendInstruction(new ALOAD(0), factory); //push String
        
        return "Ljava/lang/Object;";
    }    

    /*
     * Create a statement to load "this" on the stack
     *
     * @param _sl The StatementList in which to insert the required bytecode
     * @return Signature component of argument pushed on stack
     */
    public static String pushOuterThisOnStack( StatementList _sl ) 
        throws StatementListPreparedException {

        if (_sl.isPrepared()) throw new StatementListPreparedException();
        _sl.appendInstruction(new ALOAD(0), factory); //Load 'this' ref
        _sl.appendInstruction(new GETFIELD(2), factory); //call getfield to push outer 'this'
        
        return "Ljava/lang/Object;";
    }    


    /*
     * Create and push an instance of Thread on the stack
     *
     * @param _sl The StatementList in which to insert the required bytecode
     * @return Signature component of argument pushed on stack
     */
    public static String pushThreadOnStack( StatementList _sl) 
        throws StatementListPreparedException {

        if (_sl.isPrepared()) throw new StatementListPreparedException();

        final int threadMethod = _sl.cp().addMethodref("java.lang.Thread", 
            "currentThread", "()Ljava/lang/Thread;");
            
        _sl.appendInstruction((new INVOKESTATIC(threadMethod)), factory); //get current thread, result put on stack for next call
        
        return "Ljava/lang/Thread;";
    }    


    /*
     * This method returns false if this StatementList is malformed. That is, it
     * contains probe delimiters internally, or isn't bounded by probe delimiters.
     */
    public static boolean validatePreparedStatementList(StatementList _sl) {
    
        return !(StatementFactory.improperUseOfDelimiters(_sl, true, true, false));
    }

    /*
     * This method returns false if this StatementList is malformed. That is, it
     * contains ANY probe delimiters.
     */
    public static boolean validateUnpreparedStatementList(StatementList _sl) {
    
        return !(StatementFactory.improperUseOfDelimiters(_sl, false, false, true));
    }


    /*
     * This method searches the StatementList for delimiters. It can search
     * for bounding delimiters, internal delimiters, or any delimiters.
     * _all should only be used on unprepared StatementLists, it doesn't make
     * sense to call it otherwise. If _all is TRUE, _bounding & _internal are
     * ignored.
     *
     * @return TRUE if the StatementList is malformed for the given check requested.
     * Set _bounding & _internal to check for properly formed & prepared probe.
     * Set _all to true to check (unprepared) probe for delimiters
     */
    private static boolean improperUseOfDelimiters(StatementList _sl, 
        boolean _bounding, boolean _internal, boolean _all) {
    
        //Search StatementList for probe delimiter strings
        InstructionList il = _sl.list(); //cloned list, cannot modify
        Instruction[] insList = il.getInstructions();

        //Initially set flag to false, indicating no problems found.
        boolean bounding = false;
        boolean internalAll = false;
        
        if (_bounding && !_all) {
            //sets boolean to TRUE only if problems were found
            bounding = !(StatementFactory.hasBoundingDelimiters(_sl, insList));
        }
        
        if (_internal || _all) {
            
            int startPos;
            int endPos;
            
            //Set bounds for instruction ptrs
            if (_internal & !_all) {
                startPos = 2;                //first internal instruction
                endPos = insList.length - 3; //last internal instruction            
            } else { //_all is true 
                startPos = 0;              // first instruction
                endPos = insList.length-1; //last instruction
            }
            
            Instruction ins;
            for (int i = startPos; i <= endPos; i++ ) {
                ins = insList[i];
                if (ins instanceof LDC) {
                    Object o = ((LDC)ins).getValue(_sl.cp());
                    if (o instanceof String) {
                        if ( ((String)o).equals(ProbeInterface.startMarker) || 
                            ((String)o).equals(ProbeInterface.endMarker) ) {
                            internalAll = true;
                            break;
                        }
                    }
                }
            }
            
         }
            
        return (bounding || internalAll);
    }


    
    /*
     *  This method ensures that the statementlist is bounded properly.
     *  Returns TRUE if start and end delimiters were found.
     */
    private static boolean hasBoundingDelimiters(StatementList _sl, Instruction[] _insList) {
    
        //Search StatementList for probe delimiter strings
        //Specifically, it checks the first and 2nd to last statement for probe delimiters
        Instruction[] insList = _insList;
        
        //Return false if the StatementList is too small to contain start&end delimiters
        if (insList.length < 4) return false;        
        
        boolean foundSM = false;
        boolean foundEM = false;
        Instruction ins;
        ins = insList[0];
        if (ins instanceof LDC) {
            Object o = ((LDC)ins).getValue(_sl.cp());
            if (o instanceof String) {
                if (! ((String)o).equals(ProbeInterface.startMarker)) {
                    foundSM = true;
                }
            }
        }
        ins = insList[insList.length-2];
        if (ins instanceof LDC) {
            Object o = ((LDC)ins).getValue(_sl.cp());
            if (o instanceof String) {
                if (! ((String)o).equals(ProbeInterface.endMarker) ) {
                    foundEM = true;
                }
            }
        }
        
        return (foundSM && foundEM);
    }


    
    public static void codeforinsertingprobe() {
/*
        //******************************************************************
        //*********************** Do the modification **********************
        //******************************************************************
         int where;
         
         //*** Get user-specified location -- extract from data Object ***
         int insertionPoint = data.getInsertionPt(); 
         if (insertionPoint == BasicInsertionInstrumentor.START_INSERTION_PT) 
            where=0;
         else if (insertionPoint == BasicInsertionInstrumentor.END_INSERTION_PT) 
            where=theMethod.getCode().ins.size()-1;
         else 
            where = insertionPoint; //assumes a specific byteCode line

        //*** Insert the new instructions ***
         theMethod.getCode().insertInstructionsAt( post_ins, where);
         theMethod.getCode().insertInstructionsAt( ins1, where);
         theMethod.getCode().insertInstructionsAt( pre_ins, where);

         // Rename the modified class
//        theMethod.getDeclaringClass().resetName("PrintInserterMod");

         // See the modified method
//        theMethod.print(System.out);

        // **Don't write - wait for user to save
        //theMethod.getDeclaringClass().write();
        
        System.out.println(">>Successfully Inserted the probe: "+probeClassName+"::"+probeClassMethod.getMethodName()+
                "into <"+theMethod.getShortName()+"> at line: "+ (new Integer(where)).intValue(), Print.D0);
        
        

         System.out.println(">>********Modifying ALL Exceptions with Probe *****<<", Print.D0);
         BT_ExceptionTableEntryVector exceptions = theMethod.getCode().exceptions;
         for (Enumeration e = exceptions.elements() ; e.hasMoreElements() ;) {  
             BT_ExceptionTableEntry entry = (BT_ExceptionTableEntry) e.nextElement();
             //Get name of theexception being caught
             String eName = entry.catchType.className();
             if (eName == null) eName = "<<NULL>>";
             //Get the instruction in the code for this exception
             BT_Ins eIns = entry.handlerTarget;
             
             //Get the instruction vector & look up this instruction's location
             BT_InsVector v = theMethod.getCode().ins;
             //Locate the instr.
             int loc = v.indexOf(eIns);
             if (loc <1) { //error, eh?
                 System.out.println(">>********Could not find exception "+eName+ " in Instruction Vector! ", Print.D0);
             } else {
                System.out.println(">>********Inserting probe into exception: "+eName, Print.D0);  
                theMethod.getCode().insertInstructionsAt( post_ins, loc+3);
                theMethod.getCode().insertInstructionsAt( pre_ins, loc+3);
             }
         }
        
        //Routines to look at:
            //InstructionList.setPositions()  - processes the list
        
        return true; //success
*/
    }
    
    
    private int findArg(LocalVariableGen[] table, String name) {
        int index=-1;
        for (int i = 0; i < table.length; i++) {            
            LocalVariableGen local = table[i];
            //if the names match then it must be an argument variable since their scope
            //the entire method -- and ergo it is not possible to define another var with this name
            if (local.getName().equals(name)) { // && local.getStart().getPosition()==0)
                index = i;
                break; //we found it
            }
        }
        return index;
    }

    /* This method locates the argument index by it's argument position in the parameter
     * list. If the method being instrumented is static, there is no "this" so the pos
     * is correct for indexing intot he LocalVarGen array. If the method is not static,
     * we need to increment the pos by 1 since "this" will be in pos=0;
     */
    private int findArgByPos(LocalVariableGen[] table, int pos, boolean staticMeth) {
        pos = staticMeth ? pos : pos+1; 
        return pos;
    }
    
 
    
    
//COPY of method here:
    /*
     * Create a set of statements to call the specified probe 
     * with all of the arguments passed into the method.
     *
     * @param _sl The StatementList in which to insert the required bytecode
     * @param themethod The method being instrumented
     * @param probe The probe to be called
     */
/*     
    public static void createStubProbeWithArgsCallStmt(StatementList _sl, String _probePlugClassName, 
                                    String _probePlugMethodName, MethodObject theMethod) 
        throws StatementListPreparedException {

        if (_sl.isPrepared()) throw new StatementListPreparedException();

        //??? OLD?
        //IFactory = new InstructionFactory(_sl.cp());

        //Call routine to init the variables used by this method.
        //This is done lazily at this point. Could o.w. be in static{} in the class.

        //The probe method to be called - 
        //1. Get handle to this method, then
        //2. Assemble arguments, then
        //3. Construct call to method
  
  //4. *** Should look up & ensure that GenericArgumentArray is in the classpath of the target VM ***
  
        final int probeMethod = _sl.cp().addMethodref(_probePlugClassName, _probePlugMethodName,
                        "(Lcom/objs/surveyor/probemeister/probe/GenericArgumentArray;)V");
            
        //CustomData data = (CustomData) theData;
                
        System.out.println("Instrumenting: Passing method's arguments to probe.");
                
                

        //***************************************************************************
        //Generate instructions to call the probe -- with arguments
        //***************************************************************************
        int argumentArrayMethod=-1;
        InstructionList vv = new InstructionList();
    
        //Get the argument TYPES
            //1. Create a MethodGen object, then we'll have access to the signature
 /////           MethodGen methodGen = new MethodGen(theMethod.getBytecodeMethod(), theMethod.getClassObject().name(), _sl.cp());
            MethodGen methodGen = theMethod.getMethodGen();
                
            Type[] types = methodGen.getArgTypes();
            int numArgs = types.length; //get the # of arguments
            String[] argNames;
            try {
                argNames = methodGen.getArgNames();
            } catch (NullPointerException npe) {
                argNames = new String[types.length];
                for (int i=0; i<argNames.length; i++) 
                    argNames[i] = new String("arg"+i);
            }

                
        //Get the argument NAMES & TYPES  - put into "varNames"                
        if (numArgs == 0)  {
            //throw new InstrumentationException("No Local Variable Found!!");
            System.out.println("This method ("+methodGen.getName()+") has no arguments to pass!! Passing null object");
            //return false;
        }
                                        
            //Define the handle to the new() method to create a new array object
            arrayClass_newMethod = _sl.cp().addMethodref(
                arrayClass, "<init>", "(I)V");

            //CREATE NEW ARRAY FOR THE ARGUMENTS TO BE PASSED
            // 1. First, we create the array object - by creating a call to the
            //    new() method of the array object & passing the nec. arguments
            InstructionHandle startInstruction = 
                vv.append(new NEW(_sl.cp().addClass(arrayClass)));
            vv.append(new DUP());
            vv.append(new ICONST(numArgs)); 
            vv.append(new INVOKESPECIAL(arrayClass_newMethod));
            //Create a new local variable to hold this reference
            LocalVariableGen arrayClass_newMethodVar = 
                    methodGen.addLocalVariable("myArray",
                                    new ObjectType(arrayClass),
                                    startInstruction,
                                    null); //set the endInstruction later                    
            vv.append(new ASTORE(arrayClass_newMethodVar.getIndex()));

            LocalVariableGen[] locals = methodGen.getLocalVariables();
                    
            //This for loop processes each method argument. It bascially calls
            //the appropriate setxxx() method in the arrayClass class passing it
            //the current argument being processed. In this way it builds up an
            //array of the arguments which is then passed to the probe.                    
            for (int i = 0; i<numArgs; i++) { //"<=numArgs" to include this in register 0

                //Determine the index in the LocalVariableTable for this argument
                Type   argType  = types[i];
                String argName  = methodGen.getArgName(i);
                int    argIndex = factory.findArg(locals, argName);
                        
                        
                //*** COMMON to all *** ==> push the argument array we're going to pass 
                vv.append(new ALOAD(arrayClass_newMethodVar.getIndex()));
                        
                        
                //If it's an array, pass as object and pass primitive type so it can be recast
                if (argType instanceof ArrayType) {

                    vv.append(new ALOAD(argIndex)); //aload - pass ref.
                    vv.append(new LDC(_sl.cp().addString(argType.toString()))); //pass the array type as a string
                    if (setArrayObject == -1)
                        setArrayObject = _sl.cp().addMethodref(
                            arrayClass, "set", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)V");
                    argumentArrayMethod = setArrayObject; 
                    System.out.println("1");
                }

                if (argType instanceof ObjectType) { //arg's type is a class 
                    vv.append(new ALOAD(argIndex)); //aload - pass ref.
                    if (setObject == -1)
                        setObject = _sl.cp().addMethodref(
                            arrayClass, "set", "(Ljava/lang/Object;Ljava/lang/String;)V");
                    argumentArrayMethod = setObject;
                    System.out.println("2");
                }
                if (argType instanceof BasicType) {
                    if (argType.equals(Type.INT)) {
                        vv.append(new ILOAD(argIndex));
                        if (setInt == -1)
                            setInt = _sl.cp().addMethodref(
                                arrayClass, "set", "(ILjava/lang/String;)V");
                        argumentArrayMethod = setInt;
                    System.out.println("3");
                            
                    }
                    else if (argType.equals(Type.SHORT)) {
                        vv.append(new ILOAD(argIndex));
                        if (setShort == -1)
                            setShort = _sl.cp().addMethodref(
                                arrayClass, "set", "(SLjava/lang/String;)V");
                        argumentArrayMethod = setShort;
                        System.out.println("4");
                        
                    }
                    else if (argType.equals(Type.CHAR)) {
                        vv.append(new ILOAD(argIndex));
                        if (setChar == -1)
                            setChar = _sl.cp().addMethodref(
                                arrayClass, "set", "(CLjava/lang/String;)V");
                        argumentArrayMethod = setChar;
                        System.out.println("5");                    
                    }
                    else if (argType.equals(Type.DOUBLE)) {
                        vv.append(new DLOAD(argIndex));
                        if (setDouble == -1)
                            setDouble = _sl.cp().addMethodref(
                                arrayClass, "set", "(DLjava/lang/String;)V");
                        argumentArrayMethod = setDouble;
                        System.out.println("6");
                    }
                    else if (argType.equals(Type.FLOAT)) {
                        vv.append(new FLOAD(argIndex));
                        if (setFloat == -1)
                            setFloat = _sl.cp().addMethodref(
                                arrayClass, "set", "(FLjava/lang/String;)V");
                        argumentArrayMethod = setFloat;
                        System.out.println("7");                        
                    }
                    else if (argType.equals(Type.LONG)) {
                        vv.append(new LLOAD(argIndex));
                        if (setLong == -1)
                            setLong = _sl.cp().addMethodref(
                                arrayClass, "set", "(JLjava/lang/String;)V");
                        argumentArrayMethod = setLong;
                        System.out.println("8");                        
                    }
                    else if (argType.equals(Type.BOOLEAN)) {
                        vv.append(new LLOAD(argIndex));
                        if (setBool == -1)
                            setBool = _sl.cp().addMethodref(
                                arrayClass, "set", "(ZLjava/lang/String;)V");                                
                        argumentArrayMethod = setBool;
                        System.out.println("9");                        
                    }
                    else if (argType.equals(Type.BYTE)) {
                        vv.append(new ILOAD(argIndex)); 
                        if (setByte == -1)
                            setByte = _sl.cp().addMethodref(
                                arrayClass, "set", "(BLjava/lang/String;)V");                                
                        argumentArrayMethod = setByte;
                        System.out.println("10");                        
                    }
                }
                        
                //*** COMMON to all *** => push the variable name
                        
                //Find name of this argument, if it exists, ow. pass NULL as name
                int varNameIndex = -1;
                LocalVariableTable lvt = methodGen.getLocalVariableTable(methodGen.getConstantPool());
                if (lvt != null) {
                    LocalVariable lv = lvt.getLocalVariable(argIndex); // hope this works
                    if (lv != null)
                        varNameIndex = lv.getNameIndex();
                }
                if (varNameIndex==-1) //then load NULL
                    vv.append(new ACONST_NULL());
                else { // load ref (index) to the argument's name
/////                    vv.append(new LDC(varNameIndex));
                    int argpos = _sl.cp().addString(argName);
                    vv.append(new LDC(argpos));
                }
                //Append bytecode to invoke array method to add argument to array
                vv.append(new INVOKEVIRTUAL(argumentArrayMethod));
                        
                        
            }        
                
            // *** Add the call to the probe, passing in the new JBCI_ArgumentArray object
            vv.append(new ALOAD(arrayClass_newMethodVar.getIndex())); //aload - load argument to pass (created array object)
            InstructionHandle endInstruction = 
                vv.append(new INVOKESTATIC(probeMethod)); //invoke call to probe method
            //Set end instruction of where the array variable is valid/used
            //This will ensure that two probes' vars don't overlap in use
            arrayClass_newMethodVar.setEnd(endInstruction);                    


        //******************************************************************
        //These are the instructions BEFORE the calls to probeMethod(object)
        //******************************************************************
        //Insert the start probe marker, id, and comment

        _sl.appendInstruction(vv, factory);
        _sl.setStackMax(numArgs+1); //Max # of args passed to a method call.
        
    }    
*/
    
    
    
    
}