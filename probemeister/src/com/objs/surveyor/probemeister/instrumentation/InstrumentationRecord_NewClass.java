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

import java.io.File;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.ClassType;

import com.sun.jdi.event.BreakpointEvent;

import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.ByteArrayInputStream;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.Constants;


//This class is holds the instrumentation details for a
//given modification event of a target VM.
public class InstrumentationRecord_NewClass implements InstrumentationRecord {

    private long time=0;
    private String className=null; // the file+path sent to target VM
    private String fileLocation=null;
    private String error = null;

    public InstrumentationRecord_NewClass() {}
    public void setClassName(String _c) {className = _c;} 
    public void setTime(long _t) {time = _t;}

    public String getError() {return error;}
    public void setError(String _s) {error = _s;}

    public String getID() {return fileLocation+className;}

    //Pass in name/path of new file, and name of class contained in the file
    public InstrumentationRecord_NewClass(String _fileLoc, String _className) {
        className = _className;
        fileLocation = _fileLoc;
        time = getTimeNow();
    }
/*    
    public Object clone() {
        InstrumentationRecord r = new InstrumentationRecord_NewClass(fileLocation, className);
        r.setTime(time);
        return r;
    }
*/    
    protected long getTimeNow() { return System.currentTimeMillis(); }
    
    public String getType() {return "New Class";}
    public String getClassName() {return className;} //at least we should know this for every type
    public long getTime() {return time;}

    public String getFileLocation() {return fileLocation;} //at least we should know this for every type

    //Need to address redundant method sigs
    public boolean reapplyAction(TargetVMConnector _tvmc) {return false;}
    public boolean reapplyAction(TargetVMConnector _tvmc, com.sun.jdi.event.Event _bkpt) {

        if (! (_bkpt instanceof BreakpointEvent)) return false;
        BreakpointEvent bkpt = (BreakpointEvent)_bkpt;

        if (getFileLocation().endsWith("jar")||getFileLocation().endsWith("zip")) {
            
            return defineFromJar(_tvmc, bkpt, getFileLocation());
        }
        
        ReferenceType newClass = null;
        try {
            File theFile = new java.io.File(fileLocation);       
            newClass = _tvmc.exportClassToTargetVM((ClassType)bkpt.location().declaringType() , bkpt.thread(), theFile, className);
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.warning("Exception while reapplying InstrumentationRecord[New Class]: "+e);            
            return false;
        }
        if (newClass != null) return true;
        else return false;
    }

    //Add subtype-specific attrs to InstrumentationRecordSerialized
    public void serializeAttrs(InstrumentationRecordSerialized _irs) {
        
        _irs.addAttrValPair("File", fileLocation);
        
    }

    public void processAttribute(String _name, String _value) {
        if (_name.equals("File"))
            fileLocation = _value;
        else
            com.objs.surveyor.probemeister.Log.out.warning("InstrumentationRecord_NewClass.processAttribute() saw attrName it did not recognize.");
    }

    public void processGroup(String _name, java.util.Map _values) {
        //if (_name.equals("Params")) this.probeParams = _values;
    }

    public String toString() {
        return this.getClass().getName()+":: class="+getClassName()+"  file="+this.getFileLocation();
    }

    private boolean defineFromJar2(TargetVMConnector _tvmc, BreakpointEvent _bkpt , String _jar) {

        ReferenceType newClass = null;

        boolean result = true;
        try {
            java.util.zip.ZipFile zipDir = new java.util.zip.ZipFile(_jar);
            java.util.Enumeration files = zipDir.entries();
            
            while (files.hasMoreElements()) {
                
                java.util.zip.ZipEntry ze = (java.util.zip.ZipEntry)files.nextElement();
                if (ze!=null) {
                    java.io.InputStream is = zipDir.getInputStream(ze);
                    String clsName = ze.getName();
                    if (!clsName.endsWith(".class"))
                        continue; // not a class file
                    clsName = clsName.substring(0, clsName.length()-6);
                    clsName = clsName.replace('/', '.'); //change path to pkg name
                    com.objs.surveyor.probemeister.Log.out.finer("defineFromJar:: defining class: "+clsName); 

                    newClass = _tvmc.exportClassToTargetVM((ClassType)_bkpt.location().declaringType() , _bkpt.thread(), is, clsName);
                    result &= (newClass != null);
                    
                    //Dump to temp file for inspection
                    java.io.FileOutputStream fos = new java.io.FileOutputStream("ZZTEMP.class");
                    byte[] in = new byte[is.available()];
                    is.read(in);
                    fos.write(in);
                    fos.close();
                    com.objs.surveyor.probemeister.Log.out.fine("defineFromJar:: class written to ZZTEMP.class");
                    
                    com.objs.surveyor.probemeister.Log.out.finer("defineFromJar:: RETURNING PREMATURELY");
                    return result;
                    
                }
            }
        } catch (Exception zipe) {
            com.objs.surveyor.probemeister.Log.out.warning("Zip Exception: "+zipe.toString());
            //continue;
            return false;
        }                
        return result;
    }


    private boolean defineFromJar(TargetVMConnector _tvmc, BreakpointEvent _bkpt , String _jar) {

        ReferenceType newClass = null;

        boolean result = true;
        try {
            
            Vector jarfiles = new Vector();
            Vector jarnames = new Vector();
            loadClassesFromJar(_jar, jarfiles, jarnames);
            //Hashtable jarfiles = loadClassesFromJar(_jar);
            if (jarfiles == null) return false;
            
            //java.util.Enumeration enum = jarfiles.keys();
            
            boolean ok = predefineClasses(jarnames, _tvmc, _bkpt);
            if (!ok) {
                com.objs.surveyor.probemeister.Log.out.warning("defineFromJar:: predefine failed.");
                return false;   
            }
            //while (enum.hasMoreElements()) {
            for (int i=0; i<jarfiles.size(); i++) {    

                String clsName = (String)jarnames.get(i);
                ByteArrayInputStream is = (ByteArrayInputStream)jarfiles.get(i);

                //String clsName = (String)enum.nextElement();
                //ByteArrayInputStream is = (ByteArrayInputStream)jarfiles.get(clsName);
                    
                    
                String osName = clsName;
                clsName = clsName.substring(0, clsName.length()-6);
                clsName = clsName.replace('/', '.'); //change path to pkg name
                com.objs.surveyor.probemeister.Log.out.fine("defineFromJar:: defining class: "+clsName); 

                newClass = _tvmc.exportClassToTargetVM((ClassType)_bkpt.location().declaringType() , _bkpt.thread(), is, clsName);
                result &= (newClass != null);
                    
                //Dump errors to temp file for inspection
                if (newClass == null) {
                    osName = osName.substring(osName.lastIndexOf('/')+1, osName.length());
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(new File("temp/sienaDump", osName));
                    is.reset();
                    byte[] in = new byte[is.available()];
                    is.read(in);
                    fos.write(in);
                    fos.close();
                    com.objs.surveyor.probemeister.Log.out.fine("defineFromJar:: class written to "+osName);
                }                    
                //System.out.println("defineFromJar:: RETURNING PREMATURELY");
                //return result;
                     
            }
        } catch (Exception zipe) {
            com.objs.surveyor.probemeister.Log.out.warning("Zip Exception: "+zipe.toString());
            zipe.printStackTrace();
            //continue;
            return false;
        }                
        return result;
    }


    boolean predefineClasses(Vector _names, TargetVMConnector _tvmc, BreakpointEvent _bkpt ) {
        
//        JavaClass jc = com.objs.surveyor.probemeister.bytecoder.Bytecoder.loadClassFromClassFile("projectFiles\\Empty.class");
//        if (jc == null)
//            return false;
        com.objs.surveyor.probemeister.Log.out.fine("predefineClasses:: starting...");
        boolean result = true;
        for (int i = 0; i<_names.size(); i++) {
            String clsName = (String)_names.get(i);
            String osName = clsName;
            clsName = clsName.substring(0, clsName.length()-6);
            clsName = clsName.replace('/', '.'); //change path to pkg name
            
            ClassGen cg = new ClassGen(clsName, "java.lang.Object", 
                      osName, Constants.ACC_PUBLIC | Constants.ACC_SUPER, 
                      null);
            cg.setClassName(clsName);

            //Define a constructor            
            cg.addEmptyConstructor(Constants.ACC_PUBLIC);
            
            JavaClass jc = cg.getJavaClass();
            int pos = cg.getConstantPool().lookupString(clsName);
            if (pos >= 0)
                com.objs.surveyor.probemeister.Log.out.finer("predefineClasses:: found class name in cp at "+pos);
            
            
            com.objs.surveyor.probemeister.Log.out.finer("predefineClasses:: class name = "+jc.getClassName());
            
            byte[] cbytes = jc.getBytes();
            com.objs.surveyor.probemeister.Log.out.finest("predefineClasses:: bytes length = "+cbytes.length);
            ByteArrayInputStream is = new ByteArrayInputStream(cbytes);   
            Object newClass = null;
            try {
                newClass = _tvmc.exportClassToTargetVM((ClassType)_bkpt.location().declaringType() , _bkpt.thread(), is, clsName);                
            } catch (com.objs.surveyor.probemeister.DuplicateClassException dce) {
                com.objs.surveyor.probemeister.Log.out.warning("predefineClasses:: Dup Class Exception");
                //return false;
            } catch (com.objs.surveyor.probemeister.NotAtBreakpointException nabe) {
                com.objs.surveyor.probemeister.Log.out.warning("predefineClasses:: Dup Class Exception");
                //return false;
            } catch (NoClassDefFoundError ncdf) {
                com.objs.surveyor.probemeister.Log.out.warning("predefineClasses:: NoClassDefFoundError Exception");
            } catch (Exception e) {
                com.objs.surveyor.probemeister.Log.out.warning("predefineClasses:: Exception: "+e+"\n e.toString()");
            }
            try {
                if (newClass == null) {
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(new File("temp/sienaDump", osName));
                    is.reset();
                    byte[] in = new byte[is.available()];
                    is.read(in);
                    fos.write(in);
                    fos.close();
                    com.objs.surveyor.probemeister.Log.out.fine("defineFromJar:: class written to "+osName);
                    return false;
                }                                
            } catch (Exception e) {
                com.objs.surveyor.probemeister.Log.out.warning("defineFromJar:: exception writing to disk: "+e);
                return false;
            }
        }            
        return result;
        
    }


    Hashtable loadClassesFromJar(String _jarFileName) {
        Hashtable files = new Hashtable(20,20);
        final boolean debug = true;
        try {
            java.io.FileInputStream fis=new java.io.FileInputStream(_jarFileName);
            java.io.BufferedInputStream bis=new java.io.BufferedInputStream(fis);
            ZipInputStream zis=new ZipInputStream(bis);
            ZipEntry ze=null;
            while ((ze=zis.getNextEntry())!=null) {
                
                if (ze.isDirectory()) {
                    continue; //not a java class
                }
                
                if (!ze.getName().endsWith(".class"))
                    continue; //not a java class
                
                if (debug) {
                    com.objs.surveyor.probemeister.Log.out.finer("ze.getName()="+ze.getName()+","+"getSize()="+ze.getSize());
                }
                int size=(int)ze.getSize();
                if (size==-1) { //size not unknown
                    com.objs.surveyor.probemeister.Log.out.finer("Zip entry size unknown. Skipping file.");
                    //size=((Integer)htSizes.get(ze.getName())).intValue();
                    continue;
                }
                byte[] b=new byte[(int)size];
                int rb=0;
                int chunk=0;
                while (((int)size - rb) > 0) {
                    chunk=zis.read(b,rb,(int)size - rb);
                    if (chunk==-1) {
                    break;
                    }
                    rb+=chunk;
                }
                //store file
                files.put(ze.getName(), new ByteArrayInputStream(b));
                com.objs.surveyor.probemeister.Log.out.finer(ze.getName()+"  rb="+rb+",size="+size+",csize="+ze.getCompressedSize());
            }
            return files;
        } catch (NullPointerException e) {
            com.objs.surveyor.probemeister.Log.out.warning("done.");
        } catch (java.io.FileNotFoundException e) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.WARNING, "File not found", e);
        } catch (java.io.IOException ioe) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.WARNING, "IO Exception", ioe);
        }
        return null;
    }

    void loadClassesFromJar(String _jarFileName, Vector _files, Vector _names) {
        //Hashtable files = new Hashtable(20,20);
        final boolean debug = true;
        try {
            java.io.FileInputStream fis=new java.io.FileInputStream(_jarFileName);
            java.io.BufferedInputStream bis=new java.io.BufferedInputStream(fis);
            ZipInputStream zis=new ZipInputStream(bis);
            ZipEntry ze=null;
            while ((ze=zis.getNextEntry())!=null) {
                
                if (ze.isDirectory()) {
                    continue; //not a java class
                }
                
                if (!ze.getName().endsWith(".class"))
                    continue; //not a java class
                
                com.objs.surveyor.probemeister.Log.out.finer("ze.getName()="+ze.getName()+","+"getSize()="+ze.getSize());
                int size=(int)ze.getSize();
                if (size==-1) { //size not unknown
                    com.objs.surveyor.probemeister.Log.out.finer("Zip entry size unknown. Skipping file.");
                    //size=((Integer)htSizes.get(ze.getName())).intValue();
                    continue;
                }
                byte[] b=new byte[(int)size];
                int rb=0;
                int chunk=0;
                while (((int)size - rb) > 0) {
                    chunk=zis.read(b,rb,(int)size - rb);
                    if (chunk==-1) {
                    break;
                    }
                    rb+=chunk;
                }
                //store file
                _files.add(new ByteArrayInputStream(b));
                _names.add(ze.getName());
                com.objs.surveyor.probemeister.Log.out.finer(ze.getName()+"  rb="+rb+",size="+size+",csize="+ze.getCompressedSize());
            }
        } catch (NullPointerException e) {
            com.objs.surveyor.probemeister.Log.out.finer("done.");
        } catch (java.io.FileNotFoundException e) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.WARNING, "File Not Found", e);
        } catch (java.io.IOException ioe) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.WARNING, "IO Exception", ioe);
        }
    }


}