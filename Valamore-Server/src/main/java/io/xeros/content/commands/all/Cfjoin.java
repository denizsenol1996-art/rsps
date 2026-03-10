package io.xeros.content.commands.all;

import java.util.Optional;
import io.xeros.content.casino.CoinFlip;
import io.xeros.content.commands.Command;
import io.xeros.model.entity.player.Player;

public class Cfjoin extends Command {
    @Override
    public void execute(Player c, String commandName, String input) {
        if (input == null || input.isEmpty()) {
            c.sendMessage("Usage: ::cfjoin [player name]");
            return;
        }
        CoinFlip.joinGame(c, input.trim());
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Join a coinflip game - ::cfjoin [player]");
    }
}
