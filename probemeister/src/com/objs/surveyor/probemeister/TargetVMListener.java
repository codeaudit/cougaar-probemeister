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

 
 
package com.objs.surveyor.probemeister;
 

/* Any class wanting to be kept informed of TargetVMConnector activity
 * should extend this class. 
 *
 * Would be good if this class had its own thread. However some events like
 * Breakpoints requiring listener action need to be processed before the
 * breakpoint is resumed, so a simple notification design is not appropriate
 * in this case.
 */
public abstract class TargetVMListener {
    
    public Object o=null;
    public TargetVMListener() {}
    public TargetVMListener(Object _o) {o=_o;}    
    
    public void classListUpdated(TargetVMEvent e) {}

    public void vmConnected(TargetVMEvent e) {}

    /* The Target VM was started, then suspended immediately - so it is not running now 
     * Only several core java OS classes have been loaded at this time.
     */
    public boolean vmStartedInterrupted(TargetVMEvent e) { return true;}

    /* The Target VM was started running (all classes should be loaded) */
    public void vmStartedRunning(TargetVMEvent e) {}

    public void vmDisconnected(TargetVMEvent e) {}
    
    /* The TargetVM has been suspended */
    public boolean vmInterrupted(TargetVMEvent e) { return true;}
    
    public void vmResumed(TargetVMEvent e) {}
    
    /* Called when a VM attaches, waiting to be probed. */
    public void newVMAttached(TargetVMEvent e) {}

    /* The TargetVM has reached a breakpoint. Cetain kinds of
       instrumentation can only occur when a breakpoint is reached.
       It is possible that the only time a breakpoint occurs is
       when the target VM is first loaded, and even then it is
       not guaranteed to occur.

       In theory we should be able to get the two items we need for applying class
       exports ( the classType, and the thread reference) from the breakpoint event
       via location().declaringType, and thread(). See exportClassToTargetVM. These
       attributes are passed into the InstrumentationRecord_NewClass when (re)generating
       the instrumentation action.
     */  
    public boolean vmBreakpointEvent(TargetVMEvent e) { return true; }

    /* The TargetVM has been modified. getSource() returns the
     * related InstrumentationRecord.
     */
    public void vmInstrumentedEvent(TargetVMEvent e) {}
        
    /* The TargetVM command-line params have been retrieved. getSource() returns the
     * related TargetVMCLParams object.
     */
    public void vmUpdatedVMParamsEvent(TargetVMEvent e) {}
        
}