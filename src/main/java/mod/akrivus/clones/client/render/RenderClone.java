package mod.akrivus.clones.client.render;

import mod.akrivus.clones.client.render.layer.LayerAlexSkin;
import mod.akrivus.clones.client.render.layer.LayerDeadmau5Head;
import mod.akrivus.clones.client.render.layer.LayerSteveSkin;
import mod.akrivus.clones.entity.EntityClone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerElytra;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;

public class RenderClone extends RenderLivingBase<EntityClone> {
	public RenderClone() {
		super(Minecraft.getMinecraft().getRenderManager(), new ModelPlayer(0.0F, false), 0.25F);
		this.addLayer(new LayerBipedArmor(this));
        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerArrow(this));
        this.addLayer(new LayerDeadmau5Head(this));
        this.addLayer(new LayerElytra(this));
        this.addLayer(new LayerAlexSkin(this));
        this.addLayer(new LayerSteveSkin(this));
	}
	protected boolean canRenderName(EntityClone entity) {
		return entity.hasCustomName();
	}
	protected void renderModel(EntityClone entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
		if (entity.skinLoaded) {    
            if (!entity.skinChanged) {
                if (entity.playerSkin != null) {
                    entity.texture = this.renderManager.renderEngine.getDynamicTextureLocation("csg_" + entity.getCustomNameTag(), new DynamicTexture(entity.playerSkin));
                    entity.calculateArmThickness();
                }
                else {
                    entity.texture = DefaultPlayerSkin.getDefaultSkin(entity.getUniqueID());
                    if (entity.texture.getResourcePath().equals("textures/entity/alex.png")) {
                        entity.hasSmallArms = true;
                    }
                }
                entity.skinChanged = true;
                if (!entity.modelChanged) {
                    this.mainModel = new ModelPlayer(0.0F, entity.hasSmallArms);
                    entity.modelChanged = true;
                }
            }
        }
    }
	protected ResourceLocation getEntityTexture(EntityClone entity) {
		return entity.texture;
	}
}
