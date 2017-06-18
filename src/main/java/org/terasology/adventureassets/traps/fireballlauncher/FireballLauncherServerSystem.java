/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.adventureassets.traps.fireballlauncher;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.adventureassets.traps.swingingblade.SwingingBladeComponent;
import org.terasology.assets.management.AssetManager;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.projectile.ProjectileActionComponent;
import org.terasology.registry.In;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.items.BlockItemComponent;
import org.terasology.world.block.items.OnBlockItemPlaced;
import org.terasology.world.block.items.OnBlockToItem;

@RegisterSystem(RegisterMode.AUTHORITY)
public class FireballLauncherServerSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final Logger logger = LoggerFactory.getLogger(FireballLauncherServerSystem.class);

    @In
    private EntityManager entityManager;
    @In
    private Time time;
    @In
    private AssetManager assetManager;

    @ReceiveEvent(components = {BlockItemComponent.class})
    public void onItemToBlock(OnBlockItemPlaced event, EntityRef itemEntity,
                              FireballLauncherComponent fireballLauncherComponent) {
        EntityRef entity = event.getPlacedBlock();
        entity.addOrSaveComponent(fireballLauncherComponent);
    }

    @ReceiveEvent
    public void onBlockToItem(OnBlockToItem event, EntityRef blockEntity, FireballLauncherComponent fireballLauncherComponent) {
        event.getItem().addOrSaveComponent(fireballLauncherComponent);
    }

    @Override
    public void update(float delta) {
        for (EntityRef fireballLauncher : entityManager.getEntitiesWith(FireballLauncherComponent.class, BlockComponent.class)) {
            FireballLauncherComponent fireballLauncherComponent = fireballLauncher.getComponent(FireballLauncherComponent.class);
            if (time.getGameTime() > fireballLauncherComponent.timePeriod + fireballLauncherComponent.lastShotTime) {
                logger.info("LAUNCH " + fireballLauncher.getId() + " " + fireballLauncherComponent.timePeriod);
                Prefab fireballPrefab = assetManager.getAsset("Projectile:fireball", Prefab.class).get();
                EntityBuilder fireballEntityBuilder = entityManager.newBuilder(fireballPrefab);
                EntityRef fireball = fireballEntityBuilder.build();

                ProjectileActionComponent projectileActionComponent = fireball.getComponent(ProjectileActionComponent.class);
                projectileActionComponent.direction = new Vector3f(0,0,1);
                projectileActionComponent.currentVelocity = new Vector3f(projectileActionComponent.direction).mul(projectileActionComponent.velocity);
                Vector3f pos = fireballLauncher.getComponent(LocationComponent.class).getWorldPosition();
                LocationComponent location = new LocationComponent(pos.add(projectileActionComponent.direction));
                location.setWorldScale(projectileActionComponent.iconScale);
                location.setWorldRotation(new Quat4f(Quat4f.IDENTITY));
                fireball.addOrSaveComponent(location);
                fireball.saveComponent(projectileActionComponent);

                fireballLauncherComponent.lastShotTime = time.getGameTime();
                fireballLauncher.saveComponent(fireballLauncherComponent);
            }
        }
    }
}

