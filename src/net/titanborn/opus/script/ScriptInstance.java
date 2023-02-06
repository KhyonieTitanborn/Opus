package net.titanborn.opus.script;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import coffee.khyonieheart.hyacinth.Hyacinth;
import coffee.khyonieheart.hyacinth.Logger;
import coffee.khyonieheart.hyacinth.Message;
import coffee.khyonieheart.hyacinth.util.marker.NotEmpty;
import coffee.khyonieheart.hyacinth.util.marker.NotNull;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

/**
 * An instance of a running script for a player. Interperets a script as a series of instructions and arguments.
 */ 
public class ScriptInstance
{
	private final String[] data;
	private final Player player;
	private final Script script;

	private Map<String, Integer> storedIntegers = new HashMap<>();
	private Map<String, Integer> storedLabels = new HashMap<>();
	private Map<String, LivingEntity> storedEntities = new HashMap<>(); 
	private Deque<Integer> stack = new ArrayDeque<>();
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
		Hyacinth.getScheduler().runTask(Hyacinth.getInstance(), () -> performScript());
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
			if (isStopped || line < 0 || line >= data.length)
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
			execute(player, instruction, args);
		}

		// Reset state
		ScriptManager.registerStop(player, this);
	}

	private void execute(Player target, String instruction, String[] args)
	{
		try {
			switch (instruction)
			{
				case "Jump" -> {	
					this.line = Integer.parseInt(args[0]) - 1;
				}
				case "JumpTrue" -> {
					if (previousOperationSuccess)
					{
						this.line = Integer.parseInt(args[0]) - 1;
					}
				}
				case "JumpNot" -> {
					if (!previousOperationSuccess)
					{
						this.line = Integer.parseInt(args[0]) - 1;
					}
				}
				case "JumpEqual" -> {
					if (Integer.parseInt(args[0]) == Integer.parseInt(args[1]))
					{
						this.line = Integer.parseInt(args[2]) - 1;
					}
				}
				case "JumpNotEqual" -> {
					if (Integer.parseInt(args[0]) != Integer.parseInt(args [1]))
					{
						this.line = Integer.parseInt(args[2]) - 1;
					}
				}
				case "Call" -> {
					storedIntegers.put("return", line);
					this.line = Integer.parseInt(args[0]) - 1;
				}
				case "Return" -> {
					this.line = storedIntegers.get("return");
				}
				case "Delay" -> {
					try {
						Thread.sleep(Long.parseLong(args[0]));
					} catch (InterruptedException e) {
						Message.send(target, "§eScript thread unexpectedly woke up! This is harmless, but please contact an administrator.");
						e.printStackTrace();
					}
				}
				case "Synchronize" -> {
					Thread current = Thread.currentThread();
					new BukkitRunnable()
					{
						@Override
						public void run() 
						{
							synchronized (this)
							{
								current.notify();
							}
						}
					}.runTask(Hyacinth.getInstance());

					synchronized (this)
					{
						current.wait();
					}
				}

				//
				// Variables
				//
				
				case "SetInteger" -> {
					storedIntegers.put(args[0], Integer.parseInt(args[1]));
				}
				case "Push" -> {
					stack.addFirst(Integer.parseInt(args[0]));
				}
				case "Pop" -> {
					storedIntegers.put(args[0], stack.removeFirst());
				}
				case "Touch" -> {
					for (String s : args)
					{
						storedIntegers.get(s);
					}
				}

				//
				// Math
				//
				
				case "Increment" -> {
					storedIntegers.put(args[0], storedIntegers.get(args[0]) + 1);
				}
				case "Decrement" -> {
					storedIntegers.put(args[0], storedIntegers.get(args[0]) - 1);
				}
				case "Add" -> {
					storedIntegers.put(args[0], storedIntegers.get(args[0]) + Integer.parseInt(args[1]));
				}
				case "Subtract" -> {
					storedIntegers.put("result", Integer.parseInt(args[0]) - Integer.parseInt(args[1]));
				}
				case "Multiply" -> {
					storedIntegers.put(args[0], storedIntegers.get(args[0]) * Integer.parseInt(args[1]));
				}
				case "Divide" -> {
					storedIntegers.put("result", Integer.parseInt(args[0]) / Integer.parseInt(args[1]));
				}

				//
				// Boolean
				//
				
				case "Compare" -> {
					previousOperationSuccess = (Integer.parseInt(args[0]) == Integer.parseInt(args[1]));
				}

				//
				// Script
				//

				case "EndScript" -> {
					this.isStopped = true;
				}
				case "None" -> {} // Nothing
				case "Print" -> {
					Logger.log(args[0]);
				}

				//
				// World
				//

				case "SetWorld" -> {
					this.world = Bukkit.getWorld(args[0]);
				}
				case "PlayWorldSound" -> {
					this.world.playSound(this.locFromArguments(args, 0, 1, 2), Sound.valueOf(args[3]), Float.parseFloat(args[4]), Float.parseFloat(args[5]));
				}
				case "BreakBlock" -> {
					this.world.getBlockAt(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2])).breakNaturally();
				}
				case "SpawnFallingBlock" -> {
					Block block = this.world.getBlockAt(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
					this.world.spawnFallingBlock(block.getLocation(), block.getBlockData());
				}
				case "SpawnRegularEntity" -> {
					LivingEntity ent = (LivingEntity) this.world.spawnEntity(this.locFromArguments(args, 2, 3, 4), EntityType.valueOf(args[1]));
					storedEntities.put(args[0], ent);
				}

				//
				// Entity
				//

				case "GiveEffect" -> {
					LivingEntity targetEnt = target;

					if (!args[0].equals("player"))
					{
						targetEnt = storedEntities.get(args[0]);
					}

					PotionEffect effect = new PotionEffect(PotionEffectType.getByName(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
					targetEnt.addPotionEffect(effect);
				} 
				case "GiveItem" -> {
					storedEntities.get(args[0]).getEquipment().setItem(EquipmentSlot.valueOf(args[1]), new ItemStack(Material.valueOf(args[2])));
				}
				case "SetEntityName" -> {
					LivingEntity ent = storedEntities.get(args[0]);
					ent.setCustomName(args[1]);
					ent.setCustomNameVisible(true);
				}

				//
				// Chat
				//

				case "Chat" -> {
					Message.send(target, args[0]);
				}
				case "PlayPlayerSound" -> {
					target.playSound(target.getLocation(), Sound.valueOf(args[0]), Float.parseFloat(args[1]), Float.parseFloat(args[2]));
				}
				case "PrimeScriptButton" -> {
					ScriptManager.primeAction(target);
				}
				case "ChatButton" -> {
					target.spigot().sendMessage(buildClickableFromArg(args[0]));
				}

				// Etc.
				default -> Message.send(target, "§cUnknown instruction \"" + instruction + "\" @ line " + line + " in script " + this.getIdentifier());
			}
		} catch (Exception e) {
			Message.send(target, "§cEncountered an error @ line " + line + " in script " + this.getIdentifier() + ". Please contact an administrator.");
			e.printStackTrace();
		}
	}

	private Location locFromArguments(
		@NotNull String[] args,
		@NotEmpty int... range
	) {
		return new Location(this.world, Double.parseDouble(args[range[0]]), Double.parseDouble(args[range[1]]), Double.parseDouble(args[range[2]]));
	}

	private static BaseComponent[] buildClickableFromArg(String arg) 
	{
		Pattern pattern = Pattern.compile("<(.+?)>");
		Matcher matcher = pattern.matcher(arg);

		if (!matcher.find())
		{
			return new ComponentBuilder()
				.append(arg)
				.create();
		}
		matcher.reset();
		
		// Preprocess buttons
		Map<Integer, String[]> clickData = new HashMap<>();
		while (matcher.find())
		{
			clickData.put(clickData.size(), matcher.group().substring(1, matcher.group().length() - 1) .split("[|]"));
		}

		int offset = 1;
		String[] argBuffer = arg.split("<.*?>", -1);

		ComponentBuilder builder = new ComponentBuilder();
		for (int i = 0; i < argBuffer.length; i++)
		{
			builder = builder.append(argBuffer[i]);
			if (clickData.containsKey(i))
			{
				builder.append(clickData.get(i)[0]);
				builder.getComponent(i + offset).setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + clickData.get(i)[1]));
				clickData.remove(i);
				offset++;
			}
		}

		return builder.create();
	}
}
