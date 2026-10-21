package br.mackenzie.chrono.input;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import java.util.concurrent.ConcurrentLinkedQueue;

// Futuramente vai ser o botão do ESP32, mas até o momento é a barra de espaço

public final class GameInput {
    private final ConcurrentLinkedQueue<Long> presses = new ConcurrentLinkedQueue<>();
    private long lastSignalNanos = Long.MIN_VALUE;

    public void update() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            presses.add(System.nanoTime());
        }
    }

    public Long poll() {
        Long t = presses.poll();
        if (t != null) lastSignalNanos = System.nanoTime();
        return t;
    }

    public void clear() {
        presses.clear();
    }

    public boolean isReady() {
        return true;
    }

    public double secondsSinceSignal() {
        return lastSignalNanos == Long.MIN_VALUE ? Double.MAX_VALUE : (System.nanoTime() - lastSignalNanos) / 1e9;
    }
}
