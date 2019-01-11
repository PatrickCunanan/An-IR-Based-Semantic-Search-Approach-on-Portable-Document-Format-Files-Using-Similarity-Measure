package model;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;

import utilities.TFIDF;
import utilities.TermTFIDF;
import utilities.luceneIndex;
import utilities.luceneSearch;

public class ConceptSpacesModel {
	private HashMap<Integer, ArrayList<TermTFIDF>> docTFIDF;
	private ArrayList<String> docPath, conceptPath;
	private int alpha;
	private int[] conceptFreq;
	private String conceptIndexPath, docsIndexPath, conceptSpaceIndexPath, logMessage;
	private IndexReader docsReader, conceptsReader;
	private StringBuilder str;
	
	public ConceptSpacesModel(int numConcepts) {
		docTFIDF = new HashMap<Integer, ArrayList<TermTFIDF>>();
		docPath = new ArrayList<String>();
		conceptPath = new ArrayList<String>();
		conceptFreq = new int[numConcepts];
		logMessage = "";
		str = new StringBuilder("");
	}
	
	public HashMap<Integer, ArrayList<TermTFIDF>> getDocTFIDF() {
		return docTFIDF;
	}

	public void setDocTFIDF(HashMap<Integer, ArrayList<TermTFIDF>> docTFIDF) {
		this.docTFIDF = docTFIDF;
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public String getConceptIndexPath() {
		return conceptIndexPath;
	}

	public void setConceptIndexPath(String conceptIndexPath) {
		this.conceptIndexPath = conceptIndexPath;
	}

	public String getDocsIndexPath() {
		return docsIndexPath;
	}

	public void setDocsIndexPath(String docsIndexPath) {
		this.docsIndexPath = docsIndexPath;
	}

	public String getConceptSpaceIndexPath() {
		return conceptSpaceIndexPath;
	}

	public void setConceptSpaceIndexPath(String conceptSpaceIndexPath) {
		this.conceptSpaceIndexPath = conceptSpaceIndexPath;
	}

	public ArrayList<String> getDocPath() {
		return docPath;
	}

	public ArrayList<String> getConceptPath() {
		return conceptPath;
	}

	public int[] getConceptFreq() {
		return conceptFreq;
	}

	public String getLogMessage() {
		return logMessage;
	}

	public void setReaders() throws IOException {
		docsReader = DirectoryReader.open(FSDirectory.open(Paths.get(docsIndexPath)));
		conceptsReader = DirectoryReader.open(FSDirectory.open(Paths.get(conceptIndexPath)));
	}

	public void closeReaders() throws IOException {
		docsReader.close();
		conceptsReader.close();
	}

	public void conceptSpaceConstruction() throws Exception {
		setReaders();
		System.out.println("Constructing Concept Space...");
		getDCF();
		getTopDCF();
		indexConceptSpace();
		int numDocs = docsReader.numDocs();
		for (int i = 0; i < numDocs; i++) {
			Document doc = docsReader.document(i);
			String path = doc.get("path");
			docPath.add(path);
		}
		closeReaders();
		logMessage = str.toString();
	}

	public void getDCF() throws Exception {
		IndexSearcher conceptsSearcher = new IndexSearcher(conceptsReader);
		IndexSearcher docsSearcher = new IndexSearcher(docsReader);
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("words", analyzer);
		int numDocs = docsReader.numDocs();
		int numConcepts = conceptsReader.numDocs();
		for (int i = 0; i < numDocs; i++) {
			Document doc = docsReader.document(i);
			System.out.println("Processing " + doc.get("path"));
			str.append("\r\n");
			str.append("Processing " + doc.get("path") + "\r\n");

			// TFIDF
			str.append("computing TFIDF value...\r\n");
			TFIDF tfidf = new TFIDF();
			tfidf.setDocId(i);
			tfidf.setAlpha(alpha);
			tfidf.setReader(docsReader);
			tfidf.setSearcher(docsSearcher);
			tfidf.setStr(str);
			tfidf.computeTFIDF();
			String line = tfidf.getHeavyTerms();
			str = tfidf.getStr();
			str.append("Heavy Weighted Terms: "+ line + "\r\n");
			docTFIDF.put(i, tfidf.getTermsTFIDF());
			
			// luceneSearch

			Query query = parser.parse(line);
			luceneSearch ls = new luceneSearch();
			ls.setNumDocs(numConcepts);
			ls.setSearcher(conceptsSearcher);
			ls.setConceptFreq(conceptFreq);
			ls.setQuery(query);
			ls.search();
			conceptFreq = ls.getConceptFreq();
		}
		str.append("Document Concept Frequency (DCF): \r\n");
		for (int i = 0; i < conceptFreq.length; i++){
			Document doc = conceptsReader.document(i);
			Path path = Paths.get(doc.get("path"));
			String fileName = path.getFileName().toString();
			double dcf = conceptFreq[i] / (double) numDocs;
			str.append(fileName + ": " + conceptFreq[i] + "/" + numDocs + " = " + dcf + "\r\n");
		}
	}

	public void getTopDCF() throws IOException {
		System.out.println("Selecting top 100 Concepts...");
		str.append("Selecting top 100 Concepts...\r\n");
		double dcf = 0;
		ArrayList<Double> conceptDCF = new ArrayList<Double>();
		for (int i = 0; i < conceptFreq.length; i++) {
			dcf = conceptFreq[i] / (double) conceptFreq.length;
			Document concept = conceptsReader.document(i);
			String path = concept.get("path");
			if (conceptDCF.isEmpty()) {
				conceptDCF.add(dcf);
				conceptPath.add(path);
			} else if (conceptDCF.get(conceptDCF.size() - 1) > dcf) {
				conceptDCF.add(dcf);
				conceptPath.add(path);
			} else {
				for (int j = 0; j < conceptDCF.size(); j++) {
					if (conceptDCF.get(j) <= dcf) {
						conceptDCF.add(j, dcf);
						conceptPath.add(j, path);
						break;
					}
				}
			}
		}
		conceptPath.subList(100, conceptPath.size()).clear();
		str.append("Concept Space: \r\n");
		for(int i = 0; i < conceptPath.size(); i++){
			str.append(conceptPath.get(i) + "\r\n");
		 }
	}

	public void indexConceptSpace() {
		luceneIndex li = new luceneIndex();
		li.setIndexPath(conceptSpaceIndexPath);
		li.setCreate(true);
		li.setDocIdCounter(0);
		for (int i = 0; i < conceptPath.size(); i++) {
			li.setDocsPath(conceptPath.get(i));
			li.indexFiles();
			li.setCreate(false);
			str.append(li.getLogMessage() + "\r\n");
		}
	}
	

	public static void main(String[] args) throws Exception {
		 double table[][] = new double[900000][200];
		 for(int i = 0; i < table.length; i++) {
			 for(int j = 0; j < table[i].length;j++) {
				 table[i][j] = 9;
			 }
		 }
	}
}
