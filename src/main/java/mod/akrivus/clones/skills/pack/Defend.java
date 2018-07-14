package mod.akrivus.clones.skills.pack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mod.akrivus.clones.entity.EntityClone;
import mod.akrivus.clones.skills.Speak;
import mod.akrivus.clones.util.LinguisticsHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;

public class Defend extends Speak {
	private EntityLivingBase principle = null;
	private EntityLivingBase enemy = null;
	private int lastHitTime;
	public Defend() {
		this.TRIGGER_VERBS = new ArrayList<String>(Arrays.asList(new String[] { 
			"defend",
			"guard",
			"protect"
		}));
		this.TRIGGER_NOUNS = new ArrayList<String>();
		this.canBeStopped = true;
		this.can(RunWith.TARGETTING);
		this.priority(Priority.LOW);
		this.task(true);
	}
	@Override
	public boolean proceed(EntityClone clone) {
		return this.principle != null && !this.principle.isDead;
	}
	@Override
	public void init(EntityClone clone) {
		if (this.principle == null) {
			if (this.selectedNoun.equals("me")) {
				this.principle = clone.lastPlayerSpokenTo;
			}
			else {
				List<EntityLivingBase> entities = clone.world.<EntityLivingBase>getEntitiesWithinAABB(EntityLivingBase.class, clone.getEntityBoundingBox().expand(16.0D, 8.0D, 16.0D), null);
				if (!entities.isEmpty()) {
					double minDistance = Double.MAX_VALUE;
					for (EntityLivingBase entity : entities) {
						double distance = clone.getDistanceSqToEntity(entity);
						if (clone.getDistanceSqToEntity(entity) < minDistance && !clone.equals(entity)) {
							if (LinguisticsHelper.getDistance(entity.getName(), this.selectedNoun) < 3) {
								this.principle = entity;
								minDistance = distance;
							}
						}
					}
				}
			}
		}
		if (this.enemy == null && this.principle != null) {
			List<EntityLivingBase> entities = clone.world.<EntityLivingBase>getEntitiesWithinAABB(EntityLiving.class, clone.getEntityBoundingBox().expand(16.0D, 8.0D, 16.0D), null);
			if (!entities.isEmpty()) {
				double minDistance = Double.MAX_VALUE;
				for (EntityLivingBase entity : entities) {
					double distance = clone.getDistanceSqToEntity(entity);
					if (this.principle.getDistanceSqToEntity(entity) < minDistance && !clone.equals(entity)) {
						boolean attackable = false;
						if (entity instanceof EntityLiving) {
							EntityLiving living = (EntityLiving) entity;
							if (living.getAttackTarget() != null) {
								attackable = living.getAttackTarget().equals(this.principle);
							}
						}
						if (this.principle.getAITarget() != null) {
							attackable = this.principle.getAITarget().equals(entity);
						}
						if (attackable) {
							this.enemy = entity;
							minDistance = distance;
						}
					}
				}
			}
			if (this.enemy != null) {
				clone.setAttackTarget(this.enemy);
			}
		}
	}
	@Override
	public void run(EntityClone clone) {
		if (this.principle != null) {
			if (clone.getAttackTarget() != null) {
				clone.lookAt(this.enemy);
				if (clone.getDistanceSqToEntity(this.enemy) < clone.getAttackRange()) {
					if (this.lastHitTime > clone.getAttackSpeed()) {
						clone.attackEntityAsMob(this.enemy);
						this.lastHitTime = 0;
					}
					++this.lastHitTime;
				}
				else {
					clone.tryToMoveTo(this.enemy.getPosition());
				}
				if (this.enemy.isDead) {
					this.enemy = null;
					this.init(clone);
				}
			}
			else {
				clone.lookAt(this.principle);
				if (clone.getDistanceSqToEntity(this.principle) > clone.getAttackRange()) {
					clone.tryToMoveTo(this.principle.getPosition());	
				}
				else {
					this.init(clone);
				}
			}
		}
	}
	@Override
	public void reset(EntityClone clone) {
		this.principle = null;
		this.enemy = null;
	}
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.principle;
	}
}
