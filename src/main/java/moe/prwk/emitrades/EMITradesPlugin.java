package moe.prwk.emitrades;

import com.mojang.logging.LogUtils;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import moe.prwk.emitrades.mixin.EmiRecipesAccessor;
import moe.prwk.emitrades.recipes.TradeEntry;
import moe.prwk.emitrades.util.Constants;
import moe.prwk.emitrades.util.EmiIngredients;
import moe.prwk.emitrades.util.FakeFactory;
import moe.prwk.emitrades.util.VersionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
//? if >=1.20.6 {
import net.minecraft.core.component.DataComponents;
//?}
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
//? if >=1.21 {
import net.minecraft.tags.EnchantmentTags;
//?}
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
//? if <1.20.6 {
/*import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
*///?} else {
import net.minecraft.world.item.alchemy.PotionContents;
//?}
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("CommentedOutCode")
@EmiEntrypoint
public class EMITradesPlugin implements EmiPlugin {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final EmiRecipeCategory VILLAGER_TRADES
            = new EmiRecipeCategory(VersionUtil.identifier("emitrades", "villager_trades"), EmiStack.of(Items.EMERALD));

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void register(EmiRegistry registry) {
        Minecraft client = Minecraft.getInstance();
        registry.addCategory(VILLAGER_TRADES);
        BuiltInRegistries.VILLAGER_PROFESSION.forEach(
                prof -> {
                    BuiltInRegistries.POINT_OF_INTEREST_TYPE.asHolderIdMap().forEach(
                            poi -> {
                                if (prof.acquirableJobSite().test(poi)) {
                                    poi.value().matchingStates().forEach(
                                            state -> {
                                                EmiStack block = EmiStack.of(state.getBlock());
                                                if (!(EmiRecipesAccessor.getWorkstations()
                                                        .getOrDefault(VILLAGER_TRADES, List.of()).contains(block))) {
                                                    registry.addWorkstation(VILLAGER_TRADES, block);
                                                }
                                            }
                                    );
                                }
                            }
                    );

                    AtomicInteger id = new AtomicInteger();
                    Map<Integer, VillagerTrades.ItemListing[]> offers = VillagerTrades.TRADES.get(prof);

                    if (!(offers == null || offers.isEmpty())) {
                        int level = 0;
                        while (level < 5) {
                            Villager villager1 = (Villager) BuiltInRegistries.ENTITY_TYPE.get(
                                    VersionUtil.identifier("minecraft", "villager")
                            ).create(client.level);
                            if (villager1 != null) {
                                villager1.setVillagerData(villager1.getVillagerData().setProfession(prof).setLevel(level + 1));
                            }
                            for (VillagerTrades.ItemListing offer : offers.get(level + 1)) {
                                if (isVanillaFactory(offer)) {
                                    addEntries(registry, villager1, prof, level, offer, id);
                                } else {
                                    try {
                                        int attempts = 5;
                                        TreeSet<MerchantOffer> genOffers = new TreeSet<>(EMITradesPlugin::compareOffers);
                                        MerchantOffer inOffer;
                                        while (attempts > 0) {
                                            inOffer = offer.getOffer(Objects.requireNonNull(client.player), client.level.random);
                                            if (genOffers.add(inOffer))
                                                attempts++;
                                            else
                                                attempts--;
                                        }
                                        int finalLevel = level;
                                        genOffers.forEach(tradeOffer ->
                                                addEntries(registry,
                                                        villager1, prof, finalLevel,
                                                        new FakeFactory(tradeOffer), id
                                                )
                                        );
                                    } catch (Exception ignored) {}
                                }
                            }
                            level++;
                        }
                    }
                }
        );

        WanderingTrader wanderingTrader = (WanderingTrader) BuiltInRegistries.ENTITY_TYPE
                .get(VersionUtil.identifier("minecraft", "wandering_trader"))
                .create(Objects.requireNonNull(client.level));
        AtomicInteger wanderingTraderId = new AtomicInteger();
        VillagerTrades.WANDERING_TRADER_TRADES.forEach((lvl, offers) -> {
            for (VillagerTrades.ItemListing offer : offers) {
                if (isVanillaFactory(offer)) {
                    addEntries(registry, wanderingTrader, Constants.WANDERING_TRADER, lvl, offer, wanderingTraderId);
                } else {
                    try {
                        int attempts = 5;
                        TreeSet<MerchantOffer> genOffers = new TreeSet<>(EMITradesPlugin::compareOffers);
                        MerchantOffer inOffer;
                        while (attempts > 0) {
                            inOffer = offer.getOffer(Objects.requireNonNull(client.player), client.level.random);
                            if (genOffers.add(inOffer))
                                attempts++;
                            else
                                attempts--;
                        }
                        genOffers.forEach(tradeOffer -> addEntries(
                                registry,
                                wanderingTrader, Constants.WANDERING_TRADER, lvl,
                                new FakeFactory(tradeOffer), wanderingTraderId
                        ));
                    } catch (Exception ignored) {}
                }
            }
        });

        LOGGER.info("Reloaded.");
    }

    private static boolean isVanillaFactory(VillagerTrades.ItemListing offer) {
        return offer instanceof VillagerTrades.SuspiciousStewForEmerald ||
                offer instanceof VillagerTrades.EnchantedItemForEmeralds ||
                offer instanceof VillagerTrades.EnchantBookForEmeralds ||
                offer instanceof VillagerTrades.TreasureMapForEmeralds ||
                offer instanceof VillagerTrades.TippedArrowForItemsAndEmeralds ||
                offer instanceof VillagerTrades.DyedArmorForEmeralds ||
                offer instanceof VillagerTrades.EmeraldsForVillagerTypeItem ||
                offer instanceof VillagerTrades.EmeraldForItems ||
                offer instanceof VillagerTrades.ItemsForEmeralds ||
                offer instanceof VillagerTrades.ItemsAndEmeraldsToItems;
    }

    private static int compareOffers(@NotNull MerchantOffer a, @NotNull MerchantOffer b) {
        int diff = BuiltInRegistries.ITEM.getId(a.getBaseCostA().getItem()) - BuiltInRegistries.ITEM.getId(b.getBaseCostA().getItem());
        if (diff != 0) return diff;
        diff = BuiltInRegistries.ITEM.getId(a.getCostB().getItem()) - BuiltInRegistries.ITEM.getId(b.getCostB().getItem());
        if (diff != 0) return diff;
        diff = BuiltInRegistries.ITEM.getId(a.getResult().getItem()) - BuiltInRegistries.ITEM.getId(b.getResult().getItem());
        return diff;
    }

    private static void addEntries(EmiRegistry registry, AbstractVillager merchant, VillagerProfession prof, int villagerLevel, VillagerTrades.ItemListing offer, AtomicInteger numId) {
        Level level = merchant.level();

        List<EmiIngredient> inputs = new ArrayList<>();
        Optional<Integer> villagerXp = Optional.empty();
        Optional<Integer> maxUses = Optional.empty();

        if (offer instanceof VillagerTrades.EnchantBookForEmeralds factory) {
            villagerXp = Optional.of(factory.villagerXp);

            inputs.add(0, EmiIngredients.rangedAmount(EmiStack.of(Items.EMERALD), 5, 64));
            inputs.add(1, EmiStack.of(Items.BOOK));

            Optional<Integer> finalVillagerXp = villagerXp;
            Optional<Integer> finalMaxUses = maxUses;

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
                                    registry.addRecipe(new TradeEntry(
                                            inputs, List.of(EmiStack.of(stack)), merchant, prof, villagerLevel, finalVillagerXp, finalMaxUses, numId.getAndIncrement())
                                    );
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
                            registry.addRecipe(new TradeEntry(
                                    inputs, List.of(EmiStack.of(stack)), merchant, prof, villagerLevel, finalVillagerXp, finalMaxUses, numId)
                            );
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
                            registry.addRecipe(new TradeEntry(
                                    inputs, List.of(EmiStack.of(stack)), merchant, prof, villagerLevel, finalVillagerXp, finalMaxUses, numId.getAndIncrement())
                            );
                        }
                    }
            );
            *///?}
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
            List<EmiStack> outputs = List.of(EmiStack.of(Items.EMERALD, /*? if >=1.20.4 {*/factory.emeraldAmount/*?} else {*/ /*factory.cost *//*?}*/));
            registry.addRecipe(new TradeEntry(
                    inputs, outputs, merchant, prof, villagerLevel, villagerXp, maxUses, numId.getAndIncrement())
            );
        } else if (offer instanceof VillagerTrades.ItemsForEmeralds factory) {
            villagerXp = Optional.of(factory.villagerXp);
            maxUses = Optional.of(factory.maxUses);

            inputs.add(0, EmiStack.of(Items.EMERALD, factory.emeraldCost));
            inputs.add(1, EmiStack.EMPTY);
            registry.addRecipe(new TradeEntry(
                    inputs, List.of(EmiStack.of(factory.itemStack)), merchant, prof, villagerLevel, villagerXp, maxUses, numId.getAndIncrement())
            );
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
            registry.addRecipe(new TradeEntry(
                    inputs, List.of(EmiStack.of(stack)), merchant, prof, villagerLevel, villagerXp, maxUses, numId.getAndIncrement())
            );
        } else if (offer instanceof VillagerTrades.ItemsAndEmeraldsToItems factory) {
            villagerXp = Optional.of(factory.villagerXp);
            maxUses = Optional.of(factory.maxUses);

            inputs.add(0, EmiStack.of(Items.EMERALD, factory.emeraldCost));
            //? if >=1.20.6 {
            inputs.add(1, EmiStack.of(factory.fromItem.itemStack()));
            //?} else {
            /*inputs.add(1, EmiStack.of(factory.fromItem));
             *///?}

            registry.addRecipe(new TradeEntry(
                    inputs, List.of(EmiStack.of(factory.toItem.copy())), merchant, prof, villagerLevel, villagerXp, maxUses, numId.getAndIncrement())
            );
        } else if (offer instanceof VillagerTrades.EnchantedItemForEmeralds factory) {
            villagerXp = Optional.of(factory.villagerXp);
            maxUses = Optional.of(factory.maxUses);

            inputs.add(0, EmiIngredients.rangedAmount(
                    EmiStack.of(Items.EMERALD),
                    factory.baseEmeraldCost, Math.min(factory.baseEmeraldCost + 19, 64)
            ));
            inputs.add(1, EmiStack.EMPTY);

            Optional<Integer> finalVillagerXp1 = villagerXp;
            Optional<Integer> finalMaxUses1 = maxUses;
            //? if >=1.21 {
            merchant.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                    .getTag(EnchantmentTags.ON_TRADED_EQUIPMENT).ifPresent(
                            holders -> {
                                for (int i = 5; i < 20; i++) {
                                    EnchantmentHelper.getAvailableEnchantmentResults(i, factory.itemStack, holders.stream())
                                            .forEach(enchantment -> {
                                                ItemStack stack = factory.itemStack.copy();
                                                stack.enchant(enchantment.enchantment, enchantment.level);
                                                registry.addRecipe(new TradeEntry(
                                                        inputs, List.of(EmiStack.of(stack)), merchant, prof, villagerLevel, finalVillagerXp1, finalMaxUses1, numId.getAndIncrement())
                                                );
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
                            registry.addRecipe(new TradeEntry(
                                    inputs, List.of(EmiStack.of(stack)), merchant, prof, villagerLevel, finalVillagerXp1, finalMaxUses1, numId)
                            );
                        });
            }
            *///?} else {
            /*for (int i = 5; i < 20; i++) {
                EnchantmentHelper.getAvailableEnchantmentResults(i, factory.itemStack, false)
                        .forEach(enchantment -> {
                            ItemStack stack = factory.itemStack.copy();
                            stack.enchant(enchantment.enchantment, enchantment.level);
                            registry.addRecipe(new TradeEntry(
                                    inputs, List.of(EmiStack.of(stack)), merchant, prof, villagerLevel, finalVillagerXp1, finalMaxUses1, numId.getAndIncrement())
                            );
                        });
            }
            *///?}
        } else if (offer instanceof VillagerTrades.EmeraldsForVillagerTypeItem factory) {
            villagerXp = Optional.of(factory.villagerXp);
            maxUses = Optional.of(factory.maxUses);

            Optional<Integer> finalVillagerXp2 = villagerXp;
            Optional<Integer> finalMaxUses2 = maxUses;
            factory.trades.values().forEach(item -> {
                inputs.add(0, EmiStack.of(item, factory.cost));
                inputs.add(1, EmiStack.EMPTY);
                registry.addRecipe(new TradeEntry(
                        inputs, List.of(EmiStack.of(Items.EMERALD)), merchant, prof, villagerLevel, finalVillagerXp2, finalMaxUses2, numId.getAndIncrement())
                );
            });
        } else if (offer instanceof VillagerTrades.TippedArrowForItemsAndEmeralds factory) {
            villagerXp = Optional.of(factory.villagerXp);
            maxUses = Optional.of(factory.maxUses);

            inputs.add(0, EmiStack.of(Items.EMERALD, factory.emeraldCost));
            inputs.add(1, EmiStack.of(factory.fromItem, factory.fromCount));

            Optional<Integer> finalVillagerXp3 = villagerXp;
            Optional<Integer> finalMaxUses3 = maxUses;
            //? if >=1.20.6 {
            BuiltInRegistries.POTION.holders().filter((reference) ->
                    !reference.value().getEffects().isEmpty() && level.potionBrewing().isBrewablePotion(reference)
            ).forEach(potion -> {
                ItemStack stack = new ItemStack(factory.toItem.getItem(), factory.toCount);
                stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
                registry.addRecipe(new TradeEntry(
                        inputs, List.of(EmiStack.of(stack)), merchant, prof, villagerLevel, finalVillagerXp3, finalMaxUses3, numId.getAndIncrement())
                );
            });
            //?} else {
            /*BuiltInRegistries.POTION.holders().filter((reference) ->
                    !reference.value().getEffects().isEmpty() && PotionBrewing.isBrewablePotion(reference.value())
            ).forEach(potion -> {
                ItemStack stack = new ItemStack(factory.toItem.getItem(), factory.toCount);
                stack = PotionUtils.setPotion(stack, potion.value());
                registry.addRecipe(new TradeEntry(
                        inputs, List.of(EmiStack.of(stack)), merchant, prof, villagerLevel, finalVillagerXp3, finalMaxUses3, numId.getAndIncrement())
                );
            });
            *///?}
        } else if (offer instanceof VillagerTrades.TreasureMapForEmeralds factory) {
            villagerXp = Optional.of(factory.villagerXp);
            maxUses = Optional.of(factory.maxUses);

            inputs.add(0, EmiStack.of(Items.EMERALD, factory.emeraldCost));
            inputs.add(1, EmiStack.of(Items.COMPASS));

            MutableComponent tooltip = Component.translatable("emi.emitrades.structure")
                    .withStyle(ChatFormatting.YELLOW).append(Component.translatable(factory.displayName));
            registry.addRecipe(new TradeEntry(
                    inputs, List.of(EmiIngredients.asStack(EmiIngredients.appendTooltips(
                            EmiStack.of(Items.FILLED_MAP),
                            ClientTooltipComponent.create(tooltip.getVisualOrderText())
                    ))), merchant, prof, villagerLevel, villagerXp, maxUses, numId.getAndIncrement())
            );
        } else if (offer instanceof VillagerTrades.DyedArmorForEmeralds factory) {
            villagerXp = Optional.of(factory.villagerXp);
            maxUses = Optional.of(factory.maxUses);

            inputs.add(0, EmiStack.of(Items.EMERALD, factory.value));
            inputs.add(1, EmiStack.EMPTY);
            registry.addRecipe(new TradeEntry(
                    inputs, List.of(EmiIngredients.asStack(EmiIngredients.appendTooltips(
                            EmiStack.of(factory.item),
                            ClientTooltipComponent.create(
                                    Component.translatable("emi.emitrades.random_colored").getVisualOrderText()
                            )
                    ))), merchant, prof, villagerLevel, villagerXp, maxUses, numId.getAndIncrement())
            );
        } else if (offer instanceof FakeFactory factory) {
            villagerXp = Optional.of(factory.villagerXp);
            maxUses = Optional.of(factory.maxUses);

            inputs.add(0, EmiStack.of(factory.first));
            inputs.add(1, EmiStack.of(factory.second));

            registry.addRecipe(new TradeEntry(
                    inputs, List.of(EmiStack.of(factory.sell)), merchant, prof, villagerLevel, villagerXp, maxUses, numId.getAndIncrement())
            );
        } else {
            inputs.add(0, EmiStack.EMPTY);
            inputs.add(1, EmiStack.EMPTY);
            registry.addRecipe(new TradeEntry(
                    inputs, List.of(EmiStack.EMPTY), merchant, prof, villagerLevel, villagerXp, maxUses, numId.getAndIncrement())
            );
        }
    }
}
