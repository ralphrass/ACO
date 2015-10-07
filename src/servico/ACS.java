package servico;

import entidade.Aresta;
import entidade.Cidade;
import entidade.Formiga;
import entidade.Visita;
import util.GerarResultado;
import util.LerArquivo;

import java.util.*;

/**
 * Created by Ralph on 05/10/2015.
 */
public class ACS {

    public static final int NR_ITERACOES = 1;

    public static final int NR_FORMIGAS = 2; //padrão = 10. Não pode exceder o número de cidades

    public static final double RHO = 0.1;  //Deve ser > 0 e < 1    Padrao = 0.1 (?) Taxa de Evaporação
    public static final double ALFA = RHO; //Deve ser > 0 e < 1    Padrao = RHO (?)
    public static final double BETA = 2d;  //Deve ser > 0          Padrao = 2 (?)
    private static final double q0 = 0.9;   //Deve ser 0 <= q0 <= 1 Padrao = 0.0

    private static double TAU_ZERO; //Calculado pelo Algoritmo do Vizinho mais Próximo
    private static double L_BEST; //Menor distância entre origem e destino

    public static final int ID_CIDADE_ORIGEM = 0;
    public static final int ID_CIDADE_DESTINO = 1;

    private static List<Formiga> formigas = new ArrayList<>();
    public static List<Cidade> cidades;
    //private static List<Cidade> MELHOR_SOLUCAO_GLOBAL; //Visita realizada pelo Algoritmo do Vizinho mais Próximo
    //private static List<Aresta> arestas;

    private static final String ARQUIVO = "C:\\Users\\Ralph\\workspace-gpin-intellij\\ACO\\src\\recursos\\cidades_12.txt";

    /**
     * Fase (1) - Parte I/III
     * Importa o TXT e popula os vetores de cidades e arestas
     * */
    private static void inicializarCidades(){

        LerArquivo.lerArquivo(ARQUIVO);

        ACS.cidades = LerArquivo.cidades;
        //ACS.arestas = LerArquivo.arestas;
    }

    /**
     * Fase (1) - Parte II/III - Posiciona formigas em cidades de forma randômica
     * */
    private static void inicializarFormigas(){

        for (int i=0; i<NR_FORMIGAS; i++){

            Cidade cidade = ACS.cidades.get((int)(Math.random()* ACS.cidades.size()));

            Formiga formiga = new Formiga(i, cidade);
            formiga.getTour().add(new Visita(cidade, null));

            formigas.add(formiga);
        }
    }

    /**
     * Fase (1) - Parte III/III
     * Executa o algoritmo do vizinho mais próximo (Nearest Neighbour)
     * */
    private static void inicializarFeromonios(){

        L_BEST = NearestNeighbour.obterDistanciaDoMenorTour();

        final int TAMANHO = ACS.cidades.size();
        final Double LNN_INVERSO = Math.pow(TAMANHO * L_BEST, -1); //Pg. 56, último parágrafo

        TAU_ZERO = reduzirValor(LNN_INVERSO);

        //Atualiza o feromônio de todas as arestas
        for (Cidade cidade : ACS.cidades) {

            for (Aresta aresta : cidade.getArestas()){

                if (aresta.getFeromonio() == 0){

                    aresta.setFeromonio(TAU_ZERO);
                }
            }
        }

        //System.out.print(ACS.cidades);
    }

    /**
     * Fase (2)
     * Move todas as formigas da cidade inicial até o destino, armazenando os caminhos tomados
     * */
    private static Set<Formiga> moverFormigas(){

        int nrFormigasComTourCompleto = 0;
        Set<Formiga> formigasComTourCompleto = new HashSet<>();

        //TODO repetir durante "n" iterações

        final int NR_CIDADES = ACS.cidades.size();

        do { //Até que todas as formigas completem o Tour

            for (int i=0; i<=NR_CIDADES; i++){

                if (i < NR_CIDADES) { //Ida

                    for (Formiga formiga : ACS.formigas) {

                        if (formiga.getCidadePosicionada().getId() == ID_CIDADE_DESTINO) { //Formiga no destino final

                            formigasComTourCompleto.add(formiga);
                            nrFormigasComTourCompleto++;
                            continue;
                        }

                        Aresta proximaAresta = escolherProximaCidade(formiga);

                        if (proximaAresta == null) { //Formiga em loop

                            nrFormigasComTourCompleto++;
                            continue;
                        }

                        formiga.setCidadePosicionada(proximaAresta.getVizinho());
                        formiga.getTour().add(new Visita(proximaAresta.getVizinho(), proximaAresta));

                        if (formiga.getTour().size() == 2) { //É a primeira visita da formiga, saindo da sua cidade inicial

                            formiga.getTour().get(0).setAresta(proximaAresta); //Mesma aresta da próxima cidade
                        }

                        Double feromonio = computarFeromonioLocal(proximaAresta);
                        atualizarFeromonioLocal(feromonio, proximaAresta);

                        System.out.println(feromonio);
                    }

                } else { //Volta TODO (???)

                    for (Formiga formiga : ACS.formigas) {

                        formiga.setCidadePosicionada(formiga.getCidadeInicial());
                        formiga.getTour().add(new Visita(formiga.getCidadeInicial(), null));
                    }
                }
            }

        } while (nrFormigasComTourCompleto < ACS.formigas.size());

        return formigasComTourCompleto;
    }

    /**
     * Regra (5)
     * */
    private static Double computarFeromonioLocal(Aresta aresta){

        Double feromonio = (1 - RHO) * aresta.getFeromonio() + RHO * TAU_ZERO;
        feromonio = reduzirValor(feromonio);

        return feromonio;
    }

    /**
     * Fase (3) - "Compute Lbest", Update edges belonging to Lbest using (4)
     * */
    private static void atualizarFeromonioGlobal(Set<Formiga> formigasComTourCompleto){

        double Lbest = L_BEST;
        List<Visita> melhorTour = new ArrayList<>();

        //Encontra a formiga com o melhor Tour
        for (Formiga formiga : formigasComTourCompleto) {

            Double Lk = obterDistanciaDoTour(formiga);

            if (Lk < Lbest){

                Lbest = Lk;
                melhorTour = formiga.getTour();
            }
        }

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

        /*for (Visita visita : melhorTour){

            Double feromonio = computarFeromonioGlobal(visita, Lbest);

            if (visita.getAresta() != null) //Ignora a cidade inicial
                visita.getAresta().setFeromonio(feromonio);
        }*/
    }

    /**
     * Regra (4)
     * */
    private static Double computarFeromonioGlobal(Aresta aresta, Double Lbest, List<Visita> melhorTour){

        double DELTA_TAU;
        boolean isArestaNoMelhorTour = false;

        for (Visita visita : melhorTour) {

            if (visita.getAresta().equals(aresta)) {

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
        feromonio = reduzirValor(feromonio);

        return feromonio;
    }

    /**
     * Fase (3) - "Compute Lk"
     * */
    private static Double obterDistanciaDoTour(Formiga formiga){

        double distancia = 0d;

        for (Visita visita : formiga.getTour())
            if (visita.getAresta() != null) //Ignora a cidade inicial
                distancia += visita.getAresta().getDistancia();

        return distancia;
    }

    //TODO - Possivelmente modificar como a trilha é armazenada
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
     * Regra (3)
     * */
    private static Aresta escolherProximaCidade(Formiga formiga){

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
     * Regra (1) - probabilidade ponderada
     * */
    private static Aresta obterCidadePorProbabilidadePonderada(Formiga formiga){

        double denominador, probabilidadeLocal, probabilidadeGlobal = 0d;
        Aresta proximaAresta = null;

        List<Aresta> arestas = formiga.getCidadePosicionada().getArestas();

        for (Aresta aresta : arestas) { //Explora todas as cidades adjacentes à posição atual

            if (isArestaJaVisitada(formiga, aresta)) //Ignora arestas já visitadas
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
     * Regra (3) - arg max
     * */
    private static Aresta calcularTransicao(Formiga formiga){

        double transicaoGlobal = 0, transicaoLocal;
        Aresta melhorAresta = null;

        List<Aresta> arestas = formiga.getCidadePosicionada().getArestas();

        for (Aresta aresta : arestas) {

            if (isArestaJaVisitada(formiga, aresta)) //Ignora arestas já visitadas
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
     * Auxiliar
     * */
    private static boolean isArestaJaVisitada(Formiga formiga, Aresta aresta){

        boolean jaVisitado = false;

        for (Visita visita : formiga.getTour())
            if (visita.getAresta() != null && visita.getAresta().equals(aresta))
                jaVisitado = true;

        return jaVisitado;
    }

    /**
     * Regra (1) / Regra (3) - Parte I (numerador)
     * */
    private static double calcularFeromonioLocal(Aresta aresta){

        Double feromonioLocal = aresta.getFeromonio() * obterDistanciaInversa(aresta);
        feromonioLocal = reduzirValor(feromonioLocal);

        return feromonioLocal;
    }

    /**
     * Regra (1) / Regra (3) - (termo à direita)
     * */
    private static double obterDistanciaInversa(Aresta aresta){

        Double distanciaInversa = Math.pow((1 / aresta.getDistancia()), BETA);
        distanciaInversa = reduzirValor(distanciaInversa);

        return distanciaInversa;
    }

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

    public static void main(String args[]){

        //Fase (1)
        inicializarCidades();
        inicializarFeromonios();
        inicializarFormigas();

        if (NR_FORMIGAS > ACS.cidades.size()){

            System.out.print("Numero de Formigas deve ser menor que o numero de cidades");
            return;
        }

        //Fase (2)
        Set<Formiga> formigas = moverFormigas();

        //Fase (3)
        atualizarFeromonioGlobal(formigas);

        GerarResultado.exportarResultado();
    }
}