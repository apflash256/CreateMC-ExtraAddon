package com.sturdycobble.createrevision.contents.heat.source;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.sturdycobble.createrevision.config.CreateRevisionConfig;
import com.sturdycobble.createrevision.contents.heat.CapabilityHeat;
import com.sturdycobble.createrevision.contents.heat.HeatContainer;
import com.sturdycobble.createrevision.contents.heat.IHeatableTileEntity;
import com.sturdycobble.createrevision.contents.heat.SimpleHeatContainer;
import com.sturdycobble.createrevision.init.ModTileEntityTypes;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.Tags.Blocks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class FrictionHeaterTileEntity extends KineticTileEntity implements IHeatableTileEntity, ITickableTileEntity {

	public boolean checkConnection;
	
	private double heatCapacity;
	private double conductivity;
	private double sourcePower;
	private Map<IHeatableTileEntity, SimpleEntry<Direction, Long>> neighborMap;
	
	public FrictionHeaterTileEntity() {
		super(ModTileEntityTypes.FRICTION_HEATER.get());
		heatCapacity = 4;
		conductivity = 0.3;
		checkConnection= true;
		sourcePower = 0.2;
		neighborMap = new HashMap<IHeatableTileEntity, SimpleEntry<Direction, Long>>();
	}
	
	private LazyOptional<HeatContainer> heatContainer = LazyOptional.of(() -> new SimpleHeatContainer(300, heatCapacity, conductivity));

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		Direction facing = world.getBlockState(pos).get(FrictionHeaterBlock.FACING);
		
		if (cap == CapabilityHeat.HEAT_CAPABILITY && !(side == facing || side == facing.getOpposite())) {
			return heatContainer.cast();
		}
		return super.getCapability(cap, null);
	}
	
	@Override
	public void tick() {
		if (checkConnection = true) {
			updateConnection();
			checkConnection = false;
		}

		if (world.getWorldInfo().getGameTime() % 5 == 0) {
			double temp = this.heatContainer.orElse(null).getTemp();;
			double heatCurrent = getHeatCurrent(neighborMap, temp);
			
			temp += (heatCurrent + getPower())/heatCapacity;
			
			this.heatContainer.orElse(null).setTemp(temp);
		}
	}

	@Override
	public void markConnection() {
		checkConnection = true;
	}

	public double getPower() {
		return isFrontBlocked() ? MathHelper.clamp(sourcePower*Math.abs(getSpeed())-0.1*(heatContainer.orElse(null).getTemp()-300), 0, 100) : 0;
	}
	
	public boolean isFrontBlocked() {
		Direction facing = world.getBlockState(pos).get(FrictionHeaterBlock.FACING);
		if (world.getBlockState(pos.offset(facing.getOpposite())).getBlock().isIn(Blocks.STONE) )
			return true;
		return false;
	}
	
	@Override
	public Map<IHeatableTileEntity, SimpleEntry<Direction, Long>> findNeighborNode() {
		BlockPos.Mutable mpos = new BlockPos.Mutable();
		TileEntity te;
		Direction facing = world.getBlockState(pos).get(FrictionHeaterBlock.FACING);
		Map<IHeatableTileEntity, SimpleEntry<Direction, Long>> nodeMap = new HashMap<IHeatableTileEntity, SimpleEntry<Direction, Long>>();
		for (Direction d : Direction.values()) {
			if (d != facing && d != facing.getOpposite()) {
				mpos.setPos(this.getPos());
				mpos.move(d);
				te = this.getWorld().getTileEntity(mpos);
				if (te != null && te instanceof IHeatableTileEntity) {
					if (((IHeatableTileEntity) te).isNode() == true) {
						nodeMap.put((IHeatableTileEntity) world.getTileEntity(mpos), 
								new SimpleEntry<Direction, Long>(d, 1L));
					}
				}
			}
		}
		return nodeMap;
	}

	@Override
	public void updateConnection() {
		neighborMap = findNeighborNode();
		for (IHeatableTileEntity neighbor : neighborMap.keySet())
			neighbor.markConnection();
	}

	@Override
	public boolean isNode() {
		return true;
	}
	
	@Override
	public float calculateStressApplied() {
		float impact = (float) CreateRevisionConfig.COMMON.friction_heater_stress_applied.get();
		return impact;
	}
	
	
	public Map<IHeatableTileEntity, SimpleEntry<Direction, Long>> getNeighborMap() {
		return neighborMap;
	}

	@Override
	public double getConductivity() {
		return conductivity;
	}
	
}
