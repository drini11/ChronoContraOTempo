package br.mackenzie.chrono.logic;


public final class LogicDemo {
    public static void main(String[] args) {
        Phase phase = Phase.ALL[0];
        BattleModel model = new BattleModel(phase, 42);
        System.out.println(phase.name + " - " + phase.monsterName + " (vida " + phase.monsterHp + ")");
        System.out.printf("Margem do crítico nesta fase: ±%.3f s%n%n", HitGrade.CRITICAL.margin(phase.marginScale));

        double[] errors = {0.02, -0.10, 0.60, Double.NaN, 0.30, 0.0};

        for (double error : errors) {
            float target = model.nextTarget();
            double marked = Double.isNaN(error) ? Double.NaN : target + error;
            RoundResult r = model.submit(target, marked);

            String marcou = r.timedOut ? "  —  " : String.format("%.2f s", r.elapsed);
            System.out.printf("Rodada %d | alvo %.1f s | marcou %s | %-13s | dano causado %3d | dano sofrido %d"
                    + " | vida do monstro %3d | chances %d (%2d hp) | combo %d | pontos %d%n",
                r.round, r.target, marcou, r.grade.label, r.damageDealt, r.damageTaken,
                model.monsterHp(), model.player().chances(), model.player().hp(), model.combo(), model.score());

            if (model.isMonsterDefeated()) {
                System.out.println("\n>>> Monstro derrotado! Fase concluída.");
                break;
            }
            if (model.isPlayerDefeated()) {
                System.out.println("\n>>> Fim de jogo: acabaram as chances.");
                break;
            }
        }
    }
}
