package org.processmining.dialogs.dc;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.models.AcceptingPetriNet;
import org.processmining.parameters.dc.DecomposeBySESEsAndBridgingParameters;

import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class DecomposeBySESEsAndBridgingDialog extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4751592035000718658L;

	public DecomposeBySESEsAndBridgingDialog(AcceptingPetriNet net, 
			final DecomposeBySESEsAndBridgingParameters parameters) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, 30 } };
		setLayout(new TableLayout(size));
		
		int min = 1;
		int max = net.getNet().getEdges().size();
		int value = (parameters.getMaxSize() > max)? max: parameters.getMaxSize();

		final NiceIntegerSlider slider = SlickerFactory.instance().
				createNiceIntegerSlider(
						"Maximal size of the components (k)", 
						min, 
						max, 
						value,
						Orientation.HORIZONTAL);
		slider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				parameters.setMaxSize(slider.getValue());
			}
			
		});
		slider.setOpaque(false);
		add(slider, "0, 0");
}
}
