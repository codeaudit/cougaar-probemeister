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

//import com.objs.surveyor.probemeister.bytecoder.StatementList;

/*
 * This is the core interface for probe types. ProbeTypes are responsible
 * for delivering the [source/byte] code payload required for insertion, as 
 * well as the reconstruction (aka dehydration) of a probe object from its 
 * deployed format.
 *
 * Instances of these should be registered in the ProbeCatalog. They register 
 * when the ProbeCatalog invokes their (overridden) register() method.
 * The output of a probe type is a unique probe. 
 */
public abstract class ProbeType {
   
    /* Return single instance of this stub */
    public static ProbeType getStub() {return null;} 
    public void register(com.objs.surveyor.probemeister.probe.ProbeCatalog _cat) {
    }
    
    //public String getNextID() { return "ProbeType - No ID"; }
    
    public String getName() {return "No Name";}
    public String getRegisteredProbeTypeName() {return "No Name"; }
    
    /* Method for probe creation */
    public ProbeInterface generateProbe(Location _loc) {
        com.objs.surveyor.probemeister.Log.out.fine("ProbeType:generateProbe");
        return null;
    }
    
    /* probe rehydration - just add water */
    public ProbeInterface regenerateProbe(DehydratedProbe _dp) {
        return null;
    }
    
    //*no creation of probes directly from probe class
 
    /* Display customized information about the dehydrated probe 
     * to override the basic ProbeInfoDialog GUI.
     *
     * Not supported at this time.
     */
    public void displayInfoAboutProbe(DehydratedProbe _dp, java.awt.Window _window) {

    }
    
    /* used to process encoded parameters for display to user. */
    public String[] prettyPrintParamList(String[] _params) { return _params;}
    
}