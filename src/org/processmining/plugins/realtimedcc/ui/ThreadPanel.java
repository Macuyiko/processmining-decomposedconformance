package org.processmining.plugins.realtimedcc.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JTextArea;

public class ThreadPanel extends JPanel {
	private static final long serialVersionUID = -4929817252818366608L;
	private JTextArea textArea;

	public ThreadPanel() {
		this.setLayout(new BorderLayout());
		this.textArea = new JTextArea("Waiting for data...");
		this.add(textArea, BorderLayout.CENTER);
	}
	
	public void updateText(String text) {
		textArea.setText(text);
	}

	 @Override
     public Dimension getPreferredSize() {
         return new Dimension(ThreadListPanel.W, ThreadListPanel.H);
     }
}