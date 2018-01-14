/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hungryants;

import java.awt.Point;

/**
 * Definisce gli orientamenti (o direzioni) che la formica puo' assumere.
 * <p>
 * Gli orientamenti corrispondono ai punti cardinali.
 * @author nicola
 */
public enum Orientamento {
    N, S, W, E, NE, NW, SE, SW;

    /**
     * Data una posizione iniziale e una posizione finale,
     * determina l'orientamento necessario per tale spostamento.
     * <p>
     * Ad esempio se la posizione finale e' a nord-est rispetto a quella iniziale,
     * l'orientamento calcolato e' "nord-est"
     * @param sorg posizione iniziale
     * @param dest posizione finale
     * @return orientamento
     */
    public static Orientamento orientSpostamento(Point sorg, Point dest) {
        Orientamento o;
        if (sorg.x == dest.x) {
            if (dest.y > sorg.y) {
                o= S;
            } else {
                o= N;
            }
        } else if (sorg.y == dest.y) {
            if (dest.x > sorg.x) {
                o= E;
            } else {
                o= W;
            }
        } else if (dest.x > sorg.x) {
            if (dest.y > sorg.y) {
                o= SE;
            } else {
                o= NE;
            }
        } else { // (dest.x < sorg.x)
            if (dest.y > sorg.y) {
                o= SW;
            } else {
                o= NW;
            }
        }
        return o;
    }

    /**
     * Restituisce true se tutte le posizioni sono null
     * @param pos
     * @return
     */
    private static boolean isVuoto(Point pos[]) {
        boolean vuoto= true;
        int k= 0;

        while  ( (k<pos.length) && vuoto ) {
            if (pos[k]!=null) {
                vuoto= false;
            }
        }

        return vuoto;
    }

    /**
     * Calcola la posizione adiacente e frontale (cioe' davanti) ad una data posizione.
     * @param pos posizione di riferimento
     * @param or orientamento
     * @return la posizione frontale
     */
    public static Point posizioneDavanti(Point pos, Orientamento or) {
        Point adiac= null;
        // calcolo la posizione adiacente a "pos" nella direzione "or"
        switch (or) {
            case N:
                adiac= new Point(pos.x, pos.y-1);
                break;
            case NE:
                adiac= new Point(pos.x+1, pos.y-1);
                break;
            case E:
                adiac= new Point(pos.x+1, pos.y);
                break;
            case SE:
                adiac= new Point(pos.x+1, pos.y+1);
                break;
            case S:
                adiac= new Point(pos.x, pos.y+1);
                break;
            case SW:
                adiac= new Point(pos.x-1, pos.y+1);
                break;
            case W:
                adiac= new Point(pos.x-1, pos.y);
                break;
            case NW:
                adiac= new Point(pos.x-1, pos.y-1);
                break;
        }
        return adiac;
    }

    /**
     * Restituisce tutte le posizioni adiacenti a quella data poste frontalmente
     * rispetto ad una direzione. Restituisce anche le posizioni non valide.
     * <p>
     * Ad esesmpio se l'orientamento e' NORD, le posizioni frontali sono NORD-OVEST,
     * NORD e NORD-EST.
     * @param pos posizione di riferimento
     * @param or orientamento di riferimento
     * @return posizioni adiacenti
     */
    public static Point[] posizioniFrontali(Point pos, Orientamento or) {
        Point []arr;
        Point adiac=null;
        int k, iAdiac, len;

        adiac= posizioneDavanti(pos, or);
        arr= posizioniAdiacenti(pos);
        len= arr.length;

        // cerco dove si trova "adiac" nell'array "arr"
        k= 0;
        while (!arr[k].equals(adiac)) {
            k++;
        }
        iAdiac= k;
        /// ver. 4 direzioni
//        for (k=1; k<arr.length; k+=2) {
//            arr[k]=null;
//        }
//        for (k=0; k<arr.length; k++) {
//            if (arr[k]!=null && arr[k].equals(adiac)) {
//                break;
//            }
//        }
//        iAdiac= k;

        /*
         Per avere solo le posizioni frontali, devo mantenere solo l'elemento
         precedente e seguente a "iAdiac". Gli altri vanno posti a null
         */
        for (k=0; k<arr.length; k++) {
            if ( (k!=mod(iAdiac-1, len)) && (k!=iAdiac) && (k!=mod(iAdiac+1, len)) ) {
//            if ( (k!=mod(iAdiac-2, len)) && (k!=iAdiac) && (k!=mod(iAdiac+2, len)) ) {
                arr[k]= null;
            }
        }

        return arr;
    }


    /**
     * Restituisce tutte le posizioni adiacenti a quella data poste lateralmente
     * rispetto ad una direzione. Restituisce anche le posizioni non valide.
     * <p>
     * Ad esesmpio se l'orientamento e' NORD, le posizioni laterali sono OVEST ed
     * EST.
     * @param pos posizione di riferimento
     * @param or orientamento di riferimento
     * @return posizioni adiacenti
     */
    public static Point[] posizioniLaterali(Point pos, Orientamento or) {
        Point []arr;
        Point adiac=null;
        int k, iAdiac, len;

        adiac= posizioneDavanti(pos, or);
        arr= posizioniAdiacenti(pos);
        len= arr.length;

        // cerco dove si trova "adiac" nell'array "arr"
        k= 0;
        while (!arr[k].equals(adiac)) {
            k++;
        }
        iAdiac= k;

        /*
         Per avere solo le posizioni laterali, gli altri vanno posti a null
         */
        for (k=0; k<len; k++) {
            if ( (k!=mod(iAdiac-2, len)) && (k!=mod(iAdiac+2, len)) ) {
                arr[k]= null;
            }
        }

        return arr;
    }

    /**
     * Restituisce tutte le posizioni adiacenti a quella data, anche quelle non valide
     * @param pos posizione di riferimento
     * @return posizioni adiacenti
     */
    public static Point[] posizioniAdiacenti(Point pos) {
        Point []arr= new Point[8];
        Point p;

        // NORD
        p= new Point(pos.x, pos.y-1);
        arr[0]= p;

        // NORD-EST
        p= new Point(pos.x+1, pos.y-1);
        arr[1]= p;

        // EST
        p= new Point(pos.x+1, pos.y);
        arr[2]= p;

        // SUD-EST
        p= new Point(pos.x+1, pos.y+1);
        arr[3]= p;

        // SUD
        p= new Point(pos.x, pos.y+1);
        arr[4]= p;

        // SUD-OVEST
        p= new Point(pos.x-1, pos.y+1);
        arr[5]= p;

        // OVEST
        p= new Point(pos.x-1, pos.y);
        arr[6]= p;

        // NORD-OVEST
        p= new Point(pos.x-1, pos.y-1);
        arr[7]= p;

        return arr;
    }



    public static double mod(double x, double y) {
        return x - y * Math.floor (x / y);
    }
}
