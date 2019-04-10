package org.processmining.utils.rpst.petrinet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.impl.DivideAndConquerFactory;
import org.processmining.models.rpst.petrinet.PetriNetRPSTNode;
import org.processmining.models.semantics.petrinet.Marking;

public class RPSTNode2PetriNet {
	
	public static Petrinet convertToPetriNet(PetriNetRPSTNode node){
		Map<DirectedGraphElement, DirectedGraphElement> map = new HashMap<DirectedGraphElement, DirectedGraphElement>();
		return convertToPetriNet(node, map);
	}
	
	public static Petrinet convertToPetriNet(PetriNetRPSTNode node, Map<DirectedGraphElement, DirectedGraphElement> map){
		Petrinet net = PetrinetFactory.newPetrinet(node.getName());	
		for (Transition origT: node.getTrans()){
			Transition newT = net.addTransition(origT.getLabel());
			map.put(origT, newT);
		}
		for(Place origP: node.getPlaces()){
			Place newP = net.addPlace(origP.getLabel());
			map.put(origP, newP);
		}
		for(Arc origA: node.getArcs()){
			PetrinetNode source = (PetrinetNode) map.get(origA.getSource());
			PetrinetNode target = (PetrinetNode) map.get(origA.getTarget());
			if(source instanceof Transition){
				Arc newA = net.addArc((Transition)source, (Place)target, origA.getWeight());
				map.put(origA, newA);
			}
			else if (source instanceof Place){
				Arc newA = net.addArc((Place) source, (Transition) target, origA.getWeight());
				map.put(origA, newA);
			}
		}
		return net;
	}
	
	public static AcceptingPetriNet convertToAcceptingPetriNet(PetriNetRPSTNode node, Marking iniM, Set<Marking> endMarkings){
		Map<DirectedGraphElement, DirectedGraphElement> map = new HashMap<DirectedGraphElement, DirectedGraphElement>();
		return convertToAcceptingPetriNet(node, iniM, endMarkings, map);
		
	}
	
	public static AcceptingPetriNet convertToAcceptingPetriNet(PetriNetRPSTNode node, Marking iniM, Set<Marking> endMarkings, 
		Map<DirectedGraphElement, DirectedGraphElement> map){	
		//Generate the Net
		Petrinet net = RPSTNode2PetriNet.convertToPetriNet(node, map);
		
		//Generate the Initial Marking
		Marking newIniM = new Marking();
		for(Place origP: iniM){
			if(map.containsKey(origP)) newIniM.add((Place) map.get(origP));
		}
		
		//Generate the Final Markings
		Set<Marking> newEndMarkings = new HashSet<Marking>();
		for(Marking origEndM: endMarkings){
			Marking newEndM = new Marking();
			for(Place origP: origEndM){
				if(map.containsKey(origP)) newEndM.add((Place) map.get(origP));
			}
			newEndMarkings.add(newEndM);
		}
		
		//Construct the cloned Accepting Petri Net
		AcceptingPetriNet acceptingNet = DivideAndConquerFactory.createAcceptingPetriNet();
		acceptingNet.init(net);
		acceptingNet.setInitialMarking(newIniM);
		acceptingNet.setFinalMarkings(newEndMarkings);
		return acceptingNet;
	}
}
