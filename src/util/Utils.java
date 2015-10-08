package util;

import entidade.Aresta;
import entidade.Formiga;
import entidade.Visita;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public static List<Aresta> obterArestasInexploradas(Formiga formiga){

        Set<Aresta> arestasInexploradas = new HashSet<>();

        boolean jaVistado = false;

        for (Aresta aresta : formiga.getCidadePosicionada().getArestas()){

            for (Visita visita : formiga.getTour()){

                if (visita.getAresta() != null && visita.getAresta().equals(aresta)){

                    jaVistado = true;
                }

                if (visita.getCidade().equals(aresta.getVizinho())){

                    jaVistado = true;
                }
            }

            if (!jaVistado)
                arestasInexploradas.add(aresta);
        }

        List<Aresta> arestas = new ArrayList<>(arestasInexploradas);

        return arestas;
    }
}
