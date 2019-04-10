package org.processmining.plugins.seppedccc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.deckfour.xes.classification.XEventClass;
import org.processmining.connections.ReplayEventLogArrayOnAcceptingPetriNetArrayConnection;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.EventLogArray;
import org.processmining.models.ReplayResultArray;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.impl.DivideAndConquerFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.parameters.ReplayEventLogArrayOnAcceptingPetriNetArrayParameters;
import org.processmining.plugins.neconformance.models.ProcessReplayModel;
import org.processmining.plugins.neconformance.models.impl.AryaPetrinetReplayModel;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.processmining.plugins.seppedccc.models.ReplayModelArray;

@Plugin(name = "Convert Replay Model Array to Replay Result Array", 
		parameterLabels = {"Event Log Array", "Accepting Petri Net Array", "Replay Model Array"},
		returnLabels = {"Replay Result Array"},
		returnTypes = {
			ReplayResultArray.class
		},
		userAccessible = true,
		help = "Converts Seppe's replay array to Arya's")

public class ConvertReplayModelArrayToAryaReplayArray {
	
	@UITopiaVariant(uiLabel = "Convert Replay Model Array to Replay Result Array",
			affiliation = "KU Leuven",
			author = "Seppe K.L.M. vanden Broucke",
			email = "seppe.vandenbroucke@kuleuven.be",
			website = "http://www.processmining.be")

	@PluginVariant(variantLabel = "Default settings", requiredParameterLabels = { 0, 1, 2 })
	public static ReplayResultArray executePluginDefault(PluginContext context, 
			EventLogArray logs, AcceptingPetriNetArray nets, 
			ReplayModelArray replayModelArray) {
		ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters = 
				new ReplayEventLogArrayOnAcceptingPetriNetArrayParameters(logs, nets);
		ReplayResultArray replays = makeReplayResultArray(replayModelArray);
		context.getConnectionManager().addConnection(
					new ReplayEventLogArrayOnAcceptingPetriNetArrayConnection(nets, logs, replays, parameters));	
		return replays;
	}

	private static ReplayResultArray makeReplayResultArray(ReplayModelArray replayModelArray) {
		ReplayResultArray replays = DivideAndConquerFactory.createReplayResultArray();
		replays.init();
		for (List<ProcessReplayModel<Transition, XEventClass, Marking>> set : replayModelArray) {
			Set<SyncReplayResult> col = new HashSet<SyncReplayResult>();
			for (ProcessReplayModel<Transition, XEventClass, Marking> re : set) {
				if (!(re instanceof AryaPetrinetReplayModel))
					return null; // bail out!
				PNRepResult arm = ((AryaPetrinetReplayModel)re).getAryaReplayResult();
				for (SyncReplayResult srr : arm) {
					col.add(srr);
				}
			}
			PNRepResult replay = new PNRepResult(col);
			replays.addReplay(replay);
		}
		return replays;
	}
	

	

}
