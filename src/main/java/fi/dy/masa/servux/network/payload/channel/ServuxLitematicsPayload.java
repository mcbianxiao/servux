package fi.dy.masa.servux.network.payload.channel;

import fi.dy.masa.servux.network.payload.PayloadType;
import fi.dy.masa.servux.network.payload.PayloadTypeRegister;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/**
 * Intended as a new future Servux Data Provider for providing a common Litematic storage server
 */
public record ServuxLitematicsPayload(NbtCompound data) implements CustomPayload
{
    public static final Id<ServuxLitematicsPayload> TYPE = new Id<>(PayloadTypeRegister.INSTANCE.getIdentifier(PayloadType.SERVUX_LITEMATICS));
    public static final PacketCodec<PacketByteBuf, ServuxLitematicsPayload> CODEC = CustomPayload.codecOf(ServuxLitematicsPayload::write, ServuxLitematicsPayload::new);
    public static final String KEY = PayloadTypeRegister.INSTANCE.getKey(PayloadType.SERVUX_LITEMATICS);

    public ServuxLitematicsPayload(PacketByteBuf buf) { this(buf.readNbt()); }

    private void write(PacketByteBuf buf) { buf.writeNbt(data); }

    @Override
    public Id<? extends CustomPayload> getId() { return TYPE; }
}
