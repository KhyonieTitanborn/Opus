package net.titanborn.opus;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import coffee.khyonieheart.hyacinth.command.BukkitCommandMeta;
import net.titanborn.opus.script.Script;

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
		switch (args[0])
		{
			case "play":
				new Script(new File("./Titanborn/Scripts/" + args[1] + ".script")).play((Player) sender);
				break;
		}
		return true;
	}
}
