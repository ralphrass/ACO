package entidade;

/**
 * Created by Ralph on 07/10/2015.
 */
public class Visita {

    private Cidade cidade;
    private Aresta aresta;

    public Visita(Cidade cidade, Aresta aresta){

        this.cidade = cidade;
        this.aresta = aresta;
    }

    public Cidade getCidade() {
        return cidade;
    }

    public void setCidade(Cidade cidade) {
        this.cidade = cidade;
    }

    public Aresta getAresta() {
        return aresta;
    }

    public void setAresta(Aresta aresta) {
        this.aresta = aresta;
    }
}
