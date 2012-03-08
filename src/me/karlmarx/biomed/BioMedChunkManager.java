package me.karlmarx.biomed;

import java.util.Random;

import net.minecraft.server.BiomeBase;
import net.minecraft.server.BiomeCache;
import net.minecraft.server.BiomeCacheBlock;
import net.minecraft.server.ChunkCoordIntPair;
import net.minecraft.server.ChunkPosition;
import net.minecraft.server.World;
import net.minecraft.server.WorldChunkManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BioMedChunkManager extends WorldChunkManager implements BioMedManager{

	public final WorldChunkManager inner;
	private Map<Long, BiomeBase[]> biomeData;
	private BiomeCache biomeCache;
	private BiomeBase globalBiome;
	
	public BioMedChunkManager(World world, BiomeBase biome){
		this(world);
		globalBiome = biome;
	}
	
    public BioMedChunkManager(World world){
    	this.inner = world.worldProvider.c;
    	biomeData = new HashMap<Long, BiomeBase[]>();
    	biomeCache = new BiomeCache(this);
    	globalBiome = null;
    }

    /*
     * get List of spawn biomes
     * status - done, may need revision
     */
    @SuppressWarnings("rawtypes")
	public List a() {
        return inner.a();
    }

    /*
     * get biome of a single block
     * status - done, no changes needed
     */
    public BiomeBase getBiome(ChunkCoordIntPair chunkcoordintpair){
        return this.getBiome(chunkcoordintpair.x << 4, chunkcoordintpair.z << 4);
    }

    /*
     * get biome of a single block
     * status - done
     */
    public BiomeBase getBiome(int x, int z){
    	BiomeBase[] biomes;
    	biomes = biomeData.get(longKey(x >> 4, z >> 4));
    	if(biomes != null && biomes[(z & 0xF) * 16 + (x & 0xF)] != null)
            return biomes[(z & 0xF) * 16 + (x & 0xF)];
    	if(globalBiome != null)
    		return globalBiome;
    	biomes = this.biomeCache.d(x, z);
        return biomes[(z & 0xF) * 16 + (x & 0xF)];
    }

    /*
     * Get the "wetness" value of a region.
     * status - done
     */
    public float[] getWetness(float[] wetness, int x, int z, int lx, int lz){
    	int x1 = x & (~(int)0xF);
    	int z1 = z & (~(int)0xF);
    	int x2 = (x + lx - 1) | 0xF;
    	int z2 = (z + lz - 1) | 0xF;
    	boolean hit = false;
    	
    	if(globalBiome != null){
    		if(wetness == null || wetness.length < lx * lz)
    			wetness = new float[lx * lz];
    		Arrays.fill(wetness, globalBiome.z);
    	}
    	else
    		wetness = inner.getWetness(wetness, x, z, lx, lz);
    	
    	BiomeBase[] biomestemp = new BiomeBase[(x2 - x1 + 1) * (z2 - z1 + 1)];
    	
    	for(int j = 0; j < (z2 - z1 + 1); j += 16)
    		for(int i = 0; i < (x2 - x1 + 1); i += 16){
    			BiomeBase[] chunk = getBiomeChunk((x >> 4) + (i >> 4), (z >> 4) + (j >> 4));
    			if(chunk != null){
    				hit = true;
    				for(int l = 0; l < 16; l++)
    					for(int k = 0; k < 16; k++)
    						biomestemp[(j + l) * (x2 - x1 + 1) + i + k] = chunk[l * 16 + k];
    			}
    		}
    	
    	if(hit){
    		for(int j = 0; j < lz; j++)
    			for(int i = 0; i < lx; i++){
    				BiomeBase bb = biomestemp[(z - z1 + j) * (x2 - x1 + 1) + x - x1 + i];
    				if(bb != null)
    					wetness[j * lx + i] = bb.z;
    			}
    	}
    	
    	/*//16*16, chunk-aligned only
    	float[] wetness = inner.getWetness(afloat, x, z, lx, lz);
    	BiomeBase[] biomes = getBiomeChunk(x >> 4, z >> 4);
    	if(lx == 16 && lz == 16 && biomes != null)
    		for(int i = 0; i < wetness.length; i++)
    			if(biomes[i] != null)
    				wetness[i] = biomes[i].z;
    	*/

    	return wetness;
    }

    /*
     * Get temperature of a block
     * status - done
     */
    public float a(int x, int y, int z){
        return biomeCache.c(x, z);
    }

    /*
     * Get temperature adjusted for elevation - currently pointless
     * status - done, no changes needed
     */
    public float a(float temp, int y){
        return temp;
    }

    /*
     * Get temp of a region
     * status - done
     * seems to only be called for chunk-aligned regions...
     */
    public float[] a(int x, int z, int lx, int lz){
    	//if((x & 0xF) != 0 || (z & 0xF) != 0 || lx != 16 || lz != 16)
    	//	System.out.println("[BIOMES]{a} non 16x16 region requested");
        this.a = biomeCache.a(x, z).a;
        return this.a;
    }

    /*
     * Get temperature of region
     * status - done
     */
    public float[] getTemperatures(float[] temps, int x, int z, int lx, int lz){
    	int x1 = x & (~(int)0xF);
    	int z1 = z & (~(int)0xF);
    	int x2 = (x + lx - 1) | 0xF;
    	int z2 = (z + lz - 1) | 0xF;
    	boolean hit = false;
    	
    	if(globalBiome != null){
    		if(temps == null || temps.length < lx * lz)
    			temps = new float[lx * lz];
    		Arrays.fill(temps, globalBiome.y);
    	}
    	else
    		temps = inner.getTemperatures(temps, x, z, lx, lz);
    	
    	BiomeBase[] biomestemp = new BiomeBase[(x2 - x1 + 1) * (z2 - z1 + 1)];
    	
    	for(int j = 0; j < (z2 - z1 + 1); j += 16)
    		for(int i = 0; i < (x2 - x1 + 1); i += 16){
    			BiomeBase[] chunk = getBiomeChunk((x >> 4) + (i >> 4), (z >> 4) + (j >> 4));
    			if(chunk != null){
    				hit = true;
    				for(int l = 0; l < 16; l++)
    					for(int k = 0; k < 16; k++)
    						biomestemp[(j + l) * (x2 - x1 + 1) + i + k] = chunk[l * 16 + k];
    			}
    		}
    	
    	if(hit){
    		for(int j = 0; j < lz; j++)
    			for(int i = 0; i < lx; i++){
    				BiomeBase bb = biomestemp[(z - z1 + j) * (x2 - x1 + 1) + x - x1 + i];
    				if(bb != null)
    					temps[j * lx + i] = bb.y;
    			}
    	}
    	/*//chunk-aligned only
    	temps = inner.getTemperatures(temps, x, z, lx, lz);
    	BiomeBase[] biomes = getBiomeChunk(x >> 4, z >> 4);
    	if(lx == 16 && lz == 16 && biomes != null)
    		for(int i = 0; i < temps.length; i++)
    			if(biomes[i] != null)
    				temps[i] = biomes[i].y;
    	*/
    	
    	return temps;
    }

    /*
     * Get biomes of a region
     * status - done
     */
    public BiomeBase[] getBiomes(BiomeBase[] biomes, int x, int z, int lx, int lz){
    	int x1 = x & (~(int)0xF);
    	int z1 = z & (~(int)0xF);
    	int x2 = (x + lx - 1) | 0xF;
    	int z2 = (z + lz - 1) | 0xF;
    	boolean hit = false;
    	
    	if(globalBiome != null){
    		if(biomes == null || biomes.length < lx * lz)
    			biomes = new BiomeBase[lx * lz];
    		Arrays.fill(biomes, globalBiome);
    	}
    	else
    		biomes = inner.getBiomes(biomes, x, z, lx, lz);
    	
    	BiomeBase[] biomestemp = new BiomeBase[(x2 - x1 + 1) * (z2 - z1 + 1)];
    	
    	for(int j = 0; j < (z2 - z1 + 1); j += 16)
    		for(int i = 0; i < (x2 - x1 + 1); i += 16){
    			BiomeBase[] chunk = getBiomeChunk((x >> 4) + (i >> 4), (z >> 4) + (j >> 4));
    			if(chunk != null){
    				hit = true;
    				for(int l = 0; l < 16; l++)
    					for(int k = 0; k < 16; k++)
    						biomestemp[(j + l) * (x2 - x1 + 1) + i + k] = chunk[l * 16 + k];
    			}
    		}
    	
    	if(hit){
    		for(int j = 0; j < lz; j++)
    			for(int i = 0; i < lx; i++){
    				BiomeBase bb = biomestemp[(z - z1 + j) * (x2 - x1 + 1) + x - x1 + i];
    				if(bb != null)
    					biomes[j * lx + i] = bb;
    			}
    	}
    	
    	/*//chunk-aligned regions only
    	System.out.println("{getBiomes} x: " + x + " z: " + z + " lx: " + lx + " lz: " + lz + " null? " + (biomesrc == null));
    	if(lx == 16 && lz == 16 && (x & 15) == 0 && (z & 15) == 0 && biomesrc != null){
    		System.out.println("{getBiomes} region is 16x16");
    		for(int i = 0; i < biomes.length; i++)
    			if(biomesrc[i] != null)
    				biomes[i] = biomesrc[i];
    	}
    	*/
    	
    	return biomes;
    }

    /*
     * Get biomes of a region from cache
     * status - done, no changes needed
     */
    public BiomeBase[] getBiomeBlock(BiomeBase[] biomes, int x, int z, int lx, int lz){
        return this.a(biomes, x, z, lx, lz, true);
    }

    /*
     * Get biomes of a region - choice of cache
     * status - done
     */
    public BiomeBase[] a(BiomeBase[] biomes, int x, int z, int lx, int lz, boolean useCache){
        if (useCache && (x & 0xF) == 0 && (z & 0xF) == 0 && lx == 16 && lz == 16)
        	return biomeCache.d(x, z);
        else
        	return this.getBiomes(biomes, x, z, lx, lz);
    }

    /*
     * Check if chunk is valid spawn?
     * status - done
     * TODO patch in modified data?
     */
    @SuppressWarnings("rawtypes")
	public boolean a(int i, int j, int k, List list) {
        return inner.a(i, j, k, list);
    }

    /*
     * Find a valid spawn chunk?
     * status - done
     * TODO patch in modified data?
     */
    @SuppressWarnings("rawtypes")
	public ChunkPosition a(int i, int j, int k, List list, Random random) {
        return inner.a(i, j, k, list, random);
    }

    /*
     * Wipe old chunks from cache
     * status - done
     */
    public void b() {
    	biomeCache.a();
        inner.b();
    }
    
    public void insertBiomeRegion(int x, int z, int lx, int lz, BiomeBase biome){
    	int x1 = x & (~(int)0xF);
    	int z1 = z & (~(int)0xF);
    	int x2 = (x + lx - 1) | 0xF;
    	int z2 = (z + lz - 1) | 0xF;
    	int xl = x - x1;
    	int zl = z - z1;
    	int xh = xl + lx - 1;
    	int zh = zl + lz - 1;
    	
    	//System.out.println("X: " + x + " Z: " + z + " LX: " + lx + " LZ: " + lz);
    	//System.out.println("x1: " + x1 + " x2: " + x2);
    	//System.out.println("z1: " + z1 + " z2: " + z2);
    	//System.out.println("xl: " + xl + " xh: " + xh);
    	//System.out.println("zl: " + zl + " zh: " + zh);
    	
    	BiomeBase[] biomes = new BiomeBase[(x2 - x1 + 1) * (z2 - z1 + 1)];
    	
    	for(int j = zl; j <= zh; j++)
    		for(int i = xl; i <= xh; i++ )
    			biomes[(x2 - x1 + 1) * j + i] = biome;
    	
    	for(int j = 0; j < (z2 - z1 + 1); j += 16)
    		for(int i = 0; i < (x2 - x1 + 1); i += 16){
    			BiomeBase[] chunk = new BiomeBase[16 * 16];
    			for(int l = 0; l < 16; l++)
    				for(int k = 0; k < 16; k++)
    					chunk[l * 16 + k] = biomes[(j + l) * (x2 - x1 + 1) + i + k];
    			insertBiomeChunk(chunk, (x1 + i) >> 4, (z1 + j) >> 4);
    		}
    }
    
    public void insertBiomeChunk(BiomeBase[] chunk, int x, int z){
    	//System.out.println("{insertBiomeChunk} at x: " + x + " z: " + z);
    	//long key = (((long) z) << (Integer.SIZE - 4)) | ((long) x);
    	long key = longKey(x, z);
    	BiomeBase[] oldChunk = (BiomeBase[]) biomeData.get(key);
    	if(chunk != null && oldChunk != null){
    		for(int i = 0; i < oldChunk.length; i++)
    			if(chunk[i] != null)
    				oldChunk[i] = chunk[i];
    	}
    	else
    		oldChunk = chunk;
    	biomeData.put(key, oldChunk);
    	
    	 BiomeCacheBlock bcb = biomeCache.a(x << 4, z << 4);
    	 getTemperatures(bcb.a, x << 4, z << 4, 16, 16);
    	 getWetness(bcb.b, x << 4, z << 4, 16, 16);
    	 getBiomes(bcb.c, x << 4, z << 4, 16, 16);
    }
    
    public BiomeBase[] getBiomeChunk(int x, int z){
    	return (BiomeBase[]) biomeData.get(longKey(x, z));
    }
    
    public long longKey(int x, int z){
    	return (long) x & 0xFFFFFFFFL | ((long) z & 0xFFFFFFFFL) << 32;
    }

	public BiomeBase globalBiome(){
		return globalBiome;
	}

	public void setGlobalBiome(BiomeBase biome){
		globalBiome = biome;
		biomeCache = new BiomeCache(this);
	}

	public void clearBiomeRegion(int x, int z, int lx, int lz) {
    	int x1 = x & (~(int)0xF);
    	int z1 = z & (~(int)0xF);
    	int x2 = (x + lx - 1) | 0xF;
    	int z2 = (z + lz - 1) | 0xF;
    	
    	for(int j = z1; j <= z2; j += 16)
    		for(int i = x1; i <= x2; i += 16)
    			insertBiomeChunk(null, (x1 + i) >> 4, (z1 + j) >> 4);
	}
}
