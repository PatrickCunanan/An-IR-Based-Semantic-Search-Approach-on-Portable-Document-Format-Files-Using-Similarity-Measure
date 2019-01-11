package utilities;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;

public class Stemmer {
	public static String wordNetPath;
	public static IDictionary dictionary;
	public static String path;
	public static URL url;
	public static void initialize(String wordNetPath) {
		path = wordNetPath + File.separator + "dict";
		try {
			url = new URL("File", null, path);
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
	public static String stemIt(String original) {
		WordnetStemmer stemmer = new WordnetStemmer(dictionary);
		String[] originalTokens = original.split(" ");
		String stemmedOriginal = "";
		for (int i = 0; i < originalTokens.length; i++) {
			boolean stopper = false;
			boolean partOfSpeech[] = { false, false, false, false, stopper };
			List<String> noun = null;
			List<String> verb = null;
			List<String> adj = null;
			List<String> adv = null;
			try {
				noun = stemmer.findStems(originalTokens[i], POS.NOUN);
				verb = stemmer.findStems(originalTokens[i], POS.VERB);
				adj = stemmer.findStems(originalTokens[i], POS.ADJECTIVE);
				adv = stemmer.findStems(originalTokens[i], POS.ADVERB);
			}
			catch(IllegalArgumentException e) {
				noun = new ArrayList<String>();
				verb = new ArrayList<String>();
				adj = new ArrayList<String>();
				adv = new ArrayList<String>();
				stopper = true;
			}
			
			if (noun.size() == 0 || (noun.size() > 0 && verb.size() > 0 && adj.size() > 0 && adv.size() > 0)
					|| noun.size() > 1) {
				partOfSpeech[0] = false;
			} else {
				partOfSpeech[0] = true;
			}
			if (verb.size() == 0 || (noun.size() > 0 && verb.size() > 0 && adj.size() > 0 && adv.size() > 0)
					|| verb.size() > 1) {
				partOfSpeech[1] = false;
			} else {
				partOfSpeech[1] = true;
			}
			if (adj.size() == 0 || (noun.size() > 0 && verb.size() > 0 && adj.size() > 0 && adv.size() > 0)
					|| adj.size() > 1) {
				partOfSpeech[2] = false;
			} else {
				partOfSpeech[2] = true;
			}
			if (adv.size() == 0 || (noun.size() > 0 && verb.size() > 0 && adj.size() > 0 && adv.size() > 0)
					|| adv.size() > 1) {
				partOfSpeech[3] = false;
			} else {
				partOfSpeech[3] = true;
			}

			for (int j = 0; j < partOfSpeech.length; j++) {
				if (partOfSpeech[4] == false) {
					break;
				}
				if (partOfSpeech[j] == true) {
					if (j == 0) {
						originalTokens[j] = noun.get(0);
					} else if (j == 1) {
						originalTokens[j] = verb.get(0);
					} else if (j == 2) {
						originalTokens[j] = adj.get(0);
					} else if (j == 3) {
						originalTokens[j] = adv.get(0);
					}
				}
			}
		}
		for(int i = 0; i < originalTokens.length; i++) {
			if(i == 0) {
				stemmedOriginal += originalTokens[i];
			}
			else {
				stemmedOriginal += " "+originalTokens[i];
			}
		}
		return stemmedOriginal;
	}
}
