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

import java.util.Map;
import java.util.Iterator;


//This class supports a flattened representation of an InformationRecord object.
//Currently, this class output and consumes an XML representation.
public class InstrumentationRecordSerialized {
    
    //Tags used to minimize coding errors in classes that read this xml
    public static final String recordTag = "InstrumentationRecordSerialized";
    public static final String recTypeTag  = "type";  
    public static final String recTargetTag  = "targetClass";
    public static final String recTimeTag  = "time";
    public static final String attrTag   = "Attr";
    public static final String attrNameTag = "name";
    public static final String attrValTag  = "value";
    public static final String groupTag  = "Group";
    public static final String groupNameTag  = "type";
    private static final String pre = "    <"+recordTag+"\n"; // no closing '>'
    private String all;
    private String body;
    private static final String post = "    </"+recordTag+">\n";
    String indent="    ";
    void increaseIndent() {indent = indent+"    ";}
    void decreaseIndent() {if (indent.length() >4) indent = indent.substring(0, indent.length()-4);}
    
    public InstrumentationRecordSerialized(InstrumentationRecord _irObject) {
        all = pre + 
            indent+recTypeTag+"=\""+ _irObject.getClass().getName() + "\"\n" +
            indent+recTargetTag+"=\"" + _irObject.getClassName() + "\"\n" +
            indent+recTimeTag+"=\""+ Long.toString(_irObject.getTime()) + "\"" +
            " >\n";
        increaseIndent();
    }
    
    public String toString() {return all+post;}

    public void addAttrValPair(String _attr, String _val) {
        addAttrValPair(_attr, _val, "");
    }
    //Append an attr value pair
    private void addAttrValPair(String _attr, String _val, String indent) {
        
        all = all + indent +
            "       <"+attrTag+" "+attrNameTag+"=\"" +  _attr + "\"  "+attrValTag+"=\"" + _val + "\"/>\n";
            
    }
 
    //Supports grouping of attr-value pairs in named element
    public void addGroup(String _name, Map _values) {

        all = all + indent + "<"+groupTag+" "+groupNameTag+"=\"" + _name + "\">\n";
        
        //Now add the probe specific attrs    
        if (_values != null) {
            Iterator keys = _values.keySet().iterator();
            increaseIndent();
            while (keys.hasNext()) {
                String key = (String)keys.next();
                this.addAttrValPair(key, (String)_values.get(key), "   ");
            }
            decreaseIndent();
        }
        all = all + indent + "</"+groupTag+">\n";
    }
 
 
    //Pass an XML fragment for deserialization. It will return
    //a deserialized InformationRecord
    public InstrumentationRecord deserialize(String _frag) {
        
        //extract header info, and create a subtype of InformationRecord
        
        
        //Extract attr value pairs & pass to InformationRecord instance
        //in hashtable
        
        //Finally, return the instance.
        return null;
        
        
    }
    
    
}