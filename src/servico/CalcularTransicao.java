package servico;

import entidade.Aresta;
import entidade.Formiga;
import util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Ralph on 08/10/2015.
 * Cálculo da função de transição (equações {1} e {3})
 */
public class CalcularTransicao {

    /**
     * Eq. {1} - probabilidade pseudo-randômica
     * */
    public static Aresta escolherPorProbabilidadePseudoRandomica(List<Aresta> arestas){

        int indice = 0;
        double denominador, probabilidadeLocal, pesoTotal = 0d;
        Aresta proximaAresta = null;

        double[] pesos = new double[arestas.size()];

        for (Aresta aresta : arestas) { //Explora todas as cidades adjacentes à cidade em que a formiga se encontra

            denominador = 0d;

            final double NUMERADOR = calcularCustoDaAresta(aresta);

            List<Aresta> arestasRestantes = new ArrayList<>(arestas);
            arestasRestantes.remove(aresta);

            //Somatório das arestas restantes para obter o denominador
            for (Aresta arestaRestante : arestasRestantes){

                denominador += calcularCustoDaAresta(arestaRestante);
            }

            probabilidadeLocal = NUMERADOR/denominador;

            pesoTotal += probabilidadeLocal;

            pesos[indice++] = probabilidadeLocal;
        }

        int indiceRandomico = -1;

        double valorRandomico = Math.random() * pesoTotal;

        for (int i=0; i<arestas.size(); i++){

            valorRandomico -= pesos[i];

            if (valorRandomico <= 0d){

                indiceRandomico = i;
                break;
            }
        }

        return arestas.get(indiceRandomico);
    }

    /**
     * Eq. {3} - arg max (exploitation)
     * */
    public static Aresta escolherAMelhorAresta(List<Aresta> arestas){

        double menorCustoLocal = 0, custoLocal;
        Aresta melhorAresta = null;

        for (Aresta aresta : arestas) {

            custoLocal = calcularCustoDaAresta(aresta);

            if (menorCustoLocal < custoLocal){

                menorCustoLocal = custoLocal;
                melhorAresta = aresta;
            }
        }

        return melhorAresta;
    }

    /**
     * Eq. {1} / Eq. {3} - Parte I (numerador)
     * */
    private static double calcularCustoDaAresta(Aresta aresta){

        Double feromonioLocal = aresta.getFeromonio() * obterDistanciaInversa(aresta);
        return feromonioLocal;
    }

    /**
     * Eq. {1} / Eq. {3} - (termo à direita)
     * */
    private static double obterDistanciaInversa(Aresta aresta){

        double ETA = 1 / aresta.getDistancia();
        Double distanciaInversa = Math.pow(ETA, ACS.BETA);

        return distanciaInversa;
    }
}
