package com.geooda1.mod;

import java.util.UUID;

/** Estado de un jugador tal como lo ve el cliente. status: 0 vivo, 1 caido, 2 muerto definitivo. */
public record PlayerEntry(UUID id, String name, int lives, int status, long remainingMs) {
}
