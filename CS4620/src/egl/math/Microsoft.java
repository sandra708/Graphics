package egl.math;

public class Microsoft {

	public Microsoft() {
		// TODO Auto-generated constructor stub
	}
	
	public static Integer[] combine(Integer[] first, Integer[] second){
		
		Integer[] result = new Integer[first.length + second.length];
		for(int i = 0; i < result.length; i++){
			if(i < first.length){
				result[i] = first[i];
			} else {
				result[i] = second[i - first.length];
			}
		}
		return result;
	}
	
	public static int size(Integer[] arr){
		int i = 0;
		for(Integer j: arr){
			i++;
		}
		return i;
	}

}
