package org.processmining.plugins.realtimedcc.ui;

import java.awt.BorderLayout;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

import org.processmining.framework.util.Pair;
import org.processmining.plugins.realtimedcc.models.StreamedEvent;
import org.processmining.plugins.realtimedcc.replayer.ReplayWorkerThread;

public class ReplayerDashboardPanel extends JPanel {
	private static final long serialVersionUID = 8521838591060800254L;
	private DashboardReplayListener parent;
	
	private MergedAcceptingPetrinetArrayPanel mainPetriPanel;
	private ModelTickerListPanel tickersPanel;
	private ThreadListPanel threadsPanel;
	
	public ReplayerDashboardPanel(DashboardReplayListener modelDashboard) {
		this.parent = modelDashboard;
		this.setLayout(new BorderLayout());
		
		this.mainPetriPanel = new MergedAcceptingPetrinetArrayPanel(this.parent);
		this.tickersPanel = new ModelTickerListPanel(this.parent);
		this.threadsPanel = new ThreadListPanel(this.parent);
		
		JSplitPane mainSplit = new JSplitPane(SwingConstants.HORIZONTAL);
		JSplitPane subSplit = new JSplitPane(SwingConstants.VERTICAL);
		
		JScrollPane completeModelScroller = new JScrollPane();
		JScrollPane decomposedModelScroller = new JScrollPane();
		JScrollPane threadScroller = new JScrollPane();
		
		completeModelScroller.setViewportView(this.mainPetriPanel);
		decomposedModelScroller.setViewportView(this.tickersPanel);
		decomposedModelScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		threadScroller.setViewportView(this.threadsPanel);
		threadScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		subSplit.add(decomposedModelScroller);
		subSplit.add(threadScroller);
		mainSplit.add(completeModelScroller);
		mainSplit.add(subSplit);
		
		this.add(mainSplit);
	}

	public void update() {
		updateModelList();
		updateGlobalModel();
		updateThreadList();
	}
	
	public void updateThreadList() {
		for (int i = 0; i < parent.getController().getWorkerThreads().size(); i++) {
			ReplayWorkerThread wt = parent.getController().getWorkerThreads().get(i);
			String repr = "";
			repr += "Thread number: "+i+"\r\n";
			repr += "- Throughput: "+wt.getThroughput()+"\r\n";
			repr += "- Queue size: "+wt.getEventsToHandle().size()+"\r\n";
			repr += "- Number of models: "+wt.getModels().size()+"\r\n";
			repr += "- Number of replayers: "+wt.getReplayers().size()+"\r\n";
			repr += "- Number of replayed events: "+"\r\n";
			for (Entry<Integer, Set<StreamedEvent>> e : wt.getEventsHandled().entrySet())
				repr += "   Model "+e.getKey()+": "+e.getValue().size()+"\r\n";
			
			if (threadsPanel.getThreadPanels() != null)
				threadsPanel.getThreadPanels().get(i).updateText(repr);
		}
	}
	
	public void updateModelList() {
		for (Entry<Integer, Integer> entry : parent.getModelToFaults().entrySet()) {
			tickersPanel.getTickerPanels().get(entry.getKey()).updateTitle(
					"Model "+entry.getKey()+" (last violated activity: "+
							parent.getModelToLastViolatedActivity().get(entry.getKey())+")");
			tickersPanel.getTickerPanels().get(entry.getKey()).updateData(new float[] {entry.getValue()});

			parent.getModelToFaults().put(entry.getKey(), 0);
		}
	}
	
	public void updateGlobalModel() {
		for (Entry<String, Pair<Boolean, Integer>> entry : parent.getTransitionToFaultsOrCorrect().entrySet()) {
			if (entry.getValue().getSecond() > 0)
				mainPetriPanel.updateTransitionColor(entry.getKey(),
						entry.getValue().getSecond(), entry.getValue().getFirst());
		}
	}

	public MergedAcceptingPetrinetArrayPanel getMainPetriPanel() {
		return mainPetriPanel;
	}

	public ModelTickerListPanel getTickersPanel() {
		return tickersPanel;
	}

	public ThreadListPanel getThreadsPanel() {
		return threadsPanel;
	}

	
}