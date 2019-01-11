package utilities;

import java.util.List;

public class VectorTFIDF {

	public double termFrequency(List<String> concept, String term) {
		double frequency = 0;
		double tfScore;

		for (String word : concept) {
			if (term.equalsIgnoreCase(word)) {
				frequency++;
			}
		}

		tfScore = Math.sqrt(frequency);

		return tfScore;
	}

	public double inverseDocumentFrequency(List<List<String>> conceptSet, String term) {
		double frequency = 1;
		double setSize = conceptSet.size();
		double idfScore;

		for (List<String> concept : conceptSet) {
			for (String word : concept) {
				if (term.equalsIgnoreCase(word)) {
					frequency++;
					break;
				}
			}
		}

		idfScore = 1 + Math.log((setSize / frequency));

		return idfScore;
	}

	public double TFIDFCompute(List<String> concept, List<List<String>> conceptSet, String term) {
		double TFScore = termFrequency(concept, term);
		double IDFScore = inverseDocumentFrequency(conceptSet, term);
		double TFIDFScore = TFScore * IDFScore;

		return TFIDFScore;

	}

}