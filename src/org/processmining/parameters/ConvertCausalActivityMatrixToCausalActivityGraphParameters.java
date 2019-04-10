package org.processmining.parameters;


public class ConvertCausalActivityMatrixToCausalActivityGraphParameters {

	private double zeroValue;	
	private double concurrencyRatio;
	
	public ConvertCausalActivityMatrixToCausalActivityGraphParameters() {
		zeroValue = 0.0;
		concurrencyRatio = 0.0;
	}
	
	public void setZeroValue(double value) {
		if (-1.0 <= value && value <= 1.0) {
			zeroValue = value;
		}
	}
	
	public double getZeroValue() {
		return zeroValue;
	}
	
	public void setConcurrencyRatio(double value) {
		if (0.0 <= value && value <= 2.0) {
			concurrencyRatio = value;
		}
	}
	
	public double getConcurrencyRatio() {
		return concurrencyRatio;
	}
	
	public double correct(double fwd, double bwd) {
		double value = Math.abs(fwd - bwd) < concurrencyRatio ? -1.0 : fwd;
		if (value == zeroValue) {
			return 0.0;
		} else if (value > zeroValue) {
			return value == 1.0 ? 1.0 : (value - zeroValue)/(1.0 - zeroValue);
		} else {
			return value == -1.0 ? -1.0 : (value - zeroValue)/(1.0 + zeroValue);
		}
	}
	
	public boolean equals(Object object) {
		if (object instanceof ConvertCausalActivityMatrixToCausalActivityGraphParameters) {
			ConvertCausalActivityMatrixToCausalActivityGraphParameters parameters = (ConvertCausalActivityMatrixToCausalActivityGraphParameters) object;
			return zeroValue == parameters.zeroValue && concurrencyRatio == parameters.concurrencyRatio;
		}
		return false;
	}
}
