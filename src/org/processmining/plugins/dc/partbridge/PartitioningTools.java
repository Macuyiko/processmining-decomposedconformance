package org.processmining.plugins.dc.partbridge;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.processmining.models.rpst.petrinet.PetriNetRPST;
import org.processmining.models.rpst.petrinet.PetriNetRPSTNode;

/**
 * Tools to perform cuts and partitions over a RPST
 * 
 * @author Jorge Munoz-Gama (jmunoz)
 */
public class PartitioningTools {
	
	/**
	 * Return a set of RPST Nodes with the largest number of arcs below a given 
	 * threshold, such that the set represents a partition over the edges of the
	 * original Petri net (if the RPST has not been modified), i.e., all edges of
	 * the petri net belong to one node, and only one.
	 * 
	 * See: Jorge Munoz-Gama, Josep Carmona and Wil M.P. van der Aalst. 
	 * Conformance Checking in the Large: Partitioning and Topology
	 * @param rpst RPST 
	 * @param maxSize The nodes returned have a number of arcs assigned at most as large as the given threshold.
	 * @return Set of nodes of the RPST
	 */
	public static List<PetriNetRPSTNode> partitioning(PetriNetRPST rpst, int maxSize){
		List<PetriNetRPSTNode> part = new LinkedList<PetriNetRPSTNode>();
		
		Queue<PetriNetRPSTNode> toExplore = new LinkedList<PetriNetRPSTNode>();
		toExplore.add(rpst.getRoot());
		while(!toExplore.isEmpty()){
			PetriNetRPSTNode curr = toExplore.poll();
			if(curr.getArcs().size() <= maxSize){
				part.add(curr);
			}
			else{
				toExplore.addAll(rpst.getChildren(curr));
			}
		}
		
		return part;
	}

}
