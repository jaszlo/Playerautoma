package net.jasper.mod.util;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.jasper.mod.PlayerAutomaClient;
import net.jasper.mod.mixins.PlayerEntityAccessor;
import net.jasper.mod.util.keybinds.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.UUID;

public class ReplayPreviewEntity extends PlayerEntity {

    public static class Renderer extends LivingEntityRenderer<PlayerEntity, PlayerEntityModel<PlayerEntity>> {
        public Renderer(EntityRendererFactory.Context ctx, boolean slim) {
            super(ctx, new PlayerEntityModel<>(ctx.getPart(slim ? EntityModelLayers.PLAYER_SLIM : EntityModelLayers.PLAYER), slim), 0.5f);
        }


        @Override
        public Identifier getTexture(PlayerEntity entity) {
            if (MinecraftClient.getInstance().player == null) return null;
            return MinecraftClient.getInstance().player.getSkinTextures().texture();
        }
    }

    private static final String NAMESPACE = "playerautoma";
    private static final String IDENTIFIER_PATH = "replay_preview";

    public static final EntityType<ReplayPreviewEntity> REPLAY_PREVIEW_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(NAMESPACE, IDENTIFIER_PATH),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, ReplayPreviewEntity::entityFactory).fireImmune().dimensions(EntityDimensions.fixed(2f, 2f)).build()
    );

    public static final EntityModelLayer REPLAY_PREVIEW_ENTITY_LAYER = new EntityModelLayer(new Identifier(NAMESPACE, IDENTIFIER_PATH), "main");

    public static void register() {
        FabricDefaultAttributeRegistry.register(REPLAY_PREVIEW_ENTITY_TYPE, createAttributes());
        EntityRendererRegistry.register(REPLAY_PREVIEW_ENTITY_TYPE, (context) -> new Renderer(context, false));
        EntityModelLayerRegistry.registerModelLayer(
                REPLAY_PREVIEW_ENTITY_LAYER,
                () -> TexturedModelData.of(PlayerEntityModel.getTexturedModelData(Dilation.NONE, false), 64, 64)
        );
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (Constants.preview != null)
                PlayerAutomaClient.LOGGER.info(Constants.preview.getBlockPos().toString());
        });

    }

    private static DefaultAttributeContainer createAttributes() {
        return PlayerEntity.createPlayerAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, Double.MAX_VALUE)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.0D)
            .build();
    }


    // Prevent damage, knock-back, block modification and collisions
    @Override
    public boolean damage(DamageSource source, float amount) { return false; }
    @Override
    public boolean canModifyBlocks() { return false; }
    @Override
    public boolean collidesWith(Entity other) { return false; }
    @Override
    public boolean canBeHitByProjectile() { return false; }
    @Override
    public boolean isMainPlayer() { return false; }

    private static ReplayPreviewEntity entityFactory(EntityType<? extends Entity> type, World world) {
        return new ReplayPreviewEntity(MinecraftClient.getInstance());
    }

    public ReplayPreviewEntity(MinecraftClient client) {
        // PlayerEntity
        super(client.world, client.player.getBlockPos(), client.player.getYaw(), client.getGameProfile());

        // ClientPlayerEntity
        //super(client, Objects.requireNonNull(client.world), Objects.requireNonNull(client.getNetworkHandler()), Objects.requireNonNull(Objects.requireNonNull(client.player).getStatHandler()), client.player.getRecipeBook(), false, false);
        //this.input = new Input();
        this.setInvulnerable(true);
        this.setPosition(client.player.getPos());
        this.setCustomName(Text.of(""));
        this.setCustomNameVisible(true);
        this.setUuid(UUID.randomUUID());
        PlayerEntityAccessor accessor = (PlayerEntityAccessor) this;
        accessor.setType(REPLAY_PREVIEW_ENTITY_TYPE);
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }
}
