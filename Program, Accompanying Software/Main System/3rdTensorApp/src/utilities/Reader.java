package utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class Reader {
	public static double[][][] readTensor(String path) {
		double[][][] tensor;
		File file = null;
		BufferedReader bReader = null;
		try {
			file = new File(path);
			bReader = new BufferedReader(new InputStreamReader( new FileInputStream(file), "UTF-8"));
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		String line = "";
		Scanner scan;
		try {
			line = bReader.readLine();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		scan = new Scanner(line);
		scan.useDelimiter("###");
		int docSize, termSize, conceptSize;
		docSize = scan.nextInt();
		termSize = scan.nextInt();
		conceptSize = scan.nextInt();
		scan.close();
		tensor = new double[docSize][termSize][conceptSize];
		try {
			int i = 0, j = 0, k = 0;
			while((line = bReader.readLine()) != null) {
				tensor[i][j][k]  = Double.parseDouble(line);
				k++; //<-- concepts
				if(k == conceptSize) {
					k = 0;
					j++;
				}
				if(j == termSize) {
					k = 0;
					j = 0;
					i++;
				}
			}
			bReader.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return tensor;
	}
	public static double[][] readMatrix(String path){
		double[][] matrix;
		File file = null;
		BufferedReader bReader = null;
		try {
			file = new File(path);
			bReader = new BufferedReader(new InputStreamReader( new FileInputStream(file), "UTF-8"));
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		String line = "";
		Scanner scan;
		try {
			line = bReader.readLine();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		scan = new Scanner(line);
		scan.useDelimiter("###");
		int conSize, termSize;
		termSize = scan.nextInt();
		conSize = scan.nextInt();
		scan.close();
		matrix = new double[termSize][conSize];
		try {
			int i = 0, j = 0;
			while((line = bReader.readLine()) != null) {
				matrix[i][j] = Double.parseDouble(line);
				j++; //<-- concepts
				if(j == conSize) {
					j = 0;
					i++;
				}
			}
			bReader.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return matrix;
	}
	public static ArrayList<String> readDocuPath(String path){
		ArrayList<String> docPaths = new ArrayList<String>();
		File file = null;
		BufferedReader bReader = null;
		try {
			file = new File(path);
			bReader = new BufferedReader(new InputStreamReader( new FileInputStream(file), "UTF-8"));
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		String line = "";
		try {
			while((line = bReader.readLine()) != null) {
				docPaths.add(line);
			}
			bReader.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return docPaths;
	}
	public static HashMap<String, Integer> readTerms(String path){
		HashMap<String, Integer> terms = new HashMap<String, Integer>();
		File file = null;
		BufferedReader bReader = null;
		try {
			file = new File(path);
			bReader = new BufferedReader(new InputStreamReader( new FileInputStream(file), "UTF-8"));
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		String line = "";
		Scanner scan;
		try {
			while((line = bReader.readLine()) != null) {
				scan = new Scanner(line);
				scan.useDelimiter("###");
				terms.put(scan.next(), scan.nextInt());
			}
			bReader.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return terms;
	}
	public static HashSet<String> readStopWords(String path){
		HashSet<String> stopWords = new HashSet<String>();
		File file = null;
		BufferedReader bReader = null;
		try {
			file = new File(path);
			bReader = new BufferedReader(new InputStreamReader( new FileInputStream(file), "UTF-8"));
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		String line = "";
		try {
			while((line = bReader.readLine()) != null) {
				stopWords.add(line);
			}
			bReader.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return stopWords;
	}
}
