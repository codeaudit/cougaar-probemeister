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
import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;
import com.sun.jdi.ClassType;

import java.util.Vector;

//Created by ClassObject
public class MethodObject {
    
    ClassObject classObject;
    private org.apache.bcel.classfile.Method bcmethod;
    private InstructionList insList=null;
    com.sun.jdi.Method method;
    private MethodGen mGen = null;
    java.util.Vector dehydratedProbeList; //vector of DehydratedBytecodeProbe
    java.util.Vector probeList; //vector of ProbeInterface
    int indexPos;
    int probeCount=0;
 
    boolean adjustLocalVarsOffset = false; //set to true when inserted probes access the method's arguments
    /*Set to true when an inserted probe references local vars*/
    public void setProbeReferencesMethodArgs(boolean _v) {adjustLocalVarsOffset=_v;}
    
    protected MethodObject(ClassObject _clazz, com.sun.jdi.Method _meth, int _pos) {
    
        indexPos = _pos;
        classObject = _clazz;
        method   = _meth;
        bcmethod = classObject.getBytecodeMethodAtIndex(indexPos);

        
        dehydratedProbeList = new Vector();
        probeList = new Vector();
        //** Also generate probe count (& update probeCount value) on instantiation
        //==> Scan the method code for probes.
    }
    
    org.apache.bcel.classfile.Method getBytecodeMethod() { return bcmethod; }

    /* To properly update/modify the bytecode, a common method gen object must be used */
    MethodGen getMethodGen() { 
        if (mGen == null) {
            String clsName = this.classObject.getClassName();
            mGen = new MethodGen(bcmethod, clsName , classObject.getConstantPoolGen());
//////            mGen.stripAttributes(true); // don't use LocalVarTable - doesn't work with repeated 
                                        // use of a MethodGen object
        }
        return mGen; 
    }
     InstructionList getinsList() { 
        if (insList == null) {
//            System.out.println("---Got insList---");
            insList = getMethodGen().getInstructionList(); //should occur ONCE
        }
        return insList; 
    }
    void setinsList(InstructionList _il) { insList = _il;}
    
    /*
     * Called (by other methods) when a probe is added or removed from this method.
     * It updates the bcmethod var with the new bytecode, updates the ClassObject's
     * array of bytecode methods, and flags the modification by setting the 
     * bytecodeHasChanged boolean to true.
     */
    private void setBytecodeMethod(org.apache.bcel.classfile.Method _bc) {
        this.bcmethod = _bc;
        classObject.updateBytecodeMethodArray(this.bcmethod, this.indexPos);
        bytecodeHasChanged();
    }
 
    /*
     * Underlying bytecode of this method has changed. Clear out all
     * related instance attributes. This will ensure that they will be
     * updated when needed again.
     */
    void bytecodeHasChanged() {
        
        dehydratedProbeList = null;
    }
 
    /*
     * @return Vector of current probes in this method
     *
     */
    public Vector getDehydratedProbeList() throws CodeNotAccessibleException {
        try {
/////// try updating every time due to TargetLostExceptions
        //if (dehydratedProbeList == null) //update vector
                dehydratedProbeList = extractDehydratedProbes();
            return dehydratedProbeList;
        } catch (ProbeFormatErrorException pfee) {
            com.objs.surveyor.probemeister.Log.out.warning("Probe Format Exception in Method: "+this.method.name());  
            return new Vector(); //return empty vector
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.warning("Exception trying to extract probes in Method: "+this.method.name());  
//e.printStackTrace();
            return new Vector(); //return empty vector
        }
    }
     
    protected ClassObject getClassObject() { return classObject; }
        
    public java.util.List getLocalVars() {
        if (bcmethod != null) {
            LocalVariableTable lvt = bcmethod.getLocalVariableTable();
            if (lvt != null) {
                LocalVariable[] lvs = lvt.getLocalVariableTable();
                if (lvs != null) {
                    java.util.Vector v = new java.util.Vector(lvs.length);
                    for (int i=0; i<lvs.length; i++) 
                        v.add(lvs[i].getName());
                    return v;
                }
            }
        }
        java.util.Vector v2 = new java.util.Vector(1);
        v2.add("Cannot Access vars for "+ method.name());
        return v2;
    }
     
    /*
     * insertProbe should be public and insertStatementList should be private
     *
     */
    public boolean insertProbe(ProbeInterface _pi) throws DuplicateProbeException, 
                                                          UnsupportedFunctionException {

        //***At some point, could support insertion of source code probes too???***
        if (_pi.isBytecodeProbe()) {
            //Make sure that this probe does not already exist.
            try {
                if (findDehydratedProbe(_pi)!=null) // then don't add it again.
                    throw new DuplicateProbeException();
            } catch (CodeNotAccessibleException cna) {
                com.objs.surveyor.probemeister.Log.out.warning("Could not insert probe, code not available: "+this.getBytecodeMethod().getName());  
                return false;
            }
                    
            boolean in = this.insertStatementList(_pi, _pi.getBytecodeStmts(), _pi.getProbeLocation());
            if (in) { // then update probe mgmt metadata
                this.probeCount++;
                probeList.add(_pi);
                return true;
            } else
                return false;
        } else
            throw new UnsupportedFunctionException("Source Probe Insertion Not Supported.");
    }
    
    /*
     * removeProbe removes a probe from this method's bytecode
     *
     */
    public boolean removeProbe(ProbeInterface _pi) throws NoSuchProbeException, UnsupportedFunctionException {

        //***At some point, could support removal of source code probes too***
        if (_pi.isBytecodeProbe()) {
        
            //Make sure that this probe does exist in this method.
            DehydratedBytecodeProbe dProbe = null;
            try {
                dProbe = findDehydratedProbe(_pi);
            } catch (CodeNotAccessibleException cna) {
                com.objs.surveyor.probemeister.Log.out.warning("Could not remove probe, code not available: "+this.getBytecodeMethod().getName());  
                return false;
            }
            
            if (dProbe == null) // then we cannot remove it
                throw new NoSuchProbeException();

            boolean out = this.removeProbeBytecode(dProbe); //removal is by probe ID...
            if (out) {
                this.probeCount--;
                // REMOVE Probe from probeList vector
                probeList.remove(_pi);
                return true;
            } else 
                return false;
        } else
            throw new UnsupportedFunctionException("Source Probe Removal Not Supported.");
    }

    /*
     * removeProbe removes a probe from this method's bytecode
     *
     */
    public boolean removeProbe(DehydratedProbe _dProbe) throws NoSuchProbeException {
        
        if (! (_dProbe instanceof DehydratedBytecodeProbe)) {
            com.objs.surveyor.probemeister.Log.out.warning("Cannot remove source code probes at this time.");
            return false;            
        }
        boolean out = this.removeProbeBytecode((DehydratedBytecodeProbe)_dProbe); //removal is by probe ID...
        if (out) {
            this.probeCount--;
            // Do not remove Probe from dehydratedProbeList vector, the vector is auto-updated

            // REMOVE Probe from probeList vector, if one is there.
            ProbeInterface pi = this.findProbe((DehydratedBytecodeProbe)_dProbe);
            if (pi != null)
                probeList.remove(pi);
                
            return true;
        }
        return false;
    }

    
    /*
     * Removes the probe from this method.
     * Current removal is based upon probe ID (and probe type?)
     *
     */
    private boolean removeProbeBytecode(DehydratedBytecodeProbe _probe) {
        
        //remove bytecode from startmarker to endmarker + next instruction
        InstructionHandle start = _probe.getStartInsH();
        InstructionHandle end   = _probe.getEndInsH();
        InstructionHandle current = start;
        InstructionHandle next;
        String clsName = this.classObject.getClassName();
        //always create a new MethodGen for editing
/////        MethodGen           mg  = new MethodGen(bcmethod, clsName , classObject.getConstantPoolGen());
        MethodGen           mg  = getMethodGen();
        InstructionList     il  = mg.getInstructionList();
//        InstructionList     il  = getinsList();
        
        int count = 0; //keep track of deletions
        try {

            //See if any GOTOs pointed to our probe. If so, 
            //we need to redirect them to the next instruction
            //BEFORE we remove the probe.
            InstructionHandle redirectInsH = end.getNext();
            if (redirectInsH != null) {  //then there's a stmt after our probe,
                                         //so we need to redirect all GOTOs

                InstructionHandle[] ihs = il.getInstructionHandles();            
                for(int j=1; j < ihs.length; j++) {
                    Instruction currIns = ihs[j].getInstruction();
                    //Looking only for GOTO instructions (at this pt)
                    if(currIns instanceof GotoInstruction) {
                        GotoInstruction gotoIns = (GotoInstruction)currIns;
                        if (gotoIns.getTarget() == start) { //if it pts to the probe
                            gotoIns.setTarget(redirectInsH);    //redirect to next insH
                            System.out.println("***\n*** Redirected GOTO Ins\n***");
                        }
                    }
                }
            }
            
            //Remove any exceptions that were created/used by this probe            
            int startPos = start.getPosition();
            int endPos   = end.getPosition();
            com.objs.surveyor.probemeister.Log.out.finest("Looking for exceptions... start = "+startPos+" end = "+endPos);      
            CodeExceptionGen[] hs = mg.getExceptionHandlers();            
            if (hs != null) {
                for (int i = 0; i < hs.length; i++ ) {
                    CodeExceptionGen h = hs[i];
                    if (h.getStartPC().getPosition() >= startPos && h.getEndPC().getPosition()<=endPos) {
                        //System.out.println("    --->FOUND EXCEPTION!!  startPos = "+
                        //    h.getStartPC().getPosition()+"endPos = "+h.getEndPC().getPosition());
                        mg.removeExceptionHandler(h);
                    }
                }
            }
//***Look at mg.removeLocalVariable(LocalVariableGen l) 
            
//            il.delete(start.getInstruction(), end.getInstruction());
            il.delete(start, end);

/*            do {
                System.out.println("***Delete Count = "+count);
                next = current.getNext();
                System.out.println("***Deleting ih: "+next.toString());
                if (next == null) break; // should not happen...
                il.delete(current);
                count ++;
                current = next;
            } while (current != end);
            //il.delete(end.getNext());
            il.delete(end);
*/            
        } catch (TargetLostException tle) { //This SHOULD BE THROWN EVERY TIME...
            
           // System.out.println("***TargetLostException: Updating targets.***");
            
            InstructionHandle[] targets = tle.getTargets();
	        for(int i=0; i < targets.length; i++) {
	            InstructionTargeter[] targeters = targets[i].getTargeters();
             
	            for(int j=0; j < targeters.length; j++)
	                targeters[j].updateTarget(targets[i], null);
            }
        }
        com.objs.surveyor.probemeister.Log.out.info("***Probe removed. Updating method.***");

//        mg.removeLocalVariables(); //TEST
        il.setPositions(true);
        this.setinsList(il); //update list        
        mg.setInstructionList(il); 
//        mg.getInstructionList().setPositions(true);
//        mg.removeLineNumbers(); //TEST
        //update method       

        this.setBytecodeMethod(mg.getMethod());  
        //propagate changes to VM
        classObject.postUpdates(); 
//        il.dispose();      
        return true;

    }
    
    public boolean insertStatementList(ProbeInterface _pi, StatementList _sl, Location _loc) {
    
        //Make sure that the statementlist has been prepared for insertion
        if (! _sl.isPrepared() ) 
            _sl.prepare(_pi);
        
        if (bcmethod == null || _sl == null ) { // || _loc == null) {
            com.objs.surveyor.probemeister.Log.out.warning("Cannot insert stmts...");
            return false;
        }
        
//        Code   code  = bcmethod.getCode();
        int    flags = bcmethod.getAccessFlags();
        String name  = bcmethod.getName();
//        System.out.println("Method Before: "+code.toString());
                
                
        // Sanity check
        if(bcmethod.isNative() || bcmethod.isAbstract() || (getinsList() == null)) {
            com.objs.surveyor.probemeister.Log.out.warning("Method is native or abstract, cannot modify.");
            return false;
        }                
                
        String class_name = classObject.name();                
        BytecodeLocation bLoc = (BytecodeLocation)_loc;
        int offset = bLoc.getOffset();
        int aprepend = bLoc.getAprepend();
        //validate offset - make sure it is within method boundaries!
        //done below.
        
        
        //NO - always create a new MethodGen for editing
/////        MethodGen           mg  = new MethodGen(bcmethod, class_name, classObject.getConstantPoolGen());
        MethodGen           mg  = getMethodGen();
        InstructionList     il  = mg.getInstructionList();
//        InstructionList     il  = getinsList(); //reuse 
        InstructionHandle[] ihs = il.getInstructionHandles();
        InstructionHandle insertionPt = null;
     
        if (offset >= ihs.length) { //then insert at first line
            offset = 0;
            aprepend = BytecodeLocation.PREPEND;
            com.objs.surveyor.probemeister.Log.out.warning("Inserting probe: Bytecode instruction # is out of bounds ("+offset+">"+ihs.length+". Setting to 0.");
        }

        com.objs.surveyor.probemeister.Log.out.info("Inserting probe into "+class_name+"::"+name);

        if (offset == 0) { //then insert the probe as soon as possible (at start of method)
            if(name.equals("<init>")) { // First let the super or other constructor be called
                for(int j=1; j < ihs.length; j++) {
                    if(ihs[j].getInstruction() instanceof INVOKESPECIAL) {
                        insertionPt = il.append(ihs[j], _sl.origlist()); // Should check: method name == "<init>"
                        break;
                    }
                }
                com.objs.surveyor.probemeister.Log.out.finest("Method was <init>, inserted after initialization stmts");
            } else {
                if (aprepend == BytecodeLocation.PREPEND)
                    insertionPt = il.insert(ihs[0], _sl.origlist());
                else if (aprepend == BytecodeLocation.APPEND)
                    insertionPt = il.append(ihs[0], _sl.origlist());
                com.objs.surveyor.probemeister.Log.out.finest("Modified Method Object bytecode at method start");
            }
        } else //insert probe at end of method
        if (offset < 0) {
            InstructionHandle endInsH = il.getEnd();
            insertionPt = il.insert(endInsH, _sl.origlist());
            
            //If the last instruction is a return stmt, 
            //Redirect GOTOs that jump to that Return stmt
            //to jump to our probe.
            if (endInsH.getInstruction() instanceof ReturnInstruction) { 
                for(int j=1; j < ihs.length; j++) {
                    Instruction currIns = ihs[j].getInstruction();
                    //Looking only for GOTO instructions (at this pt)
                    if(currIns instanceof GotoInstruction) {
                        GotoInstruction gotoIns = (GotoInstruction)currIns;
                        if (gotoIns.getTarget() == endInsH) {
                            gotoIns.setTarget(insertionPt);
                            System.out.println("***\n*** Redirected GOTO Ins\n***");
                        }
                    }
                }
            }    
            com.objs.surveyor.probemeister.Log.out.finest("Modified Method Object bytecode at method end...");
        } else { //insert probe at user-specified instruction offset
            if (aprepend == BytecodeLocation.PREPEND)
                insertionPt = il.insert(ihs[offset], _sl.origlist());
            else if (aprepend == BytecodeLocation.APPEND)
                insertionPt = il.append(ihs[offset], _sl.origlist());
            com.objs.surveyor.probemeister.Log.out.finest("Modified Method Object bytecode at instruction "+offset);
        }
        
        
        /* Stack size must be at least as large as required.
        */
        int stackS = _sl.getStackMax();
        if(mg.getMaxStack() < stackS)
            mg.setMaxStack(stackS);
        //mg.setMaxLocals(mg.getLocalVariables().length+1);
        mg.setMaxLocals(mg.getMaxLocals());

        il.setPositions(true);
        mg.setInstructionList(il); //****TEST****
        this.setinsList(il); //update list
//        mg.getInstructionList().setPositions(true);
//        mg.removeLineNumbers();
//        Method mTemp = mg.getMethod();
        
        // Adjust LocalVariableTable offsets if nec.
///        if (this.adjustLocalVarsOffset) 
///DO it always so 'this' is adjusted properly
        adjustLocalVarBoundaries(mg, il.getStart(), il.getEnd());
        
        //Adjust Probe variable offsets
        adjustProbeVarBoundaries(mg, _sl, il.getStart(), il.getEnd());
        
        mg.removeLineNumbers();
        
//        com.objs.surveyor.probemeister.Log.out.finest("Method JUST AFTER After: "+ il.getByteCode()); // mTemp.getCode().toString());

        this.setBytecodeMethod(mg.getMethod());        
        
//        il.dispose(); // Reuse instruction list
        
        
        com.objs.surveyor.probemeister.Log.out.finest("->Finished modifying method object.");
        return true;
       
    
    }
    
    /*
     *  @returns the # of probes in this method
     *
     */
    int getProbeCount() {  
        //System.out.println("MethodObject::getProbeCount => returning '5'");
        return probeCount;
    }

    /* This method adjusts the PC range in which the method's arguments are valid.
     * Each time code is inserted, the start PC for each argument is advanced passed the 
     * end of the inserted range, so they are not accessible to the new code. This
     * method sets the start PC for every argument to the start instruction in the method.
     * This must be called after all modifications to the method are completed.
     */
    static public void adjustLocalVarBoundaries(MethodGen _mg, InstructionHandle _startIH, InstructionHandle _endIH) {
        
        boolean itsStatic = _mg.isStatic();
        
        int numArgs = _mg.getArgumentTypes().length;
        if (numArgs ==0 && itsStatic) return; // no args to modify & no 'this'
        
        com.objs.surveyor.probemeister.Log.out.finest("MethodObject::Adjusting local argument boundaries...");
                
        LocalVariableGen[] locals = _mg.getLocalVariables();
        
        //Increase count if not static method so 'this' is included.
        int endPos  = itsStatic ? numArgs : numArgs+1; 
        
        for (int x=0; x<endPos; x++) {
            locals[x].setStart(_startIH);
            locals[x].setEnd(_endIH);
        }
    }
    
    /* This method adjusts the PC range in which the method's arguments are valid.
     * Each time code is inserted, the start PC for each argument is advanced passed the 
     * end of the inserted range, so they are not accessible to the new code. This
     * method sets the start PC for every argument to the start instruction in the method.
     * This must be called after all modifications to the method are completed.
     */
    static public void adjustProbeVarBoundaries(MethodGen _mg, StatementList _sl, InstructionHandle _startIH, InstructionHandle _endIH) {

        
        //If no new probe vars, return
        //Otherwise, search instructionList for probe vars & adjust start & end
        LocalVariableGen[] locals = _mg.getLocalVariables();
        
        java.util.Enumeration e = _sl.getProbeVars().elements();
        while(e.hasMoreElements()) {
            LocalVariableGen lvg = (LocalVariableGen)e.nextElement();
            lvg.setEnd(_endIH);
            lvg.setStart(_startIH);
            com.objs.surveyor.probemeister.Log.out.finest("--------> Adjusting start/end pts of probe var: "+lvg);
        }
        
    }



    /*
     * Scans bytecode for all probes and creates a DehydratedBytecodeProbe
     * for each occurrence.
     *
     */
    java.util.Vector extractDehydratedProbes() 
        throws ProbeFormatErrorException, CodeNotAccessibleException {
        //vector of ProbeInterface
        
        boolean error     = false;
        boolean foundStart= false;
        boolean foundEnd  = false;
        boolean foundID   = false; 
        String idString = null;
        String descString = null;
        String typeString = null;
        InstructionHandle startMarkerIns = null;
        InstructionHandle endMarkerIns = null;
        //int startMarkerLoc = -1;        

        java.util.Vector probeLocs = new java.util.Vector(); //used to return results

        //get the code segment
//////        Code code = this.getBytecodeMethod().getCode();
//////       InstructionList ins = new InstructionList( code.getCode() ); 
        InstructionList ins = this.getinsList();
        if (ins == null) {
            com.objs.surveyor.probemeister.Log.out.warning("MethodObject ERROR: Code Segment is NULL!");
            //Thread.currentThread().dumpStack();
            throw new CodeNotAccessibleException("Code Segment in this method is Null");
//            return probeLocs;
        }
        //System.out.println("   ===Code length ="+ins.size() +"===");
        
        //put instructions (actually InstructionHandles) into enumeration
        java.util.Iterator insE = ins.iterator();
        
        InstructionHandle theOne = null;
        //int theOneLoc = -1;
        //int index=-1;
        
        java.util.Vector strVector = new java.util.Vector();
        
        //Look through the instructions to find the probe
        int ic=0;
        while (insE.hasNext()) {
            InstructionHandle currInsH = (InstructionHandle)insE.next();        
            Instruction currIns = currInsH.getInstruction();
            //index++; //index matches location in instruction array
            if (currIns instanceof LDC) {
                //System.out.println("***Found LDC.toString(): "+op.toString());
                Object oVal = ((LDC)currIns).getValue(this.classObject.cp);
                if (!(oVal instanceof String)) {
                    com.objs.surveyor.probemeister.Log.out.warning("LDC found, but not a string val: "+ oVal.toString());
                    continue;
                } //not what we're looking for
                String s1 = (String)oVal;
               // System.out.println("***at ByteIndex: "+currInsH.getPosition());
                
                if (s1.equals(ProbeInterface.startMarker)){ //see if this is the start marker
                    if (!foundStart && !foundEnd && !foundID) { 
                        //System.out.println("   ===Found Start Marker at "+currInsH.getPosition()+"===");
                        startMarkerIns = currInsH;
                        //startMarkerLoc = index;
                        foundStart=true;
                    } else { // this is a second start before an end marker!!
                        error = true;
                        com.objs.surveyor.probemeister.Log.out.warning("ERROR: Found end Marker or ID before a Start Marker!");
                        throw new ProbeFormatErrorException();
                    }
                } else if (s1.equals(ProbeInterface.endMarker)){ //see if this is the end marker
                    if (!foundEnd && foundStart && foundID) { 
                        //System.out.println("   ===Found End Marker at "+currInsH.getPosition()+"===");                        
                        foundEnd=true;
                        endMarkerIns = currInsH.getNext(); //the final POP that rids this marker.
                    } else { // this is a second start before an end marker!!
                        error = true;
                        com.objs.surveyor.probemeister.Log.out.warning("ERROR: Found End Marker before a Start Marker or ID Marker!");
                        throw new ProbeFormatErrorException();
                    } 
                } else if (s1.indexOf(ProbeInterface.probeIDMarker)>=0) {
                    if (foundStart && !foundEnd && !foundID) { //see if this is the correct probeID
                       // System.out.println("   ===Found Probe ID at "+currInsH.getPosition()+"===");
                        foundID = true;
                        idString = s1.substring(ProbeInterface.probeIDMarker.length());
                    } else { // this is a second start before an end marker!!
                        error = true;
                        com.objs.surveyor.probemeister.Log.out.warning("ERROR: Found ID Marker before a Start and End Marker!");
                        throw new ProbeFormatErrorException();
                    } 
                } else if (s1.indexOf(ProbeInterface.probeDescMarker)>=0) {
                        descString = s1.substring(ProbeInterface.probeDescMarker.length());
                } else if (s1.indexOf(ProbeInterface.probeTypeMarker)>=0) {
                        typeString = s1.substring(ProbeInterface.probeTypeMarker.length());
                } else { //add string to params list (it's up tp the ProbeType to interpret)
                    strVector.add(s1);
                    //System.out.println("Dehydrating===== found string: "+s1);
                }

                if (foundStart && foundEnd && foundID) { //then we can add the probe 
                    BytecodeLocation loc = new BytecodeLocation (classObject.getClassMgr() ,(ClassType)this.getClassObject().getVMReferenceType() , this.method , startMarkerIns.getPosition());
                    loc.setAprepend(0); //probe loc is AT that location
                    probeLocs.add(new DehydratedBytecodeProbe(this, startMarkerIns, endMarkerIns, 
                                            idString, descString, typeString, strVector, loc));       //to our list
                    //Reset flags and resume looking for next probe
                    foundStart = false;
                    foundEnd  = false;
                    foundID   = false; 
                    strVector = new Vector(); //clear out collected strings
                }
            }          
        }
        
        return probeLocs;
        
        
    }
    
    /*
     * Algorithm is simply based upon identifying probes by their ID
     * @return first probe with the specified ID.
     *
     */
    DehydratedBytecodeProbe findDehydratedProbe(ProbeInterface _pi) 
    throws CodeNotAccessibleException {
        Vector plist = this.getDehydratedProbeList();        
        java.util.Enumeration e = plist.elements();
        while (e.hasMoreElements()) {
            DehydratedBytecodeProbe p = (DehydratedBytecodeProbe)e.nextElement();
            if (p.getID().equals(_pi.getProbeID()))
                return p;
        }
        return null;        
    }
    
    /*
     * Algorithm is simply based upon identifying probes by their ID
     * @return first probe with the specified ID.
     *
     */
    public DehydratedBytecodeProbe findDehydratedProbe(String _probeID) 
        throws CodeNotAccessibleException {
        
        //com.objs.surveyor.probemeister.Log.out.finer("findDehydratedProbe called...");
        Vector plist = this.getDehydratedProbeList();        
        java.util.Enumeration e = plist.elements();
        while (e.hasMoreElements()) {
            DehydratedBytecodeProbe p = (DehydratedBytecodeProbe)e.nextElement();
            if (p.getID().equals(_probeID))
                return p;
        }
        return null;
    }

    /*
     * Algorithm is simply based upon identifying probes by their ID
     * @return first probe with the specified ID.
     *
     * This method searches the probeList vector of ProbeInterface instances
     */
    ProbeInterface findProbe(DehydratedBytecodeProbe _dp) {

        Vector plist = this.probeList;        
        java.util.Enumeration e = plist.elements();
        while (e.hasMoreElements()) {
            ProbeInterface p = (ProbeInterface)e.nextElement();
            if (p.getProbeID().equals(_dp.getID()))
                return p;
        }
        return null;
    }
    
    
    /***********************************************************************/
    //This method locates all probes bounded by start/end markers
/*    Vector findProbe(BT_Method method, String startMarker, 
                String endMarker) throws ProbeFormatErrorException {

        boolean error     = false;
        boolean foundStart= false;
        boolean foundEnd  = false;
        boolean foundID   = false; 
        String idString = null;
        String descString = null;
        BT_Ins startMarkerIns = null;
        int startMarkerLoc = -1;        

        Vector probeLocs = new Vector(); //used to return results

        //get the code segment
        BT_CodeAttribute code = method.getCode();
        BT_InsVector ins = code.ins; 
        if (ins == null) {
            System.out.println("ERROR: Code Segment is NULL!");
            return null;
        }
        
        //put instructions into enumeration
        java.util.Enumeration insE = ins.elements();
        BT_Ins theOne = null;
        int theOneLoc = -1;
        int index=-1;
        
        //Look through the instructions to find the probe
        while (insE.hasMoreElements()) {
            BT_Ins currIns = (BT_Ins)insE.nextElement();        
            index++; //index matches location in instruction array
            if (currIns.isReturnIns()) // we're at the end of the method!
                break; //and process findings
            if (currIns.isLoadConstantStringIns()) {
                //System.out.println("***Found LDC.toString(): "+op.toString());
                String s1 = ((BT_ConstantStringIns)currIns).value;
                //System.out.println("***at ByteIndex: "+op.byteIndex);
                
                if (s1.equals(startMarker)){ //see if this is the start marker
                    if (!foundStart && !foundEnd && !foundID) { 
                        //System.out.println("===Found Start Marker at "+index+
                        //                         ":"+currIns.byteIndex+"===");
                        startMarkerIns = currIns;
                        startMarkerLoc = index;
                        foundStart=true;
                    } else { // this is a second start before an end marker!!
                        error = true;
                        System.out.println("ERROR: Found end Marker or ID before a Start Marker!");
                        throw new ProbeFormatErrorException();
                    }
                } else if (s1.equals(endMarker)){ //see if this is the end marker
                    if (!foundEnd && foundStart && foundID) { 
                        //System.out.println("===Found End Marker at "+index+
                        //                         ":"+currIns.byteIndex+"===");                        
                        foundEnd=true;
                    } else { // this is a second start before an end marker!!
                        error = true;
                        System.out.println("ERROR: Found End Marker before a Start Marker or ID Marker!");
                        throw new ProbeFormatErrorException();
                    } 
                } else if (s1.indexOf(ProbeInterface.probeIDMarker)>=0) {
                    if (foundStart && !foundEnd && !foundID) { //see if this is the correct probeID
                        //System.out.println("===Found Probe ID at "+index+
                        //                         ":"+currIns.byteIndex+"===");
                        foundID = true;
                        idString = s1;
                    } else { // this is a second start before an end marker!!
                        error = true;
                        System.out.println("ERROR: Found ID Marker before a Start and End Marker!");
                        throw new ProbeFormatErrorException();
                    } 
                } else if (s1.indexOf(ProbeInterface.probeDescMarker)>=0) {
                        descString = s1;
                }

                if (foundStart && foundEnd && foundID) { //then we can add the probe
                    probeLocs.add(new ProbeInfo(method, startMarkerIns, idString, descString, startMarker, endMarker));       //to our list
                    //Reset flags and resume looking for next probe
                    foundStart = false;
                    foundEnd  = false;
                    foundID   = false; 
                }
            }          
        }
        
        return probeLocs;
    }

*/

    /***********************************************************************/
    //Removes the probe, starting at the provided instruction and
    //continuing until the probe end marker is found.
//    boolean removeProbe(ProbeInfo info) {
//        return removeProbe(info.method, info.probeStart, info.endMarker);
//    }

    /***********************************************************************/
    //Removes the probe, starting at the provided instruction and
    //continuing until the probe end marker is found.
/*    boolean removeProbe(BT_Method method, BT_Ins startingInstruction,
                               String endMarker) {
        
        BT_CodeAttribute code = method.getCode();
        BT_Ins currIns = startingInstruction;
        BT_Ins nextIns = code.nextInstruction(currIns);

        //While true, remove instructions until we find the end marker...
        while (true) {
            int insLoc = currIns.byteIndex;
            code.removeInstruction(currIns);
            System.out.println("......Removed Instruction at byteIndex="+insLoc);
            if (nextIns.isLoadConstantStringIns()) {
                String s1 = ((BT_ConstantStringIns)nextIns).value;
                if (s1.equals(endMarker)){ //remove this endmarker entry
                    currIns = nextIns;
                    nextIns = code.nextInstruction(nextIns);
                    code.removeInstruction(nextIns);
                    code.removeInstruction(currIns);            
                    break;
                }
            }
            currIns = nextIns;
            nextIns = code.nextInstruction(nextIns);
        }
        return true;
    }        

*/

    
    
    
    
}