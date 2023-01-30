package net.titanborn.opus.waypoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import coffee.khyonieheart.hyacinth.util.marker.NotNull;
import coffee.khyonieheart.hyacinth.Hyacinth;

public class WaypointManager implements Listener
{
	private static Map<Player, List<Waypoint>> ACTIVE_WAYPOINTS = new HashMap<>();
	private static Map<Player, List<Waypoint>> MARKED_FOR_UNSUBSCRIPTION = new HashMap<>();
	private static BukkitTask checkingTask;

	public static void startWaypointChecking(
		long period
	) {
		if (checkingTask != null)
		{
			throw new IllegalStateException("Cannot start a new waypoint collision checker without cancelling the previous one");
		}

		checkingTask = new BukkitRunnable()
		{
			@Override
			public void run() 
			{
				if (this.isCancelled())
				{
					return;
				}

				ACTIVE_WAYPOINTS.forEach((player, waypoints) -> {
					for (Waypoint w : waypoints)
					{
						// Skip waypoints that the player doesn't share a dimension with 
						if (!w.isInDimension(player.getLocation()))
						{
							continue;
						}

						// Skip waypoints that aren't near the player
						if (!w.getAabb().isNear(player.getLocation(), 2.0))
						{
							continue;
						}

						if (!w.contains(player.getLocation()))
						{
							continue;
						}

						w.run(player);
					}
				});						

				// Housekeeping
				if (MARKED_FOR_UNSUBSCRIPTION.size() == 0)
				{
					return;
				}
				MARKED_FOR_UNSUBSCRIPTION.forEach((player, subscriptions) -> {
					ACTIVE_WAYPOINTS.get(player).removeAll(subscriptions);
				});
				MARKED_FOR_UNSUBSCRIPTION.clear();
			}
		}.runTaskTimer(Hyacinth.getInstance(), 0l, period);
	}

	/**
	 * Adds a waypoint to the list of waypoints for the player to unsubscribe from after the next waypoint check concludes.
	 * @param player Player to unsubscribe with 
	 * @param waypoint Waypoint to unsubscribe from 
	 */
	public static void markForUnsubscription(
		@NotNull Player player, 
		@NotNull Waypoint waypoint
	) {
		if (!MARKED_FOR_UNSUBSCRIPTION.containsKey(player))
		{
			MARKED_FOR_UNSUBSCRIPTION.put(player, new ArrayList<>());
		}

		MARKED_FOR_UNSUBSCRIPTION.get(player).add(waypoint);
	}

	public static boolean isSubscribed(
		Player player, 
		Waypoint waypoint
	) {
		if (!ACTIVE_WAYPOINTS.containsKey(player))
		{
			return false;
		}

		return ACTIVE_WAYPOINTS.get(player).contains(waypoint);
	}

	public static void stopChecking()
	{
		if (checkingTask == null)
		{
			return;
		}

		checkingTask.cancel();
	}

	public static void subscribe(
		@NotNull Player player,
		@NotNull Waypoint target
	) {
		if (!ACTIVE_WAYPOINTS.containsKey(player))
		{
			ACTIVE_WAYPOINTS.put(player, new ArrayList<>());
		}
		
		ACTIVE_WAYPOINTS.get(player).add(target);
	}

	@EventHandler
	public synchronized void onDisconnect(PlayerQuitEvent event)
	{
		ACTIVE_WAYPOINTS.remove(event.getPlayer());
	}
}
