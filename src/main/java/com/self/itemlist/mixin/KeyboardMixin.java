package com.self.itemlist.mixin;

import com.self.itemlist.ItemListScreen;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onChar", at = @At("HEAD"), cancellable = true)
    private void itemlist$onChar(long windowPointer, int codePoint, int modifiers, CallbackInfo ci) {
        Screen screen = this.client.currentScreen;
        if (screen instanceof HandledScreen) {
            if (ItemListScreen.charTyped((char) codePoint, modifiers)) {
                ci.cancel();
            }
        }
    }
}
