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
 * SourceInsertionPoint.java
 *
 * Created on August 21, 2002, 2:44 PM
 */

package com.objs.surveyor.probemeister.gui;
import jreversepro.reflect.*;
/**
 *
 * @author  Administrator
 */
public class SourceInsertionPoint {
    
    public static final Placement PREPEND = new Placement(0); //insert
    public static final Placement APPEND = new Placement(1);  //append
    
    public int index;
    public int pend;
    public boolean inBlock = false;
    //public boolean inTry = false;
    //public boolean inCatch = false;
    static boolean verbose = false;
    
    /** Creates a new instance of SourceInsertionPoint */
    public SourceInsertionPoint(int _i, int _state, Placement _p) {    
        index = _i;        
        pend = _p.pend; //either prepend or append
        if (_state == JLineOfCode.INBLOCK)
            inBlock = true;
        //if (_state == JLineOfCode.CATCH_INSIDE)
        //    inCatch = true;
    }
    
    public static void setVerbose(boolean _b) { verbose = _b; }
    public String toString() { 
        String r = " ";
        if (verbose) {
            r = r+index+":";
            //if (inTry) r=r+"[t]";
            //if (inCatch) r=r+"[c]";
        }
        return r;
    }
    
    static class Placement {
        int pend;
        public Placement(int _p) { pend = _p; }
    }
}
