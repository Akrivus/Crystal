package mod.akrivus.clones.skills.pack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mod.akrivus.clones.entity.EntityClone;
import mod.akrivus.clones.skills.Speak;
import mod.akrivus.clones.util.LinguisticsHelper;
import net.minecraft.entity.player.EntityPlayer;

public class KillOtherPlayers extends Speak {
	private EntityPlayer otherPlayer = null;
	private int lastHitTime = 0;
	public KillOtherPlayers() {
		this.TRIGGER_VERBS = new ArrayList<String>(Arrays.asList(new String[] {
			"execute",
			"kill",
			"terminate",
			"destroy",
			"slay",
			"murder",
			"hunt",
			"assassinate",
			"liquidate",
			"eliminate"
		}));
		this.TRIGGER_NOUNS = new ArrayList<String>();
		this.canBeStopped = true;
		this.killsOnEnd = true;
		this.can(RunWith.TARGETTING);
		this.task(true);
	}
	@Override
	public boolean proceed(EntityClone clone) {
		return this.otherPlayer != null && !this.otherPlayer.isDead;
	}
	@Override
	public void init(EntityClone clone) {
		List<EntityPlayer> players = clone.world.<EntityPlayer>getEntitiesWithinAABB(EntityPlayer.class, clone.getEntityBoundingBox().expand(16.0D, 8.0D, 16.0D), null);
		if (!players.isEmpty()) {
			double minDistance = Double.MAX_VALUE;
			for (EntityPlayer player : players) {
				double distance = clone.getDistanceSqToEntity(player);
				if (clone.getDistanceSqToEntity(player) < minDistance) {
					if (LinguisticsHelper.getDistance(player.getName(), this.selectedNoun) < 3) {
						this.otherPlayer = player;
						minDistance = distance;
					}
				}
			}
		}
		if (this.otherPlayer != null) {
			clone.setAttackTarget(this.otherPlayer);
		}
	}
	@Override
	public void run(EntityClone clone) {
		if (this.otherPlayer != null) {
			clone.lookAt(this.otherPlayer);
			if (clone.getDistanceSqToEntity(this.otherPlayer) < clone.getAttackRange()) {
				if (this.lastHitTime > clone.getAttackSpeed()) {
					clone.attackEntityAsMob(this.otherPlayer);
					this.lastHitTime = 0;
				}
				++this.lastHitTime;
			}
			else {
				clone.tryToMoveTo(this.otherPlayer.getPosition());
			}
		}
	}
	@Override
	public void reset(EntityClone clone) {
		this.otherPlayer = null;
	}
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.otherPlayer;
	}
}
