package dev.visfox.forbiddenreforged.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.visfox.forbiddenreforged.WrathCageBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class WrathCageRenderer implements BlockEntityRenderer<WrathCageBlockEntity, WrathCageRenderer.State> {
    public static class State extends BlockEntityRenderState {
        public boolean hasMob;
        public float spin;
    }

    public WrathCageRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WrathCageBlockEntity cage, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(cage, state, partialTicks, cameraPosition, breakProgress);
        state.hasMob = false;
        String mobId = cage.getMobId();
        if (mobId == null || cage.getLevel() == null) {
            return;
        }
        state.hasMob = true;
        state.spin = (cage.getLevel().getGameTime() % 360L) * 3.0F;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        // Entity rendering disabled - requires further investigation
        // The cage block itself will still render
    }

    @Override
    public AABB getRenderBoundingBox(WrathCageBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(1.0);
    }
}
