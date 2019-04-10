package org.processmining.plugins.realtimedcc.ui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.jgraph.ProMGraphModel;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.plugins.MergeAcceptingPetriNetArrayIntoAcceptingPetriNetPlugin;
import org.processmining.plugins.kutoolbox.visualizators.GraphViewPanel;
import org.processmining.plugins.seppedccc.models.FakePluginContext;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;

public class MergedAcceptingPetrinetArrayPanel extends JPanel {
	private static final long serialVersionUID = 9070570696748020069L;
	private AcceptingPetriNet mergedNet;
	private GraphViewPanel viewPanel;
	
	public MergedAcceptingPetrinetArrayPanel(DashboardReplayListener parent) {
		this.setLayout(new BorderLayout());
	}

	public void initialize(AcceptingPetriNetArray array) {
		MergeAcceptingPetriNetArrayIntoAcceptingPetriNetPlugin pl = new MergeAcceptingPetriNetArrayIntoAcceptingPetriNetPlugin();
		mergedNet = pl.mergeDefault(new FakePluginContext(), array);
		viewPanel = new GraphViewPanel(buildJGraph(mergedNet.getNet()));
		this.remove(viewPanel);
		this.add(viewPanel, BorderLayout.CENTER);
	}

	public static ProMJGraph buildJGraph(Petrinet petrinet){
		ViewSpecificAttributeMap map = new ViewSpecificAttributeMap();
		GraphLayoutConnection layoutConnection = new GraphLayoutConnection(petrinet);
		ProMGraphModel model = new ProMGraphModel(petrinet);
		ProMJGraph jGraph = new ProMJGraph(model, map, layoutConnection);
		JGraphHierarchicalLayout layout = new JGraphHierarchicalLayout();
		layout.setDeterministic(false);
		layout.setCompactLayout(false);
		layout.setFineTuning(true);
		layout.setParallelEdgeSpacing(15);
		layout.setFixRoots(false);
		layout.setOrientation(map.get(petrinet, AttributeMap.PREF_ORIENTATION, SwingConstants.EAST));

		if(!layoutConnection.isLayedOut()){
			JGraphFacade facade = new JGraphFacade(jGraph);
			facade.setOrdered(false);
			facade.setEdgePromotion(true);
			facade.setIgnoresCellsInGroups(false);
			facade.setIgnoresHiddenCells(false);
			facade.setIgnoresUnconnectedCells(false);
			facade.setDirected(true);
			facade.resetControlPoints();
			facade.run(layout, true);
			java.util.Map<?, ?> nested = facade.createNestedMap(true, true);
			jGraph.getGraphLayoutCache().edit(nested);
			layoutConnection.setLayedOut(true);
		}	
		
		jGraph.setUpdateLayout(layout);	
		layoutConnection.updated();
		
		return jGraph;
	}

	public void updateTransitionColor(String key, Integer value, boolean correct) {
		for (Transition t : mergedNet.getNet().getTransitions()) {
			float saturation = (20F + (float)value/80F)/255F;
			if (saturation >= 1F) saturation = 1F;
			if (t.getLabel().equals(key)) {
				Color color = correct 
						? Color.getHSBColor(127F/360F, saturation, 1F)
						: Color.getHSBColor(1F, saturation, 1F);
				t.getAttributeMap().put(AttributeMap.FILLCOLOR, color);
			}
		}
		viewPanel.redraw();
	}

}
