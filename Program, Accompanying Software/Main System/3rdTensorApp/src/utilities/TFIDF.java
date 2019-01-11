package utilities;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class TFIDF {
	private IndexReader reader;
	private IndexSearcher searcher;
	private int docId, alpha;
	private ArrayList<TermTFIDF> termsTFIDF, sortedTermsTFIDF;
	private TermsEnum iterator, counter;
	private long totalDocCount, totalTerm;
	private StringBuilder str;
	
	public TFIDF(){
		termsTFIDF = new ArrayList<TermTFIDF>();
		sortedTermsTFIDF = new ArrayList<TermTFIDF>();
	}

	public IndexReader getReader() {
		return reader;
	}

	public void setReader(IndexReader reader) {
		this.reader = reader;
	}

	public IndexSearcher getSearcher() {
		return searcher;
	}

	public void setSearcher(IndexSearcher searcher) {
		this.searcher = searcher;
	}

	public int getDocId() {
		return docId;
	}

	public void setDocId(int docId) {
		this.docId = docId;
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public long getTotalDocCount() {
		return totalDocCount;
	}

	public void setTotalDocCount(long totalDocCount) {
		this.totalDocCount = totalDocCount;
	}

	public long getTotalTerm() {
		return totalTerm;
	}

	public void setTotalTerm(long totalTerm) {
		this.totalTerm = totalTerm;
	}

	public ArrayList<TermTFIDF> getTermsTFIDF() {
		return termsTFIDF;
	}

	public StringBuilder getStr() {
		return str;
	}

	public void setStr(StringBuilder str) {
		this.str = str;
	}

	public void setIterators() throws IOException{
		Terms termVector = reader.getTermVector(docId, "words");
		iterator = termVector.iterator();
		counter = termVector.iterator();
	}
	
	public void setTotalTerms() throws IOException {
		long totalTerms = 0;
		while (true) {
			BytesRef ref = counter.next();
			if (ref == null) {
				break;
			}

			long termFreq = counter.totalTermFreq();
			totalTerms += termFreq;
		}
		setTotalTerm(totalTerms);
	}
	
	public void computeTFIDF() throws IOException {
		setIterators();
		setTotalTerms();
		CollectionStatistics collectionStats = searcher.collectionStatistics("words");
		setTotalDocCount(collectionStats.docCount());
		IndexReaderContext context = searcher.getTopReaderContext();
		while (true) {
			BytesRef ref = iterator.next();
			if (ref == null) {
				break;
			}

			// termFreq = Number of times term t appears in a document
			long termFreq = iterator.totalTermFreq();
			double tf = Math.sqrt((double) termFreq / (double) totalTerm);

			Term term = new Term("words", ref);
			TermContext termContext = TermContext.build(context, term);

			TermStatistics termStats = searcher.termStatistics(term, termContext);
			// docFreq = Number of documents with term t in it
			long docFreq = termStats.docFreq();
			double idf = 1 + (Math.log((double) (totalDocCount + 1) / (docFreq + 1)));

			TermTFIDF t = new TermTFIDF(ref.utf8ToString(), tf * idf);
			str.append("	"+t.toString() + "\r\n");
			
			termsTFIDF.add(t);

			if (sortedTermsTFIDF.isEmpty()) {
				sortedTermsTFIDF.add(t);
			} else if (sortedTermsTFIDF.get(sortedTermsTFIDF.size() - 1).tfidfValue > t.tfidfValue) {
				sortedTermsTFIDF.add(t);
			} else {
				for (int j = 0; j < sortedTermsTFIDF.size(); j++) {
					if (sortedTermsTFIDF.get(j).tfidfValue <= t.tfidfValue) {
						sortedTermsTFIDF.add(j, t);
						break;
					}
				}
			}
		}
	}
	
	public String getHeavyTerms(){
		String heavyTerms = "";
		int cutOff = (int) (sortedTermsTFIDF.size() * ((double) alpha / 100));
		for (int i = 0; i < cutOff; i++) {
			if(i == (cutOff-1)){
				heavyTerms += sortedTermsTFIDF.get(i).term;
			}
			else
				heavyTerms += sortedTermsTFIDF.get(i).term + " ";
		}
		return heavyTerms;
	}
	
	public static void main(String[] args) throws Exception {
		IndexReader docsReader = DirectoryReader.open(FSDirectory.open(Paths.get("docsIndex")));
		IndexSearcher docsSearcher = new IndexSearcher(docsReader);
		TFIDF tfidf = new TFIDF();
		tfidf.setDocId(83);
		tfidf.setAlpha(20);
		tfidf.setReader(docsReader);
		tfidf.setSearcher(docsSearcher);
		tfidf.computeTFIDF();
		for(int i = 0; i < tfidf.getTermsTFIDF().size(); i++){
			System.out.println(tfidf.getTermsTFIDF().get(i));
		}
		System.out.println(tfidf.getHeavyTerms());
	}
}