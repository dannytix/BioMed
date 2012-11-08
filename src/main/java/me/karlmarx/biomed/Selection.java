package me.karlmarx.biomed;

import java.io.Serializable;

public class Selection implements Serializable {
	private static final long serialVersionUID = 1L;
	public int x;
	public int z;
	public int lx;
	public int lz;

	public Selection(){
		x = 0;
		z = 0;
		lx = 0;
		lz = 0;
	}
	
	public Selection(int x, int z, int lx, int lz){
		this.x = x;
		this.z = z;
		this.lx = lx;
		this.lz = lz;
	}
	
	public boolean isEmpty() {
		return lx == 0 || lz == 0;
	}
	
	@Override
	public String toString(){
		return "x: " + x + " z: " + z + " lx: " + lx + " lz: " + lz;
	}
	
}
