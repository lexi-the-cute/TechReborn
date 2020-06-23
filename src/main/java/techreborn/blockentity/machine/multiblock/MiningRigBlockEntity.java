package techreborn.blockentity.machine.multiblock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import reborncore.client.screen.BuiltScreenHandlerProvider;
import reborncore.client.screen.builder.BuiltScreenHandler;
import reborncore.client.screen.builder.ScreenHandlerBuilder;
import reborncore.common.blockentity.MultiblockWriter;
import techreborn.blockentity.machine.GenericMachineBlockEntity;
import techreborn.init.TRBlockEntities;
import techreborn.init.TRContent;
import techreborn.utils.WorldHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MiningRigBlockEntity extends GenericMachineBlockEntity implements BuiltScreenHandlerProvider {
	private int pipeReserveCount;
	private BlockPos reserveHead = null;

	private int drillDepth;
	private BlockPos drillHead =  null;
	private boolean finishedY = false;

	// Position from just inside block at bottom
	private final int DRILL_OFFSET = 3;


	public MiningRigBlockEntity() {
		// TODO config these values
		super(TRBlockEntities.MINING_RIG, "MiningRig", 512, 50000, TRContent.Machine.MINING_RIG.block, 6);

		finishedY = true;
	}

	@Override
	public void tick() {
		if(world == null || world.isClient || !isMultiblockValid()){
			return;
		}

		if (world.getTime() % 40 == 0) {
			advanceDrill();
		}




	}

	private void Drill(){

	}



	// Update pipe reserve count and reserveHead
	private void updatePipeReserve() {
		pipeReserveCount = WorldHelper.getBlockCountAlongY(this.pos,1, TRContent.DRILL_PIPE, world);

		// Reserve position is just machine offset by count
		reserveHead = this.pos.offset(Direction.UP, pipeReserveCount);
	}
	// Updates drilldepth (Includes drillhead)
	private void updatePipeDrillDepth(){
		Block[] blocks = {TRContent.DRILL_PIPE, TRContent.DRILL_HEAD};
		drillDepth = WorldHelper.getBlockCountAlongY(this.pos.offset(Direction.DOWN, DRILL_OFFSET),-1, Arrays.asList(blocks), world);
	}

	// Check if there's pipe directly above machine
	private boolean hasPipeAbove(){
		return world.getBlockState(this.pos.offset(Direction.UP)).getBlock() == TRContent.DRILL_PIPE;
	}
	// Take a pipe from reserve off
	private boolean consumePipe(){
		// If reserve is empty but there's a pipe above, update count and head position.
		if(pipeReserveCount == 0 && hasPipeAbove()){
			updatePipeReserve();
		}

		// No pipe, can't consume
		if(pipeReserveCount == 0){
			return false;
		}

		world.setBlockState(reserveHead, Blocks.AIR.getDefaultState());
		pipeReserveCount--;
		reserveHead = reserveHead.offset(Direction.DOWN);

		return true;
	}

	// Ensure the head of the reserve stack has air above it, pipe or machine below
	private boolean validReserveHead(){
		if(reserveHead == null){
			return false;
		}

		Block blockHead = world.getBlockState(reserveHead).getBlock();
		Block blockAbove = world.getBlockState(reserveHead.offset(Direction.UP)).getBlock();
		Block blockBelow = world.getBlockState(reserveHead.offset(Direction.DOWN)).getBlock();

		Block pipe = TRContent.DRILL_PIPE;
		return blockHead.is(pipe) && blockAbove.is(Blocks.AIR) &&
				blockBelow.is(pipe) || blockBelow.is(this.getBlockType());
	}
	private boolean validDrillHead(){
		if(drillHead == null){
			return false;
		}

		Block blockHead = world.getBlockState(drillHead).getBlock();
		Block blockAbove = world.getBlockState(drillHead.offset(Direction.UP)).getBlock();

		return blockHead.is(TRContent.DRILL_HEAD) && blockAbove.is(TRContent.DRILL_PIPE);
	}

	private boolean addDrillHead(){
		updatePipeDrillDepth();

		// Drill head should be at the end of pipe (ASSUMING no drill)
		drillHead = this.pos.offset(Direction.DOWN,   drillDepth + 1 + DRILL_OFFSET);

 		if(world.getBlockState(drillHead).getBlock() != TRContent.DRILL_HEAD) {
			world.setBlockState(drillHead, TRContent.DRILL_HEAD.getDefaultState());
			drillDepth++;
		}

		return true;
	}
	private void removeDrillHead(){
		// Could improve this to be more performant, but this is safer TODO
		List<BlockPos> drillHeads = WorldHelper.getBlocksAlongY(this.pos,-1,TRContent.DRILL_HEAD,world,true, null);

		for(BlockPos pos : drillHeads){
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
		}
	}
	private void rebuildDrillHead(){
		this.removeDrillHead();
		this.addDrillHead();
	}


	// Move the drill down one, consuming pipe
	private boolean advanceDrill(){
		if(!validDrillHead()){
			rebuildDrillHead();
		}

		if(!validReserveHead()){
			updatePipeReserve();

			if(pipeReserveCount == 0){
				return false;
			}
		}

		BlockPos nextDrillPosition = drillHead.offset(Direction.DOWN);
		Block nextBlock = world.getBlockState(nextDrillPosition).getBlock();
		if(!nextBlock.is(Blocks.BEDROCK) && (nextBlock.is(Blocks.AIR) || world.breakBlock(nextDrillPosition, false))) {
			world.setBlockState(drillHead, TRContent.DRILL_PIPE.getDefaultState());

			// Move drill to new position and update depth
			drillHead = nextDrillPosition;
			world.setBlockState(drillHead, TRContent.DRILL_HEAD.getDefaultState());
			drillDepth++;

			consumePipe();
		}

		return false;
	}

	@Override
	public void writeMultiblock(MultiblockWriter writer) {
		BlockState basic = TRContent.MachineBlocks.BASIC.getCasing().getDefaultState();
		BlockState advanced = TRContent.MachineBlocks.ADVANCED.getCasing().getDefaultState();
		BlockState reinGlass = TRContent.REINFORCED_GLASS.getDefaultState();
		BlockState pipe = TRContent.DRILL_PIPE.getDefaultState();

		// TODO use predicate for manual blocks
		writer.translate(-1, -4, -1)
				.ring(Direction.Axis.Y, 3, 1, 3, (view, pos) -> view.getBlockState(pos) == basic, basic, (view, pos) -> view.getBlockState(pos) == pipe, pipe)
				.ring(Direction.Axis.Y, 3, 3, 3, (view, pos) -> view.getBlockState(pos) == basic, basic, (view, pos) -> view.getBlockState(pos) == pipe, pipe)
				.add(2, 2, 2, advanced) // Corner 1
				.add(1, -2, 1, advanced) // Corner 2
				.add(1, -2, -1, advanced) // Corner 3
				.add(-1, -2, 1, advanced) // Corner 4
				.add(0,-2, 1, reinGlass) // Glass 1
				.add(1,-2, 0, reinGlass) // Glass 2
				.add(-1,-2, 0, reinGlass) // Glass 3
				.add(0,-2, -1, reinGlass) // Glass 4
				.add(0, -2, 0, pipe); // Pipe in middle
	}

	@Override
	public BuiltScreenHandler createScreenHandler(int syncID, PlayerEntity player) {
		return new ScreenHandlerBuilder("miningrig").player(player.inventory).inventory().hotbar().addInventory()
				.blockEntity(this).addInventory().create(this, syncID);
	}

	@Override
	public boolean canBeUpgraded() {
		return false;
	}


}
