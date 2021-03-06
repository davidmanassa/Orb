package com.nuno1212s.sellsigns.listeners;

import com.nuno1212s.main.MainData;
import com.nuno1212s.playermanager.PlayerData;
import com.nuno1212s.sellsigns.main.Main;
import com.nuno1212s.sellsigns.signs.StoreSign;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Sign click listener
 */
public class SignClickListener implements Listener {

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null && e.getClickedBlock().getType() != Material.AIR) {
            if (e.getClickedBlock().getState() instanceof Sign) {
                Sign s = (Sign) e.getClickedBlock().getState();

                StoreSign sign = Main.getIns().getSignManager().getSign(s.getLocation());

                if (sign == null) {
                    return;
                }

                e.setCancelled(true);

                if (e.getAction() == Action.LEFT_CLICK_BLOCK) {

                    if (Main.getIns().getSignManager().isEditing(e.getPlayer().getUniqueId())) {

                        Main.getIns().getSignManager().removeSign(sign);
                        MainData.getIns().getMessageManager().getMessage("REMOVED_SIGN").sendTo(e.getPlayer());

                        return;
                    }

                    if (sign.isCanSell()) {

                        int price = sign.getSellPrice();

                        PlayerData d = MainData.getIns().getPlayerManager().getPlayer(e.getPlayer().getUniqueId());

                        double rankMultiplier = sign.getRankMultiplier(d);

                        double finalPrice = (double) price * rankMultiplier,
                                perItemPrice = (double) finalPrice / (double) sign.getItem().getAmount();

                        int amount;

                        PlayerInventory p = e.getPlayer().getInventory();

                        if (e.getPlayer().isSneaking()) {
                            amount = 0;
                            if (p.containsAtLeast(sign.getItem(), 1)) {
                                for (ItemStack itemStack : p.getContents()) {
                                    if (itemStack == null || itemStack.getType() == Material.AIR) {
                                        continue;
                                    }
                                    if (itemStack.isSimilar(sign.getItem())) {
                                        amount += itemStack.getAmount();
                                        p.removeItem(itemStack);
                                    }
                                }
                            }


                            int coins = (int) Math.floor(perItemPrice * amount);

                            MainData.getIns().getServerCurrencyHandler().addCurrency(d, coins);
                            MainData.getIns().getMessageManager().getMessage("SOLD_ITEM_S").format("%price%", String.valueOf(coins))
                                    .format("%amount%", String.valueOf(amount)).format("%multiplier%", String.format("%.2f", rankMultiplier)).sendTo(e.getPlayer());

                        } else {
                            ItemStack item = sign.getItem();

                            if (p.containsAtLeast(sign.getItem(), item.getAmount())) {
                                int coinsToAdd = (int) Math.floor(perItemPrice * item.getAmount());
                                p.removeItem(item);
                                MainData.getIns().getServerCurrencyHandler().addCurrency(d, coinsToAdd);
                                MainData.getIns().getMessageManager().getMessage("SOLD_ITEM_S").format("%price%", String.valueOf(coinsToAdd))
                                        .format("%amount%", String.valueOf(item.getAmount()))
                                        .format("%multiplier%", String.format("%.2f", rankMultiplier)).sendTo(e.getPlayer());
                                return;
                            }

                        }

                    }
                } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {

                    if (e.getPlayer().isSneaking()) {
                        if (Main.getIns().getSignManager().isEditing(e.getPlayer().getUniqueId())) {
                            if (sign.getItem() == null || sign.getItem().getType() == Material.AIR) {
                                ItemStack itemInHand = e.getPlayer().getItemInHand();

                                sign.setItem(itemInHand.clone());
                                sign.updateName();
                                e.getPlayer().sendMessage(ChatColor.RED + "Store item set to the item in your hand");
                                return;
                            }
                        }
                    }

                    if (sign.isCanBuy() && sign.getItem() != null) {
                        int price = sign.getPrice();

                        PlayerData d = MainData.getIns().getPlayerManager().getPlayer(e.getPlayer().getUniqueId());
                        MainData.getIns().getServerCurrencyHandler().removeCurrency(d, price).thenAccept((completed) -> {
                            if (completed) {
                                e.getPlayer().getInventory().addItem(sign.getItem().clone());
                                MainData.getIns().getMessageManager().getMessage("BOUGHT_ITEM_S").format("%price%", String.valueOf(price))
                                        .sendTo(e.getPlayer());
                            } else {
                                MainData.getIns().getMessageManager().getMessage("NO_COINS").sendTo(e.getPlayer());
                            }

                        });
                    }

                }

            }
        }
    }


}
