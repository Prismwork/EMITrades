package moe.prwk.emitrades.recipes;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import moe.prwk.emitrades.EMITradesPlugin;
import moe.prwk.emitrades.util.Constants;
import moe.prwk.emitrades.util.LivingEntityRenderWidget;
import moe.prwk.emitrades.util.VersionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings({
        "unused",
        "OptionalUsedAsFieldOrParameterType",
})
public class TradeEntry implements EmiRecipe {
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;
    private final List<EmiIngredient> catalysts = List.of();
    private final AbstractVillager merchant;
    private final Font textRenderer;
    private final MutableComponent title;
    private final Optional<Integer> villagerXp;
    private final Optional<Integer> maxUses;
    private final ResourceLocation id;

    public TradeEntry(
            List<EmiIngredient> inputs,
            List<EmiStack> outputs,
            AbstractVillager merchant,
            VillagerProfession prof,
            int level,
            Optional<Integer> villagerXp,
            Optional<Integer> maxUses,
            int numId) {
        this.textRenderer = Minecraft.getInstance().font;

        this.id = VersionUtil.identifier(
                "emitrades", "/trade/"
                        + prof.name().replace(':', '/')
                        + "/offer_" + numId
        );
        this.inputs = inputs;
        this.outputs = outputs;
        this.merchant = merchant;
        this.villagerXp = villagerXp;
        this.maxUses = maxUses;
        if (prof.equals(Constants.WANDERING_TRADER)) {
            this.title = Component.translatable("emi.emitrades.placeholder.wandering_trader");
        } else {
            this.title = Component.translatable("entity.minecraft.villager." + prof.name().substring(prof.name().lastIndexOf(":") + 1))
                    .append(" - ").append(Component.translatable("emi.emitrades.profession.lvl." + level));
        }
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EMITradesPlugin.VILLAGER_TRADES;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return catalysts;
    }

    @Override
    public int getDisplayWidth() {
        return Math.max(106, 21 + textRenderer.width(title));
    }

    @Override
    public int getDisplayHeight() {
        if (villagerXp.isPresent() || maxUses.isPresent()) return 28 + 9;
        return 28;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.add(new LivingEntityRenderWidget(merchant, 1.5f, 1, 6));
        widgets.addText(title,
                21, 0, 16777215, true);
        widgets.addSlot(inputs.get(0), 21, 10);
        widgets.addSlot(inputs.get(1), 41, 10);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 60, 10);
        SlotWidget outputSlot = new SlotWidget(outputs.get(0), 85, 10).recipeContext(this);
        widgets.add(outputSlot);

        if (villagerXp.isPresent()) {
            Component xp = Component.literal("XP: " + villagerXp.get());
            int xpX = 22;
            int xpY = getDisplayHeight() - 8;
            widgets.addText(xp, xpX + 1, xpY, 0, false);
            widgets.addText(xp, xpX - 1, xpY, 0, false);
            widgets.addText(xp, xpX, xpY + 1, 0, false);
            widgets.addText(xp, xpX, xpY - 1, 0, false);
            widgets.addText(xp, xpX, xpY, 0x80FF20, false);
        }

        if (maxUses.isPresent()) {
            Component max = Component.literal("MAX: " + maxUses.get());
            int maxX = 55;
            int maxY = getDisplayHeight() - 8;
            widgets.addText(max, maxX, maxY, 0xD48333, true);
        }
    }
}
