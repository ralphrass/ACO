package servico;

import entidade.*;
import servico.ACS;
import util.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Created by Ralph on 05/10/2015.
 *
 * Lê um arquivo texto e organiza os linhas em um vetor
 */
public class Inicializar {

    final static String PIPE = "|";
    final static String SEPARADOR = "\\"+PIPE;

    public static List<Cidade> cidades = new ArrayList<>();

    //private static final String ARQUIVO = "C:\\Users\\Ralph\\workspace-gpin-intellij\\ACO\\src\\recursos\\cidades_12.txt";
    private static final String ARQUIVO = "C:\\Users\\Ralph\\workspace-gpin-intellij\\ACO\\src\\recursos\\cenario_50.txt";

    public static void lerArquivo(String caminho){

        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {

            String line;
            String[] item;
            boolean lerArestas = false;
            int id = 0, idAresta = 0;

            while ((line = br.readLine()) != null) {

                if (!lerArestas)
                    if (!line.contains(PIPE))
                        lerArestas = true;

                if (line.length() > 1) {

                    item = line.split(SEPARADOR);

                    if (!lerArestas){

                        adicionarCidade(item, id);
                        id++;

                    } else {

                        if (adicionarAresta(item, idAresta))
                            idAresta++;
                    }
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private static void adicionarCidade(String[] item, int id){

        final int x = Integer.parseInt(item[0]);
        final int y = Integer.parseInt(item[1]);

        final Coordenada coordenada = new Coordenada(x, y);

        final Cidade cidade = new Cidade(id++, coordenada);

        cidades.add(cidade);
    }

    private static boolean adicionarAresta(String[] item, int idAresta){

        final Cidade cidade1 = cidades.get(Integer.parseInt(item[0]));
        final Cidade cidade2 = cidades.get(Integer.parseInt(item[1]));

        if (cidade1.equals(cidade2)){

            System.out.println("Aresta inválida para a cidade: "+cidade1);
            return false;
        }

        //TODO, substituir pela distância euclidiana (evitar loop)
        final BigDecimal distancia = calcularDistancia(cidade1, cidade2);

        Aresta aresta = new Aresta(idAresta, cidade1, cidade2, distancia, new BigDecimal(0));

        ACS.arestas.add(aresta);

        return true;
    }

    private static BigDecimal calcularDistancia(Cidade cidade1, Cidade cidade2){

        final int DOIS = 2;

        final int deltaX = cidade1.getCoordenada().getX() - cidade2.getCoordenada().getX();
        final int deltaY = cidade1.getCoordenada().getY() - cidade2.getCoordenada().getY();

        final BigDecimal distancia = new BigDecimal(Math.sqrt(Math.pow(deltaX, DOIS) + Math.pow(deltaY, DOIS))).setScale(8, RoundingMode.HALF_UP);

        return distancia;
    }

    /**
     * Fase (1) - Parte I/III
     * Importa o TXT e popula os vetores de cidades e arestas
     * */
    private static void inicializarCidades(){

        Inicializar.lerArquivo(ARQUIVO);

        ACS.cidades = Inicializar.cidades;
    }

    /**
     * Fase (1) - Parte II/III
     * Executa o algoritmo do vizinho mais próximo (Nearest Neighbour)
     * */
    private static void inicializarFeromonios(){

        ACS.L_BEST = NearestNeighbour.obterDistanciaDoMenorTour();

        final BigDecimal TAMANHO = new BigDecimal(ACS.cidades.size());

        ACS.TAU_ZERO = new BigDecimal(Math.pow(ACS.L_BEST.multiply(TAMANHO).doubleValue(), -1)).setScale(8, RoundingMode.HALF_UP);

        //Atualiza o feromônio de todas as arestas
        for (Aresta aresta : ACS.arestas) {

            aresta.setFeromonio(ACS.TAU_ZERO);
        }
    }

    /**
     * Fase (1) - Parte III/III - Posiciona formigas em cidades de forma randômica
     * */
    public static void inicializarFormigas(){

        for (int i=0; i<ACS.NR_FORMIGAS; i++){

            Cidade cidade = ACS.cidades.get(ACS.ID_CIDADE_ORIGEM);

            Formiga formiga = new Formiga(i, cidade);
            formiga.getTour().add(new Visita(cidade, null));

            ACS.formigas.add(formiga);
        }
    }

    public static void inicializarGrafo(){

        Inicializar.inicializarCidades();
        Inicializar.inicializarFeromonios();
        Inicializar.inicializarFormigas();
    }
}
