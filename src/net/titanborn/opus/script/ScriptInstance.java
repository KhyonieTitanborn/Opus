package net.titanborn.opus.script;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import coffee.khyonieheart.hyacinth.Hyacinth;
import coffee.khyonieheart.hyacinth.Logger;
import coffee.khyonieheart.hyacinth.Message;

/**
 * An instance of a running script for a player.
 */ 
public class ScriptInstance
{
	private final String[] data;
	private final Player player;
	private final Script script;

	private Map<String, Integer> storedIntegers = new HashMap<>();
	private Map<String, Integer> storedLabels = new HashMap<>();
	private Map<String, LivingEntity> storedEntities = new HashMap<>(); 
	private ArrayDeque<Integer> stack = new ArrayDeque<>();
	private World world;
	private boolean previousOperationSuccess = false;
	private int line = 0;
	private boolean isStopped = false;

	public ScriptInstance(String[] data, Player player, Script script)
	{
		this.data = data;
		this.player = player;
		this.script = script;
	}

	public String getIdentifier()
	{
		return script.getIdentifier();
	}

	public void play()
	{
		// Store player's coordinates
		storedIntegers.put("playerX", (int) player.getLocation().getX());
		storedIntegers.put("playerY", (int) player.getLocation().getY());
		storedIntegers.put("playerZ", (int) player.getLocation().getZ());

		// Setup $return
		storedIntegers.put("return", 0);

		// Thread script
		Hyacinth.getInstance().getServer().getScheduler().runTask(Hyacinth.getInstance(), () -> performScript());
	}

	public void setLine(int line)
	{
		this.line = line;
	}

	private void performScript()
	{
		Pattern variablePattern = Pattern.compile("\\B\\$(\\w+)"); // Pattern that matches variables
		Pattern stringArgPattern = Pattern.compile("\"(.+)\""); // Pattern that matches anything contained in quote literals
		Matcher variableMatcher = variablePattern.matcher("");
		Matcher quoteMatcher = stringArgPattern.matcher("");

		String instruction = "None";
		String[] args;

		// Preprocess labels so they're always available
		for (int i = 0; i < data.length; i++)
		{
			if (!data[i].startsWith("Label "))
			{
				continue;
			}

			storedLabels.put(data[i].split(" ")[1], i);
		}

		// Fetch + decode
		for ( ; line < data.length; line++)
		{
			if (isStopped)
			{
				break;
			}

			String lineFull = data[line];
			storedIntegers.put("line", line);
			if (lineFull.equals("") || lineFull.startsWith("//"))
			{
				continue;
			}

			// Trim comments
			lineFull = lineFull.replaceAll("//.+", "").trim();

			// Process variables
			variableMatcher.reset(lineFull);
			while (variableMatcher.find())
			{
				String identifier = variableMatcher.group().substring(1).trim();
				if (storedLabels.containsKey(identifier))
				{
					lineFull = variableMatcher.replaceFirst("" + storedLabels.get(identifier));
					variableMatcher.reset(lineFull);
					continue;
				}

				if (storedIntegers.containsKey(identifier))
				{
					lineFull = variableMatcher.replaceFirst("" + storedIntegers.get(identifier));
					variableMatcher.reset(lineFull);
					continue;
				}

				Logger.log("§cUnknown variable \"" + identifier + "\" referenced @ line " + line + " of script " + this.getIdentifier());
				variableMatcher.replaceFirst(identifier);
				variableMatcher.reset(lineFull);
			}

			// Process strings
			quoteMatcher.reset(lineFull);
			while (quoteMatcher.find())
			{
				// Replace quoted spaces with null character \0
				String str = quoteMatcher.group()
					.replace(' ', (char) 0)
					.replaceAll("\"", "");

				lineFull = quoteMatcher.replaceFirst(str);
				variableMatcher.reset(lineFull);
			}

			// Build args and fix quoted strings
			List<String> argBuffer = new ArrayList<>();
			boolean isFirst = true;
			for (String s : lineFull.split(" "))
			{
				if (isFirst)
				{
					instruction = s;
					isFirst = false;
					continue;
				}
				
				argBuffer.add(s.replace('\0', ' '));
			}

			args = argBuffer.toArray(new String[argBuffer.size()]);
			// Args built, execute next instruction
		}

		// Reset state
		ScriptManager.registerStop(player, this);
	}

	private void execute(Player target, String instruction, String[] args)
	{
		try {
			switch (instruction)
			{
				default -> Message.send(target, "§cUnknown instruction \"" + instruction + "\" @ line " + line + " in script ");
			}
		} catch (Exception e) {
			Message.send(target, "§cEncountered an error @ line " + line + " in script " + this.getIdentifier() + ". Please contact an administrator.");
			e.printStackTrace();
		}
	}
}
