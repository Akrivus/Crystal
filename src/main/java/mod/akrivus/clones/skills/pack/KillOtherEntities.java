package mod.akrivus.clones.skills.pack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mod.akrivus.clones.entity.EntityClone;
import mod.akrivus.clones.skills.Speak;
import mod.akrivus.clones.util.LinguisticsHelper;
import net.minecraft.entity.EntityLiving;

public class KillOtherEntities extends Speak {
	private EntityLiving otherEntity = null;
	private EntityLiving lastEntity = null;
	private boolean ignoreDeath;
	private int goal = 1;
	private int amountBeforeGoal = 0;
	private int lastHitTime = 0;
	public KillOtherEntities() {
		this.TRIGGER_VERBS = new ArrayList<String>(Arrays.asList(new String[] {
			"kill",
			"destroy",
			"slay",
			"hunt"
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
					this.goal = 2;
				}
			}
		}
		return previous;
	}
	@Override
	public boolean proceed(EntityClone clone) {
		if (this.amountBeforeGoal < this.goal && this.otherEntity != null && this.otherEntity.isDead) {
			this.ignoreDeath = true;
		}
		return this.otherEntity != null && (!this.otherEntity.isDead || this.ignoreDeath) && this.amountBeforeGoal < this.goal;
	}
	@Override
	public void init(EntityClone clone) {
		List<EntityLiving> entities = clone.world.<EntityLiving>getEntitiesWithinAABB(EntityLiving.class, clone.getEntityBoundingBox().expand(16.0D, 8.0D, 16.0D), null);
		if (!entities.isEmpty()) {
			double minDistance = Double.MAX_VALUE;
			for (EntityLiving entity : entities) {
				double distance = clone.getDistanceSqToEntity(entity);
				if (clone.getDistanceSqToEntity(entity) < minDistance && !clone.equals(entity)) {
					if (LinguisticsHelper.getDistance(entity.getName(), this.selectedNoun) < 3) {
						this.ignoreDeath = false;
						this.otherEntity = entity;
						minDistance = distance;
					}
				}
			}
		}
		if (this.otherEntity != null) {
			clone.setAttackTarget(this.otherEntity);
		}
	}
	@Override
	public void run(EntityClone clone) {
		if (this.otherEntity != null) {
			clone.lookAt(this.otherEntity);
			if (clone.getDistanceSqToEntity(this.otherEntity) < clone.getAttackRange()) {
				if (this.lastHitTime > clone.getAttackSpeed()) {
					clone.attackEntityAsMob(this.otherEntity);
					if (this.otherEntity.getHealth() <= 0.0F && !this.otherEntity.equals(this.lastEntity)) {
						this.lastEntity = this.otherEntity;
						++this.amountBeforeGoal;
					}
					this.lastHitTime = 0;
				}
				++this.lastHitTime;
			}
			else {
				clone.tryToMoveTo(this.otherEntity.getPosition());
			}
			if (this.amountBeforeGoal < this.goal) {
				this.otherEntity = null;
				this.init(clone);
			}
		}
	}
	@Override
	public void reset(EntityClone clone) {
		this.otherEntity = null;
		this.amountBeforeGoal = 0;
		this.goal = 0;
	}
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.otherEntity;
	}
}
