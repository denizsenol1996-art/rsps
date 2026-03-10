package io.xeros.content.casino;

import io.xeros.model.entity.player.Boundary;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.PlayerHandler;
import io.xeros.util.Misc;

/**
 * Central manager for all casino games.
 * Handles area checks, wager validation, currency, and broadcasting.
 * 
 * Casino area is located near the existing gambling zone.
 * Teleport with ::casino
 * 
 * @author T4C Casino System
 */
public class CasinoManager {

    /** Casino area boundary - adjust coords to your map */
    public static final Boundary CASINO_AREA = new Boundary(3109, 3516, 3140, 3545);

    /** Teleport destination for ::casino */
    public static final int CASINO_X = 3120;
    public static final int CASINO_Y = 3525;
    public static final int CASINO_Z = 0;

    /** Currency item ID - 995 = coins, change to your server's currency */
    public static final int CURRENCY_ID = 995;
    public static final String CURRENCY_NAME = "coins";

    /** Minimum and maximum wagers */
    public static final int MIN_BET = 1_000;
    public static final int MAX_BET = 2_147_000_000;

    /** Cooldown between games in milliseconds */
    public static final long GAME_COOLDOWN = 2000;

    /**
     * Check if a player is in the casino area.
     */
    public static boolean inCasino(Player player) {
        return Boundary.isIn(player, CASINO_AREA) 
            || Boundary.isIn(player, Boundary.FLOWER_POKER_AREA);
    }

    /**
     * Validate that a player can gamble (not banned, in area, not busy).
     */
    public static boolean canGamble(Player player) {
        if (player.isGambleBanned()) {
            player.sendMessage("@red@You are banned from gambling.");
            return false;
        }
        if (!inCasino(player)) {
            player.sendMessage("@red@You must be in the casino area. Use ::casino to teleport.");
            return false;
        }
        return true;
    }

    /**
     * Validate and deduct a wager from the player.
     * Returns true if the wager was successfully taken.
     */
    public static boolean takeWager(Player player, int amount) {
        if (amount < MIN_BET) {
            player.sendMessage("@red@Minimum bet is " + Misc.formatCoins(MIN_BET) + " " + CURRENCY_NAME + ".");
            return false;
        }
        if (amount > MAX_BET) {
            player.sendMessage("@red@Maximum bet is " + Misc.formatCoins(MAX_BET) + " " + CURRENCY_NAME + ".");
            return false;
        }
        if (player.getItems().getItemAmount(CURRENCY_ID) < amount) {
            player.sendMessage("@red@You don't have enough " + CURRENCY_NAME + ". You need " + Misc.formatCoins(amount) + ".");
            return false;
        }
        player.getItems().deleteItem(CURRENCY_ID, amount);
        return true;
    }

    /**
     * Give winnings to a player.
     */
    public static void giveWinnings(Player player, int amount) {
        player.getItems().addItem(CURRENCY_ID, amount);
    }

    /**
     * Broadcast a casino event to all players in the casino area.
     */
    public static void broadcast(String message) {
        PlayerHandler.nonNullStream()
            .filter(p -> inCasino(p))
            .forEach(p -> p.sendMessage(message));
    }

    /**
     * Broadcast a big win server-wide.
     */
    public static void broadcastBigWin(Player player, String game, int amount) {
        if (amount >= 10_000_000) {
            PlayerHandler.nonNullStream().forEach(p -> 
                p.sendMessage("[@red@CASINO@bla@] @blu@" + player.getDisplayName() 
                    + " @bla@just won @gre@" + Misc.formatCoins(amount) 
                    + " " + CURRENCY_NAME + "@bla@ playing @red@" + game + "@bla@!"));
        }
    }

    /**
     * Format coins for display.
     */
    public static String formatAmount(int amount) {
        return Misc.formatCoins(amount);
    }

    /**
     * Check game cooldown for a player.
     */
    public static boolean checkCooldown(Player player) {
        if (System.currentTimeMillis() - player.casinoCooldown < GAME_COOLDOWN) {
            player.sendMessage("@red@Please wait before starting another game.");
            return false;
        }
        player.casinoCooldown = System.currentTimeMillis();
        return true;
    }
}
