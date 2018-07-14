package mod.akrivus.clones.skills;

import mod.akrivus.clones.entity.EntityClone;
import mod.akrivus.clones.skills.pack.Come;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;

public class SkillBase {
	public EntityPlayer commandingPlayer;
	public boolean isAllowedToRun = false;
	public boolean canBeStopped;
	public boolean killsOnEnd;
	public boolean readyForRemoval;
	public boolean notified;
	public enum RunWith {
		EVERYTHING,
		TARGETTING,
		LOOKING,
		ATTACKING,
		SWIMMING,
		RESTING,
		NOTHING
	}
	public enum Priority {
		CORE,
		HIGH,
		NORMAL,
		LOW
	}
	private Priority priority = Priority.NORMAL;
	private boolean isTask;
	private int mutexMask;
	
	/**
	 * Gets the load order, or priority, of the task.
	 * @return the skill's priority.
	 */
	public Priority priority() {
		return this.priority;
	}
	/**
	 * Sets the load order, or priority, of the task.
	 * @param priority the set load order or priority.
	 */
	public void priority(Priority priority) {
		this.priority = priority;
	}
	/**
	 * Gets what tasks the skill can run with.
	 * @return the task the skill can run with.
	 */
	public RunWith runsWith() {
		return RunWith.values()[this.mutexMask];
	}
	/**
	 * Sets what tasks the skill can run with.
	 * @param runWith the types of tasks the skill can run with.
	 */
	public void can(RunWith runWith) {
		this.mutexMask = runWith.ordinal();
	}
	/**
	 * Gets if the skill is a task.
	 * @return true means the skill is a task.
	 */
	public boolean isTask() {
		return this.isTask;
	}
	/**
	 * Sets if the skill is a task.
	 * @param task true means the skill is a task.
	 */
	public void task(boolean task) {
		this.isTask = task;
	}
	/**
	 * Called on entity creation.
	 * @param clone the entity the skill is being used by.
	 */
	public void create(EntityClone clone) {
		
	}
	/**
	 * Called on entity loading.
	 * @param clone the entity the skill is being used by.
	 * @param compound the NBT tag compound of the entity.
	 */
	public void read(EntityClone clone, NBTTagCompound compound) {
		
	}
	/**
	 * Called on entity saving.
	 * @param clone the entity the skill is being used by.
	 * @param compound the NBT tag compound of the entity.
	 */
	public void write(EntityClone clone, NBTTagCompound compound) {
		
	}
	/**
	 * Called on entity interaction (right click.)
	 * @param clone the entity the skill is being used by.
	 * @param player the player interacting.
	 * @param hand the hand used to interact.
	 * @param stack the stack in hand; can be empty.
	 * @return true means the interaction passed.
	 */
	public boolean interact(EntityClone clone, EntityPlayer player, EnumHand hand, ItemStack stack) {
		return false;
	}
	/**
	 * Called on entity voice interaction.
	 * @param clone the entity the skill is being used by.
	 * @param player the player speaking.
	 * @param message the message spoken.
	 * @return true means the voice interaction was correct.
	 */
	public boolean speak(EntityClone clone, EntityPlayer player, String message) {
		return false;
	}
	/**
	 * Called on entity picking up items.
	 * @param clone the entity the skill is being used by.
	 * @param stack the stack picked up.
	 * @return true means the item was picked up.
	 */
	public boolean item(EntityClone clone, ItemStack stack) {
		return false;
	}
	/**
	 * Called on entity update.
	 * @param clone the entity the skill is being used by.
	 */
	public void update(EntityClone clone) {
		
	}
	/**
	 * Called on entity struck by lightning.
	 * @param clone the entity the skill is being used by.
	 * @param lightningBolt the lightning bolt.
	 */
	public void lightning(EntityClone clone, EntityLightningBolt lightningBolt) {
		
	}
	/**
	 * Called on entity attack.
	 * @param clone the entity the skill is being used by.
	 * @param victim the entity being attacked.
	 * @return true means the attack was made.
	 */
	public boolean attack(EntityClone clone, Entity victim) {
		return true;
	}
	/**
	 * Called on entity hit.
	 * @param clone the entity the skill is being used by.
	 * @param source the source of damage done.
	 * @param amount the amount of damage done.
	 * @param attacker the attacker entity (may be null.)
	 * @return true means the damage was dealt.
	 */
	public boolean hit(EntityClone clone, DamageSource source, float amount, EntityLivingBase attacker) {
		return true;
	}
	/**
	 * Called when the skill is ran through the entity's task manager for the first time.
	 * @param clone the entity the skill is being used by.
	 * @return true means the task will run.
	 */
	public boolean triggered(EntityClone clone) {
		return this.isAllowedToRun;
	}
	/**
	 * Called when the skill is ran through the entity's task manager after the first time.
	 * @param clone the entity the skill is being used by.
	 * @return true means the task will run.
	 */
	public boolean proceed(EntityClone clone) {
		return false;
	}
	/**
	 * Called when the skill is ran for the first time in a cycle.
	 * @param clone the entity the skill is being used by.
	 */
	public void init(EntityClone clone) {
		
	}
	/**
	 * Called when the skill is ran continuously after the first time.
	 * @param clone the entity the skill is being used by.
	 */
	public void run(EntityClone clone) {
		
	}
	/**
	 * Called when the skill is halted.
	 * @param clone the entity the skill is being used by.
	 */
	public void reset(EntityClone clone) {
		
	}
	/**
	 * Called to handle clones speaking.
	 * @param clone the entity the skill is being used by.
	 * @param message the message being sent.
	 */
	public void feedback(EntityClone clone, String message) {
		clone.feedback(message);
		this.notified = true;
	}
	@Deprecated
	public boolean shouldExecute(EntityClone clone) {
		return this.triggered(clone) && this.isAllowedToRun;
	}
	@Deprecated
	public boolean continueExecuting(EntityClone clone) {
		return this.proceed(clone) && this.isAllowedToRun;
	}
	@Deprecated
	public void resetTask(EntityClone clone) {
		clone.setItemClass(Item.class);
		this.isAllowedToRun = false;
		this.readyForRemoval = true;
		this.notified = false;
		this.reset(clone);
		if (this.killsOnEnd && clone.isFromThePast()) {
			if (this.commandingPlayer == null || this instanceof Come) {
				clone.attackEntityFrom(DamageSource.OUT_OF_WORLD, clone.getMaxHealth());
			}
			else if (this.commandingPlayer != null) {
				Come skill = new Come();
				skill.selectedNoun = "me";
				skill.killsOnEnd = true;
				clone.addSkill(skill, this.commandingPlayer);
			}
		}
	}
}
