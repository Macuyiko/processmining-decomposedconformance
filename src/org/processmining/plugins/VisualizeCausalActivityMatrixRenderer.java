package org.processmining.plugins;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class VisualizeCausalActivityMatrixRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 402249607120906232L;

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int col) {
		Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

		if (col > 0) {

			double s = Double.valueOf((String) table.getModel().getValueAt(row, col));

			comp.setBackground(getColorValue(s));
		} else {
			comp.setBackground(Color.WHITE);
		}

		return (comp);
	}

	private Color getColorValue(double d) {
		/* int color */

		int r = 255, g = 255, b = 255;
		if (d > 0.0) {
			g = (int) ((1.0 - d) * 127.0);
			r = g;
		}
		if (d < 0.0) {
			g = (int) ((d + 1.0) * 127.0);
			b = g;
		}

		return new Color(r, g, b);
	}
}
