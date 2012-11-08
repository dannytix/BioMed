package me.karlmarx.biomed;

import java.lang.ref.WeakReference;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;

final class WETools {
	private static WeakReference<WorldEditPlugin> weRef;
	
	private WETools() {}
	
	private static WorldEditPlugin getWorldEdit(Server server) {
		if(weRef != null && weRef.get() != null)
			return weRef.get();
		
		Plugin we = server.getPluginManager().getPlugin("WorldEdit");
		if (we != null && we instanceof WorldEditPlugin) {
			WorldEditPlugin we2 = (WorldEditPlugin) we;
			weRef = new WeakReference<WorldEditPlugin>(we2);
			return we2;
		} else
			return null;
	}
	
	public static boolean isAvailable(Server server) {
		return getWorldEdit(server) != null;
	}
	
	public static Selection getSelection(Player player) {
		WorldEditPlugin we = getWorldEdit(player.getServer());
		if(we == null)
			return null;
		else {
			Selection selection = new Selection();
			try {
				Region region = we.getSelection(player).getRegionSelector().getRegion();
				selection.x = region.getMinimumPoint().getBlockX();
				selection.z = region.getMinimumPoint().getBlockZ();
				selection.lx = region.getWidth();
				selection.lz = region.getLength();
				return selection;
			} catch(IncompleteRegionException e) {}
			return selection;
		}
	}

}
