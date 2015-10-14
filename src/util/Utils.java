package util;

import entidade.Aresta;
import entidade.Cidade;
import entidade.Formiga;
import entidade.Visita;
import servico.ACS;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ralph on 08/10/2015.
 */
public class Utils {

    /**
     * Analisa as arestas adjacentes � cidade aonde a formiga est� posicionada
     * */
    public static List<Aresta> obterArestasInexploradas(Formiga formiga){

        List<Aresta> arestasInexploradas = new ArrayList<>();

        for (Aresta aresta : ACS.arestas){

            if (aresta.getCidades().contains(formiga.getCidadePosicionada())){

                if (ACS.DESCONSIDERAR_ARESTAS_VISITADAS){

                    if (!isArestaJaVisitada(formiga, aresta)) {

                        arestasInexploradas.add(aresta);
                    }

                } else {

                    arestasInexploradas.add(aresta);
                }
            }
        }

        if (ACS.DESCONSIDERAR_CIDADES_VISITADAS) {

            //P�s-tratamento de arestas inexploradas: evita que a formiga re-visite uma cidade
            List<Aresta> arestasRejeitadas = new ArrayList<>();

            for (Aresta aresta : arestasInexploradas) {

                if (isCidadeJaVisitada(formiga, aresta)) {

                    arestasRejeitadas.add(aresta);
                }
            }

            arestasInexploradas.removeAll(arestasRejeitadas);
        }

        return arestasInexploradas;
    }

    public static boolean isArestaJaVisitada(Formiga formiga, Aresta aresta){

        for (Visita visita : formiga.getTour()) {

            if (visita.getAresta() != null && visita.getAresta().equals(aresta)){ //Formiga j� visitou a aresta

                return true;
            }
        }

        return false;
    }

    private static boolean isCidadeJaVisitada(Formiga formiga, Aresta aresta){

        Cidade cidadePosicionada = formiga.getCidadePosicionada();

        List<Cidade> cidadesDaAresta = new ArrayList<>(aresta.getCidades()); //"new" evita que a remo��o seja gravada na lista de cidades da aresta

        cidadesDaAresta.remove(cidadePosicionada);

        for (Visita visita : formiga.getTour()) {

            if (cidadesDaAresta.contains(visita.getCidade())) {

                return true;
            }
        }

        return false;
    }

    /**
     * Identifica para onde a formiga est� indo com base na cidade aonde ela est� posicionada.
     * Apesar de percorrer o vetor de cidades, h� apenas duas possibilidades em uma aresta.
     * */
    public static Cidade obterCidadeDestino(Cidade origem, Aresta aresta){

        Cidade destino = null;

        for (Cidade cidade : aresta.getCidades()){

            if (!origem.equals(cidade)){

                destino = cidade;
                break;
            }
        }

        return destino;
    }
}
