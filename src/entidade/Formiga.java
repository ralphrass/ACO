package entidade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ralph on 05/10/2015.
 */
public class Formiga {

    private int id;
    private Cidade cidadePosicionada;
    private List<Visita> tour = new ArrayList<>();
    //private double DeltaTau; // Somatório de 1/Lk {Lk = distância}, útil para o cálculo de depósito de feromônio local {regra (02)}
    private Map<Aresta, Double> DeltaTau = new HashMap<>();

    public Formiga(int id, Cidade cid){

        this.id = id;
        this.cidadePosicionada = cid;
        //this.DeltaTau = 0d;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Formiga formiga = (Formiga) o;

        return id == formiga.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Formiga{" +
                "id=" + id +
                '}';
    }

    public Map<Aresta, Double> getDeltaTau() {
        return DeltaTau;
    }

    public void setDeltaTau(Map<Aresta, Double> deltaTau) {
        this.DeltaTau = deltaTau;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTour(List<Visita> tour) {
        this.tour = tour;
    }

    public List<Visita> getTour() {
        return tour;
    }

    public Cidade getCidadePosicionada() {
        return cidadePosicionada;
    }

    public void setCidadePosicionada(Cidade cidadePosicionada) {
        this.cidadePosicionada = cidadePosicionada;
    }
}
