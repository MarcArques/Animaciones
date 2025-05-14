package com.project;

import com.badlogic.gdx.Game;
import com.project.GameScreen;


public class Main extends Game {
    @Override
    public void create() {
        this.setScreen(new GameScreen(this)); // pantalla inicial
    }
}
