package com.conveyal.disser.census;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class SummaryFile {
	private static final int NHEADERFIELDS = 5;
	Map<String, TableAddress> tableLocs;
	String basedir;
	String stateabbrev;
	int year;

	public SummaryFile(String basedir, String stateabbrev, int year) throws IOException {
		this.basedir=basedir;
		this.stateabbrev=stateabbrev;
		this.year=year;
		
		File bd = new File(basedir);
		File ff = new File(bd, stateabbrev+year+".sf1.prd.packinglist.txt");
		
        PackingList pl = new PackingList(ff.getPath());
        tableLocs = pl.getTableLocations();
    }

	public CensusTable getTable(String tableName) throws IOException {
		TableAddress ta = tableLocs.get(tableName);
		if(ta==null){
			return null;
		}
		
		// construct filename
		File bd = new File(basedir);
		String fileno = String.format("%05d", ta.fileno);
		File ff = new File(bd, stateabbrev+fileno+year+".sf1");
		String filename = ff.getPath();
		
		// open the file
		InputStream fis = new FileInputStream(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
		
		// read in the range of fields that carry our table
		CensusTable ret = new CensusTable(tableName);
		String line;
		while((line=br.readLine())!=null){
			
			String[] fileFields = line.split(",");
			
			String fileid = fileFields[0];
			String stusab = fileFields[1];
			String chariter = fileFields[2];
			String cifsn = fileFields[3];
			String logrecno = fileFields[4];
			String[] tableFields = Arrays.copyOfRange(fileFields, NHEADERFIELDS+ta.offset, NHEADERFIELDS+ta.offset+ta.ncells);
			ret.addRecord( new CensusRecord(fileid, stusab, chariter, cifsn, logrecno, tableFields) );

		}
		
		br.close();
		
		return ret;
	}

}
