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

public class Disser {
    public static void main(String[] args) throws IOException {
    	if( args.length < 1 ) {
    		System.out.println( "usage: cmd shapfile" );
    		return;
    	}
    	
    	String shapefile_name = args[0];
    	
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

    }
}
