package util;

import entidade.Visita;
import servico.ACS;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.util.List;

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

        parametros.append(ACS.ALFA.setScale(8, RoundingMode.HALF_UP));
        parametros.append(SEPARADOR);
        parametros.append(ACS.BETA);
        parametros.append(SEPARADOR);
        parametros.append(ACS.RHO.setScale(8, RoundingMode.HALF_UP));
        parametros.append(SEPARADOR);
        parametros.append(ACS.NR_FORMIGAS);
        parametros.append(SEPARADOR);
        parametros.append(ACS.NR_ITERACOES);
        
        writer.println(parametros);

        for (List<Visita> visitas : ACS.melhoresTours){

            StringBuilder tour = new StringBuilder();

            for (int i=0; i<visitas.size(); i++) {

                tour.append(visitas.get(i).getCidade().getId());

                if (i != (visitas.size()-1)) {

                    tour.append(SEPARADOR);
                }
            }

            writer.println(tour);
        }

        writer.close();
    }
}
