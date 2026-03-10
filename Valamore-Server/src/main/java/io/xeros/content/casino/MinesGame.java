package io.xeros.content.casino;

import io.xeros.model.entity.player.Player;
import io.xeros.util.Misc;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mines - Solo game. Player picks tiles on a 5x5 grid.
 * Some tiles have mines (bombs), safe tiles increase the multiplier.
 * Player can cash out at any time.
 * 
 * Usage:
 *   ::mines [amount]     - Start a mines game with default 3 mines
 *   ::mines [amount] [mines]  - Start with custom mine count (1-24)
 *   ::pick [1-25]        - Pick a tile
 *   ::cashout            - Cash out current winnings
 * 
 * More mines = higher multipliers per safe tile.
 * 
 * @author T4C Casino System
 */
public class MinesGame {

    private static final int GRID_SIZE = 25; // 5x5
    private static final int MIN_MINES = 1;
    private static final int MAX_MINES = 24;
    private static final int DEFAULT_MINES = 3;

    /** House edge (1.0 = no edge, 0.97 = 3% house edge) */
    private static final double HOUSE_EDGE_FACTOR = 0.97;

    /**
     * Start a new mines game.
     */
    public static void startGame(Player player, int wager, int mineCount) {
        if (!CasinoManager.canGamble(player)) return;
        if (!CasinoManager.checkCooldown(player)) return;

        if (player.minesActive) {
            player.sendMessage("@red@You already have an active mines game! Use ::pick [1-25] or ::cashout.");
            return;
        }

        if (mineCount < MIN_MINES || mineCount > MAX_MINES) {
            player.sendMessage("@red@Mine count must be between " + MIN_MINES + " and " + MAX_MINES + ".");
            return;
        }

        if (!CasinoManager.takeWager(player, wager)) return;

        // Generate mine positions
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < GRID_SIZE; i++) positions.add(i);
        Collections.shuffle(positions, new SecureRandom());

        boolean[] mines = new boolean[GRID_SIZE];
        for (int i = 0; i < mineCount; i++) {
            mines[positions.get(i)] = true;
        }

        // Store game state
        player.minesActive = true;
        player.minesGrid = mines;
        player.minesRevealed = new boolean[GRID_SIZE];
        player.minesWager = wager;
        player.minesMineCount = mineCount;
        player.minesSafeCount = 0;
        player.minesCurrentMultiplier = 1.0;

        showGrid(player);
        player.sendMessage("[@red@MINES@bla@] Game started! " + mineCount + " mines hidden in a 5x5 grid.");
        player.sendMessage("[@red@MINES@bla@] Wager: @gre@" + CasinoManager.formatAmount(wager) + "@bla@");
        player.sendMessage("[@red@MINES@bla@] Use @blu@::pick [1-25]@bla@ to reveal a tile, or @blu@::cashout@bla@ to collect.");
    }

    /**
     * Start a game with default mine count.
     */
    public static void startGame(Player player, int wager) {
        startGame(player, wager, DEFAULT_MINES);
    }

    /**
     * Pick a tile on the grid.
     * @param tileNum 1-25 tile number
     */
    public static void pickTile(Player player, int tileNum) {
        if (!player.minesActive) {
            player.sendMessage("@red@You don't have an active mines game. Start one with ::mines [amount].");
            return;
        }

        int index = tileNum - 1;
        if (index < 0 || index >= GRID_SIZE) {
            player.sendMessage("@red@Pick a tile between 1 and 25.");
            return;
        }

        if (player.minesRevealed[index]) {
            player.sendMessage("@red@That tile is already revealed! Pick another one.");
            return;
        }

        player.minesRevealed[index] = true;

        if (player.minesGrid[index]) {
            // Hit a mine! Game over.
            player.minesActive = false;
            revealAll(player);
            showGrid(player);
            player.sendMessage("[@red@MINES@bla@] @red@BOOM! You hit a mine on tile " + tileNum + "!");
            player.sendMessage("[@red@MINES@bla@] You lost @red@" 
                + CasinoManager.formatAmount(player.minesWager) + " " + CasinoManager.CURRENCY_NAME + "@bla@.");
            player.startAnimation(2304); // Death emote
            CasinoManager.broadcast("[@red@MINES@bla@] @blu@" + player.getDisplayName() 
                + " @bla@hit a mine and lost @red@" + CasinoManager.formatAmount(player.minesWager) + "@bla@!");
        } else {
            // Safe tile!
            player.minesSafeCount++;
            player.minesCurrentMultiplier = calculateMultiplier(player.minesMineCount, player.minesSafeCount);
            int currentWin = (int) (player.minesWager * player.minesCurrentMultiplier);

            showGrid(player);
            player.sendMessage("[@red@MINES@bla@] @gre@Safe! Tile " + tileNum + " is clear.");
            player.sendMessage("[@red@MINES@bla@] Multiplier: @gre@" 
                + String.format("%.2fx", player.minesCurrentMultiplier) 
                + "@bla@ | Cashout: @gre@" + CasinoManager.formatAmount(currentWin) + "@bla@");

            // Check if all safe tiles revealed
            int safeTiles = GRID_SIZE - player.minesMineCount;
            if (player.minesSafeCount >= safeTiles) {
                // All safe tiles found! Auto-cashout
                cashout(player);
                return;
            }

            player.sendMessage("[@red@MINES@bla@] @blu@::pick [1-25]@bla@ to continue or @blu@::cashout@bla@ to collect.");
        }
    }

    /**
     * Cash out current winnings.
     */
    public static void cashout(Player player) {
        if (!player.minesActive) {
            player.sendMessage("@red@You don't have an active mines game.");
            return;
        }

        if (player.minesSafeCount == 0) {
            player.sendMessage("@red@You need to reveal at least one safe tile before cashing out.");
            return;
        }

        player.minesActive = false;
        int winnings = (int) (player.minesWager * player.minesCurrentMultiplier);
        CasinoManager.giveWinnings(player, winnings);

        revealAll(player);
        showGrid(player);

        player.sendMessage("[@red@MINES@bla@] @gre@Cashed out! " 
            + String.format("%.2fx", player.minesCurrentMultiplier) + " multiplier!");
        player.sendMessage("[@red@MINES@bla@] You won @gre@" 
            + CasinoManager.formatAmount(winnings) + " " + CasinoManager.CURRENCY_NAME + "@bla@!");

        CasinoManager.broadcast("[@red@MINES@bla@] @blu@" + player.getDisplayName() 
            + " @bla@cashed out at @gre@" + String.format("%.2fx", player.minesCurrentMultiplier) 
            + "@bla@ and won @gre@" + CasinoManager.formatAmount(winnings) + "@bla@!");

        CasinoManager.broadcastBigWin(player, "Mines", winnings);
    }

    /**
     * Show the current grid state to the player.
     */
    private static void showGrid(Player player) {
        player.sendMessage("[@red@MINES@bla@] ===== GRID =====");
        StringBuilder row = new StringBuilder();
        for (int i = 0; i < GRID_SIZE; i++) {
            if (i > 0 && i % 5 == 0) {
                player.sendMessage("[@red@MINES@bla@] " + row.toString());
                row = new StringBuilder();
            }
            if (player.minesRevealed[i]) {
                if (player.minesGrid[i]) {
                    row.append("@red@[X]@bla@ "); // Mine
                } else {
                    row.append("@gre@[").append(String.format("%02d", i + 1)).append("]@bla@ "); // Safe
                }
            } else {
                row.append("@bla@[").append(String.format("%02d", i + 1)).append("] "); // Hidden
            }
        }
        player.sendMessage("[@red@MINES@bla@] " + row.toString());
        player.sendMessage("[@red@MINES@bla@] ================");
    }

    /**
     * Reveal all tiles (on game end).
     */
    private static void revealAll(Player player) {
        for (int i = 0; i < GRID_SIZE; i++) {
            player.minesRevealed[i] = true;
        }
    }

    /**
     * Calculate the multiplier based on mines and safe tiles revealed.
     * Based on the probability of surviving each pick.
     * 
     * Formula: For each safe tile picked, the odds decrease.
     * Multiplier = (totalTiles / safeTiles) * (totalTiles-1 / safeTiles-1) * ... * houseEdge
     */
    static double calculateMultiplier(int mineCount, int safeRevealed) {
        double multiplier = 1.0;
        int totalTiles = GRID_SIZE;
        int safeTiles = totalTiles - mineCount;

        for (int i = 0; i < safeRevealed; i++) {
            double prob = (double) (safeTiles - i) / (totalTiles - i);
            multiplier *= (1.0 / prob);
        }

        return multiplier * HOUSE_EDGE_FACTOR;
    }

    /**
     * Clean up a player's mines game (e.g. on logout).
     * Wager is forfeited.
     */
    public static void cleanupPlayer(Player player) {
        if (player.minesActive) {
            player.minesActive = false;
            player.sendMessage("@red@Your mines game has been forfeited due to logout.");
        }
    }
}
