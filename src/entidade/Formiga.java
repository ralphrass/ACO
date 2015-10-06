package entidade;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ralph on 05/10/2015.
 */
public class Formiga {

    private int id;
    private Cidade cidadePosicionada;
    private List<Cidade> tour = new ArrayList<>();

    public Formiga(int id, Cidade cid){

        this.id = id;
        this.cidadePosicionada = cid;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTour(List<Cidade> tour) {
        this.tour = tour;
    }

    public List<Cidade> getTour() {
        return tour;
    }

    public Cidade getCidadePosicionada() {
        return cidadePosicionada;
    }

    public void setCidadePosicionada(Cidade cidadePosicionada) {
        this.cidadePosicionada = cidadePosicionada;
    }
}
