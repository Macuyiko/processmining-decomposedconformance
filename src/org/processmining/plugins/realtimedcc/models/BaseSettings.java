package org.processmining.plugins.realtimedcc.models;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;

public class BaseSettings {

	public int port = 4441;
	public String delimiter = "\t\t\t";
	public XEventClassifier classifier = XLogInfoImpl.STANDARD_CLASSIFIER;

}
