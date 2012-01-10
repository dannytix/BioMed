package me.karlmarx.biomed;

import java.io.Serializable;

import org.bukkit.block.Biome;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import com.sk89q.worldedit.regions.Region;

public class BiomeRegion implements Serializable {

	private static final long serialVersionUID = -1627782473188840703L;
	public int x;
	public int z;
	public int lx;
	public int lz;
	public Biome biome;

	public BiomeRegion(){
		x = 0;
		z = 0;
		lx = 0;
		lz = 0;
		biome = null;
	}
	
	public BiomeRegion(int x, int z, int lx, int lz, Biome biome){
		this.x = x;
		this.z = z;
		this.lx = lx;
		this.lz = lz;
		this.biome = biome;
	}
	
	public BiomeRegion(Region region, Biome biome){
		this.x = (region.getMaximumPoint().getBlockX() < region.getMinimumPoint().getBlockX())
			? region.getMaximumPoint().getBlockX() : region.getMinimumPoint().getBlockX();
		this.z = (region.getMaximumPoint().getBlockZ() < region.getMinimumPoint().getBlockZ())
			? region.getMaximumPoint().getBlockZ() : region.getMinimumPoint().getBlockZ();
		this.lx = region.getMaximumPoint().getBlockX() - region.getMinimumPoint().getBlockX();
		if(this.lx < 0) this.lx *= -1;
		this.lx++;
		this.lz = region.getMaximumPoint().getBlockZ() - region.getMinimumPoint().getBlockZ();
		if(this.lz < 0) this.lz *= -1;
		this.lz++;
		this.biome = biome;
	}
	public String toString(){
		return "x: " + x + " z: " + z + " lx: " + lx + " lz: " + lz + " biome: " + biome;
	}
}
