package fi.dy.masa.servux.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;

public class ServuxBuf extends PacketByteBuf
{
    public ServuxBuf(ByteBuf parent)
    {
        super(parent);
    }
}
