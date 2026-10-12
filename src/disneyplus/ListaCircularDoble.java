package disneyplus;

import disneyplus.Pantalla2.Contenido;
import java.util.ArrayList;

/**
 * Lista circular doblemente enlazada de películas.
 *
 * - cabeza apunta al primer nodo agregado; cabeza.anterior es el último.
 * - El último nodo apunta a la cabeza con "siguiente" y la cabeza apunta
 *   al último con "anterior", por eso el recorrido nunca llega a null.
 * - actual es el nodo que se está mostrando en la ficha de detalles.
 */
public class ListaCircularDoble {

    private Nodo cabeza;
    private Nodo actual;
    private int tamanio;

    public boolean estaVacia() {
        return cabeza == null;
    }

    public int tamanio() {
        return tamanio;
    }

    // Inserta al final para conservar el orden en que se agregan las películas.
    public void agregar(Contenido pelicula) {
        Nodo nuevo = new Nodo(pelicula);

        if (cabeza == null) {
            cabeza = nuevo;
            actual = nuevo;
        } else {
            Nodo ultimo = cabeza.anterior;
            nuevo.anterior = ultimo;
            nuevo.siguiente = cabeza;
            ultimo.siguiente = nuevo;
            cabeza.anterior = nuevo;
        }
        tamanio++;
    }

    public Contenido obtenerActual() {
        return actual == null ? null : actual.pelicula;
    }

    // Avanza al nodo siguiente. Desde el último vuelve a la cabeza.
    public Contenido obtenerSiguiente() {
        if (actual == null) {
            return null;
        }
        actual = actual.siguiente;
        return actual.pelicula;
    }

    // Retrocede al nodo anterior. Desde la cabeza salta al último.
    public Contenido obtenerAnterior() {
        if (actual == null) {
            return null;
        }
        actual = actual.anterior;
        return actual.pelicula;
    }

    // Mueve "actual" al nodo que contiene esa película. Devuelve false si no está en la lista.
    public boolean irA(Contenido pelicula) {
        Nodo nodo = buscarNodo(pelicula);
        if (nodo == null) {
            return false;
        }
        actual = nodo;
        return true;
    }

    // Posición (empezando en 1) de la película actual, para mostrar "3 de 7".
    public int posicionActual() {
        if (actual == null) {
            return 0;
        }
        int posicion = 1;
        Nodo recorrido = cabeza;
        while (recorrido != actual) {
            recorrido = recorrido.siguiente;
            posicion++;
        }
        return posicion;
    }

    // Búsqueda por título exacto, sin distinguir mayúsculas ni espacios extra.
    public Contenido buscar(String titulo) {
        if (cabeza == null || titulo == null) {
            return null;
        }
        String buscado = titulo.trim();
        Nodo recorrido = cabeza;
        do {
            if (recorrido.pelicula.getNombre().equalsIgnoreCase(buscado)) {
                return recorrido.pelicula;
            }
            recorrido = recorrido.siguiente;
        } while (recorrido != cabeza);
        return null;
    }

    public boolean eliminar(String titulo) {
        Contenido pelicula = buscar(titulo);
        return pelicula != null && eliminar(pelicula);
    }

    public boolean eliminar(Contenido pelicula) {
        Nodo nodo = buscarNodo(pelicula);
        if (nodo == null) {
            return false;
        }

        if (tamanio == 1) {
            cabeza = null;
            actual = null;
        } else {
            // los vecinos se apuntan entre sí y el nodo queda fuera del círculo
            nodo.anterior.siguiente = nodo.siguiente;
            nodo.siguiente.anterior = nodo.anterior;
            if (nodo == cabeza) {
                cabeza = nodo.siguiente;
            }
            if (nodo == actual) {
                actual = nodo.siguiente;
            }
        }
        nodo.siguiente = nodo;
        nodo.anterior = nodo;
        tamanio--;
        return true;
    }

    // Copia de las películas en orden, desde la cabeza. Sirve para dibujar el catálogo.
    public ArrayList<Contenido> aLista() {
        ArrayList<Contenido> resultado = new ArrayList<>();
        if (cabeza == null) {
            return resultado;
        }
        Nodo recorrido = cabeza;
        do {
            resultado.add(recorrido.pelicula);
            recorrido = recorrido.siguiente;
        } while (recorrido != cabeza);
        return resultado;
    }

    private Nodo buscarNodo(Contenido pelicula) {
        if (cabeza == null || pelicula == null) {
            return null;
        }
        Nodo recorrido = cabeza;
        do {
            if (recorrido.pelicula == pelicula) {
                return recorrido;
            }
            recorrido = recorrido.siguiente;
        } while (recorrido != cabeza);
        return null;
    }
}
