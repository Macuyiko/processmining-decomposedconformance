package org.processmining.models.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.util.Pair;
import org.processmining.models.CausalActivityGraph;
import org.processmining.utils.HTMLUtils;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class CausalActivityGraphImpl implements CausalActivityGraph {

	private DoubleMatrix2D values;
	private List<XEventClass> sortedActivities;
	private Set<XEventClass> setActivities;
	private Map<XEventClass, Integer> indices;
	private Set<Pair<XEventClass, XEventClass>> causalities;
	private String label;

	protected CausalActivityGraphImpl() {
		
	}
	
	public void init(String label, Set<XEventClass> activities) {
		this.label = label;
		/*
		 * Sort the activities (based on their ids) and store them in an
		 * unmodifiable list.
		 */
		sortedActivities = new ArrayList<XEventClass>(activities);
		Collections.sort(sortedActivities);
		sortedActivities = Collections.unmodifiableList(sortedActivities);
		setActivities = new HashSet<XEventClass>(sortedActivities);
		/*
		 * Create an index for every sorted activity.
		 */
		indices = new HashMap<XEventClass, Integer>();
		int n = 0;
		for (XEventClass activity : sortedActivities) {
			indices.put(activity, n);
			n++;
		}
		values = new SparseDoubleMatrix2D(n, n);
		causalities = null;
	}

	public String getLabel() {
		return label;
	}
	
	public void setActivity(XEventClass activity) {
		if (sortedActivities.contains(activity)) {
			setActivities.add(activity);
		}
	}

	public void setCausality(XEventClass fromActivity, XEventClass toActivity) {
		if (indices.containsKey(fromActivity) && indices.containsKey(toActivity)) {
			setActivities.add(fromActivity);
			setActivities.add(toActivity);
			values.set(indices.get(fromActivity), indices.get(toActivity), 1.0);
			causalities = null;
		}
	}

	public void resetActivity(XEventClass activity) {
		if (indices.containsKey(activity)) {
			for (int c = 0; c < sortedActivities.size(); c++) {
				values.set(indices.get(activity), c, 0.0);
			}
			for (int r = 0; r < sortedActivities.size(); r++) {
				values.set(r, indices.get(activity), 0.0);
			}
			values.trimToSize();
			causalities = null;
		}
		setActivities.remove(activity);
	}

	public void resetCausality(XEventClass fromActivity, XEventClass toActivity) {
		if (indices.containsKey(fromActivity) && indices.containsKey(toActivity)) {
			values.set(indices.get(fromActivity), indices.get(toActivity), 0.0);
			causalities = null;
		}
	}

	public List<XEventClass> getActivities() {
		return sortedActivities;
	}

	public Set<XEventClass> getSetActivities() {
		return setActivities;
	}

	public Set<Pair<XEventClass, XEventClass>> getSetCausalities() {
		if (causalities == null) {
			causalities = new HashSet<Pair<XEventClass, XEventClass>>();
			int n = sortedActivities.size();
			for (int r = 0; r < n; r++) {
				for (int c = 0; c < n; c++) {
					if (values.get(r, c) == 1.0) {
						causalities.add(new Pair<XEventClass, XEventClass>(sortedActivities.get(r), sortedActivities
								.get(c)));
					}
				}
			}
		}
		return causalities;
	}

	public Set<XEventClass> getPreSetActivities(XEventClass activity) {
		if (indices.containsKey(activity)) {
			Set<XEventClass> activities = new HashSet<XEventClass>();
			int n = sortedActivities.size();
			int r = indices.get(activity);
			for (int c = 0; c < n; c++) {
				if (values.get(r, c) == 1.0) {
					activities.add(sortedActivities.get(c));
				}
			}
			return activities;
		}
		return null;
	}

	public Set<XEventClass> getPostSetActivities(XEventClass activity) {
		if (indices.containsKey(activity)) {
			Set<XEventClass> activities = new HashSet<XEventClass>();
			int n = sortedActivities.size();
			int c = indices.get(activity);
			for (int r = 0; r < n; r++) {
				if (values.get(r, c) == 1.0) {
					activities.add(sortedActivities.get(r));
				}
			}
			return activities;
		}
		return null;
	}

	public Set<XEventClass> getSourceSetActivities() {
		Set<XEventClass> activities = new HashSet<XEventClass>();
		for (XEventClass activity : setActivities) {
			if (getPreSetActivities(activity).size() == 0) { 
				activities.add(activity);
			}
		}
		return activities;
	}

	public Set<XEventClass> getSinkSetActivities() {
		Set<XEventClass> activities = new HashSet<XEventClass>();
		for (XEventClass activity : setActivities) {
			if (getPostSetActivities(activity).size() == 0) { 
				activities.add(activity);
			}
		}
		return activities;
	}

	public void importFromStream(InputStream input) throws IOException {
		Reader streamReader = new InputStreamReader(input);
		CsvReader csvReader = new CsvReader(streamReader);
		Map<String, XEventClass> map = new HashMap<String, XEventClass>();
		List<XEventClass> activities = new ArrayList<XEventClass>();
		if (csvReader.readRecord()) {
			label = csvReader.get(0);
			for (int i = 1; i < csvReader.getColumnCount(); i++) {
				String s = csvReader.get(i);
				XEventClass eventClass = new XEventClass(s, i - 1);
				map.put(s, eventClass);
				activities.add(eventClass);
			}
		}
		init(csvReader.get(0), new HashSet<XEventClass>(activities));
		while (csvReader.readRecord()) {
			XEventClass rowEventClass = map.get(csvReader.get(0));
			assert (rowEventClass != null);
			int rowIndex = indices.get(rowEventClass);
			for (int i = 1; i < csvReader.getColumnCount(); i++) {
				XEventClass columnEventClass = activities.get(i-1);
				assert (columnEventClass != null);
				int columnIndex = indices.get(columnEventClass);
				String s = csvReader.get(i);
				values.set(rowIndex, columnIndex, s.length() > 0 ? 1.0 : 0.0);
			}
		}
		csvReader.close();
	}

	public void exportToFile(File file) throws IOException {
		Writer fileWriter = new FileWriter(file);
		CsvWriter csvWriter = new CsvWriter(fileWriter, ',');
		csvWriter.write(label);
		for (XEventClass eventClass : sortedActivities) {
			csvWriter.write(eventClass.getId());
		}
		csvWriter.endRecord();
		for (XEventClass rowActivity : sortedActivities) {
			csvWriter.write(rowActivity.getId());
			for (XEventClass columnActivity : sortedActivities) {
				double value = values.get(indices.get(rowActivity), indices.get(columnActivity));
				boolean edge = (value == 1.0);
				csvWriter.write(edge ? "X" : "");
			}
			csvWriter.endRecord();
		}
		csvWriter.close();
	}
	

	public String toHTMLString(boolean includeHTMLTags) {
		StringBuffer buffer = new StringBuffer();
		int n = sortedActivities.size();
		if (includeHTMLTags) {
			buffer.append("<html>");
		}
		buffer.append("<h1>Causal Activity Graph for \"" + HTMLUtils.encode(label) + "\"</h1>");
		buffer.append("<table>");
		buffer.append("<tr>");
		buffer.append("<th>From \\ To</th>");
		for (int c = 0; c < n; c++) {
			buffer.append("<th>" + sortedActivities.get(c).getId() + "</th>");
		}
		buffer.append("</tr>");
		for (int r = 0; r < n; r++) {
			buffer.append("<tr>");
			buffer.append("<th>" + sortedActivities.get(r).getId() + "</th>");
			for (int c = 0; c < n; c++) {
				buffer.append("<td align=\"center\">" + (values.get(r, c) == 1.0 ? "X" : "") + "</td>");
			}
			buffer.append("</tr>");
		}
		buffer.append("</table>");
		if (includeHTMLTags) {
			buffer.append("</html>");
		}
		return buffer.toString();
	}

}
