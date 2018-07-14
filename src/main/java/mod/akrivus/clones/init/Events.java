package mod.akrivus.clones.init;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import mod.akrivus.clones.entity.EntityClone;
import mod.akrivus.clones.util.LinguisticsHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Events {
	@SubscribeEvent
	public void onServerChat(ServerChatEvent e) {
		System.out.println(ArrayUtils.toString(LinguisticsHelper.parseSentences(e.getMessage())));
		EntityPlayer player = e.getPlayer();
		List<EntityClone> list = player.world.<EntityClone>getEntitiesWithinAABB(EntityClone.class, e.getPlayer().getEntityBoundingBox().expand(48.0D, 16.0D, 48.0D));
		for (EntityClone clone : list) {
    		boolean result = clone.spokenTo(player, e.getMessage());
    		if (result && !e.isCanceled()) {
    			e.setCanceled(true);
    		}
        }
	}
}
