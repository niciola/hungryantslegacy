/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Mappa.java
 *
 * Created on 2-feb-2012, 15.44.03
 */

package hungryants.gui;

import hungryants.Colonia;
import hungryants.Terreno;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;

/**
 *
 * @author nicola
 */
public class Mappa extends javax.swing.JPanel {

    private Terreno terreno=null;
    private Colonia colonia=null;
    private Convertitore cc;
    private double fondoScalaFeromone= 500;

    /**
     * Costruttore senza parametri per poterlo usato nell'IDE NetBeans
     */
    public Mappa() {
        terreno= null;
        colonia= null;
        initComponents();
    }

    /**
     * Passa alla mappa il terreno e la colonia da disegnare
     * @param t il terreno
     * @param c la colonia di formiche
     * @param dim dimensione in pixel delle caselle
     */
    public void setScenario(Terreno t, Colonia c, int dim) {
        this.terreno= t;
        this.colonia= c;
        this.cc= new Convertitore(dim);
        this.setSize(cc.getDim() * terreno.getWidth(), cc.getDim() * terreno.getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (terreno!=null) {
            myPaintComponent(g);
        }
    }

    /**
     * Esegue il ridisegno vero e proprio dando per veri l'inizializzazione del
     * terreno e della colonia
     * @param g
     */
    private void myPaintComponent(Graphics g) {
        Graphics2D g2= (Graphics2D) g;
        int larg, alt;
        Point pos= new Point();
        Terreno.TipoCasella tipoCas;
        int dimCaselle= cc.getDim();

        alt= terreno.getHeight();
        larg= terreno.getWidth();

        // disegno il terreno
        for (int x=0; x<larg; x++) {
            for (int y=0; y<alt; y++) {
                pos.setLocation(x, y);

                tipoCas= terreno.getTipoCasella(pos);
                switch (tipoCas) {
                    case CIBO:
                        g2.setColor(Color.blue);
                        break;
                    case LIBERO:
                        // Tinta fra 0 e 360 (primi 60 bianco-giallo)
                        // Tinta fra 60 e 240 (primi 60 bianco-verde)
                        double f= terreno.getFeromone(pos);
                        //f= f + colonia.getAlpha();
                        //f= f/colonia.getAlpha()-1;
                        if (f>fondoScalaFeromone) f=fondoScalaFeromone;
                        f= f/fondoScalaFeromone;
                        //f= Math.pow(f, 1/colonia.getBeta());
                        f= Math.sqrt(f);
                        //f= (Math.log10(f+1.0/10.0)+1.0)/(Math.log10(1.0+1.0/10.0)+1.0);
                        f= f*180;
                        Color c;
                        if (f<60) {
                            // verde: H=120; giallo: H=60
                            c= Color.getHSBColor(120.0f/360.0f, (float)(f/60), 1.0f);
                        } else {
                            c= Color.getHSBColor((float) (f+60.0)/360.0f, 1.0f, 1.0f);
                        }
                        g2.setColor(c);

                        break;
                    case NIDO:
                        g2.setColor(Color.orange);
                        break;
                    case OSTACOLO:
                        g2.setColor(Color.darkGray);
                        break;
                }
                g2.fillRect(cc.c2p(x), cc.c2p(y), dimCaselle, dimCaselle);

                // contorno a nido e cibo
                switch (tipoCas) {
                    case CIBO:
                        g2.setColor(Color.gray);
                        g2.drawRect(cc.c2p(x), cc.c2p(y), dimCaselle-1, dimCaselle-1);
                        break;
                    case NIDO:
                        g2.setColor(Color.black);
                        g2.drawRect(cc.c2p(x), cc.c2p(y), dimCaselle-1, dimCaselle-1);
                        break;
                }
            }
        }

        // disegno le formiche
        Point nido= terreno.getPosNido();
        for (Colonia.Formica f : colonia) {
            Color dentro, contorno;
            
            pos.setLocation(f.getPosizione());

            if (terreno.getTipoCasella(f.getPosizione()) == Terreno.TipoCasella.CIBO) {
                // sopra il cibo
                dentro= Color.blue;
                contorno= Color.white;
            } else if (f.haCibo()) {
                // con il cibo in posizione libera
                //dentro= Color.white;
                //contorno= Color.black;
                dentro= Color.red;
                contorno= Color.red;
            } else if (f.getPosizione().equals(nido)) {
                // nel nido prima di partire
                dentro= Color.orange;
                contorno= Color.black;
            } else {
                // formiche in cerca di cibo
                dentro= Color.black;
                contorno= Color.black;
                //contorno= Color.gray;
            }
            int deltaF, largF;
            if (dimCaselle<=10) {
                deltaF= dimCaselle/2-1;
                largF= 3;
            } else {
                deltaF= dimCaselle/4;
                largF= dimCaselle/2;
            }
            g2.setColor(dentro);
            g2.fillRect(cc.c2p(pos.x)+deltaF, cc.c2p(pos.y)+deltaF, largF, largF);
            g2.setColor(contorno);
//            g2.drawRect(cc.c2p(pos.x)+deltaF, cc.c2p(pos.y)+deltaF, largF-1, largF-1);
        }
    }

    public void setFondoScalaFeromone(double f) {
        if (f<=0) {
            throw new RuntimeException("Fondo scala feromone >0");
        }
        this.fondoScalaFeromone= f;
    }

    public double getFondoScalaFeromone() {
        return this.fondoScalaFeromone;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setMinimumSize(new java.awt.Dimension(100, 100));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    


    /**
     * Override per aver il tool-tip che segue il cursorse del mouse
     */
    @Override
    public Point getToolTipLocation(MouseEvent event) {
        Point p= event.getPoint();
        p.translate(20, 10);
        return p;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
