package mod.akrivus.clones.skills.pack;

import java.util.ArrayList;
import java.util.Arrays;

import mod.akrivus.clones.entity.EntityClone;
import mod.akrivus.clones.skills.Speak;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemAxe;
import net.minecraft.util.math.BlockPos;

public class CutDownTrees extends Speak {
	private BlockPos treeLocation = null;
	private boolean cuttingDownTrees = true;
	private boolean countTreesNotWood = true;
	private int goal = 1;
	private int amountBeforeGoal = 0;
	private int lastBlockBreak = 0;
	public CutDownTrees() {
		this.TRIGGER_VERBS = new ArrayList<String>(Arrays.asList(new String[] { 
			"cut",
			"chop",
			"find",
			"get"
		}));
		this.TRIGGER_NOUNS = new ArrayList<String>(Arrays.asList(new String[] {
			"trees",
			"tree",
			"wood",
			"log",
			"logs"
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
					this.goal = 2;
				}
			}
			if (this.selectedNoun.startsWith("tree")) {
				this.countTreesNotWood = true;
			}
		}
		return previous;
	}
	@Override
	public boolean proceed(EntityClone clone) {
		return this.treeLocation != null && (this.cuttingDownTrees || this.amountBeforeGoal < this.goal);
	}
	@Override
	public void init(EntityClone clone) {
		ArrayList<BlockPos> trees = new ArrayList<BlockPos>();
		ArrayList<float[]> points = new ArrayList<float[]>();
		for (int x = -16; x < 16; ++x) {
			for (int y = -8; y < 8; ++y) {
				for (int z = -16; z < 16; ++z) {
					IBlockState state = clone.world.getBlockState(clone.getPosition().add(x, y, z));
					if (this.isCorrectBlock(state)) {
						for (int i = 0; i < 64; ++i) {
							Block top = clone.world.getBlockState(clone.getPosition().add(x, y + i, z)).getBlock();
							if (top instanceof BlockLeaves) {
								BlockPos tree = clone.getPosition().add(x, y - 1, z);
								if (!trees.contains(new float[] { tree.getX(), tree.getZ() })) {
									points.add(new float[] { tree.getX(), tree.getZ() });
									trees.add(tree);
								}
							}
						}
					}
				}
			}
		}
		double minDistance = Double.MAX_VALUE;
		for (int i = 0; i < trees.size(); ++i) {
			double distance = clone.getDistanceSqToCenter(trees.get(i));
			if (distance < minDistance) {
				this.treeLocation = trees.get(i);
				minDistance = distance;
			}
		}
		if (this.treeLocation == null) {
			this.cuttingDownTrees = false;
		}
		else {
			this.cuttingDownTrees = true;
		}
		if (this.cuttingDownTrees) {
			clone.setItemClass(ItemAxe.class);
		}
	}
	@Override
	public void run(EntityClone clone) {
		if (this.treeLocation != null) {
			BlockPos nextPos = this.treeLocation.add(0, 1, 0);
			clone.lookAt(nextPos);
			if (clone.getDistanceSqToCenter(this.treeLocation.down((int)(this.treeLocation.getY() - clone.posY))) < clone.getBuildRange()) {
				if (this.lastBlockBreak > clone.getHarvestSpeed(clone.world.getBlockState(nextPos))) {	
					if (clone.world.getBlockState(nextPos).getBlock() instanceof BlockLog) {
						boolean cut = clone.breakBlock(nextPos);
						if (cut) {
							if (!this.countTreesNotWood) {
								++this.amountBeforeGoal;
							}
						}
					}
					else {
						this.cuttingDownTrees = false;
						++this.amountBeforeGoal;
					}
					this.treeLocation = nextPos;
					this.lastBlockBreak = 0;
				}
				++this.lastBlockBreak;
			}
			else {
				clone.tryToMoveTo(this.treeLocation.add(1, 1, 0));
			}
			if (this.amountBeforeGoal < this.goal && !this.cuttingDownTrees) {
				this.cuttingDownTrees = true;
				this.treeLocation = null;
				this.init(clone);
			}
		}
		else {
			this.cuttingDownTrees = false;
		}
	}
	@Override
	public void reset(EntityClone clone) {
		this.cuttingDownTrees = false;
		this.treeLocation = null;
		this.amountBeforeGoal = 0;
		this.goal = 0;
	}
	public boolean isCorrectBlock(IBlockState state) {
		Block block = state.getBlock();
		if (block instanceof BlockLog) {
			String modifier = this.selectedPhrase.get(Math.max(0, this.selectedNounIndex - 2));
			switch (block.getMetaFromState(state)) {
			case 0:
				if (modifier.equals("oak")) {
					return true;
				}
			case 1:
				if (modifier.equals("spruce")) {
					return true;
				}
			case 2:
				if (modifier.equals("birch")) {
					return true;
				}
			case 3:
				if (modifier.equals("jungle")) {
					return true;
				}
			case 4:
				if (modifier.equals("acacia")) {
					return true;
				}
			}
			return true;
		}
		return false;
	}
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.treeLocation;
	}
}
