package com.project;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.project.clases.Joystick;


public class GameScreen implements Screen {
    private final Game game;
    private SpriteBatch batch;
    private SpriteBatch uiBatch;
    private ShapeRenderer shapeRenderer;
    private ShapeRenderer uiShapeRenderer;
    private BitmapFont font, titleFont;
    private Texture backgroundTexture;

    private OrthographicCamera camera;

    private float playerX, playerY;
    private Vector2 movementOutput;

    private Joystick joystick;

    private Texture torchRed;
    private TextureRegion[][] torchFrames;

    private float animationTimer = 0f;
    private float frameDuration = 0.1f; // 10 fps


    public GameScreen(Game game) {
        this.game = game;

        movementOutput = new Vector2();
        playerX = 500;
        playerY = 400;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1000, 800);

        batch = new SpriteBatch(); // para el mundo
        uiBatch = new SpriteBatch(); // para la UI

        shapeRenderer = new ShapeRenderer(); // para mundo
        uiShapeRenderer = new ShapeRenderer(); // para UI

        font = new BitmapFont();
        titleFont = new BitmapFont();

        joystick = new Joystick(175, 175, 75);

        initTextures();
    }

    private void initTextures() {
        torchRed = new Texture("Amongus.png");
        backgroundTexture = new Texture("background.png"); 
        torchFrames = extractFrames(torchRed, 200, 200, 4, 4);    }

    private TextureRegion[][] extractFrames(Texture sheet, int frameWidth, int frameHeight, int totalRows, int framesPerRow) {
        TextureRegion[][] allFrames = new TextureRegion[totalRows][framesPerRow];

        int offsetY = 20; // cuanto cortar desde arriba (ajusta tú este valor)
        int visibleHeight = 200; // altura útil del personaje dentro del frame

        for (int row = 0; row < totalRows; row++) {
            for (int col = 0; col < framesPerRow; col++) {
                int x = col * frameWidth;
                int y = row * frameHeight + offsetY;
                allFrames[row][col] = new TextureRegion(sheet, x, y, frameWidth, visibleHeight);
            }
        }

        return allFrames;
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        animationTimer += delta;
        ScreenUtils.clear(0.18f, 0.506f, 0.2f, 1f);

        // Movimiento del jugador
        float speed = 200f;
        playerX += movementOutput.x * speed * delta;
        playerY += movementOutput.y * speed * delta;

        // Cámara sigue al jugador
        camera.position.set(playerX, playerY, 0);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        drawMap();
        drawLocalPlayer();

        // UI fija (sin cámara)
        uiShapeRenderer.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        uiBatch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

        // Dibujar joystick
        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        joystick.draw(uiShapeRenderer);
        uiShapeRenderer.end();

        // Actualizar movimiento del joystick
        Vector2 touchPosition = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        movementOutput = joystick.update(touchPosition);
    }

    private void drawMap() {
        batch.begin();

        int textureWidth = backgroundTexture.getWidth();
        int textureHeight = backgroundTexture.getHeight();

        // Calcula en quina "rajola" estàs (és a dir, quina còpia de la textura mostra la càmera)
        int startX = (int) ((camera.position.x - 500) / textureWidth) - 1;
        int startY = (int) ((camera.position.y - 400) / textureHeight) - 1;

        // Quants tiles calen per cobrir l'àrea de 1000x800?
        int tilesX = 4;
        int tilesY = 4;

        for (int x = startX; x < startX + tilesX; x++) {
            for (int y = startY; y < startY + tilesY; y++) {
                float drawX = x * textureWidth;
                float drawY = y * textureHeight;
                float scaleFactor = 6f;
                batch.draw(backgroundTexture, drawX, drawY, backgroundTexture.getWidth() * scaleFactor, backgroundTexture.getHeight() * scaleFactor);
            }
        }

        batch.end();
    }


    private void drawLocalPlayer() {
        batch.begin();
    
        int directionIndex = 0; // 0: frente, 1: atrás, 2: derecha, 3: izquierda
    
        if (movementOutput.isZero(0.1f)) {
            // Idle
            if (Math.abs(movementOutput.y) > Math.abs(movementOutput.x)) {
                directionIndex = movementOutput.y > 0 ? 1 : 0; // arriba o abajo
            } else {
                directionIndex = movementOutput.x > 0 ? 2 : 3; // derecha o izquierda
            }
            // Pero si está completamente quieto, por defecto mirará al frente
        } else {
            // Caminando
            if (Math.abs(movementOutput.y) > Math.abs(movementOutput.x)) {
                directionIndex = movementOutput.y > 0 ? 1 : 0;
            } else {
                directionIndex = movementOutput.x > 0 ? 2 : 3;
            }
        }
    
        int frameRow = movementOutput.isZero(0.1f) ? 0 : ((int)(animationTimer / frameDuration) % 2) + 1;
        TextureRegion frame = torchFrames[frameRow][directionIndex];
    
        float scale = 1f;
        float width = frame.getRegionWidth() * scale;
        float height = frame.getRegionHeight() * scale;
        float drawX = playerX - (frame.getRegionWidth() / 2f);
        float drawY = playerY - 10f;

        batch.draw(frame, drawX, drawY, width, height);
    
        batch.end();
    }
    



    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        uiBatch.dispose();
        shapeRenderer.dispose();
        uiShapeRenderer.dispose();
        font.dispose();
        titleFont.dispose();
        backgroundTexture.dispose();
        torchRed.dispose();
    }
}
