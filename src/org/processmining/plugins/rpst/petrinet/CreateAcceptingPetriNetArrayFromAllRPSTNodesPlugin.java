package org.processmining.plugins.rpst.petrinet;

import java.util.Collection;

import org.processmining.connections.rpst.petrinet.CreateAcceptingPetriNetArrayFromAllRPSTNodesConnection;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.impl.DivideAndConquerFactory;
import org.processmining.models.rpst.petrinet.PetriNetRPST;
import org.processmining.models.rpst.petrinet.PetriNetRPSTNode;
import org.processmining.parameters.rpst.petrinet.CreateAcceptingPetriNetArrayFromAllRPSTNodesParameters;
import org.processmining.utils.rpst.petrinet.RPSTNode2PetriNet;

/**
 * Plugin to generate an array of Accepting Petri Nets, one for each RPST nodes.
 * 
 * @author Jorge Munoz-Gama (jmunoz)
 */
@Plugin(name = "Create Accepting PetriNet Array From All RPST Nodes", parameterLabels = { "RPST", "Parameters" }, 
returnLabels = { "Accepting Petri Net Array" }, returnTypes = { AcceptingPetriNetArray.class })
public class CreateAcceptingPetriNetArrayFromAllRPSTNodesPlugin {

	@UITopiaVariant(affiliation = "Universitat Politecnica de Catalunya", author = "J.Munoz-Gama", 
			email = "jmunoz"+ (char) 0x40 + "lsi.upc.edu", website = "http://www.lsi.upc.edu/~jmunoz", 
			pack = "DecomposedConformance")
	@PluginVariant(variantLabel = "Create Accepting PetriNet Array From All RPST Nodes, Default", requiredParameterLabels = { 0 })
	public AcceptingPetriNetArray createDefault(PluginContext context, PetriNetRPST rpst) {
		CreateAcceptingPetriNetArrayFromAllRPSTNodesParameters parameters = 
				new CreateAcceptingPetriNetArrayFromAllRPSTNodesParameters();
		return createConnection(context, rpst, parameters);
	}

	private AcceptingPetriNetArray createConnection(PluginContext context, PetriNetRPST rpst, 
			CreateAcceptingPetriNetArrayFromAllRPSTNodesParameters parameters){
		Collection<CreateAcceptingPetriNetArrayFromAllRPSTNodesConnection> connections;
		try {
			connections = context.getConnectionManager().getConnections(
					CreateAcceptingPetriNetArrayFromAllRPSTNodesConnection.class, context, rpst);
			for (CreateAcceptingPetriNetArrayFromAllRPSTNodesConnection connection : connections) {
				if (connection.getObjectWithRole(CreateAcceptingPetriNetArrayFromAllRPSTNodesConnection.RPST)
						.equals(rpst)
						&& connection.getParameters().equals(parameters)) {
					return connection.getObjectWithRole(CreateAcceptingPetriNetArrayFromAllRPSTNodesConnection.NETS);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		AcceptingPetriNetArray nets = createPrivate(context, rpst, parameters);
		context.getConnectionManager().addConnection(
				new CreateAcceptingPetriNetArrayFromAllRPSTNodesConnection(rpst, nets, parameters));
		return nets;
	}


	private AcceptingPetriNetArray createPrivate(PluginContext context, PetriNetRPST rpst, 
			CreateAcceptingPetriNetArrayFromAllRPSTNodesParameters parameters) {	
		AcceptingPetriNetArray nets = DivideAndConquerFactory.createAcceptingPetriNetArray();
		nets.init();
		
		for(PetriNetRPSTNode node: rpst.getNodes()){
			AcceptingPetriNet acceptingNet = RPSTNode2PetriNet.convertToAcceptingPetriNet(node, 
			rpst.getNet().getInitialMarking(), rpst.getNet().getFinalMarkings());
			nets.addNet(acceptingNet);
		}
		
		return nets;		
	}
}
