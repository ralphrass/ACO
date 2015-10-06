package servico;

/**
 * Created by Ralph on 05/10/2015.
 */
abstract class ACOBase {

    /**
     * Aloca randomicamente cada formiga em uma cidade
     * */
    abstract void inicializarFormigas();

    /**
     * Calcula a probabilidade de uma formiga escolher cada caminho possível
     * */
    abstract double calcularEstadoDeTransicao();


    abstract void moverFormiga();

    /*

    void atualizarFeromonioLocal();

    void atualizarFeromonioGlobal();


*/
}
