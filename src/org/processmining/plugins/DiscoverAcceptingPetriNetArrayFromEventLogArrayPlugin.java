package org.processmining.plugins;

import java.util.Collection;
import java.util.prefs.Preferences;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.connections.DiscoverAcceptingPetriNetArrayFromEventLogArrayConnection;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.dialogs.DiscoverAcceptingPetriNetArrayFromEventLogArrayDialog;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.EventLogArray;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.impl.DivideAndConquerFactory;
import org.processmining.parameters.DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters;
import org.processmining.plugins.ilpminer.ILPMiner;
import org.processmining.plugins.ilpminer.ILPMinerSettings;
import org.processmining.plugins.ilpminer.ILPMinerSettings.SolverSetting;
import org.processmining.plugins.ilpminer.ILPMinerSettings.SolverType;
import org.processmining.plugins.ilpminer.ILPMinerUI;
import org.processmining.plugins.ilpminer.templates.javailp.EmptyAfterCompletionILPModel;

@Plugin(name = "Discover Accepting Petri Nets", parameterLabels = { "Event Log Array",
		"Parameters" }, returnLabels = { "Accepting Petri Net Array" }, returnTypes = { AcceptingPetriNetArray.class })
public class DiscoverAcceptingPetriNetArrayFromEventLogArrayPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Discover Accepting Petri Nets, UI", requiredParameterLabels = { 0 })
	public AcceptingPetriNetArray discoverUI(UIPluginContext context, EventLogArray logs) {
		DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters parameters = new DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters(
				logs);
		DiscoverAcceptingPetriNetArrayFromEventLogArrayDialog dialog = new DiscoverAcceptingPetriNetArrayFromEventLogArrayDialog(
				logs, parameters);
		InteractionResult result = context.showWizard("Configure discovery (classifier, miner)", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return discoverPrivateConnection(context, logs, parameters);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Discover Accepting Petri Nets, Default", requiredParameterLabels = { 0 })
	public AcceptingPetriNetArray discoverDefault(PluginContext context, EventLogArray logs) {
		DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters parameters = new DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters(
				logs);
		return discoverPrivateConnection(context, logs, parameters);
	}

	@PluginVariant(variantLabel = "Discover Accepting Petri Nets, Parameters", requiredParameterLabels = { 0, 1 })
	public AcceptingPetriNetArray discoverParameters(PluginContext context, EventLogArray logs, DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters parameters) {
		return discoverPrivateConnection(context, logs, parameters);
	}

	private AcceptingPetriNetArray discoverPrivateConnection(PluginContext context, EventLogArray logs,
			DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters parameters) {
		Collection<DiscoverAcceptingPetriNetArrayFromEventLogArrayConnection> connections;
		try {
			connections = context.getConnectionManager().getConnections(
					DiscoverAcceptingPetriNetArrayFromEventLogArrayConnection.class, context, logs);
			for (DiscoverAcceptingPetriNetArrayFromEventLogArrayConnection connection : connections) {
				if (connection.getObjectWithRole(DiscoverAcceptingPetriNetArrayFromEventLogArrayConnection.LOGS)
						.equals(logs) && connection.getParameters().equals(parameters)) {
					return connection.getObjectWithRole(DiscoverAcceptingPetriNetArrayFromEventLogArrayConnection.NETS);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		AcceptingPetriNetArray nets = discoverPrivate(context, logs, parameters);
		context.getConnectionManager().addConnection(
				new DiscoverAcceptingPetriNetArrayFromEventLogArrayConnection(logs, nets, parameters));
		return nets;
	}

	private AcceptingPetriNetArray discoverPrivate(PluginContext context, EventLogArray logs,
			DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters parameters) {
		AcceptingPetriNetArray nets = DivideAndConquerFactory.createAcceptingPetriNetArray();
		nets.init();
		for (int i = 0; i < logs.getSize(); i++) {
			AcceptingPetriNet acceptingNet = DivideAndConquerFactory.createAcceptingPetriNet();
			Petrinet net = PetrinetFactory.newPetrinet("Empty net");
			try {
				if (parameters.getMiner().equals(DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters.ALPHAMINER)) {
					net = invokeAlphaMiner(context, logs.getLog(i), parameters.getClassifier());
				} else if (parameters.getMiner().equals(DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters.ILPMINER)) {
					net = invokeILPMiner(context, logs.getLog(i), parameters.getClassifier());
				}
			} catch (ConnectionCannotBeObtained e) {
				e.printStackTrace();
			}
			acceptingNet.init(net);
			nets.addNet(acceptingNet);
		}
		return nets;
	}

	private Petrinet invokeAlphaMiner(PluginContext context, XLog log, XEventClassifier classifier)
			throws ConnectionCannotBeObtained {
		XLogInfo info = XLogInfoFactory.createLogInfo(log, classifier);
		return context.tryToFindOrConstructFirstNamedObject(Petrinet.class, "Alpha Miner", null, null, log, info);
	}

	private Petrinet invokeILPMiner(PluginContext context, XLog log, XEventClassifier classifier)
			throws ConnectionCannotBeObtained {
		XLogInfo info = XLogInfoFactory.createLogInfo(log, classifier);
		ILPMinerSettings settings = (new ILPMinerUI()).getSettings();
		settings.setVariant(EmptyAfterCompletionILPModel.class);
		Preferences prefs = Preferences.userNodeForPackage(ILPMiner.class);
		prefs.putInt("SolverEnum", ((SolverType) settings.getSolverSetting(SolverSetting.TYPE)).ordinal());
		prefs.put("LicenseDir", (String) settings.getSolverSetting(SolverSetting.LICENSE_DIR));
		return context.tryToFindOrConstructFirstNamedObject(Petrinet.class, "ILP Miner", null, null, log, info, settings);
	}
}
