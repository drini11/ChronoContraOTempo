package br.mackenzie.chrono.screens;

import br.mackenzie.chrono.ChronoGame;
import br.mackenzie.chrono.ui.Assets;
import br.mackenzie.chrono.ui.Draw;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import br.mackenzie.chrono.logic.RunSession;

public final class MenuScreen extends BaseScreen {
    private static final int START = 0, HELP = 1, SOUND = 2;
    private static final String[] LABELS = {"Iniciar jogo", "Como jogar", "Som", "Sair"};

    private static final String HELP_TEXT =
        "1. Um tempo-alvo aparece na tela (ex.: 4,0 s). Memorize!\n"
            + "2. O alvo some e uma contagem de 3 segundos começa.\n"
            + "3. O cronômetro começa a correr. Acione o botão quando achar que chegou no tempo-alvo.\n"
            + "4. Quanto mais perto do alvo, mais forte o golpe: crítico, forte ou fraco. "
            + "Errar custa pontos de vida.\n"
            + "5. Você tem 3 chances de 10 pontos de vida. Derrote o monstro antes que elas acabem.\n\n"
            + "Fase 1: cronômetro sempre visível.\n"
            + "Fase 2: o cronômetro some no meio do caminho.\n"
            + "Fase 3: cronômetro oculto. Confie no seu ritmo interno!\n\n"
            + "Durante o jogo, apenas o botão controla a ação. ESC pausa a partida.";

    private final Texture background;
    private int selected;
    private boolean showHelp;
    private String message = "";



    public MenuScreen(ChronoGame game) {
        super(game);
        background = game.assets.texture("backgrounds/bg_menu.jpg", false);

    }

    @Override
    public void show() {
        game.input.clear();
        game.audio.playMusic("music/menu.mp3");
    }

    @Override
    public void render(float delta) {
        while (game.input.poll() != null) {
        }
        handleInput();
        draw();
    }

    private void handleInput() {
        if (showHelp) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) showHelp = false;
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) selected = (selected + LABELS.length - 1) % LABELS.length;
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) selected = (selected + 1) % LABELS.length;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            switch (selected) {
                case START:
                    game.setScreen(new GameScreen(game, new RunSession(), 0));
                    break;
                case HELP:
                    showHelp = true;
                    break;
                case SOUND:
                    game.audio.setMuted(!game.audio.isMuted());
                    if (!game.audio.isMuted()) game.audio.playMusic("music/menu.mp3");
                    break;
                default:
                    Gdx.app.exit();
                    break;
            }
        }
    }

    private static float itemTop(int i) {
        return 420f - i * 70f;
    }

    private void draw() {
        beginFrame();
        SpriteBatch batch = game.batch;
        ShapeRenderer shapes = game.shapes;

        // 1) fundo escurecido
        batch.begin();
        batch.setColor(0.5f, 0.5f, 0.5f, 1f);
        batch.draw(background, 0f, 0f, Assets.W, Assets.H);
        batch.setColor(Color.WHITE);
        batch.end();

        // 2) destaque do item selecionado e luz do botão
        boolean flash = game.input.secondsSinceSignal() < 0.4;
        beginShapes();
        if (!showHelp) {
            shapes.setColor(1f, 1f, 1f, 0.14f);
            shapes.rect(400f, itemTop(selected) - 50f, 480f, 58f);
        }
        if (flash) shapes.setColor(1f, 1f, 1f, 1f);
        else if (game.input.isReady()) shapes.setColor(0.3f, 0.85f, 0.4f, 1f);
        else shapes.setColor(0.9f, 0.3f, 0.3f, 1f);
        shapes.circle(330f, 100f, flash ? 20f : 14f);
        shapes.end();

        // 3) textos
        batch.begin();
        Draw.centered(batch, game.assets.large, "CHRONO CONTRA O TEMPO", Assets.W / 2f, 640f, Draw.GOLD);
        Draw.centered(batch, game.assets.small, "Jogo de reabilitação com precisão de tempo",
            Assets.W / 2f, 560f, Draw.GRAY);
        for (int i = 0; i < LABELS.length; i++) {
            Color c = (i == selected && !showHelp) ? Draw.GOLD : Draw.WHITE;
            String label = LABELS[i];
            if (i == SOUND) label = game.audio.isMuted() ? "Som: desligado" : "Som: ligado";
            Draw.centered(batch, game.assets.medium, label, Assets.W / 2f, itemTop(i), c);
        }
        Draw.left(batch, game.assets.small,
            flash ? "Botão acionado!" : "Pressione a barra de espaço para testar o botão",
            370f, 112f, flash ? Draw.GOLD : Draw.WHITE);
        if (!message.isEmpty()) {
            Draw.centered(batch, game.assets.small, message, Assets.W / 2f, 170f, Draw.RED);
        }
        Draw.centered(batch, game.assets.small, "↑↓ navegar  ·  ENTER selecionar", Assets.W / 2f, 50f, Draw.GRAY);
        batch.end();

        if (showHelp) drawHelp();
    }

    private void drawHelp() {
        SpriteBatch batch = game.batch;
        beginShapes();
        game.shapes.setColor(0f, 0f, 0f, 0.88f);
        game.shapes.rect(140f, 60f, 1000f, 600f);
        game.shapes.setColor(Draw.GOLD);
        game.shapes.rect(140f, 656f, 1000f, 4f);
        game.shapes.end();

        batch.begin();
        Draw.centered(batch, game.assets.large, "COMO JOGAR", Assets.W / 2f, 650f, Draw.GOLD);
        game.assets.small.setColor(Color.WHITE);
        game.assets.small.draw(batch, HELP_TEXT, 180f, 560f, 920f, Align.left, true);
        Draw.centered(batch, game.assets.small, "Pressione qualquer tecla para voltar", Assets.W / 2f, 110f, Draw.GRAY);
        batch.end();
    }
}
