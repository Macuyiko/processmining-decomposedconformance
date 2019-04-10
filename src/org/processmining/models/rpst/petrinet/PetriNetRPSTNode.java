package org.processmining.models.rpst.petrinet;

import java.util.LinkedList;
import java.util.List;

import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * 
 * Class representing a RPST node for a RPST generated from a Petri Net..
 * 
 * See: Artem Polyvyanyy, Jussi Vanhatalo, Hagen Volzer: Simplified Computation 
 * and Generalization of the Refined Process Structure Tree. WS-FM 2010: 25-41
 * 
 * @author Jorge Munoz-Gama (jmunoz)
 */
public class PetriNetRPSTNode {
	
	/** Unique Identifier */
	private String id;
	/** Name of the RPST node */
    private String name;
    /** Description of the RPST node */
    private String desc;

	
	/** Set of Transitions */
	private List<Transition> trans;
	/** Set of Places */
	private List<Place> places;
	/** Set of Arcs */
	private List<Arc> arcs;
	
	/** Entry node */
	private PetrinetNode entry;
	/** Exit node */
	private PetrinetNode exit;
	
	public PetriNetRPSTNode(String id, String name, String desc,
			List<Transition> trans, List<Place> places, List<Arc> arcs,
			PetrinetNode entry, PetrinetNode exit){
		
		this.id = (id == null)? "" : id;
		this.name = (name == null)? "" : name;
		this.desc = (desc == null)? "" : desc;
		
		this.trans = (trans == null)?   new LinkedList<Transition>(): trans;
		this.places = (places == null)? new LinkedList<Place>(): places;
		this.arcs = (arcs == null)? new LinkedList<Arc>() : arcs;
		
		this.entry = (entry == null)? null : entry;
		this.exit = (exit == null)? null: exit;	
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}

	public List<Transition> getTrans() {
		return trans;
	}

	public List<Place> getPlaces() {
		return places;
	}

	public List<Arc> getArcs() {
		return arcs;
	}

	public PetrinetNode getEntry() {
		return entry;
	}

	public PetrinetNode getExit() {
		return exit;
	}
	
	public String toString(){
		return this.name;
	}
	

}
