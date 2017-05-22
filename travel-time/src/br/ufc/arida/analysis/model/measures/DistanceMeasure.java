package br.ufc.arida.analysis.model.measures;

public interface DistanceMeasure<E> {
	double calculate(E m1, E m2) ;//throws CostNotFoundException;
}
