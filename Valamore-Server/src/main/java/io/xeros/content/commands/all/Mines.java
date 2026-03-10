package io.xeros.content.commands.all;

import java.util.Optional;
import io.xeros.content.casino.CasinoCmdUtil;
import io.xeros.content.casino.MinesGame;
import io.xeros.content.commands.Command;
import io.xeros.model.entity.player.Player;

/**
 * Start a mines game.
 * Usage: ::mines [amount] or ::mines [amount] [mine_count]
 */
public class Mines extends Command {
    @Override
    public void execute(Player c, String commandName, String input) {
        if (input == null || input.isEmpty()) {
            c.sendMessage("Usage: ::mines [amount] - e.g. ::mines 100k");
            c.sendMessage("       ::mines [amount] [mines] - e.g. ::mines 100k 5");
            return;
        }
        try {
            String[] parts = input.trim().split("\\s+");
            int amount = CasinoCmdUtil.parseAmount(parts[0]);
            if (parts.length >= 2) {
                int mineCount = Integer.parseInt(parts[1]);
                MinesGame.startGame(c, amount, mineCount);
            } else {
                MinesGame.startGame(c, amount);
            }
        } catch (NumberFormatException e) {
            c.sendMessage("@red@Invalid input. Use: ::mines 100k or ::mines 100k 5");
        }
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Play mines - ::mines [amount] [mine_count]");
    }
}
