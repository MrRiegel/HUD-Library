package mrriegel.hudlibrary;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import mrriegel.hudlibrary.tehud.HUDSyncMessage;
import mrriegel.hudlibrary.worldgui.PlayerData;
import mrriegel.hudlibrary.worldgui.WorldGui;
import mrriegel.hudlibrary.worldgui.WorldGuiCapability;
import mrriegel.hudlibrary.worldgui.WorldGuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(modid = HUDLibrary.MODID)
public class CommonEvents {

	public static Set<UUID> openWorldGuis = new HashSet<>();
	private static Map<String, PlayerData> playerDatas = new HashMap<>();

	public static PlayerData getData(EntityPlayer player) {
		String key = player.getUniqueID() + "" + (player.world.isRemote ? Side.CLIENT : Side.SERVER);
//		key = player.getUniqueID().toString();
		PlayerData data = playerDatas.get(key);
		if (data != null)
			return data;
		playerDatas.put(key, data = new PlayerData());
		return data;
	}

	@SubscribeEvent
	public static void tick(PlayerTickEvent event) {
		if (event.side.isServer() && event.phase == Phase.END) {
			if (event.player.ticksExisted % 7 == 0) {
				HUDLibrary.snw.sendTo(new HUDSyncMessage(event.player, 7), (EntityPlayerMP) event.player);
			}
			for (WorldGuiContainer c : getData(event.player).containers.values())
				c.detectAndSendChanges();
		}
	}

	@SubscribeEvent
	public static void join(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityPlayer && !event.getWorld().isRemote) {
			HUDLibrary.snw.sendTo(new HUDSyncMessage((EntityPlayer) event.getEntity(), 7), (EntityPlayerMP) event.getEntity());
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void interact(PlayerInteractEvent event) {
		if (openWorldGuis.contains(event.getEntityPlayer().getUniqueID())) {
			if (event.isCancelable())
				event.setCanceled(true);
			event.setResult(Result.DENY);
			if (event instanceof RightClickBlock) {
				((RightClickBlock) event).setUseBlock(Result.DENY);
				((RightClickBlock) event).setUseItem(Result.DENY);
			} else if (event instanceof LeftClickBlock) {
				((LeftClickBlock) event).setUseBlock(Result.DENY);
				((LeftClickBlock) event).setUseItem(Result.DENY);
			}
		} else if (false && event instanceof RightClickBlock && !event.getEntityPlayer().isSneaking() && event.getHand() == EnumHand.MAIN_HAND) {
			TileEntity tile = event.getWorld().getTileEntity(event.getPos());
			if (tile != null && tile.hasCapability(WorldGuiCapability.cap, event.getFace())) {
				((RightClickBlock) event).setUseBlock(Result.DENY);
				event.setCanceled(true);
				if (event.getWorld().isRemote) {
					WorldGui gui = tile.getCapability(WorldGuiCapability.cap, event.getFace()).getGui(event.getEntityPlayer(), tile.getPos());
					if (gui != null) {
						WorldGui.openGui(gui);
					}
				}
			}

		}
	}

}
