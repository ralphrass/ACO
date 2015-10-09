package entidade;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ralph on 05/10/2015.
 */
public class Aresta {

    private int id;

    private List<Cidade> cidades;

    private BigDecimal distancia;
    private BigDecimal feromonio;

    public Aresta(int idP, Cidade cidade1, Cidade cidade2, BigDecimal distanciaP, BigDecimal feromonioP){

        this.id = idP;
        this.distancia = distanciaP;
        this.feromonio = feromonioP;

        this.cidades = new ArrayList<>();

        this.cidades.add(cidade1);
        this.cidades.add(cidade2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Aresta aresta = (Aresta) o;

        return id == aresta.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Aresta{" +
                "id=" + id +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BigDecimal getDistancia() {
        return distancia;
    }

    public void setDistancia(BigDecimal distancia) {
        this.distancia = distancia;
    }

    public BigDecimal getFeromonio() {
        return feromonio;
    }

    public void setFeromonio(BigDecimal feromonio) {
        this.feromonio = feromonio;
    }

    public List<Cidade> getCidades() {
        return cidades;
    }

    public void setCidades(List<Cidade> cidades) {
        this.cidades = cidades;
    }
}
