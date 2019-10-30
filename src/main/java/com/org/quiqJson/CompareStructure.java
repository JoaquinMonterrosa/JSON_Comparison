package com.org.quiqJson;


import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.io.FileReader;
import com.google.gson.*;
import java.util.HashMap;

/*
 * This program takes in two JSON files in the command line and compares
 * their structure.  This means that it this comparison checks nit-picky
 * things like mismatching value types (ie. comparing 0.6 to "0.6".) and 
 * alias names for keys (ie. "beers" and "beer-list") during the comparison.
 * 
 * The reason for a structural comparison is because I thought another common
 * use case would be for automatically checking JSON structure so that a program
 * could validate similar JSON structure before manipulating or adding bad
 * data into the program.
 * 
 * The output is a list of differences sorted by the first JSON keys and a 
 * similarity score between 0 and 1.
 * 
 * @author Joaquin Monterrosa
 */
public class CompareStructure {
    public static void main( String[] args ){
    	
    	try {
    		HashMap<String, JsonElement> brewTable = new HashMap<String, JsonElement>();
    		HashMap<String, JsonElement> brewTable2 = new HashMap<String, JsonElement>();
    		
    		JsonElement jelement = new JsonParser().parse(new FileReader(args[0]));
    		JsonElement jelement2 = new JsonParser().parse(new FileReader(args[1]));
    		
    		processJSON(jelement, "", brewTable);
    		processJSON(jelement2, "", brewTable2);
    		
    		compareJSON(brewTable, brewTable2);
    		
    		

    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
    /*
     * This function compares the structure of two JSON files that have been
     * put into a hash map.  It prints a list of differences sorted by the first JSON keys and a 
     * similarity score between 0 and 1.
     * 
     * @param json1 - first JSON elements in hash map form
     * @param json2 - second JSON elements in hash map form
     */
    public static void compareJSON(HashMap<String, JsonElement> json1, HashMap<String, JsonElement> json2) {
    	double numSimilarS = 0;	//counts the number of similar key-value pairs
		double numKeysS = 0;	//counts the number of key-value pairs in the larger JSON hash table
		
		System.out.println("---------------------------------------------------------------------------------------");
		System.out.println("                                JSON Structure Differences                             ");
		System.out.println("---------------------------------------------------------------------------------------");
		System.out.println();
		System.out.printf("%-50s   %-17s   %-20s\n","JSON 1 Key", "JSON 1 Value", "JSON 2 Value");
		System.out.println();
		
		if (json1.size() > json2.size()) {	//need to find which hash table is bigger to make it the denominator
			for (Map.Entry<String, JsonElement> key : json1.entrySet()) {
				if(json1.get(key.getKey()).equals(json2.get(key.getKey()))) {	//evaluates if the values of each JSON hash table for a given key are the same
					numSimilarS++;
				}
				else {
					System.out.printf("%-50s : %-17s - %-20s\n",key.getKey(), json1.get(key.getKey()), json2.get(key.getKey()));	//prints out the different values for a given key
				}
				numKeysS++;
			}
		}
		else {
			for (Map.Entry<String, JsonElement> key : json2.entrySet()) {
				if(json2.get(key.getKey()).equals(json1.get(key.getKey()))) {	//evaluates if the values of each JSON hash table for a given key are the same
					numSimilarS++;
				}
				else {
					System.out.printf("%-50s : %-17s - %-20s\n",key.getKey(), json1.get(key.getKey()), json2.get(key.getKey())); //prints out the different values for a given key
				}
				numKeysS++;
			}
		}
		System.out.println();
		System.out.println();
		System.out.println("---------------------------------------------------------------------------------------");
		System.out.println("                            JSON Sturcture Similarity Score                            ");
		System.out.println("---------------------------------------------------------------------------------------");
		System.out.printf("\n%s out of %s  OR  %.2f",numSimilarS, numKeysS, numSimilarS/numKeysS);

		}

    /*
     * This is a recursive function that puts all the parsed elements of a JSON file into a hash map
     * 
     * @param elem is the JSON element
     * @param path is the key path used to find the value
     * @param brewMap is the hash map that the parsed JSON elements will be put into
     * 
     * @return a hash map with the parsed JSON elements
     */
    public static HashMap<String, JsonElement> processJSON(JsonElement elem, String path, HashMap<String, JsonElement> brewMap) {
    	
    	//Deals with JSON Arrays and extracts the elements within the array
    	if (elem.getClass().toString().endsWith("JsonArray")) {
    		JsonArray jarray = elem.getAsJsonArray();
        	for(int x = 0; x < jarray.size(); x++) {
        		processJSON(jarray.get(x), path + x, brewMap);	//the x is just to give each different element or object path in an array a different unique path
        	}
    	}
    	//Deals with nested JSON Objects and extracts the elements within the nested object
    	else if (elem.getClass().toString().endsWith("JsonObject")) {
	    	JsonObject  jobject = elem.getAsJsonObject();
			Set<Entry<String, JsonElement>> entries = jobject.entrySet();
			for (Map.Entry<String, JsonElement> entry: entries) {
				//So that it doesn't print a space at the beginning of the path
				if (path == "") {
					processJSON(entry.getValue(), path + entry.getKey(), brewMap);
				}
				else {
					processJSON(entry.getValue(), path + " " + entry.getKey(), brewMap);
				}
			}
		}
    	else {
    		brewMap.put(path, elem);
    	}
    	return brewMap;
    }
}

