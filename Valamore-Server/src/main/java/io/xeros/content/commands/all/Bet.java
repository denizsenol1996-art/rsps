package io.xeros.content.commands.all;

import java.util.Optional;
import io.xeros.content.casino.CasinoCmdUtil;
import io.xeros.content.casino.CasinoDialogueHandler;
import io.xeros.content.commands.Command;
import io.xeros.model.entity.player.Player;

/**
 * Set a custom bet amount and start the selected casino game.
 * Used after selecting a game from the Casino Dealer NPC.
 */
public class Bet extends Command {
    @Override
    public void execute(Player c, String commandName, String input) {
        if (input == null || input.isEmpty()) {
            c.sendMessage("Usage: ::bet [amount] - e.g. ::bet 500k");
            return;
        }
        try {
            int amount = CasinoCmdUtil.parseAmount(input.trim());
            CasinoDialogueHandler.startSelectedGame(c, amount);
        } catch (NumberFormatException e) {
            c.sendMessage("@red@Invalid amount. Use: ::bet 500k");
        }
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Set custom casino bet - ::bet [amount]");
    }
}
