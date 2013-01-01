package me.karlmarx.biomed;

import java.util.Random;

import net.minecraft.server.v1_4_6.BiomeBase;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_4_6.block.CraftBlock;
import org.bukkit.generator.BlockPopulator;

public class GlobalBlockPopulator extends BlockPopulator {
	private byte bid;
	
	public GlobalBlockPopulator(){
		bid = -1;
	}

	@Override
	public void populate(World world, Random random, Chunk chunk){
		if(bid == -1)
			return;
		
		BioMedUtils.setBiomes(chunk, bid);
	}
	
	public void setBiome(Biome biome){
		if(biome == null)
			bid = -1;
		else
			bid = (byte)CraftBlock.biomeToBiomeBase(biome).id;
	}
	
	public Biome getBiome(){
		if(bid == -1)
			return null;
		else
			return CraftBlock.biomeBaseToBiome(BiomeBase.biomes[bid]);
	}

}
