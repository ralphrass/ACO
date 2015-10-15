package servico;

import entidade.Aresta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ralph on 08/10/2015.
 * Cálculo da função de transicao (equações {1} e {3})
 */
public class CalcularTransicao {

    /**
     * Eq. {1} - probabilidade pseudo-randômica (exploration)
     * Peso: Divisão do feromônio de cada aresta adjacente pela distância inversa entre as 2 cidades.
     * Soma os pesos de todas as arestas e escolhe uma, randomicamente, subtraindo o peso de cada aresta do total.
     * Quando o peso total for menor ou igual a ZERO, escolhe aquela aresta.
     * */
    public static Aresta escolherPorProbabilidadePseudoRandomica(List<Aresta> arestas){

        int indice = 0;
        BigDecimal numerador = new BigDecimal(0), denominador, probabilidadeLocal, pesoTotal = new BigDecimal(0);
        Aresta proximaAresta = null;
        List<Aresta> arestasRestantes;

        BigDecimal[] pesos = new BigDecimal[arestas.size()];

        for (Aresta aresta : arestas) {

            denominador = new BigDecimal(0);

            try {

                numerador = calcularCustoDaAresta(aresta);

                arestasRestantes = new ArrayList<>(arestas);
                arestasRestantes.remove(aresta);

                //Somatório das arestas restantes para obter o denominador
                for (Aresta arestaRestante : arestasRestantes) {

                    denominador = denominador.add(calcularCustoDaAresta(arestaRestante));
                }

                probabilidadeLocal = numerador.divide(denominador, ACS.CASAS_DECIMAIS, RoundingMode.HALF_UP);

                pesoTotal = pesoTotal.add(probabilidadeLocal);

                pesos[indice++] = probabilidadeLocal;

            } catch (ArithmeticException e) {

                System.out.println("Falha ao calcular probabilidade para a aresta " + aresta + ". Numerador: " + numerador.setScale(12, RoundingMode.HALF_UP) + " Denominador: "+denominador+". Arestas: "+arestas.size());
                //e.printStackTrace();
            }
        }

        int indiceRandomico = -1;

        BigDecimal valorRandomico = pesoTotal.multiply(new BigDecimal(Math.random()));

        for (int i=0; i<arestas.size(); i++){

            valorRandomico = valorRandomico.subtract(pesos[i]);

            int diferenca = valorRandomico.compareTo(new BigDecimal(0));

            if (diferenca == 0 || diferenca == -1){ // valorRandomico <= 0

                indiceRandomico = i;
                break;
            }
        }

        try {

            return arestas.get(indiceRandomico);

        } catch (Exception e) {

            System.out.println("Falha ao obter o indice: "+indiceRandomico+". Quantidade de arestas: "+arestas.size());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Eq. {3} - arg max (exploitation)
     * */
    public static Aresta escolherAMelhorAresta(List<Aresta> arestas){

        BigDecimal menorCustoLocal = new BigDecimal(0), custoLocal;
        Aresta melhorAresta = null;

        for (Aresta aresta : arestas) {

            custoLocal = calcularCustoDaAresta(aresta);

            int diferenca = menorCustoLocal.compareTo(custoLocal);

            if (diferenca == -1){ // menorCustoLocal < custoLocal

                menorCustoLocal = custoLocal;
                melhorAresta = aresta;
            }
        }

        return melhorAresta;
    }

    /**
     * Eq. {1} / Eq. {3} - Parte I (numerador)
     * */
    private static BigDecimal calcularCustoDaAresta(Aresta aresta){

        //BigDecimal feromonioLocal = aresta.getFeromonio().multiply(obterDistanciaInversa(aresta)).setScale(ACS.CASAS_DECIMAIS, RoundingMode.HALF_UP);
        BigDecimal feromonioLocal = aresta.getFeromonio().multiply(obterDistanciaInversa(aresta));

        return feromonioLocal;
    }

    /**
     * Eq. {1} / Eq. {3} - (termo à direita)
     * */
    private static BigDecimal obterDistanciaInversa(Aresta aresta){

        try {

            BigDecimal ETA = new BigDecimal(Math.pow( (1d/aresta.getDistancia().doubleValue()), ACS.BETA));

            return ETA;

        } catch (Exception e) {

            System.out.println("Falha ao obter distância inversa para a aresta: " + aresta);

            if (aresta != null){

                System.out.println(" de distância: "+aresta.getDistancia());

            } else {

                System.out.println(" esta aresta não possui distância");
            }

            e.printStackTrace();
        }

        return null;
    }
}
