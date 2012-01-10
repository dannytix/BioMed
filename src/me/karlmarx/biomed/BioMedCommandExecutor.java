package me.karlmarx.biomed;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;

public class BioMedCommandExecutor implements CommandExecutor {
	private BioMedPlugin plugin;
	
	
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
			CraftWorld world;
			
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
				world = (CraftWorld) player.getWorld();
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
			CraftWorld world;
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
				world = (CraftWorld) player.getWorld();
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
				plugin.addRegionToWorld(world, x, z, lx, lz, biome);
				sender.sendMessage("Set your chunk's biome to "
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
		else if("set-selection".equals(args[0])){
			if(!(sender.hasPermission("biomed.set") || (plugin.allowOp && sender.isOp()))){
				sender.sendMessage("You do not have permission to run this command.");
				return true;
			}
			
			if(player == null){
				sender.sendMessage("This command only available to players.");
				return true;
			}
			if(args.length < 2)
				return false;

			StringBuilder sb = new StringBuilder(args[1].toUpperCase());
			for (int i = 2; i < args.length; i++) {
				sb.append('_').append(args[i].toUpperCase());
			}

			try {
				WorldEditPlugin we = getWorldEdit();
				if (we == null) {
					sender.sendMessage("This server does not have the WorldEdit plugin installed.");
					sender.sendMessage("Use \"" + label + " set\" instead.");
					return true;
				}
				Region region = we.getSelection(player).getRegionSelector().getRegion();
				Biome biome = Biome.valueOf(sb.toString());
				int x = region.getMinimumPoint().getBlockX();
				int z = region.getMinimumPoint().getBlockZ();
				int lx = region.getMaximumPoint().getBlockX() - x + 1;
				int lz = region.getMaximumPoint().getBlockZ() - z + 1;
				plugin.addRegionToWorld((CraftWorld)player.getWorld(), x, z, lx, lz, biome);
				sender.sendMessage("Set your selection's biome to "
						+ sb.charAt(0)
						+ sb.substring(1).toLowerCase().replace('_', ' '));
			} catch (IllegalArgumentException ex) {
				sender.sendMessage("Unknown biome.");
			} catch (Exception ex) {
				sender.sendMessage("You have not defined a selection in WorldEdit.");
				ex.printStackTrace();
			}
			return true;
		}
		else if("clear".equals(args[0])){
			if(!(sender.hasPermission("biomed.clear") || (plugin.allowOp && sender.isOp()))){
				sender.sendMessage("You do not have permission to run this command.");
				return true;
			}
			
			int x, z, lx, lz;
			CraftWorld world;
			
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
				world = (CraftWorld) player.getWorld();
				x = player.getLocation().getBlockX();
				z = player.getLocation().getBlockZ();
				lx = 1;
				lz = 1;
			}

			try {
				int cleared = plugin.clearRegionFromWorld(world, x, z, lx, lz);
				sender.sendMessage("Cleared " + cleared + " regions!");
			} catch (Exception ex) {
				sender.sendMessage("An unknown error has occurred.\n"
						+ "Check your server log for more details.");
				ex.printStackTrace();
			}
			return true;
		}
		else if("clear-selection".equals(args[0])){
			if(!(sender.hasPermission("biomed.clear") || (plugin.allowOp && sender.isOp()))){
				sender.sendMessage("You do not have permission to run this command.");
				return true;
			}
			
			if(player == null){
				sender.sendMessage("This command only available to players.");
				return true;
			}

			try {
				WorldEditPlugin we = getWorldEdit();
				if (we == null) {
					sender.sendMessage("This server does not have the WorldEdit plugin installed.\n"
							+ "Use \"" + label + " clear\" instead.");
					return false;
				}
				Region region = we.getSelection(player).getRegionSelector().getRegion();
				int x = region.getMinimumPoint().getBlockX();
				int z = region.getMinimumPoint().getBlockZ();
				int lx = region.getMaximumPoint().getBlockX() - x + 1;
				int lz = region.getMaximumPoint().getBlockZ() - z + 1;
				int cleared = plugin.clearRegionFromWorld((CraftWorld)player.getWorld(), x, z, lx, lz);
				sender.sendMessage("Cleared " + cleared + " regions!");
			} catch (Exception ex) {
				sender.sendMessage("You have not defined a selection in WorldEdit.");
				//ex.printStackTrace();
			}
			return true;
		}
		else if("get-global".equals(args[0])){
			if(!(sender.hasPermission("biomed.get") || (plugin.allowOp && sender.isOp()))){
				sender.sendMessage("You do not have permission to run this command.");
				return true;
			}
			
			CraftWorld world;
			
			if(player == null || args.length > 1){
				if(args.length == 1)
					return false;
				world = getWorldByName(args[1]);
				if(world == null){
					sender.sendMessage("Unrecognized world: " + args[1]);
					return true;
				}
			}
			else
				world = (CraftWorld) player.getWorld();
			
			String globalName = plugin.getGlobal(world);
			sender.sendMessage("The global biome is " + globalName);
			
			return true;
		}
		else if("set-global".equals(args[0])){
			if(!(sender.hasPermission("biomed.setglobal") || (plugin.allowOp && sender.isOp()))){
				sender.sendMessage("You do not have permission to run this command.");
				return true;
			}
			
			CraftWorld world;
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
				world = (CraftWorld) player.getWorld();
			
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
			
			plugin.setGlobal(world, biome);
			sender.sendMessage("Changed the global biome to "
					+ sb.charAt(0)
					+ sb.substring(1).toLowerCase().replace('_', ' '));
			
			return true;
		}
		
		return false;
	}
	
	private CraftWorld getWorldByName(String name){
		for(World world : plugin.getServer().getWorlds()){
			if(world.getName().equalsIgnoreCase(name))
				return (CraftWorld) world;
		}
		return null;
	}
	
	private WorldEditPlugin getWorldEdit() {
		Plugin worldEdit = plugin.getServer().getPluginManager().getPlugin("WorldEdit");

		if (worldEdit != null && worldEdit instanceof WorldEditPlugin)
			return (WorldEditPlugin) worldEdit;
		else
			return null;
	}
}
