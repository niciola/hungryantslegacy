/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hungryants;

import java.awt.Point;
import java.util.Iterator;
import java.util.Random;

/**
 * Questa classe rappresenta la colonia di formiche.
 * <p>
 * Caratteristiche delle formiche:
 * <dl>
 *      <ul>la formica e' sul terreno (in una data posizione) oppure non e' presente
 *      sul terreno (e' nel nido e probabilmente vi uscira' in seguito)
 *      <ul>posizione della formica sul terreno (ammesso che sia sul terreno)
 *      <ul>stato della formica
 *      <ul>vita rimasta
 * </dl>
 * <p>
 * Le formiche cercano il cibo per un determinato numero di passi, al termine dei
 * quali la formica torna al nido per una nuova ricerca. In realta' allo scadere
 * del tempo la formica viene semplicemente rimossa dal terreno e fatta uscire
 * dal nido.
 * <p>
 * Le formiche che tornano con il cibo verso il nido depositano il feromone sul
 * loro percorso.
 * @author nicola
 */
public class Colonia implements Iterable<Colonia.Formica>{
    /** Generatore di numeri casuali*/
    private Random fato;
    /** Numero totale di formiche nella colonia (attive o meno) */
    private int numeroFormiche;
    /** La colonia di formiche */
    private Formica coloniaForm[];
    /** Numero massimo di passi di vita delle formiche */
    private int vitaMassima;
    /** Terreno in cui si trova la formica */
    private Terreno terreno;
    /** Livello di attrazione delle caselle senza feromone */
    private double alpha;
    /** Nonlinearita' delle scelte delle formiche in base al livello di feromone */
    private double beta;
    /** Quantita' di feromone depositato dalle formiche */
    private double quantitaFeromone;
    /** Quantita' di cibo raccolto */
    private int ciboRaccolto;
    /** Numero di formiche attive (fuori dal nido) */
    private int formicheAttive;
    /** Miglior percorso nido-cibo trovato dalle formiche */
    private double migliorPercorso;
    /** Passo in cui e' stato trovato il miglior percorso nido-cibo */
    private int passoMigliorPercorso;
    /** Passo di iterazione della colonia */
    private int contaPassiColonia;
    /** Numero di formiche che possono uscire contemporaneamente dal nido */
    private int flussoFormiche;

    /**
     * Crea una colonia di formiche con parametri predefiniti per quantita' di
     * feromone, alpha e beta.
     * @param terreno terreno su cui si trova la colonia
     * @param numForm numero di formiche della colonia
     * @param vitaFormi vita massima di ogni formica
     * @param maxConc numero massimo di formiche per casella
     */
    public Colonia(Terreno terreno, int numForm, int vitaForm) {
        this(terreno, numForm, vitaForm, 5.0, 1.0, 5.0);
    }

    /**
     * Crea una colonia di formiche
     * @param terreno terreno su cui si trova la colonia
     * @param numForm numero di formiche della colonia
     * @param vitaFormi vita massima di ogni formica
     * @param qFer Quantita' di feromone depositato dalle formiche
     * @param aplha Livello di attrazione delle caselle senza feromone
     * @param beta Nonlinearita' delle scelte delle formiche in base al livello di feromone
     */
    public Colonia(Terreno terreno, int numForm, int vitaForm, double qFer, double alpha, double beta) {
        this.fato= new Random();
        this.numeroFormiche= numForm;
        this.terreno= terreno;
        this.vitaMassima= vitaForm;
//        this.flussoFormiche= this.numeroFormiche/this.vitaMassima;
//        if (this.flussoFormiche <= 0) {
//            this.flussoFormiche= 1;
//        }
//        this.flussoFormiche= this.flussoFormiche * 2;
        this.flussoFormiche= 4;
        System.out.println("Numero di formiche che escono dal nido per ogni passo: " + flussoFormiche);
        this.ciboRaccolto= 0;
        this.migliorPercorso= Double.POSITIVE_INFINITY;
        this.passoMigliorPercorso= 0;
        this.contaPassiColonia= 0;
        this.formicheAttive= 0;

        coloniaForm= new Formica[this.numeroFormiche];
        for (int k=0; k<this.numeroFormiche; k++) {
            coloniaForm[k]= new Formica();
        }
        
        this.alpha= alpha;
        this.beta= beta;
        this.quantitaFeromone= qFer;
    }

    /**
     * Esegue un passo dell'intera colonia
     */
    public void passo() {
        int numDormienti= 0;
        int k=0;

        contaPassiColonia++;
        while (k<numeroFormiche) {
            if ( (numDormienti<flussoFormiche) && (!coloniaForm[k].formicaAttiva()) ) {
                coloniaForm[k].avviaRicercaCibo();
                numDormienti++;
            } else {
                // eseguo un passo della formica, che puo' essere ricerca cibo o torna al nido
                coloniaForm[k].passo();
            }
            k++;
        }
    }

    public int getFlussoFormiche() {
        return flussoFormiche;
    }

    /**
     * Restituiscve il numero di formiche attive (in ricerca del cibo o di ritorno al nido)
     * @return numero formiche attive
     */
    public int getFormicheAttive() {
        return formicheAttive;
    }

    /**
     * Imposta la quantita' di feromone rilasciato dalle formiche
     * @param f quantita' di feromone (>0)
     */
    public void setQuantitaFeromone(double f) {
        if (f<=0) {
            throw new RuntimeException("Le formiche devono rilasciare una quantita' di feromone >0");
        }
        quantitaFeromone= f;
    }

    /**
     * Imposta alpha.
     * <p>
     * Alpha e' il livello di attrazione delle caselle senza feromone.
     * @param f alpha
     */
    public void setAlpha(double alpha) {
        this.alpha= alpha;
    }

    /**
     * Imposta beta.
     * <p>
     * Beta e' il coefficiente di nonlinearita' delle scelte delle formiche in base al livello di feromone.
     * @param f alpha
     */
    public void setBeta(double beta) {
        this.beta= beta;
    }

    /**
     * Restituisce il numero di formiche della colonia
     * @return
     */
    public int getDimColonia() {
        return numeroFormiche;
    }
    /**
     * Restituisce la vita massima delle formiche
     * @return
     */
    public int getVitaFormiche() {
        return vitaMassima;
    }
    /**
     * Restituisce la quantita' di cibo raccolto dalla colonia
     * @return cibo raccolto
     */
    public int getCiboRaccolto() {
        return ciboRaccolto;
    }

    /**
     * Restituisce la quantita' di feromone rilasciato dalle formiche
     * @return
     */
    public double getQuantitaFeromone() {
        return quantitaFeromone;
    }
    /**
     * Restituisce il parametro alpha
     * @return
     */
    public double getAlpha() {
        return alpha;
    }
    /**
     * Restituisce il parametro beta
     * @return
     */
    public double getBeta() {
        return beta;
    }

    /**
     * Restituisce il miglior percorso nido-cibo trovato dalle formiche.
     * Restituisce +inf se ancora non e' stato trovato un percorso nido-cibo
     * <p>
     * Questo valore viene aggiornato non appena una formica trova del cibo.
     * @return miglior percorso nodo-cibo
     */
    public double getMigliorPercorso() {
        return migliorPercorso;
    }


    /**
     * Restituisce il passo in cui e' stato effettuato il miglior percorso nido-cibo trovato dalle formiche.
     * Restituisce 0 se ancora non e' stato trovato un percorso nido-cibo
     * <p>
     * Questo valore viene aggiornato non appena una formica trova del cibo.
     * @return passo del miglior percorso nodo-cibo
     */
    public int getPassoMigliorPercorso() {
        return passoMigliorPercorso;
    }

    /**
     * Fa tornare tutte le formiche nel nido e azzera il contatore del cibo raccolto
     */
    public void initColonia() {

        for (int k=0; k<numeroFormiche; k++) {
            coloniaForm[k].uccidi();
        }
        ciboRaccolto= 0;
        migliorPercorso= Double.POSITIVE_INFINITY;
        passoMigliorPercorso= 0;
        contaPassiColonia= 0;
        formicheAttive= 0;
    }


    public Iterator<hungryants.Colonia.Formica> iterator() {
        return new iteraColonia();
    }

    /**
     * Itera le formiche attive (alla ricerca del cibo o di ritorno al nido)
     */
    private class iteraColonia implements Iterator<Formica> {
        /** Scorre l'array di formiche: indice della prossima formica da restituire */
        int cursore;
        /** Numero di formiche effettivamente attive */
        int formicheAttive;

        public iteraColonia() {
            super();
            formicheAttive= 0;
            // calcolo il numero di formiche attive
            for (int k=0; k<numeroFormiche; k++) {
                if (coloniaForm[k].formicaAttiva()) {
                    formicheAttive++;
                }
            }
            cursore= 0;
        }

        public boolean hasNext() {
            // itero fintanto ci sono altre formiche attive
            if ( (cursore < formicheAttive) && (cursore < numeroFormiche) ) {
                return true;
            } else {
                return false;
            }
        }

        public Formica next() {
            // salto le formiche non attive
            while (!coloniaForm[cursore].formicaAttiva()) {
                cursore++;
            }
            cursore++;
            return coloniaForm[cursore-1];
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    /**
     * * Caratteristiche delle formiche:
     * <dl>
     *      <ul>la formica e' sul terreno (in una data posizione) oppure non e' presente
     *      sul terreno (e' nel nido e probabilmente vi uscira' in seguito)
     *      <ul>posizione della formica sul terreno (ammesso che sia sul terreno)
     *      <ul>punto cardinale verso cui e' riviolta (N, E, S, W)
     *      <ul>stato della formica
     *      <ul>vita rimasta
     * </dl>
     * <p>
     * Le formiche cercano il cibo per un determinato numero di passi, al termine dei
     * quali la formica torna al nido per una nuova ricerca. In realta' allo scadere
     * del tempo la formica viene semplicemente rimossa dal terreno e fatta uscire
     * dal nido.
     * <p>
     * Le formiche che tornano con il cibo verso il nido depositano il feromone sul
     * loro percorso.
     */
    public class Formica {
        /** Posizone corrente della formica sul terreno */
        private Point posizione;
        /** Vale true se la formica e' sul terreno, false altrimenti (e' nel nido) */
        private boolean sulTerreno;
        /** La formica sta portando del cibo? */
        private boolean haCibo;
        /** Numero passi di vita rimasti. Quando vale 0 la formica e' morta. */
        private double vitaRimasta;
        /** Memoria del tragitto percorso nella ricerca del cibo */
        private Point percorso[];
        /** Indice per lo scorrimento del percorso (usato sia in fase di ricerca del cibo che di ritorno al nido).
         * E' L'indice in cui fare il prossimo inserimento
         */
        private int iPerc;
        /** Orientamento della formica */
        private Orientamento orientamento;

        /**
         * Crea una nuova formica posizionata dentro il nido. Per fare partire
         * la ricerca del cibo richiamare il metodo avviaRicercaCibo()
         */
        protected Formica() {
            posizione= terreno.getPosNido();
            sulTerreno= false;
            haCibo= false;
            vitaRimasta= vitaMassima;
            // creo il vettore in cui verra' memorizzato il percorso
            percorso= new Point[vitaMassima+1]; // memorizzo anche la posizione iniziale
            for (int k=0; k<vitaMassima+1; k++) {
                percorso[k]= new Point();
            }
            iPerc= 0;
            orientamento= Orientamento.N;
        }

        /**
         * Restituisce la posizione corrente della formica
         * @return posizione corrente
         */
        public Point getPosizione() {
            return posizione;
        }

        /**
         * Uccide forzatamente la formica (torna nel nido da cui puo' succesivamente
         * uscire
         */
        public void uccidi() {
            // ha senso uccidere solo le formiche attive
            if (formicaAttiva()) {
                sulTerreno= false;
                formicheAttive--;
            }
        }

        /**
         * Restituisce l'orientamento corrente della formica
         * @return orientamento corrente
         */
        public Orientamento getOrientamento() {
            return orientamento;
        }

        /**
         * Restituisce true se la formica e' attiva (sta cercando cibo oppure sta
         * tornando al nido), false altrimenti
         * @return vedi descrizione
         */
        public boolean formicaAttiva() {
            return sulTerreno;
        }

        /**
         * Verifica se la formica sta trasportando del cibo
         * @return true se la formica ha del cibno, false altrimenti
         */
        public boolean haCibo() {
            return haCibo;
        }

        /**
         * Avvia la ricerca del cibo della formica. Se la ricerca e' gia' stata
         * avviata (la formica sta cercando cibo oppure ha gia' trovato il cibo
         * e sta tornando al nido) restituisce false, altrimenti true.
         * @return true se e' stata effettivamente avviata la ricerca, false altrimenti
         */
        protected boolean avviaRicercaCibo() {
            if (formicaAttiva()) {
                return false;
            } else {
                posizione.setLocation(terreno.getPosNido());
                sulTerreno= true;
                haCibo= false;
                vitaRimasta= vitaMassima;
                iPerc= 0;
                formicheAttive++;

                return true;
            }
        }

        /**
         * Esegue un passo della formica,
         * @return true se al termine del passo la formica e' ancora sul terreno, false altrimenti
         */
        protected boolean passo() {
            boolean ret= false;

            if (sulTerreno && !haCibo) { // ricerca del cibo
                ret= ricercaCibo();
                if (!ret) {
                    formicheAttive--;
                }
            } else if (sulTerreno && haCibo) { // torna al nido
                ret= tornaAlNido();
                if (!ret) {
                    formicheAttive--;
                }
            }

            return ret;
        }

        /**
         * Esegue un passo di ritorno al nido della formica,
         * @return true se al termine del passo la formica e' ancora sul terreno, false altrimenti (ha raggiunto il nido)
         */
        protected boolean tornaAlNido() {
            boolean ret= true;

            // scorro a ritroso il percorso
            if (iPerc != 0) {
                depositaFeromone();

                iPerc--;
                posizione.setLocation(percorso[iPerc]);
                orientamento= Orientamento.orientSpostamento(percorso[iPerc+1], percorso[iPerc]);

                // sono arrivato al nido?
                if (terreno.getTipoCasella(posizione) == Terreno.TipoCasella.NIDO) {
                    // si' ci sono arrivato
                    ciboRaccolto++;
                    sulTerreno= false;
                    haCibo= false;
                    ret= false;
                }
            }
            return ret;
        }

        /**
         * Deposita il feromone sul terreno
         */
        protected void depositaFeromone() {
            /// ver. 1
            terreno.incFeromone(posizione, quantitaFeromone);

            /// ver. 2
//            double inc, v;
//            v= (double) vitaMassima;
//            inc= quantitaFeromone * v/ (v - vitaRimasta);
//            terreno.incFeromone(posizione, inc);

        }

        /**
         * Rimuove da un insieme di posizioni, quelle in cui la formica non puo'
         * andare a causa di ostacoli
         * @param pos
         */
        private void rimuoviPosizioniProibite(Point pos[]) {
            for (int k=0; k<pos.length; k++) {
                if (pos[k]!=null) {
                    if (!terreno.coordValida(pos[k])) {
                        pos[k]= null;
                    } else if ( (terreno.getTipoCasella(pos[k])==Terreno.TipoCasella.OSTACOLO) ||
                            (terreno.getTipoCasella(pos[k])==Terreno.TipoCasella.NIDO) ) {
                        pos[k]= null;
                    }
                }
                
            }
        }
        
        /**
         * Sceglie una posizione fra quelle date in base ai livelli di feromone
         * @param adiac possibili posizioni
         * @return posizione scelta
         */
        private Point scegliDoveAndare(Point adiac[]) {

            double prob[]= new double[adiac.length];
            double probTot= 0;
            double p;

            // tolgo le posizioni in cui non possono andare
            rimuoviPosizioniProibite(adiac);
            
            // calcolo le probabilità di scegliere le varie direzioni
            for (int k=0; k<adiac.length; k++) {
                if (adiac[k]!=null){
                    prob[k]= terreno.getFeromone(adiac[k]);
                    // calcolo prob[k] = (feromone + alpha)^beta
                    prob[k]= Math.pow(prob[k] + alpha, beta);
                } else {
                    // impedisco lo spostamento in questa posizione
                    prob[k]= 0;
                }
                probTot+= prob[k];
            }
            // calcolo la probabilita' "cumulata"
            for (int k=1; k<adiac.length; k++) {
                prob[k]= prob[k-1] + prob[k];
            }
            // scelgo casualmente dove orientarmi
            p= fato.nextDouble() * probTot;
            for (int k=0; k<adiac.length; k++) {
                if (p < prob[k]) {
                    return adiac[k];
                }
            }
            return null;
        }

        /**
         * Esegue un passo di ricerca del cibo della formica,
         * @return true se al termine del passo la formica e' ancora sul terreno, false altrimenti (e' morta)
         */
        protected boolean ricercaCibo() {

            Point []adiac;
            Point dest;

            if ( (terreno.getTipoCasella(posizione) == Terreno.TipoCasella.NIDO) && (iPerc==0) ) {
                // la formica si trova nel nido
                // imposto l'orientamento iniziale in base ai livelli di feromone

                percorso[0].setLocation(posizione);
                iPerc++;

                adiac= Orientamento.posizioniAdiacenti(posizione);
                // calcolo le probabilità di scegliere le varie direzioni
                dest= scegliDoveAndare(adiac);
                if (dest==null) {
                    throw new RuntimeException("non e' stato scelto un ORIENTAMENTO iniziale");
                }
                // non mi sposto nella posizione data, ma la uso per calcolare l'orientamento
                orientamento= Orientamento.orientSpostamento(posizione, dest);
            }

            // la formica e' orientata da qualche parte: scelgo dove spostarmi

            // provo prima in direzione frontale
            adiac= Orientamento.posizioniFrontali(posizione, orientamento);
            dest= scegliDoveAndare(adiac);
            /*if (dest == null) {
                // provo tutte le posizioni laterali
                adiac= Orientamento.posizioniLaterali(posizione, orientamento);
                rimuoviPosizioniProibite(adiac);
                dest= scegliDoveAndare(adiac);
            }*/
            if (dest == null) {
                // provo tutte le posizioni possibili
                adiac= Orientamento.posizioniAdiacenti(posizione);
                dest= scegliDoveAndare(adiac);
            }
            if (dest!=null) {
                // trovata la posizione dove andare
                orientamento= Orientamento.orientSpostamento(posizione, dest);

                // la posizione corrente e' gia' stata salvata
                percorso[iPerc].setLocation(dest); // salvo la posizione di destinazione
                iPerc++;
                posizione.setLocation(dest); // cambio posizione
                
                // la vita decrementa piu' velocemente per gli spostamenti in diagonale
                if ( (orientamento==Orientamento.N) ||
                        (orientamento==Orientamento.E) ||
                        (orientamento==Orientamento.S) ||
                        (orientamento==Orientamento.W) ) {
                    vitaRimasta-= 1.0;
                } else {
                    // mi sono mosso in diagonale
                    vitaRimasta-= 1.4142135623730950488;
                    //vitaRimasta-= 1;
                }

                // la formica si e' spostata... ho trovato il cibo?
                if (terreno.getTipoCasella(posizione) == Terreno.TipoCasella.CIBO) {
                    // ho trovato il cibo: si torna al nido
                    double perc;
                    perc= ((double)vitaMassima) - vitaRimasta;
                    if (perc < migliorPercorso) {
                        migliorPercorso= perc;
                        passoMigliorPercorso= contaPassiColonia;
                    }
                    haCibo= true;
                    iPerc--; // rimetto l'indice in una posizione valida

                }

            } // nota che se dest==null allora rimango fermo



            if ( (!haCibo) && (vitaRimasta <= 0) ) {
                // raggiunta la vita massima senza aver trovato cibo: la formica muore
                sulTerreno= false;
                return false;
            } else {
                return true;
            }
        }
    }
}
