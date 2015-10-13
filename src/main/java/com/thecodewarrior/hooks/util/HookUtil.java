package com.thecodewarrior.hooks.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import codechicken.lib.vec.Vector3;

import com.thecodewarrior.hooks.HookMod;

public class HookUtil
{
	private static List<AxisAlignedBB> collidingBoundingBoxes = new ArrayList<AxisAlignedBB>();
	
	private static void addCollidingBoundingBoxes(Entity e, World w, AxisAlignedBB aabb) {
        int i = MathHelper.floor_double(aabb.minX);
        int j = MathHelper.floor_double(aabb.maxX + 1.0D);
        int k = MathHelper.floor_double(aabb.minY);
        int l = MathHelper.floor_double(aabb.maxY + 1.0D);
        int i1 = MathHelper.floor_double(aabb.minZ);
        int j1 = MathHelper.floor_double(aabb.maxZ + 1.0D);

        for (int k1 = i; k1 < j; ++k1)
        {
            for (int l1 = i1; l1 < j1; ++l1)
            {
                if (w.blockExists(k1, 64, l1))
                {
                    for (int i2 = k - 1; i2 < l; ++i2)
                    {
                        Block block;

                        if (k1 >= -30000000 && k1 < 30000000 && l1 >= -30000000 && l1 < 30000000)
                        {
                            block = w.getBlock(k1, i2, l1);
                        }
                        else
                        {
                            block = Blocks.stone;
                        }

                        block.addCollisionBoxesToList(w, k1, i2, l1, aabb, collidingBoundingBoxes, e);
                    }
                }
            }
        }
	}
	
	private static Vector3 traceAABB(Vector3 start, Vector3 end, AxisAlignedBB aabb) {
		if(aabb == null || start == null || end == null)
			return null;
		MovingObjectPosition mop = aabb.calculateIntercept(start.toVec3D(), end.toVec3D());
		if(mop == null)
			return null;
		else
			return new Vector3(mop.hitVec);
	}
	
	public static Vector3 collisionRayCast(World world, Vector3 start, Vector3 offset, Entity e)
	{
	    Vector3 end   = start.copy().add(offset);

	    double minX = Math.min(start.x, end.x);
		double minY = Math.min(start.y, end.y);
		double minZ = Math.min(start.z, end.z);
		
		double maxX = Math.max(start.x, end.x);
		double maxY = Math.max(start.y, end.y);
		double maxZ = Math.max(start.z, end.z);
		
//		collidingBoundingBoxes.clear();
		List<AxisAlignedBB> scanBBs = new ArrayList<AxisAlignedBB>();
		double magS = offset.magSquared();
		if(magS > 50) {
			double count = Math.ceil( magS/50.0 ); // this is a double so the division below doesn't round
			for(int i = 0; i <= count; i++) {
				scanBBs.add(
						AxisAlignedBB.getBoundingBox(
								minX+( offset.x * i/count), minY+( offset.y * i/count), minZ+( offset.z * i/count),
								maxX-( offset.x * (count-i)/count), maxY-( offset.y * (count-i)/count), maxZ-( offset.z * (count-i)/count)
							).expand(0.5, 0.5, 0.5)
						);
			}
		} else {
			scanBBs.add(
					AxisAlignedBB.getBoundingBox(
							minX, minY, minZ,
							maxX, maxY, maxZ
						).expand(0.5, 0.5, 0.5)
				);
		}
		for (AxisAlignedBB aabb : scanBBs)
		{
			collidingBoundingBoxes = new ArrayList<AxisAlignedBB>();
			addCollidingBoundingBoxes(e, world, aabb);
			
			Vector3 shortestHit = null;
		    double shortestMagSquared = Double.MAX_VALUE;
		    for (int i = 0; i < collidingBoundingBoxes.size(); i++)
			{
		    	AxisAlignedBB currentBB = (AxisAlignedBB)collidingBoundingBoxes.get(i);
				Vector3 currentHit = traceAABB(start, end, currentBB);
				if(currentHit != null && currentHit.magSquared() < shortestMagSquared)
				{
					shortestHit = currentHit;
					shortestMagSquared = currentHit.magSquared();
				}
			}
		    if(shortestHit != null) {
		    	return shortestHit.sub(start);
			}
		}
		return offset;
	}
}
