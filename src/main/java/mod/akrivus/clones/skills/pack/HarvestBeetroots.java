package mod.akrivus.clones.skills.pack;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBeetroot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

public class HarvestBeetroots extends Harvest {
	public HarvestBeetroots() {
		super();
		this.TRIGGER_NOUNS = new ArrayList<String>(Arrays.asList(new String[] {
			"beet",
			"beets",
			"beetroot",
			"beetroots"
		}));
	}
	@Override
	public boolean isCorrectFarmBlock(IBlockState state) {
		return state.getBlock() == Blocks.FARMLAND;
	}
	@Override
	public boolean isCorrectPlant(IBlockState state) {
		return state.getBlock() == Blocks.BEETROOTS && state.getValue(BlockBeetroot.BEETROOT_AGE) >= 3;
	}
	@Override
	public Block getPlant() {
		return Blocks.BEETROOTS;
	}
	@Override
	public Item getSeed() {
		return Items.BEETROOT_SEEDS;
	}
}
