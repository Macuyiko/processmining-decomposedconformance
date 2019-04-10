package org.processmining.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;

import com.fluxicon.slickerbox.components.SlickerButton;

public class MarkingPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 314934198760709824L;

	private Map<String, Place> placeMap;

	@SuppressWarnings("unchecked")
	public MarkingPanel(final PetrinetGraph net, final Marking marking) {

		placeMap = new HashMap<String, Place>();
		for (Place place : net.getPlaces()) {
			placeMap.put(place.getLabel(), place);
		}

		double size[][] = { { TableLayoutConstants.FILL, 60, TableLayoutConstants.FILL, 60 },
				{ 30, 30, TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));

		setOpaque(false);
		
		final ProMTextField textField = new ProMTextField();
		textField.setText(convert(marking));
		add(textField, "0, 0, 2, 0");
		textField.setPreferredSize(new Dimension(100, 25));

		SlickerButton validateButton = new SlickerButton("Validate");
		validateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String input = textField.getText();
				Marking inputMarking = convert(net, input);
				if (inputMarking == null) {
					textField.visualizeStatus(false);
				} else {
					textField.visualizeStatus(true);
					textField.setText(convert(inputMarking));
					marking.clear();
					for (Place place : inputMarking.baseSet()) {
						marking.add(place, inputMarking.occurrences(place));
					}
				}
			}
		});
		add(validateButton, "3, 0");

		@SuppressWarnings("rawtypes")
		DefaultListModel listModel = new DefaultListModel();
		for (Place place : net.getPlaces()) {
			listModel.addElement(place);
		}
		final ProMList<Place> list = new ProMList<Place>("Select place to insert", listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setPreferredSize(new Dimension(100, 100));
		add(list, "0, 2, 2, 2");

		SlickerButton insertButton = new SlickerButton("Insert");
		insertButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<Place> selected = list.getSelectedValuesList();
				if (selected.size() == 1) {
					Place selectedPlace = selected.get(0);
					textField.insertText(selectedPlace.getLabel());
				}
			}
		});
		add(insertButton, "1, 1");
	}

	private String convert(Marking marking) {
		String sep = "";
		String result = "";
		for (Place place : marking.baseSet()) {
			String weight = marking.occurrences(place) != 1 ? marking.occurrences(place) + "." : "";
			result += sep + weight + place.getLabel();
			sep = " + ";
		}
		return result;
	}

	private Marking convert(PetrinetGraph net, String input) {
		String[] markedPlaces = input.split("[+]");
		Marking marking = new Marking();
		if (input.trim().length() > 0) {
			for (int i = 0; i < markedPlaces.length; i++) {
				String markedPlace = markedPlaces[i];
				int weight = 0;
				Place place = net.getPlaces().isEmpty() ? null : net.getPlaces().iterator().next();
				String[] weightedPlace = markedPlace.split("[.]");
				if (weightedPlace.length == 2) {
					weight = Integer.parseInt(weightedPlace[0].trim());
					place = placeMap.get(weightedPlace[1].trim());
				} else if (weightedPlace.length == 1) {
					weight = 1;
					place = placeMap.get(markedPlace.trim());
				}
				if (place == null || weight == 0) {
					return null;
				}
				marking.add(place, weight);
			}
		}
		return marking;
	}
}
