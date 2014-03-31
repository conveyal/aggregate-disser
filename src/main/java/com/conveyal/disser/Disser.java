package com.conveyal.disser;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.geometry.BoundingBox;

import com.conveyal.disser.census.CensusRecord;
import com.conveyal.disser.census.CensusTable;
import com.conveyal.disser.census.SummaryFile;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;


public class Disser {
    public static void main(String[] args) throws Exception {
    	if( args.length < 6 ) {
    		System.out.println( "usage: cmd shapfile basedir stateabbrev year table field" );
    		return;
    	}
    	
    	String shapefile_name = args[0];
    	String basedir = args[1];
    	String stateabbrev = args[2];
    	int year = Integer.parseInt(args[3]);
    	String tableName = args[4];
    	int field = Integer.parseInt(args[5]);
    	
    	// read a census table
    	SummaryFile sf1 = new SummaryFile(basedir,stateabbrev,year);
      
    	System.out.println( "reading sf1 geo table" );
    	Map<String,String> eg = sf1.getGeoTable();
    	System.out.println( "done, "+eg.size()+" records" );
      
    	System.out.println( "reading "+tableName+" table" );
    	CensusTable table = sf1.getTable(tableName);
    	System.out.println( "table has "+table.records.size()+" records");
      
    	//=================================
    	
    	// construct shapefile factory
        File file = new File( shapefile_name );
        Map<String,URL> map = new HashMap<String,URL>();
        map.put( "url", file.toURI().toURL() );
        DataStore dataStore = DataStoreFinder.getDataStore( map );
        
        // get shapefile as generic 'feature source'
        String typeName = dataStore.getTypeNames()[0];
        FeatureSource<?, ?> source = dataStore.getFeatureSource( typeName );
        
        // get feature iterator from feature source
        FeatureCollection<?, ?> collection = source.getFeatures( );
        FeatureIterator<?> iterator = collection.features();
        
        // print out disaggregated points
        int n=0;
        while( iterator.hasNext() ){
             Feature feature = (Feature) iterator.next();
             GeometryAttribute geo = feature.getDefaultGeometryProperty();
             
             String geoid10 = (String)feature.getProperties("GEOID10").iterator().next().getValue();
             String logrecno = eg.get(geoid10);
             CensusRecord rec = table.getRecord( logrecno );
             int count = rec.getCensusIntField( field );
             
             n += count;

         	 for(int i=0; i<count; i++){
         		 Point rando = getRandomPoint( geo );
                 System.out.println( rando.getX()+","+rando.getY()+",1" );
                 n++;
         	 }
        }
        System.out.println( "done with "+n+" points" );
    }

	private static Point getRandomPoint(GeometryAttribute geo) throws Exception {
        if( !geo.getType().getName().toString().equals("MultiPolygon") ){
       	  throw new Exception( "Geometry isn't a multipolygon; don't know what to do." );
        }
        
        BoundingBox bb = geo.getBounds();
        
        MultiPolygon mp = (MultiPolygon)geo.getValue();
        
        Random rand = new Random();
        GeometryFactory gf = new GeometryFactory();
        Point pt = null;
        for(int i=0; i<1000; i++){
        	double x = randdouble(rand, bb.getMinX(),bb.getMaxX());
        	double y = randdouble(rand, bb.getMinY(),bb.getMaxY());
        	pt = gf.createPoint( new Coordinate(x,y) );
        	if(mp.contains(pt)){
        		break;
        	}
        }
        
        return pt;
	}

	private static double randdouble(Random rand, double minX, double maxX) {
		return minX + rand.nextDouble()*(maxX-minX);
	}
}
