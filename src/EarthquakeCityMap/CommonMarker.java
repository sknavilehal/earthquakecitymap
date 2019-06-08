package EarthquakeCityMap;

import java.util.HashMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import processing.core.PGraphics;

public abstract class CommonMarker extends SimplePointMarker{
	
	CommonMarker(Location location){
		super(location);
	}
	
	CommonMarker(Location location, HashMap<String, Object> properties){
		super(location, properties);
	}
	
	public void draw(PGraphics pg, float x, float y) {
		if(!hidden) {
			drawMarker(pg, x, y);
			if(selected) {
				showTitle(pg, x, y);
			}
		}
	}

	public abstract void showTitle(PGraphics p, float x, float y);
	public abstract void drawMarker(PGraphics p, float x, float y);
}