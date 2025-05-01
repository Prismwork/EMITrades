package moe.prwk.emitrades.recipes;

import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import moe.prwk.emitrades.util.EmiIngredients;
import moe.prwk.emitrades.util.LivingEntityEmiStack;
import moe.prwk.emitrades.util.VersionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
//? if >=1.20.6 {
import net.minecraft.core.component.DataComponents;
//?}
import net.minecraft.core.registries.BuiltInRegistries;
//? if >=1.20.6 {
import net.minecraft.core.registries.Registries;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
//? if >=1.21 {
import net.minecraft.tags.EnchantmentTags;
//?}
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
//? if <1.20.6 {
/*import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
*///?} else {
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
//?}
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;

import java.util.*;

@SuppressWarnings({
        "unused",
        "UnstableApiUsage",
        "OptionalUsedAsFieldOrParameterType",
        "CommentedOutCode"
})
public class TradeEntry extends AbstractTradeEntry {
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;
    private final List<EmiIngredient> catalysts;
    private final Font textRenderer;
    private final MutableComponent title;
    private Optional<Integer> villagerXp = Optional.empty();
    private Optional<Integer> maxUses = Optional.empty();

    public TradeEntry(Trade trade, int numId) {
        super(trade, VersionUtil.identifier(
                "emitrades", "/trade/"
                        + trade.profession().name().replace(':', '/')
                        + "/offer_" + numId
        ));

        this.textRenderer = Minecraft.getInstance().font;

        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.catalysts = trade.merchant() != null ?
                List.of(LivingEntityEmiStack.ofScaled(trade.merchant(), 1.0f)) : List.of();

        AbstractVillager merchant = trade.merchant();
        VillagerProfession profession = trade.profession();
        if (profession.equals(Trade.WANDERING_TRADER)) {
            this.title = Component.translatable("emi.emitrades.placeholder.wandering_trader");
        } else {
            this.title = Component.translatable("entity.minecraft.villager." + profession.name().substring(profession.name().lastIndexOf(":") + 1))
                    .append(" - ").append(Component.translatable("emi.emitrades.profession.lvl." + trade.level()));
        }

        Map<Integer, VillagerTrades.ItemListing[]> __offers = trade.offers();
        if (__offers.size() != 1)
            throw new IllegalArgumentException("Invalid offer input for TradeEntry: must contain exactly one offer");
        VillagerTrades.ItemListing[] __offer = __offers.get(0);
        if (__offer.length != 1)
            throw new IllegalArgumentException("Invalid offer input for TradeEntry: must contain exactly one offer");
        VillagerTrades.ItemListing offer = __offer[0];

        Level level = merchant.level();
        if (offer instanceof VillagerTrades.EnchantBookForEmeralds factory) {
            villagerXp = Optional.of(factory.villagerXp);

            inputs.add(0, EmiIngredients.rangedAmount(EmiStack.of(Items.EMERALD), 5, 64));
            inputs.add(1, EmiStack.of(Items.BOOK));

            List<EmiStack> out = new ArrayList<>();
            //? if >=1.21 {
            merchant.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                    .getTag(factory.tradeableEnchantments).ifPresent(
                            holders -> holders.forEach(holder -> {
                                Enchantment enchantment = holder.value();
                                int min = Math.max(enchantment.getMinLevel(), factory.minLevel);
                                int max = Math.min(enchantment.getMaxLevel(), factory.maxLevel);

                                for (int i = min; i <= max; i++) {
                                    ItemStack stack = EnchantedBookItem.createForEnchantment(
                                            new EnchantmentInstance(holder, i)
                                    );
                                    out.add(EmiStack.of(stack));
                                }
                            })
                    );
            //?} elif >=1.20.4 {
            /*factory.tradeableEnchantments.forEach(
                    enchantment -> {
                        int min = Math.max(enchantment.getMinLevel(), factory.minLevel);
                        int max = Math.min(enchantment.getMaxLevel(), factory.maxLevel);

                        for (int i = min; i <= max; i++) {
                            ItemStack stack = EnchantedBookItem.createForEnchantment(
                                    new EnchantmentInstance(enchantment, i)
                            );
                            out.add(EmiStack.of(stack));
                        }
                    }
            );
            *///?} else {
            /*BuiltInRegistries.ENCHANTMENT.stream().filter(Enchantment::isTradeable).forEach(
                    enchantment -> {
                        int min = enchantment.getMinLevel();
                        int max = enchantment.getMaxLevel();

                        for (int i = min; i <= max; i++) {
                            ItemStack stack = EnchantedBookItem.createForEnchantment(
                                    new EnchantmentInstance(enchantment, i)
                            );
                            out.add(EmiStack.of(stack));
                        }
                    }
            );
            *///?}
            outputs.add(0, EmiIngredients.asStack(new ListEmiIngredient(out, 1)));
        } else if (offer instanceof VillagerTrades.EmeraldForItems factory) {
            villagerXp = Optional.of(factory.villagerXp);
            maxUses = Optional.of(factory.maxUses);

            //? if >=1.20.6 {
            inputs.add(0, EmiStack.of(factory.itemStack.itemStack()));
            //?} elif >= 1.20.4 {
            /*inputs.add(0, EmiStack.of(factory.itemStack));
            *///?} else {
            /*inputs.add(0, EmiStack.of(factory.item));
            *///?}
            inputs.add(1, EmiStack.EMPTY);
            outputs.add(0, EmiStack.of(Items.EMERALD, /*? if >=1.20.4 {*/factory.emeraldAmount/*?} else {*/ /*factory.cost *//*?}*/));
        } else if (offer instanceof VillagerTrades.ItemsForEmeralds factory) {
            villagerXp = Optional.of(factory.villagerXp);
            maxUses = Optional.of(factory.maxUses);

            inputs.add(0, EmiStack.of(Items.EMERALD, factory.emeraldCost));
            inputs.add(1, EmiStack.EMPTY);
            outputs.add(0, EmiStack.of(factory.itemStack));
        } else if (offer instanceof VillagerTrades.SuspiciousStewForEmerald factory) {
            villagerXp = Optional.of(factory.xp);

            inputs.add(0, EmiStack.of(Items.EMERALD));
            inputs.add(1, EmiStack.EMPTY);
            ItemStack stack = new ItemStack(Items.SUSPICIOUS_STEW, 1);
            //? if >=1.20.6 {
            stack.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, factory.effects);
            //?} elif >=1.20.4 {
            /*SuspiciousStewItem.appendMobEffects(stack, factory.effects);
            *///?} else {
            /*SuspiciousStewItem.saveMobEffect(stack, factory.effect, factory.duration);
            *///?}
            outputs.add(0, EmiStack.of(stack));
        } else if (offer instanceof VillagerTrades.ItemsAndEmeraldsToItems factory) {
            villagerXp = Optional.of(factory.villagerXp);
            maxUses = Optional.of(factory.maxUses);

            inputs.add(0, EmiStack.of(Items.EMERALD, factory.emeraldCost));
            //? if >=1.20.6 {
            inputs.add(1, EmiStack.of(factory.fromItem.itemStack()));
            //?} else {
            /*inputs.add(0, EmiStack.of(factory.fromItem));
            *///?}

            outputs.add(0, EmiStack.of(factory.toItem.copy()));
        } else if (offer instanceof VillagerTrades.EnchantedItemForEmeralds factory) {
            villagerXp = Optional.of(factory.villagerXp);
            maxUses = Optional.of(factory.maxUses);

            inputs.add(0, EmiIngredients.rangedAmount(
                    EmiStack.of(Items.EMERALD),
                    factory.baseEmeraldCost, Math.min(factory.baseEmeraldCost + 19, 64)
            ));
            inputs.add(1, EmiStack.EMPTY);

            List<EmiStack> out = new ArrayList<>();
            //? if >=1.21 {
            merchant.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                    .getTag(EnchantmentTags.ON_TRADED_EQUIPMENT).ifPresent(
                            holders -> {
                                for (int i = 5; i < 20; i++) {
                                    EnchantmentHelper.getAvailableEnchantmentResults(i, factory.itemStack, holders.stream())
                                            .forEach(enchantment -> {
                                                ItemStack stack = factory.itemStack.copy();
                                                stack.enchant(enchantment.enchantment, enchantment.level);
                                                out.add(EmiStack.of(stack));
                                            });
                                }
                            }
                    );
            //?} elif >=1.20.6 {
            /*for (int i = 5; i < 20; i++) {
                EnchantmentHelper.getAvailableEnchantmentResults(level.enabledFeatures(), i, factory.itemStack, false)
                        .forEach(enchantment -> {
                            ItemStack stack = factory.itemStack.copy();
                            stack.enchant(enchantment.enchantment, enchantment.level);
                            out.add(EmiStack.of(stack));
                        });
            }
            *///?} else {
            /*for (int i = 5; i < 20; i++) {
                EnchantmentHelper.getAvailableEnchantmentResults(i, factory.itemStack, false)
                        .forEach(enchantment -> {
                            ItemStack stack = factory.itemStack.copy();
                            stack.enchant(enchantment.enchantment, enchantment.level);
                            out.add(EmiStack.of(stack));
                        });
            }
            *///?}
            outputs.add(0, EmiIngredients.asStack(new ListEmiIngredient(out, factory.itemStack.getCount())));
        } else if (offer instanceof VillagerTrades.EmeraldsForVillagerTypeItem factory) {
            villagerXp = Optional.of(factory.villagerXp);
            maxUses = Optional.of(factory.maxUses);

            List<EmiStack> stacks = new ArrayList<>();
            factory.trades.values().forEach(item -> stacks.add(EmiStack.of(item)));
            inputs.add(0, new ListEmiIngredient(stacks, factory.cost));
            inputs.add(1, EmiStack.EMPTY);
            outputs.add(0, EmiStack.of(Items.EMERALD));
        } else if (offer instanceof VillagerTrades.TippedArrowForItemsAndEmeralds factory) {
            villagerXp = Optional.of(factory.villagerXp);
            maxUses = Optional.of(factory.maxUses);

            inputs.add(0, EmiStack.of(Items.EMERALD, factory.emeraldCost));
            inputs.add(1, EmiStack.of(factory.fromItem, factory.fromCount));

            List<EmiStack> out = new ArrayList<>();
            //? if >=1.20.6 {
            BuiltInRegistries.POTION.holders().filter((reference) ->
                    !reference.value().getEffects().isEmpty() && level.potionBrewing().isBrewablePotion(reference)
            ).forEach(potion -> {
                ItemStack stack = new ItemStack(factory.toItem.getItem(), factory.toCount);
                stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
                out.add(EmiStack.of(stack));
            });
            //?} else {
            /*BuiltInRegistries.POTION.holders().filter((reference) ->
                    !reference.value().getEffects().isEmpty() && PotionBrewing.isBrewablePotion(reference.value())
            ).forEach(potion -> {
                ItemStack stack = new ItemStack(factory.toItem.getItem(), factory.toCount);
                stack = PotionUtils.setPotion(stack, potion.value());
                out.add(EmiStack.of(stack));
            });
            *///?}
            outputs.add(0, EmiIngredients.asStack(new ListEmiIngredient(out, factory.toCount)));
        } else if (offer instanceof VillagerTrades.TreasureMapForEmeralds factory) {
            villagerXp = Optional.of(factory.villagerXp);
            maxUses = Optional.of(factory.maxUses);

            inputs.add(0, EmiStack.of(Items.EMERALD, factory.emeraldCost));
            inputs.add(1, EmiStack.of(Items.COMPASS));

            MutableComponent tooltip = Component.translatable("emi.emitrades.structure")
                    .withStyle(ChatFormatting.YELLOW).append(Component.translatable(factory.displayName));
            outputs.add(0, EmiIngredients.asStack(EmiIngredients.appendTooltips(
                    EmiStack.of(Items.FILLED_MAP),
                    ClientTooltipComponent.create(tooltip.getVisualOrderText())
            )));
        } else if (offer instanceof VillagerTrades.DyedArmorForEmeralds factory) {
            villagerXp = Optional.of(factory.villagerXp);
            maxUses = Optional.of(factory.maxUses);

            inputs.add(0, EmiStack.of(Items.EMERALD, factory.value));
            inputs.add(1, EmiStack.EMPTY);
            outputs.add(0, EmiIngredients.asStack(EmiIngredients.appendTooltips(
                    EmiStack.of(factory.item),
                    ClientTooltipComponent.create(
                            Component.translatable("emi.emitrades.random_colored").getVisualOrderText()
                    )
            )));
        } else if (offer instanceof Trade.FakeFactory factory) {
            villagerXp = Optional.of(factory.villagerXp);
            maxUses = Optional.of(factory.maxUses);

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
        int extraWidth = catalysts.isEmpty() ? 0 : 21;
        return (catalysts.isEmpty()) ?
                Math.max(86, textRenderer.width(title) + 2) :
                Math.max(extraWidth + 85, extraWidth + textRenderer.width(title));
    }

    @Override
    public int getDisplayHeight() {
        if (villagerXp.isPresent() || maxUses.isPresent()) return 28 + 9;
        return 28;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void addWidgets(WidgetHolder widgets) {
        if (catalysts.isEmpty()) {
            widgets.addText(title,
                    (getDisplayWidth() - textRenderer.width(title)) / 2, 0, 16777215, true);
            widgets.addSlot(inputs.get(0), getDisplayWidth() / 2 - 42, 10);
            widgets.addSlot(inputs.get(1), getDisplayWidth() / 2 - 22, 10);
            widgets.addTexture(EmiTexture.EMPTY_ARROW, getDisplayWidth() / 2 - 3, 10);
            SlotWidget outputSlot = new SlotWidget(outputs.get(0), getDisplayWidth() / 2 + 22, 10).recipeContext(this);
            widgets.add(outputSlot);

            if (villagerXp.isPresent()) {
                Component xp = Component.literal("XP: " + villagerXp.get());
                int xpX = (getDisplayWidth() - textRenderer.width(xp)) / 2;
                int xpY = getDisplayHeight() - 8;
                widgets.addText(xp, xpX + 1, xpY, 0, false);
                widgets.addText(xp, xpX - 1, xpY, 0, false);
                widgets.addText(xp, xpX, xpY + 1, 0, false);
                widgets.addText(xp, xpX, xpY - 1, 0, false);
                widgets.addText(xp, xpX, xpY, 0x80FF20, false);
            }

            if (maxUses.isPresent()) {
                Component max = Component.literal("MAX: " + maxUses.get());
                int maxX = (getDisplayWidth() + textRenderer.width(max)) / 2;
                int maxY = getDisplayHeight() - 8;
                widgets.addText(max, maxX, maxY, 0xD48333, true);
            }
        } else {
            SlotWidget merchantSlot = new SlotWidget(catalysts.get(0), 1, 6).drawBack(false);
            if (trade.merchant() instanceof Villager villager) {
                merchantSlot.appendTooltip(Component.translatable("emi.emitrades.profession.lvl." + villager.getVillagerData().getLevel())
                        .withStyle(ChatFormatting.YELLOW));
            }
            widgets.add(merchantSlot);
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
}
