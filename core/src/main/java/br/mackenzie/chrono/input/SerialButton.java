package br.mackenzie.chrono.input;

import com.fazecast.jSerialComm.SerialPort;

import java.util.Queue;

/**
 * O botão do ESP32. Uma thread própria procura a porta USB do ESP32, lê o que ele manda e,
 * a cada "P", coloca na fila o instante (System.nanoTime) do acionamento. A fila é a mesma
 * que a barra de espaço usava: o resto do jogo não sabe de onde o botão vem.
 * Se o cabo sair, a thread percebe e volta a procurar sozinha.
 */
public final class SerialButton {
    private static final int BAUD = 115200;
    /** Tempo esperando o primeiro batimento de uma porta (o ESP32 reinicia ao abrir a porta). */
    private static final long PROBE_MS = 3500;
    /** Sem batimento por este tempo, consideramos o botão desconectado. */
    private static final long HEARTBEAT_TIMEOUT_MS = 2500;
    private static final long RETRY_MS = 1000;

    private final Queue<Long> presses;
    private Thread thread;
    private volatile boolean running;

    private volatile String preferredPort;       // null = procurar em todas as portas
    private volatile boolean reconnectRequested;
    private volatile String connectedPort;       // null = desconectado
    private volatile long lastHeartbeatNano;
    private volatile String lastError = "";
    /** Protocolo da conexão atual (já "armado" pelo probe, guarda também linhas pela metade). */
    private ButtonProtocol activeProto;

    public SerialButton(Queue<Long> presses) {
        this.presses = presses;
    }

    public void start() {
        if (running) return;
        running = true;
        thread = new Thread(this::run, "serial-button");
        thread.setDaemon(true); // não impede o jogo de fechar
        thread.start();
    }

    public void stop() {
        running = false;
        if (thread != null) thread.interrupt();
    }

    /** O ESP32 está conectado e mandando batimentos? */
    public boolean isConnected() {
        return connectedPort != null
            && (System.nanoTime() - lastHeartbeatNano) / 1_000_000L < HEARTBEAT_TIMEOUT_MS;
    }

    public String connectedPort() {
        return connectedPort;
    }

    public String lastError() {
        return lastError;
    }

    /** Fixa uma porta (ex.: "COM3") ou null para procurar automaticamente. Reconecta na hora. */
    public void setPreferredPort(String name) {
        this.preferredPort = name;
        reconnect();
    }

    public String preferredPort() {
        return preferredPort;
    }

    /** Força o fechamento da porta atual e uma nova procura. */
    public void reconnect() {
        reconnectRequested = true;
    }

    /** Nomes das portas seriais que o sistema enxerga agora. */
    public static String[] listPortNames() {
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] names = new String[ports.length];
        for (int i = 0; i < ports.length; i++) names[i] = ports[i].getSystemPortName();
        return names;
    }

    /** Texto para mostrar no menu. */
    public String status() {
        if (isConnected()) return "OK em " + connectedPort;
        if (preferredPort != null) return "procurando " + preferredPort + "...";
        return "procurando...";
    }

    // ---------------------------------------------------------------------------------------

    private void run() {
        while (running) {
            try {
                reconnectRequested = false;
                SerialPort port = findPort();
                if (port != null) {
                    readLoop(port);
                } else {
                    sleep(RETRY_MS);
                }
            } catch (InterruptedException e) {
                return;
            } catch (RuntimeException e) {
                lastError = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                System.err.println("[SerialButton] " + lastError);
                try { sleep(RETRY_MS); } catch (InterruptedException ie) { return; }
            }
        }
    }

    /** Abre as portas candidatas, uma por vez, até achar a que manda os batimentos do Chrono. */
    private SerialPort findPort() throws InterruptedException {
        String wanted = preferredPort;
        for (SerialPort p : SerialPort.getCommPorts()) {
            if (!running || reconnectRequested) return null;
            if (wanted != null && !wanted.equalsIgnoreCase(p.getSystemPortName())) continue;
            if (wanted == null && looksUnrelated(p)) continue; // pula, por ex., portas Bluetooth

            if (probe(p)) return p;
        }
        return null;
    }

    private static boolean looksUnrelated(SerialPort p) {
        String d = String.valueOf(p.getDescriptivePortName()).toLowerCase();
        return d.contains("bluetooth");
    }

    /** Abre a porta e espera um batimento. Se vier, a porta fica aberta (e já com o protocolo "armado"). */
    private boolean probe(SerialPort p) throws InterruptedException {
        if (!openConfigured(p)) return false;
        ButtonProtocol proto = new ButtonProtocol(new Sink());
        byte[] buf = new byte[64];
        long deadline = System.nanoTime() + PROBE_MS * 1_000_000L;
        while (running && !reconnectRequested && System.nanoTime() < deadline) {
            int n = p.readBytes(buf, buf.length);
            if (n < 0) break;
            if (n > 0) proto.feed(buf, n, System.nanoTime());
            if (proto.isArmed()) {
                activeProto = proto;
                connectedPort = p.getSystemPortName();
                lastHeartbeatNano = System.nanoTime();
                return true;
            }
        }
        p.closePort();
        return false;
    }

    private boolean openConfigured(SerialPort p) {
        try {
            p.setBaudRate(BAUD);
            p.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
            return p.openPort();
        } catch (RuntimeException e) {
            lastError = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            return false;
        }
    }

    /** Lê a porta até o cabo sair, o ESP32 parar de mandar batimentos ou pedirem para reconectar. */
    private void readLoop(SerialPort p) throws InterruptedException {
        ButtonProtocol proto = activeProto;
        byte[] buf = new byte[64];
        try {
            while (running && !reconnectRequested) {
                int n = p.readBytes(buf, buf.length);
                long now = System.nanoTime(); // o instante vale desde o retorno da leitura
                if (n < 0 || !p.isOpen()) break; // cabo desconectado
                if (n > 0) proto.feed(buf, n, now);
                if ((now - lastHeartbeatNano) / 1_000_000L > HEARTBEAT_TIMEOUT_MS) break; // ESP32 mudo
            }
        } finally {
            connectedPort = null;
            p.closePort();
            presses.clear(); // acionamentos antigos não valem depois de uma queda
        }
    }

    private static void sleep(long ms) throws InterruptedException {
        Thread.sleep(ms);
    }

    /** Recebe os eventos do protocolo: o "P" vai para a fila do jogo, o "H" renova o batimento. */
    private final class Sink implements ButtonProtocol.Listener {
        @Override
        public void onPress(long nano) {
            presses.add(nano);
        }

        @Override
        public void onHeartbeat(long nano) {
            lastHeartbeatNano = System.nanoTime();
        }
    }
}
