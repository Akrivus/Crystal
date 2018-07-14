package mod.akrivus.clones.skills.pack;

import java.util.ArrayList;
import java.util.Arrays;

import mod.akrivus.clones.entity.EntityClone;
import mod.akrivus.clones.skills.Speak;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemShears;
import net.minecraft.util.math.BlockPos;

public class MowGrass extends Speak {
	private BlockPos grassLocation = null;
	private int lastBlockBreak = 0;
	public MowGrass() {
		this.TRIGGER_VERBS = new ArrayList<String>(Arrays.asList(new String[] {
			"mow",
			"cut"
		}));
		this.TRIGGER_NOUNS = new ArrayList<String>(Arrays.asList(new String[] {
			"grass",
			"lawn",
			"yard"
		}));
		this.canBeStopped = true;
		this.killsOnEnd = true;
		this.can(RunWith.RESTING);
		this.task(true);
	}
	@Override
	public boolean proceed(EntityClone clone) {
		return this.grassLocation != null && clone.canEntityBeSeen(this.commandingPlayer);
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
				this.grassLocation = flowers.get(i);
				minDistance = distance;
			}
		}
		if (this.grassLocation != null) {
			clone.setItemClass(ItemShears.class);
		}
	}
	@Override
	public void run(EntityClone clone) {
		if (this.grassLocation != null) {
			clone.lookAt(this.grassLocation.down());
			if (clone.getDistanceSqToCenter(this.grassLocation) < clone.getBuildRange()) {
				if (this.lastBlockBreak > clone.getHarvestSpeed(clone.world.getBlockState(this.grassLocation))) {
					boolean picked = clone.breakBlock(this.grassLocation);
					if (picked) {
						this.init(clone);
					}
					this.lastBlockBreak = 0;
				}
				++this.lastBlockBreak;
			}
			else {
				clone.tryToMoveTo(this.grassLocation);
			}
		}
	}
	@Override
	public void reset(EntityClone clone) {
		this.grassLocation = null;
	}
	public boolean isCorrectBlock(IBlockState state) {
		Block block = state.getBlock();
		if (block == Blocks.TALLGRASS || (block == Blocks.DOUBLE_PLANT && state.getValue(BlockDoublePlant.VARIANT) == BlockDoublePlant.EnumPlantType.GRASS)) {
			return true;
		}
		return false;
	}
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.grassLocation;
	}
}
