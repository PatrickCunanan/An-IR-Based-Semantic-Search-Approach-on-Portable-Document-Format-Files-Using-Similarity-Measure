package model;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import utilities.PDFtoString;
import utilities.Remover;
import utilities.Window;
import utilities.Stemmer;
import utilities.VectorTFIDF;

public class ConceptVector {
	private List<String> ConceptSpace;
	private List<List<String>> cSpace;
	private double[][] TFIDFTable;
	private List<Window> Terms;
	private String wordNetPath, dict, path;
	private IDictionary dictionary;
	private URL url;
	private String logMessage;

	public List<String> getConceptSpace() {
		return ConceptSpace;
	}

	public void setConceptSpace(List<String> conceptSpace) {
		ConceptSpace = conceptSpace;
	}

	public double[][] getTFIDFTable() {
		return TFIDFTable;
	}

	public void setTFIDFTable(double[][] tFIDFTable) {
		TFIDFTable = tFIDFTable;
	}

	public List<Window> getTerms() {
		return Terms;
	}

	public void setTerms(List<Window> terms) {
		Terms = terms;
	}

	public String getLogMessage() {
		return logMessage;
	}

	public void setLogMessage(String logMessage) {
		this.logMessage = logMessage;
	}

	// constructor
	public ConceptVector(String wordNetPath, List<String> ConceptSpace) {
		this.ConceptSpace = ConceptSpace;
		this.wordNetPath = wordNetPath;
		this.dict = "dict";
		this.path = this.wordNetPath + File.separator + this.dict;
		try {
			this.url = new URL("File", null, this.path);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		dictionary = new Dictionary(this.url);
		try {
			dictionary.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			cSpace = convertToPDF();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// convert concept space's pdf files to string which does not have symbols and
	// is stemmed
	public List<List<String>> convertToPDF() throws IOException {
		System.out.println("Conversion of PDF to clean tokens");
		List<List<String>> ListHolder = new ArrayList<List<String>>();
		for (String concept : ConceptSpace) {
			String conceptHolder = PDFtoString.extractPdfText(concept);
			String noSymbolConcept = Remover.removeSymbols(conceptHolder);
			String cleanConcept = Stemmer.stemIt(noSymbolConcept);	
			StringTokenizer cleanConceptTokens = new StringTokenizer(cleanConcept);
			List<String> tokens = new ArrayList<String>();
			while (cleanConceptTokens.hasMoreElements()) {
				tokens.add((String) cleanConceptTokens.nextElement());
			}
			
			ListHolder.add(tokens);
		}
		System.out.println("Process done.");
		return ListHolder;
	}

	// start merge sort
	public List<Window> mergeSortWindow(List<Window> cWindow) {
		List<Window> sortedWindow;
		if (cWindow.size() == 1) {
			sortedWindow = cWindow;
		} else {
			int mid = cWindow.size() / 2;

			List<Window> leftSide = new ArrayList<Window>();
			List<Window> rightSide = new ArrayList<Window>();

			for (int i = 0; i < mid; i++) {
				leftSide.add(cWindow.get(i));

			}
			for (int i = mid; i < cWindow.size(); i++) {
				rightSide.add(cWindow.get(i));
			}

			leftSide = mergeSortWindow(leftSide);
			rightSide = mergeSortWindow(rightSide);
			sortedWindow = mergeLeftRight(leftSide, rightSide);
		}

		return sortedWindow;
	}

	public List<Window> mergeLeftRight(List<Window> left, List<Window> right) {
		List<Window> merged = new ArrayList<Window>();

		int l = 0;
		int r = 0;

		while (l < left.size() && r < right.size()) {
			if ((left.get(l).getTargetTerm()).compareToIgnoreCase(right.get(r).getTargetTerm()) < 0) {
				merged.add(left.get(l));
				l++;
			} else {
				merged.add(right.get(r));
				r++;
			}
		}

		while (l < left.size()) {
			merged.add(left.get(l));
			l++;
		}
		while (r < right.size()) {
			merged.add(right.get(r));
			r++;
		}

		return merged;
	}
	// end merge sort

	public static double[] summary(int start, int end, double[][] matrix) {
		double[] newRow = new double[matrix[0].length];
		for (int ci = 0; ci < matrix[0].length; ci++) {
			double max = -9999;
			for (int i = start; i < end; i++) {
				if (max < matrix[i][ci]) {
					max = matrix[i][ci];
				}
				newRow[ci] = max;
			}
		}

		return newRow;
	}

	public double getAverage(List<Double> weights) {
		double sum = 0;
		double size = weights.size();
		double average;

		for (double weight : weights) {
			sum += weight;
		}

		average = sum / size;

		return average;
	}

	public double[][][] createVector() {
		StringBuilder log = new StringBuilder("");
		VectorTFIDF v = new VectorTFIDF();
		List<Window> sortedTerms = mergeSortWindow(Terms);
		System.out.println("List<Window> Terms sorted.");
		double[][] conceptSpaceMatrix = new double[sortedTerms.size()][cSpace.size()];

		int termInc = 0;
		int conceptInc = 0;
		int remaining = sortedTerms.size() * cSpace.size();
		System.out.println("Starting double[][] ConceptSpaceMatrix Computation. Dimension:[" + sortedTerms.size() + "]["
				+ cSpace.size() + "] number of computations: " + remaining);
		//log.append("Starting double[][] ConceptSpaceMatrix Computation. Dimension:[" + sortedTerms.size() + "]["
		//		+ cSpace.size() + "] number of computations: " + remaining + " \r\n");
		for (List<String> concept : cSpace) {
			for (Window sTerms : sortedTerms) {
				List<Double> scores = new ArrayList<Double>();
				remaining--;
				double aveScore = 0;
				if(sTerms.getSurroundingTerms().length == 0) {
					conceptSpaceMatrix[termInc][conceptInc] = aveScore;
				}else{
					for (String term : sTerms.getSurroundingTerms()) {
						double score = 0;
						score = v.TFIDFCompute(concept, cSpace, term);
						scores.add(score);
					}
					aveScore = getAverage(scores);
					conceptSpaceMatrix[termInc][conceptInc] = aveScore;
				}				
				System.out.println("Average TFIDF Score of surrounding terms: " + aveScore);
				//log.append("Average TFIDF Score of surrounding terms: " + aveScore + " \r\n");
				System.out.println("remaining computations: " + remaining);
				//log.append("remaining computations: " + remaining + " \r\n");
				termInc++;
			}
			termInc = 0;
			conceptInc++;
		}
		System.out.println("double[][] ConceptSpaceMatrix computed.");
		//log.append("double[][] ConceptSpaceMatrix computed. \r\n");

		double[][] nConceptSpaceMatrix = new double[conceptSpaceMatrix.length][conceptSpaceMatrix[0].length];
		remaining = sortedTerms.size();
		System.out
				.println("Starting double[][] nConceptSpaceMatrix Computation. Dimension:[" + conceptSpaceMatrix.length
						+ "][" + conceptSpaceMatrix[0].length + "] number of computations: " + remaining);
		//log.append("Starting double[][] nConceptSpaceMatrix Computation. Dimension:[" + conceptSpaceMatrix.length + "]["
		//		+ conceptSpaceMatrix[0].length + "] number of computations: " + remaining + " \r\n");
		int inc = 0;
		for (int wi = 0; wi < sortedTerms.size(); wi++) {
			remaining--;
			int k = wi + 1;
			while (k < sortedTerms.size()
					&& sortedTerms.get(k).getTargetTerm().equalsIgnoreCase(sortedTerms.get(wi).getTargetTerm())) {
				k++;
			}
			if (wi == sortedTerms.size() - 1) {
				nConceptSpaceMatrix[inc] = conceptSpaceMatrix[wi];
			} else if (k == (wi + 1)) {
				if (sortedTerms.get(k).getTargetTerm().equalsIgnoreCase(sortedTerms.get(wi).getTargetTerm())) {
					double[] newRow = summary(wi, k, conceptSpaceMatrix);
					nConceptSpaceMatrix[inc] = newRow;
				} else {
					nConceptSpaceMatrix[inc] = conceptSpaceMatrix[wi];
				}
			} else {
				double[] newRow = summary(wi, k, conceptSpaceMatrix);
				nConceptSpaceMatrix[inc] = newRow;
			}
			inc++;
			System.out.println("remaining computations: " + remaining);
			//log.append("remaining computations: " + remaining + " \r\n");
		}
		System.out.println("double[][] nConceptSpaceMatrix computed.");
		//log.append("double[][] nConceptSpaceMatrix computed. \r\n");

		double[][][] Vector = new double[TFIDFTable[0].length][TFIDFTable.length][cSpace.size()];
		remaining = TFIDFTable[0].length * TFIDFTable.length * cSpace.size();
		System.out.println("Starting double[][][] Vector Computation. Dimension:[" + TFIDFTable[0].length + "]["
				+ TFIDFTable.length + "][" + cSpace.size() + "] number of computations: " + remaining);
		//log.append("Starting double[][][] Vector Computation. Dimension:[" + TFIDFTable[0].length + "]["
		//		+ TFIDFTable.length + "][" + cSpace.size() + "] number of computations: " + remaining + " \r\n");

		for (int di = 0; di < TFIDFTable[0].length; di++) {
			for (int ci = 0; ci < cSpace.size(); ci++) {
				for (int ti = 0; ti < TFIDFTable.length; ti++) {
					remaining--;
					if(TFIDFTable[ti][di] == 0 || nConceptSpaceMatrix[ti][ci] == 0) {
						Vector[di][ti][ci] = 0;
					}
					else {
						Vector[di][ti][ci] = TFIDFTable[ti][di] + nConceptSpaceMatrix[ti][ci];
					}
					System.out.println("Vector[" + di + "][" + ti + "][" + ci + "] score: " + Vector[di][ti][ci]);
					//log.append("Vector[" + di + "][" + ti + "][" + ci + "] score: " + Vector[di][ti][ci] + "\r\n");
					System.out.println("remaining computations: " + remaining);
					//log.append("remaining computations: " + remaining + " \r\n");
				}
			}
		}
		System.out.println("double[][][] Vector computed.");
		//log.append("double[][][] Vector computed. \r\n");
		System.out.println("Concept Vector Process Finished.");
		//log.append("Concept Vector Process Finished.\r\n");

		//setLogMessage(log.toString());

		return Vector;
	}
}
