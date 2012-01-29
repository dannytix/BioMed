package me.karlmarx.biomed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.minecraft.server.BiomeBase;
import net.minecraft.server.WorldChunkManager;
import net.minecraft.server.WorldChunkManagerHell;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldListener;
import org.bukkit.plugin.java.JavaPlugin;

public class BioMedPlugin extends JavaPlugin{
	private static final Logger log = Logger.getLogger("Minecraft");
	public boolean allowGlobalBiomes;
	public boolean allowOp;

	@Override
	public void onDisable(){
		saveConfig();
		for (World world : getServer().getWorlds())
			restoreWorld((CraftWorld)world);
	}

	public void onEnable() {
		reloadConfig();
		getConfig().options().copyDefaults(true);
		getConfig().options().copyHeader(true);
		
		allowGlobalBiomes = getConfig().getBoolean("options.allowglobal", true);
		allowOp = getConfig().getBoolean("options.allowop", true);
		
		getCommand("biome").setExecutor(new BioMedCommandExecutor(this));

		for (World world : getServer().getWorlds())
			initWorld((CraftWorld)world);
		
		getServer().getPluginManager().registerEvent(Event.Type.WORLD_INIT, new WorldListener() {
			public void onWorldInit(WorldInitEvent event) {
				initWorld((CraftWorld)event.getWorld());
			}
		}, Event.Priority.Normal, this);
		
		System.out.println("[BioMed] biome data loaded");
	}
	
	public void initWorld(CraftWorld world){
		if(getConfig().getConfigurationSection("worlds." + world.getName()) == null)
			getConfig().createSection("worlds." + world.getName());
		ConfigurationSection worldConfig = getConfig().getConfigurationSection("worlds." + world.getName());
		worldConfig.addDefault("global", "none");
		BioMedManager manager;
		BiomeBase globalBiome;
		if(allowGlobalBiomes == true){
			try{
				String gName = worldConfig.getString("global", null);
				if(gName.equals("none"))
					globalBiome = null;
				else
					globalBiome = biomeToBiomeBase(Biome.valueOf(gName));
			}
			catch(Exception e){//unsupported global biome in config
				log.warning("[BioMed] Unsupported global biome for world \"" + world.getName() + "\"");
				globalBiome = null;
			}
		}
		else
			globalBiome = null;
		switch(world.getEnvironment()){
		case NORMAL:
			manager = new BioMedChunkManager(world.getHandle(), globalBiome);
			break;
		case NETHER:
			manager = new BioMedChunkManagerHell(BiomeBase.HELL, 1.0F, 0.0F, globalBiome);
			break;
		case THE_END:
			manager = new BioMedChunkManagerHell(BiomeBase.SKY, 0.5F, 0.0F, globalBiome);
			break;
		default:
			log.warning("[BioMed] Unrecognized world environment: " + world.getEnvironment());
			return;
		}
		
		world.getHandle().worldProvider.c = (WorldChunkManager)manager;
		List<Map<String, Object>> worldRegions = null;
		try{
			worldRegions = worldConfig.getMapList("regions");
		}
		catch(Exception e){
			//No regions to load
			return;
		}
		if(worldRegions == null){
			return;
		}
		for(Map<String, Object> worldRegion : worldRegions){
			int x, z, lx, lz;
			BiomeBase biome;
			try{
				x = (Integer)worldRegion.get("x");
				z = (Integer)worldRegion.get("z");
				lx = (Integer)worldRegion.get("lx");
				lz = (Integer)worldRegion.get("lz");
				biome = biomeToBiomeBase(Biome.valueOf((String)worldRegion.get("biome")));
				manager.insertBiomeRegion(x, z, lx, lz, biome);
			}
			catch(Exception e){
				log.warning("[BioMed] config for world \"" + world.getName()
						+ "\" contains an invalid region. Ignoring it.");
			}
		}
	}
	
	public void restoreWorld(CraftWorld world){
		switch(world.getEnvironment()){
		case NORMAL:
			world.getHandle().worldProvider.c = ((BioMedChunkManager)world.getHandle().worldProvider.c).inner;
			break;
		case NETHER:
			world.getHandle().worldProvider.c = new WorldChunkManagerHell(BiomeBase.HELL, 1.0F, 0.0F);
			break;
		case THE_END:
			world.getHandle().worldProvider.c = new WorldChunkManagerHell(BiomeBase.SKY, 0.5F, 0.0F);
			break;
		default:
			log.warning("[BioMed] Unrecognized world environment: " + world.getEnvironment());
			return;
		}
	}
	
	public boolean addRegionToWorld(CraftWorld world, int x, int z, int lx, int lz, Biome biome) throws IllegalArgumentException{
		BioMedManager manager = null;
		try{
			manager = (BioMedManager)world.getHandle().worldProvider.c;
		}
		catch(Exception e){
			log.warning("[BioMed] Trying to add region to unsupported world: " + world.getName());
			return false;
		}
		
		BiomeBase biomeBase = biomeToBiomeBase(biome);
		if(biomeBase == null)
			throw new IllegalArgumentException("Unsupported Biome");
		manager.insertBiomeRegion(x, z, lx, lz, biomeToBiomeBase(biome));
		
		List<Map<String, Object>> regionList;
		String path = "worlds." + world.getName() + ".regions";
		try{
			regionList = getConfig().getMapList(path);
		}
		catch(Exception e){
			regionList = null;
		}
		if(regionList == null)
			regionList = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> configRegion = new HashMap<String, Object>();
		configRegion.put("x", x);
		configRegion.put("z", z);
		configRegion.put("lx", lx);
		configRegion.put("lz", lz);
		configRegion.put("biome", biome.toString());
		regionList.add(configRegion);
		
		getConfig().set(path, regionList);
		saveConfig();
		
		return true;
	}
	
	public int clearRegionFromWorld(CraftWorld world, int x, int z, int lx, int lz){
		BioMedManager manager = null;
		int cleared = 0;
		try{
			manager = (BioMedManager)world.getHandle().worldProvider.c;
		}
		catch(Exception e){
			log.warning("[BioMed] Trying to remove region from unsupported world: " + world.getName());
			return 0;
		}
    	
		List<Map<String, Object>> regionList = getConfig().getMapList("worlds." + world.getName() + ".regions");
		if(regionList != null){
			int x1 = x & (~(int)0xF);
			int z1 = z & (~(int)0xF);
			int x2 = (x + lx - 1) | 0xF;
			int z2 = (z + lz - 1) | 0xF;
			List<BiomeRegion> refreshList = new ArrayList<BiomeRegion>();
			List<Map<String, Object>> clearList = new ArrayList<Map<String, Object>>();
			
			for(Map<String, Object> region : regionList){
				int left, right, top, bottom, height, width;
				Biome biome;
				try{
					left = (Integer)region.get("x");
					top = (Integer)region.get("z");
					height = (Integer)region.get("lz");
					width = (Integer)region.get("lx");
					right = left + width - 1;
					bottom = top + height - 1;
					biome = Biome.valueOf((String)region.get("biome"));
				}
				catch(Exception e){
					log.warning("[BioMed] config for world \"" + world.getName()
							+ "\" contains an invalid region. Ignoring it.");
					continue;
				}
				if(((left >= x1 && left <= x2) || (right >= x1 && right <= x2))
						&& ((top >= z1 && top <= z2) || (bottom >= z1 && bottom <= z2))){
					if(((x >= left && x <= right) || (x + lx - 1 >= left && x + lx - 1 <= right))
							&& ((z >= top && z <= bottom) || (z + lz - 1 >= top && z + lz - 1 <= bottom))){
						manager.clearBiomeRegion(left, top, width, height);
						clearList.add(region);
						cleared++;
					}
					else{
						refreshList.add(new BiomeRegion(left, top, width, height, biome));
					}
				}
			}
			for(BiomeRegion region : refreshList){
				manager.insertBiomeRegion(region.x, region.z, region.lx, region.lz
						, biomeToBiomeBase(region.biome));
			}
			for(Map<String, Object> region : clearList){
				regionList.remove(region);
			}
			getConfig().set("worlds." + world.getName() + ".regions", regionList);
			saveConfig();
    	}
		
		return cleared;
	}
	
	public String getGlobal(CraftWorld world){
		if(allowGlobalBiomes == false)
			return "disabled";
		
		String global;
		try{
			BioMedManager manager = (BioMedManager) world.getHandle().worldProvider.c;
			BiomeBase biome = manager.globalBiome();
			global = biome.w;
		}
		catch(Exception e){
			global = "not set";
		}
		
		return global;
	}
	
	public void setGlobal(CraftWorld world, Biome biome){
		if(allowGlobalBiomes == false)
			return;
		
		BioMedManager manager;
		try{
			manager = (BioMedManager) world.getHandle().worldProvider.c;
		}
		catch(Exception e){
			log.warning("[BioMed] Trying to change global biome in unsupported world: " + world.getName());
			return;
		}
		manager.setGlobalBiome(biomeToBiomeBase(biome));
		String path = "worlds." + world.getName() + ".global";
		String bName;
		if(biome == null)
			bName = "none";
		else
			bName = biome.toString();
		getConfig().set(path, bName);
	}
	
	public static BiomeBase biomeToBiomeBase(Biome biome){
		if(biome == null)
			return null;
		
		switch (biome) {
		case DESERT:
			return BiomeBase.DESERT;
		case FOREST:
			return BiomeBase.FOREST;
		case PLAINS:
			return BiomeBase.PLAINS;
		case SWAMPLAND:
			return BiomeBase.SWAMPLAND;
		case TAIGA:
			return BiomeBase.TAIGA;
		case HELL:
			return BiomeBase.HELL;
		case SKY:
			return BiomeBase.SKY;
		case OCEAN:
			return BiomeBase.OCEAN;
		case EXTREME_HILLS:
			return BiomeBase.EXTREME_HILLS;
		case RIVER:
			return BiomeBase.RIVER;
		case FROZEN_OCEAN:
			return BiomeBase.FROZEN_OCEAN;
		case FROZEN_RIVER:
			return BiomeBase.FROZEN_RIVER;
		case ICE_PLAINS:
			return BiomeBase.ICE_PLAINS;
		case ICE_MOUNTAINS:
			return BiomeBase.ICE_MOUNTAINS;
		case MUSHROOM_ISLAND:
			return BiomeBase.MUSHROOM_ISLAND;
		case MUSHROOM_SHORE:
			return BiomeBase.MUSHROOM_SHORE;
		case BEACH:
			return BiomeBase.BEACH;
		case DESERT_HILLS:
			return BiomeBase.DESERT_HILLS;
		case FOREST_HILLS:
			return BiomeBase.FOREST_HILLS;
		case TAIGA_HILLS:
			return BiomeBase.TAIGA_HILLS;
		case SMALL_MOUNTAINS:
			return BiomeBase.SMALL_MOUNTAINS;
		default:
			return null;
		}
	}
}
