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
import com.objs.surveyor.probemeister.probe.ProbeCatalog;
import com.objs.surveyor.probemeister.probe.ProbeType;
import com.objs.surveyor.probemeister.probe.Location;
import org.apache.bcel.generic.InstructionHandle;

/*
 * This class contains deflated or dehydrated probes extracted from method
 * bytecode. This is all that is need to manage removal & inspection. If 
 * modification is desired, the probe will need to be reconstituted. @See
 * ProbeFactory.reconstitute() .
 */
public class DehydratedBytecodeProbe implements com.objs.surveyor.probemeister.probe.DehydratedProbe {

    private MethodObject methodObject;
    private InstructionHandle startMarkerIns; 
    private InstructionHandle endMarkerIns;
    private String id; 
    private String desc;
    private String pType;
    private BytecodeLocation loc;
    private java.util.Vector params;
    
    DehydratedBytecodeProbe(MethodObject _methodObject, InstructionHandle _startMarkerIns, 
                            InstructionHandle _endMarkerIns, String _id, String _desc, String _pType,
                            java.util.Vector _otherStrings, BytecodeLocation _loc) {

        methodObject = _methodObject;
        startMarkerIns = _startMarkerIns;
        endMarkerIns = _endMarkerIns;
        id = _id;
        desc = _desc;
        pType = _pType;
        loc = _loc;
        params = _otherStrings;
//System.out.println("DehydratedBytecodeProbe: strings:");
//for (int i=0; i<params.size(); i++) {System.out.println("     "+(String)params.get(i));}
    }
    
    MethodObject getMethodObject() {return methodObject;}
    
    /* This is the first InstructionHandle of this probe */
    InstructionHandle getStartInsH() {return startMarkerIns;} 
    
    /* This is the FINAL InstructionHandle of this probe */
    InstructionHandle getEndInsH() {return endMarkerIns;}
    public String getID() {return id;}
    public String getDesc() {return desc;}      
    public String getType() { return pType;}
    public Location getLocation() {return loc;}
 
    public String[] getEmbeddedParameters() {
        if (params==null) return null;
        String[] sp = new String[params.size()];
        params.copyInto(sp);   
        return sp;
    }
    
    
    /* Displays a GUI that describes this probe */
    public boolean displayInfo(java.awt.Window _window) {        
    
        ProbeType pt = ProbeCatalog.getProbeTypeByName(this.getType());
        if (pt != null) {
            pt.displayInfoAboutProbe(this, _window);
            return true;
        } else {
            return false;            
        }
    }
    
    public String toString() { return "Probe ID: "+id; }
}