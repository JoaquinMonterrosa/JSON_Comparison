package com.org.quiqJson;


import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.io.FileReader;
import com.google.gson.*;
import java.util.HashMap;

/*
 * This program takes in two JSON files in the command line and compares
 * their content.  This means that it ignores things like mismatching value
 * types (ie. comparing 0.6 to "0.6".) and alias names for keys (ie. "beers" 
 * and "beer-list") during the comparison.  It evaluates these end types as
 * if they had similar JSON structure.
 * 
 * The reason for comparing the content is because I believe one of the most
 * common use cases of a program like this would be for someone to see if one
 * JSON file has, lets say, has a different value for beer color than another 
 * JSON file for that same beer
 * 
 * The output is a list of differences sorted by the first JSON keys and a 
 * similarity score between 0 and 1.
 * 
 * @author Joaquin Monterrosa
 */

public class CompareContent {
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
     * This function compares the content of two JSON files that have been
     * put into a hash map.  It prints a list of differences sorted by the first JSON keys and a 
     * similarity score between 0 and 1.
     * 
     * @param json1 - first JSON elements in hash map form
     * @param json2 - second JSON elements in hash map form
     */
    public static void compareJSON(HashMap<String, JsonElement> json1, HashMap<String, JsonElement> json2) {	
		double numSimilarC = 0;		//counts the number of similar key-value pairs ignoring aliased names like ("beers" and "beer-list") and differing value types like 0.6 and "0.6"
		double numKeysC = 0;		//counts the number of key-value pairs in the larger JSON hash table
		
		System.out.println("---------------------------------------------------------------------------------------");
		System.out.println("                              JSON Content Differences                                 ");
		System.out.println("---------------------------------------------------------------------------------------");
		System.out.println();
		System.out.printf("%-50s   %-17s   %-20s\n","JSON 1 Key", "JSON 1 Value", "JSON 2 Value");
		System.out.println();

		if (json1.size() > json2.size()) {	//need to find which hash table is bigger to make it the denominator
			for (Map.Entry<String, JsonElement> key : json1.entrySet()) {
				if(json1.get(key.getKey()).equals(json2.get(key.getKey()))) {	//evaluates if the values of each JSON hash table for a given key are the same 
					numSimilarC++;
				}
				else {
					System.out.printf("%-50s : %-17s - %-20s\n",key.getKey(), json1.get(key.getKey()), json2.get(key.getKey())); //prints out the different values for a given key
				}
				numKeysC++;
			}
		}
			else {
				for (Map.Entry<String, JsonElement> key : json2.entrySet()) {
					if(json2.get(key.getKey()).equals(json1.get(key.getKey()))) {	//evaluates if the values of each JSON hash table for a given key are the same
					numSimilarC++;
					}
					else {
						if(json1.get(key.getKey()) != null) {
							//Makes the values into strings to ignore the type differences of values so it can compare something like (0.6 and "0.6")
							String valStr1 = json1.get(key.getKey()).toString();
							String valStr2 = json2.get(key.getKey()).toString();
							if(valStr2.contains(valStr1)) {
								numSimilarC++;
							}
							else {
								System.out.printf("%-50s : %-17s - %-20s\n",key.getKey(), json1.get(key.getKey()), json2.get(key.getKey()));	//prints out the different values for a given key
							}
						}
						else {
							System.out.printf("%-50s : %-17s - %-20s\n",key.getKey(), json1.get(key.getKey()), json2.get(key.getKey()));	//prints out the different values for a given key
						}
					}
					numKeysC++;
			}
		}
		System.out.println();
		System.out.println();
		System.out.println("---------------------------------------------------------------------------------------");
		System.out.println("                              JSON Content Similarity Score                            ");
		System.out.println("---------------------------------------------------------------------------------------");
		System.out.printf("\n%s out of %s  OR  %.2f",numSimilarC, numKeysC, numSimilarC/numKeysC);	//prints the similarity score
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
        		processJSON(jarray.get(x), path + x, brewMap); //the x is just to give each different element or object path in an array a different unique path
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
    		path = checkAlias(path);
    		brewMap.put(path, elem);
    	}
    	return brewMap;
    }
    
    /*
     * This function (kinda tricky) changes any aliased elements such as "beers" and "beer-list" and turns those keys into
     * the same uniform key so that they can be compared above.  This allows the content comparison class to ignore those
     * odd aliased keys.
     * 
     * @param path is the current file path of an element
     */
    public static String checkAlias(String path) {
    	String newPath;
    	String fromAlias = "beer-list";
    	String toAlias = "beers";
    	if (path.contains(fromAlias)) {
    		newPath = path.replace(fromAlias, toAlias);
    		return newPath;
    	}
    	else {
    		return path;
    	}
    }
}

