import java.util.Random;
import java.util.Arrays;


class NormalQS{
	static final int SIZE = 100;
	static int[] tab = new int[SIZE];
	private static void fillArray(){
		Random rand = new Random();
		for(int i=0;i<SIZE;i++)
			tab[i] = rand.nextInt(100);
	}
	
	private static void swap(int i, int j, int[] tab){
		int tmp = tab[i];
		tab[i]=tab[j];
		tab[j]=tmp;
	}
	
	private static int partition(int l, int r, int[] tab){
		int x = tab[l];
		int i=l-1;
		int j=r+1;
		while (true){
			do{
				j--;
			} while (tab[j]>x);
			do{
				i++;
			} while (tab[i]<x);
			if(i<j) swap(i,j,tab);
			else return j;
		}
		
	}
	
	public static void quickSort(int l, int r, int tab[]){
		if(l<r){
			int q = partition(l,r,tab);
			quickSort(l,q,tab);
			quickSort(q+1,r,tab);
		}
	}
	
	public static void main(String[] args){
		fillArray();
		System.out.println(Arrays.toString(tab));
		quickSort(0,SIZE-1,tab);
		System.out.println("----------------------------------");
		System.out.println(Arrays.toString(tab));
	}
}
