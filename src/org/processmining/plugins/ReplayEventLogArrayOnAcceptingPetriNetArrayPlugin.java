package org.processmining.plugins;

import java.util.Collection;

import nl.tue.astar.AStarException;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.connections.ReplayEventLogArrayOnAcceptingPetriNetArrayConnection;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.dialogs.ReplayEventLogArrayOnAcceptingPetriNetArrayDialog;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.EventLogArray;
import org.processmining.models.ReplayResultArray;
import org.processmining.models.impl.DivideAndConquerFactory;
import org.processmining.parameters.ReplayEventLogArrayOnAcceptingPetriNetArrayParameters;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

@Plugin(name = "Replay Event Logs", parameterLabels = { "Event Log Array", "Accepting Petri net Array", "Parameters" }, returnLabels = { "Replay Result Array" }, returnTypes = { ReplayResultArray.class })
public class ReplayEventLogArrayOnAcceptingPetriNetArrayPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Replay Event Logs, UI", requiredParameterLabels = { 0, 1 })
	public ReplayResultArray replayUI(UIPluginContext context, EventLogArray logs, AcceptingPetriNetArray nets) {
		ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters = new ReplayEventLogArrayOnAcceptingPetriNetArrayParameters(
				logs, nets);
		ReplayEventLogArrayOnAcceptingPetriNetArrayDialog dialog = new ReplayEventLogArrayOnAcceptingPetriNetArrayDialog(
				logs, nets, parameters);
		int n = 0;
		String[] title = { "Configure replay (classifier)", "Configure replay (transition-activity map)" };
		InteractionResult result = InteractionResult.NEXT;
		while (result != InteractionResult.FINISHED) {
			result = context.showWizard(title[n], n == 0, n == 1, dialog.getPanel(n));
			if (result == InteractionResult.NEXT) {
				n++;
			} else if (result == InteractionResult.PREV) {
				n--;
			} else if (result == InteractionResult.FINISHED) {
				dialog.finish();
			} else {
				return null;
			}
		}
		return replayPrivateConnection(context, logs, nets, parameters);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Replay Event Logs, Default", requiredParameterLabels = { 0, 1 })
	public ReplayResultArray replayDefault(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets) {
		ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters = new ReplayEventLogArrayOnAcceptingPetriNetArrayParameters(
				logs, nets);
		return replayPrivateConnection(context, logs, nets, parameters);
	}

	@PluginVariant(variantLabel = "Replay Event Logs, Parameters", requiredParameterLabels = { 0, 1, 2 })
	public ReplayResultArray replayParameters(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters) {
		return replayPrivateConnection(context, logs, nets, parameters);
	}

	private ReplayResultArray replayPrivateConnection(PluginContext context, EventLogArray logs,
			AcceptingPetriNetArray nets, ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters) {
		Collection<ReplayEventLogArrayOnAcceptingPetriNetArrayConnection> connections;
		try {
			connections = context.getConnectionManager().getConnections(
					ReplayEventLogArrayOnAcceptingPetriNetArrayConnection.class, context, logs, nets);
			for (ReplayEventLogArrayOnAcceptingPetriNetArrayConnection connection : connections) {
				if (connection.getObjectWithRole(ReplayEventLogArrayOnAcceptingPetriNetArrayConnection.LOGS).equals(
						logs)
						&& connection.getObjectWithRole(ReplayEventLogArrayOnAcceptingPetriNetArrayConnection.NETS)
								.equals(nets) && connection.getParameters().equals(parameters)) {
					return connection.getObjectWithRole(ReplayEventLogArrayOnAcceptingPetriNetArrayConnection.REPLAYS);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		ReplayResultArray replays = replayPrivate(context, logs, nets, parameters);
		context.getConnectionManager().addConnection(
				new ReplayEventLogArrayOnAcceptingPetriNetArrayConnection(nets, logs, replays, parameters));
		return replays;
	}

	private ReplayResultArray replayPrivate(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters) {
		ReplayResultArray replays = DivideAndConquerFactory.createReplayResultArray();

		replays.init();
		int size = (nets.getSize() < logs.getSize() ? nets.getSize() : logs.getSize());
		for (int index = 0; index < size; index++) {
			PNRepResult replay = null;
			try {
				replay = parameters.getReplayer(index).replayLog(context, nets.getNet(index).getNet(),
						logs.getLog(index), parameters.getMapping(index), parameters.getReplayParameters(index));
			} catch (AStarException e) {
				e.printStackTrace();
			}
			replays.addReplay(replay);
		}
		return replays;
	}
}
