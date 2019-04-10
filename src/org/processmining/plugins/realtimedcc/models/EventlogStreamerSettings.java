package org.processmining.plugins.realtimedcc.models;

import org.processmining.contexts.uitopia.UIPluginContext;

public class EventlogStreamerSettings extends BaseSettings {

	public static EventlogStreamerSettings getUIConfiguredSettings(UIPluginContext context) {
		return new EventlogStreamerSettings();
	}

	public String hostname = "127.0.0.1";
	public double eventsPerSecond = 1;

}
