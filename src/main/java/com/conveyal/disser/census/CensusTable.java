package com.conveyal.disser.census;

import java.util.HashMap;
import java.util.Map;

public class CensusTable {

	public String tableName;
	public Map<String,CensusRecord> records = new HashMap<String,CensusRecord>();

	public CensusTable(String tableName) {
		this.tableName = tableName;
	}

	public void addRecord(CensusRecord cr) {
		records.put(cr.logrecno, cr);
	}
	
	public CensusRecord getRecord(String logrecno){
		return records.get(logrecno);
	}

}
