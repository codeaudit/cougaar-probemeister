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

import com.objs.surveyor.probemeister.event.VMEventNotifier;
import com.objs.surveyor.probemeister.*;
import com.objs.surveyor.probemeister.probe.*;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.ClassParser;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import com.sun.jdi.connect.*;

import java.util.*;
import java.io.*;


//Classes can actually be modified in three places:
//1. Bytecoder.modifyClass
//2. RuntimeClassMgr.redefineClass
//3. TargetVMConnector.exportClassToTargetVM

public class Bytecoder {


    /* Checks to see if method exists 
     *
     * if _mustBeStatic is TRUE, this method will only return TRUE if it finds a
     * static method with the given name.
     *
     * if _mustHaveNoArgs is TRUE, this method will only return TRUE if it finds a
     * method with the given name that takes no arguments.
     */
    public static boolean classMethodExists(TargetVMConnector _tvmc, String _className, String _methodName, boolean _mustBeStatic, boolean _mustHaveNoArgs){
        
        ReferenceType refType = _tvmc.findRefType(_className);
        if (refType == null) return false;
        
        java.util.List methList = refType.methodsByName(_methodName);
        boolean found = false;
        for (int i=0; i<methList.size(); i++) {

            if (!_mustHaveNoArgs && !_mustBeStatic) { //we've found one
                found = true;
                break;
            }
            Method m = (Method)methList.get(i);

            if (_mustHaveNoArgs) { //get arg count
                List args = m.argumentTypeNames();
                if (args!=null && args.size()>0) 
                    continue; // not this one
            }
            if (_mustBeStatic) {
                if (!m.isStatic())
                    continue; //not this one
            }
            found = true;
            break;
        }
        return found;        
    }


    void loadClassFromSourceFile(String filename) {
    }

    /* Import and return the JavaClass for the class in the .class file 
     * If file doesn't exist, returns NULL. Expects absolute path to file.
     */
    public static JavaClass loadClassFromClassFile(String filename) {

        try {
            //java.io.InputStream clsIS = new FileInputStream(filename);
                                       
            //Get class bytecode from file, bytecode, etc.
            //if (clsIS != null) {
                JavaClass java_class = (new ClassParser(filename)).parse();
                return java_class;
            //} else 
            //    return null;        
        } catch (java.io.IOException ioe) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.WARNING, "loadClassFromClassFile::IOException loading class.", ioe);
            return null;
        } catch (java.lang.ClassFormatError cfe) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.WARNING, "loadClassFromClassFile::ClassFormatError loading class.", cfe);
            return null;
        }
    }
    
    /* Return the class name (full name including pkg) of the class in the .class file 
     * If file doesn't exist, returns NULL. Expects absolute path to file.
     */
    public static String getClassNameFromClassFile(String filename) {
        
        if (filename.endsWith(".class")) {
            JavaClass jc = Bytecoder.loadClassFromClassFile(filename);
            if (jc == null) return null;
            
            return jc.getClassName();
        }
        return null;
    }


    void loadClassFromBytes(byte[] cbytes) {
    }

    List getAllProbes(Method meth) {
        //If meth==null get all probes in class, o.w. just ones in meth
        /**** ADD CODE ***/
        return null; 
        
    }
    
    List removeAllProbes(Method meth) {
        //If meth==null remove all probes in class, o.w. just ones in meth
        /**** ADD CODE ***/
        return null; 
        
    }
    
    boolean insertProbe(ProbeInterface p) {
        /**** ADD CODE ***/
        return false; 
    }
    
    boolean removeProbe(ProbeInterface p) {
        /**** ADD CODE ***/
        return false; 
    }
    
    byte[] getClassAsBytes() {
        /**** ADD CODE ***/
        return null; 
    }
    
    File saveClassToFile(String filename) {
        /**** ADD CODE ***/
        return null; 
    }
    
    List getMethods() {
        /**** ADD CODE ***/
        return null; 
    }
    
    String getMethodSource() { 
        
        /**** ADD CODE ***/
        return null; 
    }
    
    byte[] getMethodBytecode() {
        /**** ADD CODE ***/
        return null; 
    }


    /*
     * This method makes the actual class redefinition call
     * given the modified bytes.
     *
     */
    static boolean modifyClass(TargetVMConnector _tvmc, ReferenceType refType, byte[] modifiedClass) 
        throws OperationNotSupported {
        
        //Check to see if the VM supports this capability
        if (!_tvmc.vm().canRedefineClasses())
            throw new OperationNotSupported();        
        
        //Make sure that the attrs are populated
        if (refType == null || modifiedClass == null )
            return false;
            
        //Now submit the new bytes to the VM!!
        Map map = new HashMap();
        map.put(refType, modifiedClass);
        try {
            _tvmc.vm().redefineClasses(map);
            com.objs.surveyor.probemeister.Log.out.info("***** Class Redefined *****");
            return true;
        } catch (UnsupportedOperationException uoe) {
            throw new OperationNotSupported();        
        } catch (Throwable exc) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.WARNING,"Error redefining " + refType.name() + " - " + exc, exc);
            return false; //update failed
        }

    }

    /*
     * Loads contents of InputStream into byte array.
     *
     *
     */
    public static byte[] loadClassFromInputStream(InputStream is) {

        if (is == null) return null;
        byte[] allbytes = null;

        try {
            int avail = is.available(); //# of bytes we can read w/o blocking
            
            byte[] newbytes = new byte[avail];
            
            int offset = 0;
            int len = 0; //bytes read in
            boolean firstLoop = true; // flag to know if we must concat separate reads
            while ( (len = is.read(newbytes, offset, avail)) > 0) {
                //concat bytes
                if (!firstLoop) { // then we must concat these bytes with the last ones
                
                    byte[] tempbytes = new byte[offset+len];
                    
                    //copy current bytes to new array
                    for (int i = 0; i<offset; i++) {
                        tempbytes[i] = allbytes[i];
                    }
                    
                    //copy new bytes to new array
                    //int j;
                    for (int i = offset, j=0; i < offset+len;  i++, j++) {
                        tempbytes[i] = newbytes[j];
                    }
                    allbytes = tempbytes;
                    
                } else {
                 
                    allbytes = newbytes;
                }
                
                firstLoop = false;
                offset += len; //adjust offset
                if (offset == len) break; //all done
            }
        } catch (java.io.IOException ioe) {}
        return allbytes;

    }

    /*
     * Searches the list of classpaths provided looking for the specified class.
     * @param clsname expected NOT to include file suffix (e.g. ".class")
     * @return Null if inner class, or if class not found in classpath. Otherwise,
     *  returns a non-null InputStream object.
     */
    static public InputStream loadClassFileFromClasspath(ClassObject co, List clspath, String clsname) 
                                    throws InnerClassFileException {
        
        //Do not process inner classes
        int dollar = clsname.indexOf('$');
//        if (dollar >0) {
//            com.objs.surveyor.probemeister.Log.out.warning("This is an inner class... ");
//System.out.println("OVERRIDE");
//Commented out ne3xt line
//            throw new InnerClassFileException();
//        }

        //Process file name
        String sysFileStr = clsname;
            
        //Change '.' to '\' so pkg name becomes part of the path
        String filesep   = System.getProperty("file.separator");        
        sysFileStr = sysFileStr.replace('.', filesep.charAt(0)) + ".class";
        String jarFileStr = clsname.replace('.', '/') + ".class";

        //Start searching the classpath
        com.objs.surveyor.probemeister.Log.out.fine("Starting LOCAL File Search... for "+clsname+"\n__________________________________________");        

        InputStream is = null;
        //try jar file first, look in same place that another PM class was loaded from
        com.objs.surveyor.probemeister.Log.out.info("Trying classloader... for "+jarFileStr);        
        is = com.objs.surveyor.probemeister.runtime.RuntimeClassManager.getMgr().getClass().getResourceAsStream("/"+jarFileStr);
        if (is != null) return is;
        //System.out.println("\n.getResource 2 Search... for "+sysFileStr);        
        is = com.objs.surveyor.probemeister.runtime.RuntimeClassManager.getMgr().getClass().getResourceAsStream("/"+sysFileStr);
        if (is != null) return is;

        for (int i =0; i<clspath.size(); i++) {

            //Get the next classpath entry
            String dirStr = (String)clspath.get(i);            
            com.objs.surveyor.probemeister.Log.out.info(i+". Classpath = "+dirStr);
            
            //Find class
            //1. Create dir ref - could be dir, zip/jar, (or file?)
            File dir = new File(dirStr);
            //System.out.println("Looking in: "+dirStr);
            
            //If path exists, then look in it for our class
            if (dir.exists()) { 
                // Check to see if this is a simple directory
                if (dir.isDirectory()) { //
                    File cfile = new File(dir, sysFileStr);
                    com.objs.surveyor.probemeister.Log.out.info("Looking for: "+cfile.getAbsolutePath());
                    if (cfile.exists()) {
                        com.objs.surveyor.probemeister.Log.out.info("Found file! It is here: "+cfile.getName());
                        try {
                            is = new FileInputStream(cfile);
                            if (co != null)
                                co.setClassFile(cfile);
                        } catch (java.io.FileNotFoundException fnfe) {//really should not occur
                            continue; //then keep looking
                        }
                        break;
                    } else {
                        continue; //look in next classpath entry
                    }
                } //not a dir.
                //Check to see if this is a jar/zip archive
                else if (dirStr.endsWith(".jar")|| dirStr.endsWith(".zip")) {
                    com.objs.surveyor.probemeister.Log.out.fine("Classpath entry is a zip/jar. Searching archive...");
                    try {
                        java.util.zip.ZipFile zipDir = new java.util.zip.ZipFile(dir);
                        java.util.zip.ZipEntry ze = zipDir.getEntry(jarFileStr);
                        if (ze!=null) {
                            com.objs.surveyor.probemeister.Log.out.fine("Classfile found in archive!"); 
                            is = zipDir.getInputStream(ze);
                            if (co != null)
                                co.setClassFile(ze);
                            break;
                        }
                        else {
                            com.objs.surveyor.probemeister.Log.out.finest("Didn't find file! ["+dir+"::"+jarFileStr+"]");
                            continue; //look in next classpath entry
                        }
                    } catch (Exception zipe) {
                        com.objs.surveyor.probemeister.Log.out.warning("Zip Exception: "+zipe.toString());
                        continue;
                    }
                } else { //We don't know what this is... warn user
                    com.objs.surveyor.probemeister.Log.out.warning("***ALERT: Unknown type in classpath: "+dirStr);
                    continue;
                }
            }
        }
        
        return is;
    }

}