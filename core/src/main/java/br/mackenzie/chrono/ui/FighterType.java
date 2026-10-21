package br.mackenzie.chrono.ui;

public enum FighterType {

    CHRONO(  "chrono",    162, 162,  85,   61,  6.0f,   true,      "idle", "attack1", "attack2", "attack3", "hit", "death"),
    MINOTAUR("minotaur",  216, 160,  147,  16,  3.2f,   false,     "idle", "attack",  null,      null,      null,  null),
    GOLEM(   "golem",     160, 160,  80,   35,  3.6f,   false,     "idle", "attack",  "attack2", null,      "hit", "death"),
    ONORHC(  "onorhc",    162, 162,  85,   61,  6.4f,   true,      "idle", "attack1", "attack2", "attack3", "hit", "death");

    final String folder;
    final int frameW;
    final int frameH;
    final int pivotX;
    final int feet;
    final float scale;
    final boolean naturalFacesRight;
    final String[] files;

    FighterType(String folder, int frameW, int frameH, int pivotX, int feet, float scale,
                boolean naturalFacesRight, String... files) {
        this.folder = folder;
        this.frameW = frameW;
        this.frameH = frameH;
        this.pivotX = pivotX;
        this.feet = feet;
        this.scale = scale;
        this.naturalFacesRight = naturalFacesRight;
        this.files = files;
    }
}