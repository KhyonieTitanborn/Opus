package net.titanborn.opus;

import coffee.khyonieheart.hyacinth.module.HyacinthJavaPlugin;
import coffee.khyonieheart.hyacinth.util.Folders;
import coffee.khyonieheart.hyacinth.util.Registration;
import net.titanborn.opus.waypoint.WaypointManager;

public class Opus extends HyacinthJavaPlugin
{
	private static Opus INSTANCE;
	@Override
	public void onEnable()
	{
		INSTANCE = this;

		Folders.ensureFolder("./Titanborn/Scripts/");
		Registration.registerHyacinthCommand(new OpusCommand(), this);
		Registration.registerHyacinthCommand(new TitanscriptCommand(), this);

		WaypointManager.startWaypointChecking(1l); // TODO This might be too laggy with enough waypoints and players
	}

	@Override
	public void onDisable()
	{

	}

	public static Opus getInstance()
	{
		return INSTANCE;
	}
}
