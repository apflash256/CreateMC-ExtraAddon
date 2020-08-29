package com.sturdycobble.createrevision.contents.heat;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.simibubi.create.foundation.block.render.WrappedBakedModel;
import com.simibubi.create.foundation.utility.Iterate;
import com.sturdycobble.createrevision.init.ModBlockPartials;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

public class HeatPipeModel extends WrappedBakedModel {

	private static ModelProperty<PipeModelData> PIPE_PROPERTY = new ModelProperty<>();

	public HeatPipeModel(IBakedModel template) {
		super(template);
	}

	@Override
	public IModelData getModelData(ILightReader world, BlockPos pos, BlockState state, IModelData tileData) {
		PipeModelData data = new PipeModelData();
		for (Direction d : Iterate.directions)
			data.putRim(d, HeatPipeBlock.shouldDrawRim(world, pos, state, d));
		data.setEncased(HeatPipeBlock.shouldDrawCasing(world, pos, state));
		return new ModelDataMap.Builder().withInitial(PIPE_PROPERTY, data)
			.build();
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData data) {
		List<BakedQuad> quads = super.getQuads(state, side, rand, data);
		if (data instanceof ModelDataMap) {
			ModelDataMap modelDataMap = (ModelDataMap) data;
			if (modelDataMap.hasProperty(PIPE_PROPERTY))
				addQuads(quads, state, side, rand, modelDataMap, modelDataMap.getData(PIPE_PROPERTY));
		}
		return quads;
	}

	private void addQuads(List<BakedQuad> quads, BlockState state, Direction side, Random rand, IModelData data,
		PipeModelData pipeData) {
		for (Direction d : Iterate.directions)
			if (pipeData.getRim(d))
				quads.addAll(ModBlockPartials.HEAT_PIPE_RIMS.get(d)
					.getModel()
					.getQuads(state, side, rand, data));
		if (pipeData.isEncased())
			quads.addAll(ModBlockPartials.HEAT_PIPE_CASING.getModel()
				.getQuads(state, side, rand, data));
	}

	private class PipeModelData {
		boolean[] rims;
		boolean encased;

		public PipeModelData() {
			rims = new boolean[6];
			Arrays.fill(rims, false);
		}

		public void putRim(Direction face, boolean rim) {
			rims[face.getIndex()] = rim;
		}

		public void setEncased(boolean encased) {
			this.encased = encased;
		}

		public boolean getRim(Direction face) {
			return rims[face.getIndex()];
		}

		public boolean isEncased() {
			return encased;
		}
	}

}
