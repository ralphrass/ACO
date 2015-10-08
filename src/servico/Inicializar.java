package servico;

import entidade.*;
import servico.ACS;
import util.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
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

                        adicionarAresta(item, idAresta);
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

    private static void adicionarAresta(String[] item, int idAresta){

        final Cidade cidadeOrigem = cidades.get(Integer.parseInt(item[0]));
        final Cidade cidadeDestino = cidades.get(Integer.parseInt(item[1]));

        //TODO, substituir pela distância euclidiana (evitar loop)
        final double distancia = calcularDistancia(cidadeOrigem, cidadeDestino);

        final Aresta aresta = new Aresta(idAresta, cidadeDestino, distancia, 0);
        final Aresta aresta2 = new Aresta(idAresta, cidadeOrigem, distancia, 0);

        cidadeOrigem.getArestas().add(aresta);
        cidadeDestino.getArestas().add(aresta2);
    }

    private static double calcularDistancia(Cidade cidade1, Cidade cidade2){

        final int DOIS = 2;

        final int deltaX = cidade1.getCoordenada().getX() - cidade2.getCoordenada().getX();
        final int deltaY = cidade1.getCoordenada().getY() - cidade2.getCoordenada().getY();

        final double distancia = Math.sqrt(Math.pow(deltaX, DOIS) + Math.pow(deltaY, DOIS));

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

        final int TAMANHO = ACS.cidades.size();
        ACS.TAU_ZERO = Math.pow(TAMANHO * ACS.L_BEST, -1);

        //Atualiza o feromônio de todas as arestas
        for (Cidade cidade : ACS.cidades) {

            for (Aresta aresta : cidade.getArestas()){

                if (aresta.getFeromonio() == 0){

                    aresta.setFeromonio(ACS.TAU_ZERO);
                }
            }
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
