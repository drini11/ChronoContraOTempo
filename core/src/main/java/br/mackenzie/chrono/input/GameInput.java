package br.mackenzie.chrono.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Fonte única do "botão" do jogo. Todas as telas leem o botão por aqui, e ninguém mais
 * precisa saber de onde ele vem. O botão de verdade é o do ESP32 (classe SerialButton);
 * a barra de espaço só funciona no modo de teste, para desenvolver sem o hardware.
 */
public final class GameInput {
    /**
     * Modo de teste: deixa a barra de espaço funcionar como botão. O projeto exige controle
     * somente pelo dispositivo IoT, então na versão entregue isto fica SEMPRE false.
     */
    public static final boolean KEYBOARD_DEBUG = false;

    /** Cada acionamento fica guardado como o instante (System.nanoTime) em que aconteceu. */
    private final ConcurrentLinkedQueue<Long> presses = new ConcurrentLinkedQueue<>();
    private final SerialButton serial = new SerialButton(presses);
    private boolean keyboardFallback = KEYBOARD_DEBUG;
    private long lastSignalNanos = Long.MIN_VALUE;

    /** Começa a procurar o ESP32 (thread em segundo plano). */
    public void start() {
        serial.start();
    }

    /** Para a leitura serial e solta a porta. */
    public void dispose() {
        serial.stop();
    }

    /** Chamado uma vez por quadro pelo ChronoGame. */
    public void update() {
        if (keyboardFallback && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            presses.add(System.nanoTime());
        }
    }

    /** Devolve o próximo acionamento pendente (o instante em que ocorreu), ou null se não há nenhum. */
    public Long poll() {
        Long t = presses.poll();
        if (t != null) lastSignalNanos = System.nanoTime();
        return t;
    }

    /** Descarta os acionamentos acumulados (usado ao trocar de tela ou de etapa). */
    public void clear() {
        presses.clear();
    }

    /** O botão está pronto para uso? Falso quando o ESP32 está desconectado. */
    public boolean isReady() {
        return keyboardFallback || serial.isConnected();
    }

    /** Segundos desde o último acionamento lido (usado pela luz de teste do menu). */
    public double secondsSinceSignal() {
        return lastSignalNanos == Long.MIN_VALUE ? Double.MAX_VALUE : (System.nanoTime() - lastSignalNanos) / 1e9;
    }

    public boolean keyboardFallback() {
        return keyboardFallback;
    }

    public SerialButton serial() {
        return serial;
    }
}
