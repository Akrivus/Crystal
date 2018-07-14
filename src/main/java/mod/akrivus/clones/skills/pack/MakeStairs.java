package mod.akrivus.clones.skills.pack;

import java.util.ArrayList;
import java.util.Arrays;

import mod.akrivus.clones.entity.EntityClone;
import mod.akrivus.clones.skills.Speak;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class MakeStairs extends Speak {
	private float[] direction = new float[] { 0, 0 };
	private Block stairBlock = Blocks.COBBLESTONE;
	private boolean stillBuilding = true;
	private int lastBlockPlace = 0;
	public MakeStairs() {
		this.TRIGGER_VERBS = new ArrayList<String>(Arrays.asList(new String[] { 
			"make",
			"build",
			"create",
			"construct",
			"assemble",
			"generate"
		}));
		this.TRIGGER_NOUNS = new ArrayList<String>(Arrays.asList(new String[] {
			"stairs",
			"staircase"
		}));
		this.canBeStopped = true;
		this.killsOnEnd = true;
		this.can(RunWith.RESTING);
		this.task(true);
	}
	@Override
	public boolean speak(EntityClone clone, EntityPlayer player, String message) {
		boolean result = super.speak(clone, player, message);
		if (result) {
			int deg = MathHelper.floor(((player.rotationYaw * 4.0F) / 360.0F) + 0.5D) & 3;
			clone.rotationYaw = player.rotationYaw;
			switch (deg) {
			case 0:
				this.direction = new float[] { 0, 1 };
				break;
			case 1:
				this.direction = new float[] { -1, 0 };
				break;
			case 2:
				this.direction = new float[] { 0, -1 };
				break;
			case 3:
				this.direction = new float[] { 1, 0 };
				break;
			}
		}
		return result;
	}
	@Override
	public boolean triggered(EntityClone clone) {
		boolean previous = this.isAllowedToRun;
		if (previous) {
			boolean finished = false;
			int blocksPlaced = 0;
			BlockPos start = clone.getPosition();
			while (blocksPlaced < 128) {
				BlockPos nextPos = start.add(this.direction[0], 0, this.direction[1]);
				if (clone.world.getBlockState(nextPos).getBlock().isBlockSolid(clone.world, nextPos, EnumFacing.UP)) {
					finished = true;
					break;
				}
				start = start.add(this.direction[0], 1, this.direction[1]);
				++blocksPlaced;
			}
			previous = finished && blocksPlaced < 128;
			for (String subject : this.collectedSubjects) {
				Item item = clone.searchForItemInInventory(subject);
				if (item instanceof ItemBlock) {
					this.stairBlock = Block.getBlockFromItem(item);
				}
			}
		}
		return previous;
	}
	@Override
	public boolean proceed(EntityClone clone) {
		return this.stillBuilding;
	}
	@Override
	public void run(EntityClone clone) {
		if (this.lastBlockPlace > clone.getPlaceSpeed()) {
			BlockPos nextPos = clone.getPosition().add(this.direction[0], 0, this.direction[1]);
			clone.lookAt(nextPos.add(this.direction[0], 1, this.direction[1]));
			boolean placed = false;
			if (!clone.world.getBlockState(nextPos).getBlock().isBlockSolid(clone.world, nextPos, EnumFacing.UP)) {
				placed = clone.placeBlock(this.stairBlock, nextPos);
				if (placed) {
					clone.setItemClass(Item.getItemFromBlock(this.stairBlock));
				}
			}
			clone.tryToMoveTo(nextPos.up());
			this.stillBuilding = placed;
			this.lastBlockPlace = 0;
		}
		++this.lastBlockPlace;
	}
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
