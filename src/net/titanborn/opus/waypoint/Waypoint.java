package net.titanborn.opus.waypoint;

import java.util.function.BiConsumer;

import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

import coffee.khyonieheart.hyacinth.util.marker.NotNull;
import coffee.khyonieheart.hyacinth.util.marker.Nullable;
import net.titanborn.opus.script.Script;

public class Waypoint 
{
	private AxisAlignedBoundingBox aabb;
	private BiConsumer<Player, Waypoint> activateFunction;

	public Waypoint(
		@NotNull AxisAlignedBoundingBox aabb, 
		@Nullable BiConsumer<Player, Waypoint> activateFunction
	) {
		this.aabb = aabb;
		this.activateFunction = activateFunction;
	}

	public void setScript(Script script)
	{
		activateFunction = (player, waypoint) -> {
			script.play(player);
			waypoint.markForUnsubscription(player);
		};
	}

	public Environment getDimension()
	{
		return aabb.getCenter().getWorld().getEnvironment();
	}

	public boolean isInDimension(Location location)
	{
		return aabb.getCenter().getWorld().getEnvironment().equals(location.getWorld().getEnvironment());
	}

	public void markForUnsubscription(Player player)
	{
		WaypointManager.markForUnsubscription(player, this);	
	}

	public boolean contains(Location location)
	{
		return aabb.contains(location);
	}

	public AxisAlignedBoundingBox getAabb()
	{
		return this.aabb;
	}

	public void run(Player target)
	{
		if (activateFunction == null)
		{
			return;
		}
		
		activateFunction.accept(target, this);
	}
}
