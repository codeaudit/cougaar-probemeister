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

import java.awt.*;
import javax.swing.*;

/*
 * Probe_GetLoggerAttrsGUI.java
 *
 * Created July 2002
 *
 * @author  Administrator
 */
public class Probe_GetLoggerInitAttrsGUI extends javax.swing.JDialog {
    
    static final int width = 405;
    static final int closeHeight = 315;
    static final int openHeight = 400;
    
    
    static Probe_GetLoggerInitAttrsGUI gui;
    static {
        gui = new Probe_GetLoggerInitAttrsGUI((java.awt.Frame)null, true);
        gui.setSize(width, closeHeight);
    }
    
    //Have one instance to save on memory.
    public static Probe_GetLoggerInitAttrsGUI gui() {return gui;}
    
    String tempHost = null;
    
    boolean cancelled = true; //true if user pressed "Cancel" button, or closes window
    public void resetGUI() {
        cancelled = true;
        setcbUseInternal();
    }
    public boolean userCancelled() { return cancelled; }
    
    
    /** Creates new form Probe_GetLoggerInitAttrsGUI2 */
    public Probe_GetLoggerInitAttrsGUI(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        getContentPane().setLayout(null);
        initComponents();
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     */
   private void initComponents() {
       bgLocation = new javax.swing.ButtonGroup();
       lTitle = new javax.swing.JLabel();
       lNote1 = new javax.swing.JLabel();
       lNote2 = new javax.swing.JLabel();
       jSeparator1 = new javax.swing.JSeparator();
       jSeparator2 = new javax.swing.JSeparator();
       bLocationDetails = new javax.swing.JToggleButton();
       lHost = new javax.swing.JLabel();
       lPort = new javax.swing.JLabel();
       lLogger = new javax.swing.JLabel();
       lLoggerOpt = new javax.swing.JLabel();
       lFormatter = new javax.swing.JLabel();
       lFormatterOpt = new javax.swing.JLabel();
       vHost = new javax.swing.JTextField();
       vPort = new javax.swing.JTextField();
       vLogger = new javax.swing.JTextField();
       vFormatter = new javax.swing.JTextField();
       bOK = new javax.swing.JButton();
       bCancel = new javax.swing.JButton();
       rbAtStart = new javax.swing.JRadioButton();
       rbAtEnd = new javax.swing.JRadioButton();
       rbOffset = new javax.swing.JRadioButton();
       vOffset = new javax.swing.JTextField();
       bViewBytecode = new javax.swing.JButton();
       bViewSource = new javax.swing.JButton();
       cbUseInternal = new javax.swing.JRadioButton("Internal Collector");
       cbUseExternal = new javax.swing.JRadioButton("External Collector");
       bgInExUse = new javax.swing.ButtonGroup();
       //Boolean log = (Boolean)Globals.globals().get(GlobalVars.USE_PM_AS_LOGGER_MONITOR);
       //cbUseInternal.setEnabled(log.booleanValue()); //enable only if feature is turned on

       lName = new javax.swing.JLabel();
       vName = new javax.swing.JTextField();
       //lName.setEnabled(log.booleanValue());
       //vName.setEnabled(log.booleanValue());
       
       getContentPane().setLayout(null);
       
       addWindowListener(new java.awt.event.WindowAdapter() {
           public void windowOpened(java.awt.event.WindowEvent evt) {
               openDialog(evt);
           }
           public void windowClosing(java.awt.event.WindowEvent evt) {
               closeDialog(evt);
           }
       });
               
       
       lTitle.setFont(new java.awt.Font("Dialog", 1, 18));
       lTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
       lTitle.setText("Collector Customization");
       getContentPane().add(lTitle);
       lTitle.setBounds(0, 10, 390, 30);
       
       lNote1.setFont(new java.awt.Font("Dialog", 0, 10));
       lNote1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
       lNote1.setText("Use Collector probes to emit data to your collector application.");
       getContentPane().add(lNote1);
       lNote1.setBounds(0, 40, 390, 14);
       
       lNote2.setFont(new java.awt.Font("Dialog", 0, 10));
       lNote2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
       lNote2.setText("(This probe should be deployed/executed only once per VM)");
       getContentPane().add(lNote2);
       lNote2.setBounds(0, 50, 400, 14);
       
       getContentPane().add(cbUseExternal);
       cbUseExternal.setBounds(20, 70, 140, 20);
       cbUseExternal.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(java.awt.event.ActionEvent evt) {
               cbUseInExternalActionPerformed(evt);
           }
       });
       cbUseExternal.setToolTipText("Use if you have an external collector...");       

       getContentPane().add(cbUseInternal);
       cbUseInternal.setSelected(true);
       cbUseInternal.setBounds(220, 70, 165, 20);
       cbUseInternal.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(java.awt.event.ActionEvent evt) {
               cbUseInExternalActionPerformed(evt);
           }
       });
       cbUseInternal.setToolTipText("Use if you want to view log data using PM.");       
       bgInExUse.add(cbUseInternal);
       bgInExUse.add(cbUseExternal);
       
       lHost.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       lHost.setText("IP Addr:");
       getContentPane().add(lHost);
       lHost.setBounds(5, 95, 60, 16);

       getContentPane().add(vHost);
       vHost.setBounds(70, 95, 120, 20);
       vHost.setToolTipText("Address of your collector app");
       
       //Must be set AFTER vHost added... since it modifies the tooltip.
       setcbUseInternal();

       lName.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       lName.setText("Name:");
       getContentPane().add(lName);
       lName.setBounds(165, 95, 90, 16);

       getContentPane().add(vName);
       vName.setBounds(260, 95, 100, 20);
       vName.setToolTipText("Names the window for this output");
       
       //Separator
       getContentPane().add(jSeparator2);
       jSeparator2.setBounds(15, 123, 340, 10);
       
       
       
       lPort.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       lPort.setText("Event Collector Port:");
       getContentPane().add(lPort);
       lPort.setBounds(10, 135, 120, 16);

       getContentPane().add(vPort);
       vPort.setBounds(140, 135, 40, 20);
       vPort.setToolTipText("Use same port # in all VMs to merge logs...");       

     
       lLogger.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       lLogger.setText("Collector Name:");
       getContentPane().add(lLogger);
       lLogger.setBounds(39, 163, 90, 16);

       lLoggerOpt.setFont(new java.awt.Font("Dialog", 0, 10));
       lLoggerOpt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       lLoggerOpt.setText("(user-assigned)");
       getContentPane().add(lLoggerOpt);
       lLoggerOpt.setBounds(39, 175, 83, 16);      
       
       getContentPane().add(vLogger);
       vLogger.setBounds(140, 163, 200, 20);
       vLogger.setToolTipText("Use this name again when defining your collector probes");
       
       lFormatter.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       lFormatter.setText("Formatter:");
       getContentPane().add(lFormatter);
       lFormatter.setBounds(39, 191, 90, 16);

       getContentPane().add(vFormatter);
       vFormatter.setBounds(140, 191, 200, 20);
       vFormatter.setToolTipText("FULL class name of existing custom formatter");

       lFormatterOpt.setFont(new java.awt.Font("Dialog", 0, 10));
       lFormatterOpt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       lFormatterOpt.setText("(optional)");
       getContentPane().add(lFormatterOpt);
       lFormatterOpt.setBounds(39, 203, 83, 16);

       
       bOK.setText("OK");
       bOK.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(java.awt.event.ActionEvent evt) {
               bOK_Pressed(evt);
           }
       });
       
       getContentPane().add(bOK);
//       bOK.setBounds(160, 200, 80, 26);
       bOK.setBounds(100, 230, 80, 26);
       
       bCancel.setText("Cancel");
       bCancel.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(java.awt.event.ActionEvent evt) {
               bCancel_Pressed(evt);
           }
       });
       
       getContentPane().add(bCancel);
       bCancel.setBounds(220, 230, 80, 26);
       
       getContentPane().add(jSeparator1);
       jSeparator1.setBounds(130, 270, 240, 10);
       
       bLocationDetails.setFont(new java.awt.Font("Dialog", 0, 10));
       bLocationDetails.setText("Location Details...");
       bLocationDetails.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(java.awt.event.ActionEvent evt) {
               bLocationDetailsActionPerformed(evt);
           }
       });
       
       getContentPane().add(bLocationDetails);
       bLocationDetails.setBounds(10, 260, 120, 20);
       rbAtStart.setSelected(true);
       rbAtStart.setText("At Start");
       bgLocation.add(rbAtStart);
       
       getContentPane().add(rbAtStart);
       rbAtStart.setBounds(30, 300, 68, 24);
       
       rbAtEnd.setText("At End");
       bgLocation.add(rbAtEnd);
       
       getContentPane().add(rbAtEnd);
       rbAtEnd.setBounds(110, 300, 61, 24);
       
       rbOffset.setText("At Bytecode Offset");
       bgLocation.add(rbOffset);
       
       getContentPane().add(rbOffset);
       rbOffset.setBounds(180, 300, 131, 24);
       
       vOffset.setText("0");
       
       getContentPane().add(vOffset);
       vOffset.setBounds(310, 300, 50, 20);
       vOffset.setText("0");
       
       bViewBytecode.setText("View Bytecode");
       bViewBytecode.setEnabled(false);
       getContentPane().add(bViewBytecode);
       bViewBytecode.setBounds(200, 340, 118, 26);
       
       bViewSource.setText("View Source");
       bViewSource.setEnabled(false);
       
       getContentPane().add(bViewSource);
       bViewSource.setBounds(70, 340, 106, 26);
       
       pack();
   }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     */
    /*
   private void initComponents() {//GEN-BEGIN:initComponents
       bgLocation = new javax.swing.ButtonGroup();
       lTitle = new javax.swing.JLabel();
       lNote1 = new javax.swing.JLabel();
       lNote2 = new javax.swing.JLabel();
       jSeparator1 = new javax.swing.JSeparator();
       jSeparator2 = new javax.swing.JSeparator();
       bLocationDetails = new javax.swing.JToggleButton();
       lHost = new javax.swing.JLabel();
       lPort = new javax.swing.JLabel();
       lLogger = new javax.swing.JLabel();
       lLoggerOpt = new javax.swing.JLabel();
       lFormatter = new javax.swing.JLabel();
       lFormatterOpt = new javax.swing.JLabel();
       vHost = new javax.swing.JTextField();
       vPort = new javax.swing.JTextField();
       vLogger = new javax.swing.JTextField();
       vFormatter = new javax.swing.JTextField();
       bOK = new javax.swing.JButton();
       bCancel = new javax.swing.JButton();
       rbAtStart = new javax.swing.JRadioButton();
       rbAtEnd = new javax.swing.JRadioButton();
       rbOffset = new javax.swing.JRadioButton();
       vOffset = new javax.swing.JTextField();
       bViewBytecode = new javax.swing.JButton();
       bViewSource = new javax.swing.JButton();
       cbUseInternal = new javax.swing.JRadioButton("Internal Collector");
       cbUseExternal = new javax.swing.JRadioButton("External Collector");
       bgInExUse = new javax.swing.ButtonGroup();
       //Boolean log = (Boolean)Globals.globals().get(GlobalVars.USE_PM_AS_LOGGER_MONITOR);
       //cbUseInternal.setEnabled(log.booleanValue()); //enable only if feature is turned on

       lName = new javax.swing.JLabel();
       vName = new javax.swing.JTextField();
       //lName.setEnabled(log.booleanValue());
       //vName.setEnabled(log.booleanValue());
       
       getContentPane().setLayout(null);
       
       addWindowListener(new java.awt.event.WindowAdapter() {
           public void windowOpened(java.awt.event.WindowEvent evt) {
               openDialog(evt);
           }
           public void windowClosing(java.awt.event.WindowEvent evt) {
               closeDialog(evt);
           }
       });
               
       
       lTitle.setFont(new java.awt.Font("Dialog", 1, 18));
       lTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
       lTitle.setText("Logger Customization");
       getContentPane().add(lTitle);
       lTitle.setBounds(0, 10, 390, 30);
       
       lNote1.setFont(new java.awt.Font("Dialog", 0, 10));
       lNote1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
       lNote1.setText("Use Logger probes to emit data to your logger application.");
       getContentPane().add(lNote1);
       lNote1.setBounds(0, 40, 390, 14);
       
       lNote2.setFont(new java.awt.Font("Dialog", 0, 10));
       lNote2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
       lNote2.setText("(This probe should be deployed/executed only once per VM)");
       getContentPane().add(lNote2);
       lNote2.setBounds(0, 50, 400, 14);
       
       getContentPane().add(cbUseExternal);
       cbUseExternal.setBounds(20, 70, 140, 20);
       cbUseExternal.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(java.awt.event.ActionEvent evt) {
               cbUseInExternalActionPerformed(evt);
           }
       });
       cbUseExternal.setToolTipText("Use if you have an external collector...");       

       getContentPane().add(cbUseInternal);
       cbUseInternal.setSelected(true);
       cbUseInternal.setBounds(220, 70, 165, 20);
       cbUseInternal.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(java.awt.event.ActionEvent evt) {
               cbUseInExternalActionPerformed(evt);
           }
       });
       cbUseInternal.setToolTipText("Use if you want to view log data using PM.");       
       bgInExUse.add(cbUseInternal);
       bgInExUse.add(cbUseExternal);
       
       lHost.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       lHost.setText("IP Addr:");
       getContentPane().add(lHost);
       lHost.setBounds(5, 95, 60, 16);

       getContentPane().add(vHost);
       vHost.setBounds(70, 95, 120, 20);
       vHost.setToolTipText("Address of your logger collector app");
       
       //Must be set AFTER vHost added... since it modifies the tooltip.
       setcbUseInternal();

       lName.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       lName.setText("Name:");
       getContentPane().add(lName);
       lName.setBounds(165, 95, 90, 16);

       getContentPane().add(vName);
       vName.setBounds(260, 95, 100, 20);
       vName.setToolTipText("Names the window for this output");
       
       //Separator
       getContentPane().add(jSeparator2);
       jSeparator2.setBounds(15, 123, 340, 10);
       
       
       
       lPort.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       lPort.setText("Event Collector Port:");
       getContentPane().add(lPort);
       lPort.setBounds(10, 135, 120, 16);

       getContentPane().add(vPort);
       vPort.setBounds(140, 135, 40, 20);
       vPort.setToolTipText("Use same port # in all VMs to merge logs...");       

     
       lLogger.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       lLogger.setText("Logger Name:");
       getContentPane().add(lLogger);
       lLogger.setBounds(39, 163, 90, 16);

       lLoggerOpt.setFont(new java.awt.Font("Dialog", 0, 10));
       lLoggerOpt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       lLoggerOpt.setText("(user-assigned)");
       getContentPane().add(lLoggerOpt);
       lLoggerOpt.setBounds(39, 175, 83, 16);      
       
       getContentPane().add(vLogger);
       vLogger.setBounds(140, 163, 200, 20);
       vLogger.setToolTipText("Use this name again when defining your logger probes");
       
       lFormatter.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       lFormatter.setText("Formatter:");
       getContentPane().add(lFormatter);
       lFormatter.setBounds(39, 191, 90, 16);

       getContentPane().add(vFormatter);
       vFormatter.setBounds(140, 191, 200, 20);
       vFormatter.setToolTipText("FULL class name of existing custom formatter");

       lFormatterOpt.setFont(new java.awt.Font("Dialog", 0, 10));
       lFormatterOpt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       lFormatterOpt.setText("(optional)");
       getContentPane().add(lFormatterOpt);
       lFormatterOpt.setBounds(39, 203, 83, 16);

       
       bOK.setText("OK");
       bOK.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(java.awt.event.ActionEvent evt) {
               bOK_Pressed(evt);
           }
       });
       
       getContentPane().add(bOK);
//       bOK.setBounds(160, 200, 80, 26);
       bOK.setBounds(100, 230, 80, 26);
       
       bCancel.setText("Cancel");
       bCancel.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(java.awt.event.ActionEvent evt) {
               bCancel_Pressed(evt);
           }
       });
       
       getContentPane().add(bCancel);
       bCancel.setBounds(220, 230, 80, 26);
       
       getContentPane().add(jSeparator1);
       jSeparator1.setBounds(130, 270, 240, 10);
       
       bLocationDetails.setFont(new java.awt.Font("Dialog", 0, 10));
       bLocationDetails.setText("Location Details...");
       bLocationDetails.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(java.awt.event.ActionEvent evt) {
               bLocationDetailsActionPerformed(evt);
           }
       });
       
       getContentPane().add(bLocationDetails);
       bLocationDetails.setBounds(10, 260, 120, 20);
       rbAtStart.setSelected(true);
       rbAtStart.setText("At Start");
       bgLocation.add(rbAtStart);
       
       getContentPane().add(rbAtStart);
       rbAtStart.setBounds(30, 300, 68, 24);
       
       rbAtEnd.setText("At End");
       bgLocation.add(rbAtEnd);
       
       getContentPane().add(rbAtEnd);
       rbAtEnd.setBounds(110, 300, 61, 24);
       
       rbOffset.setText("At Bytecode Offset");
       bgLocation.add(rbOffset);
       
       getContentPane().add(rbOffset);
       rbOffset.setBounds(180, 300, 131, 24);
       
       vOffset.setText("0");
       
       getContentPane().add(vOffset);
       vOffset.setBounds(310, 300, 50, 20);
       vOffset.setText("0");
       
       bViewBytecode.setText("View Bytecode");
       bViewBytecode.setEnabled(false);
       getContentPane().add(bViewBytecode);
       bViewBytecode.setBounds(200, 340, 118, 26);
       
       bViewSource.setText("View Source");
       bViewSource.setEnabled(false);
       
       getContentPane().add(bViewSource);
       bViewSource.setBounds(70, 340, 106, 26);
       
       pack();
   }//GEN-END:initComponents
   
     */
     
    private void bOK_Pressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bOK_Pressed
        cancelled = false;
        this.setVisible(false);
    }//GEN-LAST:event_bOK_Pressed
    
    
    private void bCancel_Pressed(java.awt.event.ActionEvent evt) {
        cancelled = true;
        this.setVisible(false);
    }
    
    private void bLocationDetailsActionPerformed(java.awt.event.ActionEvent evt) {
        // If closed, expand window.
        if (bLocationDetails.isSelected()) // this.getHeight() == closeHeight)
            setSize(width, openHeight);
        else  //it's open, so shrink window
            setSize(width, closeHeight);
        rbAtStart.invalidate();
        this.validate();
    }
    
    
    private void cbUseInExternalActionPerformed(java.awt.event.ActionEvent evt) {
        if (cbUseInternal.isSelected())
            setcbUseInternal();
        else if (cbUseExternal.isSelected())
            setcbUseExternal();
    }
    
    void setcbUseInternal() {
        lName.setEnabled(true);
        vName.setEnabled(true);
        lHost.setEnabled(false);
        vHost.setEnabled(false);
        tempHost = vHost.getText(); //save user value if any
        try {
            vHost.setText(java.net.InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            vHost.setText("ERROR");
        }
        vHost.setToolTipText("Using PM's IP for internal collection.");
    }
    
    
    void setcbUseExternal() {
        lName.setEnabled(false);
        vName.setEnabled(false);
        lHost.setEnabled(true);
        vHost.setEnabled(true);
        vHost.setText(tempHost); //reset value
        vHost.setToolTipText("Address of YOUR logger collector app");
    }
    
    
    
    /** Closes the dialog */
    private void openDialog(java.awt.event.WindowEvent evt) {
        rbAtStart.setSelected(true); //set this as the default
    }
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {
        cancelled = true; //user closed instead of using OK/cancel buttons
        setVisible(false);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        //        new Probe_GetLoggerInitAttrsGUI2(new javax.swing.JFrame(), false).show();
        Probe_GetLoggerInitAttrsGUI gui = Probe_GetLoggerInitAttrsGUI.gui();
        gui.show();
        System.exit(0);
    }
    
    public boolean usePM() {return this.cbUseInternal.isSelected();}
    public String getName() {return this.vName.getText();}
    public String getHost() {return this.vHost.getText();}
    public String getPort() {return this.vPort.getText();}
    public String getLogger() {return this.vLogger.getText();}
    public String getFormatter() {return this.vFormatter.getText();}
    
    public void setByteOffset(int _loc) {
        
        if (_loc == 0)
            rbAtStart.setSelected(true);
        else if (_loc < 0)
            rbAtEnd.setSelected(true);
        else {
            rbOffset.setSelected(true);
            vOffset.setText(""+_loc);
        }
    }
    
    /* Returns 0 for start of method, -1 for end of method, and a positive
     * integer (entered by the user) as a byte offset
     */
    public int    getByteOffset() {
        if (this.rbAtStart.isSelected())
            return 0;
        else if (this.rbAtEnd.isSelected())
            return -1;
        else if (rbOffset.isSelected()) {
            String sval = vOffset.getText();
            int val = 0; //default to start
            try {
                val = Integer.parseInt(sval);
            } catch (NumberFormatException nfe) {
            }
            return val;
        }
        else //default to insert at start
            return 0;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgLocation;
    private javax.swing.JLabel lTitle;
    private javax.swing.JLabel lNote1;
    private javax.swing.JLabel lNote2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JToggleButton bLocationDetails;
    private javax.swing.JLabel lHost;
    private javax.swing.JLabel lPort;
    private javax.swing.JLabel lLogger;
    private javax.swing.JLabel lLoggerOpt;
    private javax.swing.JLabel lFormatter;
    private javax.swing.JLabel lFormatterOpt;
    private javax.swing.JTextField vHost;
    private javax.swing.JTextField vPort;
    private javax.swing.JTextField vLogger;
    private javax.swing.JTextField vFormatter;
    private javax.swing.JButton bCancel;
    private javax.swing.JButton bOK;
    private javax.swing.JRadioButton rbAtStart;
    private javax.swing.JRadioButton rbAtEnd;
    private javax.swing.JRadioButton rbOffset;
    private javax.swing.JTextField vOffset;
    private javax.swing.JButton bViewBytecode;
    private javax.swing.JButton bViewSource;
    private javax.swing.JRadioButton cbUseInternal;
    private javax.swing.JRadioButton cbUseExternal;
    private javax.swing.ButtonGroup bgInExUse;
    private javax.swing.JLabel lName;
    private javax.swing.JTextField vName;
    // End of variables declaration//GEN-END:variables
    
}
