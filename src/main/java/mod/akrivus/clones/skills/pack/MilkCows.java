package mod.akrivus.clones.skills.pack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mod.akrivus.clones.entity.EntityClone;
import mod.akrivus.clones.skills.Speak;
import mod.akrivus.clones.util.LinguisticsHelper;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class MilkCows extends Speak {
	private EntityCow otherCow = null;
	private int goal = 1;
	private int amountBeforeGoal = 0;
	private int lastHitTime = 0;
	public MilkCows() {
		this.TRIGGER_VERBS = new ArrayList<String>(Arrays.asList(new String[] {
			"collect",
			"grab",
			"get"
		}));
		this.TRIGGER_NOUNS = new ArrayList<String>(Arrays.asList(new String[] {
			"milk",
			"cows",
			"cow"
		}));
		this.canBeStopped = true;
		this.killsOnEnd = true;
		this.can(RunWith.TARGETTING);
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
		return this.otherCow != null && this.otherCow.getGrowingAge() == 0 && !clone.getItemInInventory(Items.BUCKET).isEmpty() && this.amountBeforeGoal < this.goal;
	}
	@Override
	public void init(EntityClone clone) {
		List<EntityCow> cows = clone.world.<EntityCow>getEntitiesWithinAABB(EntityCow.class, clone.getEntityBoundingBox().expand(16.0D, 8.0D, 16.0D), null);
		if (!cows.isEmpty()) {
			double minDistance = Double.MAX_VALUE;
			for (EntityCow cow : cows) {
				double distance = clone.getDistanceSqToEntity(cow);
				if (clone.getDistanceSqToEntity(cow) < minDistance && cow.getGrowingAge() == 0 && !cow.isInLove()) {
					if (LinguisticsHelper.getDistance(cow.getName(), this.selectedNoun) < 3) {
						this.otherCow = cow;
						minDistance = distance;
					}
				}
			}
		}
		if (this.otherCow != null) {
			clone.setAttackTarget(this.otherCow);
		}
	}
	@Override
	public void run(EntityClone clone) {
		if (this.otherCow != null) {
			clone.lookAt(this.otherCow);
			clone.setItemClass(Items.BUCKET);
			if (clone.getDistanceSqToEntity(this.otherCow) < clone.getAttackRange()) {
				if (this.lastHitTime > clone.getAttackSpeed()) {
					clone.swapItemInInventory(Items.BUCKET, new ItemStack(Items.MILK_BUCKET));
					++this.amountBeforeGoal;
					this.lastHitTime = 0;
				}
				++this.lastHitTime;
			}
			else {
				clone.tryToMoveTo(this.otherCow.getPosition());
			}
			if (this.amountBeforeGoal < this.goal) {
				this.otherCow = null;
				this.init(clone);
			}
		}
	}
	@Override
	public void reset(EntityClone clone) {
		this.otherCow = null;
		this.amountBeforeGoal = 0;
		this.goal = 0;
	}
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.otherCow;
	}
}
