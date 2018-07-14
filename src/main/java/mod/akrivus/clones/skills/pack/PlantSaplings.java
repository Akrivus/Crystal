package mod.akrivus.clones.skills.pack;

import java.util.ArrayList;
import java.util.Arrays;

import mod.akrivus.clones.entity.EntityClone;
import mod.akrivus.clones.skills.Speak;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

public class PlantSaplings extends Speak {
	private BlockPos plantLocation = null;
	private int goal = 4;
	private int amountBeforeGoal = 0;
	private int lastBlockBreak = 0;
	public PlantSaplings() {
		this.TRIGGER_VERBS = new ArrayList<String>(Arrays.asList(new String[] {
			"plant",
			"place",
			"locate",
			"grow",
			"farm"
		}));
		this.TRIGGER_NOUNS = new ArrayList<String>(Arrays.asList(new String[] {
			"sapling",
			"saplings",
			"tree",
			"trees"
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
					this.goal = 4;
				}
			}
		}
		return previous;
	}
	@Override
	public boolean proceed(EntityClone clone) {
		return this.plantLocation != null && this.amountBeforeGoal < this.goal;
	}
	@Override
	public void init(EntityClone clone) {
		ArrayList<BlockPos> places = new ArrayList<BlockPos>();
		for (int x = -16; x < 16; ++x) {
			for (int y = -8; y < 8; ++y) {
				for (int z = -16; z < 16; ++z) {
					IBlockState state = clone.world.getBlockState(clone.getPosition().add(x, y, z));
					if (this.isCorrectBlock(state)) {
						BlockPos place = clone.getPosition().add(x, y, z);
						if (this.plantLocation == null || this.plantLocation.getDistance(place.getX(), place.getY(), place.getZ()) > 6) {
							BlockPos above = place.up();
							if (clone.world.isAirBlock(above)) {
								places.add(above);
							}
						}
					}
				}
			}
		}
		double minDistance = Double.MAX_VALUE;
		for (int i = 0; i < places.size(); ++i) {
			double distance = clone.getDistanceSqToCenter(places.get(i));
			if (distance < minDistance) {
				this.plantLocation = places.get(i);
				minDistance = distance;
			}
		}
	}
	@Override
	public void run(EntityClone clone) {
		if (this.plantLocation != null) {
			clone.lookAt(this.plantLocation.down());
			if (clone.getDistanceSqToCenter(this.plantLocation) < clone.getBuildRange()) {
				if (this.lastBlockBreak > clone.getHarvestSpeed(clone.world.getBlockState(this.plantLocation))) {
					boolean placed = clone.placeBlock(Blocks.SAPLING, this.plantLocation);
					if (placed) {
						if (this.goal > 0) {
							++this.amountBeforeGoal;
						}
						if (this.amountBeforeGoal < this.goal) {
							this.init(clone);
						}
						if (placed) {
							clone.setItemClass(Item.getItemFromBlock(Blocks.SAPLING));
						}
					}
					this.lastBlockBreak = 0;
				}
				++this.lastBlockBreak;
			}
			else {
				clone.tryToMoveTo(this.plantLocation);
			}
		}
	}
	@Override
	public void reset(EntityClone clone) {
		this.amountBeforeGoal = 0;
		this.goal = 0;
	}
	public boolean isCorrectBlock(IBlockState state) {
		return state.getBlock() instanceof BlockGrass;
	}
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.plantLocation;
	}
}
