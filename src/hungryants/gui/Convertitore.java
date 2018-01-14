/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hungryants.gui;

/**
 * Converte fra unita' di misura in pixel e caselle
 * @author nicola
 */
public class Convertitore {

    /**
     * Dimensione in pixel delle caselle
     */
    private int dimCaselle;

    /**
     * Crea un nuovo convertitore
     * @param dimCaselle dimensione in pixel delle caselle
     */
    public Convertitore(int dimCaselle) {
        this.dimCaselle= dimCaselle;
    }

    /**
     * Restituisce la dimensione in pixel delle caselle
     * @return dimensione delle caselle
     */
    public int getDim() {
        return dimCaselle;
    }

    /**
     * Converte le coordinate da caselle in pixel (entrambi partono da 0)
     * @param coord coordinata in numero di caselle
     * @return coordinata in numero di pixel
     */
    public int c2p(int coord) {
        return coord*dimCaselle;
    }

    /**
     * Converte le coordinate da pixel in caselle (entrambi partono da 0)
     * @param coord coordinata in numero di pixel
     * @return coordinata in numero di caselle
     */
    public int p2c(int coord) {
        return (int) Math.floor( (double) coord/dimCaselle ) ;
    }

}
