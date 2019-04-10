package org.processmining.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.parameters.DecomposeEventLogUsingActivityClusterArrayParameters;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class DecomposeEventLogUsingActivityClusterArrayDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7737217369595500742L;

	public DecomposeEventLogUsingActivityClusterArrayDialog(XLog eventLog, final DecomposeEventLogUsingActivityClusterArrayParameters parameters) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, 30 } };
		setLayout(new TableLayout(size));
		List<XEventClassifier> classifiers = new ArrayList<XEventClassifier>(eventLog.getClassifiers());
		if (classifiers.isEmpty()) {
			classifiers.add(new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));
		}
		add(new ClassifierPanel(classifiers, parameters.getClassifierList()), "0, 0");

		final JCheckBox box = SlickerFactory.instance().createCheckBox("Remove empty traces", true);
		box.setSelected(parameters.getRemoveEmptyTraces());
		box.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setRemoveEmptyTraces(box.isSelected());
			}
			
		});
		box.setOpaque(false);
		add(box, "0, 1");
}
}
