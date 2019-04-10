package org.processmining.plugins.seppedccc;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JTabbedPane;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.ActivityClusterArray;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.impl.DivideAndConquerFactory;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapper;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapperPanel;

@Plugin(name = "Create Activity Clusters with Mapper", 
		parameterLabels = {"Accepting Petri net Array", "Log", "Mapping"},
		returnLabels = {"Activity Cluster Array"},
		returnTypes = {
			ActivityClusterArray.class
		},
		userAccessible = true,
		help = "Creates an Activity Cluster Array using our own internal mapper.")

public class ActivityClusterArrayFromMappingPlugin {
	
	@UITopiaVariant(uiLabel = "Create Activity Clusters with Mapper",
			affiliation = "KU Leuven",
			author = "Seppe K.L.M. vanden Broucke",
			email = "seppe.vandenbroucke@kuleuven.be",
			website = "http://www.processmining.be")
	
	/*
	 * Variant definitions
	 */
	
	@PluginVariant(variantLabel = "Wizard settings", requiredParameterLabels = { 0, 1 })
	public static ActivityClusterArray executePluginWizardUI(UIPluginContext context, AcceptingPetriNetArray nets, XLog log) {
		PetrinetLogMapper[] mapper = new PetrinetLogMapper[nets.getSize()];
		PetrinetLogMapperPanel[] mapperPanel = new PetrinetLogMapperPanel[nets.getSize()];
		JTabbedPane tabbedPane = new JTabbedPane();
		for (int i = 0; i < nets.getSize(); i++) {
			String label = "Log + Net Mapping " + (i + 1);
			mapperPanel[i] = new PetrinetLogMapperPanel(log, nets.getNet(i).getNet());
			tabbedPane.add(label, mapperPanel[i]);
		}
		InteractionResult ir = context.showWizard("Mapping", true, true, tabbedPane);
		if (!ir.equals(InteractionResult.FINISHED)) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		for (int i = 0; i < nets.getSize(); i++) {
			mapper[i] = mapperPanel[i].getMap();
		}

		return executePluginGiven(context, nets, log, mapper);
	}
	
	@PluginVariant(variantLabel = "Default settings", requiredParameterLabels = { 0, 1 })
	public static ActivityClusterArray executePluginDefaultUI(UIPluginContext context, AcceptingPetriNetArray nets, XLog log) {
		return executePluginDefault(context, nets, log);
	}
	
	@PluginVariant(variantLabel = "Given settings", requiredParameterLabels = { 0, 1, 2 })
	public static ActivityClusterArray executePluginGiventUI(UIPluginContext context, AcceptingPetriNetArray nets, XLog log, PetrinetLogMapper[] mapper) {
		return executePluginGiven(context, nets, log, mapper);
	}
	
	@PluginVariant(variantLabel = "Default settings", requiredParameterLabels = { 0, 1 })
	public static ActivityClusterArray executePluginDefault(PluginContext context, AcceptingPetriNetArray nets, XLog log) {
		PetrinetLogMapper[] mapper = new PetrinetLogMapper[nets.getSize()];
		for (int i = 0; i < nets.getSize(); i++) {
			mapper[i] = new PetrinetLogMapper(XLogInfoImpl.STANDARD_CLASSIFIER, log, nets.getNet(i).getNet());
		}
		return executePluginGiven(context, nets, log, mapper);
	}
	
	@PluginVariant(variantLabel = "Given settings", requiredParameterLabels = { 0, 1, 2 })
	public static ActivityClusterArray executePluginGiven(PluginContext context, AcceptingPetriNetArray nets, XLog log, PetrinetLogMapper[] mapper) {
		return makeActivityClusterArray(nets, log, mapper);
	}

	private static ActivityClusterArray makeActivityClusterArray(AcceptingPetriNetArray nets, XLog log, PetrinetLogMapper[] mapper) {
		Set<XEventClass> allEC = new HashSet<XEventClass>();
		List<Set<XEventClass>> clusterList = new LinkedList<Set<XEventClass>>();
		
		for (int i = 0; i < nets.getSize(); i++) {
			Set<XEventClass> cluster = new HashSet<XEventClass>();
			for (Transition t : nets.getNet(i).getNet().getTransitions()) {
				if (!mapper[i].transitionIsInvisible(t)) {
					cluster.add(mapper[i].get(t));
					allEC.add(mapper[i].get(t));
				}
			}
			clusterList.add(i, cluster);
		}
		
		ActivityClusterArray clusters = DivideAndConquerFactory.createActivityClusterArray();
		clusters.init("Extracted from Accepting Petri Net Array", allEC);
		for (int i = 0; i < clusterList.size(); i++) {
			clusters.addCluster(i, clusterList.get(i));
		}
		return clusters;
	}
	

	

}
