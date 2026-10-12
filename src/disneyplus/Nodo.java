package disneyplus;

import disneyplus.Pantalla2.Contenido;

/**
 * Nodo de la lista circular doblemente enlazada.
 * Guarda una película y las referencias a sus dos vecinos.
 */
public class Nodo {

    Contenido pelicula;
    Nodo siguiente;
    Nodo anterior;

    public Nodo(Contenido pelicula) {
        this.pelicula = pelicula;
        // un nodo recién creado está solo: se apunta a sí mismo en ambas direcciones
        this.siguiente = this;
        this.anterior = this;
    }
}
