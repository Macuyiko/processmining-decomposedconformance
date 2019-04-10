package org.processmining.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;

import com.fluxicon.slickerbox.components.SlickerButton;

public class MarkingsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2410111984378654847L;
	private Map<String, Place> placeMap;

	@SuppressWarnings("unchecked")
	public MarkingsPanel(final PetrinetGraph net, final Set<Marking> markings) {

		placeMap = new HashMap<String, Place>();
		for (Place place : net.getPlaces()) {
			placeMap.put(place.getLabel(), place);
		}

		double size[][] = { { TableLayoutConstants.FILL, 60, TableLayoutConstants.FILL, 60 },
				{ 30, 30, TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));

		setOpaque(false);
		
		final ProMTextField textField = new ProMTextField();
		textField.setText(convert(markings));
		add(textField, "0, 0, 2, 0");
		textField.setPreferredSize(new Dimension(100, 25));

		SlickerButton validateButton = new SlickerButton("Validate");
		validateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String input = textField.getText();
				Set<Marking> inputMarkings = convert(net, input);
				if (inputMarkings == null) {
					textField.visualizeStatus(false);
				} else {
					textField.visualizeStatus(true);
					textField.setText(convert(inputMarkings));
					markings.clear();
					markings.addAll(inputMarkings);
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

	private String convert(Set<Marking> markings) {
		String sep = "";
		String sep2 = "";
		String result = "";
		for (Marking marking : markings) {
			result += sep2;
			sep = "";
			for (Place place : marking.baseSet()) {
				String weight = marking.occurrences(place) != 1 ? marking.occurrences(place) + "." : "";
				result += sep + weight + place.getLabel();
				sep = " + ";
			}
			sep2 = ", ";
		}
		return result;
	}

	private Set<Marking> convert(PetrinetGraph net, String input) {
		String[] inputs = input.split("[,]");
		Set<Marking> markings = new HashSet<Marking>();
		if (input.trim().length() > 0) {
			for (int i = 0; i < inputs.length; i++) {
				if (inputs[i].trim().length() > 0) {
					Marking marking = convertMarking(net, inputs[i]);
					if (marking == null) {
						return null;
					}
					markings.add(marking);
				} else {
					markings.add(new Marking());
				}
			}
		}
		if (markings.isEmpty()) {
			markings.add(new Marking());
		}
		return markings;
	}

	private Marking convertMarking(PetrinetGraph net, String input) {
		String[] markedPlaces = input.split("[+]");
		Marking marking = new Marking();
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
		return marking;
	}
}
