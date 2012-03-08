package me.karlmarx.biomed;

import java.util.Arrays;

import net.minecraft.server.BiomeBase;
import net.minecraft.server.LongHashMap;
import net.minecraft.server.WorldChunkManagerHell;

public class BioMedChunkManagerHell extends WorldChunkManagerHell implements BioMedManager{

	private LongHashMap biomeData;
	private BiomeBase globalBiome;
	

    public BioMedChunkManagerHell(BiomeBase biome, float temp, float wet){
    	super(biome, temp, wet);
    	biomeData = new LongHashMap();
    	globalBiome = null;
    }
    
    public BioMedChunkManagerHell(BiomeBase biome, float temp, float wet, BiomeBase global){
    	this(biome, temp, wet);
    	globalBiome = global;
    }

    /*
     * get biome of a single block
     * status - done
     */
    public BiomeBase getBiome(int x, int z){
    	BiomeBase[] biomes = (BiomeBase[]) biomeData.getEntry(longKey(x >> 4, z >> 4));
    	if(biomes != null && biomes[(z & 0xF) * 16 + (x & 0xF)] != null)
    		return biomes[(z & 0xF) * 16 + (x & 0xF)];
    	else if(globalBiome != null)
    		return globalBiome;
    	else
    		return super.getBiome(x, z);
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
    	BiomeBase[] biomestemp = new BiomeBase[(x2 - x1 + 1) * (z2 - z1 + 1)];
    	if(globalBiome != null){
    		if(wetness == null || wetness.length < lx * lz)
    			wetness = new float[lx * lz];
    		Arrays.fill(wetness, globalBiome.z);
    	}
    	else
    		wetness = super.getWetness(wetness, x, z, lx, lz);
    	
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

    	return wetness;
    }

    /*
     * Get temperature of a block
     * status - done
     */
    public float a(int x, int y, int z){
    	BiomeBase[] biomes = this.getBiomeChunk(x >> 4, z >> 4);
    	if(biomes != null && biomes[(z & 0xF) * 16 + (x & 0xF)] != null)
    			return biomes[(z & 0xF) * 16 + (x & 0xF)].y;
    	else if(globalBiome != null)
    		return globalBiome.y;
    	else
    		return super.a(x, y, z);
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
    	BiomeBase[] biomestemp = new BiomeBase[(x2 - x1 + 1) * (z2 - z1 + 1)];
    	if(globalBiome != null){
    		if(temps == null || temps.length < lx * lz)
    			temps = new float[lx * lz];
    		Arrays.fill(temps, globalBiome.y);
    	}
    	else
    		temps = super.getTemperatures(temps, x, z, lx, lz);
    	
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
    	BiomeBase[] biomestemp = new BiomeBase[(x2 - x1 + 1) * (z2 - z1 + 1)];
    	if(globalBiome != null){
    		if(biomes == null || biomes.length < lx * lz)
    			biomes = new BiomeBase[lx * lz];
    		Arrays.fill(biomes, globalBiome);
    	}
    	else
    		biomes = super.getBiomes(biomes, x, z, lx, lz);
    	
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
    	
    	return biomes;
    }

    /*
     * Get biomes of a region from cache
     * status - done, no changes needed
     */
    public BiomeBase[] getBiomeBlock(BiomeBase[] biomes, int x, int z, int lx, int lz){
        return getBiomes(biomes, x, z, lx, lz);
    }

    /*
     * Get biomes of a region - choice of cache
     * status - done
     */
    public BiomeBase[] a(BiomeBase[] biomes, int x, int z, int lx, int lz, boolean useCache){
        return getBiomes(biomes, x, z, lx, lz);
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
    	BiomeBase[] oldChunk = (BiomeBase[]) biomeData.getEntry(key);
    	if(oldChunk != null){
    		for(int i = 0; i < oldChunk.length; i++)
    			if(chunk[i] != null)
    				oldChunk[i] = chunk[i];
    	}
    	else
    		oldChunk = chunk;
    	biomeData.put(key, oldChunk);
    }
    
    public BiomeBase[] getBiomeChunk(int x, int z){
    	//System.out.println("{getBiomeChunk} at x: " + x + " z: " + z);
    	long key = longKey(x, z);
    	return (BiomeBase[]) biomeData.getEntry(key);
    }
    
    public long longKey(int x, int z){
    	return (long) x & 0xFFFFFFFFL | ((long) z & 0xFFFFFFFFL) << 32;
    }

	public BiomeBase globalBiome() {
		return globalBiome;
	}

	public void setGlobalBiome(BiomeBase biome) {
		globalBiome = biome;
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
