aggregate-disser
================

A tool for reversing an aggregated data set into the set from which it was produced. Learn more about how it works here: [Building Precise Maps with Disser](http://conveyal.com/blog/2014/04/08/aggregate-disser/)

## Prerequisites

* gradle

## Building

    $ gradle farJar
    
## Running

    $ java -jar ./build/libs/aggregate-disser.jar [--(discrete|shapefile)] indicator_shp indicator_fld diss_shp diss_fld output_fn
    
## Examples

Disaggregate census population counts from blocks into NYC parcel centroids, in proportion to residential square footage:
    
    $ java -jar aggregate-disser.jar /path/to/data/census.shp POP10 /path/to/data/pluto.shp ResArea output.csv
    
Disaggregate census population counts from blocks into building outlines with no known residential are property, using only the shape area:

    $ java -jar aggregate-disser.jar /path/to/data/census.shp POP10 /path/to/data/bldg_outlines.shp ::area:: output.csv
    
Distribute evenly through disaggregate shapes:

    $ java -jar aggregate-disser.jar --discrete /path/to/data/census.shp POP10 /path/to/data/bldg_outlines.shp ::area:: output.csv
    
Build shapefile of disaggregated shapes:

    $ java -jar aggregate-disser.jar --shapefile /path/to/data/census.shp POP10 /path/to/data/bldg_outlines.shp ::area:: output.csv
    
Disaggregate according to sum of several properties:

    $ java -jar ./build/libs/aggregate-disser.jar /path/to/data/jobs_blocks.shp total /path/to/data/pluto.shp ComArea+OfficeArea+RetailArea+FactryArea jobs_parcels.shp
