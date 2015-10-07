package util;

import servico.ACS;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * Created by Ralph on 07/10/2015.
 */
public class GerarResultado {

    private static final char SEPARADOR = '|';

    public static void exportarResultado(){

        PrintWriter writer = null;

        try {

            writer = new PrintWriter("resultado.txt", "UTF-8");

        } catch (FileNotFoundException e) {

            e.printStackTrace();

        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        }

        StringBuilder parametros = new StringBuilder();
        parametros.append(ACS.ALFA);
        parametros.append(SEPARADOR);
        parametros.append(ACS.BETA);
        parametros.append(SEPARADOR);
        parametros.append(ACS.RHO);
        parametros.append(SEPARADOR);
        parametros.append(ACS.NR_FORMIGAS);
        parametros.append(SEPARADOR);
        parametros.append(ACS.NR_ITERACOES);
        
        writer.println(parametros.toString());

        writer.close();
    }
}
