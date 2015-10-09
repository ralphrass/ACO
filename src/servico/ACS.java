package servico;

import entidade.Aresta;
import entidade.Cidade;
import entidade.Formiga;
import entidade.Visita;
import util.GerarResultado;
import util.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Created by Ralph on 05/10/2015.
 */
public class ACS {

    public static final int NR_ITERACOES = 1;

    public static final int NR_FORMIGAS = 1; //padrão = 10. Não pode exceder o número de cidades

    public static final BigDecimal RHO = new BigDecimal(0.1);  //Deve ser > 0 e < 1    Padrao = 0.1 (?) Taxa de Evaporação
    public static final BigDecimal ALFA = RHO; //Deve ser > 0 e < 1    Padrao = RHO (?)
    public static final double BETA = 2d;  //Deve ser > 0          Padrao = 2 (?)
    private static final double q0 = 0.9;   //Deve ser 0 <= q0 <= 1 Padrao = 0.9

    public static BigDecimal TAU_ZERO; //Calculado pelo Algoritmo do Vizinho mais Próximo
    public static BigDecimal L_BEST; //Menor distância entre origem e destino

    public static final int ID_CIDADE_ORIGEM = 0;
    public static final int ID_CIDADE_DESTINO = 1;

    public static List<Formiga> formigas = new ArrayList<>();
    public static List<Cidade> cidades;
    public static List<Aresta> arestas = new ArrayList<>();

    public static List<List<Visita>> melhoresTours = new ArrayList<>();

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

                        Aresta proximaAresta = executarRegraDeTransicao(formiga);

                        if (proximaAresta == null) { //Formiga em loop

                            System.out.print("Formiga em loop. Na cidade "+formiga.getCidadePosicionada()+" não tem mais arestas para escolher");
                            nrFormigasComTourCompleto++;
                            continue;
                        }

                        Cidade destino = Utils.obterCidadeDestino(formiga.getCidadePosicionada(), proximaAresta);

                        formiga.setCidadePosicionada(destino);
                        formiga.getTour().add(new Visita(destino, proximaAresta));

                        if (formiga.getTour().size() == 2) { //É a primeira visita da formiga, saindo da sua cidade inicial

                            formiga.getTour().get(0).setAresta(proximaAresta); //Mesma aresta da próxima cidade
                        }

                        //Acumula depósito de feromônio (regra {02})
                        final BigDecimal DELTA_TAU = new BigDecimal(1).divide(proximaAresta.getDistancia(), 12, RoundingMode.HALF_UP);
                        formiga.getDeltaTau().put(proximaAresta, DELTA_TAU);
                    }

                } else { //Volta

                    for (Formiga formiga : ACS.formigas) {

                        for (int j=(formiga.getTour().size()-1); j>0; j--){ //Percorre o Tour inverso

                            //Obtém a cidade anterior à cidade atual no Tour
                            final Cidade cidadeRetorno = formiga.getTour().get(j-1).getCidade();

                            //Acumula o DELTA_TAU de um passo em direção à origem
                            final BigDecimal DELTA_TAU = new BigDecimal(1).divide(formiga.getTour().get(j).getAresta().getDistancia(), 12, RoundingMode.HALF_UP);
                            final BigDecimal DELTA_TAU_PRESENTE = formiga.getDeltaTau().get(formiga.getTour().get(j).getAresta());

                            final BigDecimal DELTA_TAU_TOTAL = DELTA_TAU.add(DELTA_TAU_PRESENTE);

                            formiga.getDeltaTau().put(formiga.getTour().get(j).getAresta(), DELTA_TAU_TOTAL);

                            //Move a formiga para a cidade anterior
                            formiga.setCidadePosicionada(cidadeRetorno);
                        }
                    }
                }
            }

            for (Formiga formiga : ACS.formigas) {

                for (Visita visita : formiga.getTour()){

                    BigDecimal feromonio = computarFeromonioLocal(visita.getAresta(), formiga);
                    atualizarFeromonioLocal(feromonio, visita.getAresta());
                }
            }

        } while (nrFormigasComTourCompleto < ACS.formigas.size());

        return formigasComTourCompleto;
    }

    /**
     * Eq. {5}  ?(rk ,sk) := (1-?) ?(rk ,sk) + ? ?0
     * */
    private static BigDecimal computarFeromonioLocal(Aresta aresta, Formiga formiga){

        final BigDecimal UM = new BigDecimal(1);

        final BigDecimal FEROMONIO_DEPOSITADO = formiga.getDeltaTau().get(aresta);
        final BigDecimal FEROMONIO_TOTAL = aresta.getFeromonio().add(FEROMONIO_DEPOSITADO);

        final BigDecimal feromonio = (UM.subtract(RHO)).multiply(FEROMONIO_TOTAL).add((RHO).multiply(TAU_ZERO));

        return feromonio;
    }

    /**
     * Fase {3} - Evaporação de feromônios e reinicilização de Tours
     * */
    private static void atualizarFeromonioGlobal(Set<Formiga> formigasComTourCompleto){

        BigDecimal Lbest = new BigDecimal(Math.pow(L_BEST.doubleValue(), 2)); //"piora" o melhor Tour
        List<Visita> melhorTour = new ArrayList<>();

        //Encontra a formiga com o melhor Tour
        for (Formiga formiga : formigasComTourCompleto) {

            BigDecimal Lk = obterDistanciaDoTour(formiga);

            if (Lk.compareTo(Lbest) == -1){ //Lk < Lbest

                Lbest = Lk;
                melhorTour = formiga.getTour();
            }
        }

        melhoresTours.add(melhorTour);

        //Percorre grafo inteiro e atualiza os feromônios das arestas
        for (Aresta aresta : ACS.arestas){

            BigDecimal feromonio = computarFeromonioGlobal(aresta, Lbest, melhorTour);

            aresta.setFeromonio(feromonio);
        }
    }

    /**
     * Eq. {4}
     * */
    private static BigDecimal computarFeromonioGlobal(Aresta aresta, BigDecimal Lbest, List<Visita> melhorTour){

        BigDecimal DELTA_TAU;
        boolean isArestaNoMelhorTour = false;

        for (Visita visita : melhorTour) {

            if (visita.getAresta() != null &&
                    visita.getAresta().equals(aresta)) {

                isArestaNoMelhorTour = true;
                break;
            }
        }

        if (isArestaNoMelhorTour){

            DELTA_TAU = new BigDecimal(Math.pow(Lbest.doubleValue(), -1));

        } else {

            DELTA_TAU = new BigDecimal(0);
        }

        BigDecimal feromonio = new BigDecimal(1).subtract(ALFA).multiply(aresta.getFeromonio()).add(ALFA).multiply(DELTA_TAU);

        return feromonio;
    }

    /**
     * Fase {3} - "Compute Lk"
     * */
    private static BigDecimal obterDistanciaDoTour(Formiga formiga){

        BigDecimal distancia = new BigDecimal(0);

        for (Visita visita : formiga.getTour())
            if (visita.getAresta() != null) //Ignora a cidade inicial
                distancia = distancia.add(visita.getAresta().getDistancia());

        return distancia;
    }

    private static void atualizarFeromonioLocal(BigDecimal feromonio, Aresta proximaAresta){

        for (Aresta aresta : ACS.arestas){

            if (aresta.equals(proximaAresta)){
                aresta.setFeromonio(feromonio);
            }
        }
    }

    /**
     * Eq. {3}
     * */
    private static Aresta executarRegraDeTransicao(Formiga formiga){

        Aresta proximaAresta = null;

        List<Aresta> arestasInexploradas = Utils.obterArestasInexploradas(formiga);

        if (arestasInexploradas == null || arestasInexploradas.size() == 0){ //Formiga em loop (corrigir escolha de arestas desconsiderando a cidade inicial)

            return null;
        }

        if (arestasInexploradas.size() == 1){ //Só tem uma aresta para explorar, não tem sentido aplicar métodos de decisão

            return  arestasInexploradas.get(0);
        }

        double q = Math.random();

        if (q <= q0){

            proximaAresta = CalcularTransicao.escolherAMelhorAresta(arestasInexploradas);

        } else {

            proximaAresta = CalcularTransicao.escolherPorProbabilidadePseudoRandomica(arestasInexploradas);
        }

        return proximaAresta;
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