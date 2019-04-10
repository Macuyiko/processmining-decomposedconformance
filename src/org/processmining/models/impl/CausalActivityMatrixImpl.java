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
import org.processmining.models.CausalActivityMatrix;
import org.processmining.utils.HTMLUtils;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class CausalActivityMatrixImpl implements CausalActivityMatrix {

	private DoubleMatrix2D values;
	private List<XEventClass> sortedActivities;
	private Map<XEventClass, Integer> indices;
	private String label;

	protected CausalActivityMatrixImpl() {
		
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
		/*
		 * Initialize the matrix (n equals the number of activities).
		 */
		values = new DenseDoubleMatrix2D(n, n);
	}

	public String getLabel() {
		return label;
	}
	
	public List<XEventClass> getActivities() {
		return sortedActivities;
	}

	public void setValue(XEventClass rowActivity, XEventClass columnActivity, double value) {
		if (indices.containsKey(rowActivity) && indices.containsKey(columnActivity)) {
			values.set(indices.get(rowActivity), indices.get(columnActivity), value);
		}
	}

	public double getValue(XEventClass rowActivity, XEventClass columnActivity) {
		if (indices.containsKey(rowActivity) && indices.containsKey(columnActivity)) {
			return values.get(indices.get(rowActivity), indices.get(columnActivity));
		}
		return 0.0;
	}

	public void importFromStream(InputStream input) throws IOException {
		Reader streamReader = new InputStreamReader(input);
		CsvReader csvReader = new CsvReader(streamReader);
		Map<String, XEventClass> map = new HashMap<String, XEventClass>();
		List<XEventClass> activities = new ArrayList<XEventClass>();
		if (csvReader.readRecord()) {
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
				Double value = Double.valueOf(csvReader.get(i));
				assert (value != null);
				values.set(rowIndex, columnIndex, value);
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
				csvWriter.write(String.valueOf(values.get(indices.get(rowActivity), indices.get(columnActivity))));
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
		buffer.append("<h1>Causal Activity Matrix for \"" + HTMLUtils.encode(label) + "\"</h1>");
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
				buffer.append("<td align=\"center\">" + values.get(r, c) + "</td>");
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
