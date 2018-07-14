package mod.akrivus.clones.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import mod.akrivus.clones.entity.ai.EntityAISkill;
import mod.akrivus.clones.skills.SkillBase;
import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityClone extends EntityCloneBase {
	public static final ArrayList<Class<? extends SkillBase>> SKILLS = new ArrayList<Class<? extends SkillBase>>();
	public HashMap<Class<? extends SkillBase>, SkillBase> skills = new HashMap<Class<? extends SkillBase>, SkillBase>();
	public HashMap<String, Object> savedVariables = new HashMap<String, Object>();
	public EntityPlayer lastPlayerSpokenTo;
	public EntityClone(World world) {
		super(world);
	}
	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
		this.setHealth(this.getMaxHealth());
		for (SkillBase skill : this.skills.values()) {
			skill.create(this);
		}
		return livingdata;
	}
	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        for (SkillBase skill : this.skills.values()) {
			skill.write(this, compound);
		}
	}
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		for (SkillBase skill : this.skills.values()) {
			skill.read(this, compound);
		}
	}
	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		boolean result = false;
		if (player.isSneaking()) {
			this.setSelected(!this.isSelected());
			result = true;
		}
		else {
			ItemStack stack = player.getHeldItem(hand);
			for (Class<? extends SkillBase> skillClass : SKILLS) {
				try {
					SkillBase skill;
					if (this.skills.containsKey(skillClass)) {
						skill = this.skills.get(skillClass);
					}
					else {
						skill = skillClass.newInstance();
					}
					if (skill.interact(this, player, hand, stack)) {
						this.addSkill(skill, player);
						result = true;
					}
				}
				catch (Exception ex) {
					CrashReport.makeCrashReport(ex, "Something went wrong loading skills.");
				}
			}
			if (!result) {
				this.openGUI(player);
			}
		}
		return result ? result : super.processInteract(player, hand);
	}
	public boolean spokenTo(EntityPlayer player, String message) {
		boolean result = false;
		this.lastPlayerSpokenTo = player;
		boolean canRunCommands = this.isSelected();
		if (!canRunCommands) {
			List<EntityClone> list = this.world.<EntityClone>getEntitiesWithinAABB(EntityClone.class, this.getEntityBoundingBox().expand(48.0D, 16.0D, 48.0D));
			canRunCommands = this.getName().equals(player.getName());
			for (EntityClone clone : list) {
	    		if (!this.equals(clone) && clone.isSelected()) {
	    			canRunCommands = false;
	    			break;
	    		}
	        }
		}
		if (canRunCommands) {
			for (Class<? extends SkillBase> skillClass : SKILLS) {
				try {
					SkillBase skill = skillClass.newInstance();
					if (skill.speak(this, player, message)) {
						skill.commandingPlayer = player;
						this.addSkill(skill, player);
						result = true;
					}
				}
				catch (Exception ex) {
					CrashReport.makeCrashReport(ex, "Something went wrong loading skills.");
				}
			}
		}
		return result;
	}
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		boolean result = false;
		EntityLivingBase attacker = null;
		if (source.getEntity() instanceof EntityLivingBase) {
			attacker = (EntityLivingBase) source.getEntity();
		}
		else if (source.getEntity() instanceof EntityThrowable) {
			EntityThrowable projectile = (EntityThrowable) source.getEntity();
			attacker = projectile.getThrower();
		}
		else if (source.getEntity() instanceof EntityArrow) {
			EntityArrow arrow = (EntityArrow) source.getEntity();
			if (arrow.shootingEntity instanceof EntityLivingBase) {
				attacker = (EntityLivingBase) arrow.shootingEntity;
			}
		}
		for (Class<? extends SkillBase> skillClass : SKILLS) {
			try {
				SkillBase skill;
				if (this.skills.containsKey(skillClass)) {
					skill = this.skills.get(skillClass);
				}
				else {
					skill = skillClass.newInstance();
				}
				if (skill.hit(this, source, amount, attacker)) {
					this.addSkill(skill, null);
					result = true;
				}
			}
			catch (Exception ex) {
				CrashReport.makeCrashReport(ex, "Something went wrong loading skills.");
			}
		}
		return result ? super.attackEntityFrom(source, amount) : result;
	}
	@Override
	public boolean attackEntityAsMob(Entity victim) {
		boolean result = false;
		for (Class<? extends SkillBase> skillClass : SKILLS) {
			try {
				SkillBase skill;
				if (this.skills.containsKey(skillClass)) {
					skill = this.skills.get(skillClass);
				}
				else {
					skill = skillClass.newInstance();
				}
				if (skill.attack(this, victim)) {
					this.addSkill(skill, null);
					result = true;
				}
			}
			catch (Exception ex) {
				CrashReport.makeCrashReport(ex, "Something went wrong loading skills.");
			}
		}
		return result ? super.attackEntityAsMob(victim) : result;
	}
	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		for (int i = 0; i < this.skills.size(); ++i) {
			ArrayList<SkillBase> skills = new ArrayList<SkillBase>(Arrays.asList(this.skills.values().toArray(new SkillBase[0])));
			if (skills.get(i).readyForRemoval) {
				this.removeSkill(skills.get(i));
				--i;
			}
		}
		for (SkillBase skill : this.skills.values()) {
			skill.update(this);
		}
	}
	@Override
	public void onStruckByLightning(EntityLightningBolt lightningBolt) {
		super.onStruckByLightning(lightningBolt);
		for (SkillBase skill : this.skills.values()) {
			skill.lightning(this, lightningBolt);
		}
	}
	public void addSkill(SkillBase skill, EntityPlayer player) {
		skill.commandingPlayer = player;
		if (skill.isTask()) {
			this.tasks.addTask(3, new EntityAISkill(this, skill));
		}
		this.setSelected(false);
		this.skills.put(skill.getClass(), skill);
	}
	public void removeSkill(SkillBase skill) {
		if (skill.isTask()) {
			this.tasks.removeTask(new EntityAISkill(this, skill));
		}
		this.skills.remove(skill.getClass());
	}
	public boolean createTower(int height, Block block) {
		boolean placed = false;
		for (int y = 0; y < height; ++y) {
			this.jump();
			placed = this.placeBlock(block, this.getPosition().add(0, y, 0));
			if (!placed) {
				break;
			}
		}
		return placed;
	}
}
