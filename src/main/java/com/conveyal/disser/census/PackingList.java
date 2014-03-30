package com.conveyal.disser.census;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
 * def parse_packinglist(dirname):
	# parses the packing file, returning (tablename, fileno, field_start, num_fields)

	packing_fn = dirname+"/"+dirname+".prd.packinglist.txt"

	fp=open(packing_fn)

	# hackily extract the machine-readable packing list
	sections = fp.read().split("\n\n")
	packinglist = sections[3].split("\n")
	packinglist = packinglist[:-1] # the last line is a row of meaninless "#####"s

	# parse each line in the packing list
	filecur = {}
	for item in packinglist:
		tablename, cellsaddr = item.split("|")[:-1]
		fileno, ncells = cellsaddr.split(":")
		fileno = int(fileno)
		ncells = int(ncells)

		if fileno not in filecur:
			filecur[fileno] = 0

		yield tablename, fileno, filecur[fileno], ncells

		filecur[fileno] = filecur[fileno]+ncells
 */

public class PackingList {

	private static final int HEADER_SIZE = 20;
	public ArrayList<PackingItem> items = new ArrayList<PackingItem>();

	public PackingList(String filename) throws IOException{		
		InputStream fis = new FileInputStream(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
		
		// blow through the 21-line header
		for(int i=0; i<HEADER_SIZE; i++){
			br.readLine();
		}
		
		// read packing list items until the row of "#######"
		String line;
		while(!(line=br.readLine()).startsWith("#")){
			PackingItem pi = new PackingItem( line );
			items.add( pi );
		}
		
		br.close();
	}
	
	public Map<String,TableAddress> getTableLocations(){
		Map<String,TableAddress> ret = new HashMap<String,TableAddress>();
		
		int fileno=0;
		int offset=0;
		for(PackingItem pi : items){
			if( pi.fileno>fileno ){
				offset=0;
				fileno=pi.fileno;
			}
			ret.put(pi.tableName, new TableAddress(pi.fileno, offset, pi.ncells) );
			offset += pi.ncells;
		}
		
		return ret;
	}

}
