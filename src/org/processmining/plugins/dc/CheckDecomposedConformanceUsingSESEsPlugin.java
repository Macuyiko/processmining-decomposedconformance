package org.processmining.plugins.dc;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.nikefs2.NikeFS2VirtualFileSystem;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.ActivityClusterArray;
import org.processmining.models.EventLogArray;
import org.processmining.models.ReplayResultArray;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.ConvertPetriNetToAcceptingPetriNetPlugin;
import org.processmining.plugins.DecomposeEventLogUsingActivityClusterArrayPlugin;
import org.processmining.plugins.ExtractActivityClusterArrayFromAcceptingPetriNetArrayPlugin;
import org.processmining.plugins.ReplayEventLogArrayOnAcceptingPetriNetArrayPlugin;

/**
 * Check Conformance Decomposedly using SESEs
 *  
 * @author Jorge Munoz-Gama (jmunoz)
 */
@Plugin(name = "Check Decomposed Conformance Using SESEs", parameterLabels = { "Petri Net", "Log", "Parameters" }, 
returnLabels = { "Replay Result Array " }, returnTypes = { ReplayResultArray .class })
public class CheckDecomposedConformanceUsingSESEsPlugin {
	
	@UITopiaVariant(affiliation = "Universitat Politecnica de Catalunya", author = "J.Munoz-Gama", 
			email = "jmunoz"+ (char) 0x40 + "lsi.upc.edu", website = "http://www.lsi.upc.edu/~jmunoz", 
			pack = "DecomposedConformance")
	@PluginVariant(variantLabel = "Check Decomposed Conformance Using SESEs, UI", requiredParameterLabels = { 0, 1 })
	public ReplayResultArray  checkUI(UIPluginContext context, Petrinet pn, XLog log) {	
		
		NikeFS2VirtualFileSystem.instance().setSwapFileSize(200000000);
		
		String logAndNetName = pn.getLabel()+" with log "+XConceptExtension.instance().extractName(log);
		
		log(context, "Creating Accepting Petri net ...");
		ConvertPetriNetToAcceptingPetriNetPlugin pn2apnPlugin = new ConvertPetriNetToAcceptingPetriNetPlugin();
		AcceptingPetriNet apn = pn2apnPlugin.convertUI(context, pn);
		context.getProvidedObjectManager().createProvidedObject("Accepting Petri net for: "+logAndNetName, 
				apn, AcceptingPetriNet.class, context);
		
		log(context, "Creating subnets ...");
		DecomposeBySESEsAndBridgingPlugin sesePlugin = new DecomposeBySESEsAndBridgingPlugin();
		AcceptingPetriNetArray apna = sesePlugin.decomposeUI(context, apn);
		context.getProvidedObjectManager().createProvidedObject("SESE Accepting Petri nets for: "+logAndNetName, 
				apna, AcceptingPetriNetArray.class, context);
		
		log(context, "Creating activity clusters...");
		ExtractActivityClusterArrayFromAcceptingPetriNetArrayPlugin apna2clustersPlugin = 
				new ExtractActivityClusterArrayFromAcceptingPetriNetArrayPlugin();
		ActivityClusterArray clusters = apna2clustersPlugin.extractUI(context, apna);
		context.getProvidedObjectManager().createProvidedObject("Activity Clusters for: "+logAndNetName, 
				clusters, ActivityClusterArray.class, context);
		
		log(context, "Creating sublogs...");
		DecomposeEventLogUsingActivityClusterArrayPlugin projectlogPlugin = 
				new DecomposeEventLogUsingActivityClusterArrayPlugin();
		EventLogArray loga = projectlogPlugin.decomposeUI(context, log, clusters);
		context.getProvidedObjectManager().createProvidedObject("Logs for: "+logAndNetName, 
				loga, EventLogArray.class, context);
		
		log(context, "Starting alignments ...");
		ReplayEventLogArrayOnAcceptingPetriNetArrayPlugin replayPlugin = 
				new ReplayEventLogArrayOnAcceptingPetriNetArrayPlugin();
		ReplayResultArray alignments = replayPlugin.replayUI(context, loga, apna);
	
		log(context, "Finished");
		return alignments;
	}

	@UITopiaVariant(affiliation = "Universitat Politecnica de Catalunya", author = "J.Munoz-Gama", 
			email = "jmunoz"+ (char) 0x40 + "lsi.upc.edu", website = "http://www.lsi.upc.edu/~jmunoz", 
			pack = "DecomposedConformance")
	@PluginVariant(variantLabel = "Check Decomposed Conformance Using SESEs, Default", requiredParameterLabels = { 0, 1 })
	public ReplayResultArray checkDefault(PluginContext context, Petrinet pn, XLog log) {	

		NikeFS2VirtualFileSystem.instance().setSwapFileSize(200000000);
		
		String logAndNetName = pn.getLabel()+" with log "+XConceptExtension.instance().extractName(log);
		
		log(context, "Creating Accepting Petri net ...");
		ConvertPetriNetToAcceptingPetriNetPlugin pn2apnPlugin = new ConvertPetriNetToAcceptingPetriNetPlugin();
		AcceptingPetriNet apn = pn2apnPlugin.convertDefault(context, pn);
		context.getProvidedObjectManager().createProvidedObject("Accepting Petri net for: "+logAndNetName, 
				apn, AcceptingPetriNet.class, context);
		
		log(context, "Creating subnets ...");
		DecomposeBySESEsAndBridgingPlugin sesePlugin = new DecomposeBySESEsAndBridgingPlugin();
		AcceptingPetriNetArray apna = sesePlugin.decomposeDefault(context, apn);
		context.getProvidedObjectManager().createProvidedObject("SESE Accepting Petri nets for: "+logAndNetName, 
				apna, AcceptingPetriNetArray.class, context);
		
		log(context, "Creating activity clusters...");
		ExtractActivityClusterArrayFromAcceptingPetriNetArrayPlugin apna2clustersPlugin = 
				new ExtractActivityClusterArrayFromAcceptingPetriNetArrayPlugin();
		ActivityClusterArray clusters = apna2clustersPlugin.extractDefault(context, apna);
		context.getProvidedObjectManager().createProvidedObject("Activity Clusters for: "+logAndNetName, 
				clusters, ActivityClusterArray.class, context);
		
		log(context, "Creating sublogs...");
		DecomposeEventLogUsingActivityClusterArrayPlugin projectlogPlugin = 
				new DecomposeEventLogUsingActivityClusterArrayPlugin();
		EventLogArray loga = projectlogPlugin.decomposeDefault(context, log, clusters);
		context.getProvidedObjectManager().createProvidedObject("Logs for: "+logAndNetName, 
				loga, EventLogArray.class, context);
		
		log(context, "Starting alignments ...");
		ReplayEventLogArrayOnAcceptingPetriNetArrayPlugin replayPlugin = 
				new ReplayEventLogArrayOnAcceptingPetriNetArrayPlugin();
		ReplayResultArray alignments = replayPlugin.replayDefault(context, loga, apna);
	
		log(context, "Finished");
		return alignments;
	}
	
//	@PluginVariant(variantLabel = "Decompose By SESEs (+Bridging), Parameters", requiredParameterLabels = { 0, 1 })
//	public AcceptingPetriNetArray decomposeParameters(PluginContext context, AcceptingPetriNet net,
//			CheckDecomposedConformanceUsingSESEsParameters parameters) {
//		return decomposePrivateConnection(context, net, parameters);
//	}
//	
//
//	private AcceptingPetriNetArray decomposePrivateConnection(PluginContext context, AcceptingPetriNet net, 
//			CheckDecomposedConformanceUsingSESEsParameters parameters){
//		Collection<CheckDecomposedConformanceUsingSESEsConnection> connections;
//		try {
//			connections = context.getConnectionManager().getConnections(
//					CheckDecomposedConformanceUsingSESEsConnection.class, context, net);
//			for (CheckDecomposedConformanceUsingSESEsConnection connection : connections) {
//				if (connection.getObjectWithRole(CheckDecomposedConformanceUsingSESEsConnection.NET)
//						.equals(net)
//						&& connection.getParameters().equals(parameters)) {
//					return connection.getObjectWithRole(CheckDecomposedConformanceUsingSESEsConnection.NETS);
//				}
//			}
//		} catch (ConnectionCannotBeObtained e) {
//		}
//		AcceptingPetriNetArray nets = decomposePrivate(context, net, parameters);
//		context.getConnectionManager().addConnection(
//				new CheckDecomposedConformanceUsingSESEsConnection(net, nets, parameters));
//		return nets;
//	}
//
//
//	private AcceptingPetriNetArray decomposePrivate(PluginContext context, AcceptingPetriNet net, 
//			CheckDecomposedConformanceUsingSESEsParameters parameters) {	
//		
//		//Create the RPST
//		GenerateRPSTFromPetriNetPlugin generateRPSTPlugin = new GenerateRPSTFromPetriNetPlugin();
//		PetriNetRPST rpst = generateRPSTPlugin.generateParameters(context, net,parameters.getRpstParams()); 
//		
//		//Make a K-partition over the RPST
//		List<PetriNetRPSTNode> partNodes = PartitioningTools.partitioning(rpst, parameters.getMaxSize());
//		
//		//Bridging (create explicit separated nets for the common places among nets)
//		AcceptingPetriNetArray netsBridged = BridgingTools.bridging(rpst.getNet(), partNodes);
//		
//		return netsBridged;
//	}
	
	
	public static void log(PluginContext context, String message) {
		if (context != null && context instanceof UIPluginContext) {
			context.log(message);
		}
		System.out.println(message);
	}
	
}
