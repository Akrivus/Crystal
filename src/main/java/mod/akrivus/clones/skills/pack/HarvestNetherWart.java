package mod.akrivus.clones.skills.pack;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

public class HarvestNetherWart extends Harvest {
	public HarvestNetherWart() {
		super();
		this.TRIGGER_NOUNS = new ArrayList<String>(Arrays.asList(new String[] {
			"wart"
		}));
	}
	@Override
	public boolean isCorrectFarmBlock(IBlockState state) {
		return state.getBlock() == Blocks.SOUL_SAND;
	}
	@Override
	public boolean isCorrectPlant(IBlockState state) {
		return state.getBlock() == Blocks.NETHER_WART && state.getValue(BlockNetherWart.AGE) >= 3;
	}
	@Override
	public Block getPlant() {
		return Blocks.NETHER_WART;
	}
	@Override
	public Item getSeed() {
		return Items.NETHER_WART;
	}
}
