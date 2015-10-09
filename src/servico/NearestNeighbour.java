package servico;

import entidade.Aresta;
import entidade.Cidade;
import util.Utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ralph on 07/10/2015.
 */
public class NearestNeighbour {

    public static BigDecimal obterDistanciaDoMenorTour(){

        BigDecimal Lnn = new BigDecimal(0); //distância total do Visita
        List<Cidade> cidadesVisitadas = new ArrayList<>();

        Cidade cidadeCorrente = ACS.cidades.get(ACS.ID_CIDADE_ORIGEM);
        cidadesVisitadas.add(cidadeCorrente);

        for (int i=0; i< ACS.cidades.size(); i++){

            //2. Encontra a aresta mais curta entre a cidade corrente e uma cidade vizinha que ainda não foi visitada
            Aresta aresta = obterArestaMaisCurta(cidadeCorrente, cidadesVisitadas);

            //3. Atualiza a cidade corrente
            cidadeCorrente = Utils.obterCidadeDestino(cidadeCorrente, aresta);

            //4. Marca a cidade corrente como já visitada
            cidadesVisitadas.add(cidadeCorrente);

            Lnn = Lnn.add(aresta.getDistancia());

            //5. Se chegou à cidade destino, sai fora
            if (cidadeCorrente.equals(ACS.cidades.get(ACS.ID_CIDADE_DESTINO))){

                break;
            }

            //6. Retorna ao passo 2
        }

        return Lnn;
    }

    /**
     * Fase (1) - Parte III/III (II)
     * */
    private static Aresta obterArestaMaisCurta(Cidade cidade, List<Cidade> cidadesVisitadas){

        Aresta arestaMaisCurta = null;

        for (Aresta aresta : ACS.arestas){

            if (aresta.getCidades().contains(cidade)){

                if (arestaMaisCurta == null){

                    arestaMaisCurta = aresta;

                } else if (arestaMaisCurta.getDistancia().compareTo(aresta.getDistancia()) == 1) { //arestaMaisCurta.getDistancia() > aresta.getDistancia()

                    arestaMaisCurta = aresta;
                }
            }
        }

        return arestaMaisCurta;
    }
}
