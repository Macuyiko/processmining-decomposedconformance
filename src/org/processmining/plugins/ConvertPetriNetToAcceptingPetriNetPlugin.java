package org.processmining.plugins;

import java.util.Collection;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.connections.ConvertPetriNetToAcceptingPetriNetConnection;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.dialogs.ConvertPetriNetToAcceptingPetriNetDialog;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.impl.DivideAndConquerFactory;
import org.processmining.parameters.ConvertPetriNetToAcceptingPetriNetParameters;

@Plugin(name = "Create Accepting Petri Net", parameterLabels = { "Petri Net", "Parameters" }, returnLabels = { "Accepting Petri Net" }, returnTypes = { AcceptingPetriNet.class })
public class ConvertPetriNetToAcceptingPetriNetPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Create Accepting Petri Net, UI", requiredParameterLabels = { 0 })
	public AcceptingPetriNet convertUI(UIPluginContext context, Petrinet net) {
		ConvertPetriNetToAcceptingPetriNetParameters parameters = new ConvertPetriNetToAcceptingPetriNetParameters(net);
		ConvertPetriNetToAcceptingPetriNetDialog dialog = new ConvertPetriNetToAcceptingPetriNetDialog(context, net,
				parameters);
		int n = 0;
		String[] title = { "Configure creation (initial marking)", "Configure creation (final markings)" };
		InteractionResult result = InteractionResult.NEXT;
		while (result != InteractionResult.FINISHED) {
			result = context.showWizard(title[n], n == 0, n == 1, dialog.getPanel(n));
			if (result == InteractionResult.NEXT) {
				n++;
			} else if (result == InteractionResult.PREV) {
				n--;
			} else if (result != InteractionResult.FINISHED) {
				return null;
			}
		}
		return convertPrivateConnection(context, net, parameters);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Create Accepting Petri Net, Default", requiredParameterLabels = { 0 })
	public AcceptingPetriNet convertDefault(PluginContext context, Petrinet net) {
		ConvertPetriNetToAcceptingPetriNetParameters parameters = new ConvertPetriNetToAcceptingPetriNetParameters(net);
		return convertPrivateConnection(context, net, parameters);
	}

	@PluginVariant(variantLabel = "Create Accepting Petri Net, Parameters", requiredParameterLabels = { 0, 1 })
	public AcceptingPetriNet convertParameters(PluginContext context, Petrinet net,
			ConvertPetriNetToAcceptingPetriNetParameters parameters) {
		return convertPrivateConnection(context, net, parameters);
	}

	private AcceptingPetriNet convertPrivateConnection(PluginContext context, Petrinet net,
			ConvertPetriNetToAcceptingPetriNetParameters parameters) {
		Collection<ConvertPetriNetToAcceptingPetriNetConnection> connections;
		try {
			connections = context.getConnectionManager().getConnections(
					ConvertPetriNetToAcceptingPetriNetConnection.class, context, net);
			for (ConvertPetriNetToAcceptingPetriNetConnection connection : connections) {
				if (connection.getObjectWithRole(ConvertPetriNetToAcceptingPetriNetConnection.NET).equals(net)
						&& connection.getParameters().equals(parameters)) {
					return connection.getObjectWithRole(ConvertPetriNetToAcceptingPetriNetConnection.ACCEPTINGNET);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		AcceptingPetriNet acceptingNet = convertPrivate(context, net, parameters);
		context.getConnectionManager().addConnection(
				new ConvertPetriNetToAcceptingPetriNetConnection(net, acceptingNet, parameters));
		return acceptingNet;
	}

	private AcceptingPetriNet convertPrivate(PluginContext context, Petrinet net,
			ConvertPetriNetToAcceptingPetriNetParameters parameters) {
		AcceptingPetriNet acceptingNet = DivideAndConquerFactory.createAcceptingPetriNet();
		acceptingNet.init(net);
		acceptingNet.setInitialMarking(parameters.getInitialMarking());
		acceptingNet.setFinalMarkings(parameters.getFinalMarkings());
		return acceptingNet;
	}

}
