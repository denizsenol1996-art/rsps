package io.xeros.content.commands.all;

import java.util.Optional;
import io.xeros.content.casino.CasinoCmdUtil;
import io.xeros.content.casino.CoinFlip;
import io.xeros.content.commands.Command;
import io.xeros.model.entity.player.Player;

public class Coinflip extends Command {
    @Override
    public void execute(Player c, String commandName, String input) {
        if (input == null || input.isEmpty()) {
            c.sendMessage("Usage: ::coinflip [amount] - create a coinflip game");
            c.sendMessage("       ::cflist - see active games");
            c.sendMessage("       ::cfjoin [player] - join a game");
            c.sendMessage("       ::cfcancel - cancel your listing");
            return;
        }
        try {
            int amount = CasinoCmdUtil.parseAmount(input.trim());
            CoinFlip.createGame(c, amount);
        } catch (NumberFormatException e) {
            c.sendMessage("@red@Invalid amount. Use: ::coinflip 100k");
        }
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Create a coinflip game - ::coinflip [amount]");
    }
}
