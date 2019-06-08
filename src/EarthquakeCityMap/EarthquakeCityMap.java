package EarthquakeCityMap;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.OpenStreetMap;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;

public class EarthquakeCityMap extends PApplet {
	private static final long serialVersionUID = 1L;
	UnfoldingMap map;

	private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";

	private List<Marker> cityMarkers;
	private List<Marker> quakeMarkers;
	private List<Marker> countryMarkers;
	
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	private PriorityQueue<Float> pq;
	
	public void setup() {
		size(900, 900, OPENGL);

		//earthquakesURL = "2.5_week.atom";
		map = new UnfoldingMap(this, 200, 50, 650, 600, new OpenStreetMap.OpenStreetMapProvider());
		//map = new UnfoldingMap(this, 200, 50, 650, 600, new Google.GoogleMapProvider());

		MapUtils.createDefaultEventDispatcher(this, map);
		
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
		
		cityMarkers = new ArrayList<Marker>();
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		for(Feature city : cities) {
			cityMarkers.add(new CityMarker((PointFeature)city));
		}
		
		pq = new PriorityQueue<Float>();
		quakeMarkers = new ArrayList<Marker>();
		for(PointFeature earthquake : earthquakes) {
			if(isLand(earthquake)) {
				quakeMarkers.add(new LandQuakeMarker(earthquake));
			}
			else {
				quakeMarkers.add(new OceanQuakeMarker(earthquake));
			}
		}
		
		printQuakes();
		map.addMarkers(quakeMarkers);
		map.addMarkers(cityMarkers);
	}

	public void draw() {
		background(210);
		addKey();
		map.draw();
	}
	
	private boolean isLand(PointFeature earthquake) {
		
		for (Marker country : countryMarkers) {
			if (isInCountry(earthquake, country)) {
				return true;
			}
		}

		return false;
	}
	
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		Location checkLoc = earthquake.getLocation();
		if(country.getClass() == MultiMarker.class) {
				
			for(Marker marker : ((MultiMarker)country).getMarkers()) {

				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));

					return true;
				}
			}
		}

		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			
			return true;
		}
		return false;
	}
	
	private void addKey() {	
		String title;
		EarthquakeMarker marker;
		
		fill(255, 250, 240);
		
		int xbase = 25;
		int ybase = 50;
		
		rect(xbase, ybase, 150, 250);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", xbase+25, ybase+25);
		
		fill(150, 30, 30);
		int tri_xbase = xbase + 35;
		int tri_ybase = ybase + 50;
		triangle(tri_xbase, tri_ybase-CityMarker.TRI_SIZE, tri_xbase-CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE, tri_xbase+CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE);

		fill(0, 0, 0);
		textAlign(LEFT, CENTER);
		text("City Marker", tri_xbase + 15, tri_ybase);
		
		text("Land Quake", xbase+50, ybase+70);
		text("Ocean Quake", xbase+50, ybase+90);
		text("Size ~ Magnitude", xbase+25, ybase+110);
		
		fill(255, 255, 255);
		ellipse(xbase+35, 
				ybase+70, 
				10, 
				10);
		rect(xbase+35-5, ybase+90-5, 10, 10);
		
		fill(color(255, 255, 0));
		ellipse(xbase+35, ybase+140, 12, 12);
		fill(color(0, 0, 255));
		ellipse(xbase+35, ybase+160, 12, 12);
		fill(color(255, 0, 0));
		ellipse(xbase+35, ybase+180, 12, 12);
		
		textAlign(LEFT, CENTER);
		fill(0, 0, 0);
		text("Shallow", xbase+50, ybase+140);
		text("Intermediate", xbase+50, ybase+160);
		text("Deep", xbase+50, ybase+180);
		
		// question 1
		if(lastSelected != null) {
			if(lastSelected.getClass() == LandQuakeMarker.class || lastSelected.getClass() == OceanQuakeMarker.class) {
				marker = (EarthquakeMarker)lastSelected;
				title = "Magnitude: " + marker.getMagnitude();
				text(title, xbase+20, ybase+200);
			}
		}
		
		if(pq.size() != 0) {
			float mag = pq.peek();
			String text = "Least Magnitude: " + mag;
			text(text, xbase+20, ybase+220);
		}
	}

	private void printQuakes() {
		int totalWaterQuakes = quakeMarkers.size();
		for (Marker country : countryMarkers) {
			String countryName = country.getStringProperty("name");
			int numQuakes = 0;
			for (Marker marker : quakeMarkers)
			{
				EarthquakeMarker eqMarker = (EarthquakeMarker)marker;
				if (eqMarker.isOnLand()) {
					if (countryName.equals(eqMarker.getStringProperty("country"))) {
						numQuakes++;
					}
				}
			}
			if (numQuakes > 0) {
				totalWaterQuakes -= numQuakes;
				System.out.println(countryName + ": " + numQuakes);
			}
		}
		System.out.println("OCEAN QUAKES: " + totalWaterQuakes);
	}
	
	@Override
	public void mouseMoved() {
		if(lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		}
		selectMarkerifHover(cityMarkers);
		selectMarkerifHover(quakeMarkers);
	}
	
	private void selectMarkerifHover(List<Marker> markers) {
		if(lastSelected != null) return;
		
		for(Marker marker : markers) {
			if(marker.isInside(map, mouseX, mouseY)) {
				lastSelected = (CommonMarker) marker;
				lastSelected.setSelected(true);
				return;
			}
		}
	}
	
	@Override
	public void mouseClicked() {
		/*
		if(lastClicked != null) {
			unhideMarkers();
			lastClicked = null;
		}
		else {
			checkEarthquakesForClick();
			if(lastClicked == null) {
				checkCitiesForClick();
			}
		}
		*/
		lastClicked = null;
		checkEarthquakesForClick();
	}
	
	private void unhideMarkers() {
		for(Marker city : cityMarkers) {
			city.setHidden(false);
		}
		for(Marker quake : quakeMarkers) {
			quake.setHidden(false);
		}
	}
	
	private void checkEarthquakesForClick() {
		EarthquakeMarker marker;
		for(Marker quake : quakeMarkers) {
			if(!quake.isHidden() && quake.isInside(map, mouseX, mouseY)) {
				lastClicked = (CommonMarker) quake;
				break;
			}
		}
		/*
		if(lastClicked == null) return;
		EarthquakeMarker marker = (EarthquakeMarker)lastSelected;
		
		for(Marker quake : quakeMarkers) {
			if(quake != marker) {
				quake.setHidden(true);
			}
		}
		
		for(Marker city : cityMarkers) {
			if(city.getDistanceTo(marker.getLocation()) > marker.threatCircle()) {
				city.setHidden(true);
			}
		}
		*/
		float mag;
		EarthquakeMarker quakeMarker;
		if(lastClicked != null) {
			if(lastSelected.getClass() == LandQuakeMarker.class || lastSelected.getClass() == OceanQuakeMarker.class) {
				quakeMarker = (EarthquakeMarker)lastSelected;
				mag = quakeMarker.getMagnitude();
				if(pq.size() == 3) {
					pq.poll(); pq.poll(); pq.poll();
				}
				pq.add(mag);
			}
		}

		
		return;
	}
	
	private void checkCitiesForClick() {
		for(Marker city : cityMarkers) {
			if(!city.isHidden() && city.isInside(map, mouseX, mouseY)) {
				lastClicked = (CommonMarker) city;
				break;
			}
		}
		if(lastClicked == null) return;
		CityMarker marker = (CityMarker)lastSelected;
		EarthquakeMarker quakeMarker;
		
		for(Marker city : cityMarkers) {
			if(city != marker) {
				city.setHidden(true);
			}
		}
		
		for(Marker quake : quakeMarkers) {
			quakeMarker = (EarthquakeMarker)quake;
			if(quakeMarker.getDistanceTo(marker.getLocation()) > quakeMarker.threatCircle()) {
				quakeMarker.setHidden(true);
			}
		}
		return;
	}
}
