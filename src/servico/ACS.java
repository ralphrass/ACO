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

    public static final int NR_ITERACOES = 10;
    public static final int NR_FORMIGAS = 10;

    /**
     * Taxa de Evaporacao Local: Deve ser 0 < RHO < 1
     * */
    public static final BigDecimal RHO = new BigDecimal(0.1).setScale(2, RoundingMode.HALF_UP);

    /**
     * Taxa de Evaporacao Global: Deve ser 0 < ALFA < 1
     * */
    public static final BigDecimal ALFA = new BigDecimal(0.1).setScale(2, RoundingMode.HALF_UP);

    /**
     * Importancia do feromonio versus a distancia: Deve ser 0 < BETA
     * */
    public static final double BETA = 2d;

    /**
     * Parametro para decidir o metodo de transicao (caminho mais curto ou probabilidade ponderada).
     * Quanto maior o valor de q0, maior a chance de favorecer o caminho mais curto: Deve ser 0 <= q0 <= 1
     * */
    private static final double q0 = 0.9;

    /**
     * TAU = Feromonio. TAU_ZERO = Feromonio inicial ((n*Lnn)^-1)
     * */
    public static BigDecimal TAU_ZERO;

    /**
     * Menor distancia entre origem e destino. O valor esta estatico pq o algoritmo do caminho mais curto pode entrar em loop.
     * */
    public static BigDecimal L_BEST = new BigDecimal(80000);

    public static List<Formiga> formigas = new ArrayList<>();
    public static List<Cidade> cidades;
    public static List<Aresta> arestas = new ArrayList<>();
    public static List<List<Visita>> melhoresTours = new ArrayList<>();

    /**
     * Parametros especificos
     * */
    public static final int ID_CIDADE_ORIGEM = 0;
    public static final int ID_CIDADE_DESTINO = 1;
    public static final int CASAS_DECIMAIS = 12;
    public final static boolean DESCONSIDERAR_ARESTAS_VISITADAS = false;
    public final static boolean DESCONSIDERAR_CIDADES_VISITADAS = false;
    private static final long TEMPO_LIMITE = 4000; //milisegundos por formiga

    /**
     * Fase {3}
     * Move todas as formigas da cidade inicial ate o destino, armazenando os caminhos tomados
     * */
    private static void moverFormigas(){

        for (int n=0; n<ACS.NR_ITERACOES; n++) {

            //Zera os tours e os niveis de feromonio depositado, coloca as formigas na origem
            ACS.formigas = new ArrayList<>();
            Inicializar.inicializarFormigas();

            Set<Formiga> formigasComTourCompleto = new HashSet<>();

            System.out.println("\nIteracao: " + n + " \n");

            for (Formiga formiga : ACS.formigas) {

                final long inicio = System.currentTimeMillis();

                System.out.println("---- Atual: "+formiga);

                while (true) { //Ate que a formiga chegue ao destino final ou entre em loop

                    Aresta proximaAresta = executarRegraDeTransicao(formiga);

                    moverFormigaUmPasso(formiga, proximaAresta);

                    if (formiga.getCidadePosicionada().getId() == ID_CIDADE_DESTINO) {

                        formigasComTourCompleto.add(formiga);
                        break;
                    }

                    if ( (System.currentTimeMillis() - inicio) > ACS.TEMPO_LIMITE) {

                        System.out.println("-------- Tempo limite atingido ");
                        break;
                    }

                } // while do Tour

                if (formiga.getCidadePosicionada().getId() == ID_CIDADE_DESTINO) {

                    atualizarFeromonioLocal(formiga);
                }

            } // for das Formigas

            //retornarParaOrigem();

            System.out.println("INFO: " + formigasComTourCompleto.size() + " formigas com tour completo.");

            //Fase {3}
            atualizarFeromonioGlobal(formigasComTourCompleto);

        } // for das iteracoes
    }

    /**
     * Eq. {3} Decide se vai andar pela "melhor" aresta ou se vai sortear uma aresta
     * */
    private static Aresta executarRegraDeTransicao(Formiga formiga){

        Aresta proximaAresta = null;

        List<Aresta> arestasInexploradas = Utils.obterArestasInexploradas(formiga);

        if (arestasInexploradas == null || arestasInexploradas.size() == 0){ //Formiga em loop

            return null;
        }

        if (arestasInexploradas.size() == 1){ //So tem uma aresta para explorar, nao tem sentido aplicar metodos de decisao

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

    private static void moverFormigaUmPasso(Formiga formiga, Aresta proximaAresta){

        Cidade destino = Utils.obterCidadeDestino(formiga.getCidadePosicionada(), proximaAresta);

        formiga.setCidadePosicionada(destino);
        formiga.getTour().add(new Visita(destino, proximaAresta));

        if (formiga.getTour().size() == 2) { //eh a primeira visita da formiga, saindo da sua cidade inicial

            formiga.getTour().get(0).setAresta(proximaAresta); //Mesma aresta da proxima cidade
        }

        //Acumula deposito de feromonio (regra {02})
        final BigDecimal DELTA_TAU = new BigDecimal(1).divide(proximaAresta.getDistancia(), CASAS_DECIMAIS, RoundingMode.HALF_UP);
        formiga.getDeltaTau().put(proximaAresta, DELTA_TAU);
    }

    /**
     * Reforca o feromonio das arestas que a formiga passou
     * */
    private static void atualizarFeromonioLocal(Formiga formiga){

        for (Visita visita : formiga.getTour()){

            BigDecimal feromonio = computarFeromonioLocal(visita.getAresta(), formiga);

            ACS.arestas.get(visita.getAresta().getId()).setFeromonio(feromonio);
        }
    }

    /**
     * Eq. {5}  T(rk,sk) := (1-p) * T(rk ,sk) + p * DELTA_T(r,s)
     * */
    private static BigDecimal computarFeromonioLocal(Aresta aresta, Formiga formiga){

        final BigDecimal PRIMEIRO_TERMO = new BigDecimal(1).subtract(RHO);

        final BigDecimal FEROMONIO_DEPOSITADO = formiga.getDeltaTau().get(aresta);
        final BigDecimal FEROMONIO_TOTAL = aresta.getFeromonio().add(FEROMONIO_DEPOSITADO);

        final BigDecimal SEGUNDO_TERMO = FEROMONIO_TOTAL.add(RHO).multiply(TAU_ZERO);

        //final BigDecimal feromonio = PRIMEIRO_TERMO.multiply(FEROMONIO_TOTAL).add((RHO).multiply(TAU_ZERO));

        final BigDecimal feromonio = PRIMEIRO_TERMO.multiply(SEGUNDO_TERMO).setScale(ACS.CASAS_DECIMAIS, RoundingMode.HALF_UP);

        return feromonio;
    }

    /**
     * Faz as formigas (que chegaram ao destino) voltarem para reforcar o feromonio do caminho escolhido
     * */
    private static void retornarParaOrigem(){

        for (Formiga formiga : ACS.formigas) {

            if (formiga.getCidadePosicionada().getId() == ACS.ID_CIDADE_DESTINO) {

                for (int j = (formiga.getTour().size() - 1); j > 0; j--) { //Percorre o Tour inverso

                    //Obtem a cidade anterior a cidade atual no Tour
                    final Cidade cidadeRetorno = formiga.getTour().get(j - 1).getCidade();

                    //Acumula o DELTA_TAU de um passo em direcao a origem
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

    /**
     * Fase {3} - Evaporacao de feromonios e reinicilizacao de Tours
     * */
    private static void atualizarFeromonioGlobal(Set<Formiga> formigasComTourCompleto){

        //BigDecimal Lbest = L_BEST.multiply(new BigDecimal(2)); //"piora" o melhor Tour
        BigDecimal Lbest = L_BEST;
        List<Visita> melhorTour = new ArrayList<>();

        //Encontra a formiga com o melhor Tour
        for (Formiga formiga : formigasComTourCompleto) {

            BigDecimal Lk = obterDistanciaDoTour(formiga);

            if (Lk.compareTo(Lbest) == -1){ //Lk < Lbest

                Lbest = Lk;
                melhorTour = formiga.getTour();
            }
        }

        //L_BEST = Lbest;

        melhoresTours.add(melhorTour);

        for (Aresta aresta : ACS.arestas){

            BigDecimal feromonio = computarFeromonioGlobal(aresta, Lbest, melhorTour);

            aresta.setFeromonio(feromonio);
        }
    }

    /**
     * Eq. {4} T(r,s) <- (1 - ALFA) * T(r,s) + ALFA * DELTA_T(r,s)
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

        //BigDecimal feromonio = new BigDecimal(1).subtract(ALFA).multiply(aresta.getFeromonio()).add(ALFA).multiply(DELTA_TAU);

        BigDecimal feromonio = new BigDecimal(1).subtract(ALFA);
        feromonio.multiply(aresta.getFeromonio());

        final BigDecimal SEGUNDO_TERMO = ALFA.multiply(DELTA_TAU);

        feromonio = feromonio.add(SEGUNDO_TERMO);

        return feromonio.setScale(ACS.CASAS_DECIMAIS, RoundingMode.HALF_UP);
    }

    /**
     * Fase {3} - "Compute Lk" Calcula a distancia percorrida pela formiga
     * */
    private static BigDecimal obterDistanciaDoTour(Formiga formiga){

        BigDecimal distancia = new BigDecimal(0);

        for (Visita visita : formiga.getTour())
            if (visita.getAresta() != null) //Ignora a cidade inicial
                distancia = distancia.add(visita.getAresta().getDistancia());

        return distancia;
    }

    public static void main(String args[]){

        final long inicio = System.currentTimeMillis();

        //Fase {1} - Monta o grafo e inicializa os ferom�nios respeitando a regra do caminho mais curto
        Inicializar.inicializarGrafo();

        if (NR_FORMIGAS > ACS.cidades.size()){

            System.out.print("Numero de Formigas deve ser menor que o numero de cidades");
            return;
        }

        //Fase {2}
        moverFormigas();

        GerarResultado.exportarResultado();

        final long fim = System.currentTimeMillis();
        final long tempoDeExecucao = fim - inicio;

        System.out.println("Tempo de Execucao Total: "+(tempoDeExecucao / 1000)+" segundos");
    }
}