package fi.dy.masa.servux.interfaces;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public interface IServuxStructuresListener
{
    default void reset() { }
    default void receiveServuxStructures(NbtCompound data, ServerPlayNetworking.Context ctx, Identifier id) { }
    default void sendServuxStructures(NbtCompound data, ServerPlayerEntity player) { }
    default void encodeServuxStructures(NbtCompound data, ServerPlayerEntity player, Identifier id) { }
    default void decodeServuxStructures(NbtCompound data, ServerPlayerEntity player, Identifier id) { }
}
