# ParticleFX

Плагин Paper 1.21.11: **50 анимаций целиком из частиц** — без сущностей, Armor Stand, Display Entity и внешних библиотек.

## Сборка
Нужен JDK 21.
```bash
mvn clean package
```
Готовый jar: `target/ParticleFX.jar`.

## Установка
1. Скопируйте `ParticleFX.jar` в папку `plugins/` сервера Paper 1.21.11.
2. Перезапустите сервер.
3. В игре: `/particlefx` — откроется меню.

## Проверка эффекта с орбитами
```
/particlefx play atom
```

## Команды
| Команда | Право | Описание |
|---|---|---|
| `/particlefx` | particlefx.use | открыть меню |
| `/particlefx menu` | particlefx.use | открыть меню |
| `/particlefx play <эффект> [игрок]` | particlefx.play | запустить эффект |
| `/particlefx stop` | particlefx.play | остановить свои эффекты |
| `/particlefx list` | particlefx.use | список анимаций |
| `/particlefx reload` | particlefx.admin | перезагрузить конфиг |

Алиас: `/pfx`.

## Производительность
- Единый механизм: одна задача Bukkit на эффект, всё в основном потоке.
- Общий бюджет частиц за тик (`max-particles-per-tick`), при исчерпании — пропуск без ошибок.
- Лимиты на игрока и сервер, клампинг NaN/бесконечных/отрицательных значений.
- Отдельный эффект выключается в `effects.<id>.enabled`, затем `/particlefx reload`.
