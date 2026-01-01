# MineSkill Plugin

Полнофункциональный плагин системы скиллов с деревом прокачки для Paper 1.21

## Возможности

- **4 ветки скиллов**: Сила, Ловкость, Выносливость, Мудрость
- **Интерактивное GUI**: 54-слотный интерфейс с переключением вкладок
- **Система требований**: Некоторые скиллы требуют предварительной покупки других
- **Атрибуты и эффекты**: Бонусы к урону, броне, здоровью, скорости, регенерации и ночному зрению
- **Кастомные механики**: Evasion (уклонение), критические удары, Double Jump
- **Сохранение данных**: Все данные сохраняются в YAML файл
- **Команды администратора**: Сброс скиллов, добавление очков

## Скиллы

### Сила (STRENGTH)
- **Power Blow** (5 очков) - Увеличение урона на 5% за уровень
- **Iron Skin** (10 очков) - Броня +0.5 за уровень
- **Extra Health** (7 очков) - Макс. здоровье +2 HP за уровень

### Ловкость (AGILITY)
- **Swift Movement** (5 очков) - Скорость +10% за уровень
- **Double Jump** (15 очков) - Прыжок в воздухе (требует Swift Movement)
- **Evasion** (10 очков) - Снижение урона на 5% за уровень

### Выносливость (ENDURANCE)
- **Stamina Boost** (5 очков) - Замедление усталости
- **Regeneration** (12 очков) - Регенерация I-III (требует Stamina Boost)
- **Night Vision** (8 очков) - Постоянное ночное зрение

### Мудрость (WISDOM)
- **Mana Pool** (8 очков) - Резерв маны
- **Fast Mining** (6 очков) - Ускорение добычи на 10% за уровень
- **Lucky Strike** (14 очков) - 5% шанс крита за уровень

## Команды

- `/skilltree` или `/skill` - Открыть дерево скиллов
- `/skilltree open` - Открыть дерево скиллов
- `/skilltree info` - Информация о скиллах и очках
- `/skilltree reset [player]` - Сбросить скиллы (требует права)
- `/skilltree addpoints <player> <amount>` - Добавить очки (требует права)
- `/skilltree reload` - Перезагрузить конфигурацию (требует права)

## Права

- `skilltree.use` - Использование дерева скиллов (по умолчанию: true)
- `skilltree.admin` - Административные команды (по умолчанию: op)

## Установка

1. Скомпилируйте плагин: `mvn clean package`
2. Скопируйте файл из `target/MineSkill-1.0.0.jar` в папку `plugins` сервера
3. Перезапустите сервер

## Конфигурация

Файл: `plugins/MineSkill/config.yml`

```yaml
skill-tree:
  enable-double-jump: true
  enable-custom-damage: true
  skill-point-gain: 1
  
  multipliers:
    damage: 0.05
    armor: 0.5
    health: 2.0
    speed: 0.1
```

## Структура проекта

```
MineSkill/
├── pom.xml
├── src/main/java/com/example/mineskill/
│   ├── MineSkillPlugin.java
│   ├── managers/
│   │   ├── SkillManager.java
│   │   ├── PlayerDataManager.java
│   │   └── SkillApplier.java
│   ├── gui/
│   │   ├── SkillTreeGui.java
│   │   └── GuiManager.java
│   ├── listeners/
│   │   ├── GuiClickListener.java
│   │   ├── PlayerJoinListener.java
│   │   ├── PlayerQuitListener.java
│   │   └── DamageListener.java
│   ├── commands/
│   │   └── SkillTreeCommand.java
│   ├── models/
│   │   ├── Skill.java
│   │   ├── SkillBranch.java
│   │   ├── SkillEffect.java
│   │   └── PlayerSkillData.java
│   └── utils/
│       ├── ItemBuilder.java
│       └── ColorUtil.java
└── src/main/resources/
    ├── plugin.yml
    └── config.yml
```

## Требования

- Java 17+
- Paper 1.21

## Лицензия

MIT License
