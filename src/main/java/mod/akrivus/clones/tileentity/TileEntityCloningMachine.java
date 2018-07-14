package mod.akrivus.clones.tileentity;

import mod.akrivus.clones.block.BlockCloneReturnPad;
import mod.akrivus.clones.entity.EntityClone;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public class TileEntityCloningMachine extends TileEntity implements ITickable {
	public int lastCloneTime;
	public void update() {
		if (this.isValidSetup(this.pos) && this.world.isBlockPowered(this.pos)) {
			BlockPos top = this.pos.up();
			for (EntityPlayer player : this.world.playerEntities) {
				if (player.getPosition().getDistance(top.getX(), top.getY(), top.getZ()) < 0.5D && player.isSneaking()) {
					if (this.lastCloneTime > 20) {
						for (int x = -16; x < 16; ++x) {
							for (int y = -16; y < 16; ++y) {
								for (int z = -16; z < 16; ++z) {
									BlockPos add = this.pos.add(x, y, z);
									if (this.world.getBlockState(add).getBlock() instanceof BlockCloneReturnPad) {
										if (this.isValidSetup(add)) {
											EntityClone chrone = new EntityClone(this.world);
											chrone.setCustomNameTag(player.getName());
											chrone.setLocationAndAngles(x, y, z, player.rotationYaw, player.rotationPitch);
											chrone.setFromThePast(true);
											this.world.spawnEntity(chrone);
											this.lastCloneTime = 0;
										}
									}
								}
							}
						}
						player.setSneaking(false);
					}
				}
			}
		}
		++this.lastCloneTime;
	}
	public boolean isValidSetup(BlockPos pos) {
		int validity = 0;
		for (int y = 1; y <= 2; ++y) {
			for (int x = -1; x <= 1; ++x) {
				BlockPos add = pos.add(x, y, 0);
				switch (x) {
				default:
					if (this.world.getBlockState(add).isTranslucent() && this.world.isSideSolid(add, EnumFacing.UP)) {
						++validity;
					}
				case 0:
					if (this.world.isAirBlock(add) || this.world.isSideSolid(add, EnumFacing.UP)) {
						++validity;
					}
				}
			}
			for (int z = -1; z <= 1; ++z) {
				BlockPos add = pos.add(0, y, z);
				switch (z) {
				default:
					if (this.world.getBlockState(add).isTranslucent() && this.world.isSideSolid(add, EnumFacing.UP)) {
						++validity;
					}
				case 0:
					if (this.world.isAirBlock(add) || this.world.isSideSolid(add, EnumFacing.UP)) {
						++validity;
					}
				}
			}
		}
		return this.world.isBlockPowered(pos) && validity > 9;
	}
}
