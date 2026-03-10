package io.xeros.model.entity.player.packets;

import java.util.Objects;

import io.xeros.Server;
import io.xeros.content.lootbag.LootingBag;
import io.xeros.model.cycleevent.CycleEvent;
import io.xeros.model.cycleevent.CycleEventContainer;
import io.xeros.model.cycleevent.CycleEventHandler;
import io.xeros.model.entity.groundItem.GroundItemManager;
import io.xeros.model.entity.groundItem.GroundItem;
import io.xeros.model.entity.player.*;
import io.xeros.model.entity.player.mode.group.GroupIronmanGroup;
import io.xeros.model.entity.player.mode.group.GroupIronmanRepository;
import io.xeros.model.multiplayersession.MultiplayerSessionFinalizeType;
import io.xeros.model.multiplayersession.MultiplayerSessionStage;
import io.xeros.model.multiplayersession.MultiplayerSessionType;
import io.xeros.model.multiplayersession.duel.DuelSession;

/**
 * Pickup Item
 **/
public class PickupItem implements PacketType {

    @Override
    public void processPacket(final Player player, int packetType, int packetSize) {
        if (player.getMovementState().isLocked() || player.getLock().cannotInteract(player))
            return;
        if (player.isFping()) {
            return;
        }
        player.interruptActions();
        player.walkingToItem = false;

        int y = player.getInStream().readSignedWordBigEndian();
        player.itemId = player.getInStream().readUnsignedWord();
        int x = player.getInStream().readSignedWordBigEndian();
        Position position = new Position(x,y);

        if (Math.abs(player.getX() - position.getX()) > 25 || Math.abs(player.getY() - position.getY()) > 25) {
            player.resetWalkingQueue();
            return;
        }
        DuelSession duelSession = (DuelSession) Server.getMultiplayerSessionListener().getMultiplayerSession(player, MultiplayerSessionType.DUEL);
        if (Objects.nonNull(duelSession) && duelSession.getStage().getStage() > MultiplayerSessionStage.REQUEST
                && duelSession.getStage().getStage() < MultiplayerSessionStage.FURTHER_INTERATION) {
            player.sendMessage("Your actions have declined the duel.");
            duelSession.getOther(player).sendMessage("The challenger has declined the duel.");
            duelSession.finish(MultiplayerSessionFinalizeType.WITHDRAW_ITEMS);
            return;
        }
        if (player.itemId == LootingBag.LOOTING_BAG && (player.getItems().getItemCount(LootingBag.LOOTING_BAG, true) >= 1
                || player.getItems().getItemCount(LootingBag.LOOTING_BAG_OPEN, true) >= 1)) {
            player.sendMessage("You cannot own multiples of this item.");
            return;
        }
        if (player.itemId == 12791 && player.getItems().getItemCount(12791, true) > 1) {
            player.sendMessage("You cannot own multiples of this item.");
            return;
        }
        if (player.getBankPin().requiresUnlock()) {
            player.getBankPin().open(2);
            return;
        }
        if (player.isStuck) {
            player.isStuck = false;
            player.sendMessage("@red@You've disrupted stuck command, you will no longer be moved home.");
            return;
        }
        if (player.isNpc) {
            return;
        }
        if ((Boundary.isIn(player, Boundary.OUTLAST_AREA) || Boundary.isIn(player, Boundary.LUMBRIDGE_OUTLAST_AREA)) && player.spectatingTournament) {
            return;
        }
        if (player.isDead || player.getHealth().getCurrentHealth() <= 0) {
            return;
        }

        GroundItem groundItem = GroundItemManager.INSTANCE.getItemWithId(position, player.itemId);

        if (groundItem == null) {
            return;
        }

        if (!player.getMode().isItemScavengingPermitted()) {
            String ownerName = groundItem.getOwner().get().getDisplayName();

            Player owner = PlayerHandler.getPlayerByLoginName(ownerName);
            GroupIronmanGroup group = GroupIronmanRepository.getGroupForOnline(player).orElse(null);
            if (owner == null || group == null && !player.getLoginNameLower().equalsIgnoreCase(ownerName) || group != null && player.isApartOfGroupIronmanGroup() && !group.isGroupMember(owner)) {
                player.sendMessage("Your mode restricts you from picking up items that are not yours.");
                return;
            }
        }

        if (player.getInterfaceEvent().isActive()) {
            player.sendMessage("Please finish what you're doing.");
            return;
        }
        if (player.getPA().viewingOtherBank) {
            player.getPA().resetOtherBank();
        }
        player.attacking.reset();
        if (player.teleportingToDistrict) {
            return;
        }
        if (player.getPosition().inClanWars() || player.getPosition().inClanWarsSafe()) {
            if (!player.pkDistrict) {
                return;
            }
        }


        if (position.isWithinDistance(player.getPosition(),1)) {
            pickup(player,groundItem);
        } else {
            player.walkingToItem = true;
            CycleEventHandler.getSingleton().addEvent(player, new CycleEvent() {
                @Override
                public void execute(CycleEventContainer container) {
                    if (!player.walkingToItem) {
                        container.stop();
                        return;
                    }
                    if (position.isWithinDistance(player.getPosition(),1)) {
                        pickup(player,groundItem);
                        container.stop();
                    }
                }

                @Override
                public void onStopped() {
                    player.walkingToItem = false;
                }
            }, 1);
        }

    }

    private void pickup(Player c, GroundItem groundItem) {
        GroundItemManager.INSTANCE.pickupItem(c, groundItem);
        c.getPA().sendSound(2582);
    }

}
