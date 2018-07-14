package mod.akrivus.clones.entity.ai;

import java.util.List;

import mod.akrivus.clones.entity.EntityCloneBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityArrow.PickupStatus;

public class EntityAIPickUpItems extends EntityAIBase {
    private final EntityCloneBase clone;
    private final double movementSpeed;
    private Entity item;
    public EntityAIPickUpItems(EntityCloneBase entityIn, double movementSpeedIn) {
        this.clone = entityIn;
        this.movementSpeed = movementSpeedIn;
        this.setMutexBits(8);
    }
    public boolean shouldExecute() {
    	List<EntityItem> list = this.clone.world.<EntityItem>getEntitiesWithinAABB(EntityItem.class, this.clone.getEntityBoundingBox().expand(4.0D, 1.0D, 4.0D));
        double maxDistance = Double.MAX_VALUE;
        for (EntityItem item : list) {
            double newDistance = this.clone.getDistanceSqToEntity(item);
            if (newDistance <= maxDistance && this.clone.canEntityBeSeen(item)) {
                maxDistance = newDistance;
                this.item = item;
            }
        }
    	List<EntityArrow> arrows = this.clone.world.<EntityArrow>getEntitiesWithinAABB(EntityArrow.class, this.clone.getEntityBoundingBox().expand(4.0D, 1.0D, 4.0D));
        for (EntityArrow arrow : arrows) {
            double newDistance = this.clone.getDistanceSqToEntity(arrow);
            if (newDistance <= maxDistance && this.clone.canEntityBeSeen(arrow) && arrow.pickupStatus == PickupStatus.ALLOWED) {
                maxDistance = newDistance;
                this.item = arrow;
            }
        }
        return this.item != null;
    }
    public boolean continueExecuting() {
        return this.item != null && !this.item.isDead && this.clone.canEntityBeSeen(this.item) && !this.clone.getNavigator().noPath();
    }
    public void startExecuting() {
        this.clone.getLookHelper().setLookPositionWithEntity(this.item, 60.0F, 60.0F);
    }
    public void resetTask() {
        this.clone.getNavigator().clearPathEntity();
        this.item = null;
    }
    public void updateTask() {
        if (this.clone.getDistanceSqToEntity(this.item) > 1) {
        	this.clone.getNavigator().tryMoveToEntityLiving(this.item, this.movementSpeed);
        }
    }
}
