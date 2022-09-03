package io.github.pkstdev.emitrades;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import io.github.pkstdev.emitrades.recipe.VillagerTrade;
import io.github.pkstdev.emitrades.util.TradeProfile;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class EMITradesPlugin implements EmiPlugin {
    public static final EmiRecipeCategory VILLAGER_TRADES
            = new EmiRecipeCategory(new Identifier("emitrades", "villager_trades"), EmiStack.of(Items.EMERALD));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(VILLAGER_TRADES);
        Random random = new Random();
        for (VillagerProfession profession : Registry.VILLAGER_PROFESSION) {
            AtomicInteger id = new AtomicInteger();
            Int2ObjectMap<TradeOffers.Factory[]> offers = TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(profession);
            if (offers == null || offers.isEmpty()) continue;
            int level = 0;
            while (level < 5) {
                for (TradeOffers.Factory offer : offers.get(level + 1)) {
                    if (isVanillaFactory(offer)) {
                        registry.addRecipe(new VillagerTrade(new TradeProfile(profession, offer, level + 1), id.get()));
                        id.getAndIncrement();
                    } else {
                        try {
                            int attempts = 5;
                            TreeSet<TradeOffer> genOffers = new TreeSet<>(this::compareOffers);
                            TradeOffer inOffer;
                            while (attempts > 0) {
                                inOffer = offer.create(MinecraftClient.getInstance().player, random);
                                if (offer != null && genOffers.add(inOffer))
                                    attempts++;
                                else
                                    attempts--;
                            }
                            int finalLevel = level;
                            genOffers.forEach(tradeOffer -> {
                                registry.addRecipe(new VillagerTrade(new TradeProfile(profession, new FakeFactory(tradeOffer), finalLevel + 1), id.get()));
                                id.getAndIncrement();
                            });
                        } catch (Exception ignored) {}
                    }
                }
                level++;
            }
        }
    }

    private static boolean isVanillaFactory(TradeOffers.Factory offer) {
        return offer instanceof TradeOffers.SellSuspiciousStewFactory ||
                offer instanceof TradeOffers.SellEnchantedToolFactory ||
                offer instanceof TradeOffers.EnchantBookFactory ||
                offer instanceof TradeOffers.SellMapFactory ||
                offer instanceof TradeOffers.SellPotionHoldingItemFactory ||
                offer instanceof TradeOffers.SellDyedArmorFactory ||
                offer instanceof TradeOffers.TypeAwareBuyForOneEmeraldFactory ||
                offer instanceof TradeOffers.SellItemFactory ||
                offer instanceof TradeOffers.BuyForOneEmeraldFactory ||
                offer instanceof TradeOffers.ProcessItemFactory;
    }

    private int compareOffers(@NotNull TradeOffer a, @NotNull TradeOffer b) {
        int diff = Registry.ITEM.getRawId(a.getOriginalFirstBuyItem().getItem()) - Registry.ITEM.getRawId(b.getOriginalFirstBuyItem().getItem());
        if (diff != 0) return diff;
        diff = Registry.ITEM.getRawId(a.getSecondBuyItem().getItem()) - Registry.ITEM.getRawId(b.getSecondBuyItem().getItem());
        if (diff != 0) return diff;
        diff = Registry.ITEM.getRawId(a.getSellItem().getItem()) - Registry.ITEM.getRawId(b.getSellItem().getItem());
        return diff;
    }

    public static class FakeFactory implements TradeOffers.Factory {
        public final ItemStack first;
        public final ItemStack second;
        public final ItemStack sell;

        public FakeFactory(TradeOffer offer) {
            this.first = offer.getOriginalFirstBuyItem();
            this.second = offer.getSecondBuyItem();
            this.sell = offer.getSellItem();
        }

        @Nullable
        @Override
        public TradeOffer create(Entity entity, Random random) {
            throw new AssertionError(); // Not actually functional, only used for satisfying TradeProfile so this method throws an error
        }
    }
}
