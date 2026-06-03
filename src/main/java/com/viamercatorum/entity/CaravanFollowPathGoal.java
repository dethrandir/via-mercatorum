package com.viamercatorum.entity;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.world.Caravan;
import com.viamercatorum.core.world.RouteGraph;
import com.viamercatorum.core.world.TradeRoute;
import com.viamercatorum.core.world.VillageNode;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

public class CaravanFollowPathGoal extends Goal {
    private final PathfinderMob mob;
    private final double speed;

    public CaravanFollowPathGoal(PathfinderMob mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!(mob instanceof CaravanEntity caravan)) return false;
        RouteGraph graph = ViaMercatorum.getInstance().getRouteGraph();
        Caravan sekCaravan = graph.getCaravan(caravan.getCaravanId());
        TradeRoute route = graph.getRoute(caravan.getRouteId());
        return sekCaravan != null && route != null && !sekCaravan.hasArrived();
    }

    @Override
    public void tick() {
        if (!(mob instanceof CaravanEntity caravan)) return;
        RouteGraph graph = ViaMercatorum.getInstance().getRouteGraph();
        Caravan sekCaravan = graph.getCaravan(caravan.getCaravanId());
        TradeRoute route = graph.getRoute(caravan.getRouteId());
        if (sekCaravan == null || route == null) return;

        VillageNode origin = route.getFrom();
        VillageNode dest = route.getTo();
        double progress = sekCaravan.getProgress();

        double targetX = origin.getChunkX() * 16 + 8 + (dest.getChunkX() - origin.getChunkX()) * 16 * progress;
        double targetZ = origin.getChunkZ() * 16 + 8 + (dest.getChunkZ() - origin.getChunkZ()) * 16 * progress;

        double dx = targetX - mob.getX();
        double dz = targetZ - mob.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist > 2.0) {
            mob.getNavigation().moveTo(targetX, mob.getY(), targetZ, speed);
        }
    }
}
