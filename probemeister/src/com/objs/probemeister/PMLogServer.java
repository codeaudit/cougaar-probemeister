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
 * PMLogServer.java
 *
 * Created on July 19, 2002, 4:08 PM
 */

/**
 *
 * @author  Administrator
 * @version 
 */
package com.objs.probemeister;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
//import java.io.PrintStream;
import java.util.Vector;
    
import javax.swing.text.SimpleAttributeSet;

    
    public class PMLogServer extends Thread {

        static int threadCount = 0; //incremented by each new thread
                                    //used to get a unique font style for each thread.

        static int PORT = 7887;
        static final String DEFAULT_RECSTART_DELIMITER = "<record>";
        static final String DEFAULT_RECEND_DELIMITER = "</record>";
        static final String DEFAULT_LOGSTART_DELIMITER = "<log>";
        static final String DEFAULT_LOGEND_DELIMITER = "</log>";
        
        static Vector servers = new Vector(10);
        
        LoggerWindow sendToPM = null;
        ServerSocket server = null;        
        PMLogConsumer consumer = null; //process that consumes the log data
        String recStart, recEnd, logStart, logEnd;
        int port=0;
        String name; //user identifier for logger
        
        //Return an existing log server if one exists.
        public static PMLogServer addServer(int _port, LoggerWindow _sendToPM, String _name) { 
            
            //see if this server already exists...
            for (int i=0; i<servers.size(); i++) {
                PMLogServer pls = (PMLogServer)servers.elementAt(i);
                if (pls.port == _port)
                    return pls;
            }
            return new PMLogServer(_port, _sendToPM, _name);

        }
        
        public PMLogServer(int _port, LoggerWindow _sendToPM, String _name) { 
            this(_port, (PMLogConsumer)null, (ParamManager)null, _sendToPM, _name); 
        }
            
        public PMLogServer(int _port) { 
            this(_port, (PMLogConsumer)null, (ParamManager)null); 
            System.out.println("Defaulting to "+DEFAULT_RECSTART_DELIMITER+" and "+DEFAULT_RECEND_DELIMITER+" delimiters for event stream");
            System.out.println("No event consumer, data will be emitted to the console.");
        }

        public PMLogServer(int _port, PMLogConsumer _lc, ParamManager _params) {
            this(_port, _lc, _params, null, ""); 
        }

        public PMLogServer(int _port, PMLogConsumer _lc, ParamManager _params, LoggerWindow _sendToPM, String _name) {
            port = _port;
            consumer = _lc;
            recStart = getRecordStartDelimiter(_params);
            recEnd =   getRecordEndDelimiter(_params);
            logStart = getLogStartDelimiter(_params);
            logEnd =   getLogEndDelimiter(_params);
            
            sendToPM = _sendToPM;
            name = _name;
            
            servers.add(this);
        }
        
        /**
        * @param args the command line arguments
        */
        public static void main (String args[]) {

             System.out.println("OBJS PMLogServer");
             try {
                if (args.length>0) // then arg 0 is port #
                    PORT = Integer.parseInt(args[0]);        
            } catch (Exception e) {
                System.out.println("Exception parsing port #, exiting...");
                System.exit(-1);
            }
            new PMLogServer(PORT).start();
        }
        
        /* Start Listeneing for connections */
        public void run() {
                        
            try {
                server = new ServerSocket(port, 100, InetAddress.getLocalHost());
                System.out.println("Starting OBJS PMLogServer on port "+port+"...");
            } catch (Exception e) {System.out.println("OBJS PMLogServer ServerSocket Exception:" +e);}

            try {
                while (true) { //accept connections
                    System.out.println("PMEventServer waiting for a connection...");                    
                    Socket c = server.accept();
                    System.out.println("PMEventServer accepted a connection...");                    
                    new PMLogStreamHandler(c, consumer, recStart, recEnd, 
                                  logStart, logEnd, sendToPM, name, threadCount++).start();
                }            
            } catch (Exception e) {System.out.println("PMEventServer Socket Exception:" +e); e.printStackTrace();}
        }

        
        
        String getRecordStartDelimiter(ParamManager _params) {
            String sd = null;
            if (_params == null) sd = DEFAULT_RECSTART_DELIMITER;
            else
                sd = _params.getPropertyValue("RECSTART_DELIMITER");

            if (sd == null) sd = DEFAULT_RECSTART_DELIMITER;
            return sd;
        }

        String getRecordEndDelimiter(ParamManager _params) {
            String sd = null;
            if (_params == null) sd = DEFAULT_RECEND_DELIMITER;
            else
                sd = _params.getPropertyValue("RECEND_DELIMITER");

            if (sd == null) sd = DEFAULT_RECEND_DELIMITER;
            return sd;
        }
        
        String getLogStartDelimiter(ParamManager _params) {
            String sd = null;
            if (_params == null) sd = DEFAULT_LOGSTART_DELIMITER;
            else
                sd = _params.getPropertyValue("LOGSTART_DELIMITER");

            if (sd == null) sd = DEFAULT_LOGSTART_DELIMITER;
            return sd;
        }

        String getLogEndDelimiter(ParamManager _params) {
            String sd = null;
            if (_params == null) sd = DEFAULT_LOGEND_DELIMITER;
            else
                sd = _params.getPropertyValue("LOGEND_DELIMITER");

            if (sd == null) sd = DEFAULT_LOGEND_DELIMITER;
            return sd;
        }
        
    }
    

    /* 
     * Pushes stream to console if no LogConsumer defined. Otherwise,
     * pushes string concatentated content of log record to Log Consumer
     * (minus delimiters).
     */
    class PMLogStreamHandler extends Thread {
        
        BufferedReader in;
        Socket client;
        PMLogConsumer logConsumer = null;
        String startDelim;
        String endDelim;
        String streamStart;
        String streamEnd;
        LoggerWindow sendToPM;
        SimpleAttributeSet style = null;
        boolean logOn;
        String name;
        
        int lineCount = 0;
        
        PMLogStreamHandler(Socket _c, PMLogConsumer _consumer, 
                           String _startD, String _endD, 
                           String _streamStart, String _streamEnd,
                           LoggerWindow _sendToPM, String _name,
                           int _threadCount) {
            
            try {
                in = new BufferedReader(new InputStreamReader(_c.getInputStream()));
                client = _c;
                logConsumer = _consumer;
                startDelim = _startD;
                endDelim = _endD;
                streamStart = _streamStart;
                streamEnd = _streamEnd;
                sendToPM = _sendToPM;
                logOn = (sendToPM != null);
                name = "\n"+_name+"->";
                
                if (sendToPM != null) //get font color/style to print to LoggerWindow
                    style = sendToPM.getStyle(_threadCount);
                    
            } catch (Exception e) {System.out.println("PMLogStreamHandler init Exception:" +e); e.printStackTrace();}

            if (!logOn) {
                System.out.println("PMLogStreamHandler Thread created for port "+client.getLocalPort()+"...");
                System.out.println("Using Delimiters:"+_startD+","+_endD +","+ _streamStart+","+_streamEnd);
            } else {
                sendToPM.insertText(name+"PM LSH Thread created for "+name+" on port "+client.getLocalPort()+"...", style);
            }
        }

        public void run() {
            String str;
            int maxLines = 500; //errors if log record over this amount
                        
            System.out.println("PMLogStreamHandler Thread started...");
            boolean inRecord = false;
            try { 
                if (logConsumer == null) { //just output to the console
                    while ((str = in.readLine())!= null) {                   
                        if (!logOn)
                            System.out.println("->"+str);
                        else 
                            sendToPM.insertText(name+str, style);
                    } 
                } else { //packetize into records
                    StringBuffer log = new StringBuffer();
                    while ((str = in.readLine())!= null) {
//System.out.println("-> "+str);
                        if (lineCount++ > maxLines) {
                            logConsumer.push(str, "Error. More than "+lineCount+" lines seen with no end delimiter. Clearing buffer.");
                            log.setLength(0); //clear it out                            
                        }
                        if (str.startsWith(endDelim)) {
                            log.append(str);                        
                            if (inRecord) {
                                logConsumer.push(log.toString(), null);
System.out.println("LogServer saw: "+log);
                            }
                            else //not inRecord, so didn't see new startDelim
                                logConsumer.push(log.toString(), "Error. Saw data before a start delimiter");
                            inRecord = false;
                            lineCount = 0;
                        } else 
                        if (str.startsWith(startDelim)) {
                            if (inRecord)
                                logConsumer.push("", "Error. Saw more than one start delimiter.");
                            inRecord = true;
                            log.setLength(0); //clear it out                            
                            log.append(str);                        
                        } else 
                        if (str.startsWith(streamEnd)||str.startsWith(streamStart)) {
                            //start or end of stream -- ignore
                        } else if (inRecord)
                            log.append(str);
                        else //ignore it
                            System.out.println("-");
                    } 
                }
                //System.out.println("=== PMLogStreamHandler Thread halting, saw null.");
            } catch (Exception e) {System.out.println("PMLogStreamHandler Exception:" +e);}
            finally {
                try {
                    client.close();
                } catch (Exception e) {System.out.println("PMLogStreamHandler Close Exception:" +e);}
            }
        }
    }
