package io.xeros.content.commands.all;

import java.util.Optional;
import io.xeros.content.casino.CasinoCmdUtil;
import io.xeros.content.casino.SlotsGame;
import io.xeros.content.commands.Command;
import io.xeros.model.entity.player.Player;

public class Slots extends Command {
    @Override
    public void execute(Player c, String commandName, String input) {
        if (input == null || input.isEmpty()) {
            c.sendMessage("Usage: ::slots [amount] - e.g. ::slots 50k");
            return;
        }
        try {
            int amount = CasinoCmdUtil.parseAmount(input.trim());
            SlotsGame.spin(c, amount);
        } catch (NumberFormatException e) {
            c.sendMessage("@red@Invalid amount. Use: ::slots 50k");
        }
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Play slots - ::slots [amount]");
    }
}
