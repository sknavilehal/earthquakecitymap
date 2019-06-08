package EarthquakeCityMap;

import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PGraphics;

public class OceanQuakeMarker extends EarthquakeMarker{
	
	public OceanQuakeMarker(PointFeature earthquake) {
		super(earthquake);
	}

	@Override
	public void drawEarthquake(PGraphics pg, float x, float y) {
		pg.rect(x-radius*0.8f, y-radius*0.8f, 1.6f*radius, 1.6f*radius);
		
	}
}
