package org.processmining.plugins;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.processmining.connections.ConvertCausalActivityMatrixToCausalActivityGraphConnection;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.dialogs.ConvertCausalActivityMatrixToCausalActivityGraphDialog;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.ActivityClusterArray;
import org.processmining.models.CausalActivityGraph;
import org.processmining.models.CausalActivityMatrix;
import org.processmining.models.impl.DivideAndConquerFactory;
import org.processmining.parameters.ConvertCausalActivityMatrixToCausalActivityGraphParameters;

@Plugin(name = "Filter Graph", parameterLabels = { "Causal Activity Matrix", "Parameters" }, returnLabels = { "Causal Activity Graph" }, returnTypes = { CausalActivityGraph.class })
public class ConvertCausalActivityMatrixToCausalActivityGraphPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Filter Graph, UI", requiredParameterLabels = { 0 })
	public CausalActivityGraph convertUI(UIPluginContext context, CausalActivityMatrix matrix) {
//		ConvertCausalActivityMatrixToCausalActivityGraphParameters parameters = new ConvertCausalActivityMatrixToCausalActivityGraphParameters();
		ConvertCausalActivityMatrixToCausalActivityGraphParameters parameters = optParameters(context, matrix, 100, 100);
		ConvertCausalActivityMatrixToCausalActivityGraphDialog dialog = new ConvertCausalActivityMatrixToCausalActivityGraphDialog(
				context, matrix, parameters);
		InteractionResult result = context.showWizard("Configure filter (thresholds)", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return convertPrivateConnection(context, matrix, parameters);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Filter Graph, Default", requiredParameterLabels = { 0 })
	public CausalActivityGraph convertDefault(PluginContext context, CausalActivityMatrix matrix) {
		ConvertCausalActivityMatrixToCausalActivityGraphParameters parameters = new ConvertCausalActivityMatrixToCausalActivityGraphParameters();
		return convertPrivateConnection(context, matrix, parameters);
	}

	@PluginVariant(variantLabel = "Filter Graph, Parameters", requiredParameterLabels = { 0 })
	public CausalActivityGraph convertParameters(PluginContext context, CausalActivityMatrix matrix,
			ConvertCausalActivityMatrixToCausalActivityGraphParameters parameters) {
		return convertPrivateConnection(context, matrix, parameters);
	}

	private CausalActivityGraph convertPrivateConnection(PluginContext context, CausalActivityMatrix matrix,
			ConvertCausalActivityMatrixToCausalActivityGraphParameters parameters) {
		Collection<ConvertCausalActivityMatrixToCausalActivityGraphConnection> connections;
		try {
			connections = context.getConnectionManager().getConnections(
					ConvertCausalActivityMatrixToCausalActivityGraphConnection.class, context, matrix);
			for (ConvertCausalActivityMatrixToCausalActivityGraphConnection connection : connections) {
				if (connection.getObjectWithRole(ConvertCausalActivityMatrixToCausalActivityGraphConnection.MATRIX)
						.equals(matrix) && connection.getParameters().equals(parameters)) {
					return connection
							.getObjectWithRole(ConvertCausalActivityMatrixToCausalActivityGraphConnection.GRAPH);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		CausalActivityGraph graph = convertPrivate(context, matrix, parameters);
		context.getConnectionManager().addConnection(
				new ConvertCausalActivityMatrixToCausalActivityGraphConnection(matrix, graph, parameters));
		return graph;
	}

	private CausalActivityGraph convertPrivate(PluginContext context, CausalActivityMatrix matrix,
			ConvertCausalActivityMatrixToCausalActivityGraphParameters parameters) {
		CausalActivityGraph graph = DivideAndConquerFactory.createCausalActivityGraph();
		List<XEventClass> activities = matrix.getActivities();
		graph.init(matrix.getLabel(), new HashSet<XEventClass>(activities));
		for (XEventClass rowActivity : activities) {
			for (XEventClass columnActivity : activities) {
				double correctedValue = parameters.correct(matrix.getValue(rowActivity, columnActivity),
						matrix.getValue(columnActivity, rowActivity));
				if (correctedValue > 0.0) {
					graph.setCausality(rowActivity, columnActivity);
				}
			}
		}
		return graph;
	}

	public ConvertCausalActivityMatrixToCausalActivityGraphParameters optParameters(PluginContext context,
			CausalActivityMatrix matrix, int nofZeroSteps, int nofConcSteps) {
		ConvertCausalActivityMatrixToCausalActivityGraphParameters parameters = new ConvertCausalActivityMatrixToCausalActivityGraphParameters();
		ConvertCausalActivityGraphToActivityClusterArrayPlugin converter = new ConvertCausalActivityGraphToActivityClusterArrayPlugin();
		double zeroValue = 0.0;
		double concurrencyRatio = 0.0;
		int bestValue = 0;
		int value;
		for (int z = 0; z < nofZeroSteps; z++) {
			parameters.setZeroValue(-1.0 + (2.0 * z) / nofZeroSteps);
			for (int c = 0; c < nofConcSteps; c++) {
				parameters.setConcurrencyRatio((2.0 * c) / nofConcSteps);
				CausalActivityGraph graph = convertPrivate(context, matrix, parameters);
				ActivityClusterArray clusters = converter.convert(context, graph);
				value = 1;
				for (Set<XEventClass> cluster : clusters.getClusters()) {
					value *= cluster.size();
				}
//				System.out.print(value + " ");
				if (value > bestValue) {
					bestValue = value;
					zeroValue = -1.0 + (2.0 * z) / nofZeroSteps;
					concurrencyRatio = (2.0 * c) / nofConcSteps;
				}
			}
//			System.out.println();
		}
		parameters.setZeroValue(zeroValue);
		parameters.setConcurrencyRatio(concurrencyRatio);
		return parameters;
	}
}
