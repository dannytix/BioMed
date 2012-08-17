package me.karlmarx.biomed;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BioMedPlugin extends JavaPlugin implements Listener{
	private static final Logger log = Logger.getLogger("Minecraft");
	public boolean allowOp;

	Map<String, GlobalBlockPopulator> globalBiomes;

	@Override
	public void onDisable(){
		for(Map.Entry<String, GlobalBlockPopulator> entry : globalBiomes.entrySet()){
			String biome = entry.getValue().getBiome().toString();
			String world = entry.getKey();
			getConfig().set("worlds." + world + ".global", biome);
		}
		globalBiomes = null;
		saveConfig();
	}

	@Override
	public void onEnable(){
		reloadConfig();
		getConfig().options().copyDefaults(true);
		getConfig().options().copyHeader(true);
		
		globalBiomes = new HashMap<String, GlobalBlockPopulator>();

		allowOp = getConfig().getBoolean("options.allowop", true);
		
		getCommand("biome").setExecutor(new BioMedCommandExecutor(this));

		for (World world : getServer().getWorlds())
			initWorld(world);
		
		getServer().getPluginManager().registerEvents(this, this);
		
		System.out.println("[BioMed] biome data loaded");
	}
	
	@EventHandler
	public void onWorldLoad(WorldLoadEvent event){
		initWorld(event.getWorld());
	}

	public void initWorld(World world){
		String name = world.getName();

		if(getConfig().getConfigurationSection("worlds." + name) == null)
			getConfig().createSection("worlds." + name);
		ConfigurationSection worldConfig = getConfig().getConfigurationSection("worlds." + name);
		worldConfig.addDefault("global", "none");

		Biome globalBiome;
		try{
			String gName = worldConfig.getString("global");
			if(gName.equals("none"))
				globalBiome = null;
			else
				globalBiome = Biome.valueOf(gName);
		}
		catch(Exception e){//unsupported global biome in config
			log.warning("[BioMed] Unrecognized global biome for world \"" + world.getName() + "\"");
			globalBiome = null;
		}
		if(globalBiome != null){
			GlobalBlockPopulator pop = new GlobalBlockPopulator();
			pop.setBiome(globalBiome);
			world.getPopulators().add(pop);
			globalBiomes.put(name, pop);
		}

		//load regions from config (backwards compatibility)
		List<Map<?, ?>> worldRegions = null;
		try{
			worldRegions = worldConfig.getMapList("regions");
			worldConfig.set("regions", null);
		}
		catch(Exception e){
			//No regions to load
			return;
		}
		if(worldRegions == null){
			return;
		}
		boolean found = false;
		for(Map<?, ?> worldRegion : worldRegions){
			int x, z, lx, lz;
			Biome biome;
			try{
				x = (Integer)worldRegion.get("x");
				z = (Integer)worldRegion.get("z");
				lx = (Integer)worldRegion.get("lx");
				lz = (Integer)worldRegion.get("lz");
				biome = Biome.valueOf((String)worldRegion.get("biome"));

				BioMedUtils.setBiomes(x, z, lx, lz, world, biome);
				found = true;
			}
			catch(Exception e){
				log.warning("[BioMed] config for world \"" + world.getName()
						+ "\" contains an invalid region. Ignoring it.");
			}
		}
		if(found){
			log.info("[BioMed] finished importing regions for world \"" + name + "\".");
			saveConfig();
		}
	}
	
}
