package org.processmining.parameters.dc;

import org.processmining.parameters.rpst.petrinet.GenerateRPSTFromPetriNetParameters;

public class DecomposeBySESEsAndBridgingParameters {

	private GenerateRPSTFromPetriNetParameters rpstParams;
	private int maxSize;
	
	public DecomposeBySESEsAndBridgingParameters(){		
		//DEFAULT VALUES
		this.rpstParams = new GenerateRPSTFromPetriNetParameters();
		this.maxSize = 25;
	}


	public GenerateRPSTFromPetriNetParameters getRpstParams() {
		return rpstParams;
	}


	public void setRpstParams(GenerateRPSTFromPetriNetParameters rpstParams) {
		this.rpstParams = rpstParams;
	}


	public int getMaxSize() {
		return maxSize;
	}


	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + maxSize;
		result = prime * result + ((rpstParams == null) ? 0 : rpstParams.hashCode());
		return result;
	}


	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DecomposeBySESEsAndBridgingParameters other = (DecomposeBySESEsAndBridgingParameters) obj;
		if (maxSize != other.maxSize)
			return false;
		if (rpstParams == null) {
			if (other.rpstParams != null)
				return false;
		} else if (!rpstParams.equals(other.rpstParams))
			return false;
		return true;
	}
	
	
}
