package entidade;

/**
 * Created by Ralph on 05/10/2015.
 */
public class Aresta {

    /*private Cidade cidadeOrigem;
    private Cidade cidadeDestino;*/

    private Cidade vizinho;

    private double distancia;
    private double feromonio;

    public Aresta(Cidade vizinhoP, double distanciaP, double feromonioP){

        this.vizinho = vizinhoP;
        this.distancia = distanciaP;
        this.feromonio = feromonioP;
    }

    /*public Aresta(Cidade cid1, Cidade cid2){

        this.cidadeOrigem = cid1;
        this.cidadeDestino = cid2;
    }

    public Aresta(Cidade cid1, Cidade cid2, double distanciaP, double feromonioP){

        this.cidadeOrigem = cid1;
        this.cidadeDestino = cid2;
        this.distancia = distanciaP;
        this.feromonio = feromonioP;
    }*/

    /*public Cidade getCidadeOrigem() {
        return cidadeOrigem;
    }

    public void setCidadeOrigem(Cidade cidade1) {
        this.cidadeOrigem = cidade1;
    }

    public Cidade getCidadeDestino() {
        return cidadeDestino;
    }

    public void setCidadeDestino(Cidade cidade2) {
        this.cidadeDestino = cidade2;
    }*/

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
