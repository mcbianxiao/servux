package fi.dy.masa.servux.network.server;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Interface for ServerPlayHandler
 * @param <T> (Payload Param)
 */
public interface IPluginServerPlayHandler<T extends CustomPayload> extends ServerPlayNetworking.PlayPayloadHandler<T>
{
    int FROM_SERVER = 1;
    int TO_SERVER = 2;
    int BOTH_SERVER = 3;
    int TO_CLIENT = 4;
    int FROM_CLIENT = 5;
    int BOTH_CLIENT = 6;

    /**
     * Returns your HANDLER's CHANNEL ID
     * @return (Channel ID)
     */
    Identifier getPayloadChannel();

    /**
     * Returns if your Channel ID has been registered to your Play Payload.
     * @param channel (Your Channel ID)
     * @return (true / false)
     */
    boolean isPlayRegistered(Identifier channel);

    /**
     * Sets your HANDLER as registered.
     * @param channel (Your Channel ID)
     */
    default void setPlayRegistered(Identifier channel) {}

    /**
     * Send your HANDLER a global reset() event, such as when the server is shutting down.
     * @param channel (Your Channel ID)
     */
    default void reset(Identifier channel) {}

    /**
     * Register your Payload with Fabric API.
     * See the fabric-networking-api-v1 Java Docs under PayloadTypeRegistry -> register()
     * for more information on how to do this.
     * -
     * @param channel (Your Channel ID)
     * @param direction (Payload Direction)
     * @param id (Your Payload Id<T>)
     * @param codec (Your Payload's CODEC)
     */
    default void registerPlayPayload(Identifier channel, int direction,
                                     @Nonnull CustomPayload.Id<T> id, @Nonnull PacketCodec<? super RegistryByteBuf,T> codec)
    {
        if (channel.equals(this.getPayloadChannel()) && this.isPlayRegistered(this.getPayloadChannel()) == false)
        {
            try
            {
                switch (direction)
                {
                    case TO_SERVER, FROM_CLIENT -> PayloadTypeRegistry.playC2S().register(id, codec);
                    case FROM_SERVER, TO_CLIENT -> PayloadTypeRegistry.playS2C().register(id, codec);
                    default ->
                    {
                        PayloadTypeRegistry.playC2S().register(id, codec);
                        PayloadTypeRegistry.playS2C().register(id, codec);
                    }
                }
            }
            catch (IllegalArgumentException e)
            {
                throw new IllegalArgumentException("registerPlayPayload: Channel ID "+ channel +" is already registered");
            }

            this.setPlayRegistered(channel);
        }
        else
        {
            throw new IllegalArgumentException("registerPlayPayload: Channel ID "+ channel +" is invalid, or it is already registered");
        }
    }

    /**
     * Register your Packet Receiver function.
     * You can use the HANDLER itself (Singleton method), or any other class that you choose.
     * See the fabric-network-api-v1 Java Docs under ServerPlayNetworking.registerGlobalReceiver()
     * for more information on how to do this.
     * -
     * @param channel (Your Channel ID)
     * @param id (Your Payload Id<T>)
     * @param receiver (Your Packet Receiver // if null, uses this::receivePlayPayload)
     * @return (True / False)
     */
    default boolean registerPlayReceiver(Identifier channel,
                                         @Nonnull CustomPayload.Id<T> id, @Nullable ServerPlayNetworking.PlayPayloadHandler<T> receiver)
    {
        if (channel.equals(this.getPayloadChannel()) && this.isPlayRegistered(this.getPayloadChannel()))
        {
            try
            {
                return ServerPlayNetworking.registerGlobalReceiver(id, Objects.requireNonNullElse(receiver, this::receivePlayPayload));
            }
            catch (IllegalArgumentException e)
            {
                throw new IllegalArgumentException("registerPlayReceiver: Channel ID " + channel + " payload has not been registered");
            }
        }
        else
        {
            throw new IllegalArgumentException("registerPlayReceiver: Channel ID "+ channel +" is invalid, or not registered");
        }
    }

    /**
     * Unregisters your Packet Receiver function.
     * You can use the HANDLER itself (Singleton method), or any other class that you choose.
     * See the fabric-network-api-v1 Java Docs under ServerPlayNetworking.unregisterGlobalReceiver()
     * for more information on how to do this.
     * -
     * @param channel (Your Channel ID)
     */
    default void unregisterPlayReceiver(Identifier channel)
    {
        if (channel.equals(this.getPayloadChannel()))
        {
            ServerPlayNetworking.unregisterGlobalReceiver(channel);
        }
    }

    /**
     * Receive Payload by pointing static receive() method to this to convert Payload to its data decode() function.
     * -
     * @param payload (Payload to decode)
     * @param ctx (Fabric Context)
     */
    default void receivePlayPayload(T payload, ServerPlayNetworking.Context ctx) {}

    /**
     * Receive Payload via the legacy "onCustomPayload" from a Network Handler Mixin interface.
     * -
     * @param payload (Payload to decode)
     * @param handler (Network Handler that received the data)
     * @param ci (Callbackinfo for sending ci.cancel(), if wanted)
     */
    default void receivePlayPayload(T payload, ServerPlayNetworkHandler handler, CallbackInfo ci) {}

    /**
     * Payload Decoder wrapper function.
     * Implements how the data is processed after being decoded from the receivePayload().
     * You can ignore these and implement your own helper class/methods.
     * These are provided as an example, and can be used in your HANDLER directly.
     * -
     * @param channel (Channel)
     * @param player (Player received from)
     * @param data (Data Codec)
     */
    default void decodeNbtCompound(Identifier channel, ServerPlayerEntity player, NbtCompound data) {}
    default void decodeByteBuf(Identifier channel, ServerPlayerEntity player, ServuxBuf data) {}
    default <D> void decodeObject(Identifier channel, ServerPlayerEntity player, D data1) {}
    default <D, E> void decodeObject(Identifier channel, ServerPlayerEntity player, D data1, E data2) {}
    default <D, E, F> void decodeObject(Identifier channel, ServerPlayerEntity player, D data1, E data2, F data3) {}
    default <D, E, F, G> void decodeObject(Identifier channel, ServerPlayerEntity player, D data1, E data2, F data3, G data4) {}
    default <D, E, F, G, H> void decodeObject(Identifier channel, ServerPlayerEntity player, D data1, E data2, F data3, G data4, H data5) {}

    /**
     * Payload Encoder wrapper function.
     * Implements how to encode() your Payload, then forward complete Payload to sendPayload().
     * -
     * @param player (Player to send the data to)
     * @param data (Data Codec)
     */
    default void encodeNbtCompound(ServerPlayerEntity player, NbtCompound data) {}
    default void encodeByteBuf(ServerPlayerEntity player, ServuxBuf data) {}
    default <D> void encodeObject(ServerPlayerEntity player, D data1) {}
    default <D, E> void encodeObject(ServerPlayerEntity player, D data1, E data2) {}
    default <D, E, F> void encodeObject(ServerPlayerEntity player, D data1, E data2, F data3) {}
    default <D, E, F, G> void encodeObject(ServerPlayerEntity player, D data1, E data2, F data3, G data4) {}
    default <D, E, F, G, H> void encodeObject(ServerPlayerEntity player, D data1, E data2, F data3, G data4, H data5) {}

    /**
     * Sends the Payload to the player using the Fabric-API interface.
     * -
     * @param player (Player to send the data to)
     * @param payload (The Payload to send)
     */
    default void sendPlayPayload(ServerPlayerEntity player, T payload)
    {
        if (payload.getId().id().equals(this.getPayloadChannel()) && this.isPlayRegistered(this.getPayloadChannel()) &&
                ServerPlayNetworking.canSend(player, payload.getId()))
        {
            ServerPlayNetworking.send(player, payload);
        }
    }

    /**
     * Sends the Payload to the player using the ServerPlayNetworkHandler interface.
     * @param handler (ServerPlayNetworkHandler)
     * @param payload (The Payload to send)
     */
    default void sendPlayPayload(ServerPlayNetworkHandler handler, T payload)
    {
        if (payload.getId().id().equals(this.getPayloadChannel()) && this.isPlayRegistered(this.getPayloadChannel()))
        {
            Packet<?> packet = new CustomPayloadS2CPacket(payload);

            if (handler != null && handler.accepts(packet))
            {
                handler.sendPacket(packet);
            }
        }
    }
}
