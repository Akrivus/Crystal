package mod.akrivus.clones.skills.pack;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

public class HarvestCarrots extends Harvest {
	public HarvestCarrots() {
		super();
		this.TRIGGER_NOUNS = new ArrayList<String>(Arrays.asList(new String[] {
			"carrot",
			"carrots"
		}));
	}
	@Override
	public boolean isCorrectFarmBlock(IBlockState state) {
		return state.getBlock() == Blocks.FARMLAND;
	}
	@Override
	public boolean isCorrectPlant(IBlockState state) {
		return state.getBlock() == Blocks.CARROTS && state.getValue(BlockCrops.AGE) >= 7;
	}
	@Override
	public Block getPlant() {
		return Blocks.CARROTS;
	}
	@Override
	public Item getSeed() {
		return Items.CARROT;
	}
}
