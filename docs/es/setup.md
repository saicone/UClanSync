---
sidebar_position: 3
title: Configuración
description: Como configurar UClanSync
---

La configuración de UClanSync se divide en varias partes, aquí se mostrará como configurar cada una de ellas.

## Server

Esta es la parte de la configuración más importante y se sugiere editarla inmediatamente después de instalar UClanSync.

```yaml
# Configuración del servidor
Server:
  # Cada servidor debe tener el MISMO NOMBRE que tiene en la configuración
  # del proxy para hacer funcionar los homes de manera correcta
  Name: 'MySurvival1'
```

## Addon

Esta es la parte de la configuración donde se edita el addon en general además de sus opciones.

```yaml
# Configuración del addon
Addon:
  # Nivel de logs para filtrar cuales mensajes se verán en consola, ponlo en 0 para desactivar todos los mensajes
  # 1 = errores
  # 2 = errores, advertencias
  # 3 = errores, advertencias, información
  # 4 = errores, advertencias, información, registro
  LogLevel: 3
  # Configuración del actualizador de clan
  ClanUpdater:
    # Retraso en ticks para actualizar un clan (Para evitar múltiples actualizaciones al mismo tiempo)
    # 20 ticks = 1 segundo
    Update-Delay: 40
    # Tiempo en ticks para guardar un teletransporte requerido (En caso de que el jugador aún no haya entrado el host)
    # 20 ticks = 1 segundo
    Teleport-Cache: 200
    # Tiempo en ticks para enviar a los otros servidores una actualización de los jugadores conectados
    # 20 ticks = 1 segundo
    Player-Update: 80
  # Opciones adicionales del addon
  Feature:
    # Activar o desactivar los homes multi-host
    Homes: true
    # Activar o desactivar el chat de clan multi-host
    Chat: true
```

## Messenger

Configuración del servicio de mensajería que utilizará UClanSync para la sincronización

```yaml
# Configuración de la mensajería
Messenger:
  # Canal de comunicación, los servidores que quieres sincronizar deben tener configurado
  # el mismo canal
  Channel: 'survival:uclansync'
  # Tipos de servicios de mensajería:
  # - PROXY    = No necesita ninguna instalación adicional, solo requiere que todos
  #              los servidores conectados se encuentren en el mismo Bungee o Velocity.
  # - PLUGIN   = Lo mismo que el tipo PROXY, pero requiere meter la versión de UClanSync
  #              en el Bungeecord o Velocity.
  # - REDIS    = Usar un servidor Redis para la mensajería (Se configura más abajo).
  # - RABBITMQ = Usar un servidor RabbitMQ para la mensajería (Se configura más abajo).
  Type: 'PROXY'
  # Configuración de Redis
  Redis:
    # Url de conexión con Redis
    #
    # Formato del URL: redis://[user]:[password@]host[:port][/databaseNumber]
    url: 'redis://:password@localhost:6379/0'
  # Configuración de RabbitMQ
  RabbitMQ:
    exchange: 'uclansync'
    # Url de conexión con RabbitMQ
    # Documentación: https://www.rabbitmq.com/uri-spec.html
    #
    # Formato del URL: amqp://userName:password@hostName:portNumber/virtualHost
    # Si el virtual host es "/", se debe poner como "%2F"
    url: 'amqp://guest:guest@localhost:5672/%2F'
```