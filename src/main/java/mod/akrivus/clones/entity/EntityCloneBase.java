package mod.akrivus.clones.entity;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import mod.akrivus.clones.entity.ai.EntityAIEatFood;
import mod.akrivus.clones.entity.ai.EntityAIPickUpItems;
import mod.akrivus.clones.util.LinguisticsHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityArrow.PickupStatus;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class EntityCloneBase extends EntityCreature implements IInventoryChangedListener {
	private static final DataParameter<Boolean> SELECTED = EntityDataManager.<Boolean>createKey(EntityCloneBase.class, DataSerializers.BOOLEAN);
	public ResourceLocation texture = new ResourceLocation("textures/entity/steve.png");
	public BufferedImage playerSkin;
	public boolean skinChanged;
	public boolean skinLoaded;
	public boolean hasSmallArms;
	public boolean modelChanged;
	public boolean fromThePast;
	private int foodLevel = 20;
    private float foodSaturationLevel = 5.0F;
    private float foodExhaustionLevel;
    private int foodTimer;
	private Class<? extends Item> itemClass = Item.class;
	private Item itemType = Items.AIR;
    private int attackCooldown;
    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;
	private InventoryBasic inventory;
	private BlockPos lookPosition;
	public EntityCloneBase(World world) {
		super(world);
		this.stepHeight = 1.0F;
		this.createInventory();
		this.setSize(0.6F, 1.8F);
		((PathNavigateGround) this.getNavigator()).setBreakDoors(true);
		((PathNavigateGround) this.getNavigator()).setEnterDoors(true);
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_SPEED);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5D);
        this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIOpenDoor(this, true));
		this.tasks.addTask(2, new EntityAIPickUpItems(this, 0.6));
		this.tasks.addTask(2, new EntityAIEatFood(this));
		this.dataManager.register(SELECTED, false);
	}
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
        NBTTagList nbttaglist = new NBTTagList();
        compound.setInteger("foodLevel", this.foodLevel);
        compound.setInteger("foodTickTimer", this.foodTimer);
        compound.setFloat("foodSaturationLevel", this.foodSaturationLevel);
        compound.setFloat("foodExhaustionLevel", this.foodExhaustionLevel);
        compound.setBoolean("fromThePast", this.fromThePast);
        compound.setBoolean("selected", this.isSelected());
        for (int i = 0; i < this.inventory.getSizeInventory(); ++i) {
            ItemStack itemstack = this.inventory.getStackInSlot(i);
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setByte("slot", (byte) i);
            itemstack.writeToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
        }
        compound.setTag("items", nbttaglist);
	}
	public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        NBTTagList nbttaglist = compound.getTagList("items", 10);
        this.createInventory();
        this.foodLevel = compound.getInteger("foodLevel");
        this.foodTimer = compound.getInteger("foodTickTimer");
        this.foodSaturationLevel = compound.getFloat("foodSaturationLevel");
        this.foodExhaustionLevel = compound.getFloat("foodExhaustionLevel");
        this.fromThePast = compound.getBoolean("fromThePast");
        this.setSelected(compound.getBoolean("selected"));
        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("slot") & 255;
            if (j >= 0 && j < this.inventory.getSizeInventory()) {
                this.inventory.setInventorySlotContents(j, new ItemStack(nbttagcompound));
            }
        }
	}
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (!this.world.isRemote) {
			if (!this.isDead && this.hurtResistantTime == 0) {
				for (EntityItem item : this.world.getEntitiesWithinAABB(EntityItem.class, this.getEntityBoundingBox().expand(1.0D, 0.0D, 1.0D))) {
		            if (!item.isDead && !item.getEntityItem().isEmpty()) {
		                boolean pickedUp = this.addItemToInventory(item.getEntityItem());
		                if (pickedUp) {
		                	this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, this.getSoundVolume(), this.getSoundPitch());
		                	item.setDead();
		                }
		            }
		        }
				for (EntityArrow arrow : this.world.getEntitiesWithinAABB(EntityArrow.class, this.getEntityBoundingBox().expand(1.0D, 0.0D, 1.0D))) {
		            if (!arrow.isDead && arrow.pickupStatus == PickupStatus.ALLOWED) {
		                boolean pickedUp = this.addItemToInventory(new ItemStack(Items.ARROW));
		                if (pickedUp) {
		                	this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, this.getSoundVolume(), this.getSoundPitch());
		                	arrow.setDead();
		                }
		            }
		        }
			}
	        if (this.foodExhaustionLevel > 4.0F) {
	            this.foodExhaustionLevel -= 4.0F;
	            if (this.foodSaturationLevel > 0.0F) {
	                this.foodSaturationLevel = Math.max(this.foodSaturationLevel - 1.0F, 0.0F);
	            }
	        }
	        if (this.foodSaturationLevel > 0.0F && this.shouldHeal() && this.foodLevel >= 20) {
	            ++this.foodTimer;
	            if (this.foodTimer >= 10) {
	                float exhaustion = Math.min(this.foodSaturationLevel, 6.0F);
	                this.heal(exhaustion / 6.0F);
	                this.addExhaustion(exhaustion);
	                this.foodTimer = 0;
	            }
	        }
	        else if (this.foodLevel >= 18 && this.shouldHeal()) {
	            ++this.foodTimer;
	            if (this.foodTimer >= 80) {
	                this.heal(1.0F);
	                this.addExhaustion(6.0F);
	                this.foodTimer = 0;
	            }
	        }
	        else if (this.foodLevel <= 0) {
	            ++this.foodTimer;
	            if (this.foodTimer >= 80) {
	                this.attackEntityFrom(DamageSource.STARVE, 1.0F);
	                this.foodTimer = 0;
	            }
	        }
	        else {
	            this.foodTimer = 0;
	        }
			if (this.lookPosition != null) {
				this.lookAt(this.lookPosition);
			}
		}
		else {
			this.setGlowing(this.isSelected());
			if (!this.skinLoaded) {
				this.setPlayerSkin();
			}
		}
	}
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);
		if (!this.world.isRemote) {
			if (this.hasCustomName() && this.world.getGameRules().getBoolean("showDeathMessages")) {
				for (EntityPlayer player : this.world.playerEntities) {
					player.sendMessage(cause.getDeathMessage(this));
				}
			}
			if (this.isFromThePast() && !cause.equals(DamageSource.OUT_OF_WORLD)) {
				for (int i = 0; i < this.world.loadedEntityList.size(); ++i) {
					Entity entity = this.world.loadedEntityList.get(i);
					if (entity instanceof EntityClone) {
						EntityClone clone = (EntityClone) entity;
						if (this.getName().equals(clone.getName()) && clone.isFromThePast()) {
							clone.attackEntityFrom(DamageSource.OUT_OF_WORLD, clone.getMaxHealth());
						}
					}
		        }
				for (EntityPlayer player : this.world.playerEntities) {
					if (this.getName().equals(player.getName())) {
						player.attackEntityFrom(DamageSource.OUT_OF_WORLD, player.getMaxHealth());
					}
				}
			}
			for (int i = 0; i < this.inventory.getSizeInventory(); ++i) {
				this.entityDropItem(this.inventory.getStackInSlot(i), 0.0F);
			}
			Iterator<ItemStack> iterator = this.getEquipmentAndArmor().iterator();
			while (iterator.hasNext()) {
				this.entityDropItem(iterator.next(), 0.0F);
			}
		}
	}
	protected boolean processInteract(EntityPlayer player, EnumHand hand) {
		return false;
	}
	public void setFromThePast(boolean fromThePast) {
		this.fromThePast = fromThePast;
	}
	public boolean isFromThePast() {
		return this.fromThePast;
	}
	public void feedback(String message) {
		for (EntityPlayer player : this.world.playerEntities) {
			if (player.getDistanceToEntity(this) > 16) {
				player.sendMessage(new TextComponentString("<" + this.getName() + "> " + message));
			}
		}
	}
	protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source) {
		return;
	}
	protected void jump() {
		this.addExhaustion(0.05F);
		super.jump();
	}
	public boolean attackEntityFrom(DamageSource source, float amount) {
		this.addExhaustion(0.1F);
		return super.attackEntityFrom(source, amount);
	}
	public boolean attackEntityAsMob(Entity entity) {
		this.addExhaustion(0.1F);
		boolean flag = false;
		if (this.setRangedAttacker() && entity instanceof EntityLivingBase) {
			EntityLivingBase target = (EntityLivingBase) entity;
			double distance = this.getDistanceSq(target.posX, target.getEntityBoundingBox().minY, target.posZ);
            boolean canSee = this.getEntitySenses().canSee(target);
            boolean canStillSee = this.seeTime > 0;
            flag = this.setRangedAttacker();
            if (canSee != canStillSee) {
                this.seeTime = 0;
            }
            if (canSee) {
                ++this.seeTime;
            }
            else {
                --this.seeTime;
            }
            if (distance <= 32 && this.seeTime >= 20) {
                this.getNavigator().clearPathEntity();
                ++this.strafingTime;
            }
            else {
                this.tryToMoveTo(entity.getPosition());
                this.strafingTime = -1;
            }
            if (this.strafingTime >= 20) {
                if (this.getRNG().nextFloat() < 0.3) {
                    this.strafingClockwise = !this.strafingClockwise;
                }
                if (this.getRNG().nextFloat() < 0.3) {
                    this.strafingBackwards = !this.strafingBackwards;
                }
                this.strafingTime = 0;
            }
            if (this.strafingTime > -1) {
                if (distance > 24) {
                    this.strafingBackwards = false;
                }
                else if (distance < 8) {
                    this.strafingBackwards = true;
                }
                this.getMoveHelper().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
                this.lookAt(target);
            }
            else {
                this.lookAt(target);
            }
            if (this.isHandActive()) {
                if (!canSee && this.seeTime < -60) {
                    this.resetActiveHand();
                }
                else if (canSee) {
                    int i = this.getItemInUseMaxCount();
                    if (i >= 20) {
                        this.resetActiveHand();
                        this.attackEntityWithRangedAttack(target, ItemBow.getArrowVelocity(i));
                        this.attackTime = this.attackCooldown;
                    }
                }
            }
            else if (--this.attackTime <= 0 && this.seeTime >= -60) {
                this.setActiveHand(EnumHand.MAIN_HAND);
            }
        }
        else {
			float damage = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
	        int knockback = 0;
	        if (entity instanceof EntityLivingBase) {
	            damage += EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), ((EntityLivingBase) entity).getCreatureAttribute());
	            knockback += EnchantmentHelper.getKnockbackModifier(this);
	        }
	        flag = entity.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
	        if (flag) {
	            if (knockback > 0 && entity instanceof EntityLivingBase) {
	                ((EntityLivingBase) entity).knockBack(this, knockback * 0.5F, MathHelper.sin(this.rotationYaw * 0.017453292F), -MathHelper.cos(this.rotationYaw * 0.017453292F));
	                this.motionX *= 0.6D;
	                this.motionZ *= 0.6D;
	            }
	            int fireTime = EnchantmentHelper.getFireAspectModifier(this);
	            if (fireTime > 0) {
	                entity.setFire(fireTime * 4);
	            }
	            if (entity instanceof EntityPlayer) {
	                EntityPlayer player = (EntityPlayer) entity;
	                ItemStack mainHand = this.getHeldItemMainhand();
	                ItemStack offHand = player.isHandActive() ? player.getActiveItemStack() : ItemStack.EMPTY;
	                if (!mainHand.isEmpty() && !offHand.isEmpty() && mainHand.getItem() instanceof ItemAxe && mainHand.getItem() == Items.SHIELD) {
	                    float seed = 0.25F + EnchantmentHelper.getEfficiencyModifier(this) * 0.05F;
	                    if (this.rand.nextFloat() < seed) {
	                        player.getCooldownTracker().setCooldown(Items.SHIELD, 100);
	                        this.world.setEntityState(player, (byte) 30);
	                    }
	                }
	            }
	            this.applyEnchantments(this, entity);
	        }
        }
        return flag;
    }
	public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
		EntityTippedArrow arrow = new EntityTippedArrow(this.world, this);
		double distanceFromTargetX = target.posX - this.posX;
        double distanceFromTargetY = target.getEntityBoundingBox().minY + target.height - arrow.posY;
        double distanceFromTargetZ = target.posZ - this.posZ;
        double distanceFromTargetS = (double) MathHelper.sqrt(distanceFromTargetX * distanceFromTargetX + distanceFromTargetY * distanceFromTargetY);
        arrow.setThrowableHeading(distanceFromTargetX, distanceFromTargetY + distanceFromTargetS * 0.20000000298023224D, distanceFromTargetZ, 1.6F, 0.0F);
        arrow.setDamage(distanceFactor * 2.0D + this.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue() + this.rand.nextGaussian() * 0.25D);
        arrow.pickupStatus = PickupStatus.ALLOWED;
        int power = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.POWER, this);
        int punch = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.PUNCH, this);
        boolean flame = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FLAME, this) > 0;
        if (power > 0) {
            arrow.setDamage(arrow.getDamage() + (double) power * 0.5D + 0.5D);
        }
        if (punch > 0) {
            arrow.setKnockbackStrength(punch);
        }
        if (flame) {
            arrow.setFire(100);
        }
        ItemStack itemstack = this.getHeldItem(EnumHand.OFF_HAND);
        if (itemstack.getItem() == Items.TIPPED_ARROW) {
            arrow.setPotionEffect(itemstack);
        }
        this.playSound(SoundEvents.ENTITY_ARROW_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        this.world.spawnEntity(arrow);
    }
	public boolean setRangedAttacker() {
		boolean hasArrows = false;
		if (this.getHeldItemMainhand().getItem() == Items.BOW) {
			hasArrows = this.getItemInInventory(Items.ARROW).getCount() > 0;
			if (!hasArrows) {
				hasArrows = this.getItemInInventory(Items.TIPPED_ARROW).getCount() > 0;
				if (!hasArrows) {
					hasArrows = this.getItemInInventory(Items.SPECTRAL_ARROW).getCount() > 0;
					if (!hasArrows) {
						return false;
					}
					else {
						this.setHeldItem(EnumHand.OFF_HAND, this.swapItemInInventory(this.getItemInInventory(Items.SPECTRAL_ARROW), this.getHeldItemOffhand()));
					}
				}
				else {
					this.setHeldItem(EnumHand.OFF_HAND, this.swapItemInInventory(this.getItemInInventory(Items.TIPPED_ARROW), this.getHeldItemOffhand()));
				}
			}
			else {
				this.setHeldItem(EnumHand.OFF_HAND, this.swapItemInInventory(this.getItemInInventory(Items.ARROW), this.getHeldItemOffhand()));
			}
			return hasArrows;
		}
		return false;
	}
	public float getJumpUpwardsMotion() {
		return 0.84F;
	}
	public boolean shouldHeal() {
		return this.getHealth() > 0.0F && this.getHealth() < this.getMaxHealth();
	}
	public void addStats(int foodLevelIn, float foodSaturationModifier) {
        this.foodLevel = Math.min(foodLevelIn + this.foodLevel, 20);
        this.foodSaturationLevel = Math.min(this.foodSaturationLevel + foodLevelIn * foodSaturationModifier * 2.0F, this.foodLevel);
    }
    public void addStats(ItemFood foodItem, ItemStack stack) {
        this.addStats(foodItem.getHealAmount(stack), foodItem.getSaturationModifier(stack));
    }
	public int getFoodLevel() {
        return this.foodLevel;
    }
    public boolean needFood() {
        return this.foodLevel < 20;
    }
    public void addExhaustion(float exhaustion) {
        this.foodExhaustionLevel = Math.min(this.foodExhaustionLevel + exhaustion, 40.0F);
    }
    public float getSaturationLevel() {
        return this.foodSaturationLevel;
    }
    public void setFoodLevel(int foodLevelIn) {
        this.foodLevel = foodLevelIn;
    }
    public void playBiteSound() {
    	this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
    }
	public boolean addItemToInventory(ItemStack stack) {
		ItemStack placement = this.inventory.addItem(stack);
		return placement.isEmpty();
	}
	public boolean removeItemFromInventory(Item item, int amount) {
		ItemStack stack = this.getItemInInventory(item);
		if (stack.isEmpty() || stack.getCount() < amount) {
			return false;
		}
		else {
			stack.splitStack(amount);
			return true;
		}
	}
	public boolean removeItemFromInventory(Item item) {
		return this.removeItemFromInventory(item, 1);
	}
	public boolean removeItemFromInventory(ItemStack item, int amount) {
		ItemStack stack = this.getItemInInventory(item);
		if (stack.isEmpty() || stack.getCount() < amount) {
			return false;
		}
		else {
			stack.splitStack(amount);
			return true;
		}
	}
	public boolean removeItemFromInventory(ItemStack item) {
		return this.removeItemFromInventory(item, 1);
	}
	public ItemStack getItemInInventory(Item item, int start) {
		for (int i = start; i < this.inventory.getSizeInventory(); ++i) {
			if (this.inventory.getStackInSlot(i).getItem() == item) {
				return this.inventory.getStackInSlot(i);
			}
		}
		return ItemStack.EMPTY;
	}
	public ItemStack getItemInInventory(Item item) {
		return this.getItemInInventory(item, 0);
	}
	public ItemStack getItemInInventory(ItemStack item, int start) {
		for (int i = start; i < this.inventory.getSizeInventory(); ++i) {
			if (this.inventory.getStackInSlot(i).isItemEqual(item)) {
				return this.inventory.getStackInSlot(i);
			}
		}
		return ItemStack.EMPTY;
	}
	public ItemStack getItemInInventory(ItemStack item) {
		return this.getItemInInventory(item, 0);
	}
	public ItemStack getItemInInventory(Class<? extends Item> item, int start) {
		for (int i = start; i < this.inventory.getSizeInventory(); ++i) {
			if (item.isInstance(this.inventory.getStackInSlot(i).getItem())) {
				return this.inventory.getStackInSlot(i);
			}
		}
		return ItemStack.EMPTY;
	}
	public ItemStack getItemInInventory(Class<? extends Item> item) {
		return this.getItemInInventory(item, 0);
	}
	public int indexOfItemInInventory(Item item, int start) {
		for (int i = start; i < this.inventory.getSizeInventory(); ++i) {
			if (this.inventory.getStackInSlot(i).getItem() == item) {
				return i;
			}
		}
		return -1;
	}
	public int indexOfItemInInventory(Item item) {
		return this.indexOfItemInInventory(item, 0);
	}
	public int indexOfItemInInventory(ItemStack item, int start) {
		for (int i = start; i < this.inventory.getSizeInventory(); ++i) {
			if (this.inventory.getStackInSlot(i).isItemEqual(item)) {
				return i;
			}
		}
		return -1;
	}
	public int indexOfItemInInventory(ItemStack item) {
		return this.indexOfItemInInventory(item, 0);
	}
	public ItemStack swapItemInInventory(Item out, ItemStack in) {
		int index = this.indexOfItemInInventory(out);
		if (index < 0) {
			return ItemStack.EMPTY;
		}
		else {
			ItemStack stack = this.inventory.getStackInSlot(index).copy();
			this.inventory.setInventorySlotContents(index, in);
			return stack;
		}
	}
	public ItemStack swapItemInInventory(ItemStack out, ItemStack in) {
		int index = this.indexOfItemInInventory(out);
		if (index < 0) {
			return ItemStack.EMPTY;
		}
		else {
			ItemStack stack = this.inventory.getStackInSlot(index).copy();
			this.inventory.setInventorySlotContents(index, in);
			return stack;
		}
	}
	public ItemStack getBiggestStackInInventory(Item[] items) {
		ItemStack stack = ItemStack.EMPTY;
		int biggestStack = 0;
		for (Item item : items) {
			ItemStack fetched = this.getItemInInventory(item);
			if (!stack.isEmpty()) {
				if (fetched.getCount() > biggestStack) {
					stack = fetched;
				}
			}
		}
		return stack;
	}
	public Item getBiggestItemInInventory(Item[] items) {
		return this.getBiggestStackInInventory(items).getItem();
	}
	public ItemStack searchForStackInInventory(String search, int start) {
		for (int i = start; i < this.inventory.getSizeInventory(); ++i) {
			for (String token : LinguisticsHelper.getTokens(this.inventory.getStackInSlot(i).getDisplayName())) {
				if (LinguisticsHelper.getDistance(token, search, true) < 3) {
					return this.inventory.getStackInSlot(i);
				}
			}
		}
		return ItemStack.EMPTY;
	}
	public ItemStack searchForStackInInventory(String search) {
		return this.searchForStackInInventory(search, 0);
	}
	public Item searchForItemInInventory(String search, int start) {
		return this.searchForStackInInventory(search, start).getItem();
	}
	public Item searchForItemInInventory(String search) {
		return this.searchForStackInInventory(search).getItem();
	}
	public ItemStack getBreedingItem(EntityAnimal animal) {
		for (int i = 0; i < this.inventory.getSizeInventory(); ++i) {
			ItemStack stack = this.inventory.getStackInSlot(i);
			if (animal.isBreedingItem(stack)) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}
	public void createInventory() {
        InventoryBasic inventory = this.inventory;
        this.inventory = new InventoryBasic("inventory", false, 36);
        if (inventory != null) {
        	inventory.removeInventoryChangeListener(this);
            for (int i = 0; i < this.inventory.getSizeInventory(); ++i) {
                ItemStack itemstack = inventory.getStackInSlot(i);
                this.inventory.setInventorySlotContents(i, itemstack.copy());
            }
        }
        this.inventory.addInventoryChangeListener(this);
    }
	public void onInventoryChanged(IInventory inventory) {
		int importance = 0;
		for (int i = 0; i < this.inventory.getSizeInventory(); ++i) {
			ItemStack stack = this.inventory.getStackInSlot(i);
			EntityEquipmentSlot slot = EntityCloneBase.getSlotForItemStack(stack);
			if ((this.canEquipItem(stack) && stack.isItemStackDamageable() && (this.itemClass != null && this.itemClass.isInstance(stack.getItem())))) {
				ItemStack equipped = this.indexOfItemInInventory(this.getItemStackFromSlot(slot)) > 0 ? this.getItemStackFromSlot(slot) : ItemStack.EMPTY;
				int equippedUses = equipped.getMaxDamage() - equipped.getItemDamage();
				int stackUses = stack.getMaxDamage() - stack.getItemDamage();
				if (importance <= 3 && (equipped.isEmpty() || stackUses > equippedUses)) {
					this.setItemStackToSlot(slot, stack);
					importance = 3;
				}
			}
			else if (this.itemClass == null && stack.getItem() == this.itemType) {
				if (importance <= 2) {
					this.setItemStackToSlot(slot, stack);
					importance = 2;
				}
			}
			else if (this.itemClass != null && this.itemClass.isInstance(stack.getItem())) {
				if (importance <= 1) {
					this.setItemStackToSlot(slot, stack);
					importance = 1;
				}
			}
		}
	}
	public void setItemClass(Class<? extends Item> itemClass) {
		this.itemClass = itemClass;
		this.onInventoryChanged(this.inventory);
	}
	public void setItemClass(Item itemType) {
		this.itemClass = null;
		this.itemType = itemType;
		this.onInventoryChanged(this.inventory);
	}
	public void openGUI(EntityPlayer player) {
		this.inventory.setCustomName(this.getName());
		player.displayGUIChest(this.inventory);
    }
	public boolean tryToMoveTo(int x, int y, int z) {
		return this.getNavigator().tryMoveToXYZ(x, y, z, 0.8F);
	}
	public boolean tryToMoveTo(BlockPos pos) {
		return this.tryToMoveTo(pos.getX(), pos.getY(), pos.getZ());
	}
	public void lookAt(Entity entity) {
		if (entity != null) {
			this.getLookHelper().setLookPositionWithEntity(entity, 60.0F, 60.0F);
			this.lookPosition = entity.getPosition();
		}
	}
	public void lookAt(int x, int y, int z) {
		this.getLookHelper().setLookPosition(x, y, z, 60.0F, 60.0F);
		this.lookPosition = new BlockPos(x, y, z);
	}
	public void lookAt(BlockPos pos) {
		this.lookAt(pos.getX(), pos.getY(), pos.getZ());
	}
	public boolean placeBlock(Block block, int x, int y, int z) {
		boolean canPlace = this.world.mayPlace(block, new BlockPos(x, y, z), true, EnumFacing.UP, null);
		if (canPlace) {
			boolean hasBlock = this.removeItemFromInventory(Item.getItemFromBlock(block));
			if (hasBlock) {
				return this.world.setBlockState(new BlockPos(x, y, z), block.getDefaultState(), 3);
			}
		}
		return false;
	}
	public boolean placeBlock(Block block, BlockPos pos) {
		return this.placeBlock(block, pos.getX(), pos.getY(), pos.getZ());
	}
	public boolean tryToBreakBlock(int x, int y, int z) {
		IBlockState state = this.world.getBlockState(new BlockPos(x, y, z));
		if (!state.getMaterial().isToolNotRequired()) {
			return this.getHeldItemMainhand().canHarvestBlock(state);
		}
		return true;
	}
	public boolean tryToBreakBlock(BlockPos pos) {
		return this.tryToBreakBlock(pos.getX(), pos.getY(), pos.getZ());
	}
	public boolean breakBlock(int x, int y, int z) {
		if (this.tryToBreakBlock(x, y, z)) {
			return this.world.destroyBlock(new BlockPos(x, y, z), true);
		}
		return false;
	}
	public boolean breakBlock(BlockPos pos) {
		return this.breakBlock(pos.getX(), pos.getY(), pos.getZ());
	}
	public void setPlayerSkin() {
		HttpURLConnection url = null;
        try {
        	url = (HttpURLConnection)(new URL("http://skins.minecraft.net/MinecraftSkins/" + this.getCustomNameTag() + ".png")).openConnection(Minecraft.getMinecraft().getProxy());
        	url.setDoInput(true);
        	url.setDoOutput(false);
        	url.connect();
            if (url.getResponseCode() / 100 == 2) {
                BufferedImage bufferedimage = new ImageBufferDownload().parseUserSkin(TextureUtil.readBufferedImage(url.getInputStream()));
                this.playerSkin = bufferedimage;
            }
            else {
            	this.playerSkin = null;
            }
        }
        catch (Exception ex) {
        	ex.printStackTrace();
        }
        finally {
        	this.skinLoaded = true;
            if (url != null) {
                url.disconnect();
            }
        }
	}
	public void calculateArmThickness() {
		Color pixel = new Color(this.playerSkin.getRGB(51, 16), true);
	    this.hasSmallArms = pixel.getAlpha() == 0;
	}
	public boolean isSelected() {
		return this.dataManager.get(SELECTED);
	}
	public void setSelected(boolean selected) {
		this.dataManager.set(SELECTED, selected);
	}
	public int getHarvestSpeed(IBlockState state) {
		ItemStack stack = this.getHeldItemMainhand();
		if (stack.getItem() instanceof ItemTool) {
			return (int)(stack.getStrVsBlock(state));
		}
		return 20;
	}
	public int getAttackSpeed() {
		return (int)((1.0D / this.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue()) * 20);
	}
	public int getAttackRange() {
		return this.setRangedAttacker() ? 32 : 3;
	}
	public int getPlaceSpeed() {
		return 10;
	}
	public int getBuildRange() {
		return 5;
	}
}
