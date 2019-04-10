package org.processmining.plugins;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.connections.MineCausalActivityMatrixFromEventLogConnection;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.dialogs.MineCausalActivityMatrixFromEventLogDialog;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.Pair;
import org.processmining.models.CausalActivityMatrix;
import org.processmining.models.impl.DivideAndConquerFactory;
import org.processmining.parameters.MineCausalActivityMatrixFromEventLogParameters;
import org.processmining.plugins.log.logabstraction.BasicLogRelations;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

@Plugin(name = "Discover Matrix", parameterLabels = { "Event Log", "Parameters" }, returnLabels = { "Causal Activity Matrix" }, returnTypes = { CausalActivityMatrix.class })
public class MineCausalActivityMatrixFromEventLogPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Discover Matrix, UI", requiredParameterLabels = { 0 })
	public CausalActivityMatrix mineUI(UIPluginContext context, XLog log) {
		MineCausalActivityMatrixFromEventLogParameters parameters = new MineCausalActivityMatrixFromEventLogParameters(
				log);
		MineCausalActivityMatrixFromEventLogDialog dialog = new MineCausalActivityMatrixFromEventLogDialog(log,
				parameters);
		InteractionResult result = context.showWizard("Configure discovery (classifier, miner)", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return minePrivateConnection(context, log, parameters);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Discover Matrix, Default", requiredParameterLabels = { 0 })
	public CausalActivityMatrix mineDefault(PluginContext context, XLog log) {
		MineCausalActivityMatrixFromEventLogParameters parameters = new MineCausalActivityMatrixFromEventLogParameters(
				log);
		return minePrivateConnection(context, log, parameters);
	}

	@PluginVariant(variantLabel = "Discover Matrix, Parameters", requiredParameterLabels = { 0, 1 })
	public CausalActivityMatrix mineDefault(PluginContext context, XLog log,
			MineCausalActivityMatrixFromEventLogParameters parameters) {
		return minePrivateConnection(context, log, parameters);
	}

	private CausalActivityMatrix minePrivateConnection(PluginContext context, XLog log,
			MineCausalActivityMatrixFromEventLogParameters parameters) {
		Collection<MineCausalActivityMatrixFromEventLogConnection> connections;
		try {
			connections = context.getConnectionManager().getConnections(
					MineCausalActivityMatrixFromEventLogConnection.class, context, log);
			for (MineCausalActivityMatrixFromEventLogConnection connection : connections) {
				if (connection.getObjectWithRole(MineCausalActivityMatrixFromEventLogConnection.LOG).equals(log)
						&& connection.getParameters().equals(parameters)) {
					return connection.getObjectWithRole(MineCausalActivityMatrixFromEventLogConnection.MATRIX);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		CausalActivityMatrix matrix = minePrivate(context, log, parameters);
		context.getConnectionManager().addConnection(
				new MineCausalActivityMatrixFromEventLogConnection(log, matrix, parameters));
		return matrix;
	}

	private CausalActivityMatrix minePrivate(PluginContext context, XLog log,
			MineCausalActivityMatrixFromEventLogParameters parameters) {
		CausalActivityMatrix matrix = DivideAndConquerFactory.createCausalActivityMatrix();
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, parameters.getClassifier());
		XConceptExtension conceptExtension = XConceptExtension.instance();
		String label = conceptExtension.extractName(log);
		if (label == null || label.isEmpty()) {
			label = "<unnamed log>";
		}
		matrix.init(label, new HashSet<XEventClass>(logInfo.getEventClasses().getClasses()));
		if (parameters.getMiner().equals(MineCausalActivityMatrixFromEventLogParameters.STANDARD)) {
			minePrivateStandard(context, log, logInfo, matrix);
		} else if (parameters.getMiner().equals(MineCausalActivityMatrixFromEventLogParameters.HEURISTICS)) {
			minePrivateHeuristics(context, log, logInfo, matrix);
		} else if (parameters.getMiner().equals(MineCausalActivityMatrixFromEventLogParameters.FUZZY)) {
			minePrivateFuzzy(context, log, logInfo, matrix);
		}
		return matrix;
	}

	private void minePrivateStandard(PluginContext context, XLog log, XLogInfo logInfo, CausalActivityMatrix matrix) {
		List<XEventClass> activities = matrix.getActivities();
		int n = activities.size();
		DoubleMatrix2D countMatrix = new DenseDoubleMatrix2D(n, n);
		for (XTrace trace : log) {
			XEvent prevEvent = null;
			int r = -1, c = -1;
			for (XEvent event : trace) {
				if (prevEvent != null) {
					r = c;
					c = activities.indexOf(logInfo.getEventClasses().getClassOf(event));
					countMatrix.set(r, c, countMatrix.get(r, c) + 1);
				} else {
					c = activities.indexOf(logInfo.getEventClasses().getClassOf(event));
				}
				prevEvent = event;
			}
		}
		for (int r = 0; r < n; r++) {
			for (int c = 0; c < n; c++) {
				double fwdValue = countMatrix.get(r, c);
				double bwdValue = countMatrix.get(c, r);
				matrix.setValue(activities.get(r), activities.get(c), (fwdValue - bwdValue)
						/ (fwdValue + bwdValue + 1.0));
			}
		}
	}

	private void minePrivateHeuristics(PluginContext context, XLog log, XLogInfo logInfo, CausalActivityMatrix matrix) {
		BasicLogRelations relations = new BasicLogRelations(log, logInfo, context.getProgress());
		XEventClasses events = relations.getEventClasses();
		Map<Pair<XEventClass, XEventClass>, Integer> directFollowsDep = relations.getDirectFollowsDependencies();
		for (int r = 0; r < events.size(); r++) {
			XEventClass rowActivity = events.getByIndex(r);
			for (int c = 0; c < events.size(); c++) {
				XEventClass columnActivity = events.getByIndex(c);
				Pair<XEventClass, XEventClass> fwdPair = new Pair<XEventClass, XEventClass>(rowActivity, columnActivity);
				Pair<XEventClass, XEventClass> bwdPair = new Pair<XEventClass, XEventClass>(columnActivity, rowActivity);
				int fwdNumber = directFollowsDep.containsKey(fwdPair) ? directFollowsDep.get(fwdPair) : 0;
				int bwdNumber = directFollowsDep.containsKey(bwdPair) ? directFollowsDep.get(bwdPair) : 0;

				double value = (fwdNumber - bwdNumber) / (fwdNumber + bwdNumber + 1.0);
				value = (fwdNumber == 0) ? -1 : value;
				matrix.setValue(rowActivity, columnActivity, value);
			}
		}
	}

	private void minePrivateFuzzy(PluginContext context, XLog log, XLogInfo logInfo, CausalActivityMatrix matrix) {
		BasicLogRelations relations = new BasicLogRelations(log, logInfo, context.getProgress());
		XEventClasses events = relations.getEventClasses();
		Map<Pair<XEventClass, XEventClass>, Integer> directFollowsDep = relations.getDirectFollowsDependencies();

		int n = events.size();

		int[][] frequencySignificance = new int[n][n];
		int maxFrequencySignificance = 0;
		for (int r = 0; r < n; r++) {
			XEventClass rowActivity = events.getByIndex(r);
			for (int c = 0; c < n; c++) {
				XEventClass columnActivity = events.getByIndex(c);
				Pair<XEventClass, XEventClass> fwdPair = new Pair<XEventClass, XEventClass>(rowActivity, columnActivity);
				frequencySignificance[r][c] = directFollowsDep.containsKey(fwdPair) ? directFollowsDep.get(fwdPair) : 0;
				if (frequencySignificance[r][c] > maxFrequencySignificance) {
					maxFrequencySignificance = frequencySignificance[r][c];
				}
			}
		}

		int[] routingSignificance = new int[n];
		int maxRoutingSignificance = 0;
		for (int r = 0; r < n; r++) {
			int in = 0;
			int out = 0;
			for (int c = 0; c < n; c++) {
				if (frequencySignificance[c][r] > 0) {
					in++;
				}
				if (frequencySignificance[r][c] > 0) {
					out++;
				}
				routingSignificance[r] = (out > in ? out - in : in - out);
				if (routingSignificance[r] > maxRoutingSignificance) {
					maxRoutingSignificance = routingSignificance[r];
				}
			}
		}

		double ratio = 0.8;

		double[][] normalizedSignificance = new double[n][n];
		for (int r = 0; r < n; r++) {
			for (int c = 0; c < n; c++) {
				normalizedSignificance[r][c] = ((ratio * frequencySignificance[r][c]) / maxFrequencySignificance)
						+ ((1 - ratio) * 0.5 * (routingSignificance[r] + routingSignificance[c]))
						/ maxRoutingSignificance;
			}
		}

		double[][] relativeSignificance = new double[n][n];
		double minRelativeSignificance = 1.0;
		double maxRelativeSignificance = -1.0;
		for (int r = 0; r < n; r++) {
			for (int c = 0; c < n; c++) {
				double rNormalized = 0.0;
				double cNormalized = 0.0;
				for (int i = 0; i < n; i++) {
					rNormalized += normalizedSignificance[r][i];
					cNormalized += normalizedSignificance[i][c];
				}
				relativeSignificance[r][c] = 0.5 * ((normalizedSignificance[r][c] / rNormalized) + (normalizedSignificance[r][c] / cNormalized));
				if (relativeSignificance[r][c] < minRelativeSignificance) {
					minRelativeSignificance = relativeSignificance[r][c];
				}
				if (relativeSignificance[r][c] > maxRelativeSignificance) {
					maxRelativeSignificance = relativeSignificance[r][c];
				}
			}
		}

		for (int r = 0; r < n; r++) {
			XEventClass rowActivity = events.getByIndex(r);
			for (int c = 0; c < n; c++) {
				XEventClass columnActivity = events.getByIndex(c);
				matrix.setValue(
						rowActivity,
						columnActivity,
						(2.0 * (relativeSignificance[r][c] - minRelativeSignificance) / (maxRelativeSignificance - minRelativeSignificance)) - 1.0);
			}
		}
	}
}
