package model;

public class SuperMatrix {
	private double[][][] thirdOrderTensor;
	private double[][] superMatrix;
	private String logMessage;
	private StringBuilder str;

	public double[][][] getThirdOrderTensor() {
		return thirdOrderTensor;
	}

	public void setThirdOrderTensor(double[][][] thirdOrderTensor) {
		this.thirdOrderTensor = thirdOrderTensor;
	}

	public double[][] getSuperMatrix() {
		return superMatrix;
	}

	public void setSuperMatrix(double[][] superMatrix) {
		this.superMatrix = superMatrix;
	}

	public void buildSuperMatrix() {
		//logMessage = "";
		str = new StringBuilder("");
		System.out.println("Building Super Matrix...");
		//str.append("Building Super Matrix...\r\n");
		superMatrix = new double[thirdOrderTensor[0].length][thirdOrderTensor[0][0].length];
		int numDocs = thirdOrderTensor.length;
		for (int i = 0; i < numDocs; i++) {
			System.out.println("Adding Document "+i+" matrix to Super Matrix");
			//str.append("Adding Document "+i+" matrix to Super Matrix\r\n");
			System.out.println("Remaining Document: "+(numDocs-(i+1))+"/"+numDocs);
		//	str.append("Remaining Document: "+(numDocs-(i+1))+"/"+numDocs+"\r\n");
			double[][] matrix = thirdOrderTensor[i];
			if (i == (numDocs - 1)) {
				System.out.println("Super Matrix: ");
	//			str.append("Super Matrix: \r\n");
				int p = matrix.length * matrix[0].length;
				for (int j = 0; j < matrix.length; j++) {
					for (int k = 0; k < matrix[j].length; k++) {
						superMatrix[j][k] += matrix[j][k];
						superMatrix[j][k] = superMatrix[j][k] / numDocs;
						System.out.println("Remaining compuations for SuperMatrix, "+p);
						p--;
					//	str.append("superMatrix["+j+"]["+k+"] = "+superMatrix[j][k]+"\r\n");
					}
				}
			} else {
				for (int j = 0; j < matrix.length; j++) {
					for (int k = 0; k < matrix[j].length; k++) {
						superMatrix[j][k] += matrix[j][k];
					}
				}
			}
		}
		//logMessage = str.toString();
	}
	public static void main(String[] args) throws Exception {
		double[][][] thirdOrderTensor = { { { 1, 2, 3 }, { 4, 5, 6 } }, { { 1, 2, 3 }, { 13, 14, 15 } },
				{ { 1, 2, 3 }, { 22, 23, 24 } } };
		SuperMatrix sm = new SuperMatrix();
		sm.setThirdOrderTensor(thirdOrderTensor);
		sm.buildSuperMatrix();
	}
}
