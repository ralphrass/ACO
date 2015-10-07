package entidade;

/**
 * Created by Ralph on 05/10/2015.
 */
public class Aresta {

    private int id;

    private Cidade vizinho;

    private double distancia;
    private double feromonio;

    public Aresta(int idP, Cidade vizinhoP, double distanciaP, double feromonioP){

        this.id = idP;
        this.vizinho = vizinhoP;
        this.distancia = distanciaP;
        this.feromonio = feromonioP;
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

    public Cidade getVizinho() {
        return vizinho;
    }

    public void setVizinho(Cidade vizinho) {
        this.vizinho = vizinho;
    }

    public double getDistancia() {
        return distancia;
    }

    public void setDistancia(double distancia) {
        this.distancia = distancia;
    }

    public double getFeromonio() {
        return feromonio;
    }

    public void setFeromonio(double feromonio) {
        this.feromonio = feromonio;
    }
}
