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

import java.util.Vector;


//This class is a container for sets of records. When first created,
//a default name/desc will be assigned, but when/if it is saved the
//user should be able to reassign the name/desc.
public class InstrumentationRecordSet {

    public static final String setTag  = "InstrumentationSet";
    public static final String nameTag = "name";
    public static final String descTag = "desc";

    Vector records;
    String name;
    String desc;
    String serial = null; //current serialized value of this set
    
    private int errorCount = 0;
    public int getErrorCount() {return errorCount;}
    public void setErrorCount(int _i) {errorCount = _i;}
    
    
    public InstrumentationRecordSet(String _name, String _desc, Vector _recs) {        
        name = _name;
        desc = _desc;
        records = _recs;        
    }

    public InstrumentationRecordSet(String _name, String _desc) {        
        records = new Vector();
        name = _name;
        desc = _desc;
    }

    
    public void addRecord(InstrumentationRecord _ir) { 
        synchronized(this) {
            records.add(_ir);  serial = null;
        }
    }
    public void removeRecord(int i) { 
        synchronized(this) {
            records.remove(i); 
            serial = null;
        }
    }
    public InstrumentationRecord getRecord(int _i) { 
        synchronized(this) {
            return (InstrumentationRecord)records.get(_i); 
        }
    }
    public int getSize() {return records.size();}

    public void setName(String _n) {name = _n;}
    public void setDescription(String _d) {desc = _d;}
    
    public String serializeSet() {
        if (serial != null) return serial; //return current xml, unless set has been modified
        String prexml = "<InstrumentationSet name=\""+ name + "\" desc=\"" + desc + "\">\n";
        String xml="";
        synchronized(this) {        
            for (int i=0; i<records.size(); i++) {
                InstrumentationRecord ir = (InstrumentationRecord) records.get(i);
                InstrumentationRecordSerialized irs = new InstrumentationRecordSerialized(ir);
                ir.serializeAttrs(irs);
                xml = xml + irs.toString();
            }
        }        
        serial = prexml+xml+"\n</InstrumentationSet>";
        return serial;
    }


    public boolean applySetToVM(TargetVMConnector _tvmc) {
     
        if (_tvmc != null) {
            synchronized(this) {        
                for (int i=0; i<records.size(); i++) {
                    InstrumentationRecord ir = (InstrumentationRecord) records.get(i);
                    ir.reapplyAction(_tvmc);
                }
            }
            return true;
        }
        else
            return false;
    }

    public boolean requiresBreakpoint() {

        synchronized(this) {        
            for (int i=0; i<records.size(); i++) {
                InstrumentationRecord ir = (InstrumentationRecord) records.get(i);
                if (ir instanceof InstrumentationRecord_NewClass) 
                    return true;
            }
        }
        return false;
    }

}