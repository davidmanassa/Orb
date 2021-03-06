package com.nuno1212s.crates.commands;

import com.nuno1212s.util.CommandUtil.commandexecutors.CommandManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles crate command
 */
public class CrateCommandManager extends CommandManager {

    public CrateCommandManager() {
        super();

        addCommand(new CrateAddRewardCommand());
        addCommand(new CrateCreateCommand());
        addCommand(new AnimationAddDisplayItemCommand());
        addCommand(new CrateTestCommand());
        addCommand(new CrateRemoveRewardCommand());
        addCommand(new CrateRewardListCommand());
        addCommand(new LinkCrateCommand());
        addCommand(new CrateRemoveCommand());
        addCommand(new SetCrateKeyCostCommand());
        addCommand(new GetCrateKeyCommand());
        addCommand(new SetDefaultKeyItemCommand());
        addCommand(new BuyCrateKeyCommand());
        addCommand(new CrateRewardAddItemCommand());
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length == 0) {
            if (commandSender.hasPermission("crates.commands")) {
                commandSender.sendMessage("");
                this.getCommands().forEach(cmd -> commandSender.sendMessage(cmd.usage()));
                commandSender.sendMessage("");
                return true;
            }
        }

        com.nuno1212s.util.CommandUtil.Command c = getCommand(args[0]);

        if (c != null) {
            c.execute((Player) commandSender, args);
        } else {
            if (commandSender.hasPermission("crates.commands")) {
                commandSender.sendMessage("");
                this.getCommands().forEach(cmd -> commandSender.sendMessage(cmd.usage()));
                commandSender.sendMessage("");
            }
        }

        return true;
    }
}
