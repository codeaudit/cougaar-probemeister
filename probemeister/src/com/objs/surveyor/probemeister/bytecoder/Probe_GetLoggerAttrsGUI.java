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
import com.objs.probemeister.*;

import java.awt.*;
import javax.swing.*;
import java.util.Iterator;
/*
 * Probe_GetLoggerAttrsGUI.java
 *
 * Created July 2002
 *
 * @author  Administrator
 */
public class Probe_GetLoggerAttrsGUI extends javax.swing.JDialog {

    static final int width = 405;
    static final int closeHeight = 285;
    static final int openHeight = 370;

    boolean cancelled = true; //true if user pressed "Cancel" button
    public void resetGUI() {
        cancelled = true;
    }
    
    static Probe_GetLoggerAttrsGUI gui;
    static {
        gui = new Probe_GetLoggerAttrsGUI((java.awt.Frame)null, true);
        gui.setSize(width, closeHeight);
    }
    
    //Have one instance to save on memory.
    public static Probe_GetLoggerAttrsGUI gui() {return gui;}
 
    
    /** Creates new form Probe_GetLoggerAttrsGUI2 */
    public Probe_GetLoggerAttrsGUI(java.awt.Frame parent, boolean modal) {
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
       bLocationDetails = new javax.swing.JToggleButton();
       lMsg = new javax.swing.JLabel();
       lLevel = new javax.swing.JLabel();
       lLogger = new javax.swing.JLabel();
       lAlsoEmit = new javax.swing.JLabel();
       lAlsoEmitOpt = new javax.swing.JLabel();
       vMsg = new javax.swing.JTextField();

       String[] levels = {"SEVERE", "WARNING", "INFO", "CONFIG", "FINE", "FINER", "FINEST"};
       cbLevel = new javax.swing.JComboBox(levels);
       cbLevel.setSelectedIndex(2);

       cbLogger = new javax.swing.JComboBox();

       rbNone = new javax.swing.JRadioButton("None");
       rbNone.setFont(new java.awt.Font("Dialog", 0, 12));

       rbOuterThis = new javax.swing.JRadioButton("outer this");
       rbOuterThis.setFont(new java.awt.Font("Dialog", 0, 12));

       rbThis = new javax.swing.JRadioButton("this");
       rbThis.setFont(new java.awt.Font("Dialog", 0, 12));

       rbArgs = new javax.swing.JRadioButton("Method Args");
       rbArgs.setFont(new java.awt.Font("Dialog", 0, 12));

       rbEmitChoices = new ButtonGroup();
       rbNone.setSelected(true); //default
       rbEmitChoices.add(rbNone);       
       rbEmitChoices.add(rbOuterThis);
       rbEmitChoices.add(rbThis);
       rbEmitChoices.add(rbArgs);

       bOK = new javax.swing.JButton();
       bCancel = new javax.swing.JButton();
       rbAtStart = new javax.swing.JRadioButton();
       rbAtEnd = new javax.swing.JRadioButton();
       rbOffset = new javax.swing.JRadioButton();
       vOffset = new javax.swing.JTextField();
       bViewBytecode = new javax.swing.JButton();
       bViewSource = new javax.swing.JButton();
              
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
       lTitle.setText("Logger Probe Customization");
       getContentPane().add(lTitle);
       lTitle.setBounds(0, 10, 390, 30);
       
       lNote1.setFont(new java.awt.Font("Dialog", 0, 10));
       lNote1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
       lNote1.setText("Provide values for all fields.");
       getContentPane().add(lNote1);
       lNote1.setBounds(0, 40, 390, 14);
       
       lNote2.setFont(new java.awt.Font("Dialog", 0, 10));
       lNote2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
       lNote2.setText("");//reserved for later use(This probe should be deployed/executed only once per VM)");
       getContentPane().add(lNote2);
       lNote2.setBounds(0, 50, 400, 14);
       
       getContentPane().add(jSeparator1);
       jSeparator1.setBounds(130, 240, 240, 10);
       
       bLocationDetails.setFont(new java.awt.Font("Dialog", 0, 10));
       bLocationDetails.setText("Location Details...");
       bLocationDetails.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(java.awt.event.ActionEvent evt) {
               bLocationDetailsActionPerformed(evt);
           }
       });
       
       getContentPane().add(bLocationDetails);
       bLocationDetails.setBounds(10, 230, 120, 20);
       
       lMsg.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       lMsg.setText("Message:");
       getContentPane().add(lMsg);
       lMsg.setBounds(5, 80, 125, 16);

       getContentPane().add(vMsg);
       vMsg.setBounds(140, 80, 200, 20);
       vMsg.setToolTipText("Address of your logger collector app");
       
       lLevel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       lLevel.setText("Log Level:");
       getContentPane().add(lLevel);
       lLevel.setBounds(10, 110, 120, 16);

       getContentPane().add(cbLevel);
       cbLevel.setBounds(140, 110, 100, 20);
                  
       lLogger.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       lLogger.setText("Logger Name:");
       getContentPane().add(lLogger);
       lLogger.setBounds(39, 140, 90, 16);

       getContentPane().add(cbLogger);
       cbLogger.setBounds(140, 140, 200, 20);
       cbLogger.setToolTipText("User-provided name");
       
       lAlsoEmit.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
       lAlsoEmit.setText("Also Emit:");
       getContentPane().add(lAlsoEmit);
       lAlsoEmit.setBounds(10, 172, 60, 16);

       //cbAlsoEmit.setToolTipText("FULL class name of existing custom formatter");
       rbNone.setBounds(80, 170, 55, 20);
       getContentPane().add(rbNone);
       rbThis.setBounds(140, 170, 45, 20);
       getContentPane().add(rbThis);
       rbOuterThis.setBounds(190, 170, 80, 20);
       getContentPane().add(rbOuterThis);
       rbArgs.setBounds(280, 170, 120, 20);
       getContentPane().add(rbArgs);
       
       bOK.setText("OK");
       bOK.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(java.awt.event.ActionEvent evt) {
               bOK_Pressed(evt);
           }
       });
       
       getContentPane().add(bOK);
       bOK.setBounds(100, 200, 80, 26);
       
       bCancel.setText("Cancel");
       bCancel.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(java.awt.event.ActionEvent evt) {
               bCancel_Pressed(evt);
           }
       });
       
       getContentPane().add(bCancel);
       bCancel.setBounds(220, 200, 80, 26);
       
       //Probe extras
       
       

       //Location code
       rbAtStart.setSelected(true);
       rbAtStart.setText("At Start");
       bgLocation.add(rbAtStart);
       
       getContentPane().add(rbAtStart);
       rbAtStart.setBounds(30, 270, 68, 24);
       
       rbAtEnd.setText("At End");
       bgLocation.add(rbAtEnd);
       
       getContentPane().add(rbAtEnd);
       rbAtEnd.setBounds(110, 270, 61, 24);
       
       rbOffset.setText("At Bytecode Offset");
       bgLocation.add(rbOffset);
       
       getContentPane().add(rbOffset);
       rbOffset.setBounds(180, 270, 131, 24);
       
       vOffset.setText("0");
       
       getContentPane().add(vOffset);
       vOffset.setBounds(310, 270, 50, 20);
       vOffset.setText("0");
       
       bViewBytecode.setText("View Bytecode");
       bViewBytecode.setEnabled(false);
       getContentPane().add(bViewBytecode);
       bViewBytecode.setBounds(200, 310, 118, 26);
       
       bViewSource.setText("View Source");
       bViewSource.setEnabled(false);
       
       getContentPane().add(bViewSource);
       bViewSource.setBounds(70, 310, 106, 26);
       
       pack();
   }
   
    private void bOK_Pressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bOK_Pressed
        // Add your handling code here:
        cancelled = false;
        this.setVisible(false); 
    }
    
    private void bCancel_Pressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bOK_Pressed
        // Add your handling code here:
        cancelled = true;
        this.setVisible(false); 
    }
    
    private void bLocationDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bLocationDetailsActionPerformed
        // If closed, expand window.
        if (bLocationDetails.isSelected()) // this.getHeight() == closeHeight)
            setSize(width, openHeight);        
        else  //it's open, so shrink window
            setSize(width, closeHeight);
        rbAtStart.invalidate();
        this.validate();
    }//GEN-LAST:event_bLocationDetailsActionPerformed


    /** Closes the dialog */
    private void openDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        rbAtStart.setSelected(true); //set this as the default
    }
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        cancelled = true; //user closed instead of using OK/cancel buttons
        setVisible(false);
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
//        new Probe_GetLoggerAttrsGUI2(new javax.swing.JFrame(), false).show();
        Probe_GetLoggerAttrsGUI gui = Probe_GetLoggerAttrsGUI.gui();
        gui.show();
        System.exit(0);
    }

    public String getMsg() {return this.vMsg.getText();}
    public String getLevel() {return this.cbLevel.getSelectedItem().toString();}
    public String getLogger() {return this.cbLogger.getSelectedItem().toString();}
    public void   setLoggerNames(Iterator iter) { 
       //Populate this combobox with the current list of defined loggers.
       while (iter.hasNext())
           cbLogger.addItem(iter.next());        
    }
    public String getAlsoEmit() {
        if (rbOuterThis.isSelected()) return "OUTERTHIS";
        else if (rbThis.isSelected()) return "THIS";
        else if (rbArgs.isSelected()) return "ARGS";
        else return null;
    }
    public boolean userCancelled() { return cancelled; }

    public void enableOuterThis(boolean _b) {this.rbOuterThis.setEnabled(_b);}
    public void enableThis(boolean _b) {this.rbThis.setEnabled(_b);}
    //public void setAlsoEMit(boolean _b) {this.rbOuterThis.setSelected(_b);}

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
    private javax.swing.JToggleButton bLocationDetails;
    private javax.swing.JLabel lMsg;
    private javax.swing.JLabel lLevel;
    private javax.swing.JLabel lLogger;
    private javax.swing.JLabel lAlsoEmit;
    private javax.swing.JLabel lAlsoEmitOpt;
    private javax.swing.JTextField vMsg;
    private javax.swing.JComboBox cbLevel;
    private javax.swing.JComboBox cbLogger;

    private javax.swing.ButtonGroup rbEmitChoices;
    private javax.swing.JRadioButton rbNone;
    private javax.swing.JRadioButton rbOuterThis;
    private javax.swing.JRadioButton rbThis;
    private javax.swing.JRadioButton rbArgs;

    private javax.swing.JButton bOK;
    private javax.swing.JButton bCancel;
    private javax.swing.JRadioButton rbAtStart;
    private javax.swing.JRadioButton rbAtEnd;
    private javax.swing.JRadioButton rbOffset;
    private javax.swing.JTextField vOffset;
    private javax.swing.JButton bViewBytecode;
    private javax.swing.JButton bViewSource;
    // End of variables declaration//GEN-END:variables

}
