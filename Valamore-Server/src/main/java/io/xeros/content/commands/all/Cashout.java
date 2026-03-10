package io.xeros.content.commands.all;

import java.util.Optional;
import io.xeros.content.casino.MinesGame;
import io.xeros.content.commands.Command;
import io.xeros.model.entity.player.Player;

public class Cashout extends Command {
    @Override
    public void execute(Player c, String commandName, String input) {
        MinesGame.cashout(c);
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Cash out in mines game");
    }
}
