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
 * Used by TargetVMDataPanel class tree list to filter out classes from display  
 * Could be extended to support user-specified strings too... At this point it 
 * only filters out classes <b>starting</b> with: sun | com.sun | java.
 */
package com.objs.surveyor.probemeister.gui;

import com.objs.surveyor.probemeister.PMClass;
import com.objs.surveyor.probemeister.PMMethod;
import java.util.Hashtable;
import java.util.List;

public class ClassTreeNodeFilter {

    static final String COM="com.sun";
    static final String JAVA="java";
    static final String SUN="sun";
        
    static String[] userFilters = null;
    static boolean filterMethods= true;
    static boolean filterOnMethodNameNSig = true;

    static MethodFilterList filterList = null;
    
    /* Returns TRUE if this class meets the class filter criteria
     * Filters on the toString() value returned by the object.
     * Filters out all JDK classes plus any classes starting with 
     * user-supplied strings
     */
    public static boolean filterOutNames(Object _cls) {        
        String s = _cls.toString();        
        if ( s.startsWith(COM) || s.startsWith(JAVA) || s.startsWith(SUN) )
            return true;
        else 
        if (userFilters != null) {
            //Filter out these classes
            for (int i=0; i<userFilters.length; i++) {                    
                if (s.startsWith(userFilters[i]))
                    return true;                    
            }
        } 
        return false;
    }

    /* Returns TRUE if this class meets the class filter criteria
     * Filters on the toString() value returned by the object.
     * Filters in classes - only classes containing the specified 
     * user strings are included. 
     *
     * User filters IN packages by prepending p* (e.g. "p*edu" filters in pkgs
     * starting with edu.
     *
     * User filters IN classes by prepending c* (e.g. "c*Help" filters in classes
     * starting with Help.
     */
    public static boolean filterInNames(Object _cls) {        
        
        if (userFilters != null) {

            String fullname = _cls.toString();
            int startInd = fullname.lastIndexOf('.')+1;
            if (startInd < 0) startInd = 0; //in case class is not in any pkg
            String clsname = fullname.substring(startInd, fullname.length());
//System.out.println("Filtering...clsname = "+clsname+"   fullname = "+fullname);
        
            //Filter in these classes
            for (int i=0; i<userFilters.length; i++) {                    
                String filter = userFilters[i].substring(2);
//System.out.println("Filtering...using "+filter);
                //Filter on pkg
                if (userFilters[i].startsWith("p*")) {
                    if (fullname.startsWith(filter))
                        return false;                    
                } else //Filter on class
                if (userFilters[i].startsWith("c*")) {
                    if (clsname.startsWith(filter))
                        return false;                    
                }
            }
        } 
        return true;
    }

    
    static public void setUserFilters(String[] _other) {     
        userFilters = _other;
    }

    //Return TRUE if object should NOT be visible. That is, returns TRUE if 
    //the object is NOT in the filterList
    public static boolean filterUsingList(Object _cls) {  
        
        //No probes, filter out everything.
        if (filterList == null) return true;
        
        ClassTreeNode ctn = (ClassTreeNode)_cls;
        String s = _cls.toString();        

        if (ctn.nodeType() == ClassTreeNode.CLASS) {
            if (filterList.containsClass(s))
                return false; //don't hide it
        } else
        if (ctn.nodeType() == ClassTreeNode.METH) {                

            if (filterMethods) { //show only methods in filterList

                ClassTreeNode classNode = (ClassTreeNode)((ClassTreeNode)_cls).getParent();
                String className = classNode.toString();
                String methName=""; 
                if (filterOnMethodNameNSig)
                    methName = ((PMMethod)ctn.getUserObject()).nameNSig();
                else
                    methName = ((PMMethod)ctn.getUserObject()).name();

                com.objs.surveyor.probemeister.Log.out.fine("filtering methods using list::Looking for "+className+" : "+methName);                    
                List l = (List) filterList.getMethods(className);
                com.objs.surveyor.probemeister.Log.out.fine("filtering methods using list::List contains: "+ l.toString() );                    
                if (l==null) return true; //shouldn't be null
                if (l.contains(methName)) {
                    com.objs.surveyor.probemeister.Log.out.fine("filtering methods using list:: found method.");                    
                    return false;
                }
            } else { //only filtering on classes in list, show all methods
                com.objs.surveyor.probemeister.Log.out.fine("NOT filtering methods using list");                                
                return false; 
            }
        } else
          if (ctn.nodeType() == ClassTreeNode.PROBE) {
              return false;
        }
        return true; //hide it if we haven't found it        
    }

    static public void setProbeFilter(MethodFilterList _list, boolean _b) {     
        filterMethods = _b;
        filterList = _list;
    }
    
}

