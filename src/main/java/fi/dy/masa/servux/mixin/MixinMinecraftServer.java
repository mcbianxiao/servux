package fi.dy.masa.servux.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.profiler.Profiler;
import fi.dy.masa.servux.dataproviders.DataProviderManager;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer
{
    @Shadow @Final private Profiler profiler;
    @Shadow private int ticks;

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickEnd(java.util.function.BooleanSupplier supplier, CallbackInfo ci)
    {
        this.profiler.push("servux_tick");
        DataProviderManager.INSTANCE.tickProviders((MinecraftServer) (Object) this, this.ticks);
        this.profiler.pop();
    }
}
