/*
 * Copyright 2002 Sun Microsystems, Inc. All  Rights Reserved.
 *  
 * Redistribution and use in source and binary forms, with or 
 * without modification, are permitted provided that the following 
 * conditions are met:
 * 
 * -Redistributions of source code must retain the above copyright  
 *  notice, this list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright 
 *  notice, this list of conditions and the following disclaimer in 
 *  the documentation and/or other materials provided with the 
 *  distribution.
 *  
 * Neither the name of Sun Microsystems, Inc. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY 
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY 
 * DAMAGES OR LIABILITIES  SUFFERED BY LICENSEE AS A RESULT OF OR 
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR 
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE 
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, 
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER 
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF 
 * THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *  
 * You acknowledge that Software is not designed, licensed or 
 * intended for use in the design, construction, operation or 
 * maintenance of any nuclear facility. 
 */

// Implemented under 1.4.
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;

// This example demonstrates adding drag and drop (DnD)
// support to a JLabel.
public class JLabelDragNDrop {
    JFrame aFrame;
    JPanel aPanel;
    JTextField tf;
    JLabel tl;
    JList list;
    JList list2;
    JScrollPane scroll;
    
    static DataFlavor df = new DataFlavor(JLabel.class, "JLabel");
    

    // Constructor
    public JLabelDragNDrop() {
        // Create the frame and container.
        aFrame = new JFrame("JLabel Drag and Drop Demo");
        aFrame.setSize(100, 50);
        aPanel = new JPanel();
        aPanel.setLayout(new GridLayout(2, 2));

        // Add the widgets.
        addWidgets();

        // Add the panel to the frame.
        aFrame.getContentPane().add(aPanel, BorderLayout.CENTER);

        // Exit when the window is closed.
        aFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Show the panel.
        aFrame.pack();
        aFrame.setVisible(true);
    }

    // Create and add the widgets to the panel.
    private void addWidgets() {
        // Create widgets.
        java.util.Vector labels = new java.util.Vector();
        labels.addElement(new JLabel("hi"));
        labels.addElement(new JLabel("good"));
        labels.addElement(new JLabel("pizza"));
        list = new JList(labels);
        scroll = new JScrollPane(list);
        
        java.util.Vector strs = new java.util.Vector();
        strs.addElement(new String("hi"));
        strs.addElement(new String("good"));
        strs.addElement(new String("pizza"));
        list2 = new JList(strs);
        
        tf = new JTextField(50);
        tl = new JLabel("Drop Here", SwingConstants.LEFT);

        tl.setTransferHandler(new TransferHandler("text"));
        //list.setTransferHandler(new TransferHandler("text"));

 
        MouseListener ml = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JComponent c = (JComponent)e.getSource();
                TransferHandler th = c.getTransferHandler();
                th.exportAsDrag(c, e, TransferHandler.COPY);
            }
        }; 
        tl.addMouseListener(ml);

        MouseListener ml2 = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JComponent c = (JComponent)e.getSource();
                System.out.println("object is of type = "+c.getClass().getName());
                TransferHandler th = c.getTransferHandler();
                th.exportAsDrag(c, e, TransferHandler.COPY);
            }
        };
        list2.addMouseListener(ml2);
        list2.setDragEnabled(true);
        System.out.println("Drag enabled = "+list.getDragEnabled());
        list.addMouseListener(ml2);
        list.setDragEnabled(true);
        list.setTransferHandler(new TH(null));
        


        tf.setTransferHandler(new TH(null));
//        tf.setTransferHandler(new TransferHandler("text"));

        // Add widgets to container.
        aPanel.add(tf);
        aPanel.add(tl);
        aPanel.add(scroll);
//        aPanel.add(list2);
//        tl.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    }

    // main method
    public static void main(String[] args) {
        // Set the look and feel.
        try {
            UIManager.setLookAndFeel(
                UIManager.getCrossPlatformLookAndFeelClassName());
        } catch(Exception e) {}

        JLabelDragNDrop example = new JLabelDragNDrop();
    }
    

    class TH extends javax.swing.TransferHandler {
        
//        JLabel label;
//        TH(JLabel _l) {label = _l;}

        public TH(String _p) {super();}        
            
        protected Transferable createTransferable(JComponent c) {
            System.out.println("createTransferable called with "+c.getClass().getName());
            JList jl = (JList) c;            
            return (new MyTransferable((JLabel)jl.getSelectedValue()));
        }
        
        
        public boolean importData(JComponent comp, java.awt.datatransfer.Transferable t) {
            System.out.println("ImportData called.");
            try {
                JLabel jl = (JLabel)t.getTransferData(df);
                System.out.println("----> t = "+jl.getClass().getName());
                JTextField target = ((JTextField)comp);
                target.setText(target.getText()+jl.getText());
                return true;
            } catch (java.awt.datatransfer.UnsupportedFlavorException e) {
                System.out.println("----> Flavor not supported...");               
            } catch (java.io.IOException ioe) {
                System.out.println("----> Flavor not supported... IOException");               
                ioe.printStackTrace();
            }
            
            return false;
        }
        
        
        public void exportAsDrag(JComponent comp,
                         InputEvent e,
                         int action) {
        
            System.out.println("exportAsDrag called.");
            super.exportAsDrag(comp,e,action);
        }
        
        protected void exportDone(JComponent source,
                          Transferable data,
                          int action) {

            System.out.println("exportDone called");
            super.exportDone(source, data, action);
        }
        
        
        public boolean canImport(JComponent comp,
                         java.awt.datatransfer.DataFlavor[] transferFlavors)
        { 
            
            System.out.println("Flavors:");
            for (int i=0;i<transferFlavors.length; i++) {
                java.awt.datatransfer.DataFlavor df = transferFlavors[i];
                System.out.println("  "+df.getHumanPresentableName());
                System.out.println("  --->"+df.getRepresentationClass().getName());
            }        
            return true;
        }

    
        public Icon getVisualRepresentation(Transferable t) {
            System.out.println("getVisualRepresentation called");
            return super.getVisualRepresentation(t);            
        }
    
        public int getSourceActions(JComponent c) {
            System.out.println("getSourceActions called");
            return TransferHandler.COPY;                        
        }

    }
   
    class MyTransferable implements Transferable {

        JLabel label;
        MyTransferable(JLabel _l) {
            System.out.println("MyTransferable called.");
            label = _l;
        }

        DataFlavor[] dfs = {df};
        
        public DataFlavor[] getTransferDataFlavors() {
            System.out.println("getTransferDataFlavors called.");
            return dfs;
        }
        
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            System.out.println("isDataFlavorSupported called.");
            
            if (flavor.getRepresentationClass()==JLabel.class)
                return true;
            else
                return false;            
        }
        
        public Object getTransferData(DataFlavor flavor)
                            throws java.awt.datatransfer.UnsupportedFlavorException,
                                    java.io.IOException {
            System.out.println("getTransferData called.");
                                        
            return label;                
        }
        
    }
    
}


