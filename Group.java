package com.venkat.concurrency;
/*
 * This is an enum to have the three groups
 * We can easily expand this to have more groups 
 * gives a way to validate input files and enforce 
 * that the files with one of hte groups strings are considered valid
 */
public enum Group {
	GROUP1("group1"),
	GROUP2("group2"), 
	GROUP3("group3"); 

	String grpId;
	Group(String grpId){
		this.grpId = grpId;
	}
	public String getGroupID() {
		return grpId;
	}
	public static boolean isValid(String name) {
		for(Group x: Group.values()){
			String xstr = x.getGroupID();
			if (name.indexOf(xstr) != -1)
				return true;
		}
		return false;
	}
}