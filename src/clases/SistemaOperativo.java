package clases;

public class SistemaOperativo {

    private Dashboard gui;
    private Cola<PCB> colaListos;
    private Cola<PCB> colaBloqueados;
    private Cola<PCB> colaListosSuspendidos;
    private Cola<PCB> colaBloqueadosSuspendidos;
    private PCB procesoEnCPU;
    
    // --- VARIABLES DEL RELOJ Y CONTROL (HILOS) ---
    private int relojGlobal = 0;          // Cuenta los ciclos totales
    private int duracionCiclo = 1000;     // 1000 ms = 1 segundo por defecto
    private boolean simulacionActiva = false; // Controla si el hilo corre o se detiene
    private Thread hiloReloj; 
    
    // --- VARIABLES DE MEMORIA ---
    private final int LIMITE_MEMORIA = 5; // Máximo 5 procesos permitidos en RAM a la vez
    
    public SistemaOperativo(Dashboard gui) {
        this.gui = gui;
        this.colaListos = new Cola<>();
        this.colaBloqueados = new Cola();
        this.procesoEnCPU = null;
        initSistema();
    }
    
    public void admitirProceso(PCB p) {
        int procesosEnRAM = colaListos.getTamano() + colaBloqueados.getTamano();
        if (procesoEnCPU != null) procesosEnRAM++; // Sumamos el que está en CPU

        if (procesosEnRAM < LIMITE_MEMORIA) {
            // Hay espacio: Va a RAM (Verde)
            p.setEstado("READY");
            colaListos.encolar(p);
            if(gui != null) gui.imprimirLog("🟢 [RAM] Nuevo proceso en RAM: " + p.getNombre());
        } else {
            // No hay espacio: Va a Disco (Swap - Listo Suspendido)
            p.setEstado("READY_SUSPENDED");
            colaListosSuspendidos.encolar(p);
            if(gui != null) gui.imprimirLog("💾 [DISCO] Memoria Llena. Cae en Suspendido: " + p.getNombre());
        }
        actualizarGUI();
    }
    
    private void initSistema() {
        // Inicializar colas de Swap que faltaban arriba
        this.colaListosSuspendidos = new Cola<>();
        this.colaBloqueadosSuspendidos = new Cola<>();

        // Procesos de prueba usando el Admisionador
        admitirProceso(new PCB("System_Boot", 10, 1, 50));
        admitirProceso(new PCB("Antenna_Check", 5, 2, 60));
        admitirProceso(new PCB("Beacon_Signal", 20, 1, 100));
        
        this.colaListosSuspendidos = new Cola<>();
        this.colaBloqueadosSuspendidos = new Cola<>();
        
        if (gui != null) {
            // Acción del botón Meteorito
            gui.getBtnInterrupcion().addActionListener(e -> {
                bloquearProceso();
                gui.imprimirLog("!! ALERTA: Interrupción de Hardware (Meteorito)");
            });
            
            // --- NUEVO: PASO 2.3 - Acción del botón Iniciar ---
            gui.getBtnIniciar().addActionListener(e -> {
                iniciarSimulacion(); 
            });
        }
        
        if(gui != null) gui.imprimirLog(">> Sistema Iniciado: Procesos cargados en RAM.");
        actualizarGUI();
    }
    
    // --- MÉTODOS DEL HILO (THREAD) ---

    public void iniciarSimulacion() {
        if (!simulacionActiva) { 
            simulacionActiva = true;
            
            // Creación del Hilo 
            hiloReloj = new Thread(() -> {
                while (simulacionActiva) {
                    try {
                        relojGlobal++; // Aumenta el reloj global
                        
                        if (gui != null) {
                            gui.imprimirLog("=====================================");
                            gui.imprimirLog("⏱️ INICIANDO CICLO DE RELOJ: " + relojGlobal);
                        }
                        
                        ejecutarCiclo(); 
                        
                        Thread.sleep(duracionCiclo); 
                        
                    } catch (InterruptedException e) {
                        gui.imprimirLog("⚠️ Error en el hilo del reloj: " + e.getMessage());
                    }
                }
            });
            
            hiloReloj.start(); // Arranca el motor
            gui.imprimirLog("🚀 SIMULACIÓN INICIADA");
        }
    }

    public void detenerSimulacion() {
        simulacionActiva = false;
        if (gui != null) {
            gui.imprimirLog("🛑 SIMULACIÓN DETENIDA");
        }
    }

    public void cambiarVelocidad(int milisegundos) {
        this.duracionCiclo = milisegundos;
        if (gui != null) {
            gui.imprimirLog("⚙️ Velocidad cambiada a " + milisegundos + " ms por ciclo.");
        }
    }
    
    public int getRelojGlobal() {
        return relojGlobal;
    }
    
    public void ejecutarCiclo() {
        
        // -------------------------------------------------------------
        // 1. GESTOR DE TRÁFICO (BLOQUEADOS Y SWAP) 
        // -------------------------------------------------------------
        
        // A. Calcular ocupación actual de RAM
        int procesosEnRAM = colaListos.getTamano() + colaBloqueados.getTamano() + (procesoEnCPU != null ? 1 : 0);

        // B. Swap-In: Si hay espacio en RAM y hay procesos castigados en Disco, los subimos a RAM
        if (procesosEnRAM < LIMITE_MEMORIA && !colaListosSuspendidos.esVacia()) {
            PCB p = colaListosSuspendidos.desencolar();
            p.setEstado("READY");
            colaListos.encolar(p);
            procesosEnRAM++; // Aumentamos la cuenta
            if (gui != null) gui.imprimirLog("⬆ [SWAP-IN] Recuperado a RAM: " + p.getNombre());
        }

        // C. Simular fin de bloqueo (Solo para prueba temporal: 20% de probabilidad de que un bloqueado se libere)
        if (!colaBloqueados.esVacia() && Math.random() < 0.2) { 
            PCB p = colaBloqueados.desencolar();
            p.setEstado("READY");
            colaListos.encolar(p);
            if (gui != null) gui.imprimirLog("✅ [I/O] Fin de espera. Vuelve a Listos: " + p.getNombre());
        }

        // -------------------------------------------------------------
        // 2. PLANIFICADOR DE CPU 
        // -------------------------------------------------------------
        
        // Si el CPU está libre, busca trabajo
        if (procesoEnCPU == null) {
            if (!colaListos.esVacia()) {
                procesoEnCPU = colaListos.desencolar();
                procesoEnCPU.setEstado("RUNNING");
                gui.imprimirLog("[CPU] Ejecutando: " + procesoEnCPU.getNombre());
            } else {
                // Si no hay nada en RAM, el CPU descansa
                gui.getTxtCPU().setText("[Esperando procesos...]"); 
            }
        }

        // Si hay un proceso, lo ejecuta
        if (procesoEnCPU != null) {
            procesoEnCPU.ejecutar(); // Resta 1 instrucción
            
            // Si terminó
            if (procesoEnCPU.getInstruccionesRestantes() <= 0) {
                procesoEnCPU.setEstado("TERMINATED");
                gui.imprimirLog("🏁 [FIN] Proceso terminado: " + procesoEnCPU.getNombre());
                procesoEnCPU = null; // Liberar CPU
            }
        }
        
        // Actualizar pantalla siempre al final
        actualizarGUI();
    }
    
    public void bloquearProceso() {
        // Solo podemos bloquear si hay alguien en el CPU
        if (procesoEnCPU != null) {
            // 1. Cambiamos su estado a BLOQUEADO
            procesoEnCPU.setEstado("BLOCKED");
            
            // 2. Lo mandamos a la cola amarilla
            colaBloqueados.encolar(procesoEnCPU);
            
            // 3. Liberamos el CPU
            procesoEnCPU = null;
            
            // 4. Actualizamos la pantalla para ver el cambio inmediato
            actualizarGUI();
        }
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
            if (colaBloqueados.esVacia()) {
                gui.getTxtColaBloqueados().setText("[Cola vacía]");
            } else {
                gui.getTxtColaBloqueados().setText(colaBloqueados.toString());
            }
            // Paneles de Swap
        if (gui != null) {
            gui.getTxtColaSuspendidos().setText(colaListosSuspendidos.toString());
            gui.getTxtColaBloqSusp().setText(colaBloqueadosSuspendidos.toString());
        }
        }
    }
}