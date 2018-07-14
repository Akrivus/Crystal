package mod.akrivus.clones.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class SchematicLoader {
	public HashMap<BlockPos, IBlockState> load(String location, boolean internal) {
		HashMap<BlockPos, IBlockState> mapping = new HashMap<BlockPos, IBlockState>();
		try {
			InputStream input;
			if (internal) {
				input = this.getClass().getClassLoader().getResourceAsStream(location);
			}
			else {
				input = new FileInputStream(new File(location));
			}
		    NBTTagCompound nbt = CompressedStreamTools.read((DataInputStream) input);
		    input.close();
		    byte[] blocks = nbt.getByteArray("Blocks");
		    for (short i = 0; i < blocks.length; ++i) {
		    	for (short x = 0; x < nbt.getShort("Width"); ++x) {
		    		for (short y = 0; y < nbt.getShort("Height"); ++y) {
		    			for (short z = 0; z < nbt.getShort("Length"); ++z) {
				    		mapping.put(new BlockPos(x, y, z), Block.getStateById(blocks[i] & 0xFF));
				    	}
			    	}
		    	}
		    }
		}
		catch (Exception e) {
			return null;
		}
		return mapping;
	}
}
