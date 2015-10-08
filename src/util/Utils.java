package util;

import entidade.Aresta;
import entidade.Formiga;
import entidade.Visita;

/**
 * Created by Ralph on 08/10/2015.
 */
public class Utils {

    /**
     * Auxiliar
     * */
    public static Double reduzirValor(Double valor){

        final Double valorT = Double.parseDouble(valor.toString().substring(0, obterTamanho(valor)));

        return valorT;
    }

    /**
     * Auxiliar
     * */
    private static int obterTamanho(Double valor){

        final int LIMITE = 8;

        final int TAMANHO = (valor.toString().length() > LIMITE)?LIMITE:valor.toString().length();

        return TAMANHO;
    }

    /**
     * Auxiliar
     * */
    public static boolean isCidadeJaVisitada(Formiga formiga, Aresta aresta){

        boolean jaVisitado = false;

        for (Visita visita : formiga.getTour()) {

            if (visita.getCidade().equals(aresta.getVizinho())){

                jaVisitado = true;
                break;
            }
        }

        return jaVisitado;
    }
}
