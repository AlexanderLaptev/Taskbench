- Android 8.0 и более поздние версии.
- Трёхслойная архитектура: UI, domain, data.
- Паттерн MVVM (model-view-view model).
	- UI-логика описана в самих экранах (`@Composable` функции).
	- Бизнес-логика описана во view model'ях. View model'и выступают в качестве посредников между UI и данными. Основной задачей view model'и является подготовка UI state — всех необходимых данных для отрисовки UI.
	- Слой данных обеспечивает долговременное хранение данных приложения. В зависимости от включённых модулей, соответствующие данные сохраняются либо в оперативной памяти (в случае mock-репозиториев), либо на сервере приложения (в случае network-репозиториев).

# Фреймворки и библиотеки

1. Android Jetpack — официальная вспомогательная библиотека для Android.
2. Jetpack Compose — графический интерфейс (GUI).
3. Koin — инъекция зависимостей.
4. Compose Destinations — навигация между экранами.
5. Retrofit — коммуникация с сервером по HTTP(S). Используется в слое данных для сетевых источников данных (data source).
6. Moshi — сериализация объектов в JSON (используется совместно с Retrofit).
7. OkHttp — HTTP-клиент для использования с Retrofit.
8. DataStore — асинхронная замена `SharedPreferences` (хранение настроек и JWT-токенов).

# Последовательность запуска

1. `MainActivity` настраивает `KoinApplication` (инъекция зависимостей) и `DestinationsNavHost` (осуществляет навигацию между экранами).
2. `DestinationsNavHost` загружает начальный экран — `SplashScreen`.
3. `SplashScreen` использует `BootstrapUseCase` для первоначальной загрузки приложения.
	1. `BootstrapUseCase` итерирует список всех `PreloadRepository` (задаётся в явном виде в модуле `domain.usecase.Module`). Для каждого репозитория вызывается метод `preload()`, после чего репозиторий должен выполнить первоначальную загрузку своих данных.
	2. При возникновении ошибки аутентификации во время  `preload()` приложение пытается обновить токен доступа (access token) и пробует совершить `preload()` ещё раз. Если даже после этого авторизация не удаётся, приложение переходит на экран входа/регистрации (`ui.login.LoginScreen`).
	3. В случае непредвиденной ошибки во время bootstrap'а пользователю выводится сообщение, и приложение закрывается.
4. После загрузки открывается экран создания задачи (`ui.create.TaskCreationScreen`). Пользователь может переходить между разными экранами с помощью навигационной панели (`ui.component.NavigationBar`) внизу экрана.

# Слои

## Верхний уровень

Пакет: `cs.vsu.taskbench`

1. `MainActivity` — главная активность (activity) приложения и единственная точка входа.
2. Пакет `util` — глобальные утилиты для использования во всех слоях.
	1. `Flows.kt` — утилиты для работы с потоками Kotlin.
	2. `HttpConstants.kt` — константы, используемые в протоколе HTTP.
	3. `JwtUtils.kt` — утилиты для работы с JWT-токенами.
	4. `Lipsum` — генератор превью-текста ("lorem ipsum"). Используется только для превью (`@Preview`).
	5. `MockRandom` — обёртка над стандартным генератором псевдослучайных чисел, позволяющая сбрасывать генератор к начальному состоянию. Используется только в mock-репозиториях.

## UI (графический интерфейс)

Пакет: `cs.vsu.taskbench.ui`

1. `component` — компоненты (диалоги, элементы управления и UI kit'а).
2. `create` — экран создания задачи.
3. `list` — экран списка задач.
4. `login` — экран регистрации и входа.
5. `settings` — экран настроек.
	1. `SettingsGraph` — навигационный граф для экрана настроек.
	2. `ScreenTransitions` — анимации перехода в меню настроек.
	3. `SettingsScreen` — экран настроек.
6. `theme` — стили приложения (анимации, цвета, тема).
7. `util` — утилиты для слоя UI.

Классы/объекты:

1. `Locales` — список локалей, используемых в приложении.
2. `Module` — определения модуля UI для инъекции зависимостей.
3. `ScreenTransitions` — глобальные переходы между экранами.
4. `SplashScreen` — приветственный экран.
5. `StatisticsScreen` — экран статистики.

## Domain

Пакет: `cs.vsu.taskbench.domain`

1. `model` — классы-модели.
2. `usecase` — use case'ы для использования в слое UI.
	1. `BootstrapUseCase` — осуществляет начальную подготовку приложения к работе.
	2. `Module` — определение модуля use case'ов для инъекции зависимостей.

## Data

Пакет: `cs.vsu.taskbench.data`

Под каждый модельный класс выделен отдельный вложенный пакет. Внутри пакета определён соответствующий интерфейс репозитория (например, `TaskRepository`), а также соответствующий mock-репозиторий (`FakeTaskRepository`).

В большинстве пакетов присутствует пакет `network`, в котором, как правило, находятся два класса: соответствующий сетевой data source (источник данных) и сетевой репозиторий.

1. `auth` — авторизация и аутентификация.
2. `category` — репозитории для работы с категориями.
3. `statisitcs` — репозитории для работы со статистикой.
4. `task` — репозитории для работы с задачами.
	1. `subtask` — репозитории для работы с подзадачами.
5. `user` — репозитории для работы с данными пользователя (электронная почта, статус премиум-подписки).

Классы/объекты:

1. `Module` — определение модуля данных для инъекции зависимостей. Создаёт экземпляры `Moshi` (сериализатор JSON) и `Retrofit`, а также инициализирует репозитории.
2. `PreloadRepository` — интерфейс для репозиториев, которые предварительно загружают данные при запуске приложения (при bootstrap'е).
