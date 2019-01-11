package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class luceneIndex {
	private String indexPath, docsPath, logMessage;
	private boolean create;
	private int docIdCounter;
	public String getIndexPath() {
		return indexPath;
	}
	public void setIndexPath(String indexPath) {
		this.indexPath = indexPath;
	}
	public String getDocsPath() {
		return docsPath;
	}
	public void setDocsPath(String docsPath) {
		this.docsPath = docsPath;
	}
	public boolean isCreate() {
		return create;
	}
	public void setCreate(boolean create) {
		this.create = create;
	}
	public int getDocIdCounter() {
		return docIdCounter;
	}
	public void setDocIdCounter(int docIdCounter) {
		this.docIdCounter = docIdCounter;
	}
	
	public String getLogMessage() {
		return logMessage;
	}
	public void indexFiles(){
		//logMessage = "";
		final Path docDir = Paths.get(docsPath);
		if (!Files.isReadable(docDir)) {
			//logMessage += "Document directory '" + docDir.toAbsolutePath()
			//		+ "' does not exist or is not readable, please check the path\r\n";
			System.out.println("Document directory '" + docDir.toAbsolutePath()
			+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		Date start = new Date();
		try {
			//logMessage += "Indexing to directory '" + indexPath + "'...\r\n";
			System.out.println("Indexing to directory '" + indexPath + "'...");
			Directory dir = FSDirectory.open(Paths.get(indexPath));
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

			if (create) {
				// Create a new index in the directory, removing any
				// previously indexed documents:
				iwc.setOpenMode(OpenMode.CREATE);
			} else {
				// Add new documents to an existing index:
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			}

			// Optional: for better indexing performance, if you
			// are indexing many documents, increase the RAM
			// buffer. But if you do this, increase the max heap
			// size to the JVM (eg add -Xmx512m or -Xmx1g):
			//
			// iwc.setRAMBufferSizeMB(256.0);

			IndexWriter writer = new IndexWriter(dir, iwc);
			indexDocs(writer, docDir);

			// NOTE: if you want to maximize search performance,
			// you can optionally call forceMerge here. This can be
			// a terribly costly operation, so generally it's only
			// worth it when your index is relatively static (ie
			// you're done adding documents to it):
			//
			// writer.forceMerge(1);
			writer.close();

			Date end = new Date();
			//logMessage += end.getTime() - start.getTime() + " total milliseconds\r\n";
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");
		} catch (IOException e) {
			//logMessage += " caught a " + e.getClass() + "\n with message: " + e.getMessage()+"\r\n";
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage()+"");
		}
	}
	
	/**
	 * Indexes the given file using the given writer, or if a directory is
	 * given, recurses over files and directories found under the given
	 * directory.
	 * 
	 * NOTE: This method indexes one document per input file. This is slow. For
	 * good throughput, put multiple documents into your input file(s). An
	 * example of this is in the benchmark module, which can create "line doc"
	 * files, one document per line, using the <a href=
	 * "../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
	 * >WriteLineDocTask</a>.
	 * 
	 * @param writer
	 *            Writer to the index where the given file/dir info will be
	 *            stored
	 * @param path
	 *            The file to index, or the directory to recurse into to find
	 *            files to index
	 * @throws IOException
	 *             If there is a low-level I/O error
	 */
	public void indexDocs(final IndexWriter writer, Path path) throws IOException {
		if (Files.isDirectory(path)) {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					try {
						indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
					} catch (IOException ignore) {
						// don't index files that can't be read.
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} else {
			indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
		}
	}

	/** Indexes a single document */
	public void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
		try (InputStream stream = Files.newInputStream(file)) {
			// make a new, empty document
			extractPdfText extract = new extractPdfText();
			extract.setPath(file.toString());
			String value = extract.extractPdfText();
			String cleansedValue = Remover.removeSymbols(value);
			String stringValue = Stemmer.stemIt(cleansedValue);
			Document doc = new Document();
			Field docId = new StringField("docID", Integer.toString(docIdCounter), Field.Store.YES);
			doc.add(docId);
			docIdCounter++;
			// Add the path of the file as a field named "path". Use a
			// field that is indexed (i.e. searchable), but don't tokenize
			// the field into separate words and don't index term frequency
			// or positional information:
			Field pathField = new StringField("path", file.toString(), Field.Store.YES);
			doc.add(pathField);

			// Add the last modified date of the file a field named "modified".
			// Use a LongPoint that is indexed (i.e. efficiently filterable with
			// PointRangeQuery). This indexes to milli-second resolution, which
			// is often too fine. You could instead create a number based on
			// year/month/day/hour/minutes/seconds, down the resolution you
			// require.
			// For example the long value 2011021714 would mean
			// February 17, 2011, 2-3 PM.
			doc.add(new LongPoint("modified", lastModified));
			
			// Add the contents of the file to a field named "contents". Specify
			// a Reader,
			// so that the text of the file is tokenized and indexed, but not
			// stored.
			// Note that FileReader expects the file to be in UTF-8 encoding.
			// If that's not the case searching for special characters will
			// fail.
			FieldType type = new FieldType();
			type.setStored(true);
			type.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
			type.setStored(true);
			type.setTokenized(true);
			type.setStoreTermVectorOffsets(true);
		    type.setStoreTermVectors(true);
			Field wordsField = new Field("words", stringValue, type);
			doc.add(wordsField);
			
			doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));

			if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
				// New index, so we just add the document (no old document can
				// be there):
				//logMessage += "adding " + file+"\r\n";
				System.out.println("adding " + file);
				writer.addDocument(doc);
			} else {
				// Existing index (an old copy of this document may have been
				// indexed) so
				// we use updateDocument instead to replace the old one matching
				// the exact
				// path, if present:
				//logMessage += "updating " + file+"\r\n";
				System.out.println("updating " + file+"");
				writer.updateDocument(new Term("path", file.toString()), doc);
			}
		}
	}
	public static void main(String[] args) {
		Stemmer.initialize("C://Program Files (x86)/WordNet/2.1");
		luceneIndex li = new luceneIndex();
		li.setIndexPath("C://Users/harji/Documents/DataSet/DocumentIndices");
		li.setDocsPath("C://Users/harji/Documents/DataSet/Documents");
		li.setCreate(true);
		li.setDocIdCounter(0);
		li.indexFiles();
		
		li.setIndexPath("C://Users/harji/Documents/DataSet/ConceptIndices");
		li.setDocsPath("C://Users/harji/Documents/DataSet/Concepts");
		li.setDocIdCounter(0);
		li.indexFiles();
	}
}
