package mod.akrivus.clones.skills.pack;

import java.util.ArrayList;
import java.util.Arrays;

import mod.akrivus.clones.entity.EntityClone;
import mod.akrivus.clones.skills.Speak;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemShears;
import net.minecraft.util.math.BlockPos;

public class PickFlowers extends Speak {
	private BlockPos flowerLocation = null;
	private int goal = 3;
	private int amountBeforeGoal = 0;
	private int lastBlockBreak = 0;
	public PickFlowers() {
		this.TRIGGER_VERBS = new ArrayList<String>(Arrays.asList(new String[] {
			"collect",
			"pick",
			"get",
			"cut"
		}));
		this.TRIGGER_NOUNS = new ArrayList<String>(Arrays.asList(new String[] {
			"bouquet",
			"flower",
			"flowers"
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
					this.goal = 3;
				}
			}
		}
		return previous;
	}
	@Override
	public boolean proceed(EntityClone clone) {
		return this.flowerLocation != null && this.amountBeforeGoal < this.goal;
	}
	@Override
	public void init(EntityClone clone) {
		ArrayList<BlockPos> flowers = new ArrayList<BlockPos>();
		for (int x = -16; x < 16; ++x) {
			for (int y = -8; y < 8; ++y) {
				for (int z = -16; z < 16; ++z) {
					IBlockState state = clone.world.getBlockState(clone.getPosition().add(x, y, z));
					if (this.isCorrectBlock(state)) {
						BlockPos flower = clone.getPosition().add(x, y, z);
						flowers.add(flower);
					}
				}
			}
		}
		double minDistance = Double.MAX_VALUE;
		for (int i = 0; i < flowers.size(); ++i) {
			double distance = clone.getDistanceSqToCenter(flowers.get(i));
			if (distance < minDistance) {
				this.flowerLocation = flowers.get(i);
				minDistance = distance;
			}
		}
		if (this.flowerLocation != null) {
			clone.setItemClass(ItemShears.class);
		}
	}
	@Override
	public void run(EntityClone clone) {
		if (this.flowerLocation != null) {
			clone.lookAt(this.flowerLocation.down());
			if (clone.getDistanceSqToCenter(this.flowerLocation) < clone.getBuildRange()) {
				if (this.lastBlockBreak > clone.getHarvestSpeed(clone.world.getBlockState(this.flowerLocation))) {
					boolean picked = clone.breakBlock(this.flowerLocation);
					if (picked) {
						if (this.goal > 0) {
							++this.amountBeforeGoal;
						}
						if (this.amountBeforeGoal < this.goal) {
							this.init(clone);
						}
					}
					this.lastBlockBreak = 0;
				}
				++this.lastBlockBreak;
			}
			else {
				clone.tryToMoveTo(this.flowerLocation);
			}
		}
	}
	@Override
	public void reset(EntityClone clone) {
		this.flowerLocation = null;
		this.amountBeforeGoal = 0;
		this.goal = 0;
	}
	public boolean isCorrectBlock(IBlockState state) {
		Block block = state.getBlock();
		if (block instanceof BlockFlower) {
			return true;
		}
		return false;
	}
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.flowerLocation;
	}
}
