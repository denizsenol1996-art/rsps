package io.xeros.content.commands.all;

import java.util.Optional;
import io.xeros.content.casino.CasinoCmdUtil;
import io.xeros.content.commands.Command;
import io.xeros.model.entity.player.Player;

public class Blackjack extends Command {
    @Override
    public void execute(Player c, String commandName, String input) {
        if (input == null || input.isEmpty()) {
            c.sendMessage("Usage: ::blackjack [amount] - e.g. ::blackjack 100k");
            return;
        }
        try {
            int amount = CasinoCmdUtil.parseAmount(input.trim());
            io.xeros.content.casino.Blackjack.startGame(c, amount);
        } catch (NumberFormatException e) {
            c.sendMessage("@red@Invalid amount. Use: ::blackjack 100k");
        }
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Play blackjack - ::blackjack [amount]");
    }
}
