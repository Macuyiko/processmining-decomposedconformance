package org.processmining.models;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.util.HTMLToString;

public interface ActivityClusterArray extends HTMLToString {

	void init(String label, Set<XEventClass> activities);

	/**
	 * Adds a cluster with the given activities, even if a cluster with these
	 * activities already exists.
	 * 
	 * @param activities
	 *            The given activities.
	 * @return The index of the new cluster.
	 */
	int addCluster(Set<XEventClass> activities);

	/**
	 * Removes the first cluster with the given activities, if present.
	 * 
	 * @param activities
	 *            The given activities.
	 * @return The index of the removed cluster, if present. -1 if such a
	 *         cluster is not present.
	 */
	int removeCluster(Set<XEventClass> activities);

	/**
	 * Adds the given set of activities at the given index.
	 * 
	 * @param index The given index.
	 * @param activities The given set of activities.
	 */
	void addCluster(int index, Set<XEventClass> activities);
	
	/**
	 * Removes the cluster at the given index, if valid.
	 * 
	 * @param index
	 *            The given index.
	 */
	void removeCluster(int index);

	/**
	 * Returns the cluster at the given index, if valid.
	 * 
	 * @param index
	 *            The given index.
	 * @return The cluster at the given index, if valid. null if not valid.
	 */
	Set<XEventClass> getCluster(int index);

	/**
	 * Returns an ArrayList containing all clusters that contain all of the
	 * given activities.
	 * 
	 * @param activities
	 *            The given activities.
	 * @return An ArrayList containing all clusters that contain all of the
	 *         given activities.
	 */
	ArrayList<Set<XEventClass>> getClusters(Set<XEventClass> activities);

	/**
	 * Returns an ArrayList containing all clusters.
	 * 
	 * @return An ArrayList containing all clusters.
	 */
	ArrayList<Set<XEventClass>> getClusters();
	
	public void importFromStream(InputStream input) throws IOException;
	public void exportToFile(File file) throws IOException;
}
