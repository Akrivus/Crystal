package mod.akrivus.clones.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class BlockCloneReturnPad extends Block {
	public BlockCloneReturnPad() {
		super(Material.ROCK, MapColor.DIAMOND);
		this.setUnlocalizedName("clone_return_pad");
	}
}
