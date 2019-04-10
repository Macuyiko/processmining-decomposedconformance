package org.processmining.models.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.util.Pair;
import org.processmining.models.TraceAlignment;
import org.processmining.plugins.petrinet.replayresult.StepTypes;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class TraceAlignmentImpl implements TraceAlignment {

	/*
	 * We only support step types LMGOOD (sync move), L (log move), and MREAL
	 * (model move).
	 */
	private List<Pair<StepTypes, XEventClass>> legalMoves;

	protected TraceAlignmentImpl() {
	}

	public void init() {
		legalMoves = new ArrayList<Pair<StepTypes, XEventClass>>();
	}

	public List<Pair<StepTypes, XEventClass>> getLegalMoves() {
		return legalMoves;
	}

	public Pair<StepTypes, XEventClass> getLegalMove(int index) {
		return legalMoves.get(index);
	}

	public void setLegalMoves(List<Pair<StepTypes, XEventClass>> legalMoves) {
		this.legalMoves = legalMoves;
	}

	public void addLegalMove(StepTypes stepType, XEventClass activity) {
		legalMoves.add(new Pair<StepTypes, XEventClass>(stepType, activity));
	}

	public int getSize() {
		return legalMoves.size();
	}

	public List<XEventClass> getLogMoves() {
		List<XEventClass> trace = new ArrayList<XEventClass>();
		for (Pair<StepTypes, XEventClass> legalMove : legalMoves) {
			if (legalMove.getFirst() == StepTypes.LMGOOD || legalMove.getFirst() == StepTypes.L) {
				trace.add(legalMove.getSecond());
			}
		}
		return trace;
	}

	public String toHTMLString(boolean includeHTMLTags) {
		StringBuffer buffer = new StringBuffer();
		if (includeHTMLTags) {
			buffer.append("<html>");
			buffer.append("<h1>Trace alignment</h1>");
		}
		buffer.append("<table><tr>");
		for (int index = 0; index < legalMoves.size(); index++) {
			if (legalMoves.get(index).getFirst() == StepTypes.LMGOOD || legalMoves.get(index).getFirst() == StepTypes.L) {
				buffer.append("<td>" + legalMoves.get(index).getSecond() + "</td>");
			} else {
				buffer.append("<td>&gt;&gt;</td>");
			}
		}
		buffer.append("</tr><tr>");
		for (int index = 0; index < legalMoves.size(); index++) {
			if (legalMoves.get(index).getFirst() == StepTypes.LMGOOD
					|| legalMoves.get(index).getFirst() == StepTypes.MREAL) {
				buffer.append("<td>" + legalMoves.get(index).getSecond() + "</td>");
			} else {
				buffer.append("<td>&gt;&gt;</td>");
			}
		}
		buffer.append("</tr></table>");
		if (includeHTMLTags) {
			buffer.append("</html>");
		}
		return buffer.toString();
	}

	public void exportToCSVFile(CsvWriter writer) throws IOException {
		for (Pair<StepTypes, XEventClass> legalMove : legalMoves) {
			switch (legalMove.getFirst()) {
				case LMGOOD :
					writer.write("LMGOOD");
					break;
				case L :
					writer.write("L");
					break;
				case MREAL :
					writer.write("MREAL");
					break;
				default :
					writer.write("");
					break;
			}
			writer.write(legalMove.getSecond().getId());
		}
		writer.write("END");
		writer.endRecord();
	}

	public void importFromCSVFile(CsvReader reader, Map<String, XEventClass> activities) throws IOException {
		init();
		for (int i = 0; i + 1 < reader.getColumnCount(); i += 2) {
			String stepType = reader.get(i);
			String activityId = reader.get(i + 1);
			if (!activities.keySet().contains(activityId)) {
				activities.put(activityId, new XEventClass(activityId, activities.keySet().size()));
			}
			XEventClass activity = activities.get(activityId);
			if (stepType.equals("LMGOOD")) {
				legalMoves.add(new Pair<StepTypes, XEventClass>(StepTypes.LMGOOD, activity));
			} else if (stepType.equals("L")) {
				legalMoves.add(new Pair<StepTypes, XEventClass>(StepTypes.L, activity));
			} else if (stepType.equals("MREAL")) {
				legalMoves.add(new Pair<StepTypes, XEventClass>(StepTypes.MREAL, activity));
			} else {
				// Ignore
			}
		}
	}
}
