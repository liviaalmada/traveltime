package br.ufc.arida.analysis.model.measures;

public interface TrafficComparatorMeasure<E> {
	double calculate(E m1, E m2) throws NullCostException, Exception ;
	double calculate(Long id1, Long id2, E m1, E m2) throws NullCostException, Exception ;
	
}
