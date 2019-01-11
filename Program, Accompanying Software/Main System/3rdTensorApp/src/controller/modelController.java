package controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import model.ConceptSpacesModel;
import model.ConceptVector;
import model.ConceptWindow;
import model.SuperMatrix;
import model.TFIDFTable;
import utilities.Item;
import utilities.MergeSort;
import utilities.Reader;
import utilities.Stemmer;
import utilities.TermTFIDF;
import utilities.Window;
import utilities.extractPdfText;
public class modelController {
	// paths
	private String conceptIPath, documentIPath, wordNetPath, stopWordPath, conceptSpaceIPath;
	private int alpha; 
	// resources
		// concept Space
	private ArrayList<String> documentPaths;
	private ArrayList<String> conceptPaths;
	private HashMap<Integer, ArrayList<TermTFIDF>> tf_idfTable;
		// concept window
	private HashSet<String> stopWords;
	private List<Window> uniqueTermsPerDoc, originalTermsPerDoc;
		// tfidf table
	private double[][] documentMatrix;
		// concept vector
	private double[][][] ThirdTensor;
		// super matrix
	private double[][] super_Matrix;
	
	public String getConceptIPath() {
		return conceptIPath;
	}
	public void setConceptIPath(String conceptIPath) {
		this.conceptIPath = conceptIPath;
	}
	public String getDocumentIPath() {
		return documentIPath;
	}
	public void setDocumentIPath(String documentIPath) {
		this.documentIPath = documentIPath;
	}
	public String getWordNetPath() {
		return wordNetPath;
	}
	public void setWordNetPath(String wordNetPath) {
		this.wordNetPath = wordNetPath;
	}
	public String getStopWordPath() {
		return stopWordPath;
	}
	public void setStopWordPath(String stopWordPath) {
		this.stopWordPath = stopWordPath;
	}
	public int getAlpha() {
		return alpha;
	}
	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}
	public String getConceptSpaceIPath() {
		return conceptSpaceIPath;
	}
	public void setConceptSpaceIPath(String conceptSpaceIPath) {
		this.conceptSpaceIPath = conceptSpaceIPath;
	}

	public double[][] getDocumentMatrix() {
		return documentMatrix;
	}
	public void setDocumentMatrix(double[][] documentMatrix) {
		this.documentMatrix = documentMatrix;
	}
	public double[][][] getThirdTensor() {
		return ThirdTensor;
	}
	public void setThirdTensor(double[][][] thirdTensor) {
		ThirdTensor = thirdTensor;
	}

	private ConceptSpacesModel conceptSpace;
	private ConceptWindow conceptWindow;
	private ConceptVector conceptVector;
	private SuperMatrix superMatrix;
	private extractPdfText extractor;
	private MergeSort<Window> mergeSort;
	private TFIDFTable tfidfTable;
	public void display3DMatrix(double[][][] tensor, int maxd, int maxt, int maxc, String label) {
		System.out.println(label.toUpperCase());
		System.out.println("OUTPUT: ");
		for(int i = 0; i <= maxd; i++) {
			System.out.println("DOCUMENT# "+i+", 2D MATRIX");
			for(int j = 0; j <= maxt; j++) {
				for(int k = 0; k <= maxc ; k++) {
					System.out.printf("[%5d][%5d][%5d]:%5.3f\t", i,j,k,tensor[i][j][k]);
				}
				System.out.println();
			}
			System.out.println();
		}
		System.out.println("END OF OUTPUT");
		System.out.println();
	}
	public void display2DMatrix(double [][] matrix, int maxt,int maxD ,String label) {
		System.out.println(label.toUpperCase());
		System.out.println("OUTPUT: ");
		for(int i = 0; i <= maxt; i++) {
			for(int j = 0; j <= maxD; j++) {
				System.out.printf("[%5d][%5d]:%5.3f\t", i,j,matrix[i][j]);
			}
			System.out.println();
		}
		System.out.println("END OF OUTPUT");
		System.out.println();
	}
	public void displayWindow(List<Window> a, String label) {
		System.out.println(label.toUpperCase());
		System.out.println("OUTPUT: ");
		int i = 0;
		for(Window b: a) {
			String[] surround = b.getSurroundingTerms();
			System.out.printf("Target term: %25s", b.getTargetTerm()+" Window:");
			for(int j = 0; j < surround.length; j++) {
				if(j == surround.length - 1) {
					System.out.printf("%25s",surround[j]);
				}
				else {
					System.out.printf("%25s,",surround[j]);
				}
			}
			System.out.println();
		}
		System.out.println("END OF OUTPUT");
		System.out.println();
	}
	public void displayHashWithArray(HashMap<Integer, ArrayList<TermTFIDF>> a, String label) {
		System.out.println(label.toUpperCase());
		System.out.println("OUTPUT: ");
		int i = 0;
		for(ArrayList<TermTFIDF> b: a.values()) {
			System.out.print("DOCUMENT# "+i+"   ");
			for(TermTFIDF c: b) {
				System.out.printf("%16s[%.3f]", c.term, c.tfidfValue);
			}
			System.out.println();
			i++;
		}
		System.out.println("END OF OUTPUT");
		System.out.println();
	}
	public void displayListString(ArrayList<String> a, String label) {
		System.out.println(label.toUpperCase());
		System.out.println("OUTPUT: ");
		for(String b : a) {
			System.out.println("\t"+b);
		}
		System.out.println("END OF OUTPUT");
		System.out.println("");
	}
	public void rewrite() {
		System.out.println("Rewriting Model.....................");
		System.out.println("\tSorting Model.....................");
		ThirdTensor = sortTensor(ThirdTensor);
		System.out.println("\tSorting Model Done................");
			display3DMatrix(ThirdTensor,2,ThirdTensor[0].length / 2, ThirdTensor[0][0].length / 2, "SORTED THIRD ORDER TENSOR MODEL");
		System.out.println("\tConstructing super matrix.........");
		superMatrix = new SuperMatrix();
		superMatrix.setThirdOrderTensor(ThirdTensor);
		superMatrix.buildSuperMatrix();
		super_Matrix = superMatrix.getSuperMatrix();
		System.out.println("\tConstructing super matrix done....");
			display2DMatrix(super_Matrix,super_Matrix.length / 2, super_Matrix[0].length / 2,"SORTED SUPER MATRIX");
		System.out.println("\tCaching out the models............");
		outputThirdTensor();
		outputSuperMatrix();
		System.out.println("\tCaching done......................");
		System.out.println("Rewriting Model done................");
	}
	public void create() {
		// time variable
		double b, a;
		b = System.nanoTime();
		Stemmer.initialize(wordNetPath);
		// prepare conceptSpace
		System.out.println("Concept space construction...................");
		conceptSpace = new ConceptSpacesModel(16570);
			//INPUT BEGIN
		conceptSpace.setConceptIndexPath(conceptIPath);
		conceptSpace.setDocsIndexPath(documentIPath);
		conceptSpace.setAlpha(alpha);
		conceptSpace.setConceptSpaceIndexPath(conceptSpaceIPath);
			//INPUT END
		try {
			conceptSpace.conceptSpaceConstruction();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
			//OUTPUT BEGIN
		conceptPaths = conceptSpace.getConceptPath();
		documentPaths = conceptSpace.getDocPath();
		tf_idfTable = conceptSpace.getDocTFIDF();
			//OUTPUT END
			//DISPLAY OUTPUT
			displayListString(conceptPaths,"CONCEPT SPACE");
			displayListString(documentPaths,"LIST OF DOCUMENTS");
			displayHashWithArray(tf_idfTable, "DOCUMENT MATRIX");
		System.out.println("End concept space construction...............");

		System.out.println("Concept window construction..................");
		stopWords = stopWordsReader();
		extractor = new extractPdfText();
		List<String> documents = new ArrayList<String>(documentPaths.size());
		System.out.println("Preparing Documents...");
		int j = 0;
		for(String docPath: documentPaths) {
			System.out.println("Extracting Document #: "+j+" total: "+documents.size());
			extractor.setPath(docPath);
			String document = "";
			try {
				document = extractor.extractPdfText();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
			documents.add(document);
			j++;
		}
		conceptWindow = new ConceptWindow(wordNetPath);
			//INPUT
		conceptWindow.setStopWords(stopWords);
		for( int i = 0; i < documents.size(); i++) {
			double before = System.nanoTime();
			System.out.println("Document #: "+i+" total: "+documents.size());
				//INPUT
			conceptWindow.create(documents.get(i));
			double after = (System.nanoTime() - before) / 1000000000.0;
			System.out.println("Time processed: "+after+"s");
		}
			//OUTPUT
		uniqueTermsPerDoc = conceptWindow.getUniqueTargetTerms();
		originalTermsPerDoc = conceptWindow.getTargetTerms();
			//DISPLAY
			displayWindow(uniqueTermsPerDoc, "UNIQUE LIST OF CONCEPT WINDOWS");
			displayWindow(originalTermsPerDoc, "NON-UNIQUE LIST OF CONCEPT WINDOWS");
		System.out.println("End concept window construction.............");
		
		// sort unique
		mergeSort = new MergeSort<Window>();
		mergeSort.setOrder(mergeSort.ASCENDING);
		uniqueTermsPerDoc = mergeSort.mergesort(uniqueTermsPerDoc);
			
		tfidfTable = new TFIDFTable();
			//INPUT
		tfidfTable.setDocTFIDF(tf_idfTable);
		tfidfTable.setUniqueTargetTerms(uniqueTermsPerDoc);
		tfidfTable.refine();
			//OUTPUT
		documentMatrix = tfidfTable.getDocumentSpaceMatrix();
			//DISPLAY
			display2DMatrix(documentMatrix,documentMatrix.length /2,documentMatrix[0].length / 2 ,"DOCUMENT MATRIX NEW");
			
		conceptVector = new ConceptVector(wordNetPath, conceptPaths);
			//INPUT
		conceptVector.setTFIDFTable(documentMatrix);
		conceptVector.setTerms(originalTermsPerDoc);
		
		System.out.println("concept vector construction.................");
			//OUTPUT
		ThirdTensor = conceptVector.createVector();
		System.out.println("End concept vector construction.............");
			//DISPLAY
			display3DMatrix(ThirdTensor,2,ThirdTensor[0].length / 2, ThirdTensor[0][0].length / 2, "THIRD ORDER TENSOR MODEL");
		
			
		//System.out.println("Sorting Third tensor model.................");
			//INPUT AND OUTPUT
		//ThirdTensor = sortTensor(ThirdTensor);
		//System.out.println("Sorting Third tensor model done............");
			//DISPLAY
			//display3DMatrix(ThirdTensor,2,ThirdTensor[0].length / 2, ThirdTensor[0][0].length / 2, "SORTED THIRD ORDER TENSOR MODEL");
		
			
		System.out.println("Super matrix construction..................");
		superMatrix = new SuperMatrix();
			//INPUT
		superMatrix.setThirdOrderTensor(ThirdTensor);
		superMatrix.buildSuperMatrix();
		super_Matrix = superMatrix.getSuperMatrix();
			//OUTPUT
			display2DMatrix(super_Matrix,super_Matrix.length / 2, super_Matrix[0].length / 2,"SUPER MATRIX");
		System.out.println("End super matrix construction..............");
		
		System.out.println("Creating txt files for resources...........");
		outputThirdTensor();
		outputSuperMatrix();
		outputDocumentPath();
		outputTerms();
		System.out.println("Creating txt files for resources done......");
		a = (System.nanoTime() - b) / 1000000000.0;
		System.out.println("Time duration :"+a);
	}
	public HashSet<String> stopWordsReader(){
    	HashSet<String> Space = new HashSet<String>();
    	File file;
		BufferedReader bw = null;
		try {
			file = new File(stopWordPath);
			bw = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
		}
		catch(IOException e) {
			System.out.println(e.getMessage());
		}
		String line = "";
		try {
			while((line = bw.readLine())!=null) {
				Space.add(line.toLowerCase());
			}
		}
		catch(IOException e) {
			System.out.println(e.getMessage());
		}
		return Space;
    }
	public void outputThirdTensor() {
		BufferedWriter bw = null;
		FileWriter fw = null;
		try {
			fw = new FileWriter("thirdtensor.txt");
			bw = new BufferedWriter(fw);
			bw.write(ThirdTensor.length+"###"+ThirdTensor[0].length+"###"+ThirdTensor[0][0].length);
			bw.newLine();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		for(int i = 0; i < ThirdTensor.length; i++) {
			for(int j = 0; j < ThirdTensor[i].length; j++) {
				for(int k = 0; k < ThirdTensor[i][j].length; k++) {
					try {
						bw.write(ThirdTensor[i][j][k]+"");
						bw.newLine();
					}
					catch(IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		try {
			bw.close();
			fw.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	public void outputSuperMatrix() {
		BufferedWriter bw = null;
		FileWriter fw = null;
		try {
			fw = new FileWriter("supermatrix.txt");
			bw = new BufferedWriter(fw);
			bw.write(super_Matrix.length+"###"+super_Matrix[0].length);
			bw.newLine();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		for(int i = 0; i < super_Matrix.length; i++) {
			for(int j = 0; j < super_Matrix[i].length; j++) {
				try {
					bw.write(super_Matrix[i][j]+"");
					bw.newLine();
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			bw.close();
			fw.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	public void outputDocumentPath() {
		BufferedWriter bw = null;
		FileWriter fw = null;
		try {
			fw = new FileWriter("docupath.txt");
			bw = new BufferedWriter(fw);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		for(String a: documentPaths) {
			try {
				bw.write(a);
				bw.newLine();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
		try {
			bw.close();
			fw.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	public void outputTerms() {
		BufferedWriter bw = null;
		FileWriter fw = null;
		try {
			fw = new FileWriter("terms.txt");
			bw = new BufferedWriter(fw);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		int i = 0;
		for(Window a: uniqueTermsPerDoc) {
			try {
				bw.write(a.getTargetTerm().toLowerCase()+"###"+i);
				bw.newLine();
				i++;
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
		try {
			bw.close();
			fw.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String args[]) {
		modelController creator = new modelController();
		/*
		creator.setConceptIPath("C://Users/harji/Documents/DataSet/ConceptIndices");
		creator.setAlpha(20);
		creator.setDocumentIPath("C://Users/harji/Documents/DataSet/DocumentIndices");
		creator.setConceptSpaceIPath("C://Users/harji/Documents/DataSet/ConceptSpaceIndices");
		creator.setWordNetPath("C://Program Files (x86)/WordNet/2.1");
		creator.setStopWordPath("C://Users/harji/Documents/DataSet/Table/StopWords/stopwords.txt");
		creator.create();
		*/
		creator.setThirdTensor(Reader.readTensor("thirdtensor.txt"));
		creator.rewrite();
		
	}
	public double[][][] sortTensor(double[][][] dummy){
		MergeSort<Item> merger = new MergeSort<Item>();
		merger.setOrder(merger.DESCENDING);
		List<List<Item>> sortedDocu = new ArrayList<List<Item>>();
		for(int i = 0; i < dummy.length; i++) {
			List<Item> sumPerConcept = new ArrayList<Item>();
			for(int j = 0; j < dummy[i][0].length; j++) {
				double sum = 0;
				for(int k = 0; k < dummy[i].length; k++) {
					sum	+= dummy[i][k][j];
				}
				Item con = new Item();
				con.setIndex(j);
				con.setSum(sum);
				sumPerConcept.add(con);
			}
			sumPerConcept = merger.mergesort(sumPerConcept);
			sortedDocu.add(sumPerConcept);
		}
		//double[][][] newThirdTensor = new double[dummy.length][dummy[0].length][dummy[0][0].length];
		for(int i = 0; i < dummy.length; i++) {
			List<Item> sortedConcepts = sortedDocu.get(i);
			for(int j = 0; j < dummy[i].length; j++) {
				double[] newOrder = new double[sortedConcepts.size()];
				for(int k = 0; k < dummy[i][j].length; k++) {
					newOrder[k] = dummy[i][j][sortedConcepts.get(k).getIndex()];
				}
				dummy[i][j] = newOrder;
			}
		}
		return dummy;
	}
	public void outputDocumentMatrix() {
		BufferedWriter bw = null;
		FileWriter fw = null;
		try {
			fw = new FileWriter("documentmatrix.txt");
			bw = new BufferedWriter(fw);
			bw.write(documentMatrix.length+"###"+documentMatrix[0].length);
			bw.newLine();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		for(int i = 0; i < documentMatrix.length; i++) {
			for(int j = 0; j <documentMatrix[i].length; j++) {
				try {
					bw.write(documentMatrix[i][j]+"");
					bw.newLine();
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			bw.close();
			fw.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}
