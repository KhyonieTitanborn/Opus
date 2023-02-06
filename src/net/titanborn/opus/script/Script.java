package net.titanborn.opus.script;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.bukkit.entity.Player;

import coffee.khyonieheart.hyacinth.Logger;
import coffee.khyonieheart.hyacinth.util.marker.NotNull;

public class Script 
{
	private String[] scriptActions;
	private File file;

	public Script(
		@NotNull File scriptFile
	) {
		this.file = scriptFile;
		try (Scanner scanner = new Scanner(scriptFile))
		{
			List<String> readData = new ArrayList<>();
			readData.add("// Script loaded from " + scriptFile.getName());

			while (scanner.hasNext())
			{
				readData.add(scanner.nextLine().trim());
			}

			scriptActions = readData.toArray(new String[readData.size()]);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		Logger.log("Loaded new script \"" + scriptFile.getName() + "\"");
	}

	public String getIdentifier()
	{
		return file.getName().replaceFirst("[.].*", "");
	}

	public String[] getLines()
	{
		return this.scriptActions;
	}

	public void play(
		@NotNull Player target
	) {
		ScriptInstance instance = new ScriptInstance(scriptActions, target, this);
		ScriptManager.registerPlaying(target, instance);

		// Perform script
		instance.play();
	}
}
