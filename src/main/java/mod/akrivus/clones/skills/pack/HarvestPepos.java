package mod.akrivus.clones.skills.pack;

import java.util.ArrayList;
import java.util.Arrays;

import mod.akrivus.clones.entity.EntityClone;
import mod.akrivus.clones.skills.Speak;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemAxe;
import net.minecraft.util.math.BlockPos;

public class HarvestPepos extends Speak {
	private BlockPos harvestLocation = null;
	private int goal = 0;
	private int amountBeforeGoal = 0;
	private int lastBlockBreak = 0;
	public HarvestPepos() {
		this.TRIGGER_VERBS = new ArrayList<String>(Arrays.asList(new String[] {
			"collect",
			"plant",
			"pull",
			"get",
			"cut"
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
					this.goal = 0;
				}
			}
		}
		return previous;
	}
	@Override
	public boolean proceed(EntityClone clone) {
		return this.harvestLocation != null && this.amountBeforeGoal <= this.goal;
	}
	@Override
	public void init(EntityClone clone) {
		ArrayList<BlockPos> plants = new ArrayList<BlockPos>();
		this.harvestLocation = null;
		for (int x = -16; x < 16; ++x) {
			for (int y = -8; y < 8; ++y) {
				for (int z = -16; z < 16; ++z) {
					IBlockState state = clone.world.getBlockState(clone.getPosition().add(x, y, z));
					if (this.isCorrectPlant(state)) {
						plants.add(clone.getPosition().add(x, y, z));
					}
				}
			}
		}
		double minDistance = Double.MAX_VALUE;
		for (int i = 0; i < plants.size(); ++i) {
			double distance = clone.getDistanceSqToCenter(plants.get(i));
			if (distance < minDistance) {
				this.harvestLocation = plants.get(i);
				minDistance = distance;
			}
		}
		if (this.harvestLocation != null) {
			clone.setItemClass(ItemAxe.class);
		}
	}
	@Override
	public void run(EntityClone clone) {
		if (this.harvestLocation != null) {
			clone.lookAt(this.harvestLocation.down());
			if (clone.getDistanceSqToCenter(this.harvestLocation) < clone.getBuildRange()) {
				IBlockState harvest = clone.world.getBlockState(this.harvestLocation);
				if (this.lastBlockBreak > clone.getHarvestSpeed(harvest)) {
					boolean success = false;
					success = clone.breakBlock(this.harvestLocation);
					if (success) {
						if (this.goal > 0) {
							++this.amountBeforeGoal;
						}
						if (this.amountBeforeGoal <= this.goal) {
							this.init(clone);
						}
					}
					this.lastBlockBreak = 0;
				}
				++this.lastBlockBreak;
			}
			else {
				clone.tryToMoveTo(this.harvestLocation);
			}
		}
	}
	@Override
	public void reset(EntityClone clone) {
		this.harvestLocation = null;
		this.amountBeforeGoal = 0;
		this.goal = 0;
	}
	public boolean isCorrectPlant(IBlockState state) {
		return false;
	}
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.harvestLocation;
	}
}
