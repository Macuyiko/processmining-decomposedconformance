package org.processmining.plugins.realtimedcc.plugins;

import java.io.File;
import java.io.IOException;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.kutoolbox.utils.ImportUtils;
import org.processmining.plugins.realtimedcc.models.EventlogStreamerSettings;
import org.processmining.plugins.realtimedcc.streamer.StreamController;
import org.processmining.plugins.realtimedcc.ui.DashboardStreamerListener;
import org.processmining.plugins.seppedccc.models.FakePluginContext;

@Plugin(name = "Stream Event Log to Realtime Decomposed Conformance Checker", 
		parameterLabels = {"Event Log", "Settings"},
		returnLabels = {},
		returnTypes = {},
		userAccessible = true,
		help = "")

public class StreamSenderPlugin {
	
	@UITopiaVariant(uiLabel = "Stream Event Log to Realtime Decomposed Conformance Checker",
			affiliation = "KU Leuven",
			author = "Seppe K.L.M. vanden Broucke",
			email = "seppe.vandenbroucke@kuleuven.be",
			website = "http://www.processmining.be")
	@PluginVariant(variantLabel = "Wizard settings", requiredParameterLabels = { 0 })
	public static void executePluginWizardUI(UIPluginContext context, XLog array) {
		EventlogStreamerSettings settings = EventlogStreamerSettings.getUIConfiguredSettings(context);
		executePlugin(context, array, settings);
	}
	
	@UITopiaVariant(uiLabel = "Stream Event Log to Realtime Decomposed Conformance Checker with Defaults",
			affiliation = "KU Leuven",
			author = "Seppe K.L.M. vanden Broucke",
			email = "seppe.vandenbroucke@kuleuven.be",
			website = "http://www.processmining.be")
	@PluginVariant(variantLabel = "Default settings", requiredParameterLabels = { 0 })
	public static void executePluginDefaultUI(UIPluginContext context, XLog array) {
		executePlugin(context, array, new EventlogStreamerSettings());
	}
	
	@UITopiaVariant(uiLabel = "Stream Event Log to Realtime Decomposed Conformance Checker with Given Settings",
			affiliation = "KU Leuven",
			author = "Seppe K.L.M. vanden Broucke",
			email = "seppe.vandenbroucke@kuleuven.be",
			website = "http://www.processmining.be")
	@PluginVariant(variantLabel = "Given settings", requiredParameterLabels = { 0, 1 })
	public static void executePluginGiventUI(UIPluginContext context, XLog array, EventlogStreamerSettings settings) {
		executePlugin(context, array, settings);
	}
	
	@PluginVariant(variantLabel = "Stream Event Log to Realtime Decomposed Conformance Checker with Defaults", requiredParameterLabels = { 0 })
	public static void executePluginDefault(PluginContext context, XLog array) {
		executePlugin(context, array, new EventlogStreamerSettings());
	}
	
	@PluginVariant(variantLabel = "Given settings", requiredParameterLabels = { 0, 1 })
	public static void executePlugin(PluginContext context, XLog net, EventlogStreamerSettings settings) {
		try {
			StreamController controller = new StreamController(net, settings);
			DashboardStreamerListener dashboard = new DashboardStreamerListener(controller);
			controller.addListener(dashboard);
			new Thread(controller).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.out.println("Importing event log...");
		XLog log = ImportUtils.openMXMLGZ(new File("C:\\Users\\n11093\\Documents\\Process Logs & Models\\BPM2013benchmarks\\prAm6.mxml.gz"));
		
		System.out.println("Starting streamer...");
		EventlogStreamerSettings s = new EventlogStreamerSettings();
		executePlugin(new FakePluginContext(), log, s);
	}
	

}
