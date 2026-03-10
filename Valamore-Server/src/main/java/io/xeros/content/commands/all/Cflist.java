package io.xeros.content.commands.all;

import java.util.Optional;
import io.xeros.content.casino.CoinFlip;
import io.xeros.content.commands.Command;
import io.xeros.model.entity.player.Player;

public class Cflist extends Command {
    @Override
    public void execute(Player c, String commandName, String input) {
        CoinFlip.listGames(c);
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("List active coinflip games");
    }
}
