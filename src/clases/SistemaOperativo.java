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
    
    // --- VARIABLES DE PLANIFICACIÓN (ALGORITMOS) ---
    private String algoritmoActual = "FCFS"; // Algoritmo por defecto
    private int quantumRR = 3;               // RR: 3 ciclos de tiempo en CPU
    private int contadorQuantum = 0;         // RR: Cuenta cuánto lleva el proceso actual
    
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
        
        if (gui != null) {
            // Acción del botón Meteorito
            gui.getBtnInterrupcion().addActionListener(e -> {
                bloquearProceso();
                gui.imprimirLog("!! ALERTA: Interrupción de Hardware (Meteorito)");
            });
            
            // Acción del botón Iniciar
            gui.getBtnIniciar().addActionListener(e -> {
                iniciarSimulacion(); 
            });
            
            // Acción para el ComboBox de algoritmos
            if (gui.getCmbAlgoritmos() != null) {
                gui.getCmbAlgoritmos().addActionListener(e -> {
                    algoritmoActual = gui.getCmbAlgoritmos().getSelectedItem().toString();
                    gui.imprimirLog("🔄 ALGORITMO CAMBIADO A: " + algoritmoActual);
                    contadorQuantum = 0; // Reiniciamos el reloj de RR por si acaso
                });
            }    
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
                        if(gui != null) gui.imprimirLog("⚠️ Error en el hilo del reloj: " + e.getMessage());
                    }
                }
            });
            
            hiloReloj.start(); // Arranca el motor
            if(gui != null) gui.imprimirLog("🚀 SIMULACIÓN INICIADA");
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
    
    // Método especial para EDF: Extrae el proceso con el menor tiempo límite (sin romper la regla de no usar ArrayLists)
    private PCB sacarProcesoMenorDeadline() {
        if (colaListos.esVacia()) return null;
        
        PCB mejorProceso = colaListos.desencolar();
        int menorDeadline = mejorProceso.getDeadline();
        int tamano = colaListos.getTamano();
        colaListos.encolar(mejorProceso); // Lo volvemos a meter para iniciar el ciclo
        
        // 1. Damos la vuelta a la cola buscando el menor deadline
        for (int i = 1; i < tamano; i++) {
            PCB p = colaListos.desencolar();
            if (p.getDeadline() < menorDeadline) {
                menorDeadline = p.getDeadline();
                mejorProceso = p;
            }
            colaListos.encolar(p);
        }
        
        // 2. Damos otra vuelta para extraer definitivamente al ganador
        for (int i = 0; i < tamano; i++) {
            PCB p = colaListos.desencolar();
            if (p != mejorProceso) {
                colaListos.encolar(p); // Si no es el ganador, vuelve a la cola
            }
        }
        
        return mejorProceso;
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
        // 2. PLANIFICADOR DE CPU (CON ALGORITMOS)
        // -------------------------------------------------------------
        
        // --- A. ROUND ROBIN: PREEMPTION (Expropiación por tiempo) ---
        if (algoritmoActual.contains("RR") || algoritmoActual.contains("Round")) {
            if (procesoEnCPU != null) {
                contadorQuantum++;
                if (contadorQuantum >= quantumRR && procesoEnCPU.getInstruccionesRestantes() > 0) {
                    // ¡Se le acabó el tiempo! Lo regresamos a la cola de listos
                    procesoEnCPU.setEstado("READY");
                    colaListos.encolar(procesoEnCPU);
                    if(gui != null) gui.imprimirLog("⏱️ [RR] Fin de Quantum (3 ciclos). Vuelve a cola: " + procesoEnCPU.getNombre());
                    procesoEnCPU = null; 
                    contadorQuantum = 0; 
                }
            }
        }

        // --- B. ASIGNAR NUEVO PROCESO AL CPU ---
        if (procesoEnCPU == null) {
            if (!colaListos.esVacia()) {
                
                // ¡AQUÍ ESTÁ LA MAGIA DE LOS ALGORITMOS!
                if (algoritmoActual.contains("EDF")) {
                    procesoEnCPU = sacarProcesoMenorDeadline(); // Busca el más urgente
                } else {
                    procesoEnCPU = colaListos.desencolar(); // FCFS y RR toman el primero en la fila
                }
                
                procesoEnCPU.setEstado("RUNNING");
                contadorQuantum = 0; // Reiniciamos el reloj de RR
                if(gui != null) gui.imprimirLog("⚙️ [" + algoritmoActual + "] CPU Ejecutando: " + procesoEnCPU.getNombre());
                
            } else {
                if (gui != null) gui.getTxtCPU().setText("[Esperando procesos...]"); 
            }
        }

        // --- C. EJECUCIÓN Y VALIDACIÓN DE LÍMITES (DEADLINE) ---
        if (procesoEnCPU != null) {
            procesoEnCPU.ejecutar();  // Trabaja 1 ciclo
            procesoEnCPU.envejecer(); // Acercándose a su deadline
            
            if (procesoEnCPU.getInstruccionesRestantes() <= 0) {
                procesoEnCPU.setEstado("TERMINATED");
                if(gui != null) gui.imprimirLog("🏁 [FIN] Proceso terminado con éxito: " + procesoEnCPU.getNombre());
                procesoEnCPU = null; 
            } else if (procesoEnCPU.getDeadline() <= 0) {
                // FALLO CRÍTICO: No logró terminar a tiempo
                procesoEnCPU.setEstado("FAILED");
                if(gui != null) gui.imprimirLog("❌ [FALLO DE MISIÓN] Deadline rebasado. Proceso abortado: " + procesoEnCPU.getNombre());
                procesoEnCPU = null;
            }
        }
        
        // -------------------------------------------------------------
        // 3. GENERADOR DINÁMICO DE PROCESOS (PASO 5)
        // -------------------------------------------------------------
        // 10% de probabilidad de que llegue una nueva tarea en cada ciclo
        if (Math.random() < 0.10) {
            int inst = (int) (Math.random() * 10) + 5;     // Entre 5 y 14 instrucciones
            int prio = (int) (Math.random() * 5) + 1;      // Prioridad del 1 al 5
            int dead = (int) (Math.random() * 50) + 30;    // Deadline entre 30 y 79
            
            PCB nuevaTarea = new PCB("Task_" + relojGlobal, inst, prio, dead);
            if (gui != null) gui.imprimirLog("📡 [NUEVA TAREA] Señal recibida desde la Tierra...");
            
            // Usamos tu método admitirProceso para que el sistema decida si va a RAM o a Disco
            admitirProceso(nuevaTarea);
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
            gui.getTxtColaSuspendidos().setText(colaListosSuspendidos.toString());
            gui.getTxtColaBloqSusp().setText(colaBloqueadosSuspendidos.toString());
        }
    }
}