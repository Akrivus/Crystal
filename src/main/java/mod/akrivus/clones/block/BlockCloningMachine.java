package mod.akrivus.clones.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.World;

public class BlockCloningMachine extends BlockContainer {
	public BlockCloningMachine() {
		super(Material.ROCK, MapColor.DIAMOND);
		this.setUnlocalizedName("cloning_machine");
	}
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return null;
	}
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
}
