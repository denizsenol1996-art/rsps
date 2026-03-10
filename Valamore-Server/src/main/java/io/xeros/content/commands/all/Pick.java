package io.xeros.content.commands.all;

import java.util.Optional;
import io.xeros.content.casino.MinesGame;
import io.xeros.content.commands.Command;
import io.xeros.model.entity.player.Player;

public class Pick extends Command {
    @Override
    public void execute(Player c, String commandName, String input) {
        if (input == null || input.isEmpty()) {
            c.sendMessage("Usage: ::pick [1-25] - pick a tile in mines");
            return;
        }
        try {
            int tile = Integer.parseInt(input.trim());
            MinesGame.pickTile(c, tile);
        } catch (NumberFormatException e) {
            c.sendMessage("@red@Invalid tile. Use: ::pick [1-25]");
        }
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Pick a tile in mines - ::pick [1-25]");
    }
}
