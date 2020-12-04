package com.lehaine.samples;

import com.lehaine.ldtk.LDtkProject;
import com.lehaine.ldtk.LayerAutoLayer;
import com.lehaine.ldtk.Point;

import java.util.List;

// designate class for loading and attaching LDtk file to
@LDtkProject(ldtkFileLocation = "sample.ldtk", name = "JavaWorld")
public class SampleJava {
    public static void main(String[] args) {
        // create new LDtk world
        JavaWorld world = new JavaWorld();

        // get a level
        JavaWorld.JavaWorldLevel level = world.getAllLevels().get(0);

        // iterate over a layers tiles
        for (LayerAutoLayer.AutoTile tile : level.getLayerBackground().getAutoTiles()) {
            // logic for handling the tile
            int x = tile.getRenderX();
        }

        // iterate over entities
        for (JavaWorld.EntityMob mob : level.getLayerEntities().getAllMob()) {
            JavaWorld.MobType type = mob.type;
            Point patrolPoint = mob.getPatrol();
            int health = mob.getHealth();
        }

        for (JavaWorld.EntityCart cart : level.getLayerEntities().getAllCart()) {
            // field arrays / lists
            List<JavaWorld.Items> items = cart.getItems();

            for (JavaWorld.Items item : items) {
                if (item == JavaWorld.Items.Pickaxe) {
                    // spawn pickaxe
                }
            }
        }
    }
}



