package moe.prwk.emitrades.util;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class LivingEntityRenderWidget extends Widget {
    private final @Nullable LivingEntity entity;
    private final float scale;
    private final int x, y;

    public LivingEntityRenderWidget(@Nullable LivingEntity entity, float scale, int x, int y) {
        this.entity = entity;
        this.scale = scale;
        this.x = x;
        this.y = y;
    }

    @Override
    public Bounds getBounds() {
        if (entity == null) return Bounds.EMPTY;
        float boxWidth = entity.getBbWidth();
        float boxHeight = entity.getBbHeight();
        return new Bounds(x, y, (int) (boxWidth / 2 * 8), (int) (boxHeight / 2 * 8));
    }

    @Override
    public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
        if (entity == null) return;

        //? if >=1.20.4 {
        int x2 = (int) (x + 16 * scale);
        int y2 = (int) (y + 16 * scale);
        InventoryScreen.renderEntityInInventoryFollowsMouse(draw,
                x, y, x2 ,y2, (int) (8 * scale), 0.0625f * scale,
                mouseX, mouseY,
                entity);
        //?} else {
        /*int x2 = x + 8;
        int y2 = (int) (y + 16 + entity.getBbHeight() * 2 * scale);
        InventoryScreen.renderEntityInInventoryFollowsMouse(draw,
                x2, y2, (int) (8 * scale),
                -mouseX, -mouseY,
                entity);
        *///?}
    }
}
