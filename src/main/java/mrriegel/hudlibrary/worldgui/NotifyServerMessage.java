package mrriegel.hudlibrary.worldgui;

import io.netty.buffer.ByteBuf;
import mrriegel.hudlibrary.CommonEvents;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class NotifyServerMessage implements IMessage, IMessageHandler<NotifyServerMessage, IMessage> {

	boolean openGui;

	public NotifyServerMessage() {
	}

	public NotifyServerMessage(boolean openGui) {
		this.openGui = openGui;
	}

	@Override
	public IMessage onMessage(NotifyServerMessage message, MessageContext ctx) {
		FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
			if (message.openGui)
				CommonEvents.openWorldGuis.add(ctx.getServerHandler().player.getUniqueID());
			else
				CommonEvents.openWorldGuis.remove(ctx.getServerHandler().player.getUniqueID());
		});
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		openGui = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(openGui);
	}

}
