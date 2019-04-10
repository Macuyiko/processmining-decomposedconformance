package org.processmining.plugins.ujmp;

import java.awt.BorderLayout;
import java.util.HashMap;

import org.math.plot.Plot3DPanel;
import org.math.plot.plotObjects.Base;
import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.enums.ValueType;
import org.ujmp.core.util.MathUtil;
import org.ujmp.jmathplot.AbstractJMathPlotPanel;

public class MatrixGridPanel extends AbstractJMathPlotPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4402883762404425519L;

	public MatrixGridPanel(Matrix matrix) {
		super(matrix);
	}

	public void repaintUI() {
		Matrix matrix = getMatrix();
		Matrix posMatrix = getPosMatrix(matrix);
		Matrix negMatrix = getNegMatrix(matrix);
		Plot3DPanel panel = new Plot3DPanel();
		long rows = matrix.getRowCount();
		long cols = matrix.getColumnCount();
		double[] y = MathUtil.sequenceDouble(0, rows - 1, 1);
		double[] x = MathUtil.sequenceDouble(0, cols - 1, 1);
		HashMap<String, Double> rMap = new HashMap<String, Double>();
		HashMap<String, Double> cMap = new HashMap<String, Double>();
		for (int r = 0; r < rows; r++) {
			rMap.put(matrix.getRowLabel(r), (double) r);
		}
		for (int c = 0; c < cols; c++) {
			cMap.put(matrix.getColumnLabel(c), (double) c);
		}
		panel.setAxisLabels(new String[] { "From Activity", "To Activity", "Value" });
		panel.setAxisScales(Base.STRINGS, Base.STRINGS, Base.LINEAR);
		panel.getAxis(0).setStringMap(rMap);
		panel.getAxis(1).setStringMap(cMap);
//		panel.addGridPlot("Causal Activity Matrix", x, y, matrix.toDoubleArray());
		panel.addGridPlot("Positive", x, y, posMatrix.toDoubleArray());
		panel.addGridPlot("Negative", x, y, negMatrix.toDoubleArray());
		
		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
		setPanel(panel);
		getParent().repaint();
	}
	
	private Matrix getPosMatrix(Matrix matrix) {
		Matrix m = MatrixFactory.dense(ValueType.DOUBLE, matrix.getRowCount(), matrix.getColumnCount());
		for (int c = 0; c < matrix.getColumnCount(); c++) {
			m.setColumnLabel(c, matrix.getColumnLabel(c));
		}
		for (int r = 0; r < matrix.getRowCount(); r++) {
			m.setRowLabel(r, matrix.getRowLabel(r));
			for (int c = 0; c < matrix.getColumnCount(); c++) {
				Double value = matrix.getAsDouble(r, c);
				m.setAsDouble(value > 0.0 ? value : 0.0, r, c);
			}
		}
		return m;
	}


	private Matrix getNegMatrix(Matrix matrix) {
		Matrix m = MatrixFactory.dense(ValueType.DOUBLE, matrix.getRowCount(), matrix.getColumnCount());
		for (int c = 0; c < matrix.getColumnCount(); c++) {
			m.setColumnLabel(c, matrix.getColumnLabel(c));
		}
		for (int r = 0; r < matrix.getRowCount(); r++) {
			m.setRowLabel(r, matrix.getRowLabel(r));
			for (int c = 0; c < matrix.getColumnCount(); c++) {
				Double value = matrix.getAsDouble(r, c);
				m.setAsDouble(value < 0.0 ? -value : 0.0, r, c);
			}
		}
		return m;
	}
}
