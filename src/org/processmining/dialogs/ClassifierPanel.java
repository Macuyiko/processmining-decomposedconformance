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

import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.framework.util.ui.widgets.ProMList;

public class ClassifierPanel extends JPanel {
	
	private static final long serialVersionUID = -7242932924333294111L;

	@SuppressWarnings("unchecked")
	public ClassifierPanel(List<XEventClassifier> classifiers, final List<XEventClassifier> selectedClassifiers) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));

		setOpaque(false);
		
		@SuppressWarnings("rawtypes")
		DefaultListModel listModel = new DefaultListModel();
		for (XEventClassifier classifier: classifiers) {
			listModel.addElement(classifier);
		}
		final ProMList<XEventClassifier> list = new ProMList<XEventClassifier>("Select classifier", listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final XEventClassifier defaultClassifier = selectedClassifiers.iterator().next();
		list.setSelection(defaultClassifier);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<XEventClassifier> selected = list.getSelectedValuesList();
				if (selected.size() == 1) {
					selectedClassifiers.clear();
					selectedClassifiers.add(selected.get(0));
				} else {
					/*
					 * Nothing selected. Revert to selection of default classifier.
					 */
					list.setSelection(defaultClassifier);
					selectedClassifiers.clear();
					selectedClassifiers.add(defaultClassifier);
				}
			}
		});
		list.setPreferredSize(new Dimension(100, 100));
		add(list, "0, 0");

	}

}
