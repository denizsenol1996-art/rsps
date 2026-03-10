package io.xeros.content.casino;

/**
 * Tracks which casino dialogue/action a player is currently in.
 * Used by the dialogue option handler to route clicks to the correct game.
 */
public enum CasinoAction {
    NONE,

    /** Blackjack - 2 option dialogue (hit/stand) */
    BLACKJACK_CHOICE_2,

    /** Blackjack - 3 option dialogue (hit/stand/double) */
    BLACKJACK_CHOICE_3,

    /** Coinflip - waiting for opponent to accept */
    COINFLIP_PENDING,

    /** Coinflip - choose heads or tails */
    COINFLIP_CHOOSE,

    /** Mines - choose number of mines (difficulty) */
    MINES_DIFFICULTY,

    /** Mines - choosing a tile */
    MINES_PLAYING,

    /** Mines - cashout or continue */
    MINES_CASHOUT_CHOICE,

    /** Slots - choose bet amount */
    SLOTS_BET,

    /** Casino NPC main menu */
    CASINO_MENU,

    /** Casino NPC game selection */
    CASINO_GAME_SELECT,

    /** Casino NPC bet amount selection */
    CASINO_BET_SELECT
}
