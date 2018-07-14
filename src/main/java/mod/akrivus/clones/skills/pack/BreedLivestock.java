package mod.akrivus.clones.skills.pack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mod.akrivus.clones.entity.EntityClone;
import mod.akrivus.clones.skills.Speak;
import mod.akrivus.clones.util.LinguisticsHelper;
import net.minecraft.entity.passive.EntityAnimal;

public class BreedLivestock extends Speak {
	private EntityAnimal otherAnimal = null;
	private int goal = 4;
	private int amountBeforeGoal = 0;
	private int lastHitTime = 0;
	public BreedLivestock() {
		this.TRIGGER_VERBS = new ArrayList<String>(Arrays.asList(new String[] {
			"collect",
			"birth",
			"raise",
			"help"
		}));
		this.TRIGGER_NOUNS = new ArrayList<String>();
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
					this.goal = 4;
				}
			}
		}
		return previous;
	}
	@Override
	public boolean proceed(EntityClone clone) {
		return this.otherAnimal != null && this.otherAnimal.getGrowingAge() == 0 && !this.otherAnimal.isInLove() && !clone.getBreedingItem(this.otherAnimal).isEmpty() && this.amountBeforeGoal < this.goal;
	}
	@Override
	public void init(EntityClone clone) {
		List<EntityAnimal> animals = clone.world.<EntityAnimal>getEntitiesWithinAABB(EntityAnimal.class, clone.getEntityBoundingBox().expand(16.0D, 8.0D, 16.0D), null);
		if (!animals.isEmpty()) {
			double minDistance = Double.MAX_VALUE;
			for (EntityAnimal animal : animals) {
				double distance = clone.getDistanceSqToEntity(animal);
				if (clone.getDistanceSqToEntity(animal) < minDistance && animal.getGrowingAge() == 0 && !animal.isInLove()) {
					if (LinguisticsHelper.getDistance(animal.getName(), this.selectedNoun) < 3) {
						this.otherAnimal = animal;
						minDistance = distance;
					}
				}
			}
		}
		if (this.otherAnimal != null) {
			clone.setAttackTarget(this.otherAnimal);
		}
	}
	@Override
	public void run(EntityClone clone) {
		if (this.otherAnimal != null) {
			clone.lookAt(this.otherAnimal);
			clone.setItemClass(clone.getBreedingItem(this.otherAnimal).getItem());
			if (clone.getDistanceSqToEntity(this.otherAnimal) < clone.getAttackRange()) {
				if (this.lastHitTime > clone.getAttackSpeed()) {
					clone.removeItemFromInventory(clone.getBreedingItem(this.otherAnimal));
					this.otherAnimal.setInLove(this.commandingPlayer);
					++this.amountBeforeGoal;
					this.lastHitTime = 0;
				}
				++this.lastHitTime;
			}
			else {
				clone.tryToMoveTo(this.otherAnimal.getPosition());
			}
			if (this.amountBeforeGoal < this.goal) {
				this.otherAnimal = null;
				this.init(clone);
			}
		}
	}
	@Override
	public void reset(EntityClone clone) {
		this.otherAnimal = null;
		this.amountBeforeGoal = 0;
		this.goal = 0;
	}
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.otherAnimal;
	}
}
