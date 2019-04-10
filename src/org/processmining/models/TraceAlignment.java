package org.processmining.models;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.util.HTMLToString;
import org.processmining.framework.util.Pair;
import org.processmining.plugins.petrinet.replayresult.StepTypes;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public interface TraceAlignment extends HTMLToString {

	public void init();
	public int getSize();
    public List<Pair<StepTypes, XEventClass>> getLegalMoves();
	public void setLegalMoves(List<Pair<StepTypes, XEventClass>> legalMoves);
	public void addLegalMove(StepTypes stepType, XEventClass activity);
	public List<XEventClass> getLogMoves();
	public void exportToCSVFile(CsvWriter writer) throws IOException;
	public void importFromCSVFile(CsvReader reader, Map<String, XEventClass> activities) throws IOException;
	
}
