package com.samsthenerd.hexgloop.blocks;

import com.samsthenerd.hexgloop.utils.ClientUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import at.petrak.hexcasting.api.HexAPI;

public class BlockEnlightenedCollision extends HalfTransparentBlock{
    public final boolean passByDefault;
    public BlockEnlightenedCollision(Properties settings, boolean passByDefault) {
        super(settings);
        this.passByDefault = passByDefault;
    }

    // copied from glass block
    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 1.0f;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter world, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        boolean isEnlightened = false;
        if(context instanceof EntityCollisionContext entityShapeContext && entityShapeContext.getEntity() instanceof Player player){
            if(player instanceof ServerPlayer sPlayer){
                // server
                net.minecraft.advancements.AdvancementHolder adv = sPlayer.getServer().getAdvancements().get(HexAPI.modLoc("enlightenment"));
                PlayerAdvancements advs = sPlayer.getAdvancements();
                if(advs.getOrStartProgress(adv) != null)
                    isEnlightened = advs.getOrStartProgress(adv).isDone();
            } else {
                // client - probably important because y'know, movement
                isEnlightened = ClientUtils.isEnlightened();
            }
        }
        return isEnlightened ^ passByDefault ? Shapes.empty() : state.getShape(world, pos);
    }
}
