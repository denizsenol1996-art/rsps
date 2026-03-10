package io.xeros.content.casino;

import io.xeros.model.entity.player.Player;
import io.xeros.util.Misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Blackjack - Solo game vs a virtual dealer.
 * 
 * Usage: Player starts via ::blackjack [amount] or NPC dialogue.
 * Player gets two cards, dealer gets two (one hidden).
 * Player can HIT, STAND, or DOUBLE DOWN via dialogue options.
 * 
 * Pays 2x on win, 2.5x on blackjack (21 with 2 cards).
 * Push returns the wager.
 * 
 * @author T4C Casino System
 */
public class Blackjack {

    private static final double BLACKJACK_MULTIPLIER = 2.5;
    private static final double WIN_MULTIPLIER = 2.0;

    /** Card representation */
    private static final String[] SUITS = { "♠", "♥", "♦", "♣" };
    private static final String[] RANKS = { "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K" };

    /**
     * Start a new blackjack game for the player.
     */
    public static void startGame(Player player, int wager) {
        if (!CasinoManager.canGamble(player)) return;
        if (!CasinoManager.checkCooldown(player)) return;
        if (!CasinoManager.takeWager(player, wager)) return;

        // Create and shuffle deck
        List<Integer> deck = createDeck();
        Collections.shuffle(deck);

        // Deal initial cards
        List<Integer> playerHand = new ArrayList<>();
        List<Integer> dealerHand = new ArrayList<>();

        playerHand.add(deck.remove(0));
        dealerHand.add(deck.remove(0));
        playerHand.add(deck.remove(0));
        dealerHand.add(deck.remove(0));

        // Store game state on player
        player.bjDeck = deck;
        player.bjPlayerHand = playerHand;
        player.bjDealerHand = dealerHand;
        player.bjWager = wager;
        player.bjDoubled = false;
        player.bjActive = true;

        int playerTotal = handValue(playerHand);
        int dealerTotal = handValue(dealerHand);

        // Check for natural blackjack
        if (playerTotal == 21 && dealerTotal == 21) {
            // Push
            player.bjActive = false;
            CasinoManager.giveWinnings(player, wager);
            showResult(player, "PUSH", "Both have Blackjack! Wager returned.", 0);
            return;
        }
        if (playerTotal == 21) {
            // Player blackjack!
            player.bjActive = false;
            int winnings = (int) (wager * BLACKJACK_MULTIPLIER);
            CasinoManager.giveWinnings(player, winnings);
            showResult(player, "BLACKJACK!", "You hit 21! ", winnings);
            CasinoManager.broadcastBigWin(player, "Blackjack", winnings);
            return;
        }

        // Show initial hands and prompt for action
        showHands(player, true);
        promptAction(player);
    }

    /**
     * Player chooses to HIT (take another card).
     */
    public static void hit(Player player) {
        if (!player.bjActive) return;

        player.bjPlayerHand.add(player.bjDeck.remove(0));
        int total = handValue(player.bjPlayerHand);

        if (total > 21) {
            // Bust!
            player.bjActive = false;
            showHands(player, false);
            showResult(player, "BUST!", "You went over 21.", 0);
            return;
        }
        if (total == 21) {
            // Auto-stand on 21
            stand(player);
            return;
        }

        showHands(player, true);
        promptAction(player);
    }

    /**
     * Player chooses to STAND (keep current hand).
     */
    public static void stand(Player player) {
        if (!player.bjActive) return;
        player.bjActive = false;

        // Dealer draws to 17
        while (handValue(player.bjDealerHand) < 17) {
            player.bjDealerHand.add(player.bjDeck.remove(0));
        }

        int playerTotal = handValue(player.bjPlayerHand);
        int dealerTotal = handValue(player.bjDealerHand);
        int wager = player.bjDoubled ? player.bjWager * 2 : player.bjWager;

        showHands(player, false);

        if (dealerTotal > 21) {
            // Dealer busts
            int winnings = (int) (wager * WIN_MULTIPLIER);
            CasinoManager.giveWinnings(player, winnings);
            showResult(player, "DEALER BUSTS!", "Dealer went over 21!", winnings);
            CasinoManager.broadcastBigWin(player, "Blackjack", winnings);
        } else if (playerTotal > dealerTotal) {
            // Player wins
            int winnings = (int) (wager * WIN_MULTIPLIER);
            CasinoManager.giveWinnings(player, winnings);
            showResult(player, "YOU WIN!", "Your " + playerTotal + " beats dealer's " + dealerTotal + ".", winnings);
            CasinoManager.broadcastBigWin(player, "Blackjack", winnings);
        } else if (playerTotal == dealerTotal) {
            // Push
            CasinoManager.giveWinnings(player, wager);
            showResult(player, "PUSH", "Both have " + playerTotal + ". Wager returned.", 0);
        } else {
            // Dealer wins
            showResult(player, "DEALER WINS", "Dealer's " + dealerTotal + " beats your " + playerTotal + ".", 0);
        }
    }

    /**
     * Player chooses to DOUBLE DOWN (double wager, take one card, then stand).
     */
    public static void doubleDown(Player player) {
        if (!player.bjActive) return;
        if (player.bjPlayerHand.size() != 2) {
            player.sendMessage("@red@You can only double down on your first two cards.");
            promptAction(player);
            return;
        }

        // Take additional wager
        if (!CasinoManager.takeWager(player, player.bjWager)) {
            player.sendMessage("@red@Not enough " + CasinoManager.CURRENCY_NAME + " to double down.");
            promptAction(player);
            return;
        }

        player.bjDoubled = true;
        player.bjPlayerHand.add(player.bjDeck.remove(0));

        int total = handValue(player.bjPlayerHand);
        if (total > 21) {
            player.bjActive = false;
            showHands(player, false);
            showResult(player, "BUST!", "You went over 21 on double down.", 0);
            return;
        }

        // Auto-stand after double down
        stand(player);
    }

    /**
     * Show both hands to the player.
     * @param hideDealer If true, hide dealer's second card.
     */
    private static void showHands(Player player, boolean hideDealer) {
        StringBuilder sb = new StringBuilder();
        sb.append("[@red@BLACKJACK@bla@] ");
        sb.append("Your hand: ").append(formatHand(player.bjPlayerHand));
        sb.append(" (@gre@").append(handValue(player.bjPlayerHand)).append("@bla@)");
        player.sendMessage(sb.toString());

        sb = new StringBuilder();
        sb.append("[@red@BLACKJACK@bla@] ");
        sb.append("Dealer: ");
        if (hideDealer) {
            sb.append(cardName(player.bjDealerHand.get(0))).append(" [??]");
        } else {
            sb.append(formatHand(player.bjDealerHand));
            sb.append(" (@gre@").append(handValue(player.bjDealerHand)).append("@bla@)");
        }
        player.sendMessage(sb.toString());
    }

    /**
     * Prompt the player for their next action via dialogue.
     */
    private static void promptAction(Player player) {
        if (player.bjPlayerHand.size() == 2 
                && player.getItems().getItemAmount(CasinoManager.CURRENCY_ID) >= player.bjWager) {
            player.getDH().sendOption5(
                "Hit (take a card)",
                "Stand (keep hand)",
                "Double Down (double bet, one card)",
                "Forfeit (lose wager)",
                ""
            );
            player.casinoAction = CasinoAction.BLACKJACK_CHOICE_3;
        } else {
            player.getDH().sendOption2(
                "Hit (take a card)",
                "Stand (keep hand)"
            );
            player.casinoAction = CasinoAction.BLACKJACK_CHOICE_2;
        }
    }

    /**
     * Show the result of the game.
     */
    private static void showResult(Player player, String title, String message, int winnings) {
        player.sendMessage("[@red@BLACKJACK@bla@] @gre@" + title + " @bla@" + message);
        if (winnings > 0) {
            player.sendMessage("[@red@BLACKJACK@bla@] You won @gre@" 
                + CasinoManager.formatAmount(winnings) + " " + CasinoManager.CURRENCY_NAME + "@bla@!");
        }
        CasinoManager.broadcast("[@red@BLACKJACK@bla@] @blu@" + player.getDisplayName() 
            + " @bla@" + (winnings > 0 ? "won @gre@" + CasinoManager.formatAmount(winnings) : "lost") 
            + "@bla@ (" + title + ")");
    }

    // ==================== Card Utilities ====================

    private static List<Integer> createDeck() {
        List<Integer> deck = new ArrayList<>();
        // 6 decks for blackjack (standard casino)
        for (int d = 0; d < 6; d++) {
            for (int i = 0; i < 52; i++) {
                deck.add(i);
            }
        }
        return deck;
    }

    /** Get the value of a hand, handling aces optimally. */
    public static int handValue(List<Integer> hand) {
        int value = 0;
        int aces = 0;
        for (int card : hand) {
            int rank = card % 13;
            if (rank == 0) { // Ace
                aces++;
                value += 11;
            } else if (rank >= 10) { // J, Q, K
                value += 10;
            } else {
                value += rank + 1;
            }
        }
        while (value > 21 && aces > 0) {
            value -= 10;
            aces--;
        }
        return value;
    }

    /** Format a card as a string (e.g. "K♠"). */
    private static String cardName(int card) {
        int rank = card % 13;
        int suit = card / 13 % 4;
        return RANKS[rank] + SUITS[suit];
    }

    /** Format a hand as a string. */
    private static String formatHand(List<Integer> hand) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hand.size(); i++) {
            if (i > 0) sb.append(" ");
            sb.append(cardName(hand.get(i)));
        }
        return sb.toString();
    }
}
