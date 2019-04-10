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
import org.processmining.models.ActivityClusterArray;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class ActivityClusterArrayImpl implements ActivityClusterArray {

	private List<XEventClass> sortedActivities;
	private Map<XEventClass, Integer> indices;
	private List<Set<XEventClass>> clusters;
	private String label;

	protected ActivityClusterArrayImpl() {
		
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
		/*
		 * Create an index for every sorted activity.
		 */
		indices = new HashMap<XEventClass, Integer>();
		int n = 0;
		for (XEventClass activity : sortedActivities) {
			indices.put(activity, n);
			n++;
		}
		clusters = new ArrayList<Set<XEventClass>>();
	}

	public int addCluster(Set<XEventClass> activities) {
		if (sortedActivities.containsAll(activities)) {
			Set<XEventClass> cluster = new HashSet<XEventClass>(activities);
			clusters.add(cluster);
			return clusters.indexOf(cluster);
		}
		return -1;
	}

	public int removeCluster(Set<XEventClass> activities) {
		int index = clusters.indexOf(activities);
		if (index >= 0) {
			clusters.remove(index);
		}
		return index;
	}

	public void addCluster(int index, Set<XEventClass> activities) {
		if (sortedActivities.containsAll(activities)) {
			clusters.add(index, new HashSet<XEventClass>(activities));
		}
	}

	public void removeCluster(int index) {
		if (0 <= index && index < clusters.size()) {
			clusters.remove(index);
		}
	}

	public Set<XEventClass> getCluster(int index) {
		if (0 <= index && index < clusters.size()) {
			return clusters.get(index);
		}
		return null;
	}

	public ArrayList<Set<XEventClass>> getClusters(Set<XEventClass> activities) {
		ArrayList<Set<XEventClass>> filteredClusters = new ArrayList<Set<XEventClass>>();
		for (Set<XEventClass> cluster : clusters) {
			if (cluster.containsAll(activities)) {
				filteredClusters.add(cluster);
			}
		}
		return filteredClusters;
	}

	public ArrayList<Set<XEventClass>> getClusters() {
		return getClusters(new HashSet<XEventClass>());
	}

	public String toHTMLString(boolean includeHTMLTags) {
		StringBuffer buffer = new StringBuffer();
		int n = sortedActivities.size();
		if (includeHTMLTags) {
			buffer.append("<html>");
		}
		buffer.append("<h1>Activity Cluster Array for " + label + "</h1>");
		buffer.append("<table>");
		buffer.append("<tr>");
		buffer.append("<th></th>");
		for (int c = 0; c < n; c++) {
			buffer.append("<th>" + sortedActivities.get(c).getId() + "</th>");
		}
		buffer.append("</tr>");
		int r = 1;
		for (Set<XEventClass> cluster : clusters) {
			buffer.append("<tr>");
			buffer.append("<th>Cluster " + r + "</th>");
			for (int c = 0; c < n; c++) {
				buffer.append("<td align=\"center\">" + (cluster.contains(sortedActivities.get(c)) ? "X" : "") + "</td>");
			}
			buffer.append("</tr>");
			r++;
		}
		buffer.append("</table>");
		if (includeHTMLTags) {
			buffer.append("</html>");
		}
		return buffer.toString();
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
			Set<XEventClass> cluster = new HashSet<XEventClass>();
			for (int i = 1; i < csvReader.getColumnCount(); i++) {
				XEventClass activity  = activities.get(i-1);
				assert (activity != null);
				if (csvReader.get(i).length() > 0) {
					cluster.add(activity);
				}
			}
			clusters.add(cluster);
		}
		csvReader.close();
	}

	public void exportToFile(File file) throws IOException {
		Writer fileWriter = new FileWriter(file);
		CsvWriter csvWriter = new CsvWriter(fileWriter, ',');
		csvWriter.write(label);
		for (XEventClass activity : sortedActivities) {
			csvWriter.write(activity.getId());
		}
		csvWriter.endRecord();
		for (Set<XEventClass> cluster: clusters) {
			csvWriter.write("");
			for (XEventClass activity : sortedActivities) {
				csvWriter.write(cluster.contains(activity) ? "X" : "");
			}
			csvWriter.endRecord();
		}
		csvWriter.close();
	}
	
}
