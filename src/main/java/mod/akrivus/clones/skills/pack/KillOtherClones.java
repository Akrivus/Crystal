package mod.akrivus.clones.skills.pack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mod.akrivus.clones.entity.EntityClone;
import mod.akrivus.clones.skills.Speak;

public class KillOtherClones extends Speak {
	private EntityClone lastClone = null;
	private EntityClone otherClone = null;
	private boolean ignoreDeath;
	private int goal = 1;
	private int amountBeforeGoal = 0;
	private int lastHitTime = 0;
	public KillOtherClones() {
		this.TRIGGER_VERBS = new ArrayList<String>(Arrays.asList(new String[] {
			"execute",
			"kill",
			"terminate",
			"destroy",
			"slay",
			"murder",
			"hunt",
			"assassinate",
			"liquidate",
			"eliminate"
		}));
		this.TRIGGER_NOUNS = new ArrayList<String>(Arrays.asList(new String[] {
			"clone",
			"clones",
			"other",
			"others",
			"order"
		}));
		this.can(RunWith.TARGETTING);
		this.canBeStopped = true;
		this.killsOnEnd = true;
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
		}
		return previous;
	}
	@Override
	public boolean proceed(EntityClone clone) {
		if (this.amountBeforeGoal < this.goal && this.otherClone != null && this.otherClone.isDead) {
			this.ignoreDeath = true;
		}
		return this.otherClone != null && (!this.otherClone.isDead || this.ignoreDeath) && this.amountBeforeGoal < this.goal;
	}
	@Override
	public void init(EntityClone clone) {
		List<EntityClone> clones = clone.world.<EntityClone>getEntitiesWithinAABB(EntityClone.class, clone.getEntityBoundingBox().expand(16.0D, 8.0D, 16.0D), null);
		if (!clones.isEmpty()) {
			double minDistance = Double.MAX_VALUE;
			for (EntityClone other : clones) {
				double distance = clone.getDistanceSqToEntity(other);
				if (clone.getDistanceSqToEntity(other) < minDistance && !clone.equals(other)) {
					this.ignoreDeath = false;
					this.otherClone = other;
					minDistance = distance;
				}
			}
		}
		if (this.otherClone != null) {
			clone.setAttackTarget(this.otherClone);
		}
	}
	@Override
	public void run(EntityClone clone) {
		if (this.otherClone != null) {
			clone.lookAt(this.otherClone);
			if (clone.getDistanceSqToEntity(this.otherClone) < clone.getAttackRange()) {
				if (this.lastHitTime > clone.getAttackSpeed()) {
					clone.attackEntityAsMob(this.otherClone);
					if (this.otherClone.getHealth() <= 0.0F && !this.otherClone.equals(this.lastClone)) {
						this.lastClone = this.otherClone;
						++this.amountBeforeGoal;
					}
					this.lastHitTime = 0;
				}
				++this.lastHitTime;
			}
			else {
				clone.tryToMoveTo(this.otherClone.getPosition());
			}
			if (this.amountBeforeGoal < this.goal) {
				this.otherClone = null;
				this.ignoreDeath = true;
				this.init(clone);
			}
		}
	}
	@Override
	public void reset(EntityClone clone) {
		this.otherClone = null;
		this.amountBeforeGoal = 0;
		this.goal = 0;
	}
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.otherClone;
	}
}
