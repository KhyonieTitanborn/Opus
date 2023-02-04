package net.titanborn.opus.script;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
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
import coffee.khyonieheart.hyacinth.util.marker.NotNull;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class Script 
{
	private World world;
	private Map<String, LivingEntity> spawnedEntities = new HashMap<>();
	private Map<String, Integer> createdIntegers = new HashMap<>();
	private boolean previousOperationSuccess = false;

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

	public void play(
		@NotNull Player target
	) {
		// Store player's coordinates
		createdIntegers.put("playerX", (int) target.getLocation().getX());
		createdIntegers.put("playerY", (int) target.getLocation().getY());
		createdIntegers.put("playerZ", (int) target.getLocation().getZ());

		// Setup $return
		createdIntegers.put("return", 0);

		// Perform script
		new BukkitRunnable()
		{
			@Override
			public void run() 
			{
				playScript(target);	
			}
		}.runTask(Hyacinth.getInstance());
	}

	private void playScript(Player target)
	{
		playLoop: for (int lineIndex = 0; lineIndex < scriptActions.length; lineIndex++)
		{
			String line = scriptActions[lineIndex];
			if (line.equals("") || line.startsWith("//"))
			{
				continue;
			}

			// Parse args and command
			String[] split = line.split(" ");
			List<String> splitBuffer = new ArrayList<>();
			StringBuilder builder = new StringBuilder();
			boolean addToBuffer = false;
			for (int i = 1; i < split.length; i++)
			{
				if (split[i].startsWith("$"))
				{
					split[i] = "" + createdIntegers.get(split[i].substring(1));
				}

				if (split[i].startsWith("//"))
				{
					break;
				}

				if (addToBuffer)
				{	
					if (split[i].endsWith("\""))
					{
						addToBuffer = false;
						builder.append(split[i].substring(0, split[i].length() - 1));

						splitBuffer.add(builder.toString());
						builder = new StringBuilder();
						continue;
					}

					builder.append(split[i] + " ");
					continue;
				}

				if (split[i].startsWith("\""))
				{
					if (split[i].endsWith("\""))
					{
						splitBuffer.add(split[i].substring(1, split[i].length() - 1));
						continue;
					}

					addToBuffer = true;
					builder.append(split[i].substring(1) + " ");
					continue;
				}

				splitBuffer.add(split[i]);
			}

			String action = split[0];
			String[] args = splitBuffer.toArray(new String[splitBuffer.size()]);

			try {	
				switch (action)
				{
					// Logic
					case "SetInteger" -> createdIntegers.put(args[0], Integer.parseInt(args[1]));
					case "Decrement" -> createdIntegers.put(args[0], createdIntegers.get(args[0]) - 1);
					case "Increment" -> createdIntegers.put(args[0], createdIntegers.get(args[0]) + 1);
					case "JumpNotEqual" -> {
						if (Integer.parseInt(args[0]) != Integer.parseInt(args[1]))
						{
							lineIndex = Integer.parseInt(args[2]) - 1;
						}
					}
					case "JumpEqual" -> {
						if (Integer.parseInt(args[0]) == Integer.parseInt(args[1]))
						{
							lineIndex = Integer.parseInt(args[2]) - 1;
						}
					}
					case "JumpTrue" -> {
						if (previousOperationSuccess)
						{
							lineIndex = Integer.parseInt(args[0]) - 1;
						}
					}
					case "JumpNot" -> {
						if (!previousOperationSuccess)
						{
							lineIndex = Integer.parseInt(args[0]) - 1;
						}
					}
					case "Compare" -> {
						if (Integer.parseInt(args[0]) == Integer.parseInt(args[1]))
						{
							previousOperationSuccess = true;
						} else {
							previousOperationSuccess = false;
						}
					}
					case "Call" -> {
						createdIntegers.put("return", lineIndex);
						lineIndex = Integer.parseInt(args[0]);
					}
					case "Synchronize" -> {
						Thread current = Thread.currentThread();
						new BukkitRunnable()
						{
							@Override
							public void run()
							{
								current.notify();
							}
						}.runTask(Hyacinth.getInstance());
						Thread.currentThread().wait();
					}
					case "Delay" -> {
						Thread current = Thread.currentThread();
						new BukkitRunnable()
						{
							@Override
							public void run()
							{
								current.notify();
							}
						}.runTaskLater(Hyacinth.getInstance(), Long.parseLong(args[0]));
						Thread.currentThread().wait();
					}
					case "Return" -> lineIndex = createdIntegers.get("return");
					case "SetReturn" -> createdIntegers.put("return", Integer.parseInt(args[0]));
					case "Jump" -> lineIndex = Integer.parseInt(args[0]) - 1;
					case "EndScript" -> { 
						break playLoop; 
					}
					case "Print" -> Logger.log("Script " + file.getName() + " > " + args[0]);

					// Actions
					case "SetWorld" -> world = Hyacinth.getInstance().getServer().getWorld(split[1]);
					case "SendChatButton" -> target.spigot().sendMessage(buildClickableFromArg(args[0]));
					case "Chat" -> Message.send(target, args[0]);
					case "PlayWorldSound" -> world.playSound(new Location(world, Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2])), Sound.valueOf(args[3]), Float.parseFloat(args[4]), Float.parseFloat(args[5]));
					case "BreakBlock" -> world.getBlockAt(new Location(world, Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]))).breakNaturally();
					case "PlaySound" -> target.playSound(target.getLocation(), Sound.valueOf(args[0]), Float.parseFloat(args[1]), Float.parseFloat(args[2]));
					case "SpawnRegularEntity" -> { 
						Entity ent = world.spawnEntity(new Location(world, Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4])), EntityType.valueOf(args[1]));
						spawnedEntities.put(args[0], (LivingEntity) ent); // Note that this cannot spawn non-living entities
					}
					case "GiveItem" -> spawnedEntities.get(args[0]).getEquipment().setItem(EquipmentSlot.valueOf(args[1]), new ItemStack(Material.valueOf(args[2])));
					case "GiveEffect" -> {
						LivingEntity potionTarget = target;
						if (!args[0].equals("player"))
						{
							potionTarget = spawnedEntities.get(args[0]);
						}
						potionTarget.addPotionEffect(new PotionEffect(PotionEffectType.getByName(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3])));
					}
					case "SetEntityName" -> {
						spawnedEntities.get(args[0]).setCustomName(args[1]);
						spawnedEntities.get(args[0]).setCustomNameVisible(true);
					}
					case "SpawnFallingBlock" -> {
						Location location = new Location(world, Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]));
						world.spawnFallingBlock(location, world.getBlockAt(location).getBlockData());
					}
					default -> {
						Logger.log("§cUnknown script action \"" + action + "\"");
					}
				}
			} catch (Exception e) {
				Message.send(target, "§cAn error occurred performing script " + file.getName() + " @ line " + lineIndex + ". Please contact an administrator, thanks!");
				e.printStackTrace();
			}
			
			ScriptManager.registerStop(target, this);
		}
	}

	private static BaseComponent[] buildClickableFromArg(String arg) // TODO Public is debug, should be private
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
