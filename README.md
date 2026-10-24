# geooda1 (Forge 1.20.1)

## Como compilarlo
1. Descarga el MDK oficial de Forge 1.20.1 (47.3.0): https://files.minecraftforge.net/ (boton "Mdk").
2. Descomprime el MDK, borra su carpeta `src` y copia dentro la carpeta `src` de este proyecto.
3. Reemplaza `build.gradle` del MDK por el de este proyecto (o deja el del MDK y ajusta mod id = geooda1).
4. Necesitas Java 17. En una terminal dentro de la carpeta:  `./gradlew build`  (Windows: `gradlew.bat build`)
5. El mod queda en `build/libs/geooda1-1.0.0.jar`. Ponlo en la carpeta `mods` del servidor y de cada jugador.
Para probarlo directo: `./gradlew runClient`.
