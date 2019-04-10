package org.processmining.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.util.HashSet;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.CausalActivityMatrix;
import org.processmining.models.impl.DivideAndConquerFactory;
import org.processmining.parameters.ConvertCausalActivityMatrixToCausalActivityGraphParameters;
import org.processmining.plugins.VisualizeCausalActivityMatrixPlugin;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class ConvertCausalActivityMatrixToCausalActivityGraphDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6803198545757615783L;

	public ConvertCausalActivityMatrixToCausalActivityGraphDialog(final UIPluginContext context, final CausalActivityMatrix matrix, final ConvertCausalActivityMatrixToCausalActivityGraphParameters parameters) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, 30, 30 } };
		setLayout(new TableLayout(size));
		
		final CausalActivityMatrix viewMatrix = DivideAndConquerFactory.createCausalActivityMatrix();
		viewMatrix.init(matrix.getLabel(), new HashSet<XEventClass>(matrix.getActivities()));
		updateViewMatrix(viewMatrix, matrix, parameters);
		
		final JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(100, 100));
		double panelSize[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		panel.setLayout(new TableLayout(panelSize));
		add(panel, "0, 0");
		
		final VisualizeCausalActivityMatrixPlugin visualizer = new VisualizeCausalActivityMatrixPlugin();
		panel.add(visualizer.visualizeAsXixi(context, viewMatrix), "0, 0");
		
		final NiceSlider zeroSlider = SlickerFactory.instance().createNiceDoubleSlider("Zero Threshold", -1.0, 1.0,  parameters.getZeroValue(),
				Orientation.HORIZONTAL);
		zeroSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				parameters.setZeroValue(-1.0 + zeroSlider.getSlider().getValue() * 2.0 / 10000.0);
				panel.removeAll();
				updateViewMatrix(viewMatrix, matrix, parameters);
				panel.add(visualizer.visualizeAsXixi(context, viewMatrix), "0, 0");
				revalidate();
				repaint();
			}
		});
		add(zeroSlider, "0, 1");

		final NiceSlider concSlider = SlickerFactory.instance().createNiceDoubleSlider("Concurrency Threshold", 0.0, 2.0,  parameters.getConcurrencyRatio(),
				Orientation.HORIZONTAL);
		concSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				parameters.setConcurrencyRatio(concSlider.getSlider().getValue() * 2.0 / 10000.0);
				panel.removeAll();
				updateViewMatrix(viewMatrix, matrix, parameters);
				panel.add(visualizer.visualizeAsXixi(context, viewMatrix), "0, 0");
				revalidate();
				repaint();
			}
		});
		add(concSlider, "0, 2");
}
	
	private void updateViewMatrix(CausalActivityMatrix viewMatrix, CausalActivityMatrix matrix, ConvertCausalActivityMatrixToCausalActivityGraphParameters parameters) {
		for (XEventClass rowActivity : matrix.getActivities()) {
			for (XEventClass columnActivity : matrix.getActivities()) {
				viewMatrix.setValue(rowActivity, columnActivity, parameters.correct(matrix.getValue(rowActivity, columnActivity), matrix.getValue(columnActivity, rowActivity)));
			}
		}
	}
}
