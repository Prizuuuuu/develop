/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author gaming
 */
package clases;

public class Cola<T> {
    private Nodo<T> pFirst;
    private Nodo<T> pLast;
    private int size;

    public Cola() {
        this.pFirst = null;
        this.pLast = null;
        this.size = 0;
    }

    // Método para agregar al final (Encolar)
    public void encolar(T dato) {
        Nodo<T> pNuevo = new Nodo<>(dato);
        if (esVacia()) {
            pFirst = pNuevo;
            pLast = pNuevo;
        } else {
            pLast.setpNext(pNuevo);
            pLast = pNuevo;
        }
        size++;
    }

    // Método para sacar del inicio (Desencolar)
    public T desencolar() {
        if (esVacia()) {
            return null;
        }
        T dato = pFirst.getContenido();
        pFirst = pFirst.getpNext();
        size--;
        
        if (esVacia()) { // Si quedó vacía, pLast también es null
            pLast = null;
        }
        return dato;
    }

    // Ver quién es el primero sin sacarlo
    public T dispatch() {
        return esVacia() ? null : pFirst.getContenido();
    }

    public boolean esVacia() {
        return pFirst == null;
    }

    public int getSize() {
        return size;
    }
}
