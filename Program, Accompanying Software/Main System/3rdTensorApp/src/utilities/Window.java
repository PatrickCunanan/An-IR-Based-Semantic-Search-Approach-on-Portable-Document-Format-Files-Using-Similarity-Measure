package utilities;

public class Window implements Comparable<Window>{
	private String targetTerm;
	private String[] surroundingTerms;
	private int leftSize;
	private int rightSize;
	
	public int getLeftSize() {
		return leftSize;
	}
	public void setLeftSize(int leftSize) {
		this.leftSize = leftSize;
	}
	public int getRightSize() {
		return rightSize;
	}
	public void setRightSize(int rightSize) {
		this.rightSize = rightSize;
	}
	public String getTargetTerm() {
		return targetTerm;
	}
	public void setTargetTerm(String targetTerm) {
		this.targetTerm = targetTerm;
	}
	public String[] getSurroundingTerms() {
		return surroundingTerms;
	}
	public void setSurroundingTerms(String[] surroundingTerms) {
		this.surroundingTerms = surroundingTerms;
	}
	public String toString() {
		String s = "";
		for(int i = 0; i < surroundingTerms.length; i++) {
			if(i == surroundingTerms.length - 1) {
				s += surroundingTerms[i];
			}
			else {
				s += surroundingTerms[i] + ", ";
			}
		}
		return s;
	}
	@Override
	public int compareTo(Window b) {
		return this.targetTerm.compareToIgnoreCase(b.targetTerm);
	}
}
