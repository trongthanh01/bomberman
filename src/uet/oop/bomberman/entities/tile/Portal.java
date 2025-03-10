package uet.oop.bomberman.entities.tile;

import uet.oop.bomberman.Board;
import uet.oop.bomberman.entities.Entity;
import uet.oop.bomberman.entities.character.Bomber;
import uet.oop.bomberman.graphics.Sprite;
import uet.oop.bomberman.sounds.SoundEffect;

public class Portal extends Tile {
	private Board _board;

	public Portal(int x, int y, Sprite sprite, Board board) {
		super(x, y, sprite);
		_board = board;
	}

	@Override
	public boolean collide(Entity e) {
		// TODO: xử lý khi Bomber đi vào
		if(e instanceof Bomber && _board.detectNoEnemies()) {
			_board.getSoundEffect().stop();
			SoundEffect stageComplete = new SoundEffect(SoundEffect.STAGE_COMPLETE);
			stageComplete.play();
			System.out.println(getX() + " " + getY());

			chooseLevel();

			return true;
		}
		return true;
	}

	private void chooseLevel() {
		if(this.getX() == 2 && this.getY() == 7) {
			// Play adventure mode
			_board.loadLevel(1);
		}
		else {
			// Play multiplayer mode
			_board.nextLevel();
		}
	}
}
