package fi.dy.masa.servux.deprecated;

import fi.dy.masa.servux.Servux;
import fi.dy.masa.servux.network.payload.channel.ServuxS2CLitematicsPayload;
import fi.dy.masa.servux.network.payload.channel.ServuxS2CMetadataPayload;
import fi.dy.masa.servux.network.payload.channel.ServuxS2CStructuresPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;


/**
 * Version 1 Network API --> Removed
 * canSend()
 * Wraps: canSend(player.networkHandler, payload.getId().id());
 * --> Wraps Internally as:
 * `--> ServerNetworkingImpl.getAddon(player.networkHandler).getSendableChannels().contains(payload.getId().id());
 * send()
 * Wraps internally as:
 * --> player.networkHandler.sendPacket(ServerPlayNetworking.createS2CPacket(payload));
 */
@Deprecated(forRemoval = true)
public abstract class ServerNetworkPlayHandler
{
    public static void sendServuxStructures(ServuxS2CStructuresPayload payload, ServerPlayerEntity player)
    {
        // Client-bound packet sent from the Server
        if (ServerPlayNetworking.canSend(player, payload.getId()))
        {
            ServerPlayNetworking.send(player, payload);
            Servux.printDebug("ServerNetworkPlayHandler#sendServuxStructures(): sending payload id: {}", payload.getId());
        }
        else
            Servux.printDebug("ServerNetworkPlayHandler#sendServuxStructures(): [CanSend=false] error sending payload: {} to player {}", payload.getId(), player.getName().getLiteralString());
    }
    public static void receiveServuxStructures(ServuxS2CStructuresPayload payload, ServerPlayNetworking.Context ctx)
    {
        // Server-bound packet received from the Client
        Servux.printDebug("ServerNetworkPlayHandler#receiveServuxStructures(): id: {} received ServUX Payload (size in bytes): {}", payload.getId(), payload.data().getSizeInBytes());
        //((ServuxStructuresPlayListener) ServuxStructuresPlayListener.getInstance()).receiveServuxStructures(payload.data(), ctx, payload.getId().id());
    }
    public static void sendServuxMetadata(ServuxS2CMetadataPayload payload, ServerPlayerEntity player)
    {
        // Client-bound packet sent from the Server
        if (ServerPlayNetworking.canSend(player, payload.getId()))
        {
            ServerPlayNetworking.send(player, payload);
            Servux.printDebug("ServerNetworkPlayHandler#sendServuxMetadata(): sending payload id: {}", payload.getId());
        }
    }
    public static void receiveServuxMetadata(ServuxS2CMetadataPayload payload, ServerPlayNetworking.Context ctx)
    {
        // Server-bound packet received from the Client
        Servux.printDebug("ServerNetworkPlayHandler#receiveServuxMetadata(): id: {} received ServUX Payload (size in bytes): {}", payload.getId(), payload.data().getSizeInBytes());
        //((ServuxMetadataHandler) ServuxMetadataHandler.getInstance()).receiveServuxMetadata(payload.data(), ctx, payload.getId().id());
    }
    public static void sendServuxLitematics(ServuxS2CLitematicsPayload payload, ServerPlayerEntity player)
    {
        // Client-bound packet sent from the Server
        if (ServerPlayNetworking.canSend(player, payload.getId()))
        {
            ServerPlayNetworking.send(player, payload);
            Servux.printDebug("ServerNetworkPlayHandler#sendServuxLitematics(): sending payload id: {}", payload.getId());
        }
    }
    public static void receiveServuxLitematics(ServuxS2CLitematicsPayload payload, ServerPlayNetworking.Context ctx)
    {
        // Server-bound packet received from the Client
        Servux.printDebug("ServerNetworkPlayHandler#receiveServuxLitematics(): id: {} received ServUX Payload (size in bytes): {}", payload.getId(), payload.data().getSizeInBytes());
        //((ServuxLitematicsHandler) ServuxLitematicsHandler.getInstance()).receiveServuxLitematics(payload.data(), ctx, payload.getId().id());
    }
}
