package io.xeros.model.entity.healthbar;

import dev.openrune.cache.CacheManager;
import dev.openrune.cache.filestore.definition.data.HealthDefinition;
import lombok.Getter;

public abstract class HealthBarUpdate {

    @Getter
    protected final int id;
    protected HealthDefinition template;

    public HealthBarUpdate(int id) {
        this.id = id;
        this.template = CacheManager.INSTANCE.health(id);
    }

}