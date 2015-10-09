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

    public static Double reduzirValor(Double valor){

        final Double valorT = Double.parseDouble(valor.toString().substring(0, obterTamanho(valor)));

        return valorT;
    }

    private static int obterTamanho(Double valor){

        final int LIMITE = 8;

        final int TAMANHO = (valor.toString().length() > LIMITE)?LIMITE:valor.toString().length();

        return TAMANHO;
    }

    public static boolean isArestaJaVisitada(Formiga formiga, Aresta aresta){

        for (Visita visita : formiga.getTour()) {

            if (visita.getAresta() != null && visita.getAresta().equals(aresta)){ //Formiga já visitou a aresta

                return true;
            }
        }

        return false;
    }

    private static boolean isCidadeJaVisitada(Formiga formiga, Aresta aresta){

        Cidade cidadePosicionada = formiga.getCidadePosicionada();

        List<Cidade> cidadesDaAresta = new ArrayList<>(aresta.getCidades()); //"new" evita que a remoção seja gravada na lista de cidades da aresta

        cidadesDaAresta.remove(cidadePosicionada);

        for (Visita visita : formiga.getTour()) {

            if (cidadesDaAresta.contains(visita.getCidade())) {

                return true;
            }
        }

        return false;
    }

    public static List<Aresta> obterArestasInexploradas(Formiga formiga){

        List<Aresta> arestasInexploradas = new ArrayList<>();

        for (Aresta aresta : ACS.arestas){

            if (aresta.getCidades().contains(formiga.getCidadePosicionada())){ //analisa apenas as arestas associadas à cidade aonde a formiga está posicionada

                if (!isArestaJaVisitada(formiga, aresta)){

                    arestasInexploradas.add(aresta);
                }
            }
        }

        //Pós-tratamento de arestas inexploradas: evita que a formiga re-visite uma cidade
        List<Aresta> arestasRejeitadas = new ArrayList<>();

        for (Aresta aresta : arestasInexploradas) {

            if (isCidadeJaVisitada(formiga, aresta)){

                arestasRejeitadas.add(aresta);
            }
        }

        arestasInexploradas.removeAll(arestasRejeitadas);

        return arestasInexploradas;
    }

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
