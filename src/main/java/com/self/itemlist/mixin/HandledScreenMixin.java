package com.self.itemlist.mixin;

import com.self.itemlist.ItemListScreen;
import com.self.itemlist.IngredientUsageScreen;
import com.self.itemlist.ItemRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    @Shadow
    protected Slot getSlotAt(double x, double y) { return null; }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ItemListScreen.render(context, mouseX, mouseY, delta, (HandledScreen<?>) (Object) this);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (ItemListScreen.mouseClicked(mouseX, mouseY, button, (HandledScreen<?>) (Object) this)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (keyCode == GLFW.GLFW_KEY_T) {
            HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
            // Get the hovered slot
            Slot hoveredSlot = this.getSlotAt(MinecraftClient.getInstance().mouse.getX(), MinecraftClient.getInstance().mouse.getY());
            if (hoveredSlot != null && hoveredSlot.hasStack()) {
                ItemStack stack = hoveredSlot.getStack();
                if (stack.getItem() == Items.DIAMOND && stack.getName().getString().equals("Enchanted Diamond")) {
                    // Find the custom item for enchanted diamond
                    ItemRegistry.getAllItems().stream()
                        .filter(item -> item.getId().equals("enchanted_diamond"))
                        .findFirst()
                        .ifPresent(customItem -> {
                            MinecraftClient.getInstance().setScreen(new IngredientUsageScreen(customItem, screen));
                            cir.setReturnValue(true);
                        });
                }
            }
        }

        if (ItemListScreen.keyPressed(keyCode, scanCode, modifiers)) {
            cir.setReturnValue(true);
        }
    }
}
