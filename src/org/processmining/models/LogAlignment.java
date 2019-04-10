package org.processmining.models;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.HTMLToString;

public interface LogAlignment extends HTMLToString {

	public void init();
	public Set<XEventClass> getCluster();
	public void setCluster(Set<XEventClass> cluster);
	public void addToCluster(Set<XEventClass> cluster);
	public Map<List<XEventClass>, TraceAlignment> getAlignments();
	public TraceAlignment getAlignment(List<XEventClass> trace);
	public void setAlignments(Map<List<XEventClass>, TraceAlignment> alignments);
	public void putAlignment(List<XEventClass> trace, TraceAlignment alignment);
	public void exportToFile(PluginContext context, File file) throws IOException;
	public void importFromStream(PluginContext context, InputStream input, String parent) throws IOException;
	
}
