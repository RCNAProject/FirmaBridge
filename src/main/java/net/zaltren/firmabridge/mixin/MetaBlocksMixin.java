package net.zaltren.firmabridge.mixin;

import gregtech.common.blocks.MetaBlocks;
import net.zaltren.firmabridge.integration.TFCStoneTypeHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects TFC stone type registration at the head of MetaBlocks.init() so that
 * TFC stone types are present in the StoneType registry before GT creates its
 * BlockOre instances. Those instances capture allowed StoneType values at
 * construction time, so registration must happen before MetaBlocks.init() runs.
 *
 * FirmaBridge declares required-after:gregtech, meaning GT's preInit (where
 * MetaBlocks.init() is called) executes before FirmaBridge's preInit. This
 * Mixin is the only way to insert code ahead of that call.
 */
@Mixin(value = MetaBlocks.class, remap = false)
public class MetaBlocksMixin {

    @Inject(method = "init", at = @At("HEAD"), remap = false)
    private static void firmaBridge$registerTFCStoneTypes(CallbackInfo ci) {
        TFCStoneTypeHandler.register();
    }

}
