package net.titanborn.opus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import coffee.khyonieheart.hyacinth.command.BukkitCommandMeta;

@BukkitCommandMeta(
	usage = "/titanscript <jump | attach | reload>",
	description = "Handler for titanscripts.",
	permission = "titanborn.titanscript.user",
	aliases = { "ts", "scripts" }
)
public class TitanscriptCommand implements CommandExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
	{
		return true;
	}
}
