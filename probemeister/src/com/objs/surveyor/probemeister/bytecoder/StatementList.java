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

import org.apache.bcel.generic.*;
import com.objs.surveyor.probemeister.probe.ProbeInterface;

/*
 * Defines a StatementList which contains a list of statements
 * (a statement is a list of instructions). The StatementFactory 
 * produces statements.
 */
public final class StatementList {

    private ClassObject classObject;
//    private MethodObject method;
    private InstructionList il;
    private int stackMax = 0; // set to max required for any method call
    private boolean prepared = false; // set to true once the probe delimiter stmts have
                                       // been added.

    private java.util.Vector probeVars = new java.util.Vector(); // holds vars created in this StmtList
    public void addProbeVar(LocalVariableGen _o, StatementFactory _f) { if (_f != null) probeVars.add(_o);}
    public java.util.Vector getProbeVars() {return (java.util.Vector)probeVars.clone();} 
    
    public StatementList(ClassObject _clazz) throws CannotEditClassException { //, MethodObject _meth) {
        
        classObject = _clazz;
//        method = _meth;
        
        if (classObject.prepareToEdit()) { //ensure class has been prepared (e.g. parsed)
        
            il = new InstructionList();
    
        } else
            throw new CannotEditClassException("Cannot Prepare Class To Edit");
        
    
		//{{INIT_CONTROLS
		//}}
	}
    
    public ClassObject getClassObject() { return classObject; }

    public int length() {return il.size();}

    public ConstantPoolGen cp() { return classObject.getConstantPoolGen(); }

    /* 
     * Returns a copy of the InstructionList - modifications will not affect
     * the original list.
     */
    public InstructionList list() { return il.copy() ; } //return copy for security reasons
    /* 
     * Returns the InstructionList - modifications will affect
     * the original list.
     */
    InstructionList origlist() { return il; } //return copy for security reasons


    /*
     * Returns the boolean indicating if the code has been finalized for insertion
     */
    boolean isPrepared() { return prepared; }

    /*
     * Prepares the code for insertion. Must be called ONCE prior to insertion.
     */
    void prepare(ProbeInterface _pi) {
        if (!prepared)
            prepared = StatementFactory.prepareForInsertion(this, _pi);
    }

    /*
     * Called by the StatementFactory to set the state of the prepared attribute
     */
    void setPrepared(StatementFactory _sf, boolean v) {
        if (_sf != null)
            prepared = v;
    }

    /*
     * Called by the StatementFactory to set the positions of the instructions in the list
     */
    void setPositions(StatementFactory _sf) {
        if (_sf != null)
            this.il.setPositions();
    }
    //don't allow direct modification -- only the StatementFactory can pass itself,
    //so we know no other instance is calling this routine
    /*
     * Append instruction to list
     */
    InstructionHandle appendInstruction(Instruction ins, StatementFactory factory) { 
        if (factory != null)
            return il.append(ins); 
        else return null;
    } 

    //don't allow direct modification -- only the StatementFactory can pass itself,
    //so we know no other instance is calling this routine
    /*
     * Append branch instruction to list
     */
    InstructionHandle appendInstruction(BranchInstruction ins, StatementFactory factory) { 
        if (factory != null)
            return il.append(ins); 
        else return null;
    } 

    //don't allow direct modification -- only the StatementFactory can pass itself,
    //so we know no other instance is calling this routine
    /*
     * Append instruction list to list
     */
    InstructionHandle appendInstruction(InstructionList ins, StatementFactory factory) { 
        if (factory != null)
            return il.append(ins); 
        else return null;
    } //don't allow direct modification

    //don't allow direct modification -- only the StatementFactory can pass itself,
    //so we know no other instance is calling this routine
    /*
     * Append instruction list to list
     */
    InstructionHandle appendInstruction(CompoundInstruction ins, StatementFactory factory) { 
        if (factory != null)
            return il.append(ins); 
        else return null;
    } //don't allow direct modification

    //don't allow direct modification -- only the StatementFactory can pass itself,
    //so we know no other instance is calling this routine
    /*
     * Prepend instruction list to beginning of the list
     */
    void prependInstruction(InstructionList ins, StatementFactory factory) { 
        if (factory != null)
        il.insert(ins); 
    } //don't allow direct modification


    /*
     * Allows two lists two be appended, but only if they 
     * share the same class object.
     */
    public void appendSet(StatementList _sl) {

        if (_sl.getClassObject().equals(this.classObject))
            this.il.append(_sl.origlist());
    }

	//{{DECLARE_CONTROLS
	//}}
	
	void setStackMax(int _sm) { stackMax = _sm; }
	int  getStackMax()        { return stackMax;}


}