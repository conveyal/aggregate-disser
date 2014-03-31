package com.conveyal.disser.census;

public class CensusRecord {

	String[] tableFields;
	String fileid;
	String stusab;
	String chariter;
	String cifsn;
	String logrecno;

	public CensusRecord(String fileid, String stusab, String chariter, String cifsn, String logrecno,
			String[] tableFields) {
		this.fileid = fileid;
		this.stusab = stusab;
		this.chariter = chariter;
		this.cifsn = cifsn;
		this.logrecno = logrecno;
		this.tableFields = tableFields;
	}
	
	public String toString(){
		String ret = logrecno+": [";
		if(tableFields.length>0){
			ret += tableFields[0];
		}
		for(int i=1; i<tableFields.length; i++){
			ret += ", "+tableFields[i];
		}
		ret += "]";
		return ret;
	}

	public int getCensusIntField(int i) {
		// census table fields are 1-indexed, so decrement
		i -= 1;
		
		if( i<0 || i>=tableFields.length ){
			throw new ArrayIndexOutOfBoundsException(i+1);
		}
		
		return Integer.parseInt(tableFields[i]);
	}

}
