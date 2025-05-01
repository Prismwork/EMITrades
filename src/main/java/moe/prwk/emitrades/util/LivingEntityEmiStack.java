package moe.prwk.emitrades.util;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.tooltip.RemainderTooltipComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
//? if >=1.20.6 {
import net.minecraft.core.component.DataComponentPatch;
//?}
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LivingEntityEmiStack extends EmiStack {
    private final @Nullable LivingEntity entity;
    private final float scale;

    protected LivingEntityEmiStack(@Nullable LivingEntity entity, float scale) {
        this.entity = entity;
        this.scale = scale;
    }

    public static LivingEntityEmiStack of(@Nullable LivingEntity entity) {
        return ofScaled(entity, 1.0f);
    }

    public static LivingEntityEmiStack ofScaled(@Nullable LivingEntity entity, float scale) {
        return new LivingEntityEmiStack(entity, scale);
    }

    @Override
    public EmiStack copy() {
        LivingEntityEmiStack stack = ofScaled(entity, scale);
        stack.setRemainder(getRemainder().copy());
        stack.comparison = comparison;
        return stack;
    }

    @Override
    public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
        if (isEmpty()) return;
        MouseHandler mouse = Minecraft.getInstance().mouseHandler;
        //? if >=1.20.4 {
        double mouseX = mouse.xpos() / Minecraft.getInstance().getWindow().getGuiScale();
        double mouseY = mouse.ypos() / Minecraft.getInstance().getWindow().getGuiScale();
        //?} else {
        /*double mouseX = mouse.xpos() * Minecraft.getInstance().getWindow().getGuiScale();
        double mouseY = mouse.ypos() * Minecraft.getInstance().getWindow().getGuiScale();
        *///?}
        //? if >=1.20.4 {
        int x1 = x;
        int y1 = y;
        int x2 = x1 + 16;
        int y2 = y1 + 16;
        //?} else {
        /*int x2 = x + 8;
        int y2 = y + 8;
        *///?}
        InventoryScreen.renderEntityInInventoryFollowsMouse(draw,
                /*? if >=1.20.4 {*/ x1, y1,/*?}*/ x2, y2, (int) (8 * scale)/*? if >=1.20.4 {*/, 0.0625f /*?}*/,
                (float) mouseX, (float) mouseY,
                Objects.requireNonNull(entity));
    }

    @Override
    public boolean isEmpty() {
        return entity == null;
    }

    //? if >=1.20.6 {
    @Override
    public DataComponentPatch getComponentChanges() {
        return DataComponentPatch.EMPTY;
    }
    //?} else {
    /*@Override
    public CompoundTag getNbt() {
        if (entity == null) return new CompoundTag();
        return entity.saveWithoutId(new CompoundTag());
    }
    *///?}

    @Override
    public Object getKey() {
        return entity;
    }

    @Override
    public ResourceLocation getId() {
        if (entity == null) throw new RuntimeException("Entity is null");
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
    }

    @Override
    public List<Component> getTooltipText() {
        return List.of(getName());
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        List<ClientTooltipComponent> list = new ArrayList<>();
        if (entity != null) {
            list.addAll(getTooltipText().stream().map(EmiPort::ordered).map(ClientTooltipComponent::create).toList());
            String mod;
            if (entity instanceof Villager villager) {
                mod = EmiUtil.getModName(BuiltInRegistries.VILLAGER_PROFESSION.getKey(villager.getVillagerData().getProfession()).getNamespace());
            } else {
                mod = EmiUtil.getModName(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace());
            }
            list.add(ClientTooltipComponent.create(EmiPort.ordered(EmiPort.literal(mod, ChatFormatting.BLUE, ChatFormatting.ITALIC))));
            if (!getRemainder().isEmpty()) {
                list.add(new RemainderTooltipComponent(this));
            }
        }
        return list;
    }

    @Override
    public Component getName() {
        return entity != null ? entity.getName() : Component.literal("yet another missingno");
    }
}
