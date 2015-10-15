package servico;

import entidade.*;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Created by Ralph on 05/10/2015.
 *
 * L� um arquivo texto e organiza os linhas em um vetor
 */
public class Inicializar {

    final static String PIPE = "|";
    final static String SEPARADOR = "\\"+PIPE;

    public static List<Cidade> cidades = new ArrayList<>();

    /*
    //private static final String ARQUIVO = "C:\\Users\\Ralph\\workspace-gpin-intellij\\ACO\\src\\recursos\\cidades_12.txt";
    private static final String ARQUIVO = "C:\\Users\\Ralph\\workspace-gpin-intellij\\ACO\\src\\recursos\\cenario_10.txt";
    //private static final String ARQUIVO = "C:\\Users\\Ralph\\workspace-gpin-intellij\\ACO\\src\\recursos\\cenario_50.txt";
    //private static final String ARQUIVO = "C:\\Users\\Ralph\\workspace-gpin-intellij\\ACO\\src\\recursos\\cenario_70.txt";
    //private static final String ARQUIVO = "C:\\Users\\Ralph\\workspace-gpin-intellij\\ACO\\src\\recursos\\cenario_100.txt";
    */

    //private static final String ARQUIVO = "\\src\\recursos\\cidades_12.txt";
    //private static final String ARQUIVO = "\\src\\recursos\\cenario_10.txt";
    //private static final String ARQUIVO = "\\src\\recursos\\cenario_50.txt";
    //private static final String ARQUIVO = "\\src\\recursos\\cenario_70.txt";
    private static final String ARQUIVO = "\\src\\recursos\\cenario_100.txt";

    public static void lerArquivo(String caminho){

        //try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
        try (BufferedReader br = new BufferedReader(new FileReader(new File("").getAbsolutePath()+ARQUIVO))){

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

            System.out.println("Aresta invalida para a cidade: "+cidade1);
            return false;
        }

        final BigDecimal distancia = calcularDistancia(cidade1, cidade2);

        Aresta aresta = new Aresta(idAresta, cidade1, cidade2, distancia, new BigDecimal(0));

        ACS.arestas.add(aresta);

        return true;
    }

    /**
     * TODO distancia dividido por 10
     * */
    private static BigDecimal calcularDistancia(Cidade cidade1, Cidade cidade2){

        final int DOIS = 2;

        final int deltaX = cidade1.getCoordenada().getX() - cidade2.getCoordenada().getX();
        final int deltaY = cidade1.getCoordenada().getY() - cidade2.getCoordenada().getY();

        final BigDecimal distancia = new BigDecimal(Math.sqrt(Math.pow(deltaX, DOIS) + Math.pow(deltaY, DOIS)) / 10).setScale(8, RoundingMode.HALF_UP);

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
     * Ignora o algoritmo do vizinho mais proximo (Nearest Neighbour) porque ele pode entrar em loop
     * */
    private static void inicializarFeromonios(){

        ACS.L_BEST = NearestNeighbour.obterDistanciaMedia();

        final BigDecimal TAMANHO = new BigDecimal(ACS.cidades.size());

        ACS.TAU_ZERO = new BigDecimal(Math.pow(ACS.L_BEST.multiply(TAMANHO).doubleValue(), -1)).setScale(ACS.CASAS_DECIMAIS, RoundingMode.HALF_UP);

        int fator = 1;

        //TODO - Multiplica o TAU_ZERO
        if (ACS.cidades.size() < 50) {

            fator = 3;

        } else if (ACS.cidades.size() < 200) {

            fator = 4;

        } else {

            fator = 9;
        }

        ACS.TAU_ZERO = ACS.TAU_ZERO.multiply( (new BigDecimal(10)).pow(fator) );

        for (Aresta aresta : ACS.arestas) {

            aresta.setFeromonio(ACS.TAU_ZERO);
        }
    }

    /**
     * Fase (1) - Parte III/III - Posiciona formigas em cidades de forma randomica
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
