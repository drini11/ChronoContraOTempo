/*
  Chrono Contra o Tempo - botão do ESP32

  Ligação: um terminal do push button no GPIO 4 e o outro no GND.
  (O resistor interno INPUT_PULLUP mantém o pino em HIGH; apertar o botão leva o pino a LOW.)

  Protocolo pela USB (115200 baud, uma linha por mensagem):
    "H"  batimento, a cada 500 ms: avisa ao jogo que o ESP32 está vivo
    "P"  botão pressionado (uma vez por aperto, mesmo se o botão ficar segurado)
*/

const int BUTTON_PIN = 26;                    // troque aqui se ligou em outro GPIO
const unsigned long DEBOUNCE_MS = 30;        // ignora o "tremido" do contato
const unsigned long HEARTBEAT_MS = 500;

int lastState = HIGH;                        // estado estável do botão (HIGH = solto)
unsigned long lastChangeMs = 0;
unsigned long lastBeatMs = 0;

void setup() {
  Serial.begin(115200);
  pinMode(BUTTON_PIN, INPUT_PULLUP);
#ifdef LED_BUILTIN
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, LOW);
#endif
}

void loop() {
  unsigned long now = millis();
  int state = digitalRead(BUTTON_PIN);

  // Qualquer mudança só vale se passou o tempo de debounce desde a mudança anterior.
  if (state != lastState && now - lastChangeMs >= DEBOUNCE_MS) {
    lastChangeMs = now;
    lastState = state;
    if (state == LOW) {
      Serial.println("P");                  // enviado já na primeira borda: latência mínima
    }
#ifdef LED_BUILTIN
    digitalWrite(LED_BUILTIN, state == LOW ? HIGH : LOW);   // LED acende enquanto aperta
#endif
  }

  if (now - lastBeatMs >= HEARTBEAT_MS) {
    lastBeatMs = now;
    Serial.println("H");
  }
}
