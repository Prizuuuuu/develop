package proyecto1.lucasgomez;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author gaming
 */
public class Nodo<T> {
    private T contenido; 
    private Nodo<T> pNext; 

    public Nodo(T contenido) {
        this.contenido = contenido;
        this.pNext = null;
    }

    // Getters y Setters necesarios
    public T getContenido() { return contenido; }
    public void setContenido(T contenido) { this.contenido = contenido; }
    public Nodo<T> getpNext() { return pNext; }
    public void setpNext(Nodo<T> pNext) { this.pNext = pNext; }
}