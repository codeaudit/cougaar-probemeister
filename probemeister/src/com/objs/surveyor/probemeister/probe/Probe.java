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

package com.objs.surveyor.probemeister.probe;

import com.objs.surveyor.probemeister.bytecoder.StatementList;
import com.objs.surveyor.probemeister.bytecoder.BytecodeLocation;


/* Provides a default implementation of the core methods in this interface */
public abstract class Probe implements ProbeInterface {
               
    String probeID = "defaultID";
    String probeDesc = "defaultDesc";
    Location loc = null;
    ProbeType pType=null;
    String pTypeStr=null;
    private String[] source = null;
    private StatementList sl = null;
    
    private boolean isSourceProbe = false;
    private boolean isBytecodeProbe = false;
    
    /* 
     * Public constructor for bytecode based probes.
     */
    public Probe(String _id, String _desc, ProbeType _pt, StatementList _sl, BytecodeLocation _loc) {
        probeID = _id;
        probeDesc = _desc;
        pType = _pt;
        loc = _loc;
        sl = _sl;        
        isBytecodeProbe = true;
    }
    //public ProbeInterface(String _id, String _desc, String _code, SourceLocation _loc);
    

    
    public String getProbeID() { return probeID; }
    public void   setProbeID(String _pid) { probeID = _pid; }

    public String getProbeDesc() { return probeDesc; }
    public void   setProbeDesc(String _pd) { probeDesc = _pd; }
         
    public boolean isStub() { return false; }
              
    public Location getProbeLocation() {return loc; }
    public void setProbeLocation(Location _loc) { loc = _loc; }

    //public int    getBytecodeInsertionPoint() {return probeLoc; }
    //public void   setBytecodeInsertionPoint(int _loc) {probeLoc = _loc; }

    public boolean isBytecodeProbe() {return isBytecodeProbe;}
    public void setBytecodeStmts(StatementList _sl){sl=_sl;}
    public StatementList getBytecodeStmts() {return sl;}

    public boolean isSourceProbe() {return isSourceProbe;}
    public void setSource(String[] _str) {source=_str;}
    public String[] getSource() {return source;}    
   
    public ProbeType getProbeType() {return pType;}
    public void setProbeType(ProbeType _pt) {pType = _pt;}
    
    //TEMP ? 
    public String getProbeTypeStr() {return pTypeStr;}
    public void setProbeTypeStr(String _ptStr) {pTypeStr = _ptStr;}

   
}