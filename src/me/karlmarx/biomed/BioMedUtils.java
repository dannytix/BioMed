package me.karlmarx.biomed;

import java.util.Arrays;
import java.util.List;

import net.minecraft.server.BiomeBase;
import net.minecraft.server.WorldServer;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;

public class BioMedUtils {
	
	public static void setBiomes(int x, int z, int lx, int lz, World world, Biome biome){
    	int x1 = x >> 4;
    	int z1 = z >> 4;
    	int x2 = (x + lx - 1) >> 4;
    	int z2 = (z + lz - 1) >> 4;
    	
    	byte bid = (byte) CraftBlock.biomeToBiomeBase(biome).id;
    	
    	for(int j = z1; j <= z2; j++){
    		for(int i = x1; i <= x2; i++){
    			byte[] chunk = ((CraftChunk)(world.getChunkAt(i, j))).getHandle().m();
    			int xl, xh, zl, zh;
    			boolean edge = false;
    			
    			if(j == z1){
    				zl = z & 0xF;
    				edge = true;
    			}
    			else
    				zl = 0;
    			
    			if(j == z2){
    				zh = (z + lz - 1) & 0xF;
    				edge = true;
    			}
    			else
    				zh = 15;
    			
    			if(i == x1){
    				xl = x & 0xF;
    				edge = true;
    			}
    			else
    				xl = 0;
    			
    			if(i == x2){
    				xh = (x + lx -1) & 0xF;
    				edge = true;
    			}
    			else
    				xh = 15;
    				
    			if(edge){
    				for(int l = zl; l <= zh; l++)
    					for(int k = xl; k <= xh; k++)
    						chunk[(l << 4) | k] = bid;
    			}
    			else
    				Arrays.fill(chunk, bid);
    			
    			world.refreshChunk(x1, z1);
    		}
    	}
	}
	
	public static void setBiomes(Chunk chunk, Biome biome){
		setBiomes(chunk, (byte)CraftBlock.biomeToBiomeBase(biome).id);
	}
	
	public static void setBiomes(Chunk chunk, byte biome){
		byte[] biomes = ((CraftChunk)chunk).getHandle().m();
		Arrays.fill(biomes, biome);
	}
	
	public static void clearBiomes(int x, int z, int lx, int lz, World world){
    	int x1 = x >> 4;
    	int z1 = z >> 4;
    	int x2 = (x + lx - 1) >> 4;
    	int z2 = (z + lz - 1) >> 4;

    	//BiomeBase[] biomes = getDefaultBiomeBases(x, z, lx, lz, world);
    	BiomeBase[] biomes = getDefaultBiomeBases(x1 << 4, z1 << 4, (x2 - x1 + 1) << 4, (z2 - z1 + 1) << 4, world);

    	for(int j = z1; j <= z2; j++){
    		for(int i = x1; i <= x2; i++){
    			byte[] chunk = ((CraftChunk)(world.getChunkAt(i, j))).getHandle().m();
    			int xl = 0;
    			int xh = 0;
    			int zl = 15;
    			int zh = 15;

    			if(j == z1)
    				zl = z & 0xF;

    			if(j == z2)
    				zh = (z + lz - 1) & 0xF;

    			if(i == x1)
    				xl = x & 0xF;

    			if(i == x2)
    				xh = (x + lx -1) & 0xF;

    			for(int l = zl; l <= zh; l++)
    				for(int k = xl; k <= xh; k++)
    					chunk[(l << 4) | k] = (byte)biomes[(l << 4) | k].id;
    			
    			world.refreshChunk(x1, z1);
    		}
    	}
	}
	
	public static Biome[] getDefaultBiomes(int x, int z, int lx, int lz, World world){
		BiomeBase[] src = getDefaultBiomeBases(x, z, lx, lz, world);
		Biome[] dst = new Biome[lx * lz];
		
		for(int i = 0; i < lx * lz; i++)
			dst[i] = CraftBlock.biomeBaseToBiome(src[i]);
		
		return dst;
	}
	
	public static Biome getDefaultBiome(int x, int z, World world){
		return getDefaultBiomes(x, z, 1, 1, world)[0];
	}
	
	public static void setAllLoadedBiomes(World world, Biome biome){
		WorldServer ws = ((CraftWorld)world).getHandle();
		@SuppressWarnings("rawtypes")
		List loaded = ws.chunkProviderServer.chunkList;
		byte id = (byte)CraftBlock.biomeToBiomeBase(biome).id;
		for(Object o : loaded){
			net.minecraft.server.Chunk chunk = (net.minecraft.server.Chunk) o;
			byte[] biomes = chunk.m();
			Arrays.fill(biomes, id);
			world.refreshChunk(chunk.x, chunk.z);
		}
	}
	
	public static void setGlobalBiome(World world, Biome biome){
		
	}
	
	public static void clearGlobalBiome(World world){
		
	}
	
	protected static BiomeBase[] getDefaultBiomeBases(int x, int z, int lx, int lz, World world){
		return ((CraftWorld)world).getHandle().worldProvider.c.getBiomes(null, x, z, lx, lz);
	}

}
