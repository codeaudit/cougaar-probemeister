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

package com.objs.surveyor.probemeister.instrumentation;
import  com.objs.surveyor.probemeister.TargetVMConnector;

import com.sun.jdi.event.BreakpointEvent;

import java.util.Vector;
import java.io.File;

/* Used to hold queued requests to be processed once a breakpoint occur.
 * Call processQueue() when a breakpoint occurs, to process the queue.
 */
public class InstrumentationRequestQueue {

    private TargetVMConnector tvmc;
	private Vector queue;
	
	public InstrumentationRequestQueue(TargetVMConnector _tvmc, int _x) { 
        tvmc = _tvmc;
	    queue = new Vector(_x); 
	}

    /* returns true if breakpointing is working for this VM. */
	public boolean isFunctional() {
	    return tvmc.handler().canEnableBreakpoint();       
    }	    
	    
	/* Adds a InstrumentationRecordSet file to the queue for processing.
	    * Returns TRUE if the queue was successfully enabled.
	    */
	public void add(java.io.File _o) {
	    queue.add(_o);
	    tvmc.handler().enableBreakpoint(true);       
	}

	/* Adds an InstrumentationRecordSet instance to the queue for processing.
	    * Returns TRUE if the queue was successfully enabled.
	    */
	public void add(InstrumentationRecordSet _o) {
	    queue.add(_o);
	    tvmc.handler().enableBreakpoint(true);       
	}

    /* Returns size of queue */
	public int size() {
	    return queue.size();
	}

	/* Call once all entries have been processed. 
	 * Empties queue & disables breakpoint requests 
	 */
	private void clearQueue() {
	    queue.removeAllElements();
	    tvmc.handler().enableBreakpoint(false);       
    }	        


    /* Processes request queue and returns an array of possibly empty sets (size=0) 
     * of InstrumentationRecordSets. If a set's size is null, then no errors
     * occurred. If a set is null, then the entire set could not be applied due 
     * to parsing errors.
     *
     * Requests are processed in order. It is left to the user of this class to
     * manage the request identities (via queue order) should a null set be returned.
     * For sets that are non-null, the InstrumentationRecordSet name & descriptor
     * can be inspected.
     */
    public InstrumentationRecordSet[] processQueue(BreakpointEvent _evt) {
        
        int qsize = queue.size();
        if (qsize==0) return null;
        
        InstrumentationRecordSet[] result = new InstrumentationRecordSet[qsize];        
	    for (int i=0; i< qsize; i++) {

	        com.objs.surveyor.probemeister.Log.out.info("RequestQueue::Applying Configuration Request...");
            try {
                //Get next item in the queue & process it
                Object action = queue.get(i); 
                if (action instanceof File)
                    result[i] = tvmc.recorder().playConfiguration(tvmc, (File)action, _evt);
                else if (action instanceof InstrumentationRecordSet)
                    result[i] = tvmc.recorder().playConfiguration(tvmc, (InstrumentationRecordSet)action, _evt);
                else {
                    com.objs.surveyor.probemeister.Log.out.warning("configRequestsQueue contained unknown type: "+ action.getClass().getName());
                    result[i] = null;
                }
                        
            } catch (InstrumentationParsingException _pe) {
                result[i] = null;
            }
	    }
	    clearQueue();
	    return result;
    }


}