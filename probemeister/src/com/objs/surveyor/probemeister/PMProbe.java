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

import com.objs.surveyor.probemeister.bytecoder.DehydratedBytecodeProbe;
import java.util.List;

public class PMProbe {
    
    private DehydratedBytecodeProbe dp;
    private PMMethod meth;
    
    public PMProbe (DehydratedBytecodeProbe _dp, PMMethod _meth) {
        dp = _dp;
        meth = _meth;
        
    }

    /* not for general use  */
    public DehydratedBytecodeProbe getDehydratedProbe() { 
       return dp; 
        
    }
    
    public PMMethod getPMMethod() { return meth; }
    
    /* Displays information about the probe in a dialog window */
    public void showDescriptionDialog(java.awt.Window _w) {
        dp.displayInfo(_w);
    }
    public String name() { return toString(); }
    public String toString() {        
        return dp.toString();        
    }
        
    
}