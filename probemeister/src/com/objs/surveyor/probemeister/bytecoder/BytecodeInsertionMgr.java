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
 

/*
 * This is the bytecode insertion manager. It simplifies the insertion of
 * probes.
 */
class BytecodeInsertionMgr {
    
    /* Method for creating a StatementList */
    static public StatementList createStatementList(BytecodeLocation _loc) {        
        return _loc.getClassObject().createStatementList();
    }
        
    /* This is the method to be called to insert a probe & propagate the
     * modifications to the target VM. If the target VM rejects the update,
     * the probe is removed from the bytecode, and this method returns false.
     */       
    static boolean insertProbe(ProbeInterface _pi, InstrumentationRecord _ir) throws ProbeFormatErrorException,
                                DuplicateProbeException, UnsupportedFunctionException {
        
        if (!_pi.isBytecodeProbe()) throw new ProbeFormatErrorException("Not a bytecode probe.");
        
        //Get location
        BytecodeLocation loc = (BytecodeLocation)_pi.getProbeLocation();        

        //Insert probe
System.out.println("Time = "+System.currentTimeMillis());
        boolean ok = loc.getMethodObject().insertProbe(_pi);        
        if (!ok) {
	        com.objs.surveyor.probemeister.Log.out.severe("BytecodeInsertionMgr::ERROR-- could not insert probe.");
            return false;
        }
        
        //Propagate method changes to class in JVM
	    if (loc.getClassObject().postUpdates()) {
System.out.println("Time = "+System.currentTimeMillis());
	        
	        //record action via InstrumentationRecorder
	        if (_ir != null)
                loc.classMgr.vmConnector().recorder().recordAction(_ir);
                
		//sjf com.objs.surveyor.probemeister.probe.event.EventBus.announceInsertion(_pi);    
                
	        return true;
	        
	    } else { //update failed... bad probe code(?), so remove probe.
	        try { 
	            loc.getMethodObject().removeProbe(_pi);
	        } catch(NoSuchProbeException nspe) {
	            com.objs.surveyor.probemeister.Log.out.warning("BytecodeInsertionMgr::ERROR-- could not remove probe from rejected bytecode. Code may be corrupt ***");
	        }
	        return false;
	    }
    }
}
