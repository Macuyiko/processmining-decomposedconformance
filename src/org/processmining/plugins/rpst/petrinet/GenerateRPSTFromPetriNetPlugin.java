package org.processmining.plugins.rpst.petrinet;

import java.util.Collection;

import org.processmining.connections.rpst.petrinet.GenerateRPSTFromPetriNetConnection;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.rpst.petrinet.PetriNetRPST;
import org.processmining.parameters.rpst.petrinet.GenerateRPSTFromPetriNetParameters;

/**
 * 
 * Plugin to generate the RPST from a Petri Net.
 * 
 * See: Artem Polyvyanyy, Jussi Vanhatalo, Hagen Volzer: Simplified Computation 
 * and Generalization of the Refined Process Structure Tree. WS-FM 2010: 25-41
 * 
 * @author Jorge Munoz-Gama (jmunoz)
 */
@Plugin(name = "Generate RPST from Petri net", parameterLabels = { "Accepting Petri Net", "Parameters" }, 
returnLabels = { "RPST" }, returnTypes = { PetriNetRPST.class })
public class GenerateRPSTFromPetriNetPlugin {
	
	
	@UITopiaVariant(affiliation = "Universitat Politecnica de Catalunya", author = "J.Munoz-Gama", 
			email = "jmunoz"+ (char) 0x40 + "lsi.upc.edu", website = "http://www.lsi.upc.edu/~jmunoz", 
			pack = "DecomposedConformance")
	@PluginVariant(variantLabel = "Generate RPST from Petri net, Default", requiredParameterLabels = { 0 })
	public PetriNetRPST generateDefault(PluginContext context, AcceptingPetriNet net) {
		GenerateRPSTFromPetriNetParameters parameters = 
				new GenerateRPSTFromPetriNetParameters();
		return generatePrivateConnection(context, net, parameters);
	}
	
	@PluginVariant(variantLabel = "Generate RPST from Petri net, Parameters", requiredParameterLabels = { 0, 1 })
	public PetriNetRPST generateParameters(PluginContext context, AcceptingPetriNet net,
			GenerateRPSTFromPetriNetParameters parameters) {
		return generatePrivateConnection(context, net, parameters);
	}


	private PetriNetRPST generatePrivateConnection(PluginContext context, AcceptingPetriNet net, 
			GenerateRPSTFromPetriNetParameters parameters){
		Collection<GenerateRPSTFromPetriNetConnection> connections;
		try {
			connections = context.getConnectionManager().getConnections(
					GenerateRPSTFromPetriNetConnection.class, context, net);
			for (GenerateRPSTFromPetriNetConnection connection : connections) {
				if (connection.getObjectWithRole(GenerateRPSTFromPetriNetConnection.NET)
						.equals(net)
						&& connection.getParameters().equals(parameters)) {
					return connection.getObjectWithRole(GenerateRPSTFromPetriNetConnection.RPST);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		PetriNetRPST rpst = generateRPSTPrivate(context, net, parameters);
		context.getConnectionManager().addConnection(
				new GenerateRPSTFromPetriNetConnection(net, rpst, parameters));
		return rpst;
	}


	private PetriNetRPST generateRPSTPrivate(PluginContext context, AcceptingPetriNet net,
			GenerateRPSTFromPetriNetParameters parameters) {
		//Create the RPST from the given Petri Net
		return new PetriNetRPST(net);		
	}
}
