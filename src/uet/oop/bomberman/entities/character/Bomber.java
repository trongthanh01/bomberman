package uet.oop.bomberman.entities.character;

import uet.oop.bomberman.Board;
import uet.oop.bomberman.Game;
import uet.oop.bomberman.entities.Entity;
import uet.oop.bomberman.entities.LayeredEntity;
import uet.oop.bomberman.entities.bomb.Bomb;
import uet.oop.bomberman.entities.bomb.Flame;
import uet.oop.bomberman.entities.bomb.FlameSegment;
import uet.oop.bomberman.entities.character.enemy.Enemy;
import uet.oop.bomberman.entities.tile.Wall;
import uet.oop.bomberman.entities.tile.destroyable.Brick;
import uet.oop.bomberman.graphics.Screen;
import uet.oop.bomberman.graphics.Sprite;
import uet.oop.bomberman.input.Keyboard;
import uet.oop.bomberman.level.Coordinates;

import java.util.Iterator;
import java.util.List;

public class Bomber extends Character {

    private List<Bomb> _bombs;
    protected Keyboard _input;

    /**
     * nếu giá trị này < 0 thì cho phép đặt đối tượng Bomb tiếp theo,
     * cứ mỗi lần đặt 1 Bomb mới, giá trị này sẽ được reset về 0 và giảm dần trong mỗi lần update()
     */
    protected int _timeBetweenPutBombs = 0;

    public Bomber(int x, int y, Board board) {
        super(x, y, board);
        _bombs = _board.getBombs();
        _input = _board.getInput();
        _sprite = Sprite.player_right;
    }

    @Override
    public void update() {
        clearBombs();
        if (!_alive) {
            afterKill();
            return;
        }

        if (_timeBetweenPutBombs < -7500) _timeBetweenPutBombs = 0;
        else _timeBetweenPutBombs--;

        animate();

        calculateMove();

        detectPlaceBomb();
    }

    @Override
    public void render(Screen screen) {
        calculateXOffset();

        if (_alive)
            chooseSprite();
        else
            _sprite = Sprite.player_dead1;

        screen.renderEntity((int) _x, (int) _y - _sprite.SIZE, this);
    }

    public void calculateXOffset() {
        int xScroll = Screen.calculateXOffset(_board, this);
        //Screen.setOffset(xScroll, 0);
    }

    /**
     * Kiểm tra xem có đặt được bom hay không? nếu có thì đặt bom tại vị trí hiện tại của Bomber
     */
    private void detectPlaceBomb() {
        // TODO: kiểm tra xem phím điều khiển đặt bom có được gõ và giá trị _timeBetweenPutBombs, Game.getBombRate() có thỏa mãn hay không
        // TODO:  Game.getBombRate() sẽ trả về số lượng bom có thể đặt liên tiếp tại thời điểm hiện tại
        // TODO: _timeBetweenPutBombs dùng để ngăn chặn Bomber đặt 2 Bomb cùng tại 1 vị trí trong 1 khoảng thời gian quá ngắn
        // TODO: nếu 3 điều kiện trên thỏa mãn thì thực hiện đặt bom bằng placeBomb()
        // TODO: sau khi đặt, nhớ giảm số lượng Bomb Rate và reset _timeBetweenPutBombs về 0
        if(_input.space && _timeBetweenPutBombs < 0 && Game.getBombRate() > 0 && _board.getBombAt(getXTile(), getYTile()) == null) {
            placeBomb(getXTile(), getYTile());
            _timeBetweenPutBombs = 0;
            Game.addBombRate(-1);
        }
    }

    protected void placeBomb(int x, int y) {
        // TODO: thực hiện tạo đối tượng bom, đặt vào vị trí (x, y)
        Bomb bomb = new Bomb(x, y, _board);
        _board.addBomb(bomb);
    }

    private void clearBombs() {
        Iterator<Bomb> bs = _bombs.iterator();

        Bomb b;
        while (bs.hasNext()) {
            b = bs.next();
            if (b.isRemoved()) {
                bs.remove();
                Game.addBombRate(1);
            }
        }
    }

    @Override
    public void kill() {
        if (!_alive) return;
        _alive = false;
    }

    @Override
    protected void afterKill() {
        if (_timeAfter > 0) --_timeAfter;
        else {
            _board.endGame();
        }
    }

    @Override
    protected void calculateMove() {
        // TODO: xử lý nhận tín hiệu điều khiển hướng đi từ _input và gọi move() để thực hiện di chuyển
        // TODO: nhớ cập nhật lại giá trị cờ _moving khi thay đổi trạng thái di chuyển
        double bomberSpeed = Game.getBomberSpeed();
        double x = _x, y = _y;
        if(_input.left) {
            x = _x - 1 * bomberSpeed;
        }
        if(_input.right) {
            x = _x + 1 * bomberSpeed;
        }
        if(_input.up) {
            y = _y - 1 * bomberSpeed;
        }
        if(_input.down) {
            y = _y + 1 * bomberSpeed;
        }
        if(x != _x || y != _y) {
            _moving = true;
            move(x, y);
        }
        else {
            _moving = false;
        }

    }

    @Override
    public boolean canMove(double x, double y) {
        // TODO: kiểm tra có đối tượng tại vị trí chuẩn bị di chuyển đến và có thể di chuyển tới đó hay không
        // Kiểm tra cả 4 góc để xử lý va chạm được triệt để
        // Can move có vấn đề, FIX HERE!!!
        int spriteSize = _sprite.getSize();

        Entity entityBottomLeft  = _board.getEntity(Coordinates.pixelToTile(x), Coordinates.pixelToTile(y - 1), this);
        Entity entityBottomRight = _board.getEntity(Coordinates.pixelToTile(x + spriteSize - 1), Coordinates.pixelToTile(y - 1), this);
        Entity entityTopLeft     = _board.getEntity(Coordinates.pixelToTile(x), Coordinates.pixelToTile(y - spriteSize), this);
        Entity entityTopRight    = _board.getEntity(Coordinates.pixelToTile(x + spriteSize - 1), Coordinates.pixelToTile(y - spriteSize), this);

        // DEBUG
//         System.out.println("====================");
//         System.out.println(x + " - " + Coordinates.pixelToTile(x) + ", " + (y - 1) + " - " + Coordinates.pixelToTile(y - 1) + ": " + entityBottomLeft);
//         System.out.println((x + spriteSize - 1) + " - " + Coordinates.pixelToTile(x + spriteSize - 1) + ", " + (y - 1) + " - " + Coordinates.pixelToTile(y - 1) + ": " + entityBottomRight);
//         System.out.println(x + " - " + Coordinates.pixelToTile(x) + ", " + (y - spriteSize) + " - " + Coordinates.pixelToTile(y - spriteSize) + ": " + entityTopLeft);
//         System.out.println((x + spriteSize - 1) + " - " + Coordinates.pixelToTile(x + spriteSize - 1) + ", " + (y - spriteSize) + " - " + Coordinates.pixelToTile(y - spriteSize) + ": " + entityTopRight);
        // END DEBUG

//        System.out.println(collide(entityBottomLeft) + " " + collide(entityBottomRight) + " " + collide(entityTopLeft) + " " + collide(entityTopRight));
        if(collide(entityBottomRight) || collide(entityBottomLeft) || collide(entityTopLeft) || collide(entityTopRight)) {
            return false;
        }
        return true;
    }

    @Override
    public void move(double xa, double ya) {
        // TODO: sử dụng canMove() để kiểm tra xem có thể di chuyển tới điểm đã tính toán hay không và thực hiện thay đổi tọa độ _x, _y
        // TODO: nhớ cập nhật giá trị _direction sau khi di chuyển
        if(_input.up) {
            _direction = 0;
        }
        else if(_input.right) {
            _direction = 1;
        }
        else if(_input.down) {
            _direction = 2;
        }
        else if(_input.left) {
            _direction = 3;
        }

        if(canMove(xa, ya)) {
            _x = xa;
            _y = ya;
        }
    }

    @Override
    public boolean collide(Entity e) {
        // TODO: xử lý va chạm với Flame
        // TODO: xử lý va chạm với Enemy
        if(e instanceof Flame || e instanceof Enemy || e instanceof FlameSegment) {
            this.kill();
            return true;
        }
        else if(e instanceof Wall || (e instanceof LayeredEntity && ((LayeredEntity) e).getTopEntity() instanceof Brick)) {
            return true;
        }
        // TODO: xử lý va chạm bom
        else if(e instanceof Bomb) {
            return e.collide(this);
        }
        return false;
    }

    private void chooseSprite() {
        switch (_direction) {
            case 0:
                _sprite = Sprite.player_up;
                if (_moving) {
                    _sprite = Sprite.movingSprite(Sprite.player_up_1, Sprite.player_up_2, _animate, 20);
                }
                break;
            case 1:
                _sprite = Sprite.player_right;
                if (_moving) {
                    _sprite = Sprite.movingSprite(Sprite.player_right_1, Sprite.player_right_2, _animate, 20);
                }
                break;
            case 2:
                _sprite = Sprite.player_down;
                if (_moving) {
                    _sprite = Sprite.movingSprite(Sprite.player_down_1, Sprite.player_down_2, _animate, 20);
                }
                break;
            case 3:
                _sprite = Sprite.player_left;
                if (_moving) {
                    _sprite = Sprite.movingSprite(Sprite.player_left_1, Sprite.player_left_2, _animate, 20);
                }
                break;
        }
    }
}
