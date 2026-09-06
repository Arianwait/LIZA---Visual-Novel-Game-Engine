# Liza — движок визуальной новеллы

Движок визуальной новеллы на **Java 21 + JavaFX 21.0.5**. Весь сюжет описывается в
XML-файлах, а логика расширяется через аннотации и автопоиск классов (Reflections) —
новые команды, кнопки, панели и визуальные эффекты добавляются без правки ядра.

> Игра запускается классом `kz.arianwait.game.dispatcher.GameDispatcher`, читает сцены из
> `lib/Scene/Dialog_Structured.xml` и ресурсы из папки `lib/`.

---

## Возможности

- **XML-сценарии** — сцены, реплики, команды и ветвления описываются декларативно.
- **Персонажи** — спрайты, позы, позиции (левее/центр/правее), плавные появления и уходы.
- **Визуальные эффекты** — моргание, засыпание, тряска, зум, ч/б и сепия; комбинируются и живут между кадрами.
- **Мини-игры (панели)** — крестики-нолики, ввод кода, доска улик, мемори и т.д.
- **Флаги, выборы, репутация** — состояние игрока с подстановкой переменных `{ключ}` в текст.
- **Кликабельные улики** прямо в репликах.
- **Темы интерфейса** — `hitech`, `classic`, `walk`, `dnd`, `fire` (переключаются по ходу сюжета).
- **Сохранение/загрузка** (6 слотов) и кнопка **«Назад»** с дельта-историей (экономия памяти).
- **Звук** — фоновая музыка, звуковые эффекты, озвучка реплик.

---

## Требования

| Компонент | Версия |
|-----------|--------|
| JDK       | 21     |
| Maven     | 3.8+   |
| JavaFX    | 21.0.5 (тянется Maven'ом автоматически) |

JavaFX, JSON (`json-simple`), `reflections` и `slf4j` + `logback` подключаются как
Maven-зависимости — отдельно ставить JavaFX SDK не нужно.

---

## Запуск

> ⚠️ Движок читает ресурсы по относительным путям (`lib/...`), поэтому запускать нужно
> **из корня проекта** — каталога, где лежит папка `lib/`.

### Вариант 1. Прямо из Maven (для разработки)

```bash
mvn clean javafx:run
```

### Вариант 2. Собрать «толстый» JAR (со всеми зависимостями)

```bash
mvn clean package
java -jar target/liza-engine-1.0.0-jar-with-dependencies.jar
```

### Вариант 3. Готовый `.exe` (Windows)

`mvn package` через **launch4j** дополнительно собирает `target/ProjectElein-launch4j.exe`
(ожидает рядом JRE 21+ в папке `jre`).

### Вариант 4. Портативная сборка и установщик (jpackage)

```bash
mvn -Pjpackage clean package            # target/dist/ProjectElein/ — папка с exe и рантаймом
mvn -Pinstaller clean package -DskipTests   # target/installer/ProjectElein-<версия>.exe (нужен WiX 3.x в PATH)
```

Установщик ставит игру в `%LOCALAPPDATA%\ProjectElein` без прав администратора и создаёт ярлыки.

---

## Структура проекта

```
kz.arianwait.game/
├── pom.xml                     — сборка (Maven + JavaFX + assembly + launch4j)
├── Scenario_Guide.md           — как писать сценарии (XML)
├── lib/                        — все игровые ресурсы и конфиги (НЕ в src!)
│   ├── Scene/
│   │   ├── Dialog_Structured.xml  — все диалоги игры
│   │   ├── Person.xml             — персонажи: имена, позы, спрайты
│   │   └── backgrounds/           — фоновые изображения
│   ├── person/                 — спрайты персонажей
│   ├── sound/                  — музыка и звуки
│   ├── fxml/                   — FXML-разметка панелей/меню
│   └── config/
│       ├── style.css              — общие стили (кнопки, панели, темы)
│       ├── dialog_styles.css      — стили текста диалогов
│       └── UI/                    — Buttons.xml, MainMenu.xml, SceneSettings.xml …
└── src/main/java/kz/arianwait/game/
    ├── dispatcher/             — GameDispatcher (точка входа, JavaFX Application)
    ├── engine/                 — ядро: GameEngine, parser, render, model, effect
    │   ├── parser/             — SceneXmlParser (XML → SceneFrame)
    │   ├── render/             — SceneRenderer (отрисовка кадра)
    │   ├── model/              — SceneFrame, HistoryStep, *EffectCommand …
    │   └── effect/             — проигрыватели эффектов (@VisualEffect)
    ├── panel/                  — система панелей/мини-игр (@GamePanel, PuzzleRegistry)
    ├── buttonaction/           — действия кнопок (@ButtonAction)
    ├── scenelist/              — SceneController (состояние), SceneBuilder, SceneInfo, GameData
    ├── scenedetails/           — панель диалога, оверлеи, диалоговые контроллеры
    ├── mainscene/              — главное меню, настройки, экран сохранений
    ├── soundtrack/             — SoundManager, Soundtrack, SoundEffect
    ├── appsettings/            — AppSettings, JsonParser, JsonConfigWriter
    └── utils/                  — парсеры конфигов, UiFactory, подстановка переменных
```

---

## Как это работает (кратко)

1. **`GameDispatcher.start()`** читает настройки (`AppSettings`), создаёт окно и сцену,
   подключает `lib/config/style.css` и показывает логотип-заставку.
2. **`SceneXmlParser`** разбирает `Dialog_Structured.xml` в список `SceneFrame`
   (все сцены грузятся в память на старте).
3. **`GameEngine`** ведёт игровой цикл: показывает кадр через **`SceneRenderer`**,
   обрабатывает «Далее»/«Назад», историю, сохранения и запуск панелей.
4. **`SceneXmlParser`** превращает `<command>` в объекты модели (`StateCommand`,
   `PuzzleCommand`, `*EffectCommand`, `SoundCommand`), а `GameEngine` исполняет их
   при показе кадра; эффекты и панели находятся через реестры
   (`@VisualEffect`, `@GamePanel`).
5. **`SceneController`** хранит состояние игрока (флаги, выборы, репутация) со снимками
   для отката; полный набор снимков пишется в историю и в сейв.

---

## Написание сценариев

Полная инструкция по XML-формату (сцены, реплики, команды, эффекты, улики, темы) —
в файле **[Scenario_Guide.md](Scenario_Guide.md)**.

Минимальный пример:

```xml
<game>
    <dialog id="1" nextScene="2" background="lib/Scene/Scene1/Scene1.png">
        <overlay text="Глава 1. Начало истории."/>
        <character name="Автор" color="WHITE">Ты заходишь в тёмный лес.</character>
        <character name="Liza" color="GREEN">
            <command type="character" action="showPerson" target="Liza" value="Stay"/>
            Не стой на дороге, отойди!
        </character>
    </dialog>
</game>
```

---

## Точки расширения

Движок построен на аннотациях — нужные классы находятся автоматически через Reflections.
Достаточно положить новый класс в соответствующий пакет.

| Что добавляем | Аннотация | Пакет | Вызов из XML |
|---------------|-----------|-------|--------------|
| Кнопка | `@ButtonAction("id")` | `kz.arianwait.game.buttonaction` | `id` кнопки в `lib/config/UI/Buttons.xml` |
| Панель / мини-игра | `@GamePanel(id=...)` | `kz.arianwait.game.panel` | `<command type="panel" id="..."/>` |
| Визуальный эффект | `@VisualEffect("id")` | `kz.arianwait.game.engine.effect` | `<command type="effect" effect="id"/>` |

Пример эффекта:

```java
@VisualEffect("myEffect")
public class MyEffectPlayer implements VisualEffectPlayer {
    @Override
    public void play(StackPane root, Pane sceneContentLayer,
                     VisualEffectCommand command, Runnable onComplete) { /* ... */ }
    @Override
    public void stop(StackPane root, Pane sceneContentLayer) { /* ... */ }
}
```

---

## Сохранения и история

- Сейвы пишутся в профиль пользователя: `%APPDATA%\ProjectElein\save` (формат `.ser`, 6 слотов);
  логи — в `%APPDATA%\ProjectElein\logs`. Переопределение: `-Dliza.userdata=<папка>`, `-Dliza.logs=<папка>`.
- `GameData` хранит текущую сцену/кадр, историю, переменные, выборы и тему интерфейса.
- История ведётся в дельта-режиме: снимок состояния делается **только при изменении**,
  поэтому кнопка «Назад» работает на всю глубину при минимальном расходе памяти.

---

## Примечания по сборке

- Тяжёлые ручные архивы (`*.rar`, `*.zip`, `*.7z`), сборка (`target/`, `bin/`),
  метаданные Eclipse/IDE и сейвы (`save/*.ser`) исключены через `.gitignore`.
- Игровые ассеты (картинки, звук) **хранятся в репозитории** в папке `lib/` —
  это сознательный выбор проекта.
</content>
</invoke>
