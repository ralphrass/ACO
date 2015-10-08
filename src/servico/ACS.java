package servico;

import entidade.Aresta;
import entidade.Cidade;
import entidade.Formiga;
import entidade.Visita;
import util.GerarResultado;
import util.Utils;

import java.util.*;

/**
 * Created by Ralph on 05/10/2015.
 */
public class ACS {

    public static final int NR_ITERACOES = 10;

    public static final int NR_FORMIGAS = 10; //padrão = 10. Não pode exceder o número de cidades

    public static final double RHO = 0.1;  //Deve ser > 0 e < 1    Padrao = 0.1 (?) Taxa de Evaporação
    public static final double ALFA = RHO; //Deve ser > 0 e < 1    Padrao = RHO (?)
    public static final double BETA = 1d;  //Deve ser > 0          Padrao = 2 (?)
    private static final double q0 = 0.0;   //Deve ser 0 <= q0 <= 1 Padrao = 0.0

    public static double TAU_ZERO; //Calculado pelo Algoritmo do Vizinho mais Próximo
    public static double L_BEST; //Menor distância entre origem e destino

    public static final int ID_CIDADE_ORIGEM = 0;
    public static final int ID_CIDADE_DESTINO = 1;

    public static List<Formiga> formigas = new ArrayList<>();
    public static List<Cidade> cidades;

    private static List<List<Visita>> melhoresTours = new ArrayList<>();

    /**
     * Fase {3}
     * Move todas as formigas da cidade inicial até o destino, armazenando os caminhos tomados
     * */
    private static Set<Formiga> moverFormigas(){

        int nrFormigasComTourCompleto = 0;
        Set<Formiga> formigasComTourCompleto = new HashSet<>();

        final int NR_CIDADES = ACS.cidades.size();

        do { //Até que todas as formigas completem o Tour

            for (int i=0; i<=NR_CIDADES; i++){

                if (nrFormigasComTourCompleto == ACS.NR_FORMIGAS)
                    i=NR_CIDADES;

                if (i < NR_CIDADES) { //Ida

                    for (Formiga formiga : ACS.formigas) {

                        if (formiga.getCidadePosicionada().getId() == ID_CIDADE_DESTINO) { //Formiga no destino final

                            formigasComTourCompleto.add(formiga);
                            nrFormigasComTourCompleto++;
                            continue;
                        }

                        Aresta proximaAresta = escolherProximaAresta(formiga);

                        if (proximaAresta == null) { //Formiga em loop

                            nrFormigasComTourCompleto++;
                            continue;
                        }

                        formiga.setCidadePosicionada(proximaAresta.getVizinho());
                        formiga.getTour().add(new Visita(proximaAresta.getVizinho(), proximaAresta));

                        if (formiga.getTour().size() == 2) { //É a primeira visita da formiga, saindo da sua cidade inicial

                            formiga.getTour().get(0).setAresta(proximaAresta); //Mesma aresta da próxima cidade
                        }

                        //Acumula depósito de feromônio (regra {02})
                        final double DELTA_TAU = 1 / proximaAresta.getDistancia();
                        formiga.getDeltaTau().put(proximaAresta, DELTA_TAU);
                    }

                } else { //Volta

                    for (Formiga formiga : ACS.formigas) {

                        for (int j=(formiga.getTour().size()-1); j>0; j--){ //Percorre o Tour inverso

                            //Obtém a cidade anterior à cidade atual no Tour
                            final Cidade cidadeRetorno = formiga.getTour().get(j-1).getCidade();

                            //Acumula o DELTA_TAU de um passo em direção à origem
                            final double DELTA_TAU = 1 / formiga.getTour().get(j).getAresta().getDistancia();
                            final double DELTA_TAU_PRESENTE = formiga.getDeltaTau().get(formiga.getTour().get(j).getAresta());

                            formiga.getDeltaTau().put(formiga.getTour().get(j).getAresta(), DELTA_TAU_PRESENTE + DELTA_TAU);

                            //Move a formiga para a cidade anterior
                            formiga.setCidadePosicionada(cidadeRetorno);
                        }
                    }
                }
            }

            for (Formiga formiga : ACS.formigas) {

                for (Visita visita : formiga.getTour()){

                    Double feromonio = computarFeromonioLocal(visita.getAresta(), formiga);
                    atualizarFeromonioLocal(feromonio, visita.getAresta());
                }
            }

        } while (nrFormigasComTourCompleto < ACS.formigas.size());

        return formigasComTourCompleto;
    }

    /**
     * Eq. {5}  ?(rk ,sk) := (1-?) ?(rk ,sk) + ? ?0
     * */
    private static Double computarFeromonioLocal(Aresta aresta, Formiga formiga){

        final double FEROMONIO_DEPOSITADO = formiga.getDeltaTau().get(aresta);
        final double FEROMONIO_PRESENTE = aresta.getFeromonio();

        Double feromonio = (1 - RHO) * (FEROMONIO_PRESENTE + FEROMONIO_DEPOSITADO) + RHO * TAU_ZERO;

        return feromonio;
    }

    /**
     * Fase {3} - Evaporação de feromônios e reinicilização de Tours
     * */
    private static void atualizarFeromonioGlobal(Set<Formiga> formigasComTourCompleto){

        double Lbest = Math.pow(L_BEST, 2); //"piora" o melhor Tour
        List<Visita> melhorTour = new ArrayList<>();

        //Encontra a formiga com o melhor Tour
        for (Formiga formiga : formigasComTourCompleto) {

            Double Lk = obterDistanciaDoTour(formiga);

            if (Lk < Lbest){

                Lbest = Lk;
                melhorTour = formiga.getTour();
            }
        }

        melhoresTours.add(melhorTour);

        List<Aresta> arestasJaVisitadas = new ArrayList<>();

        //Percorre grafo inteiro e atualiza os feromônios das arestas
        for (Cidade cidade : ACS.cidades){

            for (Aresta aresta : cidade.getArestas()){

                if (arestasJaVisitadas.contains(aresta))
                    continue;

                arestasJaVisitadas.add(aresta);

                Double feromonio = computarFeromonioGlobal(aresta, Lbest, melhorTour);

                aresta.setFeromonio(feromonio);
            }
        }
    }

    /**
     * Eq. {4}
     * */
    private static Double computarFeromonioGlobal(Aresta aresta, Double Lbest, List<Visita> melhorTour){

        double DELTA_TAU;
        boolean isArestaNoMelhorTour = false;

        for (Visita visita : melhorTour) {

            if (visita.getAresta() != null &&
                    visita.getAresta().equals(aresta)) {

                isArestaNoMelhorTour = true;
                break;
            }
        }

        if (isArestaNoMelhorTour){

            DELTA_TAU = Math.pow(Lbest, -1);

        } else {

            DELTA_TAU = 0;
        }

        Double feromonio = (1 - ALFA) * aresta.getFeromonio() + ALFA * DELTA_TAU;

        return feromonio;
    }

    /**
     * Fase {3} - "Compute Lk"
     * */
    private static Double obterDistanciaDoTour(Formiga formiga){

        double distancia = 0d;

        for (Visita visita : formiga.getTour())
            if (visita.getAresta() != null) //Ignora a cidade inicial
                distancia += visita.getAresta().getDistancia();

        return distancia;
    }

    private static void atualizarFeromonioLocal(double feromonio, Aresta proximaAresta){

        for (Cidade cidade : ACS.cidades){

            for (Aresta aresta : cidade.getArestas()){

                if (aresta.equals(proximaAresta)){

                    aresta.setFeromonio(feromonio);
                }
            }
        }
    }

    /**
     * Eq. {3}
     * */
    private static Aresta escolherProximaAresta(Formiga formiga){

        Aresta proximaAresta = null;

        double q = Math.random();

        if (q <= q0){

            //Percorre todas as cidades adjacentes e calcula a transição, favorecendo a maior (+ feromônio)
            proximaAresta = calcularTransicao(formiga);

        } else {

            proximaAresta = obterCidadePorProbabilidadePonderada(formiga);
        }

        return proximaAresta;
    }

    /**
     * Eq. {1} - probabilidade ponderada
     * */
    private static Aresta obterCidadePorProbabilidadePonderada(Formiga formiga){

        double denominador, probabilidadeLocal, probabilidadeGlobal = 0d;
        Aresta proximaAresta = null;

        List<Aresta> arestas = formiga.getCidadePosicionada().getArestas();

        for (Aresta aresta : arestas) { //Explora todas as cidades adjacentes à posição atual

            if (Utils.isCidadeJaVisitada(formiga, aresta)) //Ignora arestas já visitadas
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
                proximaAresta = aresta;
            }
        }

        return proximaAresta;
    }

    /**
     * Eq. {3} - arg max
     * */
    private static Aresta calcularTransicao(Formiga formiga){

        double transicaoGlobal = 0, transicaoLocal;
        Aresta melhorAresta = null;

        List<Aresta> arestas = formiga.getCidadePosicionada().getArestas();

        for (Aresta aresta : arestas) {

            if (Utils.isCidadeJaVisitada(formiga, aresta))
                continue;

            transicaoLocal = calcularFeromonioLocal(aresta);

            if (transicaoGlobal < transicaoLocal){

                transicaoGlobal = transicaoLocal;
                melhorAresta = aresta;
            }
        }

        return melhorAresta;
    }

    /**
     * Eq. {1} / Eq. {3} - Parte I (numerador)
     * */
    private static double calcularFeromonioLocal(Aresta aresta){

        Double feromonioLocal = aresta.getFeromonio() * obterDistanciaInversa(aresta);
        return feromonioLocal;
    }

    /**
     * Eq. {1} / Eq. {3} - (termo à direita)
     * */
    private static double obterDistanciaInversa(Aresta aresta){

        double ETA = 1 / aresta.getDistancia();
        Double distanciaInversa = Math.pow(ETA, BETA);

        return distanciaInversa;
    }

    public static void main(String args[]){

        //Fase {1} - Monta o grafo e inicializa os feromônios respeitando a regra do caminho mais curto
        Inicializar.inicializarGrafo();

        if (NR_FORMIGAS > ACS.cidades.size()){

            System.out.print("Numero de Formigas deve ser menor que o numero de cidades");
            return;
        }

        for (int i=0; i<NR_ITERACOES; i++){

            //Fase {2}
            Set<Formiga> formigas = moverFormigas();

            //Fase {3}
            atualizarFeromonioGlobal(formigas);

            ACS.formigas = new ArrayList<>();
            Inicializar.inicializarFormigas();
        }

        GerarResultado.exportarResultado();
    }
}