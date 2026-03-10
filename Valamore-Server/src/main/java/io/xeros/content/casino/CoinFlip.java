package io.xeros.content.casino;

import io.xeros.Server;
import io.xeros.model.entity.player.Player;
import io.xeros.model.entity.player.PlayerHandler;
import io.xeros.util.Misc;

import java.util.HashMap;
import java.util.Map;

/**
 * Coinflip - 1v1 game. One player creates a game with a wager,
 * another player accepts. Winner takes all (2x minus optional tax).
 * 
 * Usage: 
 *   ::coinflip [amount]  - Create a coinflip listing
 *   ::cfjoin [player]    - Join someone's coinflip
 *   ::cflist             - Show active coinflip games
 *   ::cfcancel           - Cancel your listing
 * 
 * The challenger automatically gets "heads", the accepter gets "tails".
 * Result is determined by SecureRandom for fairness.
 * 
 * @author T4C Casino System
 */
public class CoinFlip {

    /** Active coinflip listings: player name -> wager amount */
    private static final Map<String, CoinFlipGame> activeGames = new HashMap<>();

    /** House edge percentage (0 = no edge, 5 = 5% tax on winnings) */
    private static final double HOUSE_EDGE = 0.0;

    /**
     * Create a new coinflip listing.
     */
    public static void createGame(Player player, int wager) {
        if (!CasinoManager.canGamble(player)) return;

        String name = player.getDisplayName().toLowerCase();
        if (activeGames.containsKey(name)) {
            player.sendMessage("@red@You already have an active coinflip. Use ::cfcancel to cancel it.");
            return;
        }

        if (!CasinoManager.takeWager(player, wager)) return;

        activeGames.put(name, new CoinFlipGame(player.getDisplayName(), wager));
        player.sendMessage("[@yel@COINFLIP@bla@] Your coinflip for @gre@" 
            + CasinoManager.formatAmount(wager) + "@bla@ has been listed!");
        
        CasinoManager.broadcast("[@yel@COINFLIP@bla@] @blu@" + player.getDisplayName() 
            + " @bla@is looking for a coinflip: @gre@" + CasinoManager.formatAmount(wager) 
            + " " + CasinoManager.CURRENCY_NAME + "@bla@! Type ::cfjoin " + player.getDisplayName());
    }

    /**
     * Join an existing coinflip game.
     */
    public static void joinGame(Player player, String targetName) {
        if (!CasinoManager.canGamble(player)) return;

        String key = targetName.toLowerCase();
        CoinFlipGame game = activeGames.get(key);

        if (game == null) {
            player.sendMessage("@red@That player doesn't have an active coinflip.");
            return;
        }

        if (game.creatorName.equalsIgnoreCase(player.getDisplayName())) {
            player.sendMessage("@red@You can't join your own coinflip.");
            return;
        }

        if (!CasinoManager.takeWager(player, game.wager)) return;

        // Remove the listing
        activeGames.remove(key);

        // Find the creator
        Player creator = PlayerHandler.nonNullStream()
            .filter(p -> p.getDisplayName().equalsIgnoreCase(game.creatorName))
            .findFirst().orElse(null);

        if (creator == null) {
            // Creator is offline, refund both
            CasinoManager.giveWinnings(player, game.wager);
            player.sendMessage("@red@The other player has gone offline. Your wager has been refunded.");
            return;
        }

        // Execute the flip
        executeFlip(creator, player, game.wager);
    }

    /**
     * Execute the coinflip between two players.
     */
    private static void executeFlip(Player creator, Player joiner, int wager) {
        // Animation
        creator.startAnimation(2106); // Emote animation
        joiner.startAnimation(2106);

        // Random result: true = heads (creator wins), false = tails (joiner wins)
        boolean heads = new java.security.SecureRandom().nextBoolean();

        Player winner = heads ? creator : joiner;
        Player loser = heads ? joiner : creator;
        String result = heads ? "HEADS" : "TAILS";

        // Calculate winnings
        int totalPot = wager * 2;
        int tax = (int) (totalPot * HOUSE_EDGE / 100);
        int winnings = totalPot - tax;

        // Pay winner
        CasinoManager.giveWinnings(winner, winnings);

        // Announce
        String msg = "[@yel@COINFLIP@bla@] @red@" + result + "! @blu@" + winner.getDisplayName() 
            + " @bla@wins @gre@" + CasinoManager.formatAmount(winnings) + " " + CasinoManager.CURRENCY_NAME 
            + "@bla@ against @blu@" + loser.getDisplayName() + "@bla@!";

        creator.sendMessage(msg);
        joiner.sendMessage(msg);
        CasinoManager.broadcast(msg);

        winner.forcedChat("I just won " + CasinoManager.formatAmount(winnings) + " on a coinflip!");

        CasinoManager.broadcastBigWin(winner, "Coinflip", winnings);
    }

    /**
     * Cancel an active coinflip listing.
     */
    public static void cancelGame(Player player) {
        String name = player.getDisplayName().toLowerCase();
        CoinFlipGame game = activeGames.remove(name);
        if (game != null) {
            CasinoManager.giveWinnings(player, game.wager);
            player.sendMessage("[@yel@COINFLIP@bla@] Your coinflip has been cancelled. Wager refunded.");
        } else {
            player.sendMessage("@red@You don't have an active coinflip.");
        }
    }

    /**
     * List all active coinflip games.
     */
    public static void listGames(Player player) {
        if (activeGames.isEmpty()) {
            player.sendMessage("[@yel@COINFLIP@bla@] No active coinflip games. Create one with ::coinflip [amount]");
            return;
        }
        player.sendMessage("[@yel@COINFLIP@bla@] === Active Coinflips ===");
        for (CoinFlipGame game : activeGames.values()) {
            player.sendMessage("  @blu@" + game.creatorName + "@bla@ - @gre@" 
                + CasinoManager.formatAmount(game.wager) + " " + CasinoManager.CURRENCY_NAME 
                + "@bla@ (::cfjoin " + game.creatorName + ")");
        }
    }

    /**
     * Clean up games from offline players.
     * Call this periodically (e.g. on player logout).
     */
    public static void cleanupPlayer(Player player) {
        String name = player.getDisplayName().toLowerCase();
        CoinFlipGame game = activeGames.remove(name);
        if (game != null) {
            // Refund wager on logout
            CasinoManager.giveWinnings(player, game.wager);
        }
    }

    /** Simple data class for a coinflip listing */
    private static class CoinFlipGame {
        final String creatorName;
        final int wager;

        CoinFlipGame(String creatorName, int wager) {
            this.creatorName = creatorName;
            this.wager = wager;
        }
    }
}
