package fr.milekat.utils.storage.adapter.sql.hikari;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class HikariEngineLoaders {
    public static @NotNull Map<HikariEngine, HikariPool> loadHikariPools() {
        //  Create a map to hold the HikariPool instances
        Map<HikariEngine, HikariPool> hikariPools = new HashMap<>();
        //  Iterate over all HikariEngine values and create a new HikariPool for each one
        for (HikariEngine engine : HikariEngine.values()) {
            try {
                //  Check if the driver class is available
                Class.forName(engine.getDriverClass());

                //  If yes, create a new EnginePool instance and add it to the map
                hikariPools.put(engine, (HikariPool)
                        Class.forName(engine.getEngineClass()).getDeclaredConstructor().newInstance());
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                     InstantiationException | IllegalAccessException ignored) {}
        }
        //  Return the map of HikariPool instances
        return hikariPools;
    }
}
