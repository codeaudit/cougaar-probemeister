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
import  com.objs.surveyor.probemeister.gui.MethodFilterList;

import java.io.StringReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.util.Vector;
import java.util.Hashtable;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


//This class is used to record instrumentation activity. Any time
//modifications are made to the target VM, they must be recorded here.
//The mod records must be detailed enough so that they can be reapplied
//at another time. One recorder per VM.
//There should be an InstrumentationRecorder per TargetVMConnector.
//The class also contains static methods to deserialize the records from
//a string or file.
public class InstrumentationRecorder {
 
    TargetVMConnector tvmc = null;
    private InstrumentationRecordSet log;
 
    public InstrumentationRecorder(TargetVMConnector _tvmc) {
        
        tvmc = _tvmc;
        log = new InstrumentationRecordSet("CurrentSet", "CurrentSet of Instrumentation actions");
    }
 
    public int getSize() {return log.getSize();}
 
    public void recordAction(InstrumentationRecord _ir) {

        //Set time
        _ir.setTime(System.currentTimeMillis());
    
        //Store record
        log.addRecord(_ir);
        tvmc.emitVMEvent(TargetVMConnector.VMINSTRUMENTED, _ir);
    }

    public boolean removeAction(String id) {

        for (int i=0; i<log.getSize(); i++) {
            InstrumentationRecord ir = log.getRecord(i);
            if (ir instanceof InstrumentationRecord_Stub || ir instanceof InstrumentationRecord_Probe) {
                if (ir.getID().equals(id)) {
                    log.removeRecord(i);
                    com.objs.surveyor.probemeister.Log.out.warning("InstrumentationRecorder: Removed configuration entry with id = "+id);                    
                    return true;
                }
            } else
                com.objs.surveyor.probemeister.Log.out.warning("InstrumentationRecorder: Cannot remove entries (id="+id+") of type "+ir.getClass().getName());
            
        }
        return false;        
    }
    
    
    /* Return current set in serialized XML form */
    public String getCurrentSerializedSet() { 
        return log.serializeSet(); 
    }

    /* Return current set */
    public InstrumentationRecordSet getCurrentSet() { 
        return log; 
    }

    /* Return hashtable of current classes & methods that have probes in them */
    public MethodFilterList getCurrentClassMethods() { 

        MethodFilterList list = new MethodFilterList();
        
        for (int i=0; i<log.getSize(); i++) {
            InstrumentationRecord ir = log.getRecord(i);
            if (ir instanceof InstrumentationRecord_Probe) {
                InstrumentationRecord_Probe irp = (InstrumentationRecord_Probe)ir;                    
                list.addMethod(irp.getClassName(), irp.getMethodName());
            } else
            if (ir instanceof InstrumentationRecord_Stub) {
                InstrumentationRecord_Stub irs = (InstrumentationRecord_Stub)ir;                    
                list.addMethod(irs.getClassName(), irs.getMethodName());
            }
            
        }
        return list;
    }


    /* Calls reapply on each record in the set. NewClass instrumentations 
     * can only be applied if we're at a breakpoint, so _bkpt cannot be null.
     * If null, then those modifications will not be applied.
     *
     * This method takes a serialized set of InstrumentationRecords from a file
     * @return set of records that could NOT be applied.
     */
    public static InstrumentationRecordSet playConfiguration(TargetVMConnector _tvmc, File _file, com.sun.jdi.event.BreakpointEvent _bkpt) 
        throws InstrumentationParsingException {
        try {
            InstrumentationRecordSet config = deserialize(new InputSource(new FileReader(_file)));
            com.objs.surveyor.probemeister.Log.out.fine("InstrumentationRecorder:: Playing configuration... deserialized "+config.getSize()+" records...");     
            return playConfiguration(_tvmc, config, _bkpt);
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.warning("InstrumentationRecorder.playConfiguration() Exception: "+e);
            return null;
        }            
    }

    /* Calls reapply on each record in the set. NewClass instrumentations 
     * can only be applied if we're at a breakpoint, so _bkpt cannot be null.
     * If null, then those modifications will not be applied.
     *
     * This method takes a serialized set of InstrumentationRecords from a String
     * @return set of records that could NOT be applied.
     */
    public static InstrumentationRecordSet playConfiguration(TargetVMConnector _tvmc, String _set, com.sun.jdi.event.BreakpointEvent _bkpt) 
            throws InstrumentationParsingException {
        
        InstrumentationRecordSet config = deserialize(new InputSource(new StringReader(_set)));
        com.objs.surveyor.probemeister.Log.out.fine("InstrumentationRecorder:: Playing configuration... deserialized "+config.getSize()+" records...");     
        
        return playConfiguration(_tvmc, config, _bkpt);
    }
    
    /* Calls reapply on each record in the set. NewClass instrumentations 
     * can only be applied if we're at a breakpoint, so _bkpt cannot be null.
     * If null, then those modifications will not be applied.
     *
     * This method takes a set of InstrumentationRecords.
     * @return the same set of records with error strings in records that could NOT be applied.
     */
    public static InstrumentationRecordSet playConfiguration(TargetVMConnector _tvmc, InstrumentationRecordSet _set, com.sun.jdi.event.BreakpointEvent _bkpt) 
    {    //throws InstrumentationParsingException {

//        InstrumentationRecordSet errorSet = new InstrumentationRecordSet("ErrorSet", "Records that could not be applied");
        boolean result=true; //overall result
        boolean rez=true;    //result of each reapply() invocation
        if (_set == null) return null;
        int len = _set.getSize();
        for (int i = 0; i<len; i++) {
            InstrumentationRecord rec = _set.getRecord(i);
            if (rec instanceof InstrumentationRecord_NewClass) {
                if (_bkpt != null)
                    rez = ((InstrumentationRecord_NewClass)rec).reapplyAction(_tvmc, _bkpt);
                //else do nothing
            } else
                rez = rec.reapplyAction(_tvmc);
            if (!rez) {
                //increment error count
                _set.setErrorCount(_set.getErrorCount()+1);
                rec.setError("Error reapplying record.");
                com.objs.surveyor.probemeister.Log.out.warning("InstrumentationRecorder:: Error reapplying record: "+
                                "      =====> \n"+ rec.toString());
            }
            else
                com.objs.surveyor.probemeister.Log.out.fine("  ====> applied record successfully.");
            
        }
        return _set;
    }
    
    /* write the current log to a file */
    public boolean storeRecords(File _file) { 
        
        try {
            FileWriter fw = new FileWriter(_file);
            String data = getCurrentSerializedSet();        
            fw.write(data);
            fw.close();
        } catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.warning("InstrumentationRecorder.storeRecords() Exception: "+e);        
            return false;
        }
        return true;
    } 
    
    //show XML processing messages
    private static boolean showXMLProcessing = false;
    public static void showXMLMessages(boolean _b) { showXMLProcessing = true; }
    public static boolean showXMLMessages() { return showXMLProcessing; }
    
    static String xml = "<begin>"+
                        "<start name=\"hello\" size=\"10\"></start>\n"+
                        "<foo da=\"hello\" fee=\"10\"></foo>" +
                        "</begin>";

    
    public static void main(String[] args) {

        InstrumentationRecord_NewClass ir = new InstrumentationRecord_NewClass("theFile", "theClass"); 
        InstrumentationRecordSerialized irs = new InstrumentationRecordSerialized(ir);
        irs.addAttrValPair("probeID", "pid123");
        irs.addAttrValPair("desc", "A nice probe");
     
        try {
        deserialize(new InputSource(new StringReader(irs.toString())));
        }catch (Exception e) {
            com.objs.surveyor.probemeister.Log.out.warning("Exception: "+e);            
        }



/*
        try {
            deserialize(new InputSource(new FileReader(new File("xmltest.txt"))));
        }catch (Exception e) {
            System.out.println("Exception: "+e);            
        }
*/        
    }
    
    public static boolean hasBreakpointActions(InputSource _config) 
        throws InstrumentationParsingException {
        
        InstrumentationRecordSet set = InstrumentationRecorder.deserialize(_config);
        return set.requiresBreakpoint();
    }
    
    
    /* Replaces '<' and '>' with '&lt' and '&gt' so the XML parser 
     * can parse the string correctly. 
     */
    public static String xmlizeString(String s) {
        StringBuffer str = new StringBuffer(s);
        
        //get first/common case over with
        if (s.equals("<init>")) {
            return "&lt;init&gt;";
        }
        
        int pos = 0;
        int i;
        while ((i = s.indexOf('<', pos))>-1) {
            str.replace(i,i, "&lt;");
            pos = i+1;
        }

        pos = 0;
        while ((i = s.indexOf('>', pos))>-1) {
            str.replace(i,i, "&gt;");
            pos = i+1;
        }
        return str.toString();
    }
    
    
    
    //Static routine so it can be called at any time. Loads file of records
    //into a vector for viewing or replaying.
    // @return Vector of InstrumentationRecords contained in serialized source    
    public static InstrumentationRecordSet deserialize(InputSource _in) 
            throws InstrumentationParsingException { 
        com.objs.surveyor.probemeister.Log.out.fine("InstrumentationRecorder:: Playing configuration... deserializing source");     
        //showXMLMessages(true);
        
        InstrumentationRecordSet recordSet = new InstrumentationRecordSet("deserialized","");
        
        javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
        try {
           javax.xml.parsers.SAXParser saxParser = factory.newSAXParser();
           DefaultHandler handler = new Objectizer(recordSet);
           saxParser.parse(_in, handler); 
        } 
        catch (javax.xml.parsers.ParserConfigurationException e) {            
            com.objs.surveyor.probemeister.Log.out.warning("Exception: "+e);
            exceptionThrown(e);
        } 
        catch (SAXException se) { 
            Exception e = se.getException(); 
            com.objs.surveyor.probemeister.Log.out.warning("Exception: "+se);
            exceptionThrown((e == null) ? se : e); 
        }
        catch (java.io.IOException ioe) { 
            com.objs.surveyor.probemeister.Log.out.warning("Exception: "+ioe);
            exceptionThrown(ioe); 
        }

        if (InstrumentationRecorder.showXMLMessages()) 
            com.objs.surveyor.probemeister.Log.out.fine(""+recordSet.getSize()+" records deserialized.");
        return recordSet;
    } 
    
    static void exceptionThrown(Exception _e) throws InstrumentationParsingException {        
           throw new InstrumentationParsingException("Exception while parsing", _e);
    }     
    
}

//Does the real work of deserializing InstrumentationRecords
class Objectizer extends org.xml.sax.helpers.DefaultHandler {
    
    InstrumentationRecord currentRec = null;
    InstrumentationRecordSet recordSet = null;
    
    //GroupTag attrs
      boolean inGroup = false;
      Hashtable groupParams;
      String groupName = null;
    
    Objectizer(InstrumentationRecordSet _set) { 
        super(); 
        recordSet = _set; 
    }

    public void startElement(String namespaceURI, String localName,
                         String qName, Attributes atts) throws SAXException {

        try {
            if (InstrumentationRecorder.showXMLMessages()) {
                com.objs.surveyor.probemeister.Log.out.finest("StartElement: "+ qName);
                com.objs.surveyor.probemeister.Log.out.finest("Attrs: ");
                if (atts != null) {
                    for (int i=0; i<atts.getLength(); i++) {
                        String name = atts.getQName(i);
                        String val  = atts.getValue(i);
                        com.objs.surveyor.probemeister.Log.out.finest("    Name = "+name+"  Value = "+val);
                    }
                }
            }
                
            if (qName.equals(InstrumentationRecordSet.setTag)) { //process record
                if (InstrumentationRecorder.showXMLMessages()) 
                    com.objs.surveyor.probemeister.Log.out.finer("   In InstrumentationSet..."+qName);
              
                String setName = atts.getValue(InstrumentationRecordSet.nameTag);
                String setDesc = atts.getValue(InstrumentationRecordSet.descTag);
                recordSet.setName(setName);
                recordSet.setDescription(setDesc);
                
            } else if (qName.equals(InstrumentationRecordSerialized.recordTag)) { //process record
                if (InstrumentationRecorder.showXMLMessages()) 
                    com.objs.surveyor.probemeister.Log.out.finer("   In InstrumentationRecordSerialized..."+qName);
                
                inGroup = false;
                
                try {
             
                    String recClsName = atts.getValue(InstrumentationRecordSerialized.recTypeTag);
                    Class ir_class = Class.forName(recClsName);
                    InstrumentationRecord ir = (InstrumentationRecord) ir_class.newInstance();
                    
                    String targetClsName = atts.getValue(InstrumentationRecordSerialized.recTargetTag);
                    ir.setClassName(targetClsName);
                    
                    String time = atts.getValue(InstrumentationRecordSerialized.recTimeTag);
                    if (time == null) time="0";
                    ir.setTime(Long.decode(time).longValue());
                    
                    currentRec = ir;
                    recordSet.addRecord(ir); //add new record to vector
                    
                    if (InstrumentationRecorder.showXMLMessages()) {
                        com.objs.surveyor.probemeister.Log.out.finer("Added record to set.");
                        com.objs.surveyor.probemeister.Log.out.finest("   Now..."+recordSet.getSize()+" records deserialized.");
                    }
                } catch (Exception e) {
                    if (InstrumentationRecorder.showXMLMessages()) 
                        com.objs.surveyor.probemeister.Log.out.warning("   In InstrumentationRecordSerialized...exception: "+e);
                    throw new SAXException("Error Parsing Records");                    
                }                
            } else if (qName.equals(InstrumentationRecordSerialized.attrTag)) {
                //This is an attr value pair element - get the attr name and its value
                String attrName = atts.getValue(InstrumentationRecordSerialized.attrNameTag);
                String attrVal  = atts.getValue(InstrumentationRecordSerialized.attrValTag);
                
                //Now, send the pair to the current InstrumentationRecord for processing
                if (currentRec != null) {
                    if (inGroup && groupParams!=null) //then just add attr/val to hashtable
                        groupParams.put(attrName, attrVal);
                    else
                        currentRec.processAttribute(attrName, attrVal);                 
                }
            } else if (qName.equals(InstrumentationRecordSerialized.groupTag)) {
                if (inGroup) {
                    if (InstrumentationRecorder.showXMLMessages()) 
                        com.objs.surveyor.probemeister.Log.out.warning("XML Parse Error: Didn't see closing group tag: "+groupName);
                }
                inGroup = true;
                groupParams = new Hashtable(5);
                groupName = atts.getValue(InstrumentationRecordSerialized.groupNameTag);                
            } else
                if (InstrumentationRecorder.showXMLMessages()) {
                    com.objs.surveyor.probemeister.Log.out.warning("   Saw tag I didn't recognize: "+qName);
                }
            
        } catch (Exception e) {
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) 
        throws SAXException {        
        if (InstrumentationRecorder.showXMLMessages()) 
            com.objs.surveyor.probemeister.Log.out.finest("Saw element end: "+qName);

        if (qName.equals(InstrumentationRecordSerialized.groupTag)) {
            if (currentRec != null & inGroup)
                currentRec.processGroup(groupName, groupParams);
            inGroup = false;            
        }
    }
    
    public void startDocument() {        
        if (InstrumentationRecorder.showXMLMessages()) 
            com.objs.surveyor.probemeister.Log.out.fine("Starting to parse document---------------------");
    }    
    public void endDocument() throws SAXException {        
        if (InstrumentationRecorder.showXMLMessages()) 
            com.objs.surveyor.probemeister.Log.out.fine("Saw document end------------------------");
    }
    
    
}
    