package entidade;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ralph on 05/10/2015.
 */
public class Cidade {

    private int id;
    private Coordenada coordenada;
    private List<Aresta> arestas = new ArrayList<>();

    public Cidade(){ }

    public Cidade(int id, Coordenada coordenada){

        this.id = id;
        this.coordenada = coordenada;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cidade cidade = (Cidade) o;

        return id == cidade.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "entidade.Cidade{" +
                "id=" + id +
                '}';
    }

    public List<Aresta> getArestas() {
        return arestas;
    }

    public Coordenada getCoordenada() {
        return coordenada;
    }

    public int getId() {
        return id;
    }
}
