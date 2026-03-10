package io.xeros.content.casino;

import io.xeros.model.entity.player.Player;
import io.xeros.util.Misc;

import java.security.SecureRandom;

/**
 * Slots Machine - Solo game. Spin 3 reels and match symbols.
 * 
 * Usage: ::slots [amount]
 * 
 * Pay table:
 *   3x 7s     = 50x
 *   3x Diamond = 25x
 *   3x Cherry  = 10x
 *   3x Bar     = 5x
 *   3x Bell    = 3x
 *   2x match   = 1.5x (returns bet + 50%)
 *   No match   = lose
 * 
 * @author T4C Casino System
 */
public class SlotsGame {

    private static final SecureRandom random = new SecureRandom();

    /** Slot symbols with their weights (higher weight = more common) */
    enum Symbol {
        SEVEN("@red@7", 5),
        DIAMOND("@cya@<>", 8),
        CHERRY("@red@CH", 12),
        BAR("@whi@BA", 18),
        BELL("@yel@BL", 22),
        LEMON("@yel@LM", 35);

        final String display;
        final int weight;

        Symbol(String display, int weight) {
            this.display = display;
            this.weight = weight;
        }
    }

    /** Total weight of all symbols */
    private static final int TOTAL_WEIGHT;
    static {
        int total = 0;
        for (Symbol s : Symbol.values()) total += s.weight;
        TOTAL_WEIGHT = total;
    }

    /**
     * Spin the slot machine.
     */
    public static void spin(Player player, int wager) {
        if (!CasinoManager.canGamble(player)) return;
        if (!CasinoManager.checkCooldown(player)) return;
        if (!CasinoManager.takeWager(player, wager)) return;

        // Spin 3 reels
        Symbol reel1 = spinReel();
        Symbol reel2 = spinReel();
        Symbol reel3 = spinReel();

        // Animation
        player.startAnimation(2106);

        // Display the spin
        String display = "[@mag@SLOTS@bla@] [ " + reel1.display + "@bla@ | " 
            + reel2.display + "@bla@ | " + reel3.display + "@bla@ ]";
        player.sendMessage(display);

        // Calculate result
        double multiplier = getMultiplier(reel1, reel2, reel3);
        String resultName = getResultName(reel1, reel2, reel3);

        if (multiplier > 0) {
            int winnings = (int) (wager * multiplier);
            CasinoManager.giveWinnings(player, winnings);

            player.sendMessage("[@mag@SLOTS@bla@] @gre@" + resultName + "! " 
                + String.format("%.1fx", multiplier) + " multiplier!");
            player.sendMessage("[@mag@SLOTS@bla@] You won @gre@" 
                + CasinoManager.formatAmount(winnings) + " " + CasinoManager.CURRENCY_NAME + "@bla@!");

            if (multiplier >= 10.0) {
                player.forcedChat("JACKPOT! " + CasinoManager.formatAmount(winnings) + " on slots!");
            }

            CasinoManager.broadcast("[@mag@SLOTS@bla@] @blu@" + player.getDisplayName() 
                + " @bla@won @gre@" + CasinoManager.formatAmount(winnings) 
                + "@bla@ on slots! (" + resultName + " - " + String.format("%.1fx", multiplier) + ")");

            CasinoManager.broadcastBigWin(player, "Slots", winnings);
        } else {
            player.sendMessage("[@mag@SLOTS@bla@] @red@No match. Better luck next time!");
            CasinoManager.broadcast("[@mag@SLOTS@bla@] @blu@" + player.getDisplayName() 
                + " @bla@lost @red@" + CasinoManager.formatAmount(wager) + "@bla@ on slots.");
        }
    }

    /**
     * Spin a single reel using weighted random.
     */
    private static Symbol spinReel() {
        int roll = random.nextInt(TOTAL_WEIGHT);
        int cumulative = 0;
        for (Symbol s : Symbol.values()) {
            cumulative += s.weight;
            if (roll < cumulative) return s;
        }
        return Symbol.LEMON; // Fallback
    }

    /**
     * Get the multiplier for a spin result.
     */
    private static double getMultiplier(Symbol a, Symbol b, Symbol c) {
        // Three of a kind
        if (a == b && b == c) {
            switch (a) {
                case SEVEN:   return 50.0;
                case DIAMOND: return 25.0;
                case CHERRY:  return 10.0;
                case BAR:     return 5.0;
                case BELL:    return 3.0;
                case LEMON:   return 2.0;
                default:      return 2.0;
            }
        }

        // Two of a kind
        if (a == b || b == c || a == c) {
            Symbol matched = (a == b) ? a : c;
            switch (matched) {
                case SEVEN:   return 3.0;
                case DIAMOND: return 2.0;
                case CHERRY:  return 1.5;
                default:      return 1.2;
            }
        }

        // No match
        return 0;
    }

    /**
     * Get a descriptive name for the result.
     */
    private static String getResultName(Symbol a, Symbol b, Symbol c) {
        if (a == b && b == c) {
            switch (a) {
                case SEVEN:   return "TRIPLE SEVENS - JACKPOT";
                case DIAMOND: return "TRIPLE DIAMONDS";
                case CHERRY:  return "TRIPLE CHERRIES";
                case BAR:     return "TRIPLE BARS";
                case BELL:    return "TRIPLE BELLS";
                case LEMON:   return "TRIPLE LEMONS";
                default:      return "THREE OF A KIND";
            }
        }
        if (a == b || b == c || a == c) {
            return "PAIR";
        }
        return "NO MATCH";
    }
}
