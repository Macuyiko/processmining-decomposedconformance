/**
 * 
 */
package org.processmining.ui.rpst.petrinet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.util.ListSelectionPanel;

/**
 * @author jmunoz
 *
 */
public class FragSelectionPanel extends ListSelectionPanel {

	public FragSelectionPanel(String name, String title, boolean interactive) {
		super(name, title, interactive);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
public void selectElements(Set<PetrinetNode> nodes){
		
		Map<DirectedGraphElement, String> labelled = new HashMap<DirectedGraphElement, String>();
		for (DirectedGraphElement elt : nodes) {
			labelled.put(elt, elt.getLabel());
		}
		
		this.selectElements(labelled);
	}

}
