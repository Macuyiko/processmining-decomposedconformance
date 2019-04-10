package org.processmining.plugins.realtimedcc.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.Timer;

import org.processmining.framework.util.Pair;
import org.processmining.plugins.kutoolbox.visualizators.GraphViewPanel;
import org.processmining.plugins.realtimedcc.models.StreamedEvent;
import org.processmining.plugins.realtimedcc.replayer.AbstractReplayListener;
import org.processmining.plugins.realtimedcc.replayer.ReplayController;

public class DashboardReplayListener extends AbstractReplayListener {
	private JFrame frame;
	private ReplayerDashboardPanel panel;
	
	private Timer timer;
	public final int UPDATE_INTERVAL = 1000;
	
	private int modelUpdateCounter = 0;
	private Map<Integer, Integer> modelToFaults;
	private Map<Integer, String> modelToLastViolatedActivity;
	private Map<String, Pair<Boolean, Integer>> transitionToFaultsOrCorrect;
	
	public DashboardReplayListener(ReplayController controller) {
		super(controller);
		
		frame = new JFrame("Replayer Dashboard");
		
		this.modelToFaults = new HashMap<Integer, Integer>();
		this.transitionToFaultsOrCorrect = new HashMap<String, Pair<Boolean, Integer>>();
		this.modelToLastViolatedActivity = new HashMap<Integer, String>();
		
		panel = new ReplayerDashboardPanel(this);
		
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.setPreferredSize(new Dimension(600,400));
		frame.pack();
		frame.setVisible(true);
		
		frame.addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(WindowEvent arg0) {}

			@Override
			public void windowClosed(WindowEvent arg0) {
				getController().tearDown();
			}

			@Override
			public void windowClosing(WindowEvent arg0) {}

			@Override
			public void windowDeactivated(WindowEvent arg0) {}

			@Override
			public void windowDeiconified(WindowEvent arg0) {}

			@Override
			public void windowIconified(WindowEvent arg0) {}

			@Override
			public void windowOpened(WindowEvent arg0) {}
		});
		
		timer = new Timer(UPDATE_INTERVAL, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				panel.update();
			}
        });
		
		timer.start();
		
		this.initialize();
	}

	public void initialize() {
		panel.getMainPetriPanel().initialize(getController().getAcceptingPetriNetArray());
		panel.getTickersPanel().initialize(getController().getAcceptingPetriNetArray().getSize());
		panel.getThreadsPanel().initialize(getController().getWorkerThreads().size());
	}

	@Override
	public void notifyEventReplayed(StreamedEvent event) {
		if (!modelToFaults.containsKey(event.model))
			modelToFaults.put(event.model, 0);
		if (!transitionToFaultsOrCorrect.containsKey(event.activity))
			transitionToFaultsOrCorrect.put(event.activity, new Pair<Boolean, Integer>(true, 0));
		
		if (event.outcome == false) {
			modelToFaults.put(event.model, modelToFaults.get(event.model) + 1);
			modelToLastViolatedActivity.put(event.model, event.activity);
			if (transitionToFaultsOrCorrect.get(event.activity).getFirst() == true)
				transitionToFaultsOrCorrect.put(event.activity, new Pair<Boolean, Integer>(false, 0));
			transitionToFaultsOrCorrect.put(event.activity, new Pair<Boolean, Integer>(false, 
					transitionToFaultsOrCorrect.get(event.activity).getSecond()+1));
		} else {
			if (transitionToFaultsOrCorrect.get(event.activity).getFirst() == false)
				transitionToFaultsOrCorrect.put(event.activity, new Pair<Boolean, Integer>(true, 0));
			transitionToFaultsOrCorrect.put(event.activity, new Pair<Boolean, Integer>(true, 
					transitionToFaultsOrCorrect.get(event.activity).getSecond()+1));
		}
	}

	public void notifyShowModel(int index) {
		JFrame frame = new JFrame("Model "+index);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(new GraphViewPanel(MergedAcceptingPetrinetArrayPanel.buildJGraph(
				getController().getAcceptingPetriNetArray().getNet(index).getNet())), BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}

	public Map<Integer, Integer> getModelToFaults() {
		return modelToFaults;
	}

	public int getModelUpdateCounter() {
		return modelUpdateCounter;
	}

	public void setModelUpdateCounter(int modelUpdateCounter) {
		this.modelUpdateCounter = modelUpdateCounter;
	}

	public Map<Integer, String> getModelToLastViolatedActivity() {
		return modelToLastViolatedActivity;
	}

	public Map<String, Pair<Boolean, Integer>> getTransitionToFaultsOrCorrect() {
		return transitionToFaultsOrCorrect;
	}

	@Override
	public void notifyWaitingConnection() {
		frame.setTitle("Replayer Dashboard (waiting for connection)");
	}

	@Override
	public void notifyGotConnection() {
		frame.setTitle("Replayer Dashboard (connected)");
	}

}

