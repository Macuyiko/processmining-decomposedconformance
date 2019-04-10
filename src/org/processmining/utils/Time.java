package org.processmining.utils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.HTMLToString;

public class Time implements HTMLToString {

	private long start;
	private PluginContext context;
	private String label;
	
	private static Map<String, List<Double>> times = new HashMap<String, List<Double>>();
	
	public Time(PluginContext context, String label) {
		start = System.nanoTime();
		this.context = context;
		this.label = label;
		if (!times.keySet().contains(label)) {
			times.put(label, new ArrayList<Double>());
		}
	}
	
	public void end() {
		long period = System.nanoTime() - start;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);

		context.log(label + " took " + nf.format(period / 1000000000.0) + " seconds");
		times.get(label).add(period / 1000000000.0);
	}

	public void end(int i, int size) {
		long period = System.nanoTime() - start;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);

		context.log(label + "[" + i + "/" + size + "] took " + nf.format(period / 1000000000.0) + " seconds");
		times.get(label).add(period / 1000000000.0);
	}

	public String toHTMLString(boolean includeHTMLTags) {
		int rows = 0;
		StringBuffer buffer = new StringBuffer();
		if (includeHTMLTags) {
			buffer.append("<html>");
		}
		buffer.append("<head>");
		buffer.append("<title>Time information</title>");
		buffer.append("</head><body><font fact=\"Arial\">");
		buffer.append("<h1>Timeinformation</h1>");
		buffer.append("<table><tr>");
		for (String col: times.keySet()) {
			buffer.append("<th>" + col + "</th>");
			if (times.get(col).size() > rows) {
				rows = times.get(col).size();
			}
		}
		for (int row = 0; row < rows; row++) {
			buffer.append("</tr><tr>");
			for (String col: times.keySet()) {
				if (row < times.get(col).size()) {
					buffer.append("<td>" + times.get(col).get(row) + "</td>");
				} else {
					buffer.append("<td></td>");
				}
			}
		}
		buffer.append("</tr></table>");
		buffer.append("</font></body>");
		if (includeHTMLTags) {
			buffer.append("</html>");
		}
		return buffer.toString();
	}
}
