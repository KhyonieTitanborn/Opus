package net.titanborn.opus;

import org.bukkit.plugin.java.JavaPlugin;

import coffee.khyonieheart.hyacinth.util.Folders;
import coffee.khyonieheart.hyacinth.util.Registration;
import net.titanborn.opus.waypoint.WaypointManager;

public class Opus extends JavaPlugin
{
	@Override
	public void onEnable()
	{
		Folders.ensureFolder("./Titanborn/Scripts/");
		Registration.registerCommandExecutor(new OpusCommand(), this, "opus");
		Registration.registerCommandExecutor(new TitanscriptCommand(), this, "titanscript");

		WaypointManager.startWaypointChecking(1l); // TODO This might be too laggy with enough waypoints and players
	}

	@Override
	public void onDisable()
	{

	}
}
