package org.processmining.plugins;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.widgets.ProMTable;
import org.processmining.models.CausalActivityMatrix;
import org.processmining.plugins.ujmp.MatrixGridPanel;
import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.enums.ValueType;
import org.ujmp.jfreechart.AbstractChartPanel;
import org.ujmp.jfreechart.MatrixChartPanel;
import org.ujmp.jmathplot.AbstractJMathPlotPanel;
import org.ujmp.jmathplot.JMathPlotBar3DPanel;
import org.ujmp.jmathplot.JMathPlotBarPanel;
import org.ujmp.jmathplot.JMathPlotGridPanel;
import org.ujmp.jmathplot.JMathPlotHistogramPanel;
import org.ujmp.jmathplot.JMathPlotLine3DPanel;
import org.ujmp.jmathplot.JMathPlotLinePanel;
import org.ujmp.jmathplot.JMathPlotScatter3DPanel;
import org.ujmp.jmathplot.JMathPlotScatterPanel;
import org.ujmp.jmathplot.JMathPlotStaircasePanel;
import org.ujmp.jmathplot.JMathPlotXYPanel;

public class VisualizeCausalActivityMatrixPlugin {

	@Plugin(name = "Visualize Causal Activity Matrix using Table", returnLabels = { "Visualized Causal Activity Matrix" }, returnTypes = { JComponent.class }, parameterLabels = { "Causal Activity Matrix" }, userAccessible = false)
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(UIPluginContext context, CausalActivityMatrix matrix) {
		int n = matrix.getActivities().size();
		TableModel model = new DefaultTableModel(n + 1, n + 1);
		for (int c = 0; c < n; c++) {
			model.setValueAt(matrix.getActivities().get(c).getId(), 0, c + 1);
		}
		for (int r = 0; r < n; r++) {
			model.setValueAt(matrix.getActivities().get(r).getId(), r + 1, 0);
			for (int c = 0; c < n; c++) {
				model.setValueAt(matrix.getValue(matrix.getActivities().get(r), matrix.getActivities().get(c)), r + 1, c + 1);
			}
		}
		ProMTable table = new ProMTable(model);
		table.setColumnSelectionAllowed(true);
		return table;
	}
	
//	@Plugin(name = "Visualize Causal Activity Matrix using 3D Bars", returnLabels = { "Visualized Causal Activity Matrix" }, returnTypes = { JComponent.class }, parameterLabels = { "Causal Activity Matrix" }, userAccessible = false)
//	@Visualizer
//	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualizeAsBar3D(UIPluginContext context, CausalActivityMatrix matrix) {
		return embed(new JMathPlotBar3DPanel(getMatrix(matrix)));
	}
	 
//	@Plugin(name = "Visualize Causal Activity Matrix using Bars", returnLabels = { "Visualized Causal Activity Matrix" }, returnTypes = { JComponent.class }, parameterLabels = { "Causal Activity Matrix" }, userAccessible = false)
//	@Visualizer
//	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualizeAsBar(UIPluginContext context, CausalActivityMatrix matrix) {
		return embed(new JMathPlotBarPanel(getMatrix(matrix)));
	}
	 
//	@Plugin(name = "Visualize Causal Activity Matrix using Grid", returnLabels = { "Visualized Causal Activity Matrix" }, returnTypes = { JComponent.class }, parameterLabels = { "Causal Activity Matrix" }, userAccessible = false)
//	@Visualizer
//	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualizeAsGrid(UIPluginContext context, CausalActivityMatrix matrix) {
		return embed(new JMathPlotGridPanel(getMatrix(matrix)));
	}
	 
//	@Plugin(name = "Visualize Causal Activity Matrix using Histogram", returnLabels = { "Visualized Causal Activity Matrix" }, returnTypes = { JComponent.class }, parameterLabels = { "Causal Activity Matrix" }, userAccessible = false)
//	@Visualizer
//	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualizeAsHistogram(UIPluginContext context, CausalActivityMatrix matrix) {
		return embed(new JMathPlotHistogramPanel(getMatrix(matrix)));
	}
	 
//	@Plugin(name = "Visualize Causal Activity Matrix using 3D Line", returnLabels = { "Visualized Causal Activity Matrix" }, returnTypes = { JComponent.class }, parameterLabels = { "Causal Activity Matrix" }, userAccessible = false)
//	@Visualizer
//	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualizeAsLine3D(UIPluginContext context, CausalActivityMatrix matrix) {
		return embed(new JMathPlotLine3DPanel(getMatrix(matrix)));
	}
	 
//	@Plugin(name = "Visualize Causal Activity Matrix using Line", returnLabels = { "Visualized Causal Activity Matrix" }, returnTypes = { JComponent.class }, parameterLabels = { "Causal Activity Matrix" }, userAccessible = false)
//	@Visualizer
//	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualizeAsLine(UIPluginContext context, CausalActivityMatrix matrix) {
		return embed(new JMathPlotLinePanel(getMatrix(matrix)));
	}
	 
//	@Plugin(name = "Visualize Causal Activity Matrix using 3D Scatter", returnLabels = { "Visualized Causal Activity Matrix" }, returnTypes = { JComponent.class }, parameterLabels = { "Causal Activity Matrix" }, userAccessible = false)
//	@Visualizer
//	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualizeAsScatter3D(UIPluginContext context, CausalActivityMatrix matrix) {
		return embed(new JMathPlotScatter3DPanel(getMatrix(matrix)));
	}
	 
//	@Plugin(name = "Visualize Causal Activity Matrix using Scatter", returnLabels = { "Visualized Causal Activity Matrix" }, returnTypes = { JComponent.class }, parameterLabels = { "Causal Activity Matrix" }, userAccessible = false)
//	@Visualizer
//	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualizeAsScatter(UIPluginContext context, CausalActivityMatrix matrix) {
		return embed(new JMathPlotScatterPanel(getMatrix(matrix)));
	}
	 
//	@Plugin(name = "Visualize Causal Activity Matrix using Staircase", returnLabels = { "Visualized Causal Activity Matrix" }, returnTypes = { JComponent.class }, parameterLabels = { "Causal Activity Matrix" }, userAccessible = false)
//	@Visualizer
//	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualizeAsStaircase(UIPluginContext context, CausalActivityMatrix matrix) {
		return embed(new JMathPlotStaircasePanel(getMatrix(matrix)));
	}
	 
//	@Plugin(name = "Visualize Causal Activity Matrix using XY", returnLabels = { "Visualized Causal Activity Matrix" }, returnTypes = { JComponent.class }, parameterLabels = { "Causal Activity Matrix" }, userAccessible = false)
//	@Visualizer
//	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualizeAsXY(UIPluginContext context, CausalActivityMatrix matrix) {
		return embed(new JMathPlotXYPanel(getMatrix(matrix)));
	}
	 
	@Plugin(name = "Visualize Causal Activity Matrix using Grid", returnLabels = { "Visualized Causal Activity Matrix" }, returnTypes = { JComponent.class }, parameterLabels = { "Causal Activity Matrix" }, userAccessible = false)
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualizeAsA(UIPluginContext context, CausalActivityMatrix matrix) {
		return embed(new MatrixGridPanel(getMatrix(matrix)));
	}
	 
	private Matrix getMatrix(CausalActivityMatrix matrix) {
		int n = matrix.getActivities().size();
		Matrix m = MatrixFactory.dense(ValueType.DOUBLE, n, n);
		for (int c = 0; c < n; c++) {
			m.setColumnLabel(c, matrix.getActivities().get(c).getId());
		}
		for (int r = 0; r < n; r++) {
			m.setRowLabel(r, matrix.getActivities().get(r).getId());
			for (int c = 0; c < n; c++) {
				Double value = matrix.getValue(matrix.getActivities().get(c), matrix.getActivities().get(r));
				m.setAsDouble(value, r, c);
			}
		}
		return m;
	}

	private JPanel embed(AbstractJMathPlotPanel panel) {
		JPanel p = new JPanel();
		p.add(panel);
		panel.repaintUI();
		return panel;
	}

//	@Plugin(name = "Visualize Causal Activity Matrix using Chart", returnLabels = { "Visualized Causal Activity Matrix" }, returnTypes = { JComponent.class }, parameterLabels = { "Causal Activity Matrix" }, userAccessible = false)
//	@Visualizer
//	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualizeAsChart(UIPluginContext context, CausalActivityMatrix matrix) {
		AbstractChartPanel panel = new MatrixChartPanel(getMatrix(matrix));
		return panel;
	}
	 
	@Plugin(name = "Visualize Causal Activity Matrix using Colored Table", returnLabels = { "Visualized Causal Activity Matrix" }, returnTypes = { JComponent.class }, parameterLabels = { "Causal Activity Matrix" }, userAccessible = false)
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualizeAsXixi(UIPluginContext context, CausalActivityMatrix matrix) {
		final JTable table = new JTable();
		int n = matrix.getActivities().size();
		String[] columnNames = new String[n + 1];
		String[][] rows = new String[n][n + 1];
		for (int r = 0; r < n; r++) {
			XEventClass rowActivity = matrix.getActivities().get(r);
			rows[r][0] = rowActivity.getId();

			for (int c = 0; c < n; c++) {
				XEventClass columnActivity = matrix.getActivities().get(c);
				if (r == 0) {
					columnNames[c + 1] = columnActivity.getId();
				}

				double v = matrix.getValue(rowActivity, columnActivity);
				rows[r][c + 1] = String.valueOf(v);
			}
		}
		columnNames[0] = "Matrix";

		TableModel model = new DefaultTableModel(rows, columnNames);
		table.setModel(model);
		table.setDefaultRenderer(Object.class, new VisualizeCausalActivityMatrixRenderer());
		table.setOpaque(false);
		table.setEnabled(false);
		
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		scrollPane.setOpaque(false);
		return scrollPane;
	}
	 
}
