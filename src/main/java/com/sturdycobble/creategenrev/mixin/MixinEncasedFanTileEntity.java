package com.sturdycobble.creategenrev.mixin;

import com.simibubi.create.AllTags;
import com.simibubi.create.content.contraptions.base.GeneratingKineticTileEntity;
import com.simibubi.create.content.contraptions.components.fan.EncasedFanTileEntity;
import net.minecraft.tileentity.TileEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EncasedFanTileEntity.class)
public abstract class MixinEncasedFanTileEntity extends GeneratingKineticTileEntity{
    public MixinEncasedFanTileEntity(TileEntityType<? extends MixinEncasedFanTileEntity> type) {
        super(type);
    }

    @Shadow
    protected boolean updateAirFlow = true;

    public boolean isSuperHeated=false;

    /**@author ZoniIC645 **/
    @Overwrite(remap = false)
    public boolean blockBelowIsHot() {
        return true;
        //return this.world.getBlockState(this.pos.down()).getBlock().isIn(AllTags.AllBlockTags.FAN_HEATERS.tag);
    }

}
