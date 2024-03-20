package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Random;

import mino.Block;
import mino.Mino;
import mino.Mino_L1;
import mino.Mino_L2;
import mino.Mino_Square;
import mino.Mino_Bar;
import mino.Mino_T;
import mino.Mino_Z1;
import mino.Mino_Z2;

public class PlayManager {

    // game window settings
    final int WIDTH = 360;
    final int HEIGHT = 600;
    public static int left_x;
    public static int right_x;
    public static int top_y;
    public static int bottom_y;

    // mino
    Mino currentMino;
    final int MINO_START_X;
    final int MINO_START_Y;
    Mino nextMino;
    final int NEXT_MINO_X;
    final int NEXT_MINO_Y;
    public static ArrayList<Block> staticBlocks = new ArrayList<>();

    // others
    public static int dropInterval = 60; // mino piece drops every 60 frames
    boolean gameOver;

    // graphic effects
    boolean effectCounterOn;
    int effectCounter;
    ArrayList<Integer> effectY = new ArrayList<>();

    // score
    int level = 1;
    int rows;
    int score;

    public PlayManager() {

        // game window frame
        left_x = (GamePanel.WIDTH / 2) - (WIDTH / 2); // 1280/2 - 360/2 = 460
        right_x = left_x + WIDTH;
        top_y = 50;
        bottom_y = top_y + HEIGHT;

        MINO_START_X = left_x + (WIDTH / 2) - Block.SIZE;
        MINO_START_Y = top_y + Block.SIZE;

        NEXT_MINO_X = right_x + 175;
        NEXT_MINO_Y = top_y + 500;

        // set starting mino piece
        currentMino = pickMino();
        currentMino.setXY(MINO_START_X, MINO_START_Y);
        nextMino = pickMino();
        nextMino.setXY(NEXT_MINO_X, NEXT_MINO_Y);
    }

    private Mino pickMino() {

        // pick random mino piece
        Mino mino = null;
        int i = new Random().nextInt(7);

        switch (i) {
            case 0:
                mino = new Mino_L1();
                break;
            case 1:
                mino = new Mino_L2();
                break;
            case 2:
                mino = new Mino_Square();
                break;
            case 3:
                mino = new Mino_Bar();
                break;
            case 4:
                mino = new Mino_T();
                break;
            case 5:
                mino = new Mino_Z1();
                break;
            case 6:
                mino = new Mino_Z2();
                break;
        }
        return mino;
    }

    public void update() {

        // check if currentMino is active
        if (currentMino.active == false) {

            // store inactive mino pieces in staticBlocks
            staticBlocks.add(currentMino.b[0]);
            staticBlocks.add(currentMino.b[1]);
            staticBlocks.add(currentMino.b[2]);
            staticBlocks.add(currentMino.b[3]);

            // check for gameOver
            if (currentMino.b[0].x == MINO_START_X && currentMino.b[0].y == MINO_START_Y) {
                // if currentMino collides immediately after spawning, game is over
                gameOver = true;
            }

            currentMino.deactivating = false;

            // replace currentMino with the nextMino
            currentMino = nextMino;
            currentMino.setXY(MINO_START_X, MINO_START_Y);
            nextMino = pickMino();
            nextMino.setXY(NEXT_MINO_X, NEXT_MINO_Y);

            // after deactivating mino piece, check if row can be deleted
            checkRowDelete();

        } else {
            currentMino.update();
        }
    }

    private void checkRowDelete() {

        int x = left_x;
        int y = top_y;
        int blockCount = 0;
        int rowCount = 0;

        while (x < right_x && y < bottom_y) {

            for (int i = 0; i < staticBlocks.size(); i++) {
                // increase the blockCount if there is a static block
                if (staticBlocks.get(i).x == x && staticBlocks.get(i).y == y) {
                    blockCount++;
                }
            }

            x += Block.SIZE;

            if (x == right_x) {

                // if blockCount = 12, all blocks within current row will be deleted
                if (blockCount == 12) {

                    // delete row effect variables
                    effectCounterOn = true;
                    effectY.add(y);

                    for (int i = staticBlocks.size() - 1; i > -1; i--) {
                        // remove all blocks within current row
                        if (staticBlocks.get(i).y == y) {
                            staticBlocks.remove(i);
                        }
                    }

                    rowCount++;
                    rows++;

                    // auto drop speed
                    // speed up autoDropIntervals as levels inscrease
                    if (rows % 5 == 0 && dropInterval > 1) { // speed up every 5 levels

                        level++;
                        if (dropInterval > 10) {
                            dropInterval -= 10;
                        } else {
                            dropInterval -= 1;
                        }
                    }

                    // after deleting a row, all rows about must shift down by 1 row
                    for (int i = 0; i < staticBlocks.size(); i++) {
                        // if there is a block above current row deletion, move block down by block size
                        if (staticBlocks.get(i).y < y) {
                            staticBlocks.get(i).y += Block.SIZE;
                        }
                    }
                }

                blockCount = 0;
                x = left_x;
                y += Block.SIZE;
            }
        }

        // add score
        if (rowCount > 0) {
            int singleRowCount = 10 * level;
            score += singleRowCount * rowCount;
        }
    }

    public void draw(Graphics2D g2) {

        // draw main game area
        g2.setColor(Color.white);
        g2.setStroke(new BasicStroke(4f));
        g2.drawRect(left_x - 4, top_y - 4, WIDTH + 8, HEIGHT + 8);

        // draw peice waiting area
        int x = right_x + 100;
        int y = bottom_y - 200;
        g2.drawRect(x, y, 200, 200);
        g2.setFont(new Font("Arial", Font.PLAIN, 30));
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.drawString("NEXT", x + 60, y + 60);

        // draw score area
        g2.drawRect(x, top_y, 250, 300);
        x += 40;
        y = top_y + 50;
        g2.drawString("Level: " + level, x, y);
        y += 70;
        g2.drawString("Rows: " + rows, x, y);
        y += 70;
        g2.drawString("Score: " + score, x, y);
        y += 70;

        // draw the currentMino
        if (currentMino != null) {
            currentMino.draw(g2);
        }

        // draw nextMino
        nextMino.draw(g2);

        // draw staticBlocks
        for (int i = 0; i < staticBlocks.size(); i++) {
            staticBlocks.get(i).draw(g2);
        }

        // draw delete row effect
        if (effectCounterOn) {
            effectCounter++;

            g2.setColor(Color.red);
            for (int i = 0; i < effectY.size(); i++) {
                g2.fillRect(left_x, effectY.get(i), WIDTH, Block.SIZE);
            }

            // remove effect after 10 frames
            if (effectCounter == 10) {
                effectCounterOn = false;
                effectCounter = 0;
                effectY.clear();
            }
        }

        // draw pause or game over message
        g2.setColor(Color.yellow);
        g2.setFont(g2.getFont().deriveFont(50f));
        if (gameOver) {
            x = left_x + 50;
            y = top_y + 320;
            g2.drawString("Game Over", x, y);
        } else if (KeyHandler.pausedPressed) {
            x = left_x + 100;
            y = top_y + 320;
            g2.drawString("Paused", x, y);
        }

        // draw game title
        x = 35;
        y = top_y + 320;
        g2.setColor(Color.white);
        g2.setFont(g2.getFont().deriveFont(50f));
        g2.drawString("Tetris", x + 100, y);

    }

}