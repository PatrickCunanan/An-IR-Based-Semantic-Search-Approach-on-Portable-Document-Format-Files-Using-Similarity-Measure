package utilities;

public class Remover {
	public static String removeSymbols(String query) {
		String spacesRemoved = query.replaceAll("\\r\\n|\\r|\\n", " ");
		String symbolsRemoved = spacesRemoved.replaceAll("[^A-Za-z0-9\\u00f1\\u00d1 ]", " ");
		String normalizedWiki = org.apache.commons.lang3.StringUtils.normalizeSpace(symbolsRemoved);
		
		return normalizedWiki;
	}
	/*
	public static void main(String args[]) throws IOException {
		PDFtoString p = new PDFtoString();
		
		String textHolder = p.extractPdfText("SampleData/1.pdf");
		String text = textHolder.replaceAll("\\r\\n|\\r|\\n", " ");
		String query = text.replaceAll("[^A-Za-z0-9\\u00f1\\u00d1 ]", "");		
		String query3 = org.apache.commons.lang3.StringUtils.normalizeSpace(query);		
		System.out.println(query3);
	}
	*/
}
