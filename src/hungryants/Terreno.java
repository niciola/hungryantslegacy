/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hungryants;

import java.awt.Point;
import java.io.Serializable;

/**
 * Questa classe simula il terreno attraverso una griglia. L'angolo superiore
 * sinistro ha coordinate (0,0). Ogni casella della griglia puo' essere:
 * <ul>
 *      <li>il nido (e' ammesso uno e un solo nido)
 *      <li>cibo
 *      <li>ostacolo
 *      <li>terreno libero
 * </ul>
 * <p>
 * Sul terreno attraversabile dalle formiche (libero oppure cibo) puo' essere
 * rilasciato del feromone.
 *
 * @author nicola
 */
public class Terreno implements Serializable, Cloneable {

    /** Per la serializzazione */
    private static final long serialVersionUID = 123L;

    /** Tipo delle caselle della griglia */
    public enum TipoCasella implements Serializable { NIDO, CIBO, OSTACOLO, LIBERO };

    /**
     * La griglia rappresenta il terreno.
     * <p>
     * Dimensioni: griglia[x][y]
     */
    private Casella griglia[][];
    /** Variabile di comodo per sapere subito dove si trova il nido */
    private Point nido;
    /** Tasso di evaporazione del feromone */
    private double evapRate;

    /**
     * Crea un nuovo terreno con posizione (0,0) del nido e tasso di evaporazione
     * predefinito
     * @param width numero di caselle di larghezza
     * @param height numero di caselle di altezza
     * @param nido posizione del nido
     */
    public Terreno(int width, int height, Point posNido) {
        this(width, height, posNido, 0.001);
    }

    /**
     * Crea un nuovo terreno.
     * @param width numero di caselle di larghezza
     * @param height numero di caselle di altezza
     * @param nido posizione del nido
     * @param evapRate Tasso di evaporazione del feromone
     */
    public Terreno(int width, int height, Point posNido, double evapRate) {
        if (width<=0 || height<=0) {
            throw new RuntimeException("Le dimensioni del terreno devono essere maggiori di 0");
        }

        this.evapRate= evapRate;
        
        // inizializzo la griglia
        griglia= new Casella[width][height];
        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
                griglia[i][j]= new Casella();
            }
        }

        // inizializzo il nido
        nido= new Point(0, 0);
        moveNido(posNido);
    }

    /**
     * Sposta il nido in una nuova posizione
     * @param nuovaPos nuova posizione del nido
     */
    public void moveNido(Point nuovaPos) {
        if (!coordValida(nuovaPos)) {
            throw new RuntimeException("Coordinata non valida");
        }
        Casella c;

        c= casella(nido);
        c.setTipo(TipoCasella.LIBERO);

        nido.setLocation(nuovaPos);
        c= casella(nuovaPos);
        c.setTipo(TipoCasella.NIDO);
    }

    /**
     * Azzera il feromone su tutto il terreno
     */
    public void azzeraFeromone() {
        int w, h;
        w= getWidth();
        h= getHeight();

        for (int x=0; x<w; x++) {
            for (int y=0; y<h; y++) {
                griglia[x][y].setFeromone(0);
            }
        }
    }


    /**
     * Restituisce il tipo di una casella
     * @param pos posizione della casella in esame
     * @return tipo della casella
     */
    public TipoCasella getTipoCasella(Point pos) {
        Casella c= casella(pos);
        return c.getTipo();
    }

    /**
     * Cambio il tipo di una casella. Nota che non e' possibile cambiare il tipo
     * della casella nido: utilizzare moveNido() per questo scopo.
     * @param pos coordinate della casella da cambiare
     * @param nuovoTipo nuovo tipo della casella
     */
    public void setTipoCasella(Point pos, TipoCasella nuovoTipo) {
        Casella c= casella(pos);
        if (c.getTipo() == TipoCasella.NIDO) {
            throw new RuntimeException("Non e' permesso cambiare il tipo della casella nido");
        }
        c.setTipo(nuovoTipo);
    }

    /**
     * Imposta il tasso di evaporazione del feromone
     * @param evapRate tasso di evaporazione
     */
    public void setEvapRate(double evapRate) {
        if  ( (evapRate>=0) && (evapRate<1)) {
            this.evapRate= evapRate;
        } else {
            throw new RuntimeException("Il tasso di evaporazione deve essere 0<=rate<1");
        }
    }

    /**
     * Restituisce il tasso di evaporazione del feromone
     * @return tasso di evaporazione
     */
    public double getEvapRate() {
        return evapRate;
    }

    /**
     * Restituisce il livello di feromone di una data casella
     * @param pos posizione in esame
     * @return il livello di feromone
     */
    public double getFeromone(Point pos) {
        Casella c= casella(pos);
        return c.getFeromone();
    }

    /**
     * Imposta il livello di feromone in una data casella
     * @param pos coordinate della casella
     * @param newValue nuovo livello di feromone
     */
    public void setFeromone(Point pos, double newValue) {
        Casella c= casella(pos);
        c.setFeromone(newValue);
    }

    /**
     * Incrementa il livello di feromone di un valore dato
     * @param pos coordinate della casella
     * @param incremento valore di incremento
     */
    public void incFeromone(Point pos, double incremento) {
        Casella c= casella(pos);
        double maxLiv= 100;
        if (c.attraversabile()) {
            double f= c.getFeromone() + incremento;
            if (f > maxLiv) {
                //f= maxLiv;
            }
            c.setFeromone(f);
        }
        
    }

    /**
     * Decrementa il livello di feromone di un valore dato, fino a un minimo di 0
     * @param pos coordinate della casella
     * @param decremento valore di decremento
     */
    public void decFeromone(Point pos, double decremento) {
        Casella c= casella(pos);
        double f= c.getFeromone();
        f= f - decremento;
        if (f<0) f=0;
        c.setFeromone(f);
    }


    /**
     * Fa evapora una certa quantita' di feromone da tutto il terreno
     */
    public void evaporaFeromone() {
        int w, h;
        w= getWidth();
        h= getHeight();
        for (int i=0; i<w; i++) {
            for (int j=0; j<h; j++) {
                griglia[i][j].evaporaFeromone();
            }
        }
    }

    /**
     * Restituisce il riferimento alla casella presente in una data posizione.
     * @param pos posizione della casella
     * @return la casella
     */
    private Casella casella(Point pos) {
        if (!coordValida(pos)) {
            throw new RuntimeException("Coordinata non valida");
        }
        return griglia[pos.x][pos.y];
    }

    /**
     * Restituisce true se la casella e' libera oppure e' del cibo (la casella
     * in questi due casi e' detta attraversabile)
     * @return true se la casella e' attraversabile dalla formica
     */
    public boolean casellaAttraversabile(Point pos) {
        if (coordValida(pos)) {
            return casella(pos).attraversabile();
        } else {
            return false;
        }
    }

    /**
     * Restituisce la posizione del nido
     * @return posizione del nido
     */
    public Point getPosNido() {
        return new Point(nido);
    }

    /**
     * Restituisce il numero di caselle di larghezza del terrano
     * @return larghezza
     */
    public int getWidth() {
        return griglia.length;
    }

    /**
     * Restituisce il numero di caselle di altezza del terrano
     * @return altezza
     */
    public int getHeight() {
        return griglia[0].length;
    }

    /**
     * Verifica se le coordinate (x,y) date sono valide, cioe' sono nei limiti della
     * griglia. Le coordinate sono valide se:
     * <p>
     * (pos.x >= 0) && (pos.x < terreno.width) && (pos.y >= 0) && (pos.y < terreno.height)
     * @param pos coordinate da validare
     * @return true se le coordinate sono valide, false altrimenti
     */
    public boolean coordValida(Point pos) {
        return ( (pos.x >= 0) && (pos.x < getWidth()) && (pos.y >= 0) && (pos.y < getHeight()) );
    }

    @Override
    public Object clone() {
        
        try {
            Terreno t= (Terreno) super.clone();
            int width, height;
            
            // inizializzo la griglia
            width= this.getWidth();
            height= this.getHeight();
            t.griglia= new Casella[width][height];
            for (int i=0; i<width; i++) {
                for (int j=0; j<height; j++) {
                    t.griglia[i][j]= (Casella) this.griglia[i][j].clone();
                }
            }

            // inizializzo il nido
            t.nido= new Point(0, 0);
            t.moveNido(this.getPosNido());

            // evaporazione feromone
            t.evapRate= this.evapRate;

            return t;
            
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /**
     * Definisce le caratteristiche di una singola casella della griglia:
     * <ul>
     *      <li>livello di feromone (>=0)
     *      <li>tipo della casella (definito dall'enumerazione <code>TipoCasella</code>
     * </ul>
     * E' possibile cambiare il livello di feromone e il tipo della casella.
     * <p>
     * Nota che il livello di feronome e' definito solo per le caselle attraversabili
     * dalle formiche (caselle libere o cibo).
     */
    private class Casella implements Serializable, Cloneable {

        /** Per la serializzazione */
        private static final long serialVersionUID = 321L;

        /** livello di feromone (>=0) */
        private double feromone;
        /** tipo di questa casella */
        private TipoCasella tipo;

        /**
         * Inizializza la casella
         */
        public Casella() {
            initFeromone();
            this.tipo = TipoCasella.LIBERO;
        }

        /**
         * Fa evapora una certa quantita' di feromone
         */
        public void evaporaFeromone() {
            // feromone = feromone - feromone*evapRate
            feromone= feromone * (1 - evapRate);
        }

        /**
         * Restituisce il tipo di questa casella
         * @return tipo della casella
         */
        public TipoCasella getTipo(){
            return tipo;
        }
        
        /**
         * Imposta il tipo della casella
         * @param tipo tipo della casella
         */
        public void setTipo(TipoCasella tipo) {
            this.tipo= tipo;
        }

        /**
         * Inizializza a 0 il livello di feromone
         */
        public void initFeromone() {
            this.feromone= 0;
        }

        /**
         * Restituisce il corrente livello di feromone. Se questa non e' una
         * casella attraversabile allora restituisce 0.
         * @return livello di feromone
         */
        public double getFeromone() {
            if (!attraversabile()) {
                throw new RuntimeException("Livello di feromone non definito per caselle non attraversabili");
                //System.out.println("Livello di feromone non definito per caselle non attraversabili");
            }
            return feromone;
        }

        /**
         * Imposta il livello di feromone (>=0)
         * @param feromone nuovo livello di feromone
         */
        public void setFeromone(double feromone) {
            if (feromone<0) {
                throw new RuntimeException("Livello di feromone>=0");
            }
            this.feromone= feromone;
        }

        /**
         * Restituisce true se la casella e' libera oppure e' del cibo (la casella
         * in questi due casi e' detta attraversabile)
         * @return true se la casella e' attraversabile dalla formica
         */
        public boolean attraversabile() {
            return ( (tipo==Terreno.TipoCasella.CIBO) || (tipo==Terreno.TipoCasella.LIBERO) );
        }

        @Override
        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                // this shouldn't happen, since we are Cloneable
                throw new InternalError();
            }
        }
    }
}
