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
import javax.swing.JOptionPane;

/*
 * This is the core interface for bytecode probe types
 */
public abstract class BytecodeProbeType  extends com.objs.surveyor.probemeister.probe.ProbeType {
    
//    public ProbeInterface generateProbe(Location _loc);
    
    /* probe rehydration - just add water */
//    public ProbeInterface regenerateProbe(DehydratedProbe _dp);

    /* Return the instance of the class -- each subtype should create one instance */
    public static BytecodeProbeType getProbeType() {return null;}

    public ProbeInterface generateProbe(String _id, String _desc, BytecodeLocation _loc, java.util.Map _params) {
        
        return null;
    }
    
    public void displayInfoAboutProbe(DehydratedProbe _dp, java.awt.Window _window) {

        if (_dp != null) {
            //Process parameters embedded in probe for viewing
            ProbeType pt = ProbeCatalog.getProbeTypeByName(_dp.getType());
            String[] params = _dp.getEmbeddedParameters();
            if (pt !=null && params!=null) 
                params = pt.prettyPrintParamList(params);
            
            //Create dialog for viewing
            ProbeInfoDialog dialog = new ProbeInfoDialog("Probe Information");
            BytecodeLocation bLoc = (BytecodeLocation)_dp.getLocation();
            
            //Populate dialog with values
            dialog.configure(_dp.getID(), _dp.getType(), _dp.getDesc(), 
	                      bLoc.getClassName(), bLoc.getMethodName(), bLoc.getOffsetAsString(),
	                      _dp.getEmbeddedParameters());
            
            dialog.setVisible(true);            
        } else {
    	    JOptionPane.showMessageDialog(_window, 
	            "No information can be displayed at this time.", "Error", JOptionPane.INFORMATION_MESSAGE);		    
            com.objs.surveyor.probemeister.Log.out.warning("RTClassMgr::Cannot display probe info -- not a dehydrated probe!");
        }
    }

}