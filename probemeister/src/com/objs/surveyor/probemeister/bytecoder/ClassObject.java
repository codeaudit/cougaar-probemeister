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

//import com.objs.surveyor.probemeister.Core;
import com.sun.jdi.*;
import com.objs.surveyor.probemeister.TargetVMConnector;
import com.objs.surveyor.probemeister.Globals;
import com.objs.surveyor.probemeister.GlobalVars;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.*;
import java.net.URL;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/*
 * This class maintains data about each class. Since a class may be represented
 * in multiple forms, this class provides attributes for each possible representation.
 * When possible, we attempt to convert from one representation (e.g. bytecode) to
 * another (e.g. source).
 *
 * This class is used internally by Bytecoder, and possibly the ProbeManager[GUI].
 * *Note* That the JVM JDI API and BCEL APIs have class names in common. In this class
 * we primarily use JDI classes.
 */
public class ClassObject {
    
    static final String OBJSProbeCountFieldName = "xxxOBJSPROBECOUNTFIELDxxx";
    
    JavaClass       java_class = null;
	ConstantPool    constants  = null;
	ConstantPoolGen cp         = null;
    ClassGen        classgen   = null;

    private TargetVMConnector_ClassMgr classMgr= null;
    org.apache.bcel.classfile.Method[] bytecodeMethods = null;
    String classname = null;
    String source = null;
    File sourceFile = null;
    File classFile  = null;
    java.util.zip.ZipFile  zipArchive    = null;
    java.util.zip.ZipEntry zipClassFile  = null;
    URL sourceURL = null;
    URL classURL  = null;
    byte[] bytes  = null;
    com.sun.jdi.ReferenceType refType = null;
    List methodNames = null;
    List methods     = null; //{of type com.sun.jdi.Method}
    boolean preparedToEdit = false;

    static final int BUFSIZE = 8192;

    int classProbeCount = 0;

    //Holds the list of MethodObjects created/needed so far
    Hashtable methodObjects = new Hashtable(); //key [com.sun.jdi.Method], value [MethodObject]


    // *** SAVE ALL MODS in this object. Future mods need to be applied
    // to the in-memory version ***
    private ClassObject() {
        
       // *** SET THE CLASS NAME ***
        
        
    }
    
    /*
     * Create a class object via the static getClassObject() method.
     *
     *
     */
    ClassObject(com.sun.jdi.ReferenceType _refType, TargetVMConnector_ClassMgr _mgr) {
        
        refType = _refType;
        classname = refType.name();
        classMgr = _mgr;
        
        //Try to access probe count field value from class file
        try {
            com.sun.jdi.Field f = refType.fieldByName(OBJSProbeCountFieldName);
            if (f != null) {
                int count = ((IntegerValue) refType.getValue(f)).value();
                this.classProbeCount = count;
                com.objs.surveyor.probemeister.Log.out.finest("     ==Probe Count = "+count);
                com.objs.surveyor.probemeister.Log.out.finest("     ==Ref type name"+_refType.name());
            }
        } catch (ClassNotPreparedException cnpe) {            
        } catch (Exception e) {
        }
        
       // *** SET THE CLASS NAME ***
        
        
    }
    
    public TargetVMConnector_ClassMgr getClassMgr() { return classMgr; }
    
    /*
     * Parse class bytecode, populate nec. structures
     * Must be called prior to editing class.
     *
     */
    public boolean prepareToEdit() {
        
        if (this.preparedToEdit)
            return true;
        
        try {
            //Locate / load class from classpath LOCAL classpath - no way now to retrieve
            //class from targetVM...
            
//            List clsPath = ((PathSearchingVirtualMachine)getClassMgr().vmConnector().vm()).classPath();
            String classPath = System.getProperty("java.class.path",".");
            StringTokenizer cpst = new StringTokenizer(classPath, ";");
            java.util.Vector clsPath = new java.util.Vector();
            while (cpst.hasMoreTokens()) {
                clsPath.addElement(cpst.nextToken());
            }

            java.io.InputStream clsIS = Bytecoder.loadClassFileFromClasspath(this, clsPath, this.name());
                                
            //Get class bytecode from file, bytecode, etc.
            if (clsIS != null) {
                java_class = (new ClassParser(clsIS, name())).parse();
                classgen = new ClassGen(java_class);
      	        byte[] b = java_class.getBytes();
                //System.out.println("Current bytes length = "+b.length);
                    	      
                constants  = java_class.getConstantPool();
                cp = new ConstantPoolGen(constants);
                
                //Load method objects
                bytecodeMethods = java_class.getMethods();    

                this.preparedToEdit = true;
                return true;
            } else
                return false;
        } catch (InnerClassFileException ice) {
            com.objs.surveyor.probemeister.Log.out.warning("ClassObject::Exception preparing to edit class ("+name()+"): it's an inner class");
            //return false;
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.WARNING,"ClassObject::Exception preparing to edit class", e);
        }
        return false; //unsuccessful
    }        
    
    protected ConstantPoolGen getConstantPoolGen() { return cp; }

    /*
     *  Call this method to reload a class from its original bytecode.
     *  This will wipe out all past modifications to this class.
     */
    public void reloadClass() {
        this.preparedToEdit = false;
        this.prepareToEdit();
        methodObjects.clear();
    }

    public StatementList createStatementList() {
        try {
            return (new StatementList(this));
        } catch (CannotEditClassException cece) {
            com.objs.surveyor.probemeister.Log.out.warning("Error attempting to edit class.");
        }
        return null;
    }    
    
    
    
    void   setSourceFile(File _srcfile) {sourceFile = _srcfile;}
    File getSourceFile() {return sourceFile;}

    void   setClassFile(File _classfile) {classFile = _classfile;}
    File getClassFile() {return classFile;}

    String getClassName() {return classname;}

    void   setClassFile(java.util.zip.ZipEntry _classfile) {zipClassFile = _classfile;}
    java.util.zip.ZipEntry getZipClassFile() {return zipClassFile;}

    void   setSourceURL(URL _srcURL) {sourceURL = _srcURL;}
    URL    getSourceURL() {return sourceURL;}

    void   setClassURL(URL _classURL) {classURL = _classURL;}
    URL    getClassURL() {return classURL;}

    void   setSource(String _src) {source = _src;}
    //We may need to generate the source from bytecode
    String getSource() {
        if (source != null) 
            return source;
                
        if (bytes != null | classFile != null) { 
            //can we generate the source?
        }
        
        if (sourceFile != null | sourceURL != null) { 
            //Read in source from file   
        }
        
        //If source is procured, assign to source var
        //source = ?;
        
        return null;
    }
    
    void   setBytes(byte[] _bytes) {bytes = _bytes;}
    //We may need to generate bytecode from source
    public byte[] getBytes() {
        
        if (java_class != null) {
            bytes = java_class.getBytes();
            return java_class.getBytes();
        }   
        
        if (bytes != null)
            return bytes;
        
        if (classFile != null | classURL != null) {
            //read in 
            //bytes = ? ;
        }
        
        if (sourceFile != null | sourceURL != null) {
            // should we try to compile?
        }
        
        return null;
            
    }
    
    // ** Use this method for all currently loaded classes **
    // The other class descriptors are for off-line modification of classes.
    // At runtime, the VMReferenceType will be available...
    void setVMReferenceType(com.sun.jdi.ReferenceType _rt) {

        refType = _rt;
        this.setMethods(refType.allMethods());
    }
    
    com.sun.jdi.ReferenceType getVMReferenceType() {return refType;}
 
    String name() { 
        if (classname != null)
            return classname;
        if (refType != null) { //lazy eval.
            classname = refType.name() ;
            return classname;
        }
        if ( classFile != null | classURL != null ) {}
        if ( sourceFile != null | sourceURL != null ) {}
        
        return null;
    }
 
    /* @returns list of com.sun.jdi.Method names
     *
     */
    void setMethodNames(List _names) {methodNames = _names;}
    List getMethodNames() {
        if (methodNames != null)
            return methodNames;
    
        if (methods != null) {
            if (methods.size()>0) { //generate a list of the method names                       
                ArrayList l = new ArrayList();
                for (int i=0; i<methods.size(); i++) {
                    l.add(((com.sun.jdi.Method)methods.get(i)).name());
                }
                methodNames = l;
                return methodNames;
            }
        }
        
        //Any other way we want to generate the list of methods???
        //E.g. use BCEL?
        
        return null;
    }

    void setMethods(List _meths) {methodNames = _meths;}

    /* @returns list of com.sun.jdi.Methods
     *
     */
    List getMethods() {return methodNames;}


    /* @returns MethodObject for specified com.sun.jdi.Method
     *
     */
    public MethodObject getMethodObject(com.sun.jdi.Method _meth) {
    
        //lazily add methods to this hashtable => key [com.sun.jdi.Method], value [MethodObject]
        
        MethodObject mo = (MethodObject)methodObjects.get(_meth);
        if (mo == null) {
            int pos = getIndexForMethodName(_meth.name(), _meth.signature());
            if (pos < 0 ) {
                com.objs.surveyor.probemeister.Log.out.warning("ClassObject:: Cannot find method!");
                return null; // no method, likely an inner class
            }
            mo = new MethodObject(this, _meth, pos );
            methodObjects.put(_meth, mo);
        }
        return mo;
        
    }

    /* Takes modified class code & updates running VM version with new bytecodes.
     *
     */
    boolean postUpdates() {


        //System.out.println("POSTING UPDATES Using ConstantPool: "+cp);
        //Update Probe count in class
        int numProbes = 0;
        Enumeration meths = methodObjects.elements();
        while (meths.hasMoreElements()) {
                
                MethodObject temp = ((MethodObject)meths.nextElement());
                
    /////            numProbes += ((MethodObject)meths.nextElement()).getProbeCount();
                numProbes += temp.getProbeCount();
            
            //Output mods to console
            Boolean send = (Boolean)Globals.globals().get(GlobalVars.SEND_BYTECODE_TO_CONSOLE_BOOLEAN);
            if (send != null && send.booleanValue()) {
                if (temp.getProbeCount() > 0) {
                    //then print out the method byte code to see the probes
                    try {
                        System.out.println("** ClassObject::CODE AFTER: "+temp.getBytecodeMethod().getCode().toString());
                    } catch (Exception e) {
                        com.objs.surveyor.probemeister.Log.out.warning("** ClassObject::PostUpdates() Code Exception"+e);
                    }
                }
            }
                
        }
       
        java_class.setConstantPool(cp.getFinalConstantPool());
  	    byte[] b = java_class.getBytes();

  	    if (b == null || b.length == 0) {
  	        com.objs.surveyor.probemeister.Log.out.warning("No bytes to hand to VM...");
  	        return false;
  	    }

        //Set count even if 0, as the old value might have been more.
        //Set AFTER modifying class as this is an illegal runtime modification.
        //so it's only applicable when dumping to a file.
        setProbeCount(numProbes);
        setCountField(numProbes);
        
        //Save the modified class to the temp directory
        Boolean save = (Boolean)Globals.globals().get(GlobalVars.SAVE_BYTECODE_TO_FILE_BOOLEAN);
        if (save != null && save.booleanValue()) {
            try {
                dumpClass(this.name()+".class", "temp", false);
            } catch (java.io.IOException ioe) { 
                com.objs.surveyor.probemeister.Log.out.warning("** Could not dump class file ["+this.name()+".class] to the ./temp directory**"); 
            }
        }
        
        //System.out.println("New bytes length = "+b.length);
        //Update the class in it's VM
        try {
            return Bytecoder.modifyClass(classMgr.vmConnector(), refType, java_class.getBytes());            
        } catch (Exception e) {
            return false;
        }
    }
    
    /*
     * Dumps modified class to a file or to an archive. If the <b>name</b>
     * argument is NULL, the class will be saved to it's original location.
     * However, if the original location is an archive, that archive file may 
     * be locked, so updates may not be permitted.
     *
     * If the <b>name</b> argument is non-null, the class will be saved to 
     * the specified path + name. If the <b>toArchive</b> argument is TRUE,
     * the class will be saved to a new archive with the given path + name
     * (the contents of the current archive to which that class belonged will
     * also be copied to this new archive file).
     *
     * @return TRUE if successful
     */
    void dumpClass(String _name, String _path, boolean _toArchive) throws java.io.IOException {
        
        //JavaClass tempClass = java_class;
        
        //if () //then we need to update this to get the field addition included        
        //JavaClass tempClass = classgen.getJavaClass();
  	    //tempClass.setConstantPool(cp.getFinalConstantPool());
        
  	    try {
  	        if (_name == null) { 
  	            //*** OVERWRITE ORIGINAL CLASS FILE ***
                //Check to see if the class was a file or zip archive
  	            if (this.classFile != null) { // normal File
/////      	            tempClass.dump(this.classFile);
      	            this.java_class.dump(this.classFile);
      	        } else {
      	            dumpToArchive(java_class, null, null);
/////      	            dumpToArchive(tempClass, null, null);
      	        }
  	        } else { //save to specified path + name
  	            if (_toArchive) { //dump class to archive
 ///// 	                dumpToArchive(tempClass, _name, _path);
  	                dumpToArchive(java_class, _name, _path);
  	            } else {
 ///// 	                tempClass.dump(new File(_path, _name));    
                    File out = new File(_path, _name);
                    int i=0;
                    while (out.exists()) { //save to a new file, don't overwrite
                        out = new File(_path, _name+(i++));
                    }
  	                com.objs.surveyor.probemeister.Log.out.info("Dumping modified class to: "+out.getAbsolutePath());    
  	                java_class.dump(out);    
  	            }
  	        }
        } catch (Exception ioe) {
            com.objs.surveyor.probemeister.Log.out.warning("IO Exception dumping bytecode file.");
        }
        //return false;
    }

    void setProbeCount(int c) { classProbeCount = c; }

    /*
     *
     * @return TRUE if the field was added, false otherwise   
     */
    boolean setCountField(int count) {
        
        com.objs.surveyor.probemeister.Log.out.finest("setCountField...");
        
        org.apache.bcel.classfile.Field[] ffs = java_class.getFields();            
        boolean found = false;
        
        for (int i = 0; i<ffs.length; i++) {
//            System.out.println("   Searching for field...");
            org.apache.bcel.classfile.Field ff = ffs[i];
            if (ff.getName().equals(OBJSProbeCountFieldName)) {
                ConstantValue cv = ff.getConstantValue();
                int pos = cv.getConstantValueIndex();
                Constant co = cp.getConstant(pos);
                if (co instanceof ConstantInteger) {
                    ConstantInteger ci = (ConstantInteger)co;
                    ci.setBytes(count);
//                    System.out.println("   Updated probe count field value");
                    return true;
                }
            }
        }           
        
        //No such field, so let's create one IF the count > 0
        if (count == 0) return false; //no probes so no need to add field.
//        System.out.println("   No probe count field found, creating one.");

        org.apache.bcel.generic.FieldGen fg = new org.apache.bcel.generic.FieldGen(0, org.apache.bcel.generic.Type.INT, 
                                            OBJSProbeCountFieldName, cp);  
        fg.isFinal(true);
        fg.isStatic(true);
        fg.setInitValue(count);
        classgen.addField(fg.getField());
//        System.out.println("   Added field...");
        //java_class = classgen.getJavaClass(); // do NOT do - this would attempt to apply updates to
            //the RT class -- the VM does not permit adding of static vars.
                    
        return true;
    }


    void updateBytecodeMethodArray(org.apache.bcel.classfile.Method _m, int index) {
        bytecodeMethods[index] = _m;
    }
    
    /* @returns org.apache.bcel.classfile.Method with specified name
     *
     */
    org.apache.bcel.classfile.Method getBytecodeMethodAtIndex(int _pos) {
        if (_pos>=0 && _pos<bytecodeMethods.length)
            return bytecodeMethods[_pos];
        else 
            return null;
    }

    int getIndexForMethodName(String _meth, String _sig) {

        if (!preparedToEdit) this.prepareToEdit(); //needed to generate JavaClass object

        //bytecodeMethods is populated if the class is not abstract or an inner class
        if (bytecodeMethods == null) return -1; //no methods exist, e.g. if inner class
     
        org.apache.bcel.classfile.Method meth = null;
        
        int pos =-1;
        for (int i=0; i<bytecodeMethods.length; i++) {
            
            meth = bytecodeMethods[i];
            if (meth.getName().equals(_meth) && meth.getSignature().equals(_sig)) {
                pos = i;
                break;
            }
        }
        return pos;
    }
    
    
    boolean dumpToArchive(JavaClass _javaClass, String _name, String _path) {
    
        if (this.zipArchive == null) {
            com.objs.surveyor.probemeister.Log.out.warning("Zip Archive Attribute is null, cannot dump class to null archive.");
            return false;
        }
        
        try {
            File newArchive = new File("PM_tempdump"+this.classname);
            FileOutputStream fout = new FileOutputStream(newArchive);
            ZipOutputStream zout = new ZipOutputStream(fout);
            
            Enumeration entries = this.zipArchive.entries();
            byte buf[] = new byte[BUFSIZE];
            
            InputStream zin = null; 
            while (entries.hasMoreElements()) {
                //Get next entry in original file
                ZipEntry ze = (ZipEntry)entries.nextElement();
                //Write next entry to new file
                
                //Check if this is the same as the entry we want to replace...
                if (ze.getName().equals(this.zipClassFile.getName())) { 
                    //Set ZipEntry size to new length & create InputStream to modified bytes
                    byte[] tbuf = _javaClass.getBytes();
                    zin = new java.io.ByteArrayInputStream(tbuf); 
                    ze.setSize((long)tbuf.length);
                    zout.putNextEntry(ze);
                    com.objs.surveyor.probemeister.Log.out.fine("Wrote modified class " +ze.getName()+ " to archive.");
                } else { //Not modifying this class, so just write out the ZipEntry & data
                    zout.putNextEntry(ze);
                    zin = this.zipArchive.getInputStream(ze);            
                }
                //read from inputstream
                int pos = 0;
                int read = 0;
                while ((read = zin.read(buf, pos, BUFSIZE))> 0) {
                    zout.write(buf, pos, read);        
                    pos += read; //adv ptr   
                }
                zout.closeEntry();
                zin.close();
            }
            
            zout.close();
            
            //rename archive to path+name or to original name (ZipFile.getName()?)
            if (_name != null) { //rename to path + name
                newArchive.renameTo(new File(_path, _name));
            } else { // rename to original name
                //do we need to delete the old one first?
                newArchive.renameTo(new File(this.zipArchive.getName()));
            }
            
            com.objs.surveyor.probemeister.Log.out.fine("Finished writing archive to: "+newArchive.getAbsolutePath());
            return true;
            
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.warning("Error writing to archive.");
            e.printStackTrace();
        }
        return false;
    }

    
}
