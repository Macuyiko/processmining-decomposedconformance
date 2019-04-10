package org.processmining.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.framework.util.ui.widgets.ProMList;

public class MinerPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7656913719419272750L;

	@SuppressWarnings("unchecked")
	public MinerPanel(List<String> miners, final List<String> selectedMiners) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));

		setOpaque(false);
		
		@SuppressWarnings("rawtypes")
		DefaultListModel listModel = new DefaultListModel();
		for (String miner: miners) {
			listModel.addElement(miner);
		}
		final ProMList<String> list = new ProMList<String>("Select miner", listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final String defaultMiner = selectedMiners.iterator().next();
		list.setSelection(defaultMiner);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<String> selected = list.getSelectedValuesList();
				if (selected.size() == 1) {
					selectedMiners.clear();
					selectedMiners.add(selected.get(0));
				} else {
					/*
					 * Nothing selected. Revert to selection of default classifier.
					 */
					list.setSelection(defaultMiner);
					selectedMiners.clear();
					selectedMiners.add(defaultMiner);
				}
			}
		});
		list.setPreferredSize(new Dimension(100, 100));
		add(list, "0, 0");

	}

}
