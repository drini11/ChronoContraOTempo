# Chrono Contra o Tempo

Jogo de reabilitação em que o jogador derrota monstros parando um cronômetro o mais próximo possível de um tempo-alvo. A ação é feita por um botão ligado a um ESP32.

Projeto da disciplina Jogos Digitais – Universidade Presbiteriana Mackenzie.


**🎮 O que é?**

Em Chrono Contra o Tempo você enfrenta monstros parando um cronômetro o mais perto possível de um tempo-alvo. Quanto mais preciso o aperto do botão, mais forte o golpe. Errou feio? O monstro revida.

O botão físico é ligado a um ESP32 e fica posicionado de modo que cada acionamento exija um movimento terapêutico. Ou seja: enquanto você joga, está se exercitando.


## Como funciona

1. 🎯 O tempo-alvo aparece na tela (ex.: 4,0 s).
2. ⏳ Uma contagem regressiva de 3 s começa.
3. ⏱️ O cronômetro passa a correr.
4. 🔘 Você aperta o botão quando acha que chegou no alvo.
5. 💥 O jogo mede o erro e aplica o golpe.


## ⚔️ Mecânicas principais
**Qualidade do golpe**

O dano depende do erro (diferença entre o que você marcou e o alvo). As margens são multiplicadas pela escala da fase.

Golpe	            Dano no monstro	             Margem base
💎 CRÍTICO!          	100	                      ± 0,05 s
💪 GOLPE FORTE!      	60	                      ± 0,15 s
👊 GOLPE FRACO      	30                       	± 0,30 s
❌ ERROU!            	0	                       acima disso

**Vida do jogador**
Você tem 3 chances, cada uma com 10 HP.
Errar custa HP (3, 4 ou 5, conforme a fase). Zerou o HP, perde uma chance. Acabaram as chances, fim de jogo.

**Combo e pontuação**

Acertos seguidos formam um combo: +10 pontos por acerto extra, até +50.
Errar zera o combo.
Ao vencer a fase, há um bônus de consistência de 100, 200 ou 300 pontos, conforme o desvio padrão dos seus erros (quanto mais regular, melhor).

##**🐉 As 3 fases**
Fase	Monstro	            Vida	    Tempo-alvo	   Margens	        Cronômetro	    Dano ao errar
1  	Minotauro Ancião	    200       	2 a 4 s     	×1,5 (fácil)	  👁️ Visível	        3
2 	Golem Steampunk	      300	        4 a 8 s	      ×1,0	🌫️        Some depois       	4
3	  Onorhc	              500	       6 a 12 s	      ×0,8 (apertado)	 🙈 Escondido	    5

A dificuldade sobe com alvos maiores, margens menores e o cronômetro desaparecendo. Na fase 3 é só você e a sua noção de tempo.

##**🕹️ Controles**
Onde       	             Controle
Durante a partida	       Botão do ESP32 (por enquanto: barra de espaço, veja o status)
Menus                  	↑ ↓ para navegar, Enter para confirmar
Pausar	                Esc (Continuar, Som ou Sair para o menu)
Tela final	            Enter ou botão para continuar, M ou Esc para o menu

##**🧠 Reabilitação**

Além do exercício físico a cada aperto, o jogo trabalha:

Estimativa de tempo (noção temporal)
Atenção sustentada
Controle de impulso (esperar o momento certo, sem apertar antes)

##**📊 Registro de dados**

Cada sessão é gravada em CSV (separador ;, decimais com vírgula, pronto para abrir no Excel) em:
~/ChronoContraOTempo/dados/
├── rodadas.csv   # uma linha por rodada: alvo, tempo marcado, erro, golpe, combo, vida...
└── fases.csv     # uma linha por fase: taxa de acerto, erro médio, desvio padrão, pontos, duração...

Isso permite acompanhar a evolução do jogador entre sessões (erro médio, regularidade, taxa de acerto).

##**🚀 Como rodar**

Requisitos: JDK 21 ou superior.

clonar
git clone https://github.com/drini11/ChronoContraOTempo.git
cd ChronoContraOTempo

rodar o jogo
./gradlew lwjgl3:run          # Linux / macOS
gradlew.bat lwjgl3:run        # Windows

Gerar um JAR executável
./gradlew lwjgl3:jar
java -jar lwjgl3/build/libs/ChronoContraOTempo-1.0.0.jar

Também existem jarWin, jarLinux e jarMac para JARs menores, específicos de cada sistema.

Demo da lógica (sem interface)
Para ver o motor de combate funcionando só no console:

./gradlew core:classes
depois execute br.mackenzie.chrono.logic.LogicDemo pela sua IDE

##**🗂️ Estrutura do projeto**

ChronoContraOTempo/
├── assets/                 # sprites, cenários, músicas, efeitos e fonte
├── core/                   # o jogo em si (independente de plataforma)
│   └── src/main/java/br/mackenzie/chrono/
│       ├── ChronoGame.java # ponto de entrada do libGDX
│       ├── logic/          # regras: BattleModel, Phase, HitGrade, PhaseStats...
│       ├── screens/        # MenuScreen, GameScreen, EndScreen
│       ├── input/          # GameInput (botão / ESP32)
│       ├── audio/          # GameAudio
│       ├── data/           # SessionLogger (CSV)
│       └── ui/             # sprites, desenho e fontes
└── lwjgl3/                 # launcher desktop (janela 1280×720)
A lógica de combate fica separada da interface (logic/ não depende de gráficos), o que facilita testar e evoluir as regras.

##**🛠️ Tecnologias**
Java 21
libGDX 1.14.2 (LWJGL3 + FreeType)
Gradle
ESP32 (botão físico)

##**🚧 Status do projeto**
✅ 3 fases completas com monstros, músicas e efeitos sonoros
✅ Sistema de golpes, combo, vida e bônus de consistência
✅ Registro de sessões em CSV
🔧 Integração com o ESP32: a classe GameInput já isola a entrada do jogador. Hoje ela lê a barra de espaço como substituta do botão; a leitura do sinal do ESP32 entra no lugar dela.

##**🎨 Créditos**
Chrono / Onorhc (sprites): pack "Fantasy Warrior", licença CC0
Fonte: DejaVu Sans Bold (licença livre, ver assets/fonts/LICENSE-DejaVu.txt)
Músicas: Nero's Day At Disneyland
Demais créditos de sprites, cenários e efeitos sonoros em assets/CREDITS.txt

##**👥 Equipe**
	
João Pedro Pizarro Buldrini	
João Vitor Almeida	
Rodrigo Oliveira Barbosa
