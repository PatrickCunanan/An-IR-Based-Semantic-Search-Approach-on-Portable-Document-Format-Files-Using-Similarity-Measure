package utilities;
import java.util.*;

public class MergeSort<T extends Comparable <? super T>> {
	// default = ascending
	private int order = 1;
	public int ASCENDING = 1; 
	public int  DESCENDING = -1;
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		if(order == -1 || order == 1) {
			this.order = order;
		}
		else {
			this.order = 1;
		}
	}

	public List<T> mergesort(List<T> set) { 
		if(set.size() <= 1) {
			return set;
		} else { 
			List<T> left = new ArrayList<T>();
			List<T> right = new ArrayList<T>();
			
			int middle = set.size() / 2;
			for(int i=0 ; i < middle ; i++) { 
				left.add(set.get(i));
			}
			for(int i = middle; i < set.size(); i++) { 
				right.add(set.get(i));
			}
			return merge(mergesort(left), mergesort(right));
		}
	}

	private List<T> merge(List<T> a, List<T> b) { 
		List<T> combined = new ArrayList<T>();
		int ai = 0, bi = 0;
		
		while(ai < a.size() || bi < b.size()) { 
			if(ai < a.size() && bi < b.size()) {
				if(( order * a.get(ai).compareTo(b.get(bi))) <= 0.0) { 
					combined.add(a.get(ai));
					ai++;
				} else { 
					combined.add(b.get(bi));
					bi++;
				}
			} else if(ai < a.size()) { 
				combined.add(a.get(ai));
				ai++;
			} else if(bi < b.size()) { 
				combined.add(b.get(bi));
				bi++;
			}
		}
		return combined;	
	}
	public static void main (String args[]) {
		List<String> set = new ArrayList<String>();
		set.add("9");
		set.add("2");
		set.add("6");
		set.add("1");

		set.add("0");
		MergeSort<String> merger = new MergeSort<String>();
		merger.setOrder(merger.ASCENDING);
		set = merger.mergesort(set);
		for(String a: set) {
			System.out.println(a);
		}
	}
}
