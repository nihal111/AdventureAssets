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
package org.terasology.adventureassets.traps.platform;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.adventureassets.traps.swingingblade.SwingingBladeClientSystem;
import org.terasology.adventureassets.traps.swingingblade.SwingingBladeComponent;
import org.terasology.assets.management.AssetManager;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.MovementMode;
import org.terasology.logic.characters.events.SetMovementModeEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.health.OnDamagedEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.location.Location;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.physics.components.RigidBodyComponent;
import org.terasology.physics.events.ImpulseEvent;
import org.terasology.registry.In;
import org.terasology.structureTemplates.events.StructureSpawnerFromToolboxRequest;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.items.BlockItemComponent;
import org.terasology.world.block.items.OnBlockItemPlaced;
import org.terasology.world.block.items.OnBlockToItem;

@RegisterSystem(RegisterMode.AUTHORITY)
public class PlatformSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final Logger logger = LoggerFactory.getLogger(PlatformSystem.class);

    @In
    private EntityManager entityManager;
    @In
    private AssetManager assetManager;
    @In
    private InventoryManager inventoryManager;
    @In
    private Time time;

    @Override
    public void update(float delta) {

    }

    @ReceiveEvent(components = PlatformComponent.class)
    public void onDamage(OnDamagedEvent event, EntityRef entity, PlatformComponent platformComponent) {
        logger.info("damage");
        entity.send(new ImpulseEvent(new Vector3f(0,0,210)));
    }

    @ReceiveEvent(components = PlatformComponent.class)
    public void onDamage(ActivateEvent event, EntityRef entity, PlatformComponent platformComponent) {
        RigidBodyComponent rigidBodyComponent = entity.getComponent(RigidBodyComponent.class);

        logger.info(event.getInstigator().toFullDescription());
        if (platformComponent.character == null) {
            event.getInstigator().send(new SetMovementModeEvent(MovementMode.NONE));
            platformComponent.character = event.getInstigator();
            Location.attachChild(entity, platformComponent.character, new Vector3f(0, 1.5f, 0), new Quat4f());
            rigidBodyComponent.collidesWith.remove(StandardCollisionGroup.CHARACTER);
            rigidBodyComponent.collidesWith.remove(StandardCollisionGroup.DEFAULT);
        } else {
            event.getInstigator().send(new SetMovementModeEvent(MovementMode.WALKING));
            Location.removeChild(entity, platformComponent.character);
            platformComponent.character = null;
            rigidBodyComponent.collidesWith.add(StandardCollisionGroup.CHARACTER);
            rigidBodyComponent.collidesWith.add(StandardCollisionGroup.DEFAULT);
        }
        entity.saveComponent(rigidBodyComponent);
        entity.saveComponent(platformComponent);
    }
}
