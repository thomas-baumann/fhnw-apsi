package lab1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class StartPermutation {

	public static void main(String[] args) throws IOException {
		int count = 5;
		int countFound = 0;
		TextGenerator tg = new TextGenerator("rsc/Patterns.txt", true);
		TextGenerator tgOrg = new TextGenerator("rsc/Patterns_Org.txt", true);
		HashMap<Integer, String> hashesFake = new HashMap<Integer, String>();
		HashMap<Integer, String> hashesOrg = new HashMap<Integer, String>();
		HashMap<Integer, ArrayList<String>> hashesFound = new HashMap<Integer, ArrayList<String>>();
		
		DESHash hashFunction = new DESHash();
		do {
			// Fill up new hashes
			for (int i = 0; i < 2048; i++) {
				String text = tg.getNextText();
				int hash = hashFunction.hash(text);
				hashesFake.put(hash, text);
				
				String text2 = tgOrg.getNextText();
				int hash2 = hashFunction.hash(text2);
				hashesOrg.put(hash2, text2);
			}
			// Collision detection
			Iterator<Integer> it = hashesFake.keySet().iterator();
			while(it.hasNext()) {
				int h = it.next();
				if (hashesOrg.containsKey(h)) {
					if (hashesFound.containsKey(h)) {
						ArrayList<String> al = hashesFound.get(h);
						String t = hashesFake.get(h);
						String t2 = hashesOrg.get(h);
						if (!al.contains(t)) {
							countFound++;
							al.add(t);
						}
						if (!al.contains(t2)) {
							al.add(t2);
						}
						hashesFound.put(h, al);
					} else {
						countFound++;
						ArrayList<String> al = new ArrayList<String>();
						al.add(hashesOrg.get(h));
						al.add(hashesFake.get(h));
						hashesFound.put(h, al);
					}
				}
			}
		} while (count != countFound);
		
		// Show us the hashes
		Iterator<Integer> it = hashesFound.keySet().iterator();
		while (it.hasNext()) {
			int h = it.next();
			ArrayList<String> al = hashesFound.get(h);
			System.out.println("Found Hash: " + h);
			System.out.println("Strings mapping to that Hash: ");
			for (String string : al) {
				System.out.println(string);
			}
			System.out.println("--------------------------------------------");
		}
		

	}
}