package io.xeros.content.commands.all;

import java.util.Optional;

import io.xeros.Server;
import io.xeros.content.casino.CasinoDialogueHandler;
import io.xeros.content.casino.CasinoManager;
import io.xeros.content.commands.Command;
import io.xeros.model.entity.player.Player;

/**
 * Teleport the player to the casino area.
 */
public class Casino extends Command {

    @Override
    public void execute(Player c, String commandName, String input) {
        if (Server.getMultiplayerSessionListener().inAnySession(c)) {
            return;
        }
        if (c.getPosition().inClanWars() || c.getPosition().inClanWarsSafe()) {
            c.sendMessage("@cr10@You can not teleport from here.");
            return;
        }
        if (c.getPosition().inWild()) {
            c.sendMessage("You can't use this command in the wilderness.");
            return;
        }
        c.getPA().spellTeleport(CasinoManager.CASINO_X, CasinoManager.CASINO_Y, CasinoManager.CASINO_Z, false);
        c.sendMessage("[@red@CASINO@bla@] Welcome to the Casino! Games: ::blackjack, ::slots, ::mines, ::coinflip");
        c.sendMessage("[@red@CASINO@bla@] Or talk to the Casino Dealer NPC for a menu.");
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Teleports you to the casino area");
    }
}
