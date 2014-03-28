package com.conveyal.disser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;

import com.conveyal.disser.census.Census;
import com.conveyal.disser.census.GeoTable;


public class Disser {
    public static void main(String[] args) throws IOException {
    	if( args.length < 2 ) {
    		System.out.println( "usage: cmd shapfile sf1geofile" );
    		return;
    	}
    	
    	String shapefile_name = args[0];
    	String geo_filename = args[1];
    	
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
        
        // print a single feature
        System.out.println( "here is a feature from the shapefile you gave" );
        while( iterator.hasNext() ){
             Feature feature = (Feature) iterator.next();
             System.out.println( feature );             
             break;
        }
        
        System.out.println( "reading sf1 geo table" );
        GeoTable stuff = new GeoTable(geo_filename);
        Map<String, String> eg = stuff.getAllLogRecNos();
        System.out.println( "done, "+eg.size()+" records" );

    }
}
