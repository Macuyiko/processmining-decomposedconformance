package org.processmining.models;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.util.HTMLToString;
import org.processmining.framework.util.Pair;

public interface CausalActivityGraph extends HTMLToString {

	/**
	 * Initializes the graph with the given set of activities. All activities
	 * will be set, all causalities will be reset.
	 * 
	 * @param label
	 *            The label to use for this graph.
	 * @param activities
	 *            The given set of activities.
	 */
	void init(String label, Set<XEventClass> activities);

	/**
	 * Returns the label for this graph.
	 * 
	 * @return The label for this graph.
	 */
	String getLabel();
	
	/**
	 * Sets the given activity.
	 * 
	 * @param activity
	 *            The given activity.
	 */
	void setActivity(XEventClass activity);

	/**
	 * Sets the causality from the given first activity to the given second
	 * activity. Sets both activities if they are not yet set.
	 * 
	 * @param fromActivity
	 *            The given first activity.
	 * @param toActivity
	 *            The given second activity.
	 */
	void setCausality(XEventClass fromActivity, XEventClass toActivity);

	/**
	 * Resets the given activity. Resets all causalities connected to this
	 * activity.
	 * 
	 * @param activity
	 *            The given activity.
	 */
	void resetActivity(XEventClass activity);

	/**
	 * Resets the causality from the given first activity to the given second
	 * activity.
	 * 
	 * @param fromActivity
	 *            The given first activity.
	 * @param toActivity
	 *            The given second activity.
	 */
	void resetCausality(XEventClass fromActivity, XEventClass toActivity);

	/**
	 * Returns the (ordered) list of activities in the graph. This includes
	 * activities that have not been set.
	 * 
	 * @return The (ordered) list of activities in the graph.
	 */
	List<XEventClass> getActivities();

	/**
	 * Returns the set of set activities in the graph.
	 * 
	 * @return The set of set activities in the graph.
	 */
	Set<XEventClass> getSetActivities();

	/**
	 * Returns the set of set causalities in the graph.
	 * 
	 * @return The set of set causalities in the graph.
	 */
	Set<Pair<XEventClass, XEventClass>> getSetCausalities();

	/**
	 * Returns the preset of the given set activity, that is, the set of set
	 * activities that a outgoing causalities to the given activity.
	 * 
	 * @param activity
	 *            The given set activity.
	 * @return The preset of the given activity.
	 */
	Set<XEventClass> getPreSetActivities(XEventClass activity);

	/**
	 * Returns the postset of the given set activity, that is, the set of set
	 * activities that a incoming causalities from the given activity.
	 * 
	 * @param activity
	 *            The given set activity.
	 * @return The postset of the given set activity.
	 */
	Set<XEventClass> getPostSetActivities(XEventClass activity);

	/**
	 * Returns the set activities in the graph that have no incoming
	 * causalities.
	 * 
	 * @return The set activities in the graph that have no incoming
	 *         causalities.
	 */
	Set<XEventClass> getSourceSetActivities();

	/**
	 * Returns the set activities in the graph that have no outgoing
	 * causalities.
	 * 
	 * @return The set activities in the graph that have no outgoing
	 *         causalities.
	 */
	Set<XEventClass> getSinkSetActivities();
	
	public void importFromStream(InputStream input) throws IOException;
	public void exportToFile(File file) throws IOException;
}
