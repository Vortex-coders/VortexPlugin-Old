# VortexPlugin

Основной plug-in для игровых серверов Vortex.
Находится в начале разработки, будем рады вашей помощи.

## Участие в разработке

Чтобы помочь плагину, вам достаточно делать commit'ы с добавлением, правкой кода или перевода плагина.
Все необходимые правила и советы
расписаны [здесь](https://github.com/Darkdustry-Coders/DarkdustryPlugin/blob/master/CONTRIBUTING.md).

Если вы хотите вступить в команду, то свяжитесь с администраторами в [Discord](https://discord.gg/pTtQTUQM68).

## Форматирование кода

Для форматирования кода используется prettier, prettier-java и встроенный в Intellij idea checkstyle.
Для checkstyle существует [конфигурация](checkstyle.xml).

## Компиляция

Gradle может потребоваться до нескольких минут для загрузки файлов.
После сборки выходной jar-файл должен находиться в каталоге `/build/libs/`.

Сначала убедитесь, что у вас установлен JDK 19. Откройте терминал в каталоге проекта и выполните следующие команды:

### Windows

`gradlew jar`

### Linux/Mac OS

`./gradlew jar`

### Устранение неполадок

#### Permission Denied

Если терминал выдает `Permission denied` или `Command not found` на Mac/Linux, выполните `chmod +x ./gradlew` перед
запуском `./gradlew`. Это одноразовая процедура.
