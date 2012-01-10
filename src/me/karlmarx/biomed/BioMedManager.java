package me.karlmarx.biomed;

import net.minecraft.server.BiomeBase;

public interface BioMedManager {
	public void insertBiomeRegion(int x, int z, int lx, int lz, BiomeBase biome);
	
	public void insertBiomeChunk(BiomeBase[] chunk, int x, int z);
	
	public BiomeBase[] getBiomeChunk(int x, int z);
	
	public void clearBiomeRegion(int x, int z, int lx, int lz);
	
	public BiomeBase globalBiome();
	
	public void setGlobalBiome(BiomeBase biome);
}
