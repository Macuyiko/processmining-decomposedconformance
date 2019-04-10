package org.processmining.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.parameters.ConvertCausalActivityGraphToActivityClusterArrayParameters;

public class ConvertCausalActivityGraphToActivityClusterArrayDialog extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5552669985865681115L;

	public ConvertCausalActivityGraphToActivityClusterArrayDialog(final UIPluginContext context, final ConvertCausalActivityGraphToActivityClusterArrayParameters parameters) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));
		
		add(new JLabel("No configuration required"), "0, 0");
	}

}
