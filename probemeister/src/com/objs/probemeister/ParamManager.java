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
 * ParamManager.java
 *
 * Created on July 19, 2002, 4:01 PM
 */

/**
 *
 * @author  Administrator
 * @version 
 */

package com.objs.probemeister;
import java.util.*;
import java.io.*;

public class ParamManager {

    Properties props = new Properties();    
    File pFile;
        
    
    /** Creates new ParamManager */
    public ParamManager(String _initFile) {
        pFile = new File(_initFile);
        loadParams();
    }

    Properties loadParams() {


        System.out.println("Looking for file: " + pFile.getAbsolutePath());

        try {
            //Open file
            FileReader reader = new FileReader(pFile);
            System.out.println("===> File Found.");
            
            
            //Read data
            //1. Set up BufferedReader which has a readLine() method
            BufferedReader bReader = new BufferedReader(reader);  
            for (;;) { 
                String data = bReader.readLine();
                if (data != null) { // non-null until EOF
                    if (data.length() >0)
                        System.out.println("Read:      "+ String.valueOf(data));
                    //Store data in the global properties list (props)
                    if (!processData(data)) { //Error processing data, try to keep processing
                        System.out.println("Error in .dat file, unpaired attribute found");            
                        System.out.println("<Attr-Value Pairs should be '=' delimited, one pair to a line.>");
                    }
                } else { //no more data, EOF
                    break; 
                }
            }
            //All done, close file.
            bReader.close();

        } catch (java.io.FileNotFoundException fnf) {
            System.out.println("File Not Found...");
        } catch (java.io.IOException ioe) {
            System.out.println("IO Exception reading file...");
            ioe.printStackTrace();
        }
        return props;
    }
    
    /**
      * This routine takes a string and extracts '=' delimited attribute-value
      * pairs & stores them into a properties list (used at least to read the gInitFile)
      */
    boolean processData(String data) {
        
        //Init structs
        StringTokenizer st = new StringTokenizer(data, "= ");
        String attr  = null;
        String value = null;
        
        //Process Data
        while (st.hasMoreTokens()) {
           attr = st.nextToken();
           if (st.hasMoreTokens()) 
                value = st.nextToken();
           else //error -- no paired value!
                return false;
           props.put(attr, value);
           System.out.println("Processed: attr = "+attr +" value = "+value);
        }
        return true;
    
    }
    

    /**
      * This routine returns a value given its attr name read in from the init file.
      */
    public String getPropertyValue(String attrName) {        
        if (props == null) return null;
        return (String) props.getProperty(attrName);
    }
    

    
}
