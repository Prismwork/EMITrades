package moe.prwk.emitrades;

import com.mojang.logging.LogUtils;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import moe.prwk.emitrades.recipes.Trade;
import moe.prwk.emitrades.recipes.TradeEntry;
import moe.prwk.emitrades.util.LivingEntityEmiStack;
import moe.prwk.emitrades.util.VersionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

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
                    Villager villager = (Villager) BuiltInRegistries.ENTITY_TYPE
                            .get(VersionUtil.identifier("minecraft", "villager")).create(Objects.requireNonNull(client.level));

                    if (villager != null) {
                        villager.setVillagerData(villager.getVillagerData().setProfession(prof).setLevel(5));
                        registry.addWorkstation(VILLAGER_TRADES, LivingEntityEmiStack.ofScaled(villager, 1.0f));
                    }

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
                                    registry.addRecipe(new TradeEntry(
                                            Trade.impl(villager1, prof, offer, level + 1),
                                            id.get()
                                    ));
                                    id.getAndIncrement();
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
                                        genOffers.forEach(tradeOffer -> {
                                            registry.addRecipe(new TradeEntry(
                                                    Trade.impl(villager1, prof, offer, finalLevel + 1),
                                                    id.get()
                                            ));
                                            id.getAndIncrement();
                                        });
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
        registry.addWorkstation(VILLAGER_TRADES, LivingEntityEmiStack.of(wanderingTrader));
        AtomicInteger wanderingTraderId = new AtomicInteger();
        VillagerTrades.WANDERING_TRADER_TRADES.forEach((lvl, offers) -> {
            for (VillagerTrades.ItemListing offer : offers) {
                if (isVanillaFactory(offer)) {
                    registry.addRecipe(new TradeEntry(
                            Trade.impl(wanderingTrader, Trade.WANDERING_TRADER, offer, lvl),
                            wanderingTraderId.get()
                    ));
                    wanderingTraderId.getAndIncrement();
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
                        int finalLevel = lvl;
                        genOffers.forEach(tradeOffer -> {
                            registry.addRecipe(new TradeEntry(
                                    Trade.impl(wanderingTrader, Trade.WANDERING_TRADER, offer, finalLevel),
                                    wanderingTraderId.get()
                            ));
                            wanderingTraderId.getAndIncrement();
                        });
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
}
