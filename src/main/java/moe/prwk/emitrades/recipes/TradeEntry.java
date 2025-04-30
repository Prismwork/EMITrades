package moe.prwk.emitrades.recipes;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import moe.prwk.emitrades.util.EmiIngredients;
import moe.prwk.emitrades.util.VersionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"UnstableApiUsage", "SequencedCollectionMethodCanBeUsed"})
public class TradeEntry extends AbstractTradeEntry {
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;
    private final List<EmiIngredient> catalysts;
    private final Font textRenderer;
    private final MutableComponent title;
    private int villagerXp = 0;
    private int maxUses = 0;

    public TradeEntry(Trade trade, int numId) {
        super(trade, VersionUtil.identifier(
                "emitrades", "/trade/"
                        + trade.profession().name().replace(':', '/')
                        + "/offer_" + numId
        ));

        this.textRenderer = Minecraft.getInstance().font;

        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.catalysts = new ArrayList<>();

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

        if (offer instanceof VillagerTrades.EnchantBookForEmeralds factory) {
            inputs.add(0, EmiIngredients.rangedAmount(EmiStack.of(Items.EMERALD), 5, 64));
            inputs.add(1, EmiStack.of(Items.BOOK));

            List<EmiStack> out = new ArrayList<>();
            //? if >=1.21 {
            merchant.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
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
            //? } else {
            /*throw new AssertionError("Not yet implemented");
            *///? }
            outputs.add(0, EmiIngredients.asStack(new ListEmiIngredient(out, 1)));
        } else if (offer instanceof VillagerTrades.EmeraldForItems factory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, factory.emeraldAmount));
            inputs.add(1, EmiStack.EMPTY);
            //? if >=1.20.6 {
            outputs.add(0, EmiStack.of(factory.itemStack.itemStack()));
            //? } else {
            /*outputs.add(0, EmiStack.of(factory.itemStack));
            *///? }
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
        return 0;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {

    }
}
