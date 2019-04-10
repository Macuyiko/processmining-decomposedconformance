package org.processmining.models.rpst.petrinet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.jbpt.algo.tree.rpst.IRPSTNode;
import org.jbpt.petri.Flow;
import org.jbpt.petri.Node;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.impl.DivideAndConquerFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.utils.rpst.petrinet.ProMJBPTPetriNet;


/**
 * Class representing the RPST structure of a Petri Net.
 * 
 * See: Artem Polyvyanyy, Jussi Vanhatalo, Hagen Volzer: Simplified Computation 
 * and Generalization of the Refined Process Structure Tree. WS-FM 2010: 25-41
 * 
 * @author Jorge Munoz-Gama (jmunoz)
 */
public class PetriNetRPST {
	
	/** Name of the RPST */
	private String name;
	/** Petri Net base of the RPST decomposition */
	private AcceptingPetriNet net;
	/** Children relations of the RPST */
	private Map<PetriNetRPSTNode, List<PetriNetRPSTNode>> children;
	/** Root of the RPST*/
	private PetriNetRPSTNode root;
	
	
	public PetriNetRPST(AcceptingPetriNet origNet){
		this("RPST of "+origNet.getNet().getLabel(), origNet);
	}
	
	public PetriNetRPST(String name, AcceptingPetriNet origNet){
		this.name = name;
		this.net = cloneAcceptingPetriNet(origNet);
		this.children = new HashMap<PetriNetRPSTNode, List<PetriNetRPSTNode>>();
		init();
	}
	
	private void init(){
		//Build Prom/JBPT Petri Net
		ProMJBPTPetriNet multiNet = new ProMJBPTPetriNet(this.net.getNet());
		
		//Compute the RPST and the SESEs by the JBPT tool
		org.jbpt.algo.tree.rpst.RPST<Flow,Node> rpstJBPT = new org.jbpt.algo.tree.rpst.RPST<Flow,Node>(multiNet.getJbpt());
		
		//Root
		IRPSTNode<Flow, Node> rootJBPT = rpstJBPT.getRoot();
		this.root = createRPSTNode(rootJBPT, multiNet);
		
		//Preparation for exploring the RPST Tree
		Queue<IRPSTNode<Flow, Node>> toExploreJBPT = new LinkedList<IRPSTNode<Flow, Node>>();
		toExploreJBPT.add(rootJBPT);
		Queue<PetriNetRPSTNode> toExploreRPST = new LinkedList<PetriNetRPSTNode>();
		toExploreRPST.add(root);
				
		//Exploration of RPST Tree
		while(!toExploreRPST.isEmpty()){
			IRPSTNode<Flow, Node> currJBPT = toExploreJBPT.poll();
			PetriNetRPSTNode curr = toExploreRPST.poll();
			
			Collection<IRPSTNode<Flow, Node>> childrenJBPT = rpstJBPT.getChildren(currJBPT);
			List<PetriNetRPSTNode> children = new LinkedList<PetriNetRPSTNode>();
			for(IRPSTNode<Flow, Node> childJBPT : childrenJBPT){
				PetriNetRPSTNode child = createRPSTNode(childJBPT, multiNet);
				children.add(child);	
				toExploreJBPT.add(childJBPT);
				toExploreRPST.add(child);
			}
			this.children.put(curr, children);
		}
	}
	
	
	private PetriNetRPSTNode createRPSTNode(IRPSTNode<Flow, Node> nodeJBPT, ProMJBPTPetriNet multiNet) {
		//Arcs
		List<Arc> arcs = new LinkedList<Arc>();
		Set<Node> nodes = new HashSet<Node>();
		for(Flow flow: nodeJBPT.getFragment()){
			arcs.add(multiNet.jbpt2PromArc(flow));
			nodes.add(flow.getSource());
			nodes.add(flow.getTarget());
		}
		
		//Places and Transitions
		List<Transition> trans = new LinkedList<Transition>();
		List<Place> places = new LinkedList<Place>();
		for(Node node: nodes){
			if(multiNet.jbpt2PromNode(node) instanceof Transition) trans.add((Transition) multiNet.jbpt2PromNode(node));
			else if(multiNet.jbpt2PromNode(node) instanceof Place) places.add((Place) multiNet.jbpt2PromNode(node));
		}
		
		//Build the node
		return new PetriNetRPSTNode(nodeJBPT.getId(), nodeJBPT.getName(), nodeJBPT.getDescription(), 
				trans, places, arcs, multiNet.jbpt2PromNode(nodeJBPT.getEntry()), multiNet.jbpt2PromNode(nodeJBPT.getExit()));
	}

	
	
	
	public String getName() {
		return name;
	}

	public AcceptingPetriNet getNet() {
		return net;
	}

	public PetriNetRPSTNode getRoot() {
		return root;
	}
	
	public Set<PetriNetRPSTNode> getNodes(){
		return this.children.keySet();
	}
	
	public List<PetriNetRPSTNode> getChildren(PetriNetRPSTNode parent){
		return this.children.get(parent);
	}

	//TODO Move the function to DivideAndConquereFactory
	public static AcceptingPetriNet cloneAcceptingPetriNet(AcceptingPetriNet origAcceptingNet){
		//Clone the Net
		Map<DirectedGraphElement, DirectedGraphElement> map = new HashMap<DirectedGraphElement, DirectedGraphElement>();
		Petrinet cloneNet = PetrinetFactory.clonePetrinet(origAcceptingNet.getNet(), map);
		
		//Clone the Initial Marking
		Marking cloneIniM = new Marking();
		for(Place origP: origAcceptingNet.getInitialMarking()){
			cloneIniM.add((Place) map.get(origP));
		}
		
		//Clone the Final Markings
		Set<Marking> cloneEndMarkings = new HashSet<Marking>();
		for(Marking origEndM: origAcceptingNet.getFinalMarkings()){
			Marking cloneEndM = new Marking();
			for(Place origP: origEndM){
				cloneEndM.add((Place) map.get(origP));
			}
			cloneEndMarkings.add(cloneEndM);
		}
		
		//Construct the cloned Accepting Petri Net
		AcceptingPetriNet cloneAcceptingNet = DivideAndConquerFactory.createAcceptingPetriNet();
		cloneAcceptingNet.init(cloneNet);
		cloneAcceptingNet.setInitialMarking(cloneIniM);
		cloneAcceptingNet.setFinalMarkings(cloneEndMarkings);

		return cloneAcceptingNet;
	}

}
