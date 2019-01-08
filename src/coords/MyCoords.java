package coords;

import Geom.Point3D;
import Coords.Cords;

public class MyCoords {

	/**
	 * Distance in pixels between two pixel points using the pythagorean theorem.
	 * @param p0 First point.
	 * @param p1 Second point.
	 * @return distance in pixels between the two points.
	 */
	public static double pixelDistance(Point3D p0, Point3D p1) {
		int dx = Math.abs(p0.ix() - p1.ix());
		int dy = Math.abs(p0.iy() - p1.iy());
		return Math.sqrt((dx*dx) + (dy*dy));
	}
	
	/**
	 * Distance in meters between two GPS points.
	 * @param p0 First point.
	 * @param p1 Second point.
	 * @return distance in meters between the two points.
	 */
	public static double meterDistance(Point3D p0_gps, Point3D p1_gps) {
		int radius = 6371000;
		double lat0 = Math.toRadians(p0_gps.x()); double lon0 = Math.toRadians(p0_gps.y());
		double lat1 = Math.toRadians(p1_gps.x()); double lon1 = Math.toRadians(p1_gps.y());
		double dlat = Math.toRadians(p1_gps.x() - p0_gps.x());
		double dlon = Math.toRadians(p1_gps.y() - p0_gps.y());
		double a = Math.pow(Math.sin(dlat/2),2) + Math.cos(lat0) * Math.cos(lat1) * Math.pow(Math.sin(dlon/2),2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		return radius * c;
	}
	
	/**
	 * Returns a point some amount of meters off in a given azimuth from a given point.
	 * @param gps_point Given GPS point.
	 * @param meters Amount of meters to move.
	 * @param azimuth The azimuth.
	 * @return A new GPS point some amount of meters away from the given GPS point.
	 */
	public static Point3D addMetersAzimuth(Point3D gps_point, double meters, double azimuth) {
		double R = 6371;
		azimuth = Math.toRadians(azimuth);
		double km = meters/1000;

		double lat1 = Math.toRadians(gps_point.x());
		double lon1 = Math.toRadians(gps_point.y());

		double lat2 = Math.asin( Math.sin(lat1)*Math.cos(km/R) +
				Math.cos(lat1)*Math.sin(km/R)*Math.cos(azimuth));

		double lon2 = lon1 + Math.atan2(Math.sin(azimuth)*Math.sin(km/R)*Math.cos(lat1),
				Math.cos(km/R)-Math.sin(lat1)*Math.sin(lat2));

		lat2 = Math.toDegrees(lat2);
		lon2 = Math.toDegrees(lon2);
		Point3D new_gps = new Point3D(lat2, lon2);

		return new_gps;
	}

	
	
}
