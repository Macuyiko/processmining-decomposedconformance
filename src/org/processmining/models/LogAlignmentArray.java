package org.processmining.models;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.HTMLToString;

public interface LogAlignmentArray extends HTMLToString {

	public void init();

	public int addAlignment(LogAlignment alignment);

	public int removeAlignment(LogAlignment alignment);

	public void addAlignment(int index, LogAlignment alignment);

	public void removeAlignment(int index);

	public LogAlignment getAlignment(int index);

	public int getSize();

	public void importFromStream(PluginContext context, InputStream input, String parent) throws Exception;

	public void exportToFile(PluginContext context, File file) throws IOException;
}
