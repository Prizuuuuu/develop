package clases;

public class Reloj extends Thread {
    private Dashboard gui; // Referencia a la interfaz para poder actualizarla
    private int ciclos;    // Contador de tiempo global
    private boolean activo;
    
    // Configurable: Cuánto dura un ciclo en la vida real (en milisegundos)
    // El PDF pide poder modificar esto [cite: 252]
    private int tiempoCiclo = 1000; 

    public Reloj(Dashboard gui) {
        this.gui = gui;
        this.ciclos = 0;
        this.activo = false;
    }

    @Override
    public void run() {
        while (true) {
            if (activo) {
                try {
                    // 1. Actualizar el contador interno
                    ciclos++;
                    
                    // 2. Actualizar la Interfaz Gráfica (El JLabel del header)
                    // Usamos SwingUtilities para no congelar la ventana
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        gui.getLblReloj().setText("TIEMPO MISIÓN: " + ciclos + " ciclos");
                    });
                    
                    // 3. Aquí pronto llamaremos al Planificador para que revise las colas
                    // planificador.ejecutarCiclo(); (Pendiente para la próxima fase)

                    // 4. Esperar lo que dure el ciclo (Simulación de tiempo)
                    Thread.sleep(tiempoCiclo);
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                // Si está pausado, espera un poco para no quemar CPU
                try { Thread.sleep(100); } catch (InterruptedException e) {}
            }
        }
    }

    // --- Controles del Reloj ---
    
    public void iniciar() {
        this.activo = true;
    }
    
    public void pausar() {
        this.activo = false;
    }
    
    public int getCiclos() {
        return ciclos;
    }
    
    public void setTiempoCiclo(int ms) {
        this.tiempoCiclo = ms;
    }
}