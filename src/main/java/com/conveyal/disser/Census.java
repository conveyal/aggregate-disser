package com.conveyal.disser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class Census {
	class GeoTable{
		class FieldDefinition{
			String name;
			int start;
			int len;
			
			FieldDefinition(String name, int len, int start){
				this.name = name;
				this.start = start;
				this.len = len;
			}
		}
		
		FieldDefinition[] format = {
				new FieldDefinition("FILEID",6,1),
				new FieldDefinition("STUSAB",2,7),
				new FieldDefinition("SUMLEV",3,9),
				new FieldDefinition("GEOCOMP",2,12),
				new FieldDefinition("CHARITER",3,14),
				new FieldDefinition("CIFSN",2,17),
				new FieldDefinition("LOGRECNO",7,19),
				new FieldDefinition("REGION",1,26),
				new FieldDefinition("DIVISION",1,27),
				new FieldDefinition("STATE",2,28),
				new FieldDefinition("COUNTY",3,30),
				new FieldDefinition("COUNTYCC",2,33),
				new FieldDefinition("COUNTYSC",2,35),
				new FieldDefinition("COUSUB",5,37),
				new FieldDefinition("COUSUBCC",2,42),
				new FieldDefinition("COUSUBSC",2,44),
				new FieldDefinition("PLACE",5,46),
				new FieldDefinition("PLACECC",2,51),
				new FieldDefinition("PLACESC",2,53),
				new FieldDefinition("TRACT",6,55),
				new FieldDefinition("BLKGRP",1,61),
				new FieldDefinition("BLOCK",4,62)
		};
		
		BufferedReader br;
		
		GeoTable(String filename) throws FileNotFoundException{
			InputStream fis;
			
			fis = new FileInputStream(filename);
			br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
			
		}
		
		String nextLine(){
			if(br==null){
				return null;
			}
			
			try {
				return br.readLine();
			} catch (IOException e) {
				return null;
			}
		}
		
		Map<String,String> nextRecord(){
			HashMap<String,String> ret = new HashMap<String,String>();
			
			String line = this.nextLine();
			if(line==null){
				return null;
			}
			
			for( FieldDefinition fd : this.format ){
				String val = line.substring(fd.start-1,fd.start-1+fd.len).trim();
				ret.put( fd.name, val );
			}
			return ret;
		}
		
		Map<String,String> getAllLogRecNos(){
			// return map of GEOID10 -> LOGRECNO
			HashMap<String,String> ret = new HashMap<String,String>();
			
			Map<String,String> nextRec;
			while((nextRec=this.nextRecord())!=null){
				String block = nextRec.get("BLOCK");
				if(block.equals("")){
					continue;
				}
				
				String geoid10 = nextRec.get("STATE")+nextRec.get("COUNTY")+nextRec.get("TRACT")+nextRec.get("BLOCK");
				ret.put( geoid10, nextRec.get("LOGRECNO") );
			}
			
			return ret;
		}
	}
}
