package mod.akrivus.clones.skills.pack;

import java.util.ArrayList;
import java.util.Arrays;

import mod.akrivus.clones.entity.EntityClone;
import mod.akrivus.clones.skills.Speak;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class CollectLiquids extends Speak {
	private BlockPos liquidLocation = null;
	private int goal = 1;
	private int amountBeforeGoal = 0;
	private int lastBlockBreak = 0;
	public CollectLiquids() {
		this.TRIGGER_VERBS = new ArrayList<String>(Arrays.asList(new String[] {
			"collect",
			"gather",
			"get",
			"grab"
		}));
		this.TRIGGER_NOUNS = new ArrayList<String>(Arrays.asList(new String[] {
			"water",
			"lava"
		}));
		this.canBeStopped = true;
		this.killsOnEnd = true;
		this.can(RunWith.RESTING);
		this.task(true);
	}
	@Override
	public boolean triggered(EntityClone clone) {
		boolean previous = this.isAllowedToRun;
		if (previous) {
			if (!this.collectedNumbers.isEmpty()) {
				try {
					this.goal = Integer.parseInt(this.collectedNumbers.get(0));
				}
				catch (Exception ex) {
					this.goal = 1;
				}
			}
		}
		return previous;
	}
	@Override
	public boolean proceed(EntityClone clone) {
		return this.liquidLocation != null && !clone.getItemInInventory(Items.BUCKET).isEmpty();
	}
	@Override
	public void init(EntityClone clone) {
		ArrayList<BlockPos> liquids = new ArrayList<BlockPos>();
		for (int x = -16; x < 16; ++x) {
			for (int y = -8; y < 8; ++y) {
				for (int z = -16; z < 16; ++z) {
					IBlockState state = clone.world.getBlockState(clone.getPosition().add(x, y, z));
					if (this.isCorrectBlock(state)) {
						BlockPos liquid = clone.getPosition().add(x, y, z);
						liquids.add(liquid);
					}
				}
			}
		}
		double minDistance = Double.MAX_VALUE;
		for (int i = 0; i < liquids.size(); ++i) {
			double distance = clone.getDistanceSqToCenter(liquids.get(i));
			if (distance < minDistance) {
				this.liquidLocation = liquids.get(i);
				minDistance = distance;
			}
		}
		if (this.liquidLocation != null) {
			clone.setItemClass(ItemShears.class);
		}
	}
	@Override
	public void run(EntityClone clone) {
		if (this.liquidLocation != null) {
			clone.lookAt(this.liquidLocation.down());
			if (clone.getDistanceSqToCenter(this.liquidLocation) < clone.getBuildRange()) {
				if (this.lastBlockBreak > clone.getHarvestSpeed(clone.world.getBlockState(this.liquidLocation))) {
					if (this.selectedNoun.equals("water")) {
						clone.swapItemInInventory(Items.BUCKET, new ItemStack(Items.WATER_BUCKET));
					}
					else {
						clone.swapItemInInventory(Items.BUCKET, new ItemStack(Items.LAVA_BUCKET));
					}
					clone.world.setBlockToAir(this.liquidLocation);
					++this.amountBeforeGoal;
					this.lastBlockBreak = 0;
				}
				++this.lastBlockBreak;
			}
			else {
				clone.tryToMoveTo(this.liquidLocation);
			}
			if (this.amountBeforeGoal < this.goal) {
				this.liquidLocation = null;
				this.init(clone);
			}
		}
	}
	@Override
	public void reset(EntityClone clone) {
		this.liquidLocation = null;
	}
	public boolean isCorrectBlock(IBlockState state) {
		Block block = state.getBlock();
		if (this.selectedNoun.equals("water")) {
			return block.equals(Blocks.WATER);
		}
		else {
			return block.equals(Blocks.LAVA);
		}
	}
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.liquidLocation;
	}
}
