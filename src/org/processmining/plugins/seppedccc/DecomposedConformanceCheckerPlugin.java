package org.processmining.plugins.seppedccc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import nl.tue.astar.AStarException;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.cli.CLIContext;
import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.ActivityClusterArray;
import org.processmining.models.EventLogArray;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.parameters.ConvertPetriNetToAcceptingPetriNetParameters;
import org.processmining.parameters.DecomposeEventLogUsingActivityClusterArrayParameters;
import org.processmining.parameters.dc.DecomposeBySESEsAndBridgingParameters;
import org.processmining.plugins.ConvertPetriNetToAcceptingPetriNetPlugin;
import org.processmining.plugins.DecomposeEventLogUsingActivityClusterArrayPlugin;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.dc.DecomposeBySESEsAndBridgingPlugin;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapper;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.seppedccc.models.DecomposedReplayerSettings;
import org.processmining.plugins.seppedccc.models.FakePluginContext;
import org.processmining.plugins.seppedccc.models.ReplayModelArray;

@Plugin(name = "Decomposed Conformance Checker", 
		parameterLabels = {"Petri net", "Log", "Settings"},
		returnLabels = {"Replay Array", "Settings"},
		returnTypes = {
			ReplayModelArray.class,
			DecomposedReplayerSettings.class,
		},
		userAccessible = true,
		help = "Decomposed Conformance Checking both with Arya and Seppe replayers -- without the Configuration BS")

public class DecomposedConformanceCheckerPlugin {
	
	public static final Map<String, Double> LAST_RUN_TIMINGS = new HashMap<String, Double>();
	
	/*
	 * Variant definitions
	 */
	
	@UITopiaVariant(uiLabel = "Decomposed Conformance Checker",
			affiliation = "KU Leuven",
			author = "Seppe K.L.M. vanden Broucke",
			email = "seppe.vandenbroucke@kuleuven.be",
			website = "http://www.processmining.be")
	@PluginVariant(variantLabel = "Wizard settings", requiredParameterLabels = { 0, 1 })
	public static Object[] executePluginWizardUI(UIPluginContext context, Petrinet net, XLog log) {
		DecomposedReplayerSettings settings = DecomposedReplayerSettings.getUIConfiguredSettings(context, net, log);
		ReplayModelArray replayArray = runChecker(context, net, log, settings);
		return new Object[] {replayArray, settings};
	}
	
	@UITopiaVariant(uiLabel = "Decomposed Conformance Checker with Defaults",
			affiliation = "KU Leuven",
			author = "Seppe K.L.M. vanden Broucke",
			email = "seppe.vandenbroucke@kuleuven.be",
			website = "http://www.processmining.be")
	@PluginVariant(variantLabel = "Default settings", requiredParameterLabels = { 0, 1 })
	public static Object[] executePluginDefaultUI(UIPluginContext context, Petrinet net, XLog log) {
		return executePluginDefault(context, net, log);
	}
	
	@UITopiaVariant(uiLabel = "Decomposed Conformance Checker with Given Settings",
			affiliation = "KU Leuven",
			author = "Seppe K.L.M. vanden Broucke",
			email = "seppe.vandenbroucke@kuleuven.be",
			website = "http://www.processmining.be")
	@PluginVariant(variantLabel = "Given settings", requiredParameterLabels = { 0, 1, 2 })
	public static Object[] executePluginGiventUI(UIPluginContext context, Petrinet net, XLog log, DecomposedReplayerSettings settings) {
		return executePluginGiven(context, net, log, settings);
	}
	
	@PluginVariant(variantLabel = "Default settings", requiredParameterLabels = { 0, 1 })
	public static Object[] executePluginDefault(PluginContext context, Petrinet net, XLog log) {
		DecomposedReplayerSettings settings = DecomposedReplayerSettings.getDefaultSettings(net, log);
		ReplayModelArray replayArray = runChecker(context, net, log, settings);
		return new Object[] {replayArray, settings};
	}
	
	@PluginVariant(variantLabel = "Given settings", requiredParameterLabels = { 0, 1, 2 })
	public static Object[] executePluginGiven(PluginContext context, Petrinet net, XLog log, DecomposedReplayerSettings settings) {
		ReplayModelArray replayArray = runChecker(context, net, log, settings);
		return new Object[] {replayArray, settings};
	}

	/*
	 * Decomposed Conformance Checker
	 */
	
	public static ReplayModelArray runChecker(
			PluginContext context,
			Petrinet net, XLog log,
			DecomposedReplayerSettings settings) {
		
		tick("total_runtime");
		
		if (context == null) {
			// Instantiate a context for us to use
			CLIContext uiContext = new CLIContext();
			context = new CLIPluginContext(uiContext, "Dummy Context");
		}
		
		// 1. Make an accepting Petri net
		log(context, "Creating default accepting Petri net...");
		tick("make_accepting_petri");
		ConvertPetriNetToAcceptingPetriNetPlugin acceptingPetriPlugin = new ConvertPetriNetToAcceptingPetriNetPlugin();
		ConvertPetriNetToAcceptingPetriNetParameters acceptingPetriParams = new ConvertPetriNetToAcceptingPetriNetParameters(net);
		acceptingPetriParams.setInitialMarking(PetrinetUtils.getInitialMarking(net));
		final Marking fMarking = PetrinetUtils.getFinalMarking(net);
		acceptingPetriParams.setFinalMarkings(new HashSet<Marking>() {
			private static final long serialVersionUID = 2856465269506898L;
			{
				this.add(fMarking);
			}});
		AcceptingPetriNet acceptingPetriNet = acceptingPetriPlugin.convertParameters(context, net, acceptingPetriParams);
		tock("make_accepting_petri");
		
		// 2. Create Petri net RPST + 3. decompose the Petri RPST to create an accepting Petri net array
		log(context, "Decomposing into Petri net array using SESEs...");
		tick("make_petri_array");
		DecomposeBySESEsAndBridgingParameters decParams = new DecomposeBySESEsAndBridgingParameters();
		decParams.setMaxSize(settings.getMaximumSize());
		DecomposeBySESEsAndBridgingPlugin sesePlugin = new DecomposeBySESEsAndBridgingPlugin();
		AcceptingPetriNetArray acceptingPetriNetArray = sesePlugin.decomposeParameters(context, acceptingPetriNet, decParams);
		tock("make_petri_array");
		
		// 4. Onwards to activity cluster creation
		// TODO: well, at least some real mapping is being done now, but ideally we would like to start with the
		// single log - petri mapping which was created for the initial logs and then just do the mapping for all the
		// arrays based on that one
		log(context, "Creating activity clusters...");
		tick("make_activity_clusters");
		PetrinetLogMapper[] mapper = null;
		ActivityClusterArray activityClusterArray = null;
		if (context != null && context instanceof UIPluginContext) {
			mapper = ReplayEventLogArrayOnAcceptingPetriNetArrayWithSeppePlugin.makeMappings((UIPluginContext) context, acceptingPetriNetArray, log);
		} else {
			mapper = ReplayEventLogArrayOnAcceptingPetriNetArrayWithSeppePlugin.makeMappings(acceptingPetriNetArray, log);
		}
		activityClusterArray = ActivityClusterArrayFromMappingPlugin.executePluginGiven(
				context, 
				acceptingPetriNetArray, 
				log, 
				mapper);
		tock("make_activity_clusters");
		
		// 5. Make event log array
		log(context, "Creating event log array...");
		tick("make_log_array");
		DecomposeEventLogUsingActivityClusterArrayPlugin decomposePlugin = new DecomposeEventLogUsingActivityClusterArrayPlugin();
		DecomposeEventLogUsingActivityClusterArrayParameters decomposeParams = 
				new DecomposeEventLogUsingActivityClusterArrayParameters(log);
		decomposeParams.setClassifier(settings.getMapper().getEventClassifier());
		EventLogArray eventLogArray = decomposePlugin.decomposeParameters(context, log, activityClusterArray, decomposeParams);
		tock("make_log_array");
		
		// 6. And now we can make the replays
		log(context, "Starting replay model construction...");
		tick("make_replay");
		ReplayModelArray replayModelArray = new ReplayModelArray(acceptingPetriNetArray, eventLogArray, mapper);
		if (settings.isUseArya() && settings.isUsePureArya()) {
			// Call the pure plugin -- this is used for the experiments and benchmarking
			// Don't use the normal plugin -- this would screw up the mappings
			// Yes yes this is horrible... we can remove it later.
			log(context, "!! Using pure Arya call");
			
			int size = (acceptingPetriNetArray.getSize() < eventLogArray.getSize() ? acceptingPetriNetArray.getSize() : eventLogArray.getSize());
			for (int index = 0; index < size; index++) {
				try {
					PetrinetReplayerWithILP replayer = new PetrinetReplayerWithILP(); // same as used in decomp.
					XEventClasses eClasses = XEventClasses.deriveEventClasses(
							mapper[index].getEventClassifier(), 
							eventLogArray.getLog(index));
					XEventClass dummy = new XEventClass("$\\tau$", mapper[index].getEventClasses().size());
					TransEvClassMapping tecMap = makeAryaMapping(
							acceptingPetriNetArray.getNet(index).getNet(), 
							eventLogArray.getLog(index), 
							dummy,
							mapper[index]);
					
					IPNReplayParameter parameters = new CostBasedCompleteParam(
							eClasses.getClasses(), 
							dummy, 
							acceptingPetriNetArray.getNet(index).getNet().getTransitions());
					parameters.setGUIMode(false);
					parameters.setInitialMarking(PetrinetUtils.getInitialMarking(acceptingPetriNetArray.getNet(index).getNet()));
					Set<Marking> finalMarkings = new HashSet<Marking>();
					finalMarkings.add(PetrinetUtils.getFinalMarking(acceptingPetriNetArray.getNet(index).getNet()));
					parameters.setFinalMarkings(finalMarkings.toArray(new Marking[]{}));
					
					replayer.replayLog(
							new FakePluginContext(), 
							acceptingPetriNetArray.getNet(index).getNet(),
							eventLogArray.getLog(index), 
							tecMap, 
							parameters);
				} catch (AStarException e) {
					e.printStackTrace();
				}
			}
		} else {
			replayModelArray = ReplayEventLogArrayOnAcceptingPetriNetArrayWithSeppePlugin.replay(
					acceptingPetriNetArray, eventLogArray, mapper, settings.isUseGroupedLogs(), settings.isUseMultiThreaded());
		}
		tock("make_replay");
		System.out.println(LAST_RUN_TIMINGS.get("make_replay"));
		
		// Finalization... because we all love ProM
		tick("make_prom_objects");
		String logAndNetName = net.getLabel()+" with log "+XConceptExtension.instance().extractName(log);
			
		context.getProvidedObjectManager().createProvidedObject("Accepting Petri net for: "+logAndNetName, 
					acceptingPetriNet, AcceptingPetriNet.class, context);
		context.getProvidedObjectManager().createProvidedObject("Accepting Petri net Array for: "+logAndNetName, 
					acceptingPetriNetArray, AcceptingPetriNetArray.class, context);
		context.getProvidedObjectManager().createProvidedObject("Activity Cluster Array for: "+logAndNetName, 
					activityClusterArray, ActivityClusterArray.class, context);
		context.getProvidedObjectManager().createProvidedObject("Event Log Array for: "+logAndNetName, 
					eventLogArray, EventLogArray.class, context);
		tock("make_prom_objects");
		
		tock("total_runtime");
		
		return replayModelArray;

	}
	
	public static TransEvClassMapping makeAryaMapping(Petrinet net, XLog log, XEventClass dummy, PetrinetLogMapper mapper) {
		TransEvClassMapping tecMap = new TransEvClassMapping(mapper.getEventClassifier(), dummy);
		for (Transition transition : net.getTransitions()) {
			tecMap.put(transition, dummy);
			if (mapper.transitionHasEvent(transition) && !mapper.transitionIsInvisible(transition))
					tecMap.put(transition, mapper.get(transition));
			//System.err.println(transition.getLabel()+" --> "+tecMap.get(transition));
		}
		return tecMap;
	}
	
	public static void log(PluginContext context, String message) {
		if (context != null && context instanceof UIPluginContext) {
			context.log(message);
		}
		System.out.println(message);
	}
	
	public static void tick(String label) {
		LAST_RUN_TIMINGS.put(label, (System.currentTimeMillis() / 1000D));
	}
	
	public static void tock(String label) {
		LAST_RUN_TIMINGS.put(label, (System.currentTimeMillis() / 1000D) - LAST_RUN_TIMINGS.get(label));
	}
	
	public static void clac() {
		LAST_RUN_TIMINGS.clear();
	}

}
