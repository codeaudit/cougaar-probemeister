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

import com.objs.surveyor.probemeister.TargetVMConnector;

//This class is holds the instrumentation details for a
//given modification event of a target VM.
public interface InstrumentationRecord {

    public void setClassName(String _c);
    public void setTime(long _t);
    
    public String getType(); //user visible name of this record type
    public String getClassName(); //at least we should know this for every type
    public long getTime();
    
    public String getError();
    public void setError(String _s);
    
    /* Return identifiable string for this record, e.g. so it can be removed. */
    public String getID();
    
    
    //Apply action to specified VM
    public boolean reapplyAction(TargetVMConnector _tvmc);
    
    public void serializeAttrs(InstrumentationRecordSerialized _irs);
    
    //During deserialization, this is called to repopulate attributes specific
    // to a subtype of InstrumentationRecord
    public void processAttribute(String _name, String _value);
    
    public void processGroup(String _name, java.util.Map _values);
    
    public String toString();
    
}