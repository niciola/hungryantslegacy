# Descrizione

HungryAnts implementa un [Ant System](https://en.wikipedia.org/wiki/Ant_colony_optimization_algorithms) per la risoluzione del Central Place Foraging problem, riprendendo molto da vicino il comportamento reale delle formiche. L'obiettivo è infatti cercare il percorso più breve fra un punto iniziale (il nido) e una destinazione (il cibo). Il principio alla base di questi algoritmi può essere utilizzato anche per la risoluzione di problemi di scheduling, problema del commesso viaggiatore e routing dei pacchetti in una rete.

Questo programma è stato sviluppato come progetto di esame universitario. Rimando alla relazione del progetto (file `hungry_ants.pdf`) per i dettagli.

HubgryAnts è stato sviluppato con NetBeans e JDK 1.7

# Modello

Il modello si basa su una matrice di punti in cui le formiche possono muoversi. La probabilità *p_k* che la formica si sposti nella posizione *k* ad essa adiacente è proporzionale a *(alpha + f_k)^beta* dove
* *f_k* è la quantità di feromone presente nella posizione *k*
* *alpha* è il livello di attrazione delle posizioni prive di feromone
* *beta* rappresenta la non-linearità della decisione

Nel programma si può anche definire la quantità di feromone depositato ad ogni passo dalle formiche e la percentuale di evaporazione del feromone stesso.

Lasciando invariati gli altri parametri, aumentando *beta* si aumenta la sensibilità a piccole variazioni di feromone: l'algoritmo convergerà più velocemente verso un certo percorso, ma tale percorso può risultare poco performante. Con valori bassi di *beta* si ottengono risultati migliori al costo di una più lenta convergenza. Valori troppo bassi di *beta* impediscono del tutto la convergenza dell'algoritmo.
