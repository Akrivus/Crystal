package mod.akrivus.clones.client.render.layer;

import mod.akrivus.clones.client.render.RenderClone;
import mod.akrivus.clones.entity.EntityClone;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

public class LayerDeadmau5Head implements LayerRenderer<EntityClone> {
    private final RenderClone cloneRenderer;
    public LayerDeadmau5Head(RenderClone cloneRendererIn) {
        this.cloneRenderer = cloneRendererIn;
    }
    public void doRenderLayer(EntityClone entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if ("deadmau5".equals(entitylivingbaseIn.getName()) && !entitylivingbaseIn.isInvisible()) {
            this.cloneRenderer.bindTexture(entitylivingbaseIn.texture);
            for (int i = 0; i < 2; ++i) {
                float f = entitylivingbaseIn.prevRotationYaw + (entitylivingbaseIn.rotationYaw - entitylivingbaseIn.prevRotationYaw) * partialTicks - (entitylivingbaseIn.prevRenderYawOffset + (entitylivingbaseIn.renderYawOffset - entitylivingbaseIn.prevRenderYawOffset) * partialTicks);
                float f1 = entitylivingbaseIn.prevRotationPitch + (entitylivingbaseIn.rotationPitch - entitylivingbaseIn.prevRotationPitch) * partialTicks;
                GlStateManager.pushMatrix();
                GlStateManager.rotate(f, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(f1, 1.0F, 0.0F, 0.0F);
                GlStateManager.translate(0.375F * (float)(i * 2 - 1), 0.0F, 0.0F);
                GlStateManager.translate(0.0F, -0.375F, 0.0F);
                GlStateManager.rotate(-f1, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(-f, 0.0F, 1.0F, 0.0F);
                GlStateManager.scale(1.3333334F, 1.3333334F, 1.3333334F);
                ((ModelPlayer) this.cloneRenderer.getMainModel()).renderDeadmau5Head(0.0625F);
                GlStateManager.popMatrix();
            }
        }
    }
    public boolean shouldCombineTextures() {
        return true;
    }
}