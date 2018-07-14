package mod.akrivus.clones.entity.ai;

import mod.akrivus.clones.entity.EntityClone;
import mod.akrivus.clones.skills.SkillBase;
import net.minecraft.entity.ai.EntityAIBase;

@SuppressWarnings("deprecation")
public class EntityAISkill extends EntityAIBase {
	private final SkillBase cloneSkill;
	private final EntityClone clone;
	public EntityAISkill(EntityClone clone, SkillBase cloneSkill) {
		this.setMutexBits(cloneSkill.runsWith().ordinal());
		this.cloneSkill = cloneSkill;
		this.clone = clone;
	}
	@Override
	public boolean shouldExecute() {
		return this.cloneSkill.shouldExecute(this.clone);
	}
	@Override
	public void startExecuting() {
		this.cloneSkill.init(this.clone);
	}
	@Override
	public boolean continueExecuting() {
		return this.cloneSkill.continueExecuting(this.clone);
	}
	@Override
	public void updateTask() {
		this.cloneSkill.run(this.clone);
	}
	@Override
	public void resetTask() {
		this.cloneSkill.resetTask(this.clone);
	}
}
