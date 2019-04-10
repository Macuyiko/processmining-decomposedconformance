package org.processmining.plugins;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.connections.MineCausalActivityMatrixFromAcceptingPetriNetConnection;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.dialogs.MineCausalActivityMatrixFromAcceptingPetriNetDialog;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.Pair;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.CausalActivityMatrix;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.transitionsystem.CoverabilityGraph;
import org.processmining.models.impl.DivideAndConquerFactory;
import org.processmining.models.semantics.petrinet.PetrinetSemantics;
import org.processmining.models.semantics.petrinet.impl.PetrinetSemanticsFactory;
import org.processmining.parameters.MineCausalActivityMatrixFromAcceptingPetriNetParameters;
import org.processmining.plugins.petrinet.behavioralanalysis.CGGenerator;
import org.processmining.utils.ElementaryVisibleTransitionPathFinder;

@Plugin(name = "Create Matrix", parameterLabels = { "Accepting Petri Net", "Event log", "Parameters" }, returnLabels = { "Causal Activity Matrix" }, returnTypes = { CausalActivityMatrix.class })
public class MineCausalActivityMatrixFromAcceptingPetriNetPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Create Matrix, UI", requiredParameterLabels = { 0, 1 })
	public CausalActivityMatrix mineUI(UIPluginContext context, AcceptingPetriNet net, XLog log) {
		MineCausalActivityMatrixFromAcceptingPetriNetParameters parameters = new MineCausalActivityMatrixFromAcceptingPetriNetParameters(
				net, log);
		int rank = 1;
		String[] title = { "Configure creation (classifier)", "Configure creation (transition-activity map)" };
		InteractionResult result = InteractionResult.NEXT;
		while (result != InteractionResult.FINISHED) {
			MineCausalActivityMatrixFromAcceptingPetriNetDialog dialog = new MineCausalActivityMatrixFromAcceptingPetriNetDialog(
					net, log, parameters, rank);
			result = context.showWizard(title[rank - 1], rank == 1, rank == 2, dialog);
			switch (result) {
				case NEXT :
					rank++;
					break;
				case PREV :
					rank--;
					break;
				case FINISHED :
					break;
				default :
					return null;
			}
		}
		return minePrivateConnection(context, net, parameters);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Create Matrix w/ Log, Default", requiredParameterLabels = { 0, 1 })
	public CausalActivityMatrix mineDefault(PluginContext context, AcceptingPetriNet net, XLog log) {
		MineCausalActivityMatrixFromAcceptingPetriNetParameters parameters = new MineCausalActivityMatrixFromAcceptingPetriNetParameters(
				net, log);
		return minePrivateConnection(context, net, parameters);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Create Matrix w/o Log, Default", requiredParameterLabels = { 0 })
	public CausalActivityMatrix mineDefault(PluginContext context, AcceptingPetriNet net) {
		MineCausalActivityMatrixFromAcceptingPetriNetParameters parameters = new MineCausalActivityMatrixFromAcceptingPetriNetParameters(
				net);
		return minePrivateConnection(context, net, parameters);
	}

	@PluginVariant(variantLabel = "Create Matrix, Parameters", requiredParameterLabels = { 0, 2 })
	public CausalActivityMatrix mineParameters(PluginContext context, AcceptingPetriNet net,
			MineCausalActivityMatrixFromAcceptingPetriNetParameters parameters) {
		return minePrivateConnection(context, net, parameters);
	}

	private CausalActivityMatrix minePrivateConnection(PluginContext context, AcceptingPetriNet net,
			MineCausalActivityMatrixFromAcceptingPetriNetParameters parameters) {
		Collection<MineCausalActivityMatrixFromAcceptingPetriNetConnection> connections;
		try {
			connections = context.getConnectionManager().getConnections(
					MineCausalActivityMatrixFromAcceptingPetriNetConnection.class, context, net);
			for (MineCausalActivityMatrixFromAcceptingPetriNetConnection connection : connections) {
				if (connection.getObjectWithRole(MineCausalActivityMatrixFromAcceptingPetriNetConnection.NET).equals(
						net)
						&& connection.getParameters().equals(parameters)) {
					return connection.getObjectWithRole(MineCausalActivityMatrixFromAcceptingPetriNetConnection.MATRIX);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		CausalActivityMatrix matrix = minePrivate(context, net, parameters);
		context.getConnectionManager().addConnection(
				new MineCausalActivityMatrixFromAcceptingPetriNetConnection(net, matrix, parameters));
		return matrix;
	}

	private CausalActivityMatrix minePrivate(PluginContext context, AcceptingPetriNet net,
			MineCausalActivityMatrixFromAcceptingPetriNetParameters parameters) {
		CausalActivityMatrix matrix = DivideAndConquerFactory.createCausalActivityMatrix();
		Map<String, XEventClass> eventClasses = new HashMap<String, XEventClass>();
		Set<Transition> visibleTransitions = new HashSet<Transition>();
		for (Transition transition : net.getNet().getTransitions()) {
			if (!parameters.getMapping().get(transition).equals(parameters.getInvisibleActivity())) {
				visibleTransitions.add(transition);
				if (!eventClasses.containsKey(transition.getLabel())) {
					eventClasses.put(transition.getLabel(), new XEventClass(parameters.getMapping().get(transition)
							.getId(), eventClasses.keySet().size()));
				}
			}
		}
		String label = net.getNet().getLabel();
		if (label == null || label.isEmpty()) {
			label = "<unnamed net>";
		}
		matrix.init(label, new HashSet<XEventClass>(eventClasses.values()));
		for (XEventClass rowActivity : eventClasses.values()) {
			for (XEventClass columnActivity : eventClasses.values()) {
				matrix.setValue(rowActivity, columnActivity, -1.0);
			}
		}
		ElementaryVisibleTransitionPathFinder finder = new ElementaryVisibleTransitionPathFinder(visibleTransitions);
		Collection<Pair<PetrinetNode, PetrinetNode>> onePaths = finder.find(net.getNet());
		Collection<Pair<PetrinetNode, PetrinetNode>> zeroPaths = new HashSet<Pair<PetrinetNode, PetrinetNode>>();
		if (parameters.getMinerType() == MineCausalActivityMatrixFromAcceptingPetriNetParameters.CG) {
			PetrinetSemantics semantics = PetrinetSemanticsFactory.regularPetrinetSemantics(Petrinet.class);
			semantics.initialize(net.getNet().getTransitions(), net.getInitialMarking());
			CoverabilityGraph cg = CGGenerator.getCoverabilityGraph(net.getNet(), net.getInitialMarking(), semantics);
			zeroPaths.addAll(onePaths);
			onePaths.retainAll(finder.find(cg));
			zeroPaths.removeAll(onePaths);
		}
		for (Pair<PetrinetNode, PetrinetNode> path : zeroPaths) {
			String sourceLabel = path.getFirst().getLabel();
			String targetLabel = path.getSecond().getLabel();
			matrix.setValue(eventClasses.get(sourceLabel), eventClasses.get(targetLabel), 0.0);
		}
		for (Pair<PetrinetNode, PetrinetNode> path : onePaths) {
			String sourceLabel = path.getFirst().getLabel();
			String targetLabel = path.getSecond().getLabel();
			matrix.setValue(eventClasses.get(sourceLabel), eventClasses.get(targetLabel), 1.0);
		}
		return matrix;
	}
}
