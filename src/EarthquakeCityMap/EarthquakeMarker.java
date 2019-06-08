package EarthquakeCityMap;

import java.util.HashMap;
import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PConstants;
import processing.core.PGraphics;

public abstract class EarthquakeMarker extends CommonMarker{
	
	protected static final float kmPerMile = 1.6f;
	public static final float THRESHOLD_MODERATE = 5;
	public static final float THRESHOLD_LIGHT = 4;
	public static final float THRESHOLD_INTERMEDIATE = 70;
	public static final float THRESHOLD_DEEP = 300;
	protected boolean isOnLand;
	protected float radius;
	
	public EarthquakeMarker(PointFeature feature) {
		super(feature.getLocation());
		HashMap<String, Object> properties = feature.getProperties();
		float magnitude = Float.parseFloat(properties.get("magnitude").toString());
		properties.put("radius", 1.5*magnitude);
		setProperties(properties);
		this.radius = 1.5f*magnitude;
	}

	public abstract void drawEarthquake(PGraphics pg, float x, float y);
	
	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
		
		pg.pushStyle();
		colorDetermine(pg);
		drawEarthquake(pg, x, y);
		pg.popStyle();
		
	}
	
	@Override
	public void showTitle(PGraphics pg, float x, float y) {

		String title = getTitle();
		pg.pushStyle();
		
		pg.rectMode(PConstants.CORNER);
		
		pg.stroke(110);
		pg.fill(255,255,255);
		//pg.rect(x, y + 15, pg.textWidth(title) +6, 18, 5);
		
		pg.textAlign(PConstants.LEFT, PConstants.TOP);
		pg.fill(0);
		//pg.text(title, x + 3 , y +18);
		
		
		pg.popStyle();
		
	}
	
	private void colorDetermine(PGraphics pg) {
		float depth = getDepth();
	
		if (depth < THRESHOLD_INTERMEDIATE) {
			pg.fill(255, 255, 0);
		}
		else if (depth < THRESHOLD_DEEP) {
			pg.fill(0, 0, 255);
		}
		else {
			pg.fill(255, 0, 0);
		}
	}
	
	public float getRadius() {
		return radius;
	}
	
	public double threatCircle() {
		double miles = 20.0f*Math.pow(1.8, 2*getMagnitude() - 5);
		double km = miles*kmPerMile;
		return km;
	}
	
	public float getMagnitude() {
		return Float.parseFloat(getProperty("magnitude").toString());
	}
	
	public float getDepth() {
		return Float.parseFloat(getProperty("depth").toString());
	}
	
	public String getTitle() {
		return getStringProperty("title");
	}
	
	public boolean isOnLand() {
		return isOnLand;
	}
}
