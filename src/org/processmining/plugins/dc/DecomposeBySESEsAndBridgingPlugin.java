package org.processmining.plugins.dc;

import java.util.Collection;
import java.util.List;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.connections.dc.DecomposeBySESEsAndBridgingConnection;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.dialogs.dc.DecomposeBySESEsAndBridgingDialog;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.rpst.petrinet.PetriNetRPST;
import org.processmining.models.rpst.petrinet.PetriNetRPSTNode;
import org.processmining.parameters.dc.DecomposeBySESEsAndBridgingParameters;
import org.processmining.plugins.dc.partbridge.BridgingTools;
import org.processmining.plugins.dc.partbridge.PartitioningTools;
import org.processmining.plugins.rpst.petrinet.GenerateRPSTFromPetriNetPlugin;

/**
 * Decompose a Petri Net using SESEs and Bridging.
 *  
 * @author Jorge Munoz-Gama (jmunoz)
 */
@Plugin(name = "Decompose By SESEs And Bridging", parameterLabels = { "Accepting Petri Net", "Parameters" }, 
returnLabels = { "Accepting Petri Net Array" }, returnTypes = { AcceptingPetriNetArray.class })
public class DecomposeBySESEsAndBridgingPlugin {
	
	@UITopiaVariant(affiliation = "Universitat Politecnica de Catalunya", author = "J.Munoz-Gama", 
			email = "jmunoz"+ (char) 0x40 + "lsi.upc.edu", website = "http://www.lsi.upc.edu/~jmunoz", 
			pack = "DecomposedConformance")
	@PluginVariant(variantLabel = "Decompose By SESEs (+Bridging), UI", requiredParameterLabels = { 0 })
	public AcceptingPetriNetArray decomposeUI(UIPluginContext context, AcceptingPetriNet net) {	
		
		DecomposeBySESEsAndBridgingParameters parameters = 
				new DecomposeBySESEsAndBridgingParameters();
		
		DecomposeBySESEsAndBridgingDialog dialog = 
				new DecomposeBySESEsAndBridgingDialog(net, parameters);
		InteractionResult result = context.showWizard("Configure k-Decomposition and Bridging", 
				true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return decomposePrivateConnection(context, net, parameters);
	}

	@UITopiaVariant(affiliation = "Universitat Politecnica de Catalunya", author = "J.Munoz-Gama", 
			email = "jmunoz"+ (char) 0x40 + "lsi.upc.edu", website = "http://www.lsi.upc.edu/~jmunoz", 
			pack = "DecomposedConformance")
	@PluginVariant(variantLabel = "Decompose By SESEs (+Bridging), Default", requiredParameterLabels = { 0 })
	public AcceptingPetriNetArray decomposeDefault(PluginContext context, AcceptingPetriNet net) {
		DecomposeBySESEsAndBridgingParameters parameters = 
				new DecomposeBySESEsAndBridgingParameters();
		return decomposePrivateConnection(context, net, parameters);
	}
	
	@PluginVariant(variantLabel = "Decompose By SESEs (+Bridging), Parameters", requiredParameterLabels = { 0, 1 })
	public AcceptingPetriNetArray decomposeParameters(PluginContext context, AcceptingPetriNet net,
			DecomposeBySESEsAndBridgingParameters parameters) {
		return decomposePrivateConnection(context, net, parameters);
	}
	

	private AcceptingPetriNetArray decomposePrivateConnection(PluginContext context, AcceptingPetriNet net, 
			DecomposeBySESEsAndBridgingParameters parameters){
		Collection<DecomposeBySESEsAndBridgingConnection> connections;
		try {
			connections = context.getConnectionManager().getConnections(
					DecomposeBySESEsAndBridgingConnection.class, context, net);
			for (DecomposeBySESEsAndBridgingConnection connection : connections) {
				if (connection.getObjectWithRole(DecomposeBySESEsAndBridgingConnection.NET)
						.equals(net)
						&& connection.getParameters().equals(parameters)) {
					return connection.getObjectWithRole(DecomposeBySESEsAndBridgingConnection.NETS);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		AcceptingPetriNetArray nets = decomposePrivate(context, net, parameters);
		context.getConnectionManager().addConnection(
				new DecomposeBySESEsAndBridgingConnection(net, nets, parameters));
		return nets;
	}


	private AcceptingPetriNetArray decomposePrivate(PluginContext context, AcceptingPetriNet net, 
			DecomposeBySESEsAndBridgingParameters parameters) {	
		
		//Create the RPST
		GenerateRPSTFromPetriNetPlugin generateRPSTPlugin = new GenerateRPSTFromPetriNetPlugin();
		PetriNetRPST rpst = generateRPSTPlugin.generateParameters(context, net,parameters.getRpstParams()); 
		
		//Make a K-partition over the RPST
		List<PetriNetRPSTNode> partNodes = PartitioningTools.partitioning(rpst, parameters.getMaxSize());
		
		//Bridging (create explicit separated nets for the common places among nets)
		AcceptingPetriNetArray netsBridged = BridgingTools.bridging(rpst.getNet(), partNodes);
		
		return netsBridged;
	}
}
