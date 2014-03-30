package com.conveyal.disser.census;

public class TableAddress {

	int fileno;
	int offset;
	int ncells;

	public TableAddress(int fileno, int offset, int ncells) {
		this.fileno = fileno;
		this.offset = offset;
		this.ncells = ncells;
	}

}
