package com.conveyal.disser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;

import com.conveyal.disser.census.Census;
import com.conveyal.disser.census.CensusRecord;
import com.conveyal.disser.census.CensusTable;
import com.conveyal.disser.census.GeoTable;
import com.conveyal.disser.census.PackingList;
import com.conveyal.disser.census.SummaryFile;
import com.conveyal.disser.census.TableAddress;


public class Disser {
    public static void main(String[] args) throws IOException {
    	if( args.length < 5 ) {
    		System.out.println( "usage: cmd shapfile basedir stateabbrev year table" );
    		return;
    	}
    	
    	String shapefile_name = args[0];
    	String basedir = args[1];
    	String stateabbrev = args[2];
    	int year = Integer.parseInt(args[3]);
    	String tableName = args[4];
    	
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
        String geoid10=null;
        while( iterator.hasNext() ){
             Feature feature = (Feature) iterator.next();
             geoid10 = (String)feature.getProperties("GEOID10").iterator().next().getValue();
             System.out.println( geoid10 );             
             break;
        }
        
        SummaryFile sf1 = new SummaryFile(basedir,stateabbrev,year);
        
        System.out.println( "reading sf1 geo table" );
        Map<String,String> eg = sf1.getGeoTable();
        System.out.println( "done, "+eg.size()+" records" );
        
        String logrecno = eg.get(geoid10);
        System.out.println( logrecno );
        
        System.out.println( "reading "+tableName+" table" );
        CensusTable table = sf1.getTable(tableName);
        System.out.println( "table has "+table.records.size()+" records");
        
        CensusRecord rec = table.getRecord( logrecno );
        System.out.println( rec );
    }
}
