package mod.akrivus.clones.entity.ai;

import mod.akrivus.clones.entity.EntityCloneBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

public class EntityAIEatFood extends EntityAIBase {
    private final EntityCloneBase clone;
    private boolean notified;
    public EntityAIEatFood(EntityCloneBase entityIn) {
        this.clone = entityIn;
        this.setMutexBits(8);
    }
    public boolean shouldExecute() {
        if (!this.clone.getItemInInventory(ItemFood.class).isEmpty() && this.clone.getFoodLevel() < 8.0F) {
        	if (!this.notified) {
        		this.clone.feedback("I'm very hungry.");
        	}
        	return true;
        }
        return false;
    }
    public void startExecuting() {
    	ItemStack food = this.clone.getItemInInventory(ItemFood.class);
    	this.clone.setItemClass(food.getItem());
    	this.clone.playBiteSound();
        this.clone.addStats((ItemFood)(food.getItem()), food);
        this.clone.removeItemFromInventory(food);
    }
    public void resetTask() {
        this.clone.getNavigator().clearPathEntity();
        this.clone.setItemClass(Item.class);
    }
}
