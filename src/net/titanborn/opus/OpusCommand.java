package net.titanborn.opus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import coffee.khyonieheart.hyacinth.Hyacinth;
import coffee.khyonieheart.hyacinth.Message;
import coffee.khyonieheart.hyacinth.command.BukkitCommandMeta;
import coffee.khyonieheart.hyacinth.print.Grammar;
import net.titanborn.opus.script.Script;
import net.titanborn.opus.waypoint.AxisAlignedBoundingBox;
import net.titanborn.opus.waypoint.Waypoint;
import net.titanborn.opus.waypoint.WaypointManager;

@BukkitCommandMeta(
	usage = "/opus <add | subscribe | visualize | attachscript>",
	description = "Command for Titanborn questing.",
	permission = "titanborn.opus",
	aliases = { }
)
public class OpusCommand implements CommandExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
	{
		if (args.length == 0)
		{
			return false;
		}

		switch (args[0].toLowerCase())
		{
			case "subscribe":
				command_subscribe(sender, args);
				break;
			case "add": 
				command_add(sender, args);
				break;
			case "visualize":
				command_visualize(sender, args);
				break;
			case "attachscript":
				command_attachscript(sender, args);
				break;
			default: return false;
		}

		return true;
	}

	private List<Waypoint> createdWaypoints = new ArrayList<>(); // FIXME Debug

	// /opus add x y z rx ry rz <script>
	private boolean command_add(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player))
		{
			Message.send(sender, "§cOnly players can use this command.");
			return true;
		}

		if (args.length < 7)
		{
			Message.send(sender, "§cUsage: /opus add x y z rx ry rz <optional:script>");
			return true;
		}

		double[] data = new double[6];
		for (int i = 1; i < 7; i++)
		{
			try {
				data[i - 1] = Double.parseDouble(args[i]);
			} catch (NumberFormatException e) {
				Message.send(sender, "§cExpected double (x.y) at position " + i + " but received " + args[i]);
				return true;
			}
		}

		AxisAlignedBoundingBox aabb = new AxisAlignedBoundingBox(new Location(((Player) sender).getLocation().getWorld(), data[0], data[1], data[2]), data[3], data[4], data[5]);
		Waypoint waypoint = new Waypoint(aabb, (player, point) -> {
			Message.send(player, "Currently in waypoint");
			point.markForUnsubscription(player);
		});

		createdWaypoints.add(waypoint);
		WaypointManager.subscribe((Player) sender, waypoint);
		Message.send(sender, "§aCreated a new waypoint at (" + data[0] + ", " + data[1] + ", " + data[2] + ") and subscribed to it!");	

		if (args.length == 8)
		{
			File scriptFile = new File("./Titanborn/Scripts/" + args[7] + ".script");
			if (!scriptFile.exists())
			{
				Message.send(sender, "§cNo script file exists by that name. Check script name and attach with /opus attachscript " + (createdWaypoints.size() - 1) + " <script>");
				return true;
			}

			waypoint.setScript(new Script(scriptFile));
			Message.send(sender, "§aSuccessfully attached script \"" + scriptFile.getName() + "\" to waypoint!");
			return true;
		}

		Message.send(sender, "§aAttach a script to this waypoint by running /opus attachscript " + (createdWaypoints.size() - 1) + " <script>.");

		return true;
	}

	private boolean command_subscribe(CommandSender sender, String[] args)
	{		
		if (!(sender instanceof Player))
		{
			Message.send(sender, "§cOnly players can use this command.");
			return true;
		}

		if (args.length == 1)
		{
			printAllWaypoints(sender);
			return true;
		}

		int id;
		try {
			id = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			Message.send(sender, "§cExpected number between 0 and " + (createdWaypoints.size() - 1) + ", got " + args[1]);
			return true;
		}

		Waypoint waypoint = createdWaypoints.get(id);
		if (WaypointManager.isSubscribed((Player) sender, waypoint))
		{
			Message.send(sender, "§cYou're already subscribed to this waypoint.");
			return true;
		}

		WaypointManager.subscribe((Player) sender, waypoint);
		Message.send(sender, "§aSuccessfully subscribed to waypoint " + id + "!");

		return true;
	}

	// TODO Add command that handles actions such as clicking on a chat button

	private boolean command_visualize(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player))
		{
			Message.send(sender, "§cOnly players can use this command.");
			return true;
		}

		if (args.length == 1)
		{
			printAllWaypoints(sender);
			Message.send(sender, "§9§oRunning /opus visualize all will display all created waypoints.");
			return true;
		}

		if (args[1].equals("all"))
		{
			for (Waypoint w : createdWaypoints)
			{
				showWaypoint(w.getAabb());
			}
			Message.send(sender, "§aShowing all " + createdWaypoints.size() + Grammar.plural(createdWaypoints.size(), " waypoint ", " waypoints ") + " for 15 seconds.");

			return true;
		}
		
		int id;
		try {
			id = Integer.parseInt(args[1]);

			if (id >= createdWaypoints.size())
			{
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			Message.send(sender, "§cExpected number between 0 and " + (createdWaypoints.size() - 1) + ", got " + args[1]);
			return true;
		}

		AxisAlignedBoundingBox aabb = createdWaypoints.get(id).getAabb();
		showWaypoint(aabb);

		Message.send(sender, "§aShowing waypoint ID " + id + " for 15 seconds.");

		return true;
	}

	private void showWaypoint(AxisAlignedBoundingBox aabb)
	{
		Location center = aabb.getCenter();

		for (int iy = (int) (center.getY() - aabb.getRadiusY()); iy < center.getY() + aabb.getRadiusY(); iy++)
		{
			for (int ix = (int) (center.getX() - aabb.getRadiusX()); ix < center.getX() + aabb.getRadiusX(); ix++)
			{
				for (int iz = (int) (center.getZ() - aabb.getRadiusZ()); iz < center.getZ() + aabb.getRadiusZ(); iz++)
				{
					int tempX = ix;
					int tempY = iy;
					int tempZ = iz;
					long start = System.currentTimeMillis();
					new BukkitRunnable() {
						@Override
						public void run() 
						{
							if (System.currentTimeMillis() - start > 15000)
							{
								this.cancel();
								return;
							}

							center.getWorld().spawnParticle(Particle.REDSTONE, new Location(center.getWorld(), tempX + 0.5, tempY + 0.5, tempZ + 0.5), 1, new Particle.DustOptions(Color.RED, 1.0f));
						}
					}.runTaskTimer(Hyacinth.getInstance(), 0l, 1l);
				}
			}
		}
	}

	// /opus attachscript <ID> <Script name>
	private boolean command_attachscript(CommandSender sender, String[] args)
	{
		if (args.length < 3)
		{
			return false;
		}

		int id;
		try {
			id = Integer.parseInt(args[1]);

			if (id >= createdWaypoints.size())
			{
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			Message.send(sender, "§cExpected number between 0 and " + (createdWaypoints.size() - 1) + ", got " + args[1]);
			return true;
		}

		// TODO Sanitize args[1]
		File scriptFile = new File("./Titanborn/Scripts/" + args[2] + ".script");
		if (!scriptFile.exists())
		{
			Message.send(sender, "§cNo script file exists by that name.");
			return true;
		}

		createdWaypoints.get(id).setScript(new Script(scriptFile));
		Message.send(sender, "§aSuccessfully attached script " + args[2] + " to waypoint ID " + id + "!");
		return true;
	}

	private void printAllWaypoints(CommandSender sender)
	{
		Message.send(sender, "§9All available waypoint(s):");
		for (int i = 0; i < createdWaypoints.size(); i++)
		{
			AxisAlignedBoundingBox aabb = createdWaypoints.get(i).getAabb();
			Message.send(sender, "§o[" + i + "] World: " + aabb.getCenter().getWorld().getName() + " @ (" + aabb.getCenter().getX() + ", " + aabb.getCenter().getY() + ", " + aabb.getCenter().getZ() + ")");
		}
	}
}
