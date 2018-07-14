package mod.akrivus.clones.client.render.layer;

import mod.akrivus.clones.client.render.RenderClone;
import mod.akrivus.clones.entity.EntityClone;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

public class LayerAlexSkin implements LayerRenderer<EntityClone> {
    private final RenderClone cloneRenderer;
    private ModelPlayer model = new ModelPlayer(0.0F, true);
    public LayerAlexSkin(RenderClone cloneRendererIn) {
        this.cloneRenderer = cloneRendererIn;
    }
    public void doRenderLayer(EntityClone entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entitylivingbaseIn.hasSmallArms && !entitylivingbaseIn.isInvisible()) {
            this.model.setModelAttributes(this.cloneRenderer.getMainModel());
            this.model.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
            this.cloneRenderer.bindTexture(entitylivingbaseIn.texture);
            this.model.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }
    public boolean shouldCombineTextures() {
        return true;
    }
}