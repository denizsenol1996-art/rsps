package io.xeros.model.entity.healthbar;

import lombok.Getter;

public abstract class HealthBarUpdate {

    @Getter
    protected final int id;
    protected final int width;

    public HealthBarUpdate(int id) {
        this.id = id;
        this.width = 30;
    }

    public int getWidth() { return width; }
}