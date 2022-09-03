package io.github.pkstdev.emitrades.recipe;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.pkstdev.emitrades.EMITradesPlugin;
import io.github.pkstdev.emitrades.util.TradeProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffers;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VillagerTrade implements EmiRecipe {
    private final TradeProfile profile;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;
    private final int id;
    private final MutableText title;

    public VillagerTrade(TradeProfile profile, int id) {
        this.profile = profile;
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.id = id;
        this.title = EmiPort.translatable("entity.minecraft.villager." + profile.profession().getId().substring(profile.profession().getId().lastIndexOf(":") + 1))
                .append(" - ").append(EmiPort.translatable("emi.emitrades.profession.lvl." + profile.level()));
        TradeOffers.Factory offer = profile.offer();
        if (offer instanceof TradeOffers.BuyForOneEmeraldFactory factory) {
            inputs.add(0, EmiStack.of(factory.buy, factory.price));
            inputs.add(1, EmiStack.EMPTY);
            outputs.add(0, EmiStack.of(Items.EMERALD));
        } else if (offer instanceof TradeOffers.SellItemFactory factory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, factory.price));
            inputs.add(1, EmiStack.EMPTY);
            outputs.add(0, EmiStack.of(factory.sell, factory.count));
        } else if (offer instanceof TradeOffers.SellSuspiciousStewFactory factory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, 1));
            inputs.add(1, EmiStack.EMPTY);
            ItemStack stack = new ItemStack(Items.SUSPICIOUS_STEW, 1);
            SuspiciousStewItem.addEffectToStew(stack, factory.effect, factory.duration);
            outputs.add(0, EmiStack.of(stack));
        } else if (offer instanceof TradeOffers.ProcessItemFactory factory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, factory.price));
            inputs.add(1, EmiStack.of(factory.secondBuy, factory.secondCount));
            outputs.add(0, EmiStack.of(factory.sell, factory.sellCount));
        } else if (offer instanceof TradeOffers.SellEnchantedToolFactory factory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, Math.min(factory.basePrice + 5, 64)));
            inputs.add(1, EmiStack.EMPTY);
            outputs.add(0, EmiStack.of(factory.tool));
        } else if (offer instanceof TradeOffers.TypeAwareBuyForOneEmeraldFactory factory) {
            List<EmiStack> stacks = new ArrayList<>();
            factory.map.values().forEach(item -> stacks.add(EmiStack.of(item)));
            inputs.add(0, new ListEmiIngredient(stacks, factory.count));
            inputs.add(1, EmiStack.EMPTY);
            outputs.add(0, EmiStack.of(Items.EMERALD));
        } else if (offer instanceof TradeOffers.SellPotionHoldingItemFactory factory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, factory.price));
            inputs.add(1, EmiStack.of(factory.secondBuy, factory.secondCount));
            outputs.add(0, EmiStack.of(factory.sell, factory.sellCount));
        } else if (offer instanceof TradeOffers.EnchantBookFactory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, 5));
            inputs.add(1, EmiStack.of(Items.BOOK));
            outputs.add(0, EmiStack.of(Items.ENCHANTED_BOOK));
        } else if (offer instanceof TradeOffers.SellMapFactory factory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, factory.price));
            inputs.add(1, EmiStack.of(Items.COMPASS));
            outputs.add(0, EmiStack.of(Items.FILLED_MAP));
        } else if (offer instanceof TradeOffers.SellDyedArmorFactory factory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, factory.price));
            inputs.add(1, EmiStack.EMPTY);
            outputs.add(0, EmiStack.of(factory.sell));
        } else if (offer instanceof EMITradesPlugin.FakeFactory factory) {
            inputs.add(0, factory.first.emi());
            inputs.add(1, factory.second.emi());
            outputs.add(0, factory.sell.emi());
        } else {
            inputs.add(0, EmiStack.EMPTY);
            inputs.add(1, EmiStack.EMPTY);
            outputs.add(0, EmiStack.EMPTY);
        }
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EMITradesPlugin.VILLAGER_TRADES;
    }

    @Override
    public @Nullable Identifier getId() {
        return new Identifier("emi", "emitrades/villager_trades/" + profile.profession().getId().substring(profile.profession().getId().lastIndexOf(":") + 1) + "_" + id);
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
    public int getDisplayWidth() {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        return Math.max(86, textRenderer.getWidth(title) + 2);
    }

    @Override
    public int getDisplayHeight() {
        return 28;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        widgets.addText(EmiPort.ordered(title),
                (getDisplayWidth() - textRenderer.getWidth(title)) / 2, 0, 16777215, true);
        widgets.addSlot(inputs.get(0), getDisplayWidth() / 2 - 42, 10);
        widgets.addSlot(inputs.get(1), getDisplayWidth() / 2 - 22, 10);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, getDisplayWidth() / 2 - 3, 10);
        SlotWidget outputSlot = new SlotWidget(outputs.get(0), getDisplayWidth() / 2 + 22, 10).recipeContext(this);
        if (profile.offer() instanceof TradeOffers.SellDyedArmorFactory) {
            outputSlot = outputSlot.appendTooltip(EmiPort.translatable("emi.emitrades.random_colored").formatted(Formatting.YELLOW));
        } else if (profile.offer() instanceof TradeOffers.SellPotionHoldingItemFactory || profile.offer() instanceof TradeOffers.SellSuspiciousStewFactory) {
            outputSlot = outputSlot.appendTooltip(EmiPort.translatable("emi.emitrades.random_effect").formatted(Formatting.YELLOW));
        } else if (profile.offer() instanceof TradeOffers.SellMapFactory) {
            outputSlot = outputSlot.appendTooltip(EmiPort.translatable("emi.emitrades.random_structure").formatted(Formatting.YELLOW));
        }
        else if (profile.offer() instanceof TradeOffers.EnchantBookFactory || profile.offer() instanceof TradeOffers.SellEnchantedToolFactory) {
            outputSlot = outputSlot.appendTooltip(EmiPort.translatable("emi.emitrades.random_enchantment").formatted(Formatting.YELLOW));
        }
        widgets.add(outputSlot);
    }
}
