package model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import utilities.TermTFIDF;
import utilities.Window;

public class TFIDFTable {
	// ASSUMPTION UNIQUETARGETTERMS MUST BE SORTED
	private HashMap<Integer, ArrayList<TermTFIDF>> docTFIDF;
	private List<Window> uniqueTargetTerms;
	private double [][] DocumentSpaceMatrix;
	public HashMap<Integer, ArrayList<TermTFIDF>> getDocTFIDF() {
		return docTFIDF;
	}
	public void setDocTFIDF(HashMap<Integer, ArrayList<TermTFIDF>> docTFIDF) {
		this.docTFIDF = docTFIDF;
	}
	public List<Window> getUniqueTargetTerms() {
		return uniqueTargetTerms;
	}
	public void setUniqueTargetTerms(List<Window> uniqueTargetTerms) {
		this.uniqueTargetTerms = uniqueTargetTerms;
	}
	public double[][] getDocumentSpaceMatrix() {
		return DocumentSpaceMatrix;
	}
	public void setDocumentSpaceMatrix(double[][] documentSpaceMatrix) {
		DocumentSpaceMatrix = documentSpaceMatrix;
	}
	
	public void refine() {
		DocumentSpaceMatrix = new double[uniqueTargetTerms.size()][docTFIDF.size()];
		for(int i = 0; i < DocumentSpaceMatrix[0].length; i++) {
			HashMap<String, Double> keys = new HashMap<String, Double>();
			ArrayList<TermTFIDF> terms = docTFIDF.get(i);
			for(int p = 0; p < terms.size(); p++) {
				TermTFIDF t = terms.get(p);
				keys.put(t.term.toLowerCase(), t.tfidfValue);
			}
			for( int j = 0; j < uniqueTargetTerms.size(); j++) {
				Window term2 = uniqueTargetTerms.get(j);
				Double value = keys.get(term2.getTargetTerm().toLowerCase());
				if(value == null) {
					DocumentSpaceMatrix[j][i] = 0;
				}
				else {
					DocumentSpaceMatrix[j][i] = value.doubleValue();
				}
			}
		}
	}
	public static void main(String args[]) {
		HashMap<Integer, ArrayList<TermTFIDF>> a = new HashMap<Integer, ArrayList<TermTFIDF>>();
		ArrayList<TermTFIDF> b = new ArrayList<TermTFIDF>();
		ArrayList<TermTFIDF> c = new ArrayList<TermTFIDF>();
		TermTFIDF term1 = new TermTFIDF("apple", 5);
		TermTFIDF term2 = new TermTFIDF("apple", 2);
		TermTFIDF term3 = new TermTFIDF("apple", 1);
		TermTFIDF term4 = new TermTFIDF("apple", 3);
		b.add(term1);
		b.add(term2);
		c.add(term3);
		c.add(term4);
		a.put(0, b);
		a.put(1, c);
		
		List<Window> unique = new ArrayList<Window>();
		Window win = new Window();
		win.setTargetTerm("apple");
		unique.add(win);
		TFIDFTable tf = new TFIDFTable();
		tf.setDocTFIDF(a);
		tf.setUniqueTargetTerms(unique);
		tf.refine();
		double[][] matrix = tf.getDocumentSpaceMatrix();
		for(int i = 0; i < matrix.length; i++) {
			for(int j = 0; j < matrix[0].length; j++) {
				System.out.print(matrix[i][j]+" ");
			}
		}
		
	}
}
