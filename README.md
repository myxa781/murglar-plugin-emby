# Murglar Plugin — Emby

Плагин для [Murglar](https://github.com/badmannersteam/murglar-plugins), позволяющий слушать музыку с вашего сервера [Emby](https://emby.media).

---

## Возможности

- Избранные треки / альбомы / артисты
- Плейлисты
- Обзор музыкальных библиотек по папкам
- Все треки / альбомы / артисты (с пагинацией)
- Поиск треков / альбомов / артистов
- Лайк / дизлайк
- Обложки альбомов
- Прямой стрим оригинального файла без транскодирования
- Поддержка форматов: MP3, FLAC, AAC/M4A, OGG, WAV, WMA

---

## Сборка

### Требования

- JDK 17+
- Android SDK (только для APK-сборки)
- Keystore для подписи APK (только для Android)

### Шаги

```bash
# 1. Настройте local.properties
cp local.template.properties local.properties
# Отредактируйте local.properties: укажите sdk.dir и параметры keystore

# 2. Сборка
./gradlew clean build
```

### Артефакты сборки

| Платформа | Путь |
|-----------|------|
| JAR (десктоп) | `emby-core/build/libs/murglar-plugin-emby-1.jar` |
| APK (Android) | `emby-android/build/outputs/apk/release/murglar-plugin-emby-1.apk` |

### Создание keystore (если нет)

```bash
keytool -genkey -v -keystore keystore.jks -alias key -keyalg RSA -keysize 2048 -validity 10000
```

---

## Авторизация в Murglar

Откройте плагин Emby в Murglar → **Войти**. Доступны два варианта:

### Вариант 1 — API Key (рекомендуется)

1. Откройте Emby Dashboard → **Advanced → API Keys → New API Key**
2. В Murglar введите:
   - **Server URL**: адрес вашего сервера, например `http://192.168.1.10:8096`
   - **API Key**: скопированный ключ

### Вариант 2 — Логин + Пароль

В Murglar введите Server URL, Username и Password от вашего аккаунта Emby.

---

## Структура проекта

```
emby-core/          # Платформонезависимая логика (JAR)
emby-android/       # Android-обёртка (APK)
local.template.properties  # Шаблон конфигурации
```

---

## Версии

| Компонент | Версия |
|-----------|--------|
| Murglar Plugins API | 7.0 |
| Plugin version | 1 |
| Kotlin | 2.2.0 |
| Android Gradle Plugin | 8.2.2 |
