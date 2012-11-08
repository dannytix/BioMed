package me.karlmarx.biomed;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;

public class BioMedCommandExecutor implements CommandExecutor {
	private final BioMedPlugin plugin;
	
	
	public BioMedCommandExecutor(BioMedPlugin plugin){
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		Player player;
		
		if(args.length == 0)
			return false;
		
		if(sender instanceof Player)
			player = (Player)sender;
		else
			player = null;
		
		if("list".equals(args[0])){
			if(!(sender.hasPermission("biomed.list") || (plugin.allowOp && sender.isOp()))){
				sender.sendMessage("You do not have permission to run this command.");
				return true;
			}
			
			StringBuilder sb = new StringBuilder("Available biomes: ");
			boolean first = true;
			for (Biome biome : Biome.values()) {
				if (!first) {
					sb.append(", ");
				}
				first = false;
				String name = biome.name().toLowerCase().replace('_', ' ');
				sb.append(Character.toUpperCase(name.charAt(0))).append(
						name.substring(1));
			}
			sender.sendMessage(sb.toString());
			
			return true;
		}
		else if("get".equals(args[0])){
			if(!(sender.hasPermission("biomed.get") || (plugin.allowOp && sender.isOp()))){
				sender.sendMessage("You do not have permission to run this command.");
				return true;
			}
			
			int x, z;
			World world;
			
			if(player == null || args.length >= 4){
				try{
					world = getWorldByName(args[1]);
					world.getName();//throw exception if world == null
					x = Integer.parseInt(args[2]);
					z = Integer.parseInt(args[3]);
				}
				catch(Exception e){
					return false;
				}
			}
			else{
				world = player.getWorld();
				x = player.getLocation().getBlockX();
				z = player.getLocation().getBlockZ();
			}
			String biomeName = world.getBiome(x, z).toString();
			sender.sendMessage("The biome is "
					+ biomeName.charAt(0)
					+ biomeName.substring(1)
					.toLowerCase().replace('_', ' '));
			return true;
		}
		else if("set".equals(args[0])){
			if(!(sender.hasPermission("biomed.set") || (plugin.allowOp && sender.isOp()))){
				sender.sendMessage("You do not have permission to run this command.");
				return true;
			}
			
			int x, z, lx, lz;
			World world;
			StringBuilder sb;
			
			if(player == null || args.length >= 7){
				try{
					world = getWorldByName(args[1]);
					world.getName();//throw exception if world == null
					x = Integer.parseInt(args[2]);
					z = Integer.parseInt(args[3]);
					lx = Integer.parseInt(args[4]);
					lz = Integer.parseInt(args[5]);
					sb = new StringBuilder(args[6].toUpperCase());
					for (int i = 7; i < args.length; i++)
						sb.append('_').append(args[i].toUpperCase());
				}
				catch(Exception e){
					sender.sendMessage("Error processing arguments.");
					return false;
				}
			}
			else if(args.length < 2){
				sender.sendMessage("Error: you must specify a biome.\n"
						+ "Use \"" + label + " list\" to see options.");
				return false;
			}
			else{
				world = player.getWorld();
				x = player.getLocation().getBlockX() & ~(int)0xF;
				z = player.getLocation().getBlockZ() & ~(int)0xF;
				lx = 16;
				lz = 16;
				sb = new StringBuilder(args[1].toUpperCase());
				for (int i = 2; i < args.length; i++)
					sb.append('_').append(args[i].toUpperCase());
			}

			try {
				Biome biome = Biome.valueOf(sb.toString());
				BioMedUtils.setBiomes(x, z, lx, lz, world, biome);
				sender.sendMessage("Biome set to "
						+ sb.charAt(0)
						+ sb.substring(1).toLowerCase().replace('_', ' '));
			} catch (IllegalArgumentException ex) {
				sender.sendMessage("Unknown biome.");
				return false;
			} catch (Exception ex) {
				sender.sendMessage("An unknown error has occurred.\n"
						+ "Check your server log for more details.");
				ex.printStackTrace();
			}
			return true;
		}
		else if("set-selection".equals(args[0])) {
			if(!(sender.hasPermission("biomed.set") || (plugin.allowOp && sender.isOp())))
				sender.sendMessage("You do not have permission to run this command.");
			else if (!WETools.isAvailable(plugin.getServer())) {
				sender.sendMessage("This server does not have the WorldEdit plugin installed.");
				sender.sendMessage("Use \"" + label + " set\" instead.");
			} else if(player == null)
				sender.sendMessage("This command only available to players.");
			else if(args.length < 2)
				return false;
			else {
				StringBuilder sb = new StringBuilder(args[1].toUpperCase());
				for (int i = 2; i < args.length; i++) {
					sb.append('_').append(args[i].toUpperCase());
				}
				Biome biome;
				try {
					biome = Biome.valueOf(sb.toString());
				} catch(IllegalArgumentException e) {
					sender.sendMessage("Unknown/unsupported biome.");
					return true;
				}
				Selection selection = WETools.getSelection(player);
				if(selection.isEmpty()) {
					sender.sendMessage("You have not defined a WorldEdit region.");
					return true;
				}
				int x = selection.x;
				int z = selection.z;
				int lx = selection.lx;
				int lz = selection.lz;
				World world = player.getWorld();
				BioMedUtils.setBiomes(x, z, lx, lz, world, biome);
				sender.sendMessage("Set your selection's biome to "
						+ sb.charAt(0)
						+ sb.substring(1).toLowerCase().replace('_', ' '));
			}
			return true;
		}
		else if("clear".equals(args[0])){
			if(!(sender.hasPermission("biomed.clear") || (plugin.allowOp && sender.isOp()))){
				sender.sendMessage("You do not have permission to run this command.");
				return true;
			}
			
			int x, z, lx, lz;
			World world;
			
			if(player == null || args.length == 6){
				try{
					world = getWorldByName(args[1]);
					world.getName();//throw exception if world == null
					x = Integer.parseInt(args[2]);
					z = Integer.parseInt(args[3]);
					lx = Integer.parseInt(args[4]);
					lz = Integer.parseInt(args[5]);
				}
				catch(Exception e){
					sender.sendMessage("Error processing arguments.");
					return false;
				}
			}
			else{
				world = player.getWorld();
				x = player.getLocation().getBlockX() & ~(int)0xF;
				z = player.getLocation().getBlockZ() & ~(int)0xF;
				lx = 16;
				lz = 16;
			}

			try {
				BioMedUtils.clearBiomes(x, z, lx, lz, world);
				sender.sendMessage("Biome data restored.");
			} catch (Exception ex) {
				sender.sendMessage("An unknown error has occurred.\n"
						+ "Check your server log for more details.");
				ex.printStackTrace();
			}
			return true;
		}
		else if("clear-selection".equals(args[0])) {
			if(!(sender.hasPermission("biomed.clear") || (plugin.allowOp && sender.isOp())))
				sender.sendMessage("You do not have permission to run this command.");
			else if(player == null)
				sender.sendMessage("This command only available to players.");
			else if(!WETools.isAvailable(plugin.getServer())) {
				sender.sendMessage("This server does not have the WorldEdit plugin installed.");
				sender.sendMessage("Use \"" + label + " set\" instead.");
			} else {
				Selection slct = WETools.getSelection(player);
				if(slct.isEmpty())
					sender.sendMessage("You have not defined a WorldEdit region.");
				else {
					BioMedUtils.clearBiomes(slct.x, slct.z, slct.lx, slct.lz, player.getWorld());
					sender.sendMessage("Biome data restored.");
				}
			}
			return true;
		}
		else if("get-global".equals(args[0])){
			if(!(sender.hasPermission("biomed.get") || (plugin.allowOp && sender.isOp()))){
				sender.sendMessage("You do not have permission to run this command.");
				return true;
			}
			
			String world;
			
			if(player == null || args.length > 1){
				if(args.length == 1)
					return false;
				world = args[1];
			}
			else
				world = player.getWorld().getName();
			
			GlobalBlockPopulator pop = plugin.globalBiomes.get(world);
			Biome globalBiome = null;
			if(pop != null)
				globalBiome = pop.getBiome();
			String globalName;
			if(globalBiome == null)
				globalName = "not set.";
			else
				globalName = globalBiome.toString();
			sender.sendMessage("The global biome is " + globalName);
			
			return true;
		}
		else if("set-global".equals(args[0])){
			if(!(sender.hasPermission("biomed.setglobal") || (plugin.allowOp && sender.isOp()))){
				sender.sendMessage("You do not have permission to run this command.");
				return true;
			}
			
			World world;
			int bStart = 1;
			
			if(args.length < 2)
				return false;
			
			if(args[1].equals("-w")){
				if(args.length < 4)
					return false;
				bStart = 3;
				world = getWorldByName(args[2]);
				if(world == null){
					sender.sendMessage("Unrecognized world: " + args[2]);
					return true;
				}
			}
			else if(player == null){
				sender.sendMessage("Console users must specify a world with \"-w\"\n"
						+ "eg: " + label + " -w <world> <biome>");
				return true;
			}
			else
				world = player.getWorld();
			
			StringBuilder sb = new StringBuilder(args[bStart].toUpperCase());
			for (int i = bStart + 1; i < args.length; i++) {
				sb.append('_').append(args[i].toUpperCase());
			}
			
			Biome biome;
			if(sb.toString().equals("NONE"))
				biome = null;
			else{
				try{
					biome = Biome.valueOf(sb.toString());
				}
				catch(Exception e){
					sender.sendMessage("Unrecognized biome.");
					return true;
				}
			}
			
			GlobalBlockPopulator pop = plugin.globalBiomes.get(world.getName());
			if(pop == null){
				pop = new GlobalBlockPopulator();
				world.getPopulators().add(pop);
				plugin.globalBiomes.put(world.getName(), pop);
			}
			pop.setBiome(biome);
			sender.sendMessage("Changed the global biome to "
					+ sb.charAt(0)
					+ sb.substring(1).toLowerCase().replace('_', ' '));
			if(biome != null){
				sender.sendMessage("Updating all loaded chunks...");
				BioMedUtils.setAllLoadedBiomes(world, biome);
			}
			
			return true;
		}
		
		return false;
	}
	
	private World getWorldByName(String name){
		for(World world : plugin.getServer().getWorlds()){
			if(world.getName().equalsIgnoreCase(name))
				return world;
		}
		return null;
	}

}
