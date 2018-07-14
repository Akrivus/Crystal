package mod.akrivus.clones.block;

import mod.akrivus.clones.tileentity.TileEntityChroningMachine;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.World;

public class BlockChroningMachine extends BlockContainer {
	public BlockChroningMachine() {
		super(Material.ROCK, MapColor.DIAMOND);
		this.setUnlocalizedName("chroning_machine");
	}
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityChroningMachine();
	}
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
}
