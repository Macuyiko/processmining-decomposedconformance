package org.processmining.models;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.processmining.framework.plugin.PluginContext;

public interface AcceptingPetriNetArray {

	/**
	 * Initializes the array.
	 */
	void init();

	/**
	 * Adds the given net to the array.
	 * 
	 * @param net
	 *            The given net.
	 * @return The index of the added net.
	 */
	int addNet(AcceptingPetriNet net);

	/**
	 * Removes the first occurrence of the given net from the array.
	 * 
	 * @param net
	 *            The given net.
	 * @return The index of the removed net, if present. -1 if not present.
	 */
	int removeNet(AcceptingPetriNet net);

	/**
	 * Adds the given net at the given index in the array.
	 * 
	 * @param index
	 *            The given index.
	 * @param net
	 *            The given net.
	 */
	void addNet(int index, AcceptingPetriNet net);

	/**
	 * Removes the net from the given index from the array, if valid.
	 * 
	 * @param index
	 *            The given index.
	 */
	void removeNet(int index);

	/**
	 * Returns the net at the given index
	 * 
	 * @param index
	 *            The given index.
	 * @return The net at the given index, if valid. null if not valid.
	 */
	AcceptingPetriNet getNet(int index);
	
	int getSize();
	
	public void importFromStream(PluginContext context, InputStream input, String parent) throws Exception;
	public void exportToFile(PluginContext context, File file) throws IOException;
}
