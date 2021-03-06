package com.nuno1212s.bungee.commands;

import com.nuno1212s.bungee.loginhandler.SessionData;
import com.nuno1212s.bungee.loginhandler.SessionHandler;
import com.nuno1212s.main.MainData;
import com.nuno1212s.playermanager.PlayerData;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class StaffCommand extends Command {

    public StaffCommand() {
        super("staff");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (sender instanceof ProxiedPlayer) {

            SessionData session = SessionHandler.getIns().getSession(((ProxiedPlayer) sender).getUniqueId());
            if (session == null || !session.isAuthenticated()) {
                sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&cNão podes fazer este comando sem estar logado.")));
                return;
            }


            PlayerData pd = MainData.getIns().getPlayerManager().getPlayer(sender.getName());
            if (!pd.getMainGroup().hasPermission("staff.command")) {
                sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&cWithout permission..")));
                return;
            }

        } else {
            return;
        }

        sender.sendMessage(new ComponentBuilder("Equipe online:").color(ChatColor.GREEN).create());

        for (PlayerData pd : MainData.getIns().getPlayerManager().getPlayers()) {

            if (pd.getMainGroup().hasPermission("staff")) {

                ProxiedPlayer pp = ProxyServer.getInstance().getPlayer(pd.getPlayerName());
                if (pp  == null || !pp.isConnected()) {
                    continue;
                }

                TextComponent message = new TextComponent("- ");
                message.setColor(ChatColor.WHITE);
                TextComponent p1 = new TextComponent(pd.getNameWithPrefix());
                message.addExtra(p1);
                TextComponent a1 = new TextComponent(" está no servidor ");
                a1.setColor(ChatColor.WHITE);
                message.addExtra(a1);
                TextComponent s = new TextComponent(pp.getServer().getInfo().getName());
                s.setColor(ChatColor.YELLOW);
                message.addExtra(s);

                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + pp.getServer().getInfo().getName()));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to enter on this server").color(ChatColor.YELLOW).create()));
                sender.sendMessage(message);

            }
        }

    }

}
