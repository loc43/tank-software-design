package ru.mipt.bit.platformer;

import com.badlogic.gdx.Gdx;

import static com.badlogic.gdx.Input.Keys.*;

public class KeyboardInputHandler implements InputHandler {
    @Override
    public boolean isMoveUp() {
        return Gdx.input.isKeyPressed(UP) || Gdx.input.isKeyPressed(W);
    }
    @Override
    public boolean isMoveDown() {
        return Gdx.input.isKeyPressed(DOWN) || Gdx.input.isKeyPressed(S);
    }
    @Override
    public boolean isMoveLeft() {
        return Gdx.input.isKeyPressed(LEFT) || Gdx.input.isKeyPressed(A);
    }
    @Override
    public boolean isMoveRight() {
        return Gdx.input.isKeyPressed(RIGHT) || Gdx.input.isKeyPressed(D);
    }
}
