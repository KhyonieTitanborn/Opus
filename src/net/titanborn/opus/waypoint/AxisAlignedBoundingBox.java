package net.titanborn.opus.waypoint;

import org.bukkit.Location;

import coffee.khyonieheart.hyacinth.util.marker.NotNull;

/**
 * Represents a 3D axis-aligned bounding box which can check if a point is contained within.
 */
public class AxisAlignedBoundingBox
{
	private double rx, ry, rz;
	private Location center;
	private double largestRadius;

	public AxisAlignedBoundingBox(
		@NotNull Location center, 
		double rx, 
		double ry, 
		double rz
	) {
		this.center = center;
		this.rx = rx;
		this.ry = ry;
		this.rz = rz;

		this.largestRadius = rx;
		if (this.largestRadius < ry)
		{
			this.largestRadius = ry;
		}

		if (this.largestRadius < rz)
		{
			this.largestRadius = rz;
		}
	}

	public AxisAlignedBoundingBox(
		@NotNull Location center,
		double radius
	) {
		this(center, radius, radius, radius);
	}

	public Location getCenter()
	{
		return this.center;
	}

	public double getRadiusX()
	{
		return this.rx;
	}

	public double getRadiusY()
	{
		return this.ry;
	}

	public double getRadiusZ()
	{
		return this.rz;
	}

	/**
	 * Performs a comparison between the distance between the center of this AABB and the given location and the largest radius for this AABB, multiplied by the near threshold.
	 * Ex. AABB with XYZ radii (5, 5, 5) with a near threshold of 1.5 will return true if the given location is within 7.5 blocks of the center.
	 * @param location Location to compare 
	 * @param nearThreshold Modifier to radius to compare if the given location is near 
	 * @return Whether or not the center of this AABB is near the given location
	 */
	public boolean isNear(
		@NotNull Location location, 
		double nearThreshold
	) {
		return location.distanceSquared(center) <= (largestRadius * largestRadius) * nearThreshold;			
	}

	public boolean contains(
		@NotNull Location loc
	) {
		if (loc.getX() > center.getX() - rx && loc.getX() < center.getX() + rx)
		{
			if (loc.getY() > center.getY() - ry && loc.getY() < center.getY() + ry)
			{
				if (loc.getZ() > center.getZ() - rz && loc.getZ() < center.getZ() + rz)
				{
					return true;
				}
			}
		}

		return false;
	}
}
