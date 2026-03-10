package io.xeros.content.casino;

import io.xeros.model.entity.player.Player;

/**
 * Handles dialogue option clicks for casino games.
 * 
 * INTEGRATION: Add this call to your main dialogue option handler.
 * In the file that handles actionButtonId clicks (e.g. Dialogue.java or ClickingButtons.java),
 * add at the top of the method:
 * 
 *   if (CasinoDialogueHandler.handleClick(player, actionButtonId)) return;
 * 
 * @author T4C Casino System
 */
public class CasinoDialogueHandler {

    /**
     * Handle a dialogue button click.
     * @return true if handled by the casino system
     */
    public static boolean handleClick(Player player, int actionButtonId) {
        if (player.casinoAction == null || player.casinoAction == CasinoAction.NONE) {
            return false;
        }

        switch (player.casinoAction) {
            case BLACKJACK_CHOICE_2:
                return handleBlackjack2(player, actionButtonId);
            case BLACKJACK_CHOICE_3:
                return handleBlackjack3(player, actionButtonId);
            case CASINO_GAME_SELECT:
                return handleGameSelect(player, actionButtonId);
            case CASINO_BET_SELECT:
                return handleBetSelect(player, actionButtonId);
            default:
                return false;
        }
    }

    /**
     * Blackjack: 2-option dialogue (Hit / Stand)
     * Option button IDs: 9190 = option 1, 9191 = option 2
     */
    private static boolean handleBlackjack2(Player player, int actionButtonId) {
        player.casinoAction = CasinoAction.NONE;
        player.getPA().removeAllWindows();

        switch (actionButtonId) {
            case 9190: // Hit
                Blackjack.hit(player);
                return true;
            case 9191: // Stand
                Blackjack.stand(player);
                return true;
            default:
                return false;
        }
    }

    /**
     * Blackjack: 3-option dialogue (Hit / Stand / Double Down / Forfeit)
     */
    private static boolean handleBlackjack3(Player player, int actionButtonId) {
        player.casinoAction = CasinoAction.NONE;
        player.getPA().removeAllWindows();

        switch (actionButtonId) {
            case 9190: // Hit
                Blackjack.hit(player);
                return true;
            case 9191: // Stand
                Blackjack.stand(player);
                return true;
            case 9192: // Double Down
                Blackjack.doubleDown(player);
                return true;
            case 9193: // Forfeit
                player.bjActive = false;
                player.sendMessage("[@red@BLACKJACK@bla@] You forfeited your hand.");
                return true;
            default:
                return false;
        }
    }

    /**
     * Casino NPC: Game selection menu
     */
    private static boolean handleGameSelect(Player player, int actionButtonId) {
        player.casinoAction = CasinoAction.NONE;
        player.getPA().removeAllWindows();

        switch (actionButtonId) {
            case 9190: // Blackjack
                player.casinoSelectedGame = "blackjack";
                showBetMenu(player);
                return true;
            case 9191: // Slots
                player.casinoSelectedGame = "slots";
                showBetMenu(player);
                return true;
            case 9192: // Mines
                player.casinoSelectedGame = "mines";
                showBetMenu(player);
                return true;
            case 9193: // Coinflip info
                player.sendMessage("[@yel@COINFLIP@bla@] Use ::coinflip [amount] to create a game.");
                player.sendMessage("[@yel@COINFLIP@bla@] Use ::cflist to see active games, ::cfjoin [name] to join one.");
                return true;
            case 9194: // Cancel
                return true;
            default:
                return false;
        }
    }

    /**
     * Show bet amount selection.
     */
    private static void showBetMenu(Player player) {
        player.getDH().sendOption5(
            "10K " + CasinoManager.CURRENCY_NAME,
            "100K " + CasinoManager.CURRENCY_NAME,
            "1M " + CasinoManager.CURRENCY_NAME,
            "10M " + CasinoManager.CURRENCY_NAME,
            "Custom amount (type ::bet [amount])"
        );
        player.casinoAction = CasinoAction.CASINO_BET_SELECT;
    }

    /**
     * Casino NPC: Bet amount selection
     */
    private static boolean handleBetSelect(Player player, int actionButtonId) {
        player.casinoAction = CasinoAction.NONE;
        player.getPA().removeAllWindows();

        int bet = 0;
        switch (actionButtonId) {
            case 9190: bet = 10_000; break;
            case 9191: bet = 100_000; break;
            case 9192: bet = 1_000_000; break;
            case 9193: bet = 10_000_000; break;
            case 9194: // Custom
                player.sendMessage("Type ::bet [amount] to set a custom bet, then interact with the dealer again.");
                return true;
            default:
                return false;
        }

        startSelectedGame(player, bet);
        return true;
    }

    /**
     * Start the previously selected game with the given bet.
     */
    public static void startSelectedGame(Player player, int bet) {
        if (player.casinoSelectedGame == null) {
            player.sendMessage("@red@No game selected. Talk to the Casino Dealer.");
            return;
        }

        switch (player.casinoSelectedGame) {
            case "blackjack":
                Blackjack.startGame(player, bet);
                break;
            case "slots":
                SlotsGame.spin(player, bet);
                break;
            case "mines":
                MinesGame.startGame(player, bet);
                break;
            default:
                player.sendMessage("@red@Unknown game. Talk to the Casino Dealer.");
                break;
        }
        player.casinoSelectedGame = null;
    }

    /**
     * Show the main casino game selection menu.
     * Called when interacting with the Casino Dealer NPC.
     */
    public static void showGameMenu(Player player) {
        if (!CasinoManager.canGamble(player)) return;

        player.getDH().sendOption5(
            "Blackjack (solo vs dealer)",
            "Slots Machine",
            "Mines (5x5 grid)",
            "Coinflip (1v1 info)",
            "Nevermind"
        );
        player.casinoAction = CasinoAction.CASINO_GAME_SELECT;
    }
}
