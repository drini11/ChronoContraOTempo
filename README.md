# Chrono Contra o Tempo

Jogo de reabilitação em que o jogador derrota monstros parando um cronômetro o mais próximo possível de um tempo-alvo. A ação é feita por um botão ligado a um ESP32.

Projeto da disciplina Jogos Digitais – Universidade Presbiteriana Mackenzie.

## Como funciona

1. Um tempo-alvo (ex.: 4,0 s) aparece na tela.
2. Uma contagem regressiva de 3 segundos começa.
3. Um cronômetro passa a correr.
4. O jogador aperta o botão quando acha que chegou ao tempo-alvo.
5. Quanto mais perto do tempo-alvo, maior o dano no monstro.

## Mecânicas principais

- **Dano por precisão:** quanto menor o erro, mais forte o golpe. Se errar por muito, o jogador perde vida.
- **3 fases:** cada uma com um monstro diferente. A dificuldade aumenta com tempos-alvo maiores, margens menores e cronômetro oculto.
- **Controle:** durante a partida, só o botão do ESP32. Teclado e mouse são usados apenas nos menus.

## Reabilitação

O botão é posicionado para exigir um movimento terapêutico a cada acionamento, trabalhando a parte física. O jogo também exercita estimativa de tempo, atenção e controle de impulso.

## Tecnologias

- libGDX (Java)
- ESP32

## Equipe

- João Pedro Pizarro Buldrini
- João Vitor Almeida
- Rodrigo Oliveira Barbosa