import java.util.Random;
import java.util.Arrays;
import java.util.concurrent.*;
/*Program oblicza sredni czas z 10 przebiegow dla zwyklego quickSorta,
 * quickSorta zaimplementowanego przy uzyciu ForkJoin Framework
 * oraz pojedynczy przebieg bibliotecznej metody parallelSort
 * (przy wiekszej liczbie wywolan dla tej samej tablicy dochodzi do podejrzanej optymalizacji)
 * */
class QuickSort extends RecursiveAction{		//klasa reprezentujaca zadanie sortowania
	private static final long serialVersionUID = 1L;
	private int l, r;		//lewy i prawy indeks przeszukiwan tablic
	private static final int SIZE = 1_000_000;	//rozmiar tablicy do sortowania
	private static final int[] wzorTablicy = new int[SIZE];		//oryginalna, wylosowana tablica
	private final int[] tabCopy;	//poszczegolne kopie tablicy oryginalnej
	
	QuickSort(int l, int r, int[] tab){
		this.l = l;
		this.r = r;
		this.tabCopy = tab;
	}
	
	private static int[] copyArray(){			//zwraca kopie oryginalnej tablicy
		return Arrays.copyOf(wzorTablicy, wzorTablicy.length);
	}

	private static void initializeArray(){		//losuje inty do oryginalnej tablicy
		Random rand = new Random();
		for(int i=0;i<SIZE;i++)
			wzorTablicy[i] = rand.nextInt();		//mozna podac zakres losowania
	}
	
	private static void swap(int i, int j, int[] tab){		//zamiana miejsc 2 wskazanych elementow tablicy
		int tmp = tab[i];
		tab[i]=tab[j];
		tab[j]=tmp;
	}
	
	private static int partition(int l, int r, int[] tab){		//metoda sortujaca
		int x = tab[l];			//wybor elementu, wzgledem ktorego nastapi sortowanie
		int i=l-1;
		int j=r+1;
		while (true){
			do{
				j--;
			} while (tab[j]>x);		//wyszukaj element z prawej czesci tablicy mniejszy/rowny x
			do{
				i++;
			} while (tab[i]<x);		//wyszukaj element z lewej czesci tablicy wiekszy/rowny x
			if(i<j) swap(i,j,tab);		//zamien znalezione elementy
			else return j;				//lub jesli zakonczono sortowanie obu czesci zwroc indeks elementu x
		}
		
	}
	/*
	protected void compute(){
		if(l<r){
			int q = partition(l,r,this.tabCopy);
			invokeAll(new QuickSort(l,q,this.tabCopy),
					  new QuickSort(q+1,r,this.tabCopy));
		}
	}
	*/
	
	protected void compute(){		//przeciazona metoda klasy RecursiveAction, wykonywana gdy uruchomimy watek dla danego obiektu
		if(l<r){
			int q = partition(l,r,this.tabCopy);		//indeks ostatniego elementu lewej podtablicy
			QuickSort left = new QuickSort(l,q,this.tabCopy);	//utworzenie obiektow reprezentujacych rekurencyjne
			QuickSort right = new QuickSort(q+1,r,this.tabCopy);	//wywolania sortowania dla podtablic
			left.fork();		//lewa strona bedzie liczona rownolegle z prawa
			right.compute();	//licz prawa strone
			left.join();		//zwroc rezultat obliczen, gdy sie zakoncza
		}
	}
	
	public static void main(String[] args){
		initializeArray();
		ForkJoinPool pool;
		long start, duration;
		//System.out.println(Arrays.toString(wzorTablicy));
		
		int[] kopiaTablicy1 = copyArray();		
		int rIdx = kopiaTablicy1.length-1;
		NormalQS.quickSort(0, rIdx, kopiaTablicy1);	//rozgrzanie VM
		//-------------------------
		start = System.nanoTime();				//1 obieg parallelSort
		Arrays.parallelSort(copyArray());
		long durPS = System.nanoTime() - start;
		System.out.println("	parallelSort()	" + durPS/1_000_000 + " ms");
		//-------------------------
		long sumTimeQS = 0;						//10 obiegow zwyklego QS
		for (int j=0; j<10; j++){
			int[] kopiaTablicy0 = copyArray();
			start = System.nanoTime();
			NormalQS.quickSort(0, rIdx, kopiaTablicy0);
			long normalQS = System.nanoTime() - start;
			sumTimeQS += normalQS;
		}
		long avgTimeQS = sumTimeQS/10;
		System.out.println("	zwykły QS	" + avgTimeQS/1_000_000 + " ms");
		//-------------------------
		System.out.println("Liczba wątków	czas sortowania		FJ QS/parallelSort	FJ QS/zwykły QS");
		//QuickSort qs;
		//10 obiegow implementacji ForkJoin QS dla zmiennej liczby watkow(1-8)
		for(int i=1; i<=8;i++){
			long sumTime = 0;
			for(int j=0; j<10; j++){
				pool = new ForkJoinPool(i);
				int[] kopiaTablicy = copyArray();
				start = System.nanoTime();
				//System.out.println(Arrays.toString(kopiaTablicy));
				pool.invoke(new QuickSort(0,SIZE-1,kopiaTablicy));		//rozpocznij wykonywanie QS
				//pool.invoke(qs = new QuickSort(0,SIZE-1,kopiaTablicy));
				//System.out.println(Arrays.toString(qs.tabCopy));
				duration = System.nanoTime() - start;
				sumTime += duration;
				pool.shutdownNow();
			}
			System.out.println("	"+i+"		"+sumTime/(1_000_000*10)+ " ms		"+
					String.format("%.2f",(double)sumTime/(10*(double)durPS))+"			"+
					String.format("%.2f",(double)sumTime/(10*(double)avgTimeQS)));
		}
	}
}
