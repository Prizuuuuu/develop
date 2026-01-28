package clases;

public class PCB {
    // Generador de IDs únicos (autoincremental)
    private static int contadorGlobal = 0;
    
    // Atributos requeridos por el PDF
    private int id;
    private String nombre; // Ej: "P_Telemetry"
    private String estado; // Nuevo, Listo, Ejecucion, Bloqueado, Terminado
    
    // Registros simulados (se incrementan linealmente según PDF)
    private int programCounter; // PC
    private int mar; // Memory Address Register
    
    // Planificación
    private int prioridad; // 1 es alta, 3 es baja (ejemplo)
    private int deadline; // Tiempo límite en ciclos
    
    // Control de ejecución
    private int instruccionesTotales;
    private int instruccionesEjecutadas; // Para saber cuándo termina
    private int cicloLlegada; // Para métricas de espera

    // Constructor
    public PCB(String nombre, int prioridad, int instruccionesTotales, int deadline, int cicloLlegada) {
        this.id = ++contadorGlobal;
        this.nombre = nombre;
        this.prioridad = prioridad;
        this.instruccionesTotales = instruccionesTotales;
        this.deadline = deadline;
        this.cicloLlegada = cicloLlegada;
        
        // Valores iniciales
        this.estado = "Nuevo";
        this.programCounter = 0;
        this.mar = 0;
        this.instruccionesEjecutadas = 0;
    }

    // --- Lógica del Sistema ---
    
    // Simula la ejecución de 1 instrucción (1 ciclo)
    public void ejecutar() {
        this.programCounter++;
        this.mar++;
        this.instruccionesEjecutadas++;
    }
    
    // Verifica si ya terminó
    public boolean haTerminado() {
        return instruccionesEjecutadas >= instruccionesTotales;
    }

    // --- Getters y Setters necesarios para la GUI ---
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public int getPrioridad() { return prioridad; }
    public int getProgramCounter() { return programCounter; }
    public int getMar() { return mar; }
    public int getDeadline() { return deadline; }
    public void setDeadline(int deadline) { this.deadline = deadline; } // Algunos algoritmos lo modifican
}