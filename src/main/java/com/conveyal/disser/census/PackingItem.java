package com.conveyal.disser.census;

class PackingItem{
	/* A packing item is a row in the packing list in the form table_name|file_number:num_fields|.
	 * For example:
	 * 
	 * p5|3:17|
	 * 
	 * Note that this packing item is useless without context within the packing list, because the packing
	 * list doesn't specify the offset of the fields representing a table; only the length. The address
	 * is inferred from the position of the packing item in the list.
	 */
	
	String tableName;
	int fileno;
	int ncells;

	PackingItem(String row){
		String[] fields = row.split("\\|");
		this.tableName = fields[0];
		String[] loc = fields[1].split(":");
		this.fileno = Integer.parseInt(loc[0]);
		this.ncells = Integer.parseInt(loc[1]);
	}
}