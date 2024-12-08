# TrabajoSDAdrianVecino
# Juego de Ahorcado Multijugador

Este es un proyecto para la asignatura de Sistemas Distribuidos, que implementa una versión multijugador del clásico juego del **Ahorcado** utilizando un modelo cliente-servidor.

## Descripción del proyecto

El proyecto consiste en un servidor que gestiona las partidas de ahorcado con múltiples clientes que pueden conectarse para jugar en dos modalidades principales:
1. **Contra la máquina:** Un jugador se enfrenta al servidor, que selecciona una palabra aleatoria para el juego.
2. **En pareja:** Dos jugadores se enfrentan; uno elige la palabra secreta mientras el otro intenta adivinarla letra por letra.

El servidor es **concurrente** y permite la ejecución de múltiples partidas de forma simultánea. Cada partida se gestiona en un hilo independiente, garantizando que varias partidas puedan desarrollarse sin interferir unas con otras.

Además, el servidor:
- **Almacena puntuaciones** de los jugadores y permite que los clientes las consulten.
- Mantiene un sistema básico de estadísticas, con el número de partidas jugadas y ganadas.

## Características principales

- **Concurrencia:** El servidor utiliza hilos para gestionar cada partida de manera independiente.
- **Gestión de múltiples clientes:** Los clientes pueden conectarse y jugar en parejas o contra la máquina.
- **Almacenamiento de puntuaciones:** Las estadísticas de los jugadores se almacenan en un archivo y están disponibles para consulta.

## Cómo usarlo

### 1. Requisitos 
- Clonar este repositorio:
  ```bash
  https://github.com/advecino/TrabajoSDAdrianVecino
  
