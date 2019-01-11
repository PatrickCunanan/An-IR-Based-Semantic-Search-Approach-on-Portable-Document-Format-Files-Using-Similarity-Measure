package utilities;

import java.io.IOException;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

public class extractPdfText {
	String path;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String extractPdfText() throws IOException {
		PdfReader pr = new PdfReader(path);
		int pages = pr.getNumberOfPages();
		String pdfText = "";
		for (int ctr = 1; ctr <= pages; ctr++) {
			pdfText += PdfTextExtractor.getTextFromPage(pr, ctr);
		}
		pr.close();
		return pdfText;
	}
}
