package mod.akrivus.clones.skills.pack;

import java.util.ArrayList;
import java.util.Arrays;

import mod.akrivus.clones.entity.EntityClone;
import mod.akrivus.clones.skills.SkillBase;
import mod.akrivus.clones.util.LinguisticsHelper;
import net.minecraft.entity.player.EntityPlayer;

public class Stop extends SkillBase {
	public Stop() {
		this.can(RunWith.EVERYTHING);
		this.priority(Priority.CORE);
		this.task(false);
	}
	@Override
	public boolean speak(EntityClone clone, EntityPlayer player, String message) {
		ArrayList<String> WORDS = new ArrayList<String>(Arrays.asList(new String[] { "halt", "stop", "rest", "quit", "freeze" }));
		String[] tokens = LinguisticsHelper.getTokens(message);
		for (String token : tokens) {
			if (WORDS.contains(token)) {
				for (SkillBase skill : clone.skills.values()) {
					if (skill.canBeStopped) {
						skill.isAllowedToRun = false;
					}
				}
				return true;
			}
		}
		return false;
	}
	@Override
	public boolean triggered(EntityClone clone) {
		this.readyForRemoval = true;
		return false;
	}
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
