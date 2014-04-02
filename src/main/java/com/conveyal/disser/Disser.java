package com.conveyal.disser;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.BBOX;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;


public class Disser {
    public static void main(String[] args) throws Exception {
    	if( args.length < 4 ) {
    		System.out.println( "usage: cmd indicator_shp indicator_fld diss_shp diss_fld" );
    		return;
    	}
    	    	
    	String indicator_shp = args[0];
    	String indicator_fld = args[1];
    	String diss_shp = args[2];
    	String diss_fld = args[3];
      
    	//==== get indicator shapefile
    	
    	// construct shapefile factory
        File file = new File( indicator_shp );
        Map<String,URL> map = new HashMap<String,URL>();
        map.put( "url", file.toURI().toURL() );
        DataStore dataStore = DataStoreFinder.getDataStore( map );
        
        // get shapefile as generic 'feature source'
        String typeName = dataStore.getTypeNames()[0];
        FeatureSource<?, ?> indicatorSource = dataStore.getFeatureSource( typeName );
        
        //==== get diss source
        
    	// construct shapefile factory
        File dissFile = new File( diss_shp );
        Map<String,URL> dissMap = new HashMap<String,URL>();
        dissMap.put( "url", dissFile.toURI().toURL() );
        DataStore dissDataStore = DataStoreFinder.getDataStore( dissMap );
        
        // get shapefile as generic 'feature source'
        String dissTypeName = dissDataStore.getTypeNames()[0];
        FeatureSource<?, ?> dissSource = dissDataStore.getFeatureSource( dissTypeName );
        
        //==== make sure both shapefiles have the same CRS
        CoordinateReferenceSystem crs1 = indicatorSource.getSchema().getCoordinateReferenceSystem();
        CoordinateReferenceSystem crs2 = dissSource.getSchema().getCoordinateReferenceSystem();
        if( !crs1.equals(crs2) ){
        	throw new Exception( "Coordinate systems don't match. "+crs1+"\n\n"+crs2 );
        }        
        
        //==== loop through indicator shapefile, finding overlapping diss items
    	HashMap<Feature,ArrayList<Feature>> dissToInd = new HashMap<Feature,ArrayList<Feature>>();
        
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
        FeatureType schema = dissSource.getSchema();
        String geometryPropertyName = schema.getGeometryDescriptor().getLocalName();
        CoordinateReferenceSystem dissCRS = schema.getGeometryDescriptor().getCoordinateReferenceSystem();
        
        // get the part of the indicator file where diss items will be found
        ReferencedEnvelope dissBBox = dissSource.getBounds();
        String indicatorPropertyName = indicatorSource.getSchema().getGeometryDescriptor().getLocalName();
        BBOX dissFilter = ff.bbox(ff.property(indicatorPropertyName), dissBBox);

        FeatureCollection<?, ?> collection = indicatorSource.getFeatures(dissFilter);
        FeatureIterator<?> iterator = collection.features();
        int n = collection.size();
        
        System.out.println( "accumulating ind geoms under disses" );
        int i=0;
        while( iterator.hasNext() ){
        	if(i%100==0){
        		System.out.println( i+"/"+n+" ("+(100*(float)i)/n+"%)" );
        	}
        	
             Feature ind = (Feature) iterator.next();
             GeometryAttribute geoAttr = ind.getDefaultGeometryProperty();
             Geometry indGeo = (Geometry)geoAttr.getValue();
                          
             // Get every diss geometry that intersects with the indicator geometry           
             ReferencedEnvelope bbox = new ReferencedEnvelope(dissCRS);
             bbox.setBounds(geoAttr.getBounds());
             BBOX filter = ff.bbox(ff.property(geometryPropertyName), bbox);
             FeatureCollection<?, ?> dissCollection = dissSource.getFeatures(filter);
             FeatureIterator<?> dissIterator = dissCollection.features();
             
             // register the ind feature with the diss
             while(dissIterator.hasNext()){
            	 Feature diss = (Feature)dissIterator.next();
            	 GeometryAttribute dissGeoAttr = diss.getDefaultGeometryProperty();
            	 Geometry dissGeo = (Geometry)dissGeoAttr.getValue();
            	 
            	 if(dissGeo.intersects(indGeo)){
            		 ArrayList<Feature> inds = dissToInd.get(diss);
            		 if(inds==null){
            			 inds = new ArrayList<Feature>();
            			 dissToInd.put(diss, inds);
            		 }
            		 inds.add(ind);	 
            	 }
             }
             dissIterator.close();
             i++;
        }
        
        // register each diss with the inds, along with the ind's share of the diss's magnitude
        System.out.println( "accumulating diss shares under inds" );
        HashMap<Feature,ArrayList<DissShare>> indDissShares = new HashMap<Feature,ArrayList<DissShare>>();
        for( Entry<Feature, ArrayList<Feature>> entry : dissToInd.entrySet() ){
        	Feature diss = entry.getKey();
        	ArrayList<Feature> inds = entry.getValue();
        	
        	// determine diss's magnitude
        	int mag = (Integer)diss.getProperty( diss_fld ).getValue();
        	
        	Geometry dissGeo = (Geometry)diss.getDefaultGeometryProperty().getValue();
        	double dissGeoArea = dissGeo.getArea();
        	for(Feature ind : inds){
            	// find the fraction of diss overlapping each ind shape
        		Geometry indGeo = (Geometry)ind.getDefaultGeometryProperty().getValue();
        		Geometry overlap = dissGeo.intersection(indGeo);
        		double overlapArea = overlap.getArea();
        		double fraction = overlapArea/dissGeoArea;
        		
            	// assign the magnitude proportionately
        		double share = fraction*mag;
        		
        		// then register the diss feature's share with the ind feature
        		ArrayList<DissShare> shares = indDissShares.get(ind);
        		if(shares==null){
        			shares = new ArrayList<DissShare>();
        			indDissShares.put(ind, shares);
        		}
        		shares.add(new DissShare(diss,share));
        	}
        }
        
        // dole out the ind's magnitude in proportion to the diss's mag share, accumulating under the diss
        System.out.println( "doling out ind magnitudes to disses" );
        HashMap<Feature,Double> dissMags = new HashMap<Feature,Double>();
        for( Entry<Feature, ArrayList<DissShare>> entry : indDissShares.entrySet() ){
        	Feature ind = entry.getKey();
        	ArrayList<DissShare> dissShares = entry.getValue();
        	
        	// count up total shares
        	int totalDissMag = 0;
        	int totalDiss = 0;
        	for(DissShare dissShare : dissShares){
        		totalDissMag += dissShare.mag;
        		totalDiss += 1;
        	}
        	
        	// get magnitude of ind
        	int indMag = (Integer)ind.getProperty(indicator_fld).getValue();
        	
        	// for every diss associated with ind
        	for( DissShare dissShare : dissShares ){
        		// find fraction of ind doleable to diss
        		double fraction;
        		if(totalDissMag>0){
        			fraction = dissShare.mag/totalDissMag;
        		} else {
        			// if all disses under ind have 0 magnitude, but the
        			// ind still has magnitude to dole out, dole out equally
        			// to all disses.
        			fraction = 1.0/totalDiss; 
        		}
        		double doleable = indMag*fraction;
        		
        		// accumulate values doled out to disses
        		Double dissMag = dissMags.get(dissShare);
        		if(dissMag==null){
        			dissMag = new Double(0);
        		}
        		dissMag += doleable;
        		dissMags.put(dissShare.diss,dissMag);
        	}
        }
        
        // go through the dissMag list and emit points at centroids
        System.out.print( "printing to file..." );
        PrintWriter writer = new PrintWriter("afile.csv", "UTF-8");
        writer.println("lon,lat,mag");
        for( Entry<Feature, Double> entry : dissMags.entrySet() ) {
        	Geometry dissGeom = (Geometry)entry.getKey().getDefaultGeometryProperty().getValue();
        	Point centroid = dissGeom.getCentroid();
        	double mag = entry.getValue();
        	if(mag>0){
        		writer.println( centroid.getX()+","+centroid.getY()+","+mag);
        	}
        }
        writer.flush();
        writer.close();
        System.out.print("done.\n");
    }
}
