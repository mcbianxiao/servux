package fi.dy.masa.servux.network.packet;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import fi.dy.masa.servux.Servux;
import fi.dy.masa.servux.dataproviders.TweaksDataProvider;
import fi.dy.masa.servux.network.IPluginServerPlayHandler;
import fi.dy.masa.servux.network.IServerPayloadData;
import fi.dy.masa.servux.network.PacketSplitter;

@Environment(EnvType.SERVER)
public abstract class ServuxTweaksHandler<T extends CustomPayload> implements IPluginServerPlayHandler<T>
{
    private static final ServuxTweaksHandler<ServuxTweaksPacket.Payload> INSTANCE = new ServuxTweaksHandler<>() {
        @Override
        public void receive(ServuxTweaksPacket.Payload payload, ServerPlayNetworking.Context context)
        {
            ServuxTweaksHandler.INSTANCE.receivePlayPayload(payload, context);
        }
    };
    public static ServuxTweaksHandler<ServuxTweaksPacket.Payload> getInstance() { return INSTANCE; }

    public static final Identifier CHANNEL_ID = Identifier.of("servux", "tweaks");

    private boolean payloadRegistered = false;
    private final Map<UUID, Integer> failures = new HashMap<>();
    private static final int MAX_FAILURES = 4;
    private final Map<UUID, Long> readingSessionKeys = new HashMap<>();

    @Override
    public Identifier getPayloadChannel() { return CHANNEL_ID; }

    @Override
    public boolean isPlayRegistered(Identifier channel)
    {
        if (channel.equals(CHANNEL_ID))
        {
            return payloadRegistered;
        }

        return false;
    }

    @Override
    public void setPlayRegistered(Identifier channel)
    {
        if (channel.equals(CHANNEL_ID))
        {
            this.payloadRegistered = true;
        }
    }

    @Override
    public <P extends IServerPayloadData> void decodeServerData(Identifier channel, ServerPlayerEntity player, P data)
    {
        ServuxTweaksPacket packet = (ServuxTweaksPacket) data;

        if (!channel.equals(CHANNEL_ID))
        {
            return;
        }
        switch (packet.getType())
        {
            case PACKET_C2S_METADATA_REQUEST -> TweaksDataProvider.INSTANCE.sendMetadata(player);
            case PACKET_C2S_BLOCK_ENTITY_REQUEST -> TweaksDataProvider.INSTANCE.onBlockEntityRequest(player, packet.getPos());
            case PACKET_C2S_ENTITY_REQUEST -> TweaksDataProvider.INSTANCE.onEntityRequest(player, packet.getEntityId());
            case PACKET_C2S_NBT_RESPONSE_DATA ->
            {
                UUID uuid = player.getUuid();
                long readingSessionKey;

                if (!this.readingSessionKeys.containsKey(uuid))
                {
                    readingSessionKey = Random.create(Util.getMeasuringTimeMs()).nextLong();
                    this.readingSessionKeys.put(uuid, readingSessionKey);
                }
                else
                {
                    readingSessionKey = this.readingSessionKeys.get(uuid);
                }

                //Servux.debugLog("ServuxTweaksHandler#decodeServerData(): received Litematic Data Packet Slice of size {} (in bytes) // reading session key [{}]", packet.getTotalSize(), readingSessionKey);
                PacketByteBuf fullPacket = PacketSplitter.receive(this, readingSessionKey, packet.getBuffer());

                if (fullPacket != null)
                {
                    try
                    {
                        this.readingSessionKeys.remove(uuid);
                        TweaksDataProvider.INSTANCE.handleClientBulkData(player, fullPacket.readVarInt(), (NbtCompound) fullPacket.readNbt(NbtSizeTracker.ofUnlimitedBytes()));
                    }
                    catch (Exception e)
                    {
                        Servux.logger.error("ServuxTweaksHandler#decodeServerData(): Litematic Data: error reading fullBuffer [{}]", e.getLocalizedMessage());
                    }
                }
            }
            default -> Servux.logger.warn("ServuxTweaksHandler#decodeServerData(): Invalid packetType '{}' from player: {}, of size in bytes: {}.", packet.getPacketType(), player.getName().getLiteralString(), packet.getTotalSize());
        }
    }

    @Override
    public void reset(Identifier channel)
    {
        if (channel.equals(CHANNEL_ID))
        {
            this.failures.clear();
        }
    }

    public void resetFailures(Identifier channel, ServerPlayerEntity player)
    {
        if (channel.equals(CHANNEL_ID))
        {
            this.failures.remove(player.getUuid());
        }
    }

    @Override
    public void receivePlayPayload(T payload, ServerPlayNetworking.Context ctx)
    {
        if (payload.getId().id().equals(CHANNEL_ID))
        {
            ServerPlayerEntity player = ctx.player();
            ServuxTweaksHandler.INSTANCE.decodeServerData(CHANNEL_ID, player, ((ServuxTweaksPacket.Payload) payload).data());
        }
    }

    @Override
    public void encodeWithSplitter(ServerPlayerEntity player, PacketByteBuf buffer, ServerPlayNetworkHandler networkHandler)
    {
        // Send each PacketSplitter buffer slice
        ServuxTweaksHandler.INSTANCE.sendPlayPayload(player, new ServuxTweaksPacket.Payload(ServuxTweaksPacket.ResponseS2CData(buffer)));
    }

    @Override
    public <P extends IServerPayloadData> void encodeServerData(ServerPlayerEntity player, P data)
    {
        ServuxTweaksPacket packet = (ServuxTweaksPacket) data;

        // Send Response Data via Packet Splitter
        if (packet.getType().equals(ServuxTweaksPacket.Type.PACKET_S2C_NBT_RESPONSE_START))
        {
            PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
            buffer.writeVarInt(packet.getTransactionId());
            buffer.writeNbt(packet.getCompound());
            PacketSplitter.send(this, buffer, player, player.networkHandler);
        }
        else if (!ServuxTweaksHandler.INSTANCE.sendPlayPayload(player, new ServuxTweaksPacket.Payload(packet)))
        {
            UUID id = player.getUuid();

            // Packet failure tracking
            if (!this.failures.containsKey(id))
            {
                this.failures.put(id, 1);
            }
            else if (this.failures.get(id) > MAX_FAILURES)
            {
                //Servux.logger.info("Unregistering Entities Client {} after {} failures (Tweakeroo not installed perhaps)", player.getName().getLiteralString(), MAX_FAILURES);
                TweaksDataProvider.INSTANCE.onPacketFailure(player);
            }
            else
            {
                int count = this.failures.get(id) + 1;
                this.failures.put(id, count);
            }
        }
    }
}
