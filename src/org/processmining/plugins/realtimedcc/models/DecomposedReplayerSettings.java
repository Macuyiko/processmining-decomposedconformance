package org.processmining.plugins.realtimedcc.models;

import org.processmining.contexts.uitopia.UIPluginContext;

public class DecomposedReplayerSettings extends BaseSettings {

	public int numberOfThreads = 3;
	
	public static DecomposedReplayerSettings getUIConfiguredSettings(UIPluginContext context) {
		return new DecomposedReplayerSettings();
	}

}
