/*
 * Copyright (c) 2019-2022 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.galacticraft.mod.block.entity;

import dev.galacticraft.api.block.entity.RecipeMachineBlockEntity;
import dev.galacticraft.api.machine.MachineStatus;
import dev.galacticraft.api.machine.MachineStatuses;
import dev.galacticraft.api.machine.storage.MachineItemStorage;
import dev.galacticraft.api.machine.storage.display.ItemSlotDisplay;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.ResourceType;
import dev.galacticraft.api.machine.storage.io.SlotType;
import dev.galacticraft.api.screen.RecipeMachineScreenHandler;
import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.Galacticraft;
import dev.galacticraft.mod.api.Tier1EnergyMachine;
import dev.galacticraft.mod.item.GalacticraftItem;
import dev.galacticraft.mod.machine.GalacticraftMachineStatus;
import dev.galacticraft.mod.machine.storage.io.GalacticraftSlotTypes;
import dev.galacticraft.mod.recipe.FabricationRecipe;
import dev.galacticraft.mod.recipe.GalacticraftRecipe;
import dev.galacticraft.mod.screen.GalacticraftScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public class CircuitFabricatorBlockEntity extends RecipeMachineBlockEntity<Inventory, FabricationRecipe> implements Tier1EnergyMachine {
    private static final SlotType<Item, ItemVariant> DIAMOND_INPUT = SlotType.create(new Identifier(Constant.MOD_ID, "diamond_input"), TextColor.fromRgb(0xFF0000), new TranslatableText("slot_type.galacticraft.diamond_input"), v -> v.getItem() == Items.DIAMOND, ResourceFlow.INPUT, ResourceType.ITEM);
    private static final SlotType<Item, ItemVariant> SILICON_INPUT = SlotType.create(new Identifier(Constant.MOD_ID, "silicon_input"), TextColor.fromRgb(0xFF0000), new TranslatableText("slot_type.galacticraft.silicon_input"), v -> v.getItem() == GalacticraftItem.RAW_SILICON, ResourceFlow.INPUT, ResourceType.ITEM);
    private static final SlotType<Item, ItemVariant> REDSTONE_INPUT = SlotType.create(new Identifier(Constant.MOD_ID, "redstone_input"), TextColor.fromRgb(0xFF0000), new TranslatableText("slot_type.galacticraft.redstone_input"), v -> v.getItem() == Items.REDSTONE, ResourceFlow.INPUT, ResourceType.ITEM);

    public static final int CHARGE_SLOT = 0;
    public static final int INPUT_SLOT_DIAMOND = 1;
    public static final int INPUT_SLOT_SILICON = 2;
    public static final int INPUT_SLOT_SILICON_2 = 3;
    public static final int INPUT_SLOT_REDSTONE = 4;
    public static final int INPUT_SLOT = 5;
    public static final int OUTPUT_SLOT = 6;

    private final Inventory craftingInv = this.itemStorage().subInv(INPUT_SLOT, 1);

    public CircuitFabricatorBlockEntity(BlockPos pos, BlockState state) {
        super(GalacticraftBlockEntityType.CIRCUIT_FABRICATOR, pos, state, GalacticraftRecipe.FABRICATION_TYPE);
    }

    @Override
    protected @NotNull MachineItemStorage createItemStorage() {
        return MachineItemStorage.Builder.create()
                .addSlot(GalacticraftSlotTypes.ENERGY_CHARGE, new ItemSlotDisplay(8, 70))
                .addSlot(DIAMOND_INPUT, new ItemSlotDisplay(31, 15))
                .addSlot(SILICON_INPUT, new ItemSlotDisplay(62, 45))
                .addSlot(SILICON_INPUT, new ItemSlotDisplay(62, 63))
                .addSlot(REDSTONE_INPUT, new ItemSlotDisplay(107, 70))
                .addSlot(GalacticraftSlotTypes.ITEM_INPUT, new ItemSlotDisplay(134, 15))
                .addSlot(GalacticraftSlotTypes.ITEM_OUTPUT, new ItemSlotDisplay(152, 70))
                .build();
    }

    @Override
    public long getEnergyCapacity() {
        return Galacticraft.CONFIG_MANAGER.get().machineEnergyStorageSize();
    }

    @Override
    public void tickConstant(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull BlockState state) {
        super.tickConstant(world, pos, state);
        world.getProfiler().push("charge");
        this.attemptChargeFromStack(CHARGE_SLOT);
        world.getProfiler().pop();
    }

    @Override
    protected @Nullable MachineStatus extractResourcesToWork(@NotNull TransactionContext context) {
        if (this.energyStorage().extract(Galacticraft.CONFIG_MANAGER.get().circuitFabricatorEnergyConsumptionRate(), context) != Galacticraft.CONFIG_MANAGER.get().circuitFabricatorEnergyConsumptionRate()) {
            return MachineStatuses.NOT_ENOUGH_ENERGY;
        }
        return super.extractResourcesToWork(context);
    }

    @Override
    public @NotNull Inventory craftingInv() {
        return this.craftingInv;
    }

    @Override
    protected boolean outputStacks(@NotNull FabricationRecipe recipe, @NotNull TransactionContext context) {
        ItemStack output = recipe.getOutput();
        try (Transaction transaction = Transaction.openNested(context)) {
            if (this.itemStorage().insert(OUTPUT_SLOT, ItemVariant.of(output), output.getCount(), transaction) == output.getCount()) {
                transaction.commit();
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean extractCraftingMaterials(@NotNull FabricationRecipe recipe, @NotNull TransactionContext context) {
        try (Transaction transaction = Transaction.openNested(context)) {
            if (this.itemStorage().extract(INPUT_SLOT_DIAMOND, Items.DIAMOND, 1, transaction) == 1) {
                if (this.itemStorage().extract(INPUT_SLOT_SILICON, GalacticraftItem.RAW_SILICON, 1, transaction) == 1) {
                    if (this.itemStorage().extract(INPUT_SLOT_SILICON_2, GalacticraftItem.RAW_SILICON, 1, transaction) == 1) {
                        if (this.itemStorage().extract(INPUT_SLOT_REDSTONE, Items.REDSTONE, 1, transaction) == 1) {
                            if (recipe.getIngredients().get(0).test(this.itemStorage().extract(INPUT_SLOT, 1, transaction))) {
                                transaction.commit();
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected @NotNull MachineStatus workingStatus() {
        return GalacticraftMachineStatus.FABRICATING;
    }

    @Override
    protected @NotNull Optional<FabricationRecipe> findValidRecipe(@NotNull World world) {
        try (Transaction transaction = Transaction.openOuter()) {
            if (this.itemStorage().getSlot(INPUT_SLOT_DIAMOND).simulateExtract(ItemVariant.of(Items.DIAMOND), 1, transaction) == 1
                    && this.itemStorage().getSlot(INPUT_SLOT_SILICON).simulateExtract(ItemVariant.of(GalacticraftItem.RAW_SILICON), 1, transaction) == 1
                    && this.itemStorage().getSlot(INPUT_SLOT_SILICON_2).simulateExtract(ItemVariant.of(GalacticraftItem.RAW_SILICON), 1, transaction) == 1
                    && this.itemStorage().getSlot(INPUT_SLOT_REDSTONE).simulateExtract(ItemVariant.of(Items.REDSTONE), 1, transaction) == 1) {
                return super.findValidRecipe(world);
            }
        }
        return Optional.empty();
    }

    @Override
    protected int getProcessTime(@NotNull FabricationRecipe recipe) {
        return recipe.getProcessingTime();
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        if (this.security().hasAccess(player)) {
            return RecipeMachineScreenHandler.create(
                    syncId,
                    player,
                    this,
                    GalacticraftScreenHandlerType.CIRCUIT_FABRICATOR_HANDLER,
                    94
            );
        }
        return null;
    }
}