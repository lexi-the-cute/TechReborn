/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017 TechReborn
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

package techreborn.tiles.multiblock;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import reborncore.api.recipe.IRecipeCrafterProvider;
import reborncore.api.tile.IInventoryProvider;
import reborncore.api.IToolDrop;
import reborncore.common.misc.Location;
import reborncore.common.multiblock.IMultiblockPart;
import reborncore.common.powerSystem.TilePowerAcceptor;
import reborncore.common.recipes.RecipeCrafter;
import reborncore.common.util.Inventory;
import techreborn.api.Reference;
import techreborn.api.recipe.ITileRecipeHandler;
import techreborn.api.recipe.machines.BlastFurnaceRecipe;
import techreborn.blocks.BlockMachineCasing;
import techreborn.client.container.IContainerProvider;
import techreborn.client.container.builder.BuiltContainer;
import techreborn.client.container.builder.ContainerBuilder;
import techreborn.init.ModBlocks;
import techreborn.multiblocks.MultiBlockCasing;
import techreborn.tiles.TileMachineCasing;

public class TileIndustrialBlastFurnace extends TilePowerAcceptor implements IToolDrop, IInventoryProvider,
	ITileRecipeHandler<BlastFurnaceRecipe>, IRecipeCrafterProvider, IContainerProvider {

	public Inventory inventory;
	public RecipeCrafter crafter;
	public MultiblockChecker multiblockChecker;
	private int cachedHeat;

	public TileIndustrialBlastFurnace() {
		super();
		// TODO configs
		this.inventory = new Inventory(5, "TileIndustrialBlastFurnace", 64, this);
		final int[] inputs = new int[2];
		inputs[0] = 0;
		inputs[1] = 1;
		final int[] outputs = new int[2];
		outputs[0] = 2;
		outputs[1] = 3;
		this.crafter = new RecipeCrafter(Reference.blastFurnaceRecipe, this, 2, 2, this.inventory, inputs, outputs);
	}
	
	public boolean getMutliBlock() {
		final boolean layer0 = this.multiblockChecker.checkRectY(1, 1, MultiblockChecker.CASING_ANY, MultiblockChecker.ZERO_OFFSET);
		final boolean layer1 = this.multiblockChecker.checkRingY(1, 1, MultiblockChecker.CASING_ANY, new BlockPos(0, 1, 0));
		final boolean layer2 = this.multiblockChecker.checkRingY(1, 1, MultiblockChecker.CASING_ANY, new BlockPos(0, 2, 0));
		final boolean layer3 = this.multiblockChecker.checkRectY(1, 1, MultiblockChecker.CASING_ANY, new BlockPos(0, 3, 0));
		final Block centerBlock1 = this.multiblockChecker.getBlock(0, 1, 0).getBlock();
		final Block centerBlock2 = this.multiblockChecker.getBlock(0, 2, 0).getBlock();
		final boolean center1 = (centerBlock1 == Blocks.AIR || centerBlock1 == Blocks.LAVA);
		final boolean center2 = (centerBlock2 == Blocks.AIR || centerBlock2 == Blocks.LAVA);
		return layer0 && layer1 && layer2 && layer3 && center1 && center2;
	}

	@Override
	public void update() {
		if (world.isRemote){ return; }
		
		if (this.multiblockChecker == null) {
			final BlockPos pos = this.getPos().offset(this.getFacing().getOpposite(), 2);
			this.multiblockChecker = new MultiblockChecker(this.world, pos);
		}
		
		if (this.getMutliBlock()) {
			super.update();
			this.charge(4);
		}	
	}

	@Override
	public ItemStack getToolDrop(final EntityPlayer entityPlayer) {
		return new ItemStack(ModBlocks.INDUSTRIAL_BLAST_FURNACE, 1);
	}

	public int getHeat() {
		if (!this.getMutliBlock()){
			return 0;
		}
		for (final EnumFacing direction : EnumFacing.values()) {
			final TileEntity tileEntity = this.world.getTileEntity(new BlockPos(this.getPos().getX() + direction.getFrontOffsetX(),
				this.getPos().getY() + direction.getFrontOffsetY(), this.getPos().getZ() + direction.getFrontOffsetZ()));
			if (tileEntity instanceof TileMachineCasing) {
				if (((TileMachineCasing) tileEntity).isConnected()
					&& ((TileMachineCasing) tileEntity).getMultiblockController().isAssembled()) {
					final MultiBlockCasing casing = ((TileMachineCasing) tileEntity).getMultiblockController();
					final Location location = new Location(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), direction);
					location.modifyPositionFromSide(direction, 1);
					int heat = 0;
					if (this.world.getBlockState(new BlockPos(location.getX(), location.getY() - 1, location.getZ()))
						.getBlock() == tileEntity.getBlockType()) {
						return 0;
					}

					for (final IMultiblockPart part : casing.connectedParts) {
						final BlockMachineCasing casing1 = (BlockMachineCasing) this.world.getBlockState(part.getPos())
							.getBlock();
						heat += casing1
							.getHeatFromState(part.getWorld().getBlockState(part.getWorldLocation().toBlockPos()));
						// TODO meta fix
					}

					if (this.world.getBlockState(new BlockPos(location.getX(), location.getY() + 1, location.getZ()))
						.getBlock().getUnlocalizedName().equals("tile.lava")
						&& this.world
						.getBlockState(new BlockPos(location.getX(), location.getY() + 2, location.getZ()))
						.getBlock().getUnlocalizedName().equals("tile.lava")) {
						heat += 500;
					}
					return heat;
				}
			}
		}
		return 0;
	}

	@Override
	public void onDataPacket(final NetworkManager net, final SPacketUpdateTileEntity packet) {
		this.world.markBlockRangeForRenderUpdate(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX(),
			this.getPos().getY(), this.getPos().getZ());
		this.readFromNBT(packet.getNbtCompound());
	}

	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		this.crafter.readFromNBT(tagCompound);
	}

	@Override
	public NBTTagCompound writeToNBT(final NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		this.crafter.writeToNBT(tagCompound);
		return tagCompound;
	}

	// ISidedInventory
	@Override
	public int[] getSlotsForFace(final EnumFacing side) {
		return side == EnumFacing.DOWN ? new int[] { 0, 1, 2, 3 } : new int[] { 0, 1, 2, 3 };
	}

	@Override
	public boolean canInsertItem(final int slotIndex, final ItemStack itemStack, final EnumFacing side) {
		if (slotIndex >= 2)
			return false;
		return this.isItemValidForSlot(slotIndex, itemStack);
	}

	@Override
	public boolean canExtractItem(final int slotIndex, final ItemStack itemStack, final EnumFacing side) {
		return slotIndex == 2 || slotIndex == 3;
	}

	public int getProgressScaled(final int scale) {
		if (this.crafter.currentTickTime != 0) {
			return this.crafter.currentTickTime * scale / this.crafter.currentNeededTicks;
		}
		return 0;
	}

	@Override
	public double getBaseMaxPower() {
		return 10000;
	}

	@Override
	public boolean canAcceptEnergy(final EnumFacing direction) {
		return true;
	}

	@Override
	public boolean canProvideEnergy(final EnumFacing direction) {
		return false;
	}

	@Override
	public double getBaseMaxOutput() {
		return 0;
	}

	@Override
	public double getBaseMaxInput() {
		return 128;
	}

	@Override
	public boolean canCraft(final TileEntity tile, final BlastFurnaceRecipe recipe) {
		if (tile instanceof TileIndustrialBlastFurnace) {
			final TileIndustrialBlastFurnace blastFurnace = (TileIndustrialBlastFurnace) tile;
			return blastFurnace.getHeat() >= recipe.neededHeat;
		}
		return false;
	}

	@Override
	public boolean onCraft(final TileEntity tile, final BlastFurnaceRecipe recipe) {
		return true;
	}

	@Override
	public Inventory getInventory() {
		return this.inventory;
	}

	@Override
	public RecipeCrafter getRecipeCrafter() {
		return this.crafter;
	}

	public void setHeat(final int heat) {
		this.cachedHeat = heat;
	}

	public int getCachedHeat() {
		return this.cachedHeat;
	}

	@Override
	public BuiltContainer createContainer(final EntityPlayer player) {
		return new ContainerBuilder("blastfurnace").player(player.inventory).inventory().hotbar().addInventory()
				.tile(this).slot(0, 50, 27).slot(1, 50, 47).outputSlot(2, 93, 37).outputSlot(3, 113, 37)
				.energySlot(4, 8, 72).syncEnergyValue().syncCrafterValue()
				.syncIntegerValue(this::getHeat, this::setHeat).addInventory().create(this);
	}
}