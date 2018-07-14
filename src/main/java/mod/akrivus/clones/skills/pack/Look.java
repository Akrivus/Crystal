package mod.akrivus.clones.skills.pack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mod.akrivus.clones.entity.EntityClone;
import mod.akrivus.clones.skills.Speak;
import mod.akrivus.clones.util.LinguisticsHelper;
import net.minecraft.entity.EntityLivingBase;

public class Look extends Speak {
	private EntityLivingBase destination = null;
	public Look() {
		this.TRIGGER_VERBS = new ArrayList<String>(Arrays.asList(new String[] { 
			"look"
		}));
		this.TRIGGER_NOUNS = new ArrayList<String>();
		this.can(RunWith.LOOKING);
		this.priority(Priority.LOW);
		this.task(false);
	}
	@Override
	public boolean proceed(EntityClone clone) {
		return this.destination != null && !this.destination.isDead && clone.getDistanceSqToEntity(this.destination) > clone.getAttackRange();
	}
	@Override
	public void init(EntityClone clone) {
		if (this.selectedNoun.equals("me")) {
			this.destination = clone.lastPlayerSpokenTo;
		}
		else {
			List<EntityLivingBase> entities = clone.world.<EntityLivingBase>getEntitiesWithinAABB(EntityLivingBase.class, clone.getEntityBoundingBox().expand(16.0D, 8.0D, 16.0D), null);
			if (!entities.isEmpty()) {
				double minDistance = Double.MAX_VALUE;
				for (EntityLivingBase entity : entities) {
					double distance = clone.getDistanceSqToEntity(entity);
					if (clone.getDistanceSqToEntity(entity) < minDistance && !clone.equals(entity)) {
						if (LinguisticsHelper.getDistance(entity.getName(), this.selectedNoun) < 3) {
							this.destination = entity;
							minDistance = distance;
						}
					}
				}
			}
		}
	}
	@Override
	public void run(EntityClone clone) {
		if (this.destination != null) {
			clone.lookAt(this.destination);
		}
	}
	@Override
	public void reset(EntityClone clone) {
		this.destination = null;
	}
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.destination;
	}
}
