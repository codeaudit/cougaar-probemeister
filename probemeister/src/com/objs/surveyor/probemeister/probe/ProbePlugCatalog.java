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

// Object Services & Consulting, Inc. All Rights Reserved. 2001
// Author: Paul Pazandak
// Date:   16-Oct-01
// Modified 10Feb03 - Now, FIRST checks for catalog if in a jar, then checks for local file.
package com.objs.surveyor.probemeister.probe;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.StringTokenizer;
import java.io.File;

import org.apache.bcel.classfile.JavaClass;
//import com.objs.surveyor.probemeister.Core;
import com.objs.surveyor.probemeister.bytecoder.Bytecoder;
import org.apache.bcel.classfile.ClassParser;

/*
 *  This class is used to maintain a list of ProbePlugs that have been defined
 * for use. It reads the list of ProbePlugs in by class name, each defined on
 * a separate line in the ProbePlugCatalogDB.txt file.
 */
public class ProbePlugCatalog {
    
    static final String g_CatalogDBFile = "ProbePlugCatalogDB.txt";
    
    // store for the ProbePlugs defined in the classes in the catalog file 
    private static java.util.Vector plugCatalog; 
    
    //Attribute holds the one instance of ProbeControl
    private static ProbePlugCatalog cataloger;
    
    //ProbePlugCatalog per TargetVMConnector - NO just one
    //TargetVMConnector vmConnector; 
    
    static {
        ProbePlugCatalog.cataloger = new ProbePlugCatalog();
    }
    
    private ProbePlugCatalog() {       
        plugCatalog = new java.util.Vector();
        
        // read in ProbePlugs (into plugCatalog) from file, if they exist.
        readInDB();
    }        
        
    /* Returns an instance of ProbePlugCatalog (there is only one - one per tvmc!) */
    public static ProbePlugCatalog getCataloger() { return cataloger; }

    //public Class[] getProbePlugs() { return this.plugCatalog; }
    
    /*
     * This class finds all ProbePlugs that have the specified signature.
     * @return A (possibly empty) array of ProbePlugEntry instances of compatible ProbePlugs.
     */
    public ProbePlugEntry[] findCompatiblePlugs(String _sig){
        
        ProbePlugEntry[] plugs;
        java.util.Vector temp = new java.util.Vector();
com.objs.surveyor.probemeister.Log.out.fine("ProbePlugCatalog::findCompatiblePlugs, called with sig="+_sig);    
        java.util.Enumeration e = plugCatalog.elements();
        while (e.hasMoreElements()) {
            ProbePlugEntry pe = (ProbePlugEntry)e.nextElement();    
com.objs.surveyor.probemeister.Log.out.finer("ProbePlugCatalog::findCompatiblePlugs, comparing to: "+pe.getSig());    
            if (pe.getSig().equals(_sig)) 
                temp.add(pe);        
        }
        plugs = new ProbePlugEntry[temp.size()];
        temp.copyInto(plugs);
        return plugs;
    }


    /*
     * This class finds all ProbePlugs that have the specified signature.
     * @return A (possibly empty) array of ProbePlugEntry instances of compatible ProbePlugs.
     */
    public ProbePlugEntry findPlugByName(String _cls, String _name){
        
        ProbePlugEntry plug = null;
        com.objs.surveyor.probemeister.Log.out.fine("ProbePlugCatalog::findPlugByName called");    
        java.util.Enumeration e = plugCatalog.elements();
        while (e.hasMoreElements()) {
            ProbePlugEntry pe = (ProbePlugEntry)e.nextElement();    
            if (pe.getClassName().equals(_cls) && pe.getMethodName().equals(_name)) 
                return pe;
        }
        return null;
    }
    
    
    private void readInDB() {
    
        File initFile = null;
        //Load DB of plugCatalog, if one.        
        try {
            Reader reader;
            //Open file
            try { 
               InputStream is = this.getClass().getResourceAsStream("/"+g_CatalogDBFile);
                //InputStreamReader 
                reader = new InputStreamReader(is);
            } catch (Exception e) { //now try reading local file...
               initFile = new File(g_CatalogDBFile);
               reader = new FileReader(initFile);
            }
            
            com.objs.surveyor.probemeister.Log.out.info("ProbePlugCatalog::===> ProbePlug catalog File Found.");
                        
            //Read data
            //1. Set up BufferedReader which has a readLine() method
            BufferedReader bReader = new BufferedReader(reader);  
            for (;;) { 
                String data = bReader.readLine();
                if (data != null) { // non-null until EOF
                    if (data.length() < 5 || data.startsWith("#")) {
                        com.objs.surveyor.probemeister.Log.out.finest("ProbePlugCatalog::ignoring line in file - comment or length <5");
                        continue;   //essentially empty line
                    }
                    //System.out.println("Read:      "+ String.valueOf(data));
                    //Parse & Add to the list of plugCatalog 
                    if (!importDBFileEntry(data)) { //Error processing data, try to keep processing
                        com.objs.surveyor.probemeister.Log.out.severe("ProbePlugCatalog::Error in ProbePlug catalog file");            
                        com.objs.surveyor.probemeister.Log.out.severe("ProbePlugCatalog::=> Each line should contain one class name with no addition punctuation.");
                    } else
                        com.objs.surveyor.probemeister.Log.out.info("ProbePlugCatalog::=> ProbePlug "+data+" has been imported.");
                } else { //no more data, EOF
                    break; 
                }
            }
            //All done, close file.
            bReader.close();

        } catch (java.io.FileNotFoundException fnf) {
            //Assume that the file never existed, so we have no plugCatalog to load. OK to stop.
            String path = (initFile == null) ? "unknown path/"+g_CatalogDBFile : initFile.getAbsolutePath();
            com.objs.surveyor.probemeister.Log.out.severe("ProbePlugCatalog::No ProbePlug catalog found. Looking for: " + path);
        } catch (java.io.IOException ioe) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.SEVERE, "ProbePlugCatalog::IO Exception reading file...", ioe);
        } catch (NullPointerException npe) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.SEVERE, "ProbePlugCatalog::IO Exception reading file...", npe);            
        }
    }
    
    /**
      * This routine converts the strings to BCEL java_class objects, and
      * then examines the class & extracts all probe plugs. The plugs are
      * added to this.plugsCatalog 
      */
    private boolean importDBFileEntry(String _classNameAndDesc) {
        
        String className = null;
        String desc="";

        //Delimiter is a ':'
        StringTokenizer st = new StringTokenizer(_classNameAndDesc, ":");
        
        //Process Data - extract probe plug class name & description
        if (st.hasMoreTokens()) 
           className = st.nextToken();

        if (className == null) //no go, stop processing this entry
            return false;
        
        if (st.hasMoreTokens()) { //then get the description
            desc = st.nextToken();
        }
                
        //Now, process the ProbePlug class & ensure it exists.
        //Then, call another routine to identify all ProbePlug methods --
        //they MUST begin with "PP_"
        JavaClass java_class=null;
        try {
            //Locate / load class from classpath OF THE LOCAL VM
            String classPath = System.getProperty("java.class.path",".");
com.objs.surveyor.probemeister.Log.out.fine("ProbePlugCatalog:: local classpath = "+classPath);            
            StringTokenizer cpst = new StringTokenizer(classPath, ";");
            java.util.Vector clsPath = new java.util.Vector();
            while (cpst.hasMoreTokens()) {
                clsPath.addElement(cpst.nextToken());
            }
            //java.util.List clsPath = ((PathSearchingVirtualMachine)vmConnector.vm()).classPath();
            char sep = java.io.File.separatorChar; //make this platform independent
            java.io.InputStream clsIS = Bytecoder.loadClassFileFromClasspath(null, clsPath, className); //.replace('.', sep));
                                
            //Get class bytecode from file, bytecode, etc.
            if (clsIS != null) {
                java_class = (new ClassParser(clsIS, className)).parse();
            } else return false;
            
            //Extract all probe plugs
            if (java_class != null) {
                int numFound = processProbePlugClass(java_class, className, desc);
                com.objs.surveyor.probemeister.Log.out.info("ProbePlugCatalog:: "+numFound+" probes found in "+className);
            } else return false;
        }
        catch(Exception e) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.WARNING,"ProbePlugCatalog:: Error processing "+ className, e);
            return false;
        }
        return true;
    
    }
    
    /* Locates all probe plugs within a given class & registers them in this catalog 
     * @return the number of probe methods found & cataloged.
     */
    int processProbePlugClass(JavaClass _probeClass, String _className, String _desc) {

        int count = 0;
        try {
            //All methods in this class by Method object
            org.apache.bcel.classfile.Method[] classMethods = _probeClass.getMethods();
            if (classMethods == null || classMethods.length == 0) return 0;
            
            //Get each method, it's desc, params & retval
            for (int i=0; i< classMethods.length; i++) {
                
                org.apache.bcel.classfile.Method meth = classMethods[i];                
                String methName = meth.getName();
                com.objs.surveyor.probemeister.Log.out.info("ProbePlugCatalog:: Inspecting method: "+methName);
                
                //Only process this method if it starts with "PP_" and it's STATIC!
                if (methName.startsWith("PP_") && meth.isStatic()) {                    
                    String sig = meth.getSignature();                    
                    ProbePlugEntry pe = new ProbePlugEntry(_className, methName, _desc, sig);
                    this.plugCatalog.add(pe);
                    count++;
                }
            }
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.log(java.util.logging.Level.WARNING,"ProbePlugCatalog:: Exception importing probe entry" , e);
        }
        return count;
    }
}




