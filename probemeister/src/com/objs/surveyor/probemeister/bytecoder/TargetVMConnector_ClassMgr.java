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
public class TargetVMConnector_ClassMgr {
    
    
    //Hold s list of all classobjects instantiated
    static Vector classObjects;
    static {
        classObjects = new Vector(100);
    }
    

    private TargetVMConnector vmConnector = null;    
    static final int BUFSIZE = 8192;

    public TargetVMConnector_ClassMgr(TargetVMConnector _tvm) {        
        vmConnector = _tvm;        
    }
    

    /*
     * This routine ensures that only one ClassObject exists for any
     * class loaded, and that it is reused so changes do not overwrite
     * older ones... though having this be an option is a good idea.
     */
    public ClassObject getClassObject(com.sun.jdi.ReferenceType _refType) {
        
        ClassObject co = null;
        
        for (int i=0; i< classObjects.size(); i++) {
            co = (ClassObject)classObjects.get(i);
            if (co.refType.equals(_refType))
                return co;
        }
        //Not found, create a new one.
        co = new ClassObject(_refType, this);
        classObjects.add(co);
                
        return co;
        
    }
            
    public TargetVMConnector vmConnector() {return vmConnector;}
        
}