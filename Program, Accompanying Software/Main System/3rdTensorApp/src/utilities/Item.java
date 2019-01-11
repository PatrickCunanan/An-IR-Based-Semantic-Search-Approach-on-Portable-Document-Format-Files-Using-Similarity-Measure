package utilities;

public class Item implements Comparable<Item>{
	private int index;
	private double sum;
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public double getSum() {
		return sum;
	}
	public void setSum(double sum) {
		this.sum = sum;
	}
	@Override
	public int compareTo(Item arg0) {
		if(sum < arg0.getSum()) {
			return -1;
		}
		else if (sum > arg0.getSum()) {
			return 1;
		}
		else {
			return 0;
		}
	}
}
