package combatlogx.expansion.compatibility.region.protectionstones.handler;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.github.sirblobman.combatlogx.api.expansion.region.RegionHandler;
import com.github.sirblobman.combatlogx.api.object.TagType;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import combatlogx.expansion.compatibility.region.protectionstones.ProtectionStonesExpansion;
import dev.espi.protectionstones.PSRegion;

public final class ProtectionStonesRegionHandler extends RegionHandler {
    public ProtectionStonesRegionHandler(ProtectionStonesExpansion expansion) {
        super(expansion);
    }

    @Override
    public String getEntryDeniedMessagePath(TagType tagType) {
        return "expansion.protectionstones-compatibility-no-entry";
    }

    @Override
    public boolean isSafeZone(Player player, Location location, TagType tagType) {
        PSRegion region = PSRegion.fromLocation(location);
        if(region == null) return false;

        ProtectedRegion wgRegion = region.getWGRegion();
        if(wgRegion == null) return false;

        State pvpState = wgRegion.getFlag(Flags.PVP);
        return (pvpState == State.DENY);
    }
}
