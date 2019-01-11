package utilities;

import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class luceneSearch {
	private IndexSearcher searcher;
	private Query query;
	private int numDocs;
	private ArrayList<String> results;
	private int[] conceptFreq;

	public luceneSearch() {
		results = new ArrayList<String>();
	}

	public Query getQuery() {
		return query;
	}

	public void setQuery(Query query) {
		this.query = query;
	}

	public int getNumDocs() {
		return numDocs;
	}

	public void setNumDocs(int numDocs) {
		this.numDocs = numDocs;
	}

	public ArrayList<String> getResults() {
		return results;
	}

	public int[] getConceptFreq() {
		return conceptFreq;
	}

	public void setConceptFreq(int[] conceptFreq) {
		this.conceptFreq = conceptFreq;
	}

	public IndexSearcher getSearcher() {
		return searcher;
	}

	public void setSearcher(IndexSearcher searcher) {
		this.searcher = searcher;
	}

	public void search() throws Exception {
		BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
		TopDocs results = searcher.search(query, numDocs);
		ScoreDoc[] hits = results.scoreDocs;
		int numTotalHits = results.totalHits;
		for (int j = 0; j < numTotalHits; j++) {
			Document doc = searcher.doc(hits[j].doc);
			getResults().add(doc.get("path"));
			conceptFreq[Integer.parseInt(doc.get("docID"))]++;
		}
	}
	
	public void search(String query) throws Exception {
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("words", analyzer);
		Query q = parser.parse(query);
		TopDocs results = searcher.search(q, numDocs);
		ScoreDoc[] hits = results.scoreDocs;
		BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
		int numTotalHits = results.totalHits;
		for (int j = 0; j < numTotalHits; j++) {
			Document doc = searcher.doc(hits[j].doc);
			getResults().add(doc.get("path"));
		}
	}
	public static void main(String[] args) throws Exception {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("C://Users/harji/Documents/DataSet/ConceptSpaceIndices"))); 
		IndexSearcher searcher = new IndexSearcher(reader);
		luceneSearch ls = new luceneSearch();
		ls.setSearcher(searcher);
		ls.setNumDocs(100);
		ls.search("the task of determining the sense of a word in a context");
		ArrayList<String> results = ls.getResults();
		for (int i = 0; i < results.size(); i++) {
			System.out.println(results.get(i));
		}
	}
}
