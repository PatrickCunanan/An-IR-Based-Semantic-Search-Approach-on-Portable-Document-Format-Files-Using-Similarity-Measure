package utilities;

import java.io.IOException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

public class PDFtoString {

	public static String extractPdfText(String path) throws IOException {
		PdfReader pdfReader = new PdfReader(path);
		int pages = pdfReader.getNumberOfPages();
		String pdfText = "";
		for (int ctr = 1; ctr < pages + 1; ctr++) {
			pdfText += PdfTextExtractor.getTextFromPage(pdfReader, ctr);
		}
		pdfReader.close();
		return pdfText;
	}
}
