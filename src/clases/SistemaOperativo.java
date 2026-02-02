package clases;

public class SistemaOperativo {

    private Dashboard gui;
    private Cola<PCB> colaListos;
    // Más adelante agregaremos colaBloqueados, colaSuspendidos, etc.
    
    public SistemaOperativo(Dashboard gui) {
        this.gui = gui;
        this.colaListos = new Cola<>();
        initSistema();
    }
    
    // Configuración inicial del sistema
    private void initSistema() {
        // Creamos procesos "falsos" para probar que la GUI funciona
        colaListos.encolar(new PCB("System_Boot", 10, 1, 50));
        colaListos.encolar(new PCB("Antenna_Check", 5, 2, 60));
        colaListos.encolar(new PCB("Beacon_Signal", 20, 1, 100));
        colaListos.encolar(new PCB("Battery_Log", 8, 3, 200));
        
        // Actualizamos la pantalla
        actualizarGUI();
    }
    
    // Este método lo llamará el Reloj cada 1 segundo
    public void ejecutarCiclo() {
        // Aquí irá la lógica compleja (Round Robin, SRT, etc.)
        // Por ahora, solo refrescamos la pantalla
        actualizarGUI();
    }
    
    private void actualizarGUI() {
        // Escribe el contenido de la cola en el panel verde
        gui.getTxtColaListos().setText(colaListos.toString());
    }
}
