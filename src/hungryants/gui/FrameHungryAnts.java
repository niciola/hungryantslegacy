/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * FrameHungryAnts.java
 *
 * Created on 31-gen-2012, 9.42.30
 */

package hungryants.gui;

import hungryants.Colonia;
import hungryants.Terreno;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.event.ListDataListener;

/**
 *
 * @author nicola
 */
public class FrameHungryAnts extends javax.swing.JFrame {
    /*
     * modello e statistiche
     */
    private Terreno terreno;
    private Colonia colonia;
    private int contaPassi;

    /*
     * Variabili per l'avvio della simulazione.
     * Tipi di simulazione:
     *  - UNO: esegue un singolo passo di simulazione
     *  - INTEREATTIVO: esegue in continuazione la simulazione fino a quando
     *    l'utente non lo ferma. Durante la simulazione aggiorna la mappa.
     *  - BATCH: esegue N di passi senza aggiornare la mappa. Terminati gli N passi
     *    si ferma e aggiorna la mappa
     */
    private enum TipoSimulazione {UNO, INTERATTIVO, BATCH};
    private int burst; // usato in modo INTERATTIVO
    private int burstBatch; // usato in modo BATCH
    private TipoSimulazione simul;
    private boolean modificabile; // true se non e' in corso una simulazione

    /*
     * animazioni
     */
    private Timer animazio;
    private Convertitore cc;
    private final int dimensioneCaselle= 7;

    /*
     * Cartella di default per carica/salva
     */
    private static File defaultDir= null;

    /** Creates new form FrameHungryAnts */
    public FrameHungryAnts() {
        initComponents();
        setModificabile(true, true);
        burst= 1;
        burstBatch= 5000;
    }


   /**
    * Esegue la GUI
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrameHungryAnts frame= new FrameHungryAnts();

                frame.setVisible(true);
                frame.nuovoScenario(40, 40, new Point(5,5), 100, 150);
                frame.terreno.setTipoCasella(new Point(34, 34), Terreno.TipoCasella.CIBO);
            }
        });
    }

    /**
     * Crea un nuovo scenario (terreno + colonia) chiedendo all'utente i parametri
     * di terreno e colonia
     */
    public void nuovoScenario() {

        JOptionPane.showMessageDialog(this,
                "Per creare un nuovo scenario ti chiedero':\n" +
                "    - dimensione del terreno\n" +
                "    - numero di formiche\n" +
                "    - vita delle formiche.\n" +
                "L'angolo in alto a sinistra ha coordinate (0,0).\n" +
                "Il nido verra' posizionato in posizione (0,0), modificabile successivamente.\n" +
                "Gli altri parametri del modello sono prelevati dal pannello a destra.");

        int terWidth, terHeight, numForm, vitaForm;
        String str;

        str= JOptionPane.showInputDialog(this, "Larghezza terreno", 40);
        if (str==null) return;
        terWidth= Integer.parseInt(str);

        str= JOptionPane.showInputDialog(this, "Altezza terreno", 40);
        if (str==null) return;
        terHeight= Integer.parseInt(str);

        str= JOptionPane.showInputDialog(this, "Numero di formiche", 100);
        if (str==null) return;
        numForm= Integer.parseInt(str);

        str= JOptionPane.showInputDialog(this, "Vita massima delle formiche", 150);
        if (str==null) return;
        vitaForm= Integer.parseInt(str);

        nuovoScenario(terWidth, terHeight, new Point(0,0), numForm, vitaForm);
    }

    /**
     * Crea un nuovo scenario (terreno + colonia) chiedendo all'utente i parametri
     * della sola colonia
     */
    public void nuovoScenario(Terreno ter) {

        JOptionPane.showMessageDialog(this,
                "Per creare la colonia ti chiedero':\n" +
                "    - numero di formiche\n" +
                "    - vita delle formiche.\n" +
                "Gli altri parametri del modello sono prelevati dal pannello a destra.");

        int numForm, vitaForm;
        String str;

        str= JOptionPane.showInputDialog(this, "Numero di formiche", 100);
        if (str==null) return;
        numForm= Integer.parseInt(str);

        str= JOptionPane.showInputDialog(this, "Vita massima delle formiche", 150);
        if (str==null) return;
        vitaForm= Integer.parseInt(str);

        nuovoScenario(ter, numForm, vitaForm);
    }

    /**
     * Crea un nuovo scenario (terreno + colonia).
     */
    public void nuovoScenario(int width, int height, Point nido, int numFormiche, int vitaFormiche) {
        Terreno t= new Terreno(width, height, nido);
        nuovoScenario(t, numFormiche, vitaFormiche);
    }

    /**
     * Crea un nuovo scenario (terreno + colonia).
     */
    public void nuovoScenario(Terreno ter, int numFormiche, int vitaFormiche) {
        // creo terreno e colonia
        terreno= ter;
        colonia= new Colonia(terreno, numFormiche, vitaFormiche);
        contaPassi= 0;
        cc= new Convertitore(dimensioneCaselle);
        mappa.setScenario(terreno, colonia, cc.getDim());

        // simulo il click dell'utente sui bottoni per raccogliere i parametri
        butAggiornaFormiche.doClick();
        butAggiornaTerreno.doClick();

        // timer per animazione
        Action updateCursorAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int numIterazioni=0;

                switch (simul) {
                    case UNO:
                        // passo singolo
                        numIterazioni= 1;
                        break;
                    case INTERATTIVO:
                        // selezionato avvia/pausa
                        numIterazioni= burst;
                        break;
                    case BATCH:
                        // multi passo
                        numIterazioni= burstBatch;
                        break;
                }

                // ver. tanti batch
//                if (simul==TipoSimulazione.BATCH) {
//                    for (int i=0; i<20; i++) {
//                        for (int k=0; k<numIterazioni; k++) {
//                            terreno.evaporaFeromone();
//                            colonia.passo();
//                            contaPassi++;
//                        }
//
//                        System.out.println(
//                                contaPassi + ";" +
//                                colonia.getCiboRaccolto() + ";" +
//                                colonia.getMigliorPercorso() + ";" +
//                                colonia.getPassoMigliorPercorso());
//
//                        // init colonia
//                        colonia.initColonia();
//                        contaPassi= 0;
//                        // init terreno
//                        terreno.azzeraFeromone();
//                    }
//                }
                // ver. normale
                for (int k=0; k<numIterazioni; k++) {
                    terreno.evaporaFeromone();
                    colonia.passo();
                    contaPassi++;
                }
                

                aggiornaInfoPassi();
                mappa.repaint();
                if (simul==TipoSimulazione.BATCH) {
                    setModificabile(true, false);
                }
            }
        };
        // impostare qui il periodo dell'animazione
        animazio= new Timer(40, updateCursorAction);
        //animazio.setCoalesce(false);

        aggiornaInfo();

        // regola la dimensione della mappa
        Dimension dim= new Dimension(terreno.getWidth()*cc.getDim(), terreno.getHeight()*cc.getDim());
        mappa.setMinimumSize(dim);
        mappa.setMaximumSize(dim);
        mappa.setPreferredSize(dim);
    }

    /**
     * Aggiorna conteggio numero di passi
     */
    public void aggiornaInfoPassi() {
        String p1, p2;
        double d;
        d= colonia.getMigliorPercorso();
        if (Double.isInfinite(d)) {
            p1= "///";
            p2= "///";
        } else {
            d= Math.floor( (colonia.getMigliorPercorso()*100.0) + 0.5 ); // arrotondamento -> Math.round()
            p1= Double.toString(d/100.0);
            p2= Integer.toString(colonia.getPassoMigliorPercorso());
        }
        txtInfoPassi.setText(
                "Num. passi: " + contaPassi +
                "\nCibo raccolto: " + colonia.getCiboRaccolto() +
                "\nPercorso min.: " + p1  +
                "\nPasso perc. min.: " + p2 +
                "\nFormiche attive: " + colonia.getFormicheAttive()
                );
    }

    /**
     * Aggiorna informazioni sui parametri
     */
    public void aggiornaInfo() {
        aggiornaInfoPassi();
        txtParametri.setText(
                "Terreno: " + terreno.getWidth() + "x" + terreno.getHeight() +
                "\nNum. formiche: " + colonia.getDimColonia() +
                "\nVita formiche: " + colonia.getVitaFormiche() +
                "\nNascite per passo: " + colonia.getFlussoFormiche() +
                "\nFeromone: " + colonia.getQuantitaFeromone() +
                "\nAlpha: " + colonia.getAlpha() +
                "\nBeta: " + colonia.getBeta() +
                "\nEvaporazione: " + terreno.getEvapRate()
                );

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        scorriMappa = new javax.swing.JScrollPane();
        mappa = new hungryants.gui.Mappa();
        panStrumenti = new javax.swing.JPanel();
        butAvviaPausa = new javax.swing.JToggleButton();
        butPassoSingolo = new javax.swing.JButton();
        butReinizializza = new javax.swing.JButton();
        butMultiPasso = new javax.swing.JButton();
        txtInfoPassi = new javax.swing.JTextArea();
        txtParametri = new javax.swing.JTextArea();
        panParametri = new javax.swing.JTabbedPane();
        panFormiche = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtFeromoneFormiche = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtAlpha = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtBeta = new javax.swing.JTextField();
        butAggiornaFormiche = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        butInitColonia = new javax.swing.JButton();
        butNuovaColonia = new javax.swing.JButton();
        panTerreno = new javax.swing.JPanel();
        chkDisegnabile = new javax.swing.JCheckBox();
        tipiCaselle = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        txtLivFeromone = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        txtEvapora = new javax.swing.JTextField();
        butAggiornaTerreno = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        butAzzeraFeromone = new javax.swing.JButton();
        panVarie = new javax.swing.JPanel();
        butNuovoScenario = new javax.swing.JButton();
        butCarica = new javax.swing.JButton();
        butSalva = new javax.swing.JButton();
        butFondoScala = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Angry Ants");
        setMinimumSize(new java.awt.Dimension(600, 400));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        scorriMappa.setMinimumSize(new java.awt.Dimension(100, 100));
        scorriMappa.setPreferredSize(new java.awt.Dimension(400, 300));

        mappa.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                mappaMouseReleased(evt);
            }
        });
        mappa.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                mappaMouseMoved(evt);
            }
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                mappaMouseDragged(evt);
            }
        });

        javax.swing.GroupLayout mappaLayout = new javax.swing.GroupLayout(mappa);
        mappa.setLayout(mappaLayout);
        mappaLayout.setHorizontalGroup(
            mappaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        mappaLayout.setVerticalGroup(
            mappaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 562, Short.MAX_VALUE)
        );

        scorriMappa.setViewportView(mappa);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(scorriMappa, gridBagConstraints);

        panStrumenti.setMaximumSize(new java.awt.Dimension(200, 580));
        panStrumenti.setMinimumSize(new java.awt.Dimension(200, 580));
        panStrumenti.setPreferredSize(new java.awt.Dimension(200, 580));
        panStrumenti.setLayout(new java.awt.GridBagLayout());

        butAvviaPausa.setMnemonic('a');
        butAvviaPausa.setText("Avvia");
        butAvviaPausa.setToolTipText("Avvia/pausa simulazione");
        butAvviaPausa.setFocusable(false);
        butAvviaPausa.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butAvviaPausa.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butAvviaPausa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butAvviaPausaActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panStrumenti.add(butAvviaPausa, gridBagConstraints);

        butPassoSingolo.setMnemonic('p');
        butPassoSingolo.setText("Un passo");
        butPassoSingolo.setToolTipText("Singolo passo");
        butPassoSingolo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butPassoSingoloActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panStrumenti.add(butPassoSingolo, gridBagConstraints);

        butReinizializza.setMnemonic('z');
        butReinizializza.setText("Azzera");
        butReinizializza.setToolTipText("Reinizializza colonia e feromone");
        butReinizializza.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butReinizializzaActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panStrumenti.add(butReinizializza, gridBagConstraints);

        butMultiPasso.setMnemonic('m');
        butMultiPasso.setText("Multi passo");
        butMultiPasso.setToolTipText("Esegue N passi");
        butMultiPasso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butMultiPassoActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panStrumenti.add(butMultiPasso, gridBagConstraints);

        txtInfoPassi.setColumns(20);
        txtInfoPassi.setEditable(false);
        txtInfoPassi.setRows(5);
        txtInfoPassi.setText("Num. passi:\nCibo raccolto:\nPercorso min.:\nPasso perc. min.:\nFormiche attive:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 6, 5);
        panStrumenti.add(txtInfoPassi, gridBagConstraints);

        txtParametri.setColumns(20);
        txtParametri.setEditable(false);
        txtParametri.setRows(8);
        txtParametri.setText("Terreno:\nNum. formiche:\nVita Formiche:\nNascite per passo:\nFeromone:\nAlpha:\nBeta:\nEvaporazione:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 6, 5);
        panStrumenti.add(txtParametri, gridBagConstraints);

        panFormiche.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText("Feromone");
        jLabel2.setToolTipText("Feromone rilasciato");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panFormiche.add(jLabel2, gridBagConstraints);

        txtFeromoneFormiche.setText("1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panFormiche.add(txtFeromoneFormiche, gridBagConstraints);

        jLabel3.setText("Alpha");
        jLabel3.setToolTipText("Attrazione base (feromone escluso)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panFormiche.add(jLabel3, gridBagConstraints);

        txtAlpha.setText("2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panFormiche.add(txtAlpha, gridBagConstraints);

        jLabel4.setText("Beta");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panFormiche.add(jLabel4, gridBagConstraints);

        txtBeta.setText("5");
        txtBeta.setToolTipText("Coefficiente di non-linearità");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panFormiche.add(txtBeta, gridBagConstraints);

        butAggiornaFormiche.setText("Aggiorna");
        butAggiornaFormiche.setToolTipText("Aggiorna parametri di questa scheda");
        butAggiornaFormiche.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butAggiornaFormicheActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panFormiche.add(butAggiornaFormiche, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panFormiche.add(jSeparator3, gridBagConstraints);

        butInitColonia.setText("Iniz. colonia");
        butInitColonia.setToolTipText("Reinizializza la colonia");
        butInitColonia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butInitColoniaActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panFormiche.add(butInitColonia, gridBagConstraints);

        butNuovaColonia.setText("Nuova colonia");
        butNuovaColonia.setToolTipText("Nuovo colonia");
        butNuovaColonia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butNuovaColoniaActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panFormiche.add(butNuovaColonia, gridBagConstraints);

        panParametri.addTab("Formiche", panFormiche);

        panTerreno.setLayout(new java.awt.GridBagLayout());

        chkDisegnabile.setText("Disegna");
        chkDisegnabile.setToolTipText("Spunta per modificare il terreno");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panTerreno.add(chkDisegnabile, gridBagConstraints);

        tipiCaselle.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ostacolo", "Cibo", "Nido", "Libero", "Feromone" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panTerreno.add(tipiCaselle, gridBagConstraints);

        jLabel1.setText("Feromone:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 10);
        panTerreno.add(jLabel1, gridBagConstraints);

        txtLivFeromone.setText("50");
        txtLivFeromone.setToolTipText("Livello di feromone");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panTerreno.add(txtLivFeromone, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panTerreno.add(jSeparator1, gridBagConstraints);

        jLabel5.setText("Evaporazione");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panTerreno.add(jLabel5, gridBagConstraints);

        txtEvapora.setText("0.01");
        txtEvapora.setToolTipText("Tasso di evaporazione");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panTerreno.add(txtEvapora, gridBagConstraints);

        butAggiornaTerreno.setText("Aggiorna");
        butAggiornaTerreno.setToolTipText("Aggiorna parametri di questa scheda");
        butAggiornaTerreno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butAggiornaTerrenoActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panTerreno.add(butAggiornaTerreno, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panTerreno.add(jSeparator2, gridBagConstraints);

        butAzzeraFeromone.setText("Azzera feromone");
        butAzzeraFeromone.setToolTipText("Azzera il feromone in tutto il terreno");
        butAzzeraFeromone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butAzzeraFeromoneActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panTerreno.add(butAzzeraFeromone, gridBagConstraints);

        panParametri.addTab("Terreno", panTerreno);

        panVarie.setLayout(new java.awt.GridBagLayout());

        butNuovoScenario.setText("Nuovo scenario");
        butNuovoScenario.setToolTipText("Nuovo terreno e nuova colonia");
        butNuovoScenario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butNuovoScenarioActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panVarie.add(butNuovoScenario, gridBagConstraints);

        butCarica.setText("Carica terreno");
        butCarica.setToolTipText("Carica terreno");
        butCarica.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butCaricaActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panVarie.add(butCarica, gridBagConstraints);

        butSalva.setText("Salva terreno");
        butSalva.setToolTipText("Salva terreno");
        butSalva.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butSalvaActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panVarie.add(butSalva, gridBagConstraints);

        butFondoScala.setText("Fondo scala");
        butFondoScala.setToolTipText("Fondo scala feromone (per la colorazione)");
        butFondoScala.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butFondoScalaActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panVarie.add(butFondoScala, gridBagConstraints);

        panParametri.addTab("Varie", panVarie);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panStrumenti.add(panParametri, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(panStrumenti, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Avvia/pausa della simulazione: modo INTERATTIVO
     * @param evt
     */
    private void butAvviaPausaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butAvviaPausaActionPerformed
        // TODO add your handling code here:
        if (butAvviaPausa.isSelected()) {
            // devvo avviare
            
            // burst
            String str= JOptionPane.showInputDialog(this, "Numero di passi per ogni frame di animazione.\n" +
                    "(Un valore elevato fa andare piu' veloce l'animazione)", burst);
            if (str==null) {
                butAvviaPausa.setSelected(false);
                return;
            }
            burst= Integer.parseInt(str);
            simul= TipoSimulazione.INTERATTIVO;
            setModificabile(false, true);
            animazio.setRepeats(true);
            animazio.start();
        } else {
            // devo fermare
            setModificabile(true, true);

            animazio.stop();
            aggiornaInfoPassi();
        }

    }//GEN-LAST:event_butAvviaPausaActionPerformed

    /**
     * Esegue un singolo passo di simulazione: modo UNO
     * @param evt
     */
    private void butPassoSingoloActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butPassoSingoloActionPerformed
        
        simul= TipoSimulazione.UNO;
        animazio.setRepeats(false);
        animazio.start();
    }//GEN-LAST:event_butPassoSingoloActionPerformed

    /**
     * Crea un nuovo scenario da zero chiedendo tutti i parametri all'utente.
     * @param evt
     */
    private void butNuovoScenarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butNuovoScenarioActionPerformed
        
        nuovoScenario();
    }//GEN-LAST:event_butNuovoScenarioActionPerformed

    /**
     * Visualizza il livello di feromone nella casella sottostante il mouse
     */
    private void mappaMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mappaMouseMoved

        int x, y;
        String str;
        Point pos= evt.getPoint();
        x= cc.p2c(pos.x);
        y= cc.p2c(pos.y);
        pos.setLocation(x, y);
        str = "<html>";
        if (terreno.coordValida(pos)) {
            str= str + "(" + pos.x + ", " + pos.y + ")";
            if (terreno.casellaAttraversabile(pos)) {
                str= str + "<br>Feromone = " + terreno.getFeromone(pos);
            }
            mappa.setToolTipText(str);
        } else {
            mappa.setToolTipText("");
        }
    }//GEN-LAST:event_mappaMouseMoved

    /**
     * Aggiorna i parametri del terreno
     * @param evt
     */
    private void butAggiornaTerrenoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butAggiornaTerrenoActionPerformed

        double d;
        d= Double.parseDouble(txtEvapora.getText());
        terreno.setEvapRate(d);
        aggiornaInfo();
    }//GEN-LAST:event_butAggiornaTerrenoActionPerformed

    /**
     * Aggiorna i parametri della colonia
     * @param evt
     */
    private void butAggiornaFormicheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butAggiornaFormicheActionPerformed

        double f, a, b;

        f= Double.parseDouble(txtFeromoneFormiche.getText());
        a= Double.parseDouble(txtAlpha.getText());
        b= Double.parseDouble(txtBeta.getText());

        colonia.setQuantitaFeromone(f);
        colonia.setAlpha(a);
        colonia.setBeta(b);

        mappa.repaint();

        aggiornaInfo();
    }//GEN-LAST:event_butAggiornaFormicheActionPerformed

    /**
     * Permette di modificare la mappa (solo se non e' in corso una simulazione).
     * Questo evento gestisce il trascinamento.
     */
    private void mappaMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mappaMouseDragged

        mappaMouseReleased(evt);
        
    }//GEN-LAST:event_mappaMouseDragged

    /**
     * Permette di modificare la mappa (solo se non e' in corso una simulazione)
     */
    private void mappaMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mappaMouseReleased

        int x, y, fer;
        Terreno.TipoCasella vecchio, nuovo;
        String str;

        // ottengo la coordinata in termini di numero di caselle
        Point pos= evt.getPoint();
        x= cc.p2c(pos.x);
        y= cc.p2c(pos.y);
        pos.setLocation(x, y);

        /*
         * eseguo le operazioni solo se:
         *   - e' permesso
         *   - e' selezionata il check box "disegna"
         *   - il mouse si trova in una coordinata valida
         */
        if ( modificabile && chkDisegnabile.isSelected() && terreno.coordValida(pos) ){
            
            str= (String) tipiCaselle.getSelectedItem();
            if (str.equals("Feromone")) {
                // modifica del livello di feromone
                fer= Integer.parseInt(txtLivFeromone.getText());

                terreno.setFeromone(pos, fer);

            } else {
                // modifica del terreno
                if (str.equals("Cibo")) {
                    nuovo= Terreno.TipoCasella.CIBO;
                } else if (str.equals("Libero")) {
                    nuovo= Terreno.TipoCasella.LIBERO;
                } else if (str.equals("Nido")) {
                    nuovo= Terreno.TipoCasella.NIDO;
                } else {
                    nuovo= Terreno.TipoCasella.OSTACOLO;
                }

                vecchio= terreno.getTipoCasella(pos);
                if ( (vecchio != Terreno.TipoCasella.NIDO) && (nuovo != Terreno.TipoCasella.NIDO) ) {
                    terreno.setTipoCasella(pos, nuovo);
                    uccidiFormiche(pos);
                } else if (nuovo == Terreno.TipoCasella.NIDO) {
                    // cambio posizione al nido
                    terreno.moveNido(pos);
                }
            }

            // aggiorno la visualizzazione
            this.repaint();
        }
    }//GEN-LAST:event_mappaMouseReleased

    /**
     * Azzera il feromone su tutto il terreno
     * @param evt
     */
    private void butAzzeraFeromoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butAzzeraFeromoneActionPerformed

        terreno.azzeraFeromone();
        mappa.repaint();

    }//GEN-LAST:event_butAzzeraFeromoneActionPerformed

    /**
     * Salva il solo terreno su file
     * @param evt
     */
    private void butSalvaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butSalvaActionPerformed

        JFileChooser fc = new JFileChooser();
        File file;
        fc.setCurrentDirectory(defaultDir);
        fc.setApproveButtonText("Salva");
        fc.setDialogTitle("Salva Terreno");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.showSaveDialog(this);
        defaultDir= fc.getCurrentDirectory();

        file= fc.getSelectedFile();

        if (file == null) {
            // premuto annulla
            return;
        }
        try {
            //Stream di output bufferizzato da associare al file su cui salvare
            BufferedOutputStream fos= null;
            ObjectOutputStream oos= null;
            try{
                fos= new BufferedOutputStream(new FileOutputStream(file));
                oos= new ObjectOutputStream(fos);
            }catch(FileNotFoundException e){
                //file non trovato: ne creo uno con il pathname specificato
                JOptionPane.showMessageDialog(this, "File non trovato.\n" + e.getMessage(), "Attenzione", JOptionPane.ERROR_MESSAGE);
            }

            //salvo su file
            oos.writeObject(terreno);
            //chiudo lo stream aperto
            oos.flush();
            oos.close();
        }catch(IOException e){
            //errore di I/O
            JOptionPane.showMessageDialog(this, "Si e' verificato un errore di I/O.\n" + e.getMessage(), "Attenzione", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_butSalvaActionPerformed

    /**
     * Carica il terreno da file e crea con esso un nuovo scenario (quindi crea
     * anche una nuova colonia)
     * @param evt
     */
    private void butCaricaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butCaricaActionPerformed

        JFileChooser fc = new JFileChooser();
        File file;
        Terreno ter;
        
        fc.setCurrentDirectory(defaultDir);
        fc.setApproveButtonText("Carica");
        fc.setDialogTitle("Carica terreno");
        fc.showOpenDialog(this);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        defaultDir= fc.getCurrentDirectory();
        file= fc.getSelectedFile();

        if (file == null) {
            // premuto annulla
            return;
        }

        try {
            //Stream di input bufferizzato da associare al file da cui caricare
            BufferedInputStream fis= null;
            ObjectInputStream ois= null;
            //apertura dello stream
            fis= new BufferedInputStream(new FileInputStream(file));
            ois= new ObjectInputStream(fis);

            //carico da file
            ter= (Terreno) ois.readObject();
            //chiusura dello stream
            ois.close();

            // sovrascrivo i parametri del terreno presenti nella scheda
            //txtEvapora.setText(Double.toString(ter.getEvapRate()));

            // creo il nuovo scenario
            nuovoScenario(ter);


        }catch(FileNotFoundException e){
            //File non trovato
            JOptionPane.showMessageDialog(this, "Il file non e' stato trovato nel percorso specificato\n" + e.getMessage(), "Attenzione", JOptionPane.ERROR_MESSAGE);
        }catch(IOException e){
            //Errore di I/O
            JOptionPane.showMessageDialog(this, "Si e' verificato un errore nel caricamento\n" + e.getMessage(), "Attenzione", JOptionPane.ERROR_MESSAGE);
        }catch(ClassNotFoundException e){
            //Incoerenza di classi in assegnamento
            JOptionPane.showMessageDialog(this, "Classe di riferimento non trovata\n" + e.getMessage(), "Attenzione", JOptionPane.ERROR_MESSAGE);
        }

        mappa.repaint();
    }//GEN-LAST:event_butCaricaActionPerformed

    /**
     * Fa tornare la colonia al nido (la inizializza)
     * @param evt
     */
    private void butInitColoniaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butInitColoniaActionPerformed

        colonia.initColonia();
        contaPassi= 0;
        aggiornaInfoPassi();
        mappa.repaint();
    }//GEN-LAST:event_butInitColoniaActionPerformed

    /**
     * Crea una nuova colonia mantenendo lo stesso terreno
     * @param evt
     */
    private void butNuovaColoniaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butNuovaColoniaActionPerformed

        nuovoScenario(terreno);
    }//GEN-LAST:event_butNuovaColoniaActionPerformed

    /**
     * Azzera il feromone e fa tornare la colonia al nido
     * @param evt
     */
    private void butReinizializzaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butReinizializzaActionPerformed

        butInitColonia.doClick();
        butAzzeraFeromone.doClick();
        
}//GEN-LAST:event_butReinizializzaActionPerformed

    /**
     * Cambia il fondoscala usato per colorare il feromone.
     * @param evt
     */
    private void butFondoScalaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butFondoScalaActionPerformed

        String msg;
        msg= "Inserisci il valore di fondo scala del feromone.";
        String str= JOptionPane.showInputDialog(this, msg, mappa.getFondoScalaFeromone());
        if (str!=null) {
            double d= Double.parseDouble(str);
            if (d>0) {
                mappa.setFondoScalaFeromone(d);
            } else {
                JOptionPane.showMessageDialog(this, "Il valore di fondo scala deve essere >0");
            }
        }
        mappa.repaint();
    }//GEN-LAST:event_butFondoScalaActionPerformed

    /**
     * Simulazione multipasso: modo BATCH
     * @param evt
     */
    private void butMultiPassoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butMultiPassoActionPerformed

        // eseguo N passi (burst singolo)
        String str= JOptionPane.showInputDialog(this, "Numero di passi da eseguire.", burstBatch);
        if (str==null) {
            return;
        }
        burstBatch= Integer.parseInt(str);
        setModificabile(false, false);

        simul= TipoSimulazione.BATCH;
        animazio.setRepeats(false);
        animazio.start();

    }//GEN-LAST:event_butMultiPassoActionPerformed

    /**
     * Uccide tutte le formiche in una data posizione (tornano al nido)
     * @param p punto dove uccidere
     */
    private void uccidiFormiche(Point p) {
        for (Colonia.Formica f : colonia) {
            // cerco le formiche nella posizione data e attive
            if ( (f.getPosizione().equals(p)) && (f.formicaAttiva()) ) {
                f.uccidi();
            }
        }
    }

    /**
     * Attiva/disattiva la possibilita' di modificare la mappa
     * @param mod true per attivare la possibilita' di modifica
     * @param interattivo true se e' stato spinto il pulsante avvia/pausa
     */
    private void setModificabile(boolean mod, boolean interattivo) {
        modificabile= mod;

        butNuovoScenario.setEnabled(modificabile);
        butPassoSingolo.setEnabled(modificabile);
        butReinizializza.setEnabled(modificabile);
        butMultiPasso.setEnabled(modificabile);

        panParametri.setVisible(modificabile);
        
        if (interattivo) {
            if (modificabile) {
                butAvviaPausa.setText("Avvia");
            } else {
                butAvviaPausa.setText("Pausa");
            }
        } else {
            butAvviaPausa.setEnabled(modificabile);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butAggiornaFormiche;
    private javax.swing.JButton butAggiornaTerreno;
    private javax.swing.JToggleButton butAvviaPausa;
    private javax.swing.JButton butAzzeraFeromone;
    private javax.swing.JButton butCarica;
    private javax.swing.JButton butFondoScala;
    private javax.swing.JButton butInitColonia;
    private javax.swing.JButton butMultiPasso;
    private javax.swing.JButton butNuovaColonia;
    private javax.swing.JButton butNuovoScenario;
    private javax.swing.JButton butPassoSingolo;
    private javax.swing.JButton butReinizializza;
    private javax.swing.JButton butSalva;
    private javax.swing.JCheckBox chkDisegnabile;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private hungryants.gui.Mappa mappa;
    private javax.swing.JPanel panFormiche;
    private javax.swing.JTabbedPane panParametri;
    private javax.swing.JPanel panStrumenti;
    private javax.swing.JPanel panTerreno;
    private javax.swing.JPanel panVarie;
    private javax.swing.JScrollPane scorriMappa;
    private javax.swing.JComboBox tipiCaselle;
    private javax.swing.JTextField txtAlpha;
    private javax.swing.JTextField txtBeta;
    private javax.swing.JTextField txtEvapora;
    private javax.swing.JTextField txtFeromoneFormiche;
    private javax.swing.JTextArea txtInfoPassi;
    private javax.swing.JTextField txtLivFeromone;
    private javax.swing.JTextArea txtParametri;
    // End of variables declaration//GEN-END:variables

}
