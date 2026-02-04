package clases;

// Nodo genérico para poder guardar cualquier cosa 
public class Nodo<T> {
    private T contenido;
    private Nodo<T> pNext;

    // Constructor
    public Nodo(T contenido) {
        this.contenido = contenido;
        this.pNext = null;
    }

    // Getters y Setters
    public T getContenido() {
        return contenido;
    }

    public void setContenido(T contenido) {
        this.contenido = contenido;
    }

    public Nodo<T> getpNext() {
        return pNext;
    }

    public void setpNext(Nodo<T> pNext) {
        this.pNext = pNext;
    }
}
