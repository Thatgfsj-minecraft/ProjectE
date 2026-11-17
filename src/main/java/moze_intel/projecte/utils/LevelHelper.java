package moze_intel.projecte.utils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public final class LevelHelper {

	private LevelHelper() {
	}

	public static void setWeather(@NotNull ServerLevel level, int durationTicks, boolean raining, boolean thundering) {
		//1.21.11: weather is set on the server level itself
		level.setWeatherParameters(durationTicks, durationTicks, raining, thundering);
	}

	public static long getDefaultClockTime(@NotNull ServerLevel level) {
		//1.21.11 has no per-dimension world clock; use the level day time
		return level.getDayTime();
	}

	public static void setDefaultClockTime(@NotNull ServerLevel level, long totalTicks) {
		level.setDayTime(totalTicks);
	}

	public static void addDefaultClockTicks(@NotNull ServerLevel level, long ticks) {
		setDefaultClockTime(level, Math.addExact(getDefaultClockTime(level), ticks));
	}

	@NotNull
	public static LightningBolt createLightning(@NotNull ServerLevel level, @NotNull Vec3 pos) {
		LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
		if (lightning == null) {
			throw new IllegalStateException("Failed to create lightning bolt");
		}
		lightning.setPos(pos.x, pos.y, pos.z);
		return lightning;
	}
}
