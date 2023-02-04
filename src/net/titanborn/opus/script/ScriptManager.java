package net.titanborn.opus.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import coffee.khyonieheart.hyacinth.util.marker.NotNull;
import coffee.khyonieheart.hyacinth.util.marker.Nullable;

public class ScriptManager
{
	private static Map<Player, List<Script>> currentlyRunningScripts = new HashMap<>();
	private static List<Player> actionPrimedPlayers = new ArrayList<>(); // Keep track of players that have an action primed

	public static void registerPlaying(
		@NotNull Player player, 
		@NotNull Script script
	) {
		if (!currentlyRunningScripts.containsKey(player)) // TODO Handle disconnect
		{
			currentlyRunningScripts.put(player, new ArrayList<>());
		}

		currentlyRunningScripts.get(player).add(script);
	}

	public static void registerStop(
		@NotNull Player player,
		@NotNull Script script 
	) {
		if (!currentlyRunningScripts.containsKey(player))
		{
			return;
		}

		currentlyRunningScripts.get(player).remove(script);
	}

	@Nullable
	public static Script getRunningScript(
		@NotNull Player player, 
		@NotNull String identifier
	) {
		if (!currentlyRunningScripts.containsKey(player))	
		{
			return null;
		}

		for (Script s : currentlyRunningScripts.get(player))
		{
			if (s.getIdentifier().equals(identifier))
			{
				return s;
			}
		}

		return null;
	}

	public static void primeAction(
		@NotNull Player player 
	) {
		if (actionPrimedPlayers.contains(player))
		{
			return;
		}

		actionPrimedPlayers.add(player);
	}

	public static void unprimeAction(
		@NotNull Player player
	) {
		actionPrimedPlayers.remove(player);
	}
}
