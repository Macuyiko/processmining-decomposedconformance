package org.processmining.models;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

public interface AcceptingPetriNet {

	/**
	 * Initializes the AcceptingPetriNet with the given Petri net.
	 * 
	 * @param net
	 *            The given Petri net.
	 */
	void init(Petrinet net);

	void init(PluginContext context, Petrinet net);

	/**
	 * Set the initial marking to the given marking. By default, the marking
	 * where every source place contains one token is the initial marking.
	 * 
	 * @param initialMarking
	 *            The given initial marking.
	 */
	void setInitialMarking(Marking initialMarking);

	/**
	 * Set the set of final markings to the given set of markings. By default,
	 * this set contains a marking for every sink place, where the sink place is
	 * marked once in that marking.
	 * 
	 * @param finalMarkings
	 *            The set of final markings.
	 */
	void setFinalMarkings(Set<Marking> finalMarkings);

	/**
	 * Returns the Petri net.
	 * 
	 * @return The Petri net.
	 */
	Petrinet getNet();

	/**
	 * Returns the current initial marking.
	 * 
	 * @return The current initial marking.
	 */
	Marking getInitialMarking();

	/**
	 * Returns the current set of final markings.
	 * 
	 * @return The current set of final markings.
	 */
	Set<Marking> getFinalMarkings();
	
	public void importFromStream(PluginContext context, InputStream input) throws Exception;
	public void exportToFile(PluginContext context, File file) throws IOException;
}
