package org.processmining.parameters.rpst.petrinet;


public class CreateAcceptingPetriNetArrayFromAllRPSTNodesParameters {
	
	public boolean equals(Object object) {
		if (object == null)
            return false;
        if (object == this)
            return true;
		if (object instanceof CreateAcceptingPetriNetArrayFromAllRPSTNodesParameters) {
			return true;
		}
		return false;
	}
	
	public int hashCode() {
		int result = 17;
		result = 37*result;
		return result;
	}

}
