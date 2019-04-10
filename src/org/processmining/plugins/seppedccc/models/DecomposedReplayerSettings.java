package org.processmining.plugins.seppedccc.models;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapper;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapperPanel;

public class DecomposedReplayerSettings {
	// defaults
	final private static boolean USE_ARYA = false;
	final private static boolean USE_GROUPED = true;
	final private static boolean USE_MT = false;
	final private static int MAX_SIZE = 1000;
	final private static boolean USE_PURE_ARYA = false;
	
	// vars
	private boolean useArya = USE_ARYA;
	private boolean useGroupedLogs = USE_GROUPED;
	private boolean useMultiThreaded = USE_MT;
	private int maximumSize = MAX_SIZE;
	private PetrinetLogMapper mapper = null;
	private boolean usePureArya = USE_PURE_ARYA;
	
	private DecomposedReplayerSettings() { 
		// Don't make a default settings object using this constructur, see getDefaultSettings() below.
	}
	
	// helpers
	public static DecomposedReplayerSettings getDefaultSettings(Petrinet net, XLog log) {
		DecomposedReplayerSettings settings = new DecomposedReplayerSettings();
		PetrinetLogMapper mapper = PetrinetLogMapper.getStandardMap(log, net);
		settings.setMapper(mapper);
		return settings;
	}
	
	public static DecomposedReplayerSettings getUIConfiguredSettings(UIPluginContext context, Petrinet net, XLog log) {
		DecomposedReplayerSettings settings = new DecomposedReplayerSettings();
		
		// Make the mapper
		// TODO: Later: let this become the main mapping?
		
		PetrinetLogMapper mapper;
		PetrinetLogMapperPanel mapperPanel = new PetrinetLogMapperPanel(log, net);
		InteractionResult ir = context.showWizard("Mapping", true, true, mapperPanel);
		if (!ir.equals(InteractionResult.FINISHED)) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		mapper = mapperPanel.getMap();
		settings.setMapper(mapper);
		
		// Configure other params. Panel is constructed here on the fly but can be put in separate class
		JPanel panel = new JPanel();
		JCheckBox checkboxArya = new JCheckBox("Use Arya's replayer?", USE_ARYA);
		JCheckBox checkboxGrouped = new JCheckBox("Use grouped logs?", USE_GROUPED);
		JCheckBox checkboxMT = new JCheckBox("Use multi threading?", USE_MT);
		JTextField textFieldMaxSize = new JTextField("5");
		panel.add(checkboxArya);
		panel.add(checkboxGrouped);
		panel.add(checkboxMT);
		panel.add(textFieldMaxSize);
		InteractionResult irc = context.showWizard("Configuration", true, true, panel);
		if (!irc.equals(InteractionResult.FINISHED)) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		settings.setUseArya(checkboxArya.isSelected());
		settings.setUseGroupedLogs(checkboxGrouped.isSelected());
		settings.setUseMultiThreaded(checkboxMT.isSelected());
		settings.setMaximumSize(Integer.parseInt(textFieldMaxSize.getText()));
		return settings;
	}

	// getters and setters
	public boolean isUseArya() {
		return useArya;
	}

	public void setUseArya(boolean useArya) {
		this.useArya = useArya;
	}

	public PetrinetLogMapper getMapper() {
		return mapper;
	}

	public void setMapper(PetrinetLogMapper mapper) {
		this.mapper = mapper;
	}

	public boolean isUseMultiThreaded() {
		return useMultiThreaded;
	}

	public void setUseMultiThreaded(boolean useMultiThreaded) {
		this.useMultiThreaded = useMultiThreaded;
	}

	public boolean isUseGroupedLogs() {
		return useGroupedLogs;
	}

	public void setUseGroupedLogs(boolean useGroupedLogs) {
		this.useGroupedLogs = useGroupedLogs;
	}

	public int getMaximumSize() {
		return maximumSize;
	}

	public void setMaximumSize(int maximumSize) {
		this.maximumSize = maximumSize;
	}

	public boolean isUsePureArya() {
		return usePureArya;
	}

	public void setUsePureArya(boolean usePureArya) {
		this.usePureArya = usePureArya;
	}

}
