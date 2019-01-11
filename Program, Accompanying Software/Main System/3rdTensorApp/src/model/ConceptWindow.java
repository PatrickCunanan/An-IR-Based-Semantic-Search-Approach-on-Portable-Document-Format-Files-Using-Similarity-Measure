package model;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import utilities.PDFtoString;
import utilities.Remover;
import utilities.Stemmer;
import utilities.Window;

public class ConceptWindow {
	private String wordNetPath;
	private String path;
	private URL url;
	public static IDictionary dictionary;
	private List<Window> targetTerms;
	private List<Window> uniqueTargetTerms;
	private HashSet<String> uniqueTargetTermsIdentifier;
	private HashSet<String> stopWords;
	private StringBuilder logMessage;
	
	public StringBuilder getLogMessage() {
		return logMessage;
	}
	public void setLogMessage(StringBuilder logMessage) {
		this.logMessage = logMessage;
	}
	public HashSet<String> getStopWords() {
		return stopWords;
	}
	public void setStopWords(HashSet<String> stopWords) {
		this.stopWords = stopWords;
	}
	public List<Window> getTargetTerms() {
		return targetTerms;
	}
	public void setTargetTerms(List<Window> targetTerms) {
		this.targetTerms = targetTerms;
	}
	public List<Window> getUniqueTargetTerms() {
		return uniqueTargetTerms;
	}
	public void setUniqueTargetTerms(List<Window> uniqueTargetTerms) {
		this.uniqueTargetTerms = uniqueTargetTerms;
	}
	public HashSet<String> getUniqueTargetTermsIdentifier() {
		return uniqueTargetTermsIdentifier;
	}
	public void setUniqueTargetTermsIdentifier(HashSet<String> uniqueTargetTermsIdentifier) {
		this.uniqueTargetTermsIdentifier = uniqueTargetTermsIdentifier;
	}
	public ConceptWindow(String wordNetPath) {
		this.wordNetPath = wordNetPath;
		path = this.wordNetPath + File.separator + "dict";
		targetTerms = new ArrayList<Window>();
		uniqueTargetTerms = new ArrayList<Window>();
		uniqueTargetTermsIdentifier = new HashSet<String>();
		try {
			url = new URL("File", null,path);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		dictionary = new Dictionary(url);
		try {
			dictionary.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void create(String document){
		//logMessage = new StringBuilder();
		double before, after;
		List<String> Sentence = new ArrayList<String>();
		Scanner scan = new Scanner(document);
		String regx = "(?<!Mr|Mrs|\b[A-Z]|mr|mrs|dr|sr|Sr|Dr|Prof|prof|[0-9])[.][ ]+";
		scan = scan.useDelimiter(regx); // HERE
		while(scan.hasNext()) {
			Sentence.add(scan.next());
		}
		scan.close();
		List<Window> Context = new ArrayList<Window>();
		//logMessage.append("Process 1: grouping sentence of a document to create a list of windows.\r\n");
		for(int si = 0; si < Sentence.size(); si++) {
			//System.out.println("\tProcessing Context #: "+si);
			//logMessage.append("\tProcessing Context #: " +si+"\r\n");
			before = System.nanoTime();
			Window context = new Window();
			int i = si - 1;
			int j = si + 1;
			List<String> left = new ArrayList<String>();
			List<String> right = new ArrayList<String>();
			int sum = left.size() + right.size() + 1;
			while (sum != 5 && (!(i < 0 || i < si - 2) || !(j >= Sentence.size() || j > si + 2))) {
				if(i >= 0) {
					int diffL = left.size() - right.size();
					if(diffL <= 0) {
						left.add(Sentence.get(i));
					}
					else {
						right.add(Sentence.get(i));
					}
					i--;
				}
				if(j < Sentence.size()) {
					int diffR = right.size() - left.size();
					if(diffR <= 0) {
						right.add(Sentence.get(j));
					}
					else {
						left.add(Sentence.get(j));
					}
					j++;
				}
				sum = left.size() + right.size() + 1;
			}
			String surroundingSentences[] = new String[left.size()+right.size()];
			int k = 0;
			for(String a: left) {
				surroundingSentences[k] = a;
				k++;
			}
			for(String a: right) {
				surroundingSentences[k] = a;
				k++;
			}
			context.setSurroundingTerms(surroundingSentences);
			context.setTargetTerm(Sentence.get(si));
			Context.add(context);
			after = (System.nanoTime() - before) / 1000000000.0;
			//System.out.println("\t\tTime processed: "+after+"s");
			//logMessage.append("\t\tTime processed: "+after+"s"+"\r\n");
		}
		//System.out.println("Process 2: the process of choosing target terms between each words of a sentence.\n"
						 //+ "           In this process, target terms will be chosen from the words tokenized\n"
						// + "           in the sentence and their surrounding term will also be chosen in the sentence.");

		//logMessage.append("Process 2: the process of choosing target terms between each words of a sentence.\r\n");
		int inc = 0;
		for(Window con: Context) {
			//System.out.println("\tProcessing Context: "+inc+" Remaining left: "+Context.size());
			//logMessage.append("\tProcessing Context: "+inc+" Remaining left: "+Context.size()+"\r\n");;
			before = System.nanoTime();
			String currentSentence = con.getTargetTerm();
			String surroundSentences[] = con.getSurroundingTerms();
			//cleanse sentence
			String cleansedSentence = Remover.removeSymbols(currentSentence);
			// stem the sentence
			String stemmedSentence = Stemmer.stemIt(cleansedSentence);
			
			String terms[] = stemmedSentence.split(" ");
			for(int i = 0; i < terms.length;  i++) {
				before = System.nanoTime();
				if(!stopWords.contains(terms[i]) && !terms[i].equalsIgnoreCase("")/* && isTargetTerm(terms[i])*/) {
					List<String> synonyms;
					synonyms = getSynonyms(terms[i], surroundSentences);
					Window win;
					if(synonyms.size() == 0) {
						win = createWindow(i, terms);
					}
					else {
						win = createWindow(terms[i], synonyms);
						win = createWindow(i, win, terms);
					}
					if(!uniqueTargetTermsIdentifier.contains(terms[i].toLowerCase())) {
						uniqueTargetTermsIdentifier.add(terms[i].toLowerCase());
						uniqueTargetTerms.add(win);
					}
					targetTerms.add(win);
				}
				else if(!stopWords.contains(terms[i]) && !terms[i].equalsIgnoreCase("")/* && isTargetTerm(terms[i])*/) {
					String[] surroundingTerms = new String[0];
					Window win = new Window();
					win.setSurroundingTerms(surroundingTerms);
					win.setTargetTerm(terms[i]);
				}
				else {
					// skip
				}
			}
			inc++;
			after = (System.nanoTime() - before) / 1000000000.0;
			//System.out.println("\t\t\tTime processed: "+after+"s");
			//logMessage.append("\t\t\tTime processed: "+after+"s");
		}
	}
	// must ensure that the term is a target term
	public List<String> getSynonyms (String term, String sen1, String sen2, String sen3){
		return getSynonyms(term, sen1, sen2, sen3, "asdfgh");
	}
 	public List<String> getSynonyms (String term, String sen1, String sen2, String sen3, String sen4){
		String context = sen1.trim() + "." + sen2 + "." + sen3 + "." + sen4;
		// cleanse
		context = Remover.removeSymbols(context);
		// stem it
		context = Stemmer.stemIt(context);
		
		String contextTokens[] = context.split(" ");
		// prepare var
		List<String> synonymWords = new ArrayList<String>();
		HashSet<String> uniqueSynonymWords = new HashSet<String>();
		List<IWordID> ids = new ArrayList<IWordID>();
		IIndexWord indicesN = dictionary.getIndexWord(term, POS.NOUN);
		IIndexWord indicesV = dictionary.getIndexWord(term, POS.VERB);
		IIndexWord indicesAdj = dictionary.getIndexWord(term, POS.ADJECTIVE);
		IIndexWord indicesAdv = dictionary.getIndexWord(term, POS.ADVERB);
		if(indicesN != null) {
			List<IWordID> idN = indicesN.getWordIDs();
			for(IWordID a: idN) {
				ids.add(a);
			}
		}
		if(indicesV != null) {
			List<IWordID> idV = indicesV.getWordIDs();
			for(IWordID a: idV) {
				ids.add(a);
			}
		}
		if(indicesAdj != null) {
			List<IWordID> idAdj = indicesAdj.getWordIDs();
			for(IWordID a: idAdj) {
				ids.add(a);
			}
		}
		if(indicesAdv != null) {
			List<IWordID> idAdv = indicesAdv.getWordIDs();
			for(IWordID a: idAdv) {
				ids.add(a);
			}
		}
		if(ids.size() != 0) {
			for(String token: contextTokens) {
				for(int i = 0; i < ids.size(); i++) {
					if(token.equalsIgnoreCase("asdfgh")) {
						// skip
						break;
					}
					ISynset synset = dictionary.getWord(ids.get(i)).getSynset();
					List<IWord> synsetWords = synset.getWords();
					boolean Break = false;
					for(int j = 0; j < synsetWords.size(); j++) {
						if(!uniqueSynonymWords.contains(token.toLowerCase()) && !token.equalsIgnoreCase(term) && synsetWords.get(j).getLemma().toUpperCase().equals(token.toUpperCase())) {
							uniqueSynonymWords.add(token.toLowerCase());
							synonymWords.add(token.toLowerCase());
							Break = true;
							break;
						}
					}
					if(Break)
						break;
				}
			}
		}
		return synonymWords;
	}
	public List<String> getSynonyms(String term, String sen1, String sen2){
		return getSynonyms(term, sen1, sen2, "asdfgh", "asdfgh");
	}
	public Window createWindow(String term, List<String> synonyms) {
		Window win = new Window();
		String[] surroundWords;
		List<String> leftWords = new ArrayList<String>();
		List<String> rightWords = new ArrayList<String>();
		int sum = leftWords.size() + rightWords.size() + 1;
		int i = 0;
		while(/*!(sum >= 9 || i > synonyms.size() - 1)*/ !(sum >= 9 || i > synonyms.size() - 1)) {
			String synonymWord = synonyms.get(i);
			int difference = leftWords.size() - rightWords.size();
			if(difference < 0 || difference == 0) {
				leftWords.add(synonymWord.toLowerCase());
			}
			else {
				rightWords.add(synonymWord.toLowerCase());
			}
			sum = leftWords.size() + rightWords.size() + 1;
			i++;
		}
		/*
		sum = leftWords.size() + rightWords.size() + 1;
		if(sum % 2 != 1) {
			int difference = leftWords.size() - rightWords.size();
			if(difference < 0) {
				leftWords.remove(leftWords.size() - 1);
			}
			else {
				rightWords.remove(rightWords.size() - 1);
			}
		}
		*/
		sum = leftWords.size() + rightWords.size();
		surroundWords = new String[sum];
		int k = 0;
		for(int j = 0; j < leftWords.size(); j++,k++) {
			surroundWords[k] = leftWords.get(j);
		}
		for(int j = 0; j < rightWords.size(); j++,k++) {
			surroundWords[k] = rightWords.get(j);
		}
		win.setSurroundingTerms(surroundWords);
		win.setTargetTerm(term.toLowerCase());
		win.setLeftSize(leftWords.size());
		win.setRightSize(rightWords.size());
		return win;
	}
	public Window createWindow(int termIndex, Window win, String terms[]) {
		String surroundWords[] = win.getSurroundingTerms();
		List<String> leftSide = new ArrayList<String>();
		List<String> rightSide = new ArrayList<String>();
		int k = 0;
		for(int i = 0;i < win.getLeftSize(); i++) {
			leftSide.add(surroundWords[k].toLowerCase());
			k++;
		}
		for(int i = 0; i < win.getRightSize(); i++) {
			rightSide.add(surroundWords[k].toLowerCase());
			k++;
		}
		return createWindow(termIndex, leftSide, rightSide, terms);
	}
	public Window createWindow(int termIndex, List<String> leftSide, List<String> rightSide, String terms[]) {
		HashSet<String> leftHash = new HashSet<String>();
		HashSet<String> rightHash = new HashSet<String>();
		Window win = new Window();
		String[] surroundWords;
		int sum = leftSide.size() + rightSide.size() + 1;
		int l = termIndex - 1;
		int j = termIndex + 1;
		while(/*!(sum >= 9) && !( l < 0 && j > terms.length - 1)*/ !((sum >= 9) || (l < 0 && j > terms.length -1))){
			for(int i = l; i > -1; i--) {
				if(!stopWords.contains(terms[i].toLowerCase())/* && isTargetTerm(terms[i])*/){
					break;
				}
				l--;
			}
			for(int i = j; i < terms.length; i++) {
				if(!stopWords.contains(terms[i].toLowerCase())/* && isTargetTerm(terms[i])*/){
					break;
				}
				j++;
			}
			if(l >= 0) {
				int diffL = leftSide.size() - rightSide.size();
				if(diffL <= 0) {
					if(leftHash.contains(terms[l].toLowerCase()) || rightHash.contains(terms[l].toLowerCase())) {
					}
					else {
						leftSide.add(terms[l].toLowerCase());
						leftHash.add(terms[l].toLowerCase());
					}
				}
				else {
					if(leftHash.contains(terms[l].toLowerCase()) || rightHash.contains(terms[l].toLowerCase())) {
					}
					else {
						rightSide.add(terms[l].toLowerCase());
						rightHash.add(terms[l].toLowerCase());
					}
				}
				l--;
			}
			if(j <= terms.length - 1) {
				int diffR = rightSide.size() - leftSide.size();
				if(diffR <= 0) {
					if(leftHash.contains(terms[j].toLowerCase()) || rightHash.contains(terms[j].toLowerCase())) {
					}
					else {
						rightSide.add(terms[j].toLowerCase());
						rightHash.add(terms[j].toLowerCase());
					}
				}
				else {
					if(leftHash.contains(terms[j].toLowerCase()) || rightHash.contains(terms[j].toLowerCase())) {
					}
					else {
						leftSide.add(terms[j].toLowerCase());
						leftHash.add(terms[j].toLowerCase());
					}
				}
				j++;
			}
			sum = leftSide.size() + rightSide.size() + 1;
		}
		sum = leftSide.size() + rightSide.size() + 1;
		if(sum % 2 != 1) {
			int difference = leftSide.size() - rightSide.size();
			if(difference > 0) {
				leftSide.remove(leftSide.size() - 1);
			}
			else {
				rightSide.remove(rightSide.size() - 1);
			}
		}
		sum = leftSide.size() + rightSide.size();
		surroundWords = new String[sum];
		int k = 0;
		for(int p = 0; p < leftSide.size(); p++,k++) {
			surroundWords[k] = leftSide.get(p);
		}
		for(int p = 0; p < rightSide.size(); p++,k++) {
			surroundWords[k] = rightSide.get(p);
		}
		win.setSurroundingTerms(surroundWords);
		win.setTargetTerm(terms[termIndex].toLowerCase());
		win.setLeftSize(leftSide.size());
		win.setRightSize(rightSide.size());
		return win;
	}
	public Window createWindow(int termIndex,String terms[]) {
		List<String> left = new ArrayList<String>();
		List<String> right = new ArrayList<String>();
		return createWindow(termIndex, left, right, terms);
	}
	public boolean isTargetTerm(String token) {
		IIndexWord indicesN = dictionary.getIndexWord(token, POS.NOUN);
		IIndexWord indicesV = dictionary.getIndexWord(token, POS.VERB);
		IIndexWord indicesAdj = dictionary.getIndexWord(token, POS.ADJECTIVE);
		IIndexWord indicesAdv = dictionary.getIndexWord(token, POS.ADVERB);
		return (indicesN != null) || (indicesV != null) || (indicesAdj != null) || (indicesAdv != null);
	}
	public List<String> getSynonyms (String term, String [] sens){
		//String context = sen1.trim() + "." + sen2 + "." + sen3 + "." + sen4;
		String context = "";
		for(int i = 0; i < sens.length; i ++) {
			if(i == 0) {
				context += sens[i].trim() + ".";
			}
			else {
				context += sens[i] + ".";
			}
		}
		// cleanse
		context = Remover.removeSymbols(context);
		// stem it
		context = Stemmer.stemIt(context);
		
		String contextTokens[] = context.split(" ");
		// prepare var
		List<String> synonymWords = new ArrayList<String>();
		HashSet<String> uniqueSynonymWords = new HashSet<String>();
		List<IWordID> ids = new ArrayList<IWordID>();
		IIndexWord indicesN = dictionary.getIndexWord(term, POS.NOUN);
		IIndexWord indicesV = dictionary.getIndexWord(term, POS.VERB);
		IIndexWord indicesAdj = dictionary.getIndexWord(term, POS.ADJECTIVE);
		IIndexWord indicesAdv = dictionary.getIndexWord(term, POS.ADVERB);
		if(indicesN != null) {
			List<IWordID> idN = indicesN.getWordIDs();
			for(IWordID a: idN) {
				ids.add(a);
			}
		}
		if(indicesV != null) {
			List<IWordID> idV = indicesV.getWordIDs();
			for(IWordID a: idV) {
				ids.add(a);
			}
		}
		if(indicesAdj != null) {
			List<IWordID> idAdj = indicesAdj.getWordIDs();
			for(IWordID a: idAdj) {
				ids.add(a);
			}
		}
		if(indicesAdv != null) {
			List<IWordID> idAdv = indicesAdv.getWordIDs();
			for(IWordID a: idAdv) {
				ids.add(a);
			}
		}
		if(ids.size() != 0) {
			for(String token: contextTokens) {
				for(int i = 0; i < ids.size(); i++) {
					if(token.equalsIgnoreCase("asdfgh")) {
						// skip
						break;
					}
					ISynset synset = dictionary.getWord(ids.get(i)).getSynset();
					List<IWord> synsetWords = synset.getWords();
					boolean Break = false;
					for(int j = 0; j < synsetWords.size(); j++) {
						if(!uniqueSynonymWords.contains(token.toLowerCase()) && !token.equalsIgnoreCase(term) && synsetWords.get(j).getLemma().toUpperCase().equals(token.toUpperCase())) {
							uniqueSynonymWords.add(token.toLowerCase());
							synonymWords.add(token.toLowerCase());
							Break = true;
							break;
						}
					}
					if(Break)
						break;
				}
			}
		}
		return synonymWords;
	}
	
	public static void main(String args[]) {
		HashSet<String> stop = stopWordsReader();
		// initialize Stemmer
		Stemmer.initialize("C://Program Files (x86)/WordNet/2.1");
		String docuPath = "C://Users/harji/Documents/DataSet/Documents/20 Years of Tourism Research in Asia Pacific 1996 to 2015.pdf";
		String docuPath2 = "C://Users/harji/Documents/DataSet/Documents/A Community Based Tourism Model Its Conception and Use.pdf";
		String document = "";
		String document2 ="";
		try {
			document = PDFtoString.extractPdfText(docuPath);
			document2 = PDFtoString.extractPdfText(docuPath2);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		ConceptWindow windowCreator = new ConceptWindow("C://Program Files (x86)/WordNet/2.1");
		windowCreator.setStopWords(stop);
		double before = System.nanoTime();
		System.out.println(document);
		windowCreator.create(document);
		//windowCreator.create(document2);
		double after = (System.nanoTime() - before) / 1000000000.0;
		System.out.println("Time processed: "+after+"s");
		List<Window> duplicateTerms = windowCreator.getTargetTerms();
		List<Window> uniqueTerms = windowCreator.getUniqueTargetTerms();
		for(Window win: uniqueTerms) {
			System.out.println(win.getTargetTerm()+" : "+win);
		}
	}
	public static HashSet<String> stopWordsReader(){
    	HashSet<String> Space = new HashSet<String>();
    	File file;
		BufferedReader bw = null;
		try {
			file = new File("C://Users/harji/Documents/DataSet/Table/StopWords/stopwords.txt");
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
    
}
