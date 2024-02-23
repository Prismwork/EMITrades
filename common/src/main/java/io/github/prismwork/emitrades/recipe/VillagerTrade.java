package io.github.prismwork.emitrades.recipe;

import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.prismwork.emitrades.EMITradesPlugin;
import io.github.prismwork.emitrades.util.EntityEmiStack;
import io.github.prismwork.emitrades.util.ListEmiStack;
import io.github.prismwork.emitrades.util.TradeProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VillagerTrade extends AbstractVillagerTrade {
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;
    private final List<EmiIngredient> catalysts;
    private final int id;
    private final MutableText title;

    @SuppressWarnings("UnstableApiUsage")
    public VillagerTrade(TradeProfile profile, int id) {
        super(profile);
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.catalysts = profile.villager() != null ?
                List.of(EntityEmiStack.ofScaled(profile.villager(), 12.0f)) : List.of();
        this.id = id;
        VillagerProfession internalProf = profile.profession();
        if (internalProf.equals(EMITradesPlugin.WANDERING_TRADER_PLACEHOLDER)) {
            this.title = Text.translatable("emi.emitrades.placeholder.wandering_trader");
        } else {
            this.title = Text.translatable("entity.minecraft.villager." + profile.profession().id().substring(profile.profession().id().lastIndexOf(":") + 1))
                    .append(" - ").append(Text.translatable("emi.emitrades.profession.lvl." + profile.level()));
        }
        TradeOffers.Factory offer = profile.defaultOffer();
        if (offer instanceof TradeOffers.BuyItemFactory factory) {
            inputs.add(0, EmiStack.of(factory.stack, factory.price));
            inputs.add(1, EmiStack.EMPTY);
            outputs.add(0, EmiStack.of(Items.EMERALD));
        } else if (offer instanceof TradeOffers.SellItemFactory factory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, factory.price));
            inputs.add(1, EmiStack.EMPTY);
            outputs.add(0, EmiStack.of(factory.sell));
        } else if (offer instanceof TradeOffers.SellSuspiciousStewFactory factory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, 1));
            inputs.add(1, EmiStack.EMPTY);
            ItemStack stack = new ItemStack(Items.SUSPICIOUS_STEW, 1);
            SuspiciousStewItem.addEffectsToStew(stack, factory.stewEffects);
            outputs.add(0, EmiStack.of(stack));
        } else if (offer instanceof TradeOffers.ProcessItemFactory factory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, factory.price));
            inputs.add(1, EmiStack.of(factory.toBeProcessed));
            outputs.add(0, EmiStack.of(factory.processed));
        } else if (offer instanceof TradeOffers.SellEnchantedToolFactory factory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, Math.min(factory.basePrice + 5, 64)));
            inputs.add(1, EmiStack.EMPTY);

            List<EmiStack> out = new ArrayList<>();
            int enchantability = factory.tool.getItem().getEnchantability();
            int power = 5 + 15 + 1
                    + (enchantability / 4 + 1) + (enchantability / 4 + 1);
            EnchantmentHelper.getPossibleEntries(power, factory.tool, false).forEach(
                    entry -> {
                        Enchantment enchantment = entry.enchantment;
                        for (int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); i++){
                            ItemStack stack = factory.tool.copy();
                            stack.addEnchantment(entry.enchantment, i);
                            out.add(EmiStack.of(stack));
                        }
                    }
            );

            outputs.add(0, new ListEmiStack(out, factory.tool.getCount()));
        } else if (offer instanceof TradeOffers.TypeAwareBuyForOneEmeraldFactory factory) {
            List<EmiStack> stacks = new ArrayList<>();
            factory.map.values().forEach(item -> stacks.add(EmiStack.of(item)));
            inputs.add(0, new ListEmiIngredient(stacks, factory.count));
            inputs.add(1, EmiStack.EMPTY);
            outputs.add(0, EmiStack.of(Items.EMERALD));
        } else if (offer instanceof TradeOffers.SellPotionHoldingItemFactory factory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, factory.price));
            inputs.add(1, EmiStack.of(factory.secondBuy, factory.secondCount));

            List<EmiStack> out = new ArrayList<>();
            Registries.POTION.stream().filter((potion) ->
                    !potion.getEffects().isEmpty() && BrewingRecipeRegistry.isBrewable(potion)).forEach(
                            potion -> {
                                ItemStack stack = PotionUtil.setPotion(factory.sell, potion);
                                out.add(EmiStack.of(stack));
                            }
            );

            outputs.add(0, new ListEmiStack(out, factory.sellCount));
        } else if (offer instanceof TradeOffers.EnchantBookFactory factory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, 5));
            inputs.add(1, EmiStack.of(Items.BOOK));

            List<EmiStack> out = new ArrayList<>();
            factory.possibleEnchantments.forEach(
                    enchantment -> {
                        int min = Math.max(enchantment.getMinLevel(), factory.minLevel);
                        int max = Math.min(enchantment.getMaxLevel(), factory.maxLevel);

                        for (int i = min; i <= max; i++) {
                            ItemStack stack = EnchantedBookItem.forEnchantment(
                                    new EnchantmentLevelEntry(enchantment, i)
                            );
                            out.add(EmiStack.of(stack));
                        }
                    }
            );

            outputs.add(0, new ListEmiStack(out, 1));
        } else if (offer instanceof TradeOffers.SellMapFactory factory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, factory.price));
            inputs.add(1, EmiStack.of(Items.COMPASS));
            outputs.add(0, EmiStack.of(Items.FILLED_MAP));
        } else if (offer instanceof TradeOffers.SellDyedArmorFactory factory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, factory.price));
            inputs.add(1, EmiStack.EMPTY);
            outputs.add(0, EmiStack.of(factory.sell));
        } else if (offer instanceof EMITradesPlugin.FakeFactory factory) {
            inputs.add(0, EmiStack.of(factory.first));
            inputs.add(1, EmiStack.of(factory.second));
            outputs.add(0, EmiStack.of(factory.sell));
        } else {
            inputs.add(0, EmiStack.EMPTY);
            inputs.add(1, EmiStack.EMPTY);
            outputs.add(0, EmiStack.EMPTY);
        }
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return catalysts;
    }

    @Override
    public @Nullable Identifier getId() {
        return new Identifier("emitrades", "villager_trades/" + profile.profession().id().substring(profile.profession().id().lastIndexOf(":") + 1) + "_" + id);
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
        int extraWidth = catalysts.isEmpty() ? 0 : 21;
        return (catalysts.isEmpty() || !EMITradesPlugin.CONFIG.enable3DVillagerModelInRecipes) ?
                Math.max(86, textRenderer.getWidth(title) + 2) :
                Math.max(extraWidth + 85, extraWidth + textRenderer.getWidth(title));
    }

    @Override
    public int getDisplayHeight() {
        return 28;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        if (catalysts.isEmpty() || !EMITradesPlugin.CONFIG.enable3DVillagerModelInRecipes) {
            widgets.addText(title,
                    (getDisplayWidth() - textRenderer.getWidth(title)) / 2, 0, 16777215, true);
            widgets.addSlot(inputs.get(0), getDisplayWidth() / 2 - 42, 10);
            widgets.addSlot(inputs.get(1), getDisplayWidth() / 2 - 22, 10);
            widgets.addTexture(EmiTexture.EMPTY_ARROW, getDisplayWidth() / 2 - 3, 10);
            SlotWidget outputSlot = new SlotWidget(outputs.get(0), getDisplayWidth() / 2 + 22, 10).recipeContext(this);
            wrapOutput(widgets, outputSlot);
        } else {
            SlotWidget villagerSlot = new SlotWidget(catalysts.get(0), 1, 6).drawBack(false);
            if (profile.villager() instanceof VillagerEntity villager) {
                villagerSlot.appendTooltip(Text.translatable("emi.emitrades.profession.lvl." + villager.getVillagerData().getLevel()).formatted(Formatting.YELLOW));
            }
            widgets.add(villagerSlot);
            widgets.addText(title,
                    21, 0, 16777215, true);
            widgets.addSlot(inputs.get(0), 21, 10);
            widgets.addSlot(inputs.get(1), 41, 10);
            widgets.addTexture(EmiTexture.EMPTY_ARROW, 60, 10);
            SlotWidget outputSlot = new SlotWidget(outputs.get(0), 85, 10).recipeContext(this);
            wrapOutput(widgets, outputSlot);
        }
    }

    private void wrapOutput(WidgetHolder widgets, SlotWidget outputSlot) {
        if (profile.defaultOffer() instanceof TradeOffers.SellDyedArmorFactory) {
            outputSlot = outputSlot.appendTooltip(Text.translatable("emi.emitrades.random_colored").formatted(Formatting.YELLOW));
        } else if (profile.defaultOffer() instanceof TradeOffers.SellSuspiciousStewFactory) {
            outputSlot = outputSlot.appendTooltip(Text.translatable("emi.emitrades.random_effect").formatted(Formatting.YELLOW));
        } else if (profile.defaultOffer() instanceof TradeOffers.SellMapFactory) {
            outputSlot = outputSlot.appendTooltip(Text.translatable("emi.emitrades.random_structure").formatted(Formatting.YELLOW));
        }
        widgets.add(outputSlot);
    }
}
