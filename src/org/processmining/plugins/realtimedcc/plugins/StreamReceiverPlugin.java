package org.processmining.plugins.realtimedcc.plugins;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.parameters.ConvertPetriNetToAcceptingPetriNetParameters;
import org.processmining.parameters.dc.DecomposeBySESEsAndBridgingParameters;
import org.processmining.plugins.ConvertPetriNetToAcceptingPetriNetPlugin;
import org.processmining.plugins.dc.DecomposeBySESEsAndBridgingPlugin;
import org.processmining.plugins.kutoolbox.utils.ImportUtils;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.realtimedcc.models.DecomposedReplayerSettings;
import org.processmining.plugins.realtimedcc.replayer.ReplayController;
import org.processmining.plugins.realtimedcc.ui.DashboardReplayListener;
import org.processmining.plugins.seppedccc.models.FakePluginContext;

@Plugin(name = "Start Realtime Decomposed Conformance Checker", 
		parameterLabels = {"Accepting Petrinet Array", "Settings"},
		returnLabels = {},
		returnTypes = {},
		userAccessible = true,
		help = "")

public class StreamReceiverPlugin {
	
	@UITopiaVariant(uiLabel = "Start Realtime Decomposed Conformance Checker",
			affiliation = "KU Leuven",
			author = "Seppe K.L.M. vanden Broucke",
			email = "seppe.vandenbroucke@kuleuven.be",
			website = "http://www.processmining.be")
	@PluginVariant(variantLabel = "Wizard settings", requiredParameterLabels = { 0 })
	public static void executePluginWizardUI(UIPluginContext context, AcceptingPetriNetArray array) {
		DecomposedReplayerSettings settings = DecomposedReplayerSettings.getUIConfiguredSettings(context);
		executePlugin(context, array, settings);
	}
	
	@UITopiaVariant(uiLabel = "Start Realtime Decomposed Conformance Checker with Defaults",
			affiliation = "KU Leuven",
			author = "Seppe K.L.M. vanden Broucke",
			email = "seppe.vandenbroucke@kuleuven.be",
			website = "http://www.processmining.be")
	@PluginVariant(variantLabel = "Default settings", requiredParameterLabels = { 0 })
	public static void executePluginDefaultUI(UIPluginContext context, AcceptingPetriNetArray array) {
		executePlugin(context, array, new DecomposedReplayerSettings());
	}
	
	@UITopiaVariant(uiLabel = "Start Realtime Decomposed Conformance Checker with Given Settings",
			affiliation = "KU Leuven",
			author = "Seppe K.L.M. vanden Broucke",
			email = "seppe.vandenbroucke@kuleuven.be",
			website = "http://www.processmining.be")
	@PluginVariant(variantLabel = "Given settings", requiredParameterLabels = { 0, 1 })
	public static void executePluginGiventUI(UIPluginContext context, AcceptingPetriNetArray array, DecomposedReplayerSettings settings) {
		executePlugin(context, array, settings);
	}
	
	@PluginVariant(variantLabel = "Start Realtime Decomposed Conformance Checker with Defaults", requiredParameterLabels = { 0 })
	public static void executePluginDefault(PluginContext context, AcceptingPetriNetArray array) {
		executePlugin(context, array, new DecomposedReplayerSettings());
	}
	
	@PluginVariant(variantLabel = "Given settings", requiredParameterLabels = { 0, 1 })
	public static void executePlugin(PluginContext context, AcceptingPetriNetArray net, DecomposedReplayerSettings settings) {
		try {
			ReplayController controller = new ReplayController(net, settings);
			DashboardReplayListener dashboard = new DashboardReplayListener(controller);
			controller.addListener(dashboard);
			new Thread(controller).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.out.println("Importing petri net...");
		Petrinet net = ImportUtils.openTPN(new File("C:\\Users\\n11093\\Documents\\Process Logs & Models\\BPM2013benchmarks\\prAm6.tpn"));
		
		System.out.println("Creating accepting petri net...");
		ConvertPetriNetToAcceptingPetriNetPlugin acceptingPetriPlugin = new ConvertPetriNetToAcceptingPetriNetPlugin();
		ConvertPetriNetToAcceptingPetriNetParameters acceptingPetriParams = new ConvertPetriNetToAcceptingPetriNetParameters(net);
		acceptingPetriParams.setInitialMarking(PetrinetUtils.getInitialMarking(net));
		final Marking fMarking = PetrinetUtils.getFinalMarking(net);
		acceptingPetriParams.setFinalMarkings(new HashSet<Marking>() {
			private static final long serialVersionUID = 2856465269506898L;
			{
				this.add(fMarking);
			}});
		AcceptingPetriNet acceptingPetriNet = acceptingPetriPlugin.convertParameters(new FakePluginContext(), net, acceptingPetriParams);
		
		System.out.println("Decomposing...");
		DecomposeBySESEsAndBridgingParameters decParams = new DecomposeBySESEsAndBridgingParameters();
		decParams.setMaxSize(300);
		DecomposeBySESEsAndBridgingPlugin sesePlugin = new DecomposeBySESEsAndBridgingPlugin();
		AcceptingPetriNetArray acceptingPetriNetArray = sesePlugin.decomposeParameters(new FakePluginContext(), acceptingPetriNet, decParams);	
		
		System.out.println("Starting monitoring replayer...");
		DecomposedReplayerSettings s = new DecomposedReplayerSettings();
		executePlugin(new FakePluginContext(), acceptingPetriNetArray, s);
	}
	

}
