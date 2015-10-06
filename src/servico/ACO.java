package servico;

import entidade.Aresta;
import entidade.Cidade;
import entidade.Formiga;
import util.LerArquivo;

import java.util.*;

/**
 * Created by Ralph on 05/10/2015.
 */
public class ACO {

    private static final int NR_FORMIGAS = 1; //padrão = 10

    private static final double RHO = 0.1;  //Deve ser > 0 e < 1    Padrao = 0.1
    private static final double ALFA = RHO; //Deve ser > 0 e < 1    Padrao = RHO
    private static final double BETA = 2d;  //Deve ser > 0          Padrao = 2
    private static final double q0 = 0.9;   //Deve ser 0 <= q0 <= 1 Padrao = 0.0

    private static final int ID_CIDADE_ORIGEM = 0;
    private static final int ID_CIDADE_DESTINO = 1;

    private static List<Formiga> formigas = new ArrayList<>();
    private static List<Cidade> cidades;
    private static List<Aresta> arestas;

    private static final String ARQUIVO = "C:\\Users\\Ralph\\workspace-gpin-intellij\\ACO\\src\\recursos\\cidades_12.txt";

    /**
     * Fase (1) - Parte I/III
     * Importa o TXT e popula os vetores de cidades e arestas
     * */
    private static void inicializarCidades(){

        LerArquivo.lerArquivo(ARQUIVO);

        ACO.cidades = LerArquivo.cidades;
        ACO.arestas = LerArquivo.arestas;
    }

    /**
     * Fase (1) - Parte II/III
     * TODO Posiciona todas as formigas em cidades randomicamente (ao menos uma por cidade)
     * */
    private static void inicializarFormigas(){

        for (int i=0; i<NR_FORMIGAS; i++){

            Formiga formiga = new Formiga(i, ACO.cidades.get(ID_CIDADE_ORIGEM));
            formiga.getTour().add(ACO.cidades.get(ID_CIDADE_ORIGEM));
            formigas.add(formiga);
        }
    }

    /**
     * Fase (1) - Parte III/III
     * Executa o algoritmo do vizinho mais próximo (Nearest Neighbour)
     * */
    private static void inicializarFeromonios(){

        double Lnn = 0d; //distância total do Tour
        List<Cidade> cidadesVisitadas = new ArrayList<>();

        //1. Inicia em uma cidade qualquer
        Cidade cidadeCorrente = ACO.cidades.get(ID_CIDADE_ORIGEM);
        cidadesVisitadas.add(cidadeCorrente);

        for (int i=0; i<ACO.cidades.size(); i++){

            //2. Encontra a aresta mais curta entre a cidade corrente e uma cidade vizinha que ainda não foi visitada
            Aresta aresta = obterArestaMaisCurta(cidadeCorrente, cidadesVisitadas);

            //3. Atualiza a cidade corrente
            cidadeCorrente = aresta.getVizinho();

            //4. Marca a cidade corrente como já visitada
            cidadesVisitadas.add(cidadeCorrente);

            Lnn += aresta.getDistancia();

            //5. Se chegou à cidade destino, sai fora
            if (cidadeCorrente.equals(ACO.cidades.get(ID_CIDADE_DESTINO))){

                break;
            }

            //6. Retorna ao passo 2
        }

        //Atualiza o feromônio de todas as arestas
        for (Cidade cidade : ACO.cidades) {

            for (Aresta aresta : cidade.getArestas()){

                if (aresta.getFeromonio() == 0){

                    aresta.setFeromonio(Math.pow(ACO.cidades.size() * Lnn, -1));
                }
            }
        }

        System.out.print(ACO.cidades);
    }

    /**
     * Fase (1) - Parte III/III (II)
     * */
    private static Aresta obterArestaMaisCurta(Cidade cidade, List<Cidade> cidadesVisitadas){

        Aresta arestaMaisCurta = null;

        for (Aresta aresta : cidade.getArestas()){

            if (cidadesVisitadas.contains(aresta.getVizinho())){

                continue;
            }

            if (arestaMaisCurta == null){

                arestaMaisCurta = aresta;

            } else if (arestaMaisCurta.getDistancia() > aresta.getDistancia()) {

                arestaMaisCurta = aresta;
            }
        }

        return arestaMaisCurta;
    }

    /**
     * Fase (2)
     * Move todas as formigas da cidade inicial até o destino, armazenando os caminhos tomados
     * */
    private static void moverFormigas(){

        final int TOTAL_CIDADES = ACO.cidades.size();

        for (int i=0; i<=ACO.cidades.size(); i++) {

            //Move cada uma das formigas para a próxima cidade
            if ( i < TOTAL_CIDADES ) {

                for (Formiga formiga : ACO.formigas) {

                    //Se esta formiga já está no destino final, ignora
                    if (formiga.getCidadePosicionada().getId() == ID_CIDADE_DESTINO)
                        continue;

                    Cidade proximaCidade = escolherProximaCidade(formiga);

                    formiga.setCidadePosicionada(proximaCidade);
                    formiga.getTour().add(proximaCidade);
                }

            } else { //Volta


            }

            //TODO Atualiza feromônio
            for (Formiga formiga : ACO.formigas) {


            }
        }
    }

    /**
     * Regra (3)
     * */
    private static Cidade escolherProximaCidade(Formiga formiga){

        Cidade proximaCidade = null;

        double q = Math.random();

        if (q <= q0){

            //Percorre todas as cidades adjacentes e calcula a transição, favorecendo a maior (+ feromônio)
            proximaCidade = calcularTransicao(formiga);

        } else {

            proximaCidade = obterCidadePorProbabilidadePonderada(formiga);
        }

        return proximaCidade;
    }

    /**
     * Regra (1) - probabilidade ponderada
     * */
    private static Cidade obterCidadePorProbabilidadePonderada(Formiga formiga){

        double denominador, probabilidadeLocal, probabilidadeGlobal = 0d;
        Cidade proximaCidade = new Cidade();

        List<Aresta> arestas = formiga.getCidadePosicionada().getArestas();

        //Explora todas as cidades adjacentes à posição atual
        for (Aresta aresta : arestas) {

            //Ignora cidades já visitadas
            if (formiga.getTour().contains(aresta.getVizinho()))
                continue;

            denominador = 0d;

            final double NUMERADOR = calcularFeromonioLocal(aresta);

            for (int j=0; j<arestas.size(); j++){

                if (arestas.get(j) == aresta) //Ignora a aresta corrente
                    continue;

                denominador += calcularFeromonioLocal(arestas.get(j));
            }

            probabilidadeLocal = NUMERADOR/denominador;

            if (probabilidadeLocal > probabilidadeGlobal){

                probabilidadeGlobal = probabilidadeLocal;
                proximaCidade = aresta.getVizinho();
            }
        }

        return proximaCidade;
    }

    /**
     * Regra (3) - arg max
     * */
    private static Cidade calcularTransicao(Formiga formiga){

        double transicaoGlobal = 0, transicaoLocal;
        Cidade melhorCidade = null;

        List<Aresta> arestas = formiga.getCidadePosicionada().getArestas();

        for (Aresta aresta : arestas) {

            //Ignora cidades já visitadas
            if (formiga.getTour().contains(aresta.getVizinho()))
                continue;

            transicaoLocal = calcularFeromonioLocal(aresta);

            if (transicaoGlobal < transicaoLocal){

                transicaoGlobal = transicaoLocal;
                melhorCidade = aresta.getVizinho();
            }
        }

        return melhorCidade;
    }

    /**
     * Regra (1) / Regra (3) - Parte I (numerador)
     * */
    private static double calcularFeromonioLocal(Aresta aresta){

        double feromonioLocal = aresta.getFeromonio() * obterDistanciaInversa(aresta);

        return feromonioLocal;
    }

    /**
     * Regra (1) / Regra (3) - (termo à direita)
     * */
    private static double obterDistanciaInversa(Aresta aresta){

        double distanciaInversa = Math.pow((1 / aresta.getDistancia()), BETA);

        return distanciaInversa;
    }

    public static void main(String args[]){

        //Fase (1)
        inicializarCidades();
        inicializarFeromonios();
        inicializarFormigas();

        //Fase (2)
        moverFormigas();
    }
}