/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application;

import javax.swing.JDialog;
import javax.swing.JFrame;
import org.opentcs.util.gui.Icons;

/**
 * A dialog to select the initial operation mode of the application.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
class ChooseStateDialog
    extends JDialog {

  /**
   * This dialog's actual selection status.
   */
  private OperationMode selection;

  /**
   * Creates new instance.
   */
  public ChooseStateDialog() {
    super((JFrame) null, true);
    initComponents();
    setLocationRelativeTo(null);
  }

  /**
   * Returns this dialog's selection status.
   *
   * @return This dialog's selection status.
   */
  public OperationMode getSelection() {
    return selection;
  }

  /**
   * Closes this dialog with the given selection status.
   *
   * @param retStatus The selection status to be set.
   */
  private void doClose(OperationMode retStatus) {
    selection = retStatus;
    setVisible(false);
    dispose();
  }

  // CHECKSTYLE:OFF
  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    titleLabel = new javax.swing.JLabel();
    modellingButton = new javax.swing.JButton();
    jButton2 = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/opentcs/guing/res/labels"); // NOI18N
    setTitle(bundle.getString("chooseStateDialog.dialogTitle")); // NOI18N
    setIconImages(Icons.getOpenTCSIcons());
    setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
    getContentPane().setLayout(new java.awt.GridBagLayout());

    titleLabel.setText(bundle.getString("chooseStateDialog.title")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.insets = new java.awt.Insets(10, 0, 20, 0);
    getContentPane().add(titleLabel, gridBagConstraints);

    modellingButton.setText(bundle.getString("chooseStateDialog.modelling")); // NOI18N
    modellingButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        modellingButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 5);
    getContentPane().add(modellingButton, gridBagConstraints);

    jButton2.setText(bundle.getString("chooseStateDialog.operating")); // NOI18N
    jButton2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton2ActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 5);
    getContentPane().add(jButton2, gridBagConstraints);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void modellingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modellingButtonActionPerformed
    doClose(OperationMode.MODELLING);
  }//GEN-LAST:event_modellingButtonActionPerformed

  private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    doClose(OperationMode.OPERATING);
  }//GEN-LAST:event_jButton2ActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton jButton2;
  private javax.swing.JButton modellingButton;
  private javax.swing.JLabel titleLabel;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON
}
