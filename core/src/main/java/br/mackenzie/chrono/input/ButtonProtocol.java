package br.mackenzie.chrono.input;

/**
 * Entende o que o ESP32 manda pela USB. O protocolo é de linhas de texto:
 *   "H"  = batimento (o ESP32 está vivo), enviado a cada 0,5 s;
 *   "P"  = botão pressionado.
 * Qualquer outra linha (como o texto de boot do ESP32) é ignorada. Só passamos a aceitar "P"
 * depois do primeiro "H", para o lixo do boot nunca virar um acionamento falso.
 */
final class ButtonProtocol {
    interface Listener {
        /** @param nano instante (System.nanoTime) em que o primeiro byte da linha "P" chegou. */
        void onPress(long nano);

        void onHeartbeat(long nano);
    }

    private static final int MAX_LINE = 16;

    private final Listener listener;
    private final StringBuilder line = new StringBuilder();
    private long lineStartNano;
    private boolean armed;

    ButtonProtocol(Listener listener) {
        this.listener = listener;
    }

    /** Já recebeu o primeiro batimento (o botão está pronto)? */
    boolean isArmed() {
        return armed;
    }

    /**
     * @param buf  bytes lidos da porta
     * @param len  quantos bytes de buf são válidos
     * @param nano instante em que a leitura terminou (System.nanoTime)
     */
    void feed(byte[] buf, int len, long nano) {
        for (int i = 0; i < len; i++) {
            char c = (char) (buf[i] & 0xFF);
            if (c == '\n') {
                handleLine(line.toString());
                line.setLength(0);
            } else if (c != '\r') {
                if (line.length() == 0) lineStartNano = nano;
                if (line.length() < MAX_LINE) line.append(c);
                else line.append('?'); // linha longa demais: certamente não é nossa
            }
        }
    }

    private void handleLine(String s) {
        if (s.equals("H")) {
            armed = true;
            listener.onHeartbeat(lineStartNano);
        } else if (s.equals("P") && armed) {
            listener.onPress(lineStartNano);
        }
    }
}
