package org.processmining.models;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.util.HTMLToString;

public interface CausalActivityMatrix extends HTMLToString {

	/**
	 * Initializes the matrix for the given collection of activities.
	 * 
	 * @param label
	 *            The label to use for this matrix.
	 * @param activities
	 *            The given collection of activities.
	 */
	void init(String label, Set<XEventClass> activities);

	/**
	 * Returns the label for this matrix.
	 * 
	 * @return The label for this matrix.
	 */
	String getLabel();
	
	
	/**
	 * Returns the (ordered) list of activities. The i-th activity corresponds
	 * to i-th column and row.
	 * 
	 * @return The (ordered) list of activities.
	 */
	List<XEventClass> getActivities();

	/**
	 * Sets the value for the given row and column activity.
	 * 
	 * @param rowActivity
	 *            The given row activity.
	 * @param columnActivity
	 *            The given column activity.
	 * @param value
	 *            The given value.
	 */
	void setValue(XEventClass rowActivity, XEventClass columnActivity, double value);

	/**
	 * Gets the value for the given row and column activity.
	 * 
	 * @param rowActivity
	 *            The given row activity.
	 * @param columnActivity
	 *            The given column activity.
	 * @return The value in the matrix for these activities.
	 */
	double getValue(XEventClass rowActivity, XEventClass columnActivity);
	
	public void importFromStream(InputStream input) throws IOException;
	public void exportToFile(File file) throws IOException;
}
