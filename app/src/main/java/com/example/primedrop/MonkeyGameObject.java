package com.example.primedrop;

public class MonkeyGameObject extends GameObject {

    private Direction movingDirection = Direction.LEFT;
    private Direction lookingDirection = Direction.RIGHT;

    public Direction getMovingDirection() {
        return movingDirection;
    }

    public void setMovingDirection(Direction movingDirection) {
        this.movingDirection = movingDirection;
    }

    public Direction getLookingDirection() {
        return lookingDirection;
    }

    public void setLookingDirection(Direction lookingDirection) {
        this.lookingDirection = lookingDirection;
    }

}

