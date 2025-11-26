package marsh.town.brb.mixins;

import com.mojang.blaze3d.platform.Window;
import marsh.town.brb.BetterRecipeBook;
import marsh.town.brb.mixins.accessors.AbstractContainerScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseScrollHandler {
    @Shadow
    private double accumulatedDY;

    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(at = @At(value = "RETURN"), method = "onScroll")
    public void onMouseScroll(long window, double arg1, double vertical, CallbackInfo ci) {
        if (BetterRecipeBook.queuedScroll == 0 && BetterRecipeBook.config.scrolling.enableScrolling) {
            if (betterRecipeBook$isHoveringBundle()) {
                return;
            }

            assert minecraft.player != null;

            double d = (this.minecraft.options.discreteMouseScroll().get() ? Math.signum(vertical) : vertical) * this.minecraft.options.mouseWheelSensitivity().get();

            BetterRecipeBook.queuedScroll = (int) -((int) this.accumulatedDY + d);
        }
    }

    @Unique
    private boolean betterRecipeBook$isHoveringBundle() {
        if (!(minecraft.screen instanceof AbstractContainerScreen<?> containerScreen)) {
            return false;
        }

        Window window = minecraft.getWindow();
        double mouseX = minecraft.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
        double mouseY = minecraft.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();

        Slot slot = ((AbstractContainerScreenAccessor) containerScreen).invokeFindSlot(mouseX, mouseY);

        return slot != null && slot.getItem().getItem() instanceof BundleItem;
    }
}
