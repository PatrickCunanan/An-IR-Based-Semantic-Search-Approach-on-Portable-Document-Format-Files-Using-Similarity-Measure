package utilities;

import java.util.ArrayList;

public class Pdf {
	public String docPath;
	public ArrayList<String> concepts;

	public Pdf(String d, ArrayList<String> c) {
		docPath = d;
		concepts = c;
	}

	public String toString() {
		String s = "DOCUMENT: " + docPath + " CONCEPTS: [";
		for (int i = 0; i < concepts.size(); i++) {
			if (i == concepts.size() - 1)
				s += concepts.get(i)+"]";
			else
				s += concepts.get(i) + ", ";
		}
		return s;
	}
	
	public String toString2() {
		String s = docPath + "###";
		for (int i = 0; i < concepts.size(); i++) {
			if (i == concepts.size() - 1)
				s += concepts.get(i);
			else
				s += concepts.get(i) + "###";
		}
		return s;
	}
}
