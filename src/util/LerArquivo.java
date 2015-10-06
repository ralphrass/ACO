package util;

import entidade.Aresta;
import entidade.Cidade;
import entidade.Coordenada;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Created by Ralph on 05/10/2015.
 *
 * Lê um arquivo texto e organiza os linhas em um vetor
 */
public class LerArquivo {

    final static String PIPE = "|";
    final static String SEPARADOR = "\\"+PIPE;

    //TODO Não está sendo utilizado
    public static List<Aresta> arestas = new ArrayList<>();
    public static List<Cidade> cidades = new ArrayList<>();

    public static void lerArquivo(String caminho){

        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {

            String line;
            String[] item;
            boolean lerArestas = false;
            int id = 0;

            while ((line = br.readLine()) != null) {

                if (!lerArestas){

                    if (! line.contains(PIPE)){

                        lerArestas = true;
                    }
                }

                if (line.length() > 1) {

                    item = line.split(SEPARADOR);

                    if (!lerArestas){

                        adicionarCidade(item, id);
                        id++;

                    } else {

                        adicionarAresta(item);
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

    private static void adicionarAresta(String[] item){

        final Cidade cidadeOrigem = cidades.get(Integer.parseInt(item[0]));
        final Cidade cidadeDestino = cidades.get(Integer.parseInt(item[1]));

        final double distancia = calcularDistancia(cidadeOrigem, cidadeDestino);

        //TODO precisa ser em um ciclo para selecionar a menor distância
        //final double feromonioZero = Math.pow((double)cidades.size()*distancia, -1);
        //final Aresta aresta = new Aresta(cidadeOrigem, cidadeDestino, distancia, 0);

        final Aresta aresta = new Aresta(cidadeDestino, distancia, 0);
        final Aresta aresta2 = new Aresta(cidadeOrigem, distancia, 0);

        cidadeOrigem.getArestas().add(aresta);
        cidadeDestino.getArestas().add(aresta2);

        //TODO - Remover
        arestas.add(aresta);
    }

    private static double calcularDistancia(Cidade cidade1, Cidade cidade2){

        final int DOIS = 2;

        final int deltaX = cidade1.getCoordenada().getX() - cidade2.getCoordenada().getX();
        final int deltaY = cidade1.getCoordenada().getY() - cidade2.getCoordenada().getY();

        final double distancia = Math.sqrt(Math.pow(deltaX, DOIS) + Math.pow(deltaY, DOIS));

        return distancia;
    }
}
