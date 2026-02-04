package clases;

public class SistemaOperativo {

    private Dashboard gui;
    private Cola<PCB> colaListos;
    private PCB procesoEnCPU; // <--- Nuevo: ¿Quién está en la silla roja?
    
    public SistemaOperativo(Dashboard gui) {
        this.gui = gui;
        this.colaListos = new Cola<>();
        this.procesoEnCPU = null; // Al inicio no hay nadie
        initSistema();
    }
    
    private void initSistema() {
        // Procesos de prueba
        colaListos.encolar(new PCB("System_Boot", 10, 1, 50));
        colaListos.encolar(new PCB("Antenna_Check", 5, 2, 60));
        colaListos.encolar(new PCB("Beacon_Signal", 20, 1, 100));
        
        actualizarGUI();
    }
    
    public void ejecutarCiclo() {
        // 1. Planificador (Scheduler): ¿El CPU está libre?
        if (procesoEnCPU == null) {
            if (!colaListos.esVacia()) {
                // Sacamos al primero de la fila y lo pasamos al CPU
                procesoEnCPU = colaListos.desencolar();
                procesoEnCPU.setEstado("RUNNING");
            }
        }
        
        // 2. Ejecutar proceso (CPU)
        if (procesoEnCPU != null) {
            procesoEnCPU.avanzarInstruccion();
            
            // Si termina, lo sacamos (Simulación básica de salida)
            // Por ahora, si llega a 10 instrucciones, lo matamos para probar
            if (procesoEnCPU.getInstruccionesTotales() <= 0) {
                 procesoEnCPU.setEstado("EXIT");
                 procesoEnCPU = null; // CPU queda libre para el siguiente
            }
        }
        
        // 3. Refrescar pantallas
        actualizarGUI();
    }
    
    private void actualizarGUI() {
        // Panel Verde (Cola de Listos)
        if (gui != null) {
            gui.getTxtColaListos().setText(colaListos.toString());
            
            // Panel Rojo (CPU)
            if (procesoEnCPU != null) {
                gui.getTxtCPU().setText(procesoEnCPU.toString());
            } else {
                gui.getTxtCPU().setText("[Esperando procesos...]");
            }
        }
    }
}