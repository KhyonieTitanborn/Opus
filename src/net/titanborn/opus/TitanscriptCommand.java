package net.titanborn.opus;

import java.io.File;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import coffee.khyonieheart.hyacinth.Message;
import coffee.khyonieheart.hyacinth.command.HyacinthCommand;
import coffee.khyonieheart.hyacinth.module.HyacinthModule;
import net.titanborn.opus.script.Script;

/**
 * Titancript command.
 * Relevant permissions:
 * opus.titanborn.user - Enables basic functionality from buttons
 * opus.titanborn.ct - Content team commands
 */
public class TitanscriptCommand extends HyacinthCommand
{
	public TitanscriptCommand() 
	{
		super("titanscript", "/titanscript <play | attach | jump | read>", "opus.titanscript.user", "ts", "scripts");
	}

	public void play(CommandSender sender, String[] args)
	{
		if (!this.testPermissionStringSilent(sender, "opus.titanscript.ct"))
		{
			return;
		}

		new Script(new File("./Titanborn/Scripts/" + args[1] + ".script")).play((Player) sender);
	}

	public void read(CommandSender sender, String[] args)
	{
		if (!this.testPermissionStringSilent(sender, "opus.titanscript.ct"))
		{
			return;
		}

		Script script = new Script(new File("./Titanborn/Scripts/" + args[1] + ".script"));
		if (script.getLines() == null)
		{
			Message.send(sender, "§cNo script file exists by that name.");
			return;
		}

		for (String s : script.getLines())
		{
			if (s.startsWith("//"))
			{
				s = "§7" + s;
			}

			Message.send(sender, s);
		}
	}

	@Override
	public HyacinthModule getModule() 
	{
		return Opus.getInstance();
	}
}
