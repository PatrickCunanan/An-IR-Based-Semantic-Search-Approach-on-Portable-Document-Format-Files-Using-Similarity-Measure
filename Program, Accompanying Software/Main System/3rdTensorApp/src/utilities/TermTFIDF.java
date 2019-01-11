package utilities;

public class TermTFIDF {
	public String term;
	public double tfidfValue;
	public TermTFIDF(String t, double tfidf) {
		term = t;
		tfidfValue = tfidf;
	}
	public String toString(){
		String stfidf = Double.toString(tfidfValue);
        return term + " = " + stfidf;
    } 
}
