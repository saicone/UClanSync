---
sidebar_position: 3
title: Setup
description: How configure UClanSync
---

The UClanSync configuration is divided into parts:

## Server

This is the most important part of configuration, it's suggested to edit it when you start UClanSync.

```yaml
# Current server configuration
Server:
  # Each server must have the SAME NAME has proxy configuration to make
  # clan homes work properly across servers
  Name: 'MySurvival1'
```

## Addon

This configuration part allows you to edit the general options of the addon.

```yaml
# Addon configuration
Addon:
  # Logging level to see messages in console, set 0 to disable all logs
  # 1 = errors
  # 2 = errors, warnings
  # 3 = errors, warnings, info
  # 4 = errors, warnings, info, debug information
  LogLevel: 3
  # Clan updater configuration
  ClanUpdater:
    # Delay in ticks to process a clan update (To avoid multiple updates at the same time)
    # 20 ticks = 1 second
    Update-Delay: 40
    # Time in ticks to save an teleport request
    # 20 ticks = 1 second
    Teleport-Cache: 200
    # Time in ticks to tell other servers the players that are connected on current server
    # 20 ticks = 1 seconds
    Player-Update: 80
  # Addon feature list to enable or disable
  Feature:
    # Clan homes sync
    Homes: true
    # Clan chat sync
    Chat: true
```

## Messenger

This configuration part allows you to edit the UClanSync messenger.

```yaml
# Messenger configuration
Messenger:
  # Messaging channel, servers who you want to synchronize clan changes
  # must have the same channel ID
  Channel: 'survival:uclansync'
  # Available messenger types:
  # - PROXY    = Don't require any type of installation, but your servers
  #              must be in the same proxy (Bungee or Velocity).
  # - PLUGIN   = Same has PROXY but require to put UClanSync plugin in
  #              your proxy instance (And is faster than PROXY type).
  # - REDIS    = Use a Redis server for messaging (Configure it below).
  # - RABBITMQ = Use a RabbitMQ server for messaging (Configure it below).
  Type: 'PROXY'
  # Redis configuration
  Redis:
    # Redis url connection
    #
    # URL: redis://[user]:[password@]host[:port][/databaseNumber]
    url: 'redis://:password@localhost:6379/0'
  # RabbitMQ configuration
  RabbitMQ:
    exchange: 'uclansync'
    # RabbitMQ url connection
    # Documentation: https://www.rabbitmq.com/uri-spec.html
    #
    # URL: amqp://userName:password@hostName:portNumber/virtualHost
    # If the virtual host is "/", set has "%2F"
    url: 'amqp://guest:guest@localhost:5672/%2F'
```