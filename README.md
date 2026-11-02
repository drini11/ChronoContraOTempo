# Chrono Contra o Tempo

Jogo de reabilitação em que o jogador derrota monstros parando um cronômetro o mais perto possível de um tempo-alvo. A ação é feita por um **botão físico ligado a um ESP32**, o que exige um movimento terapêutico a cada acionamento.

Projeto da disciplina Jogos Digitais – Universidade Presbiteriana Mackenzie.

## Como funciona

1. Um tempo-alvo (ex.: 4,0 s) aparece na tela.
2. O alvo some e uma contagem regressiva de 3 segundos começa.
3. O cronômetro passa a correr.
4. O jogador aciona o botão quando acha que chegou ao tempo-alvo.
5. Quanto mais perto do alvo, mais forte o golpe no monstro. Se errar, o jogador perde vida.

## Regras

| Classificação | Erro (Fase 2)   | Dano no monstro          |
|---------------|-----------------|--------------------------|
| Crítico       | até 0,05 s      | 100                      |
| Forte         | até 0,15 s      | 60                       |
| Fraco         | até 0,30 s      | 30                       |
| Erro          | acima de 0,30 s | 0 (o jogador perde vida) |

As margens mudam por fase: são 50% maiores na Fase 1 e 20% menores na Fase 3.

- O jogador tem **3 chances de 10 pontos de vida**.
- Acertos seguidos formam **combos**, que dão pontos extras.
- Se o botão não for acionado até 2 s depois do alvo, a rodada conta como erro.
- Acionar o botão antes de o cronômetro começar não conta: aparece só um aviso ("Calma!"). Isso treina o controle de impulso.
- Vencer a fase com erros parecidos entre si (desvio padrão baixo) dá **bônus de consistência**.

## Fases

| Fase | Monstro          | Vida | Tempos-alvo | Cronômetro              | Dano do erro |
|------|------------------|------|-------------|-------------------------|--------------|
| 1    | Minotauro Ancião | 200  | 2 a 4 s     | sempre visível          | 3            |
| 2    | Golem Steampunk  | 300  | 4 a 8 s     | some no meio do caminho | 4            |
| 3    | Onorhc           | 500  | 6 a 12 s    | oculto                  | 5            |

## Controles

- **Durante a partida:** somente o botão do ESP32.
- **Menus:** ↑ ↓ para navegar e ENTER para selecionar.
- **ESC:** pausa a partida. Se a pausa acontecer durante a contagem ou com o cronômetro correndo, a rodada recomeça com o mesmo alvo e não conta como erro.
- **Cabo desconectado:** o jogo pausa sozinho e só continua quando o botão voltar.

## Hardware

### Materiais

- 1 placa **ESP32 DevKit V1** (ESP32-WROOM-32)
- 1 push button
- 1 protoboard e 2 jumpers
- 1 cabo USB **de dados**

### Ligação

Um terminal do botão no **GPIO 4** e o outro no **GND**. Não precisa de resistor: o firmware usa o pull-up interno do ESP32.

```
ESP32 GPIO 4 ──── [ botão ] ──── GND
```

### Firmware

O código está em [`firmware/chrono_button/chrono_button.ino`](firmware/chrono_button/chrono_button.ino).

1. Na Arduino IDE, instale a placa **esp32 (Espressif Systems)** pelo Gerenciador de Placas.
2. Selecione **ESP32 Dev Module** e a porta do ESP32 (no Windows, uma `COMx` nova ao plugar a placa).
3. Grave o sketch. Se ficar travado em `Connecting......`, segure o botão BOOT da placa durante o upload.
4. Para conferir, abra o Serial Monitor em **115200**: deve aparecer um `H` a cada 0,5 s e um `P` a cada aperto. Feche o Serial Monitor antes de abrir o jogo, porque ele ocupa a porta.

Protocolo usado pela USB (uma linha por mensagem):

| Mensagem | Significado                                          |
|----------|------------------------------------------------------|
| `H`      | batimento a cada 500 ms: o ESP32 está vivo           |
| `P`      | botão pressionado (uma vez por aperto, com debounce de 30 ms) |

O jogo procura a porta sozinho e reconecta se o cabo sair. No menu, o item **Botão** mostra o status e permite fixar uma porta (← →).

## Como executar

### Executável (Windows)

Baixe o `.zip` da última versão em [Releases](../../releases), extraia e abra `ChronoContraOTempo.exe`. O Java já vem embutido. O Windows pode mostrar o aviso do SmartScreen porque o programa não é assinado: use "Mais informações → Executar assim mesmo".

Ligue o ESP32 antes de abrir o jogo (ou durante o menu).

### A partir do código

Requisitos: **JDK 21** ou superior.

```bash
./gradlew lwjgl3:run      # roda o jogo
./gradlew lwjgl3:jar      # gera o .jar em lwjgl3/build/libs
```

No Windows, use `gradlew.bat` no lugar de `./gradlew`.

Para gerar uma pasta com `.exe` e Java embutido (JDK 21):

```bash
jpackage --type app-image --name ChronoContraOTempo --input lwjgl3/build/libs --main-jar ChronoContraOTempo-<versão>.jar --dest dist
```

Para desenvolver sem o ESP32 por perto, mude `KEYBOARD_DEBUG` para `true` em `GameInput.java`: a barra de espaço passa a valer como botão. Deixe sempre `false` na versão entregue, porque o jogo deve ser controlado somente pelo dispositivo IoT.

### Problemas comuns

- **O menu fica em "procurando...":** feche o Serial Monitor, use um cabo USB de dados e confira se o firmware está gravado (o `H` deve aparecer no Serial Monitor).
- **Nenhuma porta COM aparece no Windows:** troque o cabo; se continuar, instale o driver **CP210x** (Silicon Labs) ou **CH340**, conforme o chip da sua placa.
- **Linux:** a porta precisa de permissão: `sudo usermod -aG dialout $USER` e depois sair e entrar na sessão.

## Métricas salvas

A cada partida, o jogo grava dois arquivos CSV em `ChronoContraOTempo/dados`, dentro da pasta do usuário (no Windows, `C:\Users\<nome>\ChronoContraOTempo\dados`):

- `rodadas.csv`: uma linha por rodada, com alvo, tempo marcado, erro, classificação, dano, combo, vida e chances.
- `fases.csv`: uma linha por fase, com resultado (vitória, derrota ou abandono), taxa de acerto, erro médio, desvio padrão, melhor erro, pontos e duração.

Os arquivos usam `;` como separador e vírgula decimal, e abrem direto no Excel. Com eles dá para acompanhar a evolução do jogador ao longo das sessões.

## Reabilitação

- **Física:** o botão é posicionado para exigir um movimento terapêutico a cada acionamento (ex.: extensão de punho ou preensão).
- **Cognitiva:** o jogo trabalha estimativa de tempo, atenção sustentada, memória de curto prazo (o alvo some da tela) e controle de impulso.
- **Acompanhamento:** precisão (erro médio) e consistência (desvio padrão) ficam registradas em cada sessão.

## Estrutura do repositório

```
├── assets/                      imagens, sprites, músicas, efeitos e fontes
├── core/src/main/java/br/mackenzie/chrono/
│   ├── ChronoGame.java          ponto de entrada; recursos compartilhados
│   ├── logic/                   regras do jogo (Java puro): fases, dano, vida, combos, métricas
│   ├── input/                   GameInput, SerialButton: leitura do botão do ESP32
│   ├── data/                    SessionLogger: gravação das métricas em CSV
│   ├── audio/                   GameAudio: músicas e efeitos sonoros
│   ├── ui/                      assets, fontes, desenho, lutadores animados
│   └── screens/                 menu, batalha, resultados
├── lwjgl3/                      lançador desktop
└── firmware/chrono_button/      firmware do ESP32 (Arduino)
```

## Tecnologias

- Java 21 e [libGDX](https://libgdx.com)
- [jSerialComm](https://github.com/Fazecast/jSerialComm) para a comunicação serial
- ESP32 (Arduino framework)

## Créditos

Veja [`assets/CREDITS.txt`](assets/CREDITS.txt).

## Equipe

- João Pedro Pizarro Buldrini
- João Vitor Almeida
- Rodrigo Oliveira Barbosa
