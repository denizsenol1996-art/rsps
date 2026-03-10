package io.xeros.content.commands.all;

import java.util.Optional;
import io.xeros.content.casino.CoinFlip;
import io.xeros.content.commands.Command;
import io.xeros.model.entity.player.Player;

public class Cfcancel extends Command {
    @Override
    public void execute(Player c, String commandName, String input) {
        CoinFlip.cancelGame(c);
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Cancel your active coinflip listing");
    }
}
